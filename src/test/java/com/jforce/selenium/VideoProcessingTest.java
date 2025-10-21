package com.jforce.selenium;

import com.jforce.selenium.model.VideoMetadata;
import com.jforce.selenium.model.VideoProgress;
import com.jforce.selenium.service.VideoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for video processing functionality
 */
@SpringBootTest
public class VideoProcessingTest {

    @TempDir
    Path tempDir;

    @Test
    public void testVideoProgressCreation() {
        // Test creating a video progress tracker
        VideoProgress progress = VideoProgress.builder()
            .videoId("test-video")
            .currentTimeSeconds(30.0)
            .totalDurationSeconds(120.0)
            .frameRate(30.0)
            .build();
        
        progress.calculateProgressPercentage();
        progress.calculateCurrentFrame();
        progress.calculateTotalFrames();
        
        assertEquals(25.0, progress.getProgressPercentage(), 0.1);
        assertEquals(900, progress.getCurrentFrame());
        assertEquals(3600, progress.getTotalFrames());
    }

    @Test
    public void testVideoProgressFormatting() {
        VideoProgress progress = VideoProgress.builder()
            .currentTimeSeconds(3661.5) // 1 hour, 1 minute, 1.5 seconds
            .totalDurationSeconds(7323.0) // 2 hours, 2 minutes, 3 seconds
            .build();
        
        assertEquals("01:01:01", progress.getFormattedCurrentTime());
        assertEquals("02:02:03", progress.getFormattedDuration());
    }

    @Test
    public void testVideoService() {
        VideoService videoService = new VideoService();
        
        // Test service initialization
        assertNotNull(videoService);
        
        // Test getting active sessions (should be empty initially)
        assertTrue(videoService.getActivePlaybackSessions().isEmpty());
        
        // Cleanup
        videoService.shutdown();
    }

    @Test
    public void testVideoMetadataBuilder() {
        VideoMetadata metadata = VideoMetadata.builder()
            .videoPath(Paths.get("test-video.mp4"))
            .durationSeconds(300.0)
            .frameRate(25.0)
            .width(1920)
            .height(1080)
            .videoCodec("h264")
            .format("mp4")
            .fileSizeBytes(50_000_000L)
            .build();
        
        metadata.calculateTotalFrames();
        
        assertEquals("1920x1080", metadata.getResolution());
        assertEquals("00:05:00", metadata.getFormattedDuration());
        assertEquals(7500, metadata.getTotalFrames());
        assertEquals("47.7 MB", metadata.getFormattedFileSize());
    }
}