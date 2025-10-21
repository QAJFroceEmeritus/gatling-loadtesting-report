package com.jforce.selenium.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Represents video file metadata information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoMetadata {
    
    /**
     * Video file path
     */
    private Path videoPath;
    
    /**
     * Video duration in seconds
     */
    private double durationSeconds;
    
    /**
     * Video frame rate (fps)
     */
    private double frameRate;
    
    /**
     * Video width in pixels
     */
    private int width;
    
    /**
     * Video height in pixels
     */
    private int height;
    
    /**
     * Video codec
     */
    private String videoCodec;
    
    /**
     * Audio codec
     */
    private String audioCodec;
    
    /**
     * Video bitrate in bps
     */
    private long videoBitrate;
    
    /**
     * Audio bitrate in bps
     */
    private long audioBitrate;
    
    /**
     * File size in bytes
     */
    private long fileSizeBytes;
    
    /**
     * Total number of frames
     */
    private long totalFrames;
    
    /**
     * Video format/container
     */
    private String format;
    
    /**
     * When metadata was extracted
     */
    private LocalDateTime extractedAt;
    
    /**
     * Calculate total frames based on duration and frame rate
     */
    public void calculateTotalFrames() {
        if (frameRate > 0 && durationSeconds > 0) {
            this.totalFrames = Math.round(durationSeconds * frameRate);
        }
    }
    
    /**
     * Get formatted duration string (HH:MM:SS)
     */
    public String getFormattedDuration() {
        int hours = (int) (durationSeconds / 3600);
        int minutes = (int) ((durationSeconds % 3600) / 60);
        int secs = (int) (durationSeconds % 60);
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
    
    /**
     * Get video resolution as string
     */
    public String getResolution() {
        return width + "x" + height;
    }
    
    /**
     * Get human-readable file size
     */
    public String getFormattedFileSize() {
        if (fileSizeBytes < 1024) {
            return fileSizeBytes + " B";
        } else if (fileSizeBytes < 1024 * 1024) {
            return String.format("%.1f KB", fileSizeBytes / 1024.0);
        } else if (fileSizeBytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", fileSizeBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}