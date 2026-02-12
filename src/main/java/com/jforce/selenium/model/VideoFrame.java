package com.jforce.selenium.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Represents an extracted video frame
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoFrame {
    
    /**
     * Source video identifier or file path
     */
    private String videoId;
    
    /**
     * Frame number (0-based)
     */
    private long frameNumber;
    
    /**
     * Timestamp in video (seconds)
     */
    private double timestampSeconds;
    
    /**
     * Path to extracted frame image file
     */
    private Path framePath;
    
    /**
     * Frame image format (jpg, png, etc.)
     */
    private String imageFormat;
    
    /**
     * Frame width in pixels
     */
    private int width;
    
    /**
     * Frame height in pixels
     */
    private int height;
    
    /**
     * File size of extracted frame in bytes
     */
    private long fileSizeBytes;
    
    /**
     * When the frame was extracted
     */
    private LocalDateTime extractedAt;
    
    /**
     * Quality of extracted frame (1-100)
     */
    private int quality;
    
    /**
     * Additional metadata or description
     */
    private String metadata;
    
    /**
     * Get formatted timestamp string (HH:MM:SS.mmm)
     */
    public String getFormattedTimestamp() {
        int hours = (int) (timestampSeconds / 3600);
        int minutes = (int) ((timestampSeconds % 3600) / 60);
        double secs = timestampSeconds % 60;
        return String.format("%02d:%02d:%06.3f", hours, minutes, secs);
    }
    
    /**
     * Get frame filename without path
     */
    public String getFrameFilename() {
        return framePath != null ? framePath.getFileName().toString() : null;
    }
    
    /**
     * Get human-readable file size
     */
    public String getFormattedFileSize() {
        if (fileSizeBytes < 1024) {
            return fileSizeBytes + " B";
        } else if (fileSizeBytes < 1024 * 1024) {
            return String.format("%.1f KB", fileSizeBytes / 1024.0);
        } else {
            return String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024.0));
        }
    }
}