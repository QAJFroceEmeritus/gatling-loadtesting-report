package com.jforce.selenium.service;

import com.jforce.selenium.config.FFmpegConfig;
import com.jforce.selenium.model.VideoFrame;
import com.jforce.selenium.model.VideoMetadata;
import com.jforce.selenium.model.VideoProgress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Service for managing video operations, progress tracking, and frame extraction
 */
@Slf4j
@Service
public class VideoService {

    private final Map<String, VideoProgress> activeProgressTrackers = new ConcurrentHashMap<>();
    private final Map<String, VideoMetadata> videoMetadataCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    /**
     * Download video with progress tracking and automatic resume capability
     */
    public void downloadVideoWithProgress(String videoUrl, Path destination, 
                                        Consumer<VideoProgress> progressCallback) throws IOException {
        String videoId = destination.toString();
        
        // Create initial progress tracker
        VideoProgress progress = VideoProgress.builder()
            .videoId(videoId)
            .currentTimeSeconds(0.0)
            .progressPercentage(0.0)
            .lastUpdated(LocalDateTime.now())
            .isPlaying(false)
            .build();
        
        activeProgressTrackers.put(videoId, progress);
        
        try {
            FFmpegConfig.downloadVideo(videoUrl, destination, true, downloadProgress -> {
                progress.setProgressPercentage(downloadProgress);
                progress.setLastUpdated(LocalDateTime.now());
                
                if (progressCallback != null) {
                    progressCallback.accept(progress);
                }
            });
            
            // After download, get video metadata and update progress
            VideoMetadata metadata = getVideoMetadata(destination);
            progress.setTotalDurationSeconds(metadata.getDurationSeconds());
            progress.setFrameRate(metadata.getFrameRate());
            progress.setWidth(metadata.getWidth());
            progress.setHeight(metadata.getHeight());
            progress.calculateTotalFrames();
            
            log.info("Video download completed: {}", destination);
            
        } finally {
            activeProgressTrackers.remove(videoId);
        }
    }

    /**
     * Get or retrieve video metadata with caching
     */
    public VideoMetadata getVideoMetadata(Path videoPath) throws IOException {
        String key = videoPath.toString();
        
        VideoMetadata cached = videoMetadataCache.get(key);
        if (cached != null && Files.exists(videoPath)) {
            // Check if file was modified since metadata was cached
            try {
                long currentSize = Files.size(videoPath);
                if (currentSize == cached.getFileSizeBytes()) {
                    return cached;
                }
            } catch (IOException e) {
                // File might not exist anymore, remove from cache
                videoMetadataCache.remove(key);
            }
        }
        
        // Get fresh metadata
        VideoMetadata metadata = FFmpegConfig.getVideoMetadata(videoPath);
        videoMetadataCache.put(key, metadata);
        
        return metadata;
    }

    /**
     * Start video playback simulation with progress tracking
     */
    public VideoProgress startVideoPlayback(Path videoPath) throws IOException {
        VideoProgress progress = FFmpegConfig.createProgressTracker(videoPath);
        progress.setPlaying(true);
        
        String videoId = videoPath.toString();
        activeProgressTrackers.put(videoId, progress);
        
        // Start progress simulation (for demonstration)
        scheduler.scheduleAtFixedRate(() -> {
            VideoProgress currentProgress = activeProgressTrackers.get(videoId);
            if (currentProgress != null && currentProgress.isPlaying()) {
                double newTime = currentProgress.getCurrentTimeSeconds() + 1.0; // Advance 1 second
                if (newTime >= currentProgress.getTotalDurationSeconds()) {
                    // Video finished
                    currentProgress.setPlaying(false);
                    currentProgress.setCurrentTimeSeconds(currentProgress.getTotalDurationSeconds());
                } else {
                    currentProgress.setCurrentTimeSeconds(newTime);
                }
                
                currentProgress.calculateProgressPercentage();
                currentProgress.calculateCurrentFrame();
                currentProgress.setLastUpdated(LocalDateTime.now());
            }
        }, 1, 1, TimeUnit.SECONDS);
        
        log.info("Started video playback simulation: {}", videoPath);
        return progress;
    }

    /**
     * Pause video playback
     */
    public VideoProgress pauseVideoPlayback(String videoId) {
        VideoProgress progress = activeProgressTrackers.get(videoId);
        if (progress != null) {
            progress.setPlaying(false);
            progress.setLastUpdated(LocalDateTime.now());
            log.info("Paused video playback: {}", videoId);
        }
        return progress;
    }

