package com.jforce.selenium;

import com.jforce.selenium.model.VideoFrame;
import com.jforce.selenium.model.VideoMetadata;
import com.jforce.selenium.model.VideoProgress;
import com.jforce.selenium.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * Main application demonstrating video processing capabilities
 */
@Slf4j
@SpringBootApplication
public class VideoProcessingApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoProcessingApplication.class, args);
    }

    @Bean
    public CommandLineRunner videoProcessingDemo(VideoService videoService) {
        return args -> {
            log.info("🎬 Starting Video Processing Demo");
            
            // Example video URL and paths
            String videoUrl = "https://assets-degree.emeritus.org/portal/async-content/889ec369-d3b1-4337-a44b-ce089902ee84.mp4";
            Path videoPath = Paths.get("target/demo-video.mp4");
            Path framesDir = Paths.get("target/frames");
            Path thumbnailsDir = Paths.get("target/thumbnails");
            
            try {
                // 1. Download video with progress tracking
                log.info("📥 Downloading video...");
                videoService.downloadVideoWithProgress(videoUrl, videoPath, progress -> {
                    if (progress.getProgressPercentage() % 10 == 0) { // Log every 10%
                        log.info("Download progress: {:.1f}%", progress.getProgressPercentage());
                    }
                });
                
                // 2. Get video metadata
                log.info("📊 Analyzing video metadata...");
                VideoMetadata metadata = videoService.getVideoMetadata(videoPath);
                log.info("Video Info: {} | Duration: {} | Resolution: {} | Format: {}", 
                    metadata.getVideoPath().getFileName(),
                    metadata.getFormattedDuration(),
                    metadata.getResolution(),
                    metadata.getFormat());
                
                // 3. Generate video summary with thumbnails
                log.info("🖼️ Generating video summary...");
                VideoService.VideoSummary summary = videoService.createVideoSummary(videoPath, thumbnailsDir);
                log.info("Summary: {}", summary.getSummaryInfo());
                
                // 4. Start interactive demo
                runInteractiveDemo(videoService, videoPath, framesDir);
                
            } catch (Exception e) {
                log.error("❌ Demo failed: {}", e.getMessage(), e);
            } finally {
                videoService.shutdown();
            }
        };
    }
    
    private void runInteractiveDemo(VideoService videoService, Path videoPath, Path framesDir) {
        Scanner scanner = new Scanner(System.in);
        
        try {
            // Start video playback simulation
            log.info("▶️ Starting video playback simulation...");
            VideoProgress progress = videoService.startVideoPlayback(videoPath);
            String videoId = progress.getVideoId();
            
            log.info("\n🎮 Interactive Video Control Demo");
            log.info("Commands: [p]lay/pause, [s]eek <time>, [f]rame, [t]humbnails, [q]uit");
            
            boolean running = true;
            while (running) {
                // Show current progress
                VideoProgress currentProgress = videoService.getPlaybackProgress(videoId);
                if (currentProgress != null) {
                    log.info("⏱️ Progress: {} / {} ({:.1f}%) | Frame: {} | Status: {}", 
                        currentProgress.getFormattedCurrentTime(),
                        currentProgress.getFormattedDuration(),
                        currentProgress.getProgressPercentage(),
                        currentProgress.getCurrentFrame(),
                        currentProgress.isPlaying() ? "Playing" : "Paused");
                }
                
                System.out.print("Enter command: ");
                String input = scanner.nextLine().trim().toLowerCase();
                
                switch (input.charAt(0)) {
                    case 'p': // Play/Pause toggle
                        if (currentProgress != null && currentProgress.isPlaying()) {
                            videoService.pauseVideoPlayback(videoId);
                            log.info("⏸️ Video paused");
                        } else {
                            videoService.resumeVideoPlayback(videoId);
                            log.info("▶️ Video resumed");
                        }
                        break;
                        
                    case 's': // Seek to time
                        try {
                            String[] parts = input.split("\\s+");
                            if (parts.length > 1) {
                                double seekTime = Double.parseDouble(parts[1]);
                                videoService.seekToTime(videoId, seekTime);
                                log.info("⏩ Seeked to {}s", seekTime);
                            } else {
                                log.info("Usage: s <time_in_seconds>");
                            }
                        } catch (NumberFormatException e) {
                            log.info("Invalid time format. Usage: s <time_in_seconds>");
                        }
                        break;
                        
                    case 'f': // Extract current frame
                        try {
                            String frameFilename = String.format("current_frame_%.2fs.jpg", 
                                currentProgress.getCurrentTimeSeconds());
                            Path framePath = framesDir.resolve(frameFilename);
                            
                            VideoFrame frame = videoService.extractCurrentFrame(videoId, framePath);
                            log.info("📸 Extracted frame: {} ({})", 
                                frame.getFrameFilename(), frame.getFormattedFileSize());
                        } catch (Exception e) {
                            log.error("Failed to extract frame: {}", e.getMessage());
                        }
                        break;
                        
                    case 't': // Generate thumbnails
                        try {
                            log.info("🖼️ Generating thumbnails...");
                            List<VideoFrame> thumbnails = videoService.generateVideoThumbnails(
                                videoPath, framesDir.resolve("thumbnails"), 10);
                            log.info("Generated {} thumbnails", thumbnails.size());
                        } catch (Exception e) {
                            log.error("Failed to generate thumbnails: {}", e.getMessage());
                        }
                        break;
                        
                    case 'q': // Quit
                        running = false;
                        videoService.stopVideoPlayback(videoId);
                        log.info("👋 Demo ended");
                        break;
                        
                    default:
                        log.info("Unknown command. Available: [p]lay/pause, [s]eek <time>, [f]rame, [t]humbnails, [q]uit");
                }
                
                // Small delay to prevent spam
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
        } catch (Exception e) {
            log.error("Interactive demo error: {}", e.getMessage());
        } finally {
            scanner.close();
        }
    }
}