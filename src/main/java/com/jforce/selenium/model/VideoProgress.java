package com.jforce.selenium.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents video playback progress information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoProgress {
    
    /**
     * Video identifier or file path
     */
    private String videoId;
    
    /**
     * Current playback position in seconds
     */
    private double currentTimeSeconds;
    
    /**
     * Total video duration in seconds
     */
    private double totalDurationSeconds;
    
    /**
     * Playback progress as percentage (0-100)
     */
    private double progressPercentage;
    
    /**
     * Current frame number
     */
    private long currentFrame;
    
    /**
     * Total number of frames
     */
    private long totalFrames;
    
    /**
     * Video frame rate (fps)
     */
    private double frameRate;
    
    /**
     * Timestamp when progress was last updated
     */
    private LocalDateTime lastUpdated;
    
    /**
     * Whether video is currently playing
     */
    private boolean isPlaying;
    
    /**
     * Video resolution width
     */
    private int width;
    
    /**
     * Video resolution height
     */
    private int height;
    
    /**
     * Calculate progress percentage based on current time and duration
     */
    public void calculateProgressPercentage() {
        if (totalDurationSeconds > 0) {
            this.progressPercentage = (currentTimeSeconds / totalDurationSeconds) * 100.0;
        } else {
            this.progressPercentage = 0.0;
        }
    }
    
    /**
     * Calculate current frame based on time and frame rate
     */
    public void calculateCurrentFrame() {
        if (frameRate > 0) {
            this.currentFrame = Math.round(currentTimeSeconds * frameRate);
        }
    }
    
    /**
     * Calculate total frames based on duration and frame rate
     */
    public void calculateTotalFrames() {
        if (frameRate > 0 && totalDurationSeconds > 0) {
            this.totalFrames = Math.round(totalDurationSeconds * frameRate);
        }
    }
    
    /**
     * Get formatted time string (HH:MM:SS)
     */
    public String getFormattedCurrentTime() {
        return formatSeconds(currentTimeSeconds);
    }
    
    /**
     * Get formatted duration string (HH:MM:SS)
     */
    public String getFormattedDuration() {
        return formatSeconds(totalDurationSeconds);
    }
    
    private String formatSeconds(double seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        int secs = (int) (seconds % 60);
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}