    /**
     * Resume video playback
     */
    public VideoProgress resumeVideoPlayback(String videoId) {
        VideoProgress progress = activeProgressTrackers.get(videoId);
        if (progress != null) {
            progress.setPlaying(true);
            progress.setLastUpdated(LocalDateTime.now());
            log.info("Resumed video playback: {}", videoId);
        }
        return progress;
    }

    /**
     * Seek to specific time in video
     */
    public VideoProgress seekToTime(String videoId, double timeSeconds) {
        VideoProgress progress = activeProgressTrackers.get(videoId);
        if (progress != null) {
            FFmpegConfig.updateProgress(progress, timeSeconds);
            log.info("Seeked to {}s in video: {}", timeSeconds, videoId);
        }
        return progress;
    }

    /**
     * Get current playback progress
     */
    public VideoProgress getPlaybackProgress(String videoId) {
        return activeProgressTrackers.get(videoId);
    }

    /**
     * Stop video playback and cleanup
     */
    public void stopVideoPlayback(String videoId) {
        VideoProgress progress = activeProgressTrackers.remove(videoId);
        if (progress != null) {
            progress.setPlaying(false);
            log.info("Stopped video playback: {}", videoId);
        }
    }

    /**
     * Extract frame at current playback position
     */
    public VideoFrame extractCurrentFrame(String videoId, Path outputPath) throws IOException {
        VideoProgress progress = activeProgressTrackers.get(videoId);
        if (progress == null) {
            throw new IllegalStateException("No active playback for video: " + videoId);
        }
        
        Path videoPath = Paths.get(progress.getVideoId());
        return FFmpegConfig.extractFrameAtProgress(videoPath, progress, outputPath);
    }

    /**
     * Extract frame at specific timestamp
     */
    public VideoFrame extractFrameAtTimestamp(Path videoPath, double timestampSeconds, Path outputPath) 
            throws IOException {
        return FFmpegConfig.extractFrameAtTime(videoPath, timestampSeconds, outputPath);
    }

    /**
     * Generate video thumbnails at multiple points
     */
    public List<VideoFrame> generateVideoThumbnails(Path videoPath, Path outputDir, int count) 
            throws IOException {
        return FFmpegConfig.generatePreviewFrames(videoPath, outputDir, count);
    }

    /**
     * Extract frames at regular intervals for video analysis
     */
    public List<VideoFrame> extractFramesForAnalysis(Path videoPath, double intervalSeconds, Path outputDir) 
            throws IOException {
        return FFmpegConfig.extractFramesAtIntervals(videoPath, intervalSeconds, outputDir);
    }

    /**
     * Create video summary with key frames
     */
    public VideoSummary createVideoSummary(Path videoPath, Path outputDir) throws IOException {
        VideoMetadata metadata = getVideoMetadata(videoPath);
        
        // Generate thumbnail (10% into video)
        VideoFrame thumbnail = FFmpegConfig.generateThumbnail(videoPath, 
            outputDir.resolve("thumbnail.jpg"));
        
        // Generate preview frames (5 frames evenly distributed)
        List<VideoFrame> previewFrames = generateVideoThumbnails(videoPath, 
            outputDir.resolve("previews"), 5);
        
        return VideoSummary.builder()
            .metadata(metadata)
            .thumbnail(thumbnail)
            .previewFrames(previewFrames)
            .createdAt(LocalDateTime.now())
            .build();
    }

    /**
     * Get all active video playback sessions
     */
    public Map<String, VideoProgress> getActivePlaybackSessions() {
        return Map.copyOf(activeProgressTrackers);
    }

    /**
     * Clean up resources
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        activeProgressTrackers.clear();
        videoMetadataCache.clear();
        log.info("VideoService shutdown completed");
    }

    /**
     * Video summary containing metadata and key frames
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class VideoSummary {
        private VideoMetadata metadata;
        private VideoFrame thumbnail;
        private List<VideoFrame> previewFrames;
        private LocalDateTime createdAt;
        
        public int getTotalPreviewFrames() {
            return previewFrames != null ? previewFrames.size() : 0;
        }
        
        public String getSummaryInfo() {
            if (metadata == null) return "No metadata available";
            
            return String.format("Video: %s, Duration: %s, Resolution: %s, %d preview frames",
                metadata.getVideoPath().getFileName(),
                metadata.getFormattedDuration(),
                metadata.getResolution(),
                getTotalPreviewFrames());
        }
    }
}