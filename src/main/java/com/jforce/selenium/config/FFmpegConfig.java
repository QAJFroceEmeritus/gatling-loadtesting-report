package com.jforce.selenium.config;

import com.jforce.selenium.model.VideoFrame;
import com.jforce.selenium.model.VideoMetadata;
import com.jforce.selenium.model.VideoProgress;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@Component
public final class FFmpegConfig {

    private static final int CONNECT_TIMEOUT_MS = 15_000;
    private static final int READ_TIMEOUT_MS = 60_000;
    private static final int BUFFER_SIZE = 256 * 1024; // 256 KiB
    
    // FFmpeg paths - adjust these based on your system
    private static final String FFMPEG_PATH = "/usr/bin/ffmpeg"; // or "ffmpeg" if in PATH
    private static final String FFPROBE_PATH = "/usr/bin/ffprobe"; // or "ffprobe" if in PATH

    private static FFmpeg ffmpeg;
    private static FFprobe ffprobe;
    
    static {
        try {
            ffmpeg = new FFmpeg(FFMPEG_PATH);
            ffprobe = new FFprobe(FFPROBE_PATH);
            log.info("FFmpeg initialized successfully");
        } catch (IOException e) {
            log.error("Failed to initialize FFmpeg: {}", e.getMessage());
            // Fallback to system PATH
            try {
                ffmpeg = new FFmpeg();
                ffprobe = new FFprobe();
                log.info("FFmpeg initialized from system PATH");
            } catch (IOException ex) {
                log.error("Failed to initialize FFmpeg from PATH: {}", ex.getMessage());
            }
        }
    }

    private FFmpegConfig() {}

    // Example usage
    public static void mainMethod() {
        String videoUrl = "https://assets-degree.emeritus.org/portal/async-content/889ec369-d3b1-4337-a44b-ce089902ee84.mp4";
        Path destination = Paths.get("target/video.mp4");
        
        try {
            // Download video with progress tracking
            downloadVideo(videoUrl, destination, true, progress -> {
                System.out.println("Download progress: " + progress + "%");
            });
            
            // Get video metadata
            VideoMetadata metadata = getVideoMetadata(destination);
            System.out.println("Video metadata: " + metadata);
            
            // Extract frame at 30 seconds
            VideoFrame frame = extractFrameAtTime(destination, 30.0, Paths.get("target/frame_30s.jpg"));
            System.out.println("Extracted frame: " + frame);
            
            // Extract multiple frames at intervals
            List<VideoFrame> frames = extractFramesAtIntervals(destination, 10.0, Paths.get("target/frames"));
            System.out.println("Extracted " + frames.size() + " frames");
            
            System.out.println("Video processing completed successfully!");
            
        } catch (IOException e) {
            System.err.println("Processing failed: " + e.getMessage());
        }
    }

    /**
     * Download video with basic progress callback
     */
    public static void downloadVideo(String videoUrl, Path destination) throws IOException {
        downloadVideo(videoUrl, destination, false, null);
    }

    /**
     * Download video with resume capability
     */
    public static void downloadVideo(String videoUrl, Path destination, boolean resume) throws IOException {
        downloadVideo(videoUrl, destination, resume, null);
    }

    /**
     * Download video with progress tracking
     */
    public static void downloadVideo(String videoUrl, Path destination, boolean resume, 
                                   Consumer<Double> progressCallback) throws IOException {
        if (videoUrl == null || videoUrl.isBlank()) {
            throw new IllegalArgumentException("videoUrl must not be null/blank");
        }
        if (destination == null) {
            throw new IllegalArgumentException("destination must not be null");
        }

        Files.createDirectories(destination.toAbsolutePath().getParent());

        long existingBytes = resume && Files.exists(destination) ? Files.size(destination) : 0L;

        HttpURLConnection.setFollowRedirects(true);
        HttpURLConnection connection = (HttpURLConnection) new URL(videoUrl).openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        if (existingBytes > 0) {
            connection.setRequestProperty("Range", "bytes=" + existingBytes + "-");
        }

        connection.connect();

        int status = connection.getResponseCode();
        boolean ok = status == HttpURLConnection.HTTP_OK;           // 200
        boolean partial = status == HttpURLConnection.HTTP_PARTIAL; // 206
        if (!(ok || partial)) {
            connection.disconnect();
            throw new IOException("Server returned HTTP " + status);
        }

        // Get content length for progress tracking
        long contentLength = connection.getContentLengthLong();
        long totalSize = contentLength + existingBytes;

        // If server ignored Range, start from scratch
        if (existingBytes > 0 && ok) {
            existingBytes = 0;
            totalSize = contentLength;
        }

        try (InputStream in = new BufferedInputStream(connection.getInputStream(), BUFFER_SIZE);
             OutputStream out = new BufferedOutputStream(
                     Files.newOutputStream(
                         destination,
                         existingBytes == 0
                             ? new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE}
                             : new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND}
                     ),
                     BUFFER_SIZE
             )
        ) {
            byte[] buffer = new byte[BUFFER_SIZE];
            long totalBytes = existingBytes;
            int bytesRead;
            long lastProgressUpdate = System.currentTimeMillis();
            
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
                
                // Update progress every 500ms
                if (progressCallback != null && totalSize > 0) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastProgressUpdate > 500) {
                        double progress = (double) totalBytes / totalSize * 100.0;
                        progressCallback.accept(progress);
                        lastProgressUpdate = currentTime;
                    }
                }
            }
            out.flush();
            
            // Final progress update
            if (progressCallback != null && totalSize > 0) {
                progressCallback.accept(100.0);
            }
            
            log.info("⬇️ Downloaded {} bytes", totalBytes);
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Get comprehensive video metadata using FFprobe
     */
    public static VideoMetadata getVideoMetadata(Path videoPath) throws IOException {
        if (ffprobe == null) {
            throw new IOException("FFprobe not available");
        }
        
        FFmpegProbeResult probeResult = ffprobe.probe(videoPath.toString());
        
        // Get video stream
        FFmpegStream videoStream = probeResult.getStreams().stream()
            .filter(stream -> stream.codec_type == FFmpegStream.CodecType.VIDEO)
            .findFirst()
            .orElseThrow(() -> new IOException("No video stream found"));
        
        // Get audio stream (optional)
        FFmpegStream audioStream = probeResult.getStreams().stream()
            .filter(stream -> stream.codec_type == FFmpegStream.CodecType.AUDIO)
            .findFirst()
            .orElse(null);
        
        VideoMetadata metadata = VideoMetadata.builder()
            .videoPath(videoPath)
            .durationSeconds(probeResult.getFormat().duration)
            .frameRate(videoStream.r_frame_rate.doubleValue())
            .width(videoStream.width)
            .height(videoStream.height)
            .videoCodec(videoStream.codec_name)
            .videoBitrate(videoStream.bit_rate)
            .fileSizeBytes(Files.size(videoPath))
            .format(probeResult.getFormat().format_name)
            .extractedAt(LocalDateTime.now())
            .build();
        
        if (audioStream != null) {
            metadata.setAudioCodec(audioStream.codec_name);
            metadata.setAudioBitrate(audioStream.bit_rate);
        }
        
        metadata.calculateTotalFrames();
        
        return metadata;
    }

    /**
     * Extract a single frame at specific timestamp
     */
    public static VideoFrame extractFrameAtTime(Path videoPath, double timestampSeconds, Path outputPath) 
            throws IOException {
        return extractFrameAtTime(videoPath, timestampSeconds, outputPath, "jpg", 90);
    }

    /**
     * Extract a single frame at specific timestamp with quality control
     */
    public static VideoFrame extractFrameAtTime(Path videoPath, double timestampSeconds, Path outputPath, 
                                              String format, int quality) throws IOException {
        if (ffmpeg == null) {
            throw new IOException("FFmpeg not available");
        }
        
        // Ensure output directory exists
        Files.createDirectories(outputPath.getParent());
        
        // Build FFmpeg command for frame extraction
        FFmpegBuilder builder = new FFmpegBuilder()
            .setInput(videoPath.toString())
            .addOutput(outputPath.toString())
            .setVideoFrameRate(1)  // Extract 1 frame
            .setFrames(1)          // Only 1 frame
            .setStartOffset((long)(timestampSeconds * 1000), TimeUnit.MILLISECONDS)
            .setFormat(format)
            .done();
        
        // Add quality settings for JPEG
        if ("jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)) {
            builder.addExtraArgs("-q:v", String.valueOf(Math.max(1, Math.min(31, 31 - (quality * 30 / 100)))));
        }
        
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        FFmpegJob job = executor.createJob(builder);
        job.run();
        
        // Get video metadata for frame info
        VideoMetadata metadata = getVideoMetadata(videoPath);
        
        return VideoFrame.builder()
            .videoId(videoPath.toString())
            .frameNumber(Math.round(timestampSeconds * metadata.getFrameRate()))
            .timestampSeconds(timestampSeconds)
            .framePath(outputPath)
            .imageFormat(format)
            .width(metadata.getWidth())
            .height(metadata.getHeight())
            .fileSizeBytes(Files.size(outputPath))
            .extractedAt(LocalDateTime.now())
            .quality(quality)
            .build();
    }

    /**
     * Extract frames at regular intervals
     */
    public static List<VideoFrame> extractFramesAtIntervals(Path videoPath, double intervalSeconds, Path outputDir) 
            throws IOException {
        VideoMetadata metadata = getVideoMetadata(videoPath);
        List<VideoFrame> frames = new ArrayList<>();
        
        Files.createDirectories(outputDir);
        
        double currentTime = 0;
        int frameIndex = 0;
        
        while (currentTime < metadata.getDurationSeconds()) {
            String filename = String.format("frame_%04d_%.2fs.jpg", frameIndex, currentTime);
            Path framePath = outputDir.resolve(filename);
            
            VideoFrame frame = extractFrameAtTime(videoPath, currentTime, framePath);
            frames.add(frame);
            
            currentTime += intervalSeconds;
            frameIndex++;
        }
        
        log.info("Extracted {} frames at {}-second intervals", frames.size(), intervalSeconds);
        return frames;
    }

    /**
     * Create video progress tracker for playback simulation
     */
    public static VideoProgress createProgressTracker(Path videoPath) throws IOException {
        VideoMetadata metadata = getVideoMetadata(videoPath);
        
        return VideoProgress.builder()
            .videoId(videoPath.toString())
            .currentTimeSeconds(0.0)
            .totalDurationSeconds(metadata.getDurationSeconds())
            .progressPercentage(0.0)
            .currentFrame(0)
            .totalFrames(metadata.getTotalFrames())
            .frameRate(metadata.getFrameRate())
            .lastUpdated(LocalDateTime.now())
            .isPlaying(false)
            .width(metadata.getWidth())
            .height(metadata.getHeight())
            .build();
    }

    /**
     * Update video progress (simulate playback)
     */
    public static VideoProgress updateProgress(VideoProgress progress, double newTimeSeconds) {
        progress.setCurrentTimeSeconds(Math.max(0, Math.min(newTimeSeconds, progress.getTotalDurationSeconds())));
        progress.calculateProgressPercentage();
        progress.calculateCurrentFrame();
        progress.setLastUpdated(LocalDateTime.now());
        
        return progress;
    }

    /**
     * Extract frame at current progress position
     */
    public static VideoFrame extractFrameAtProgress(Path videoPath, VideoProgress progress, Path outputPath) 
            throws IOException {
        return extractFrameAtTime(videoPath, progress.getCurrentTimeSeconds(), outputPath);
    }

    /**
     * Generate video thumbnail (frame at 10% of duration)
     */
    public static VideoFrame generateThumbnail(Path videoPath, Path thumbnailPath) throws IOException {
        VideoMetadata metadata = getVideoMetadata(videoPath);
        double thumbnailTime = metadata.getDurationSeconds() * 0.1; // 10% into video
        
        return extractFrameAtTime(videoPath, thumbnailTime, thumbnailPath, "jpg", 85);
    }

    /**
     * Extract frames for video preview (multiple thumbnails)
     */
    public static List<VideoFrame> generatePreviewFrames(Path videoPath, Path outputDir, int frameCount) 
            throws IOException {
        VideoMetadata metadata = getVideoMetadata(videoPath);
        List<VideoFrame> frames = new ArrayList<>();
        
        Files.createDirectories(outputDir);
        
        double interval = metadata.getDurationSeconds() / (frameCount + 1);
        
        for (int i = 1; i <= frameCount; i++) {
            double timestamp = interval * i;
            String filename = String.format("preview_%02d.jpg", i);
            Path framePath = outputDir.resolve(filename);
            
            VideoFrame frame = extractFrameAtTime(videoPath, timestamp, framePath, "jpg", 80);
            frames.add(frame);
        }
        
        log.info("Generated {} preview frames", frames.size());
        return frames;
    }
}