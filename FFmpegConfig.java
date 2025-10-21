package com.jforce.selenium.config;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FFmpegConfig {
    
    // Configuration constants for better performance
    private static final int BUFFER_SIZE = 64 * 1024; // 64KB buffer for better performance
    private static final int CONNECTION_TIMEOUT = 30000; // 30 seconds
    private static final int READ_TIMEOUT = 60000; // 60 seconds
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    
    // Thread pool for async downloads
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    
    /**
     * Main method to demonstrate video download functionality
     */
    public static void main(String[] args) {
        String videoUrl = "https://example.com/video.mp4"; // your dynamic video URL
        Path destination = Paths.get("target/video.mp4");
        
        try {
            downloadVideo(videoUrl, destination);
            System.out.println("✅ Video downloaded successfully: " + destination.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("❌ Download failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Downloads a video from URL with progress tracking and optimized performance
     * @param videoUrl The URL of the video to download
     * @param destination The local path where the video will be saved
     * @throws IOException if download fails
     */
    public static void downloadVideo(String videoUrl, Path destination) throws IOException {
        downloadVideoWithProgress(videoUrl, destination, null);
    }
    
    /**
     * Downloads a video with progress callback for monitoring
     * @param videoUrl The URL of the video to download
     * @param destination The local path where the video will be saved
     * @param progressCallback Optional callback to track download progress
     * @throws IOException if download fails
     */
    public static void downloadVideoWithProgress(String videoUrl, Path destination, ProgressCallback progressCallback) throws IOException {
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Video URL cannot be null or empty");
        }
        
        if (destination == null) {
            throw new IllegalArgumentException("Destination path cannot be null");
        }
        
        URL url = new URL(videoUrl);
        HttpURLConnection connection = null;
        
        try {
            connection = (HttpURLConnection) url.openConnection();
            
            // Set connection properties for better performance and compatibility
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Accept-Encoding", "identity"); // Prevent compression for video files
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setInstanceFollowRedirects(true);
            
            // If authentication/session cookies are needed:
            // connection.setRequestProperty("Cookie", "SESSION=your_session_cookie_here");
            
            connection.connect();
            
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Server returned HTTP " + responseCode + " " + connection.getResponseMessage());
            }
            
            // Get content length for progress tracking
            long contentLength = connection.getContentLengthLong();
            String contentType = connection.getContentType();
            
            System.out.println("📥 Starting download...");
            System.out.println("📊 Content-Type: " + (contentType != null ? contentType : "Unknown"));
            System.out.println("📏 Content-Length: " + (contentLength > 0 ? formatBytes(contentLength) : "Unknown"));
            
            // Ensure parent directories exist
            Files.createDirectories(destination.getParent());
            
            // Use larger buffer and track progress
            try (InputStream in = new BufferedInputStream(connection.getInputStream(), BUFFER_SIZE);
                 OutputStream out = new BufferedOutputStream(
                     Files.newOutputStream(destination, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), 
                     BUFFER_SIZE)) {
                
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                long totalBytes = 0;
                long startTime = System.currentTimeMillis();
                long lastProgressTime = startTime;
                
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                    
                    // Progress reporting every 500ms
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastProgressTime >= 500) {
                        double progress = contentLength > 0 ? (double) totalBytes / contentLength * 100 : 0;
                        double speed = totalBytes / ((currentTime - startTime) / 1000.0); // bytes per second
                        
                        System.out.printf("\r⬇️  Progress: %s / %s (%.1f%%) - Speed: %s/s", 
                            formatBytes(totalBytes),
                            contentLength > 0 ? formatBytes(contentLength) : "Unknown",
                            progress,
                            formatBytes((long) speed)
                        );
                        
                        if (progressCallback != null) {
                            progressCallback.onProgress(totalBytes, contentLength, progress);
                        }
                        
                        lastProgressTime = currentTime;
                    }
                }
                
                // Final progress update
                long duration = System.currentTimeMillis() - startTime;
                double avgSpeed = totalBytes / (duration / 1000.0);
                
                System.out.printf("\n✅ Downloaded %s in %s (avg speed: %s/s)\n", 
                    formatBytes(totalBytes),
                    formatDuration(duration),
                    formatBytes((long) avgSpeed)
                );
                
                if (progressCallback != null) {
                    progressCallback.onComplete(totalBytes, duration);
                }
            }
            
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    /**
     * Asynchronous video download method
     * @param videoUrl The URL of the video to download
     * @param destination The local path where the video will be saved
     * @return CompletableFuture that completes when download finishes
     */
    public static CompletableFuture<Void> downloadVideoAsync(String videoUrl, Path destination) {
        return CompletableFuture.runAsync(() -> {
            try {
                downloadVideo(videoUrl, destination);
            } catch (IOException e) {
                throw new RuntimeException("Async download failed", e);
            }
        }, executor);
    }
    
    /**
     * Download multiple videos concurrently
     * @param videoUrls Array of video URLs to download
     * @param destinationDir Directory where videos will be saved
     * @return CompletableFuture that completes when all downloads finish
     */
    public static CompletableFuture<Void> downloadMultipleVideos(String[] videoUrls, Path destinationDir) {
        CompletableFuture<Void>[] futures = new CompletableFuture[videoUrls.length];
        
        for (int i = 0; i < videoUrls.length; i++) {
            String url = videoUrls[i];
            String filename = "video_" + (i + 1) + ".mp4";
            Path destination = destinationDir.resolve(filename);
            futures[i] = downloadVideoAsync(url, destination);
        }
        
        return CompletableFuture.allOf(futures);
    }
    
    /**
     * Formats bytes into human-readable format
     */
    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    /**
     * Formats duration into human-readable format
     */
    private static String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        if (seconds < 60) return seconds + "s";
        long minutes = seconds / 60;
        seconds = seconds % 60;
        if (minutes < 60) return String.format("%dm %ds", minutes, seconds);
        long hours = minutes / 60;
        minutes = minutes % 60;
        return String.format("%dh %dm %ds", hours, minutes, seconds);
    }
    
    /**
     * Callback interface for progress tracking
     */
    public interface ProgressCallback {
        void onProgress(long bytesDownloaded, long totalBytes, double percentage);
        void onComplete(long totalBytes, long durationMs);
    }
    
    /**
     * Shutdown the executor service (call this when your application shuts down)
     */
    public static void shutdown() {
        executor.shutdown();
    }
}