package com.jforce.selenium.service;

import com.jforce.selenium.model.VideoProgress;

/**
 * Interface for listening to video progress updates
 */
@FunctionalInterface
public interface VideoProgressListener {
    
    /**
     * Called when video progress is updated
     * 
     * @param progress Current video progress information
     */
    void onProgressUpdate(VideoProgress progress);
    
    /**
     * Called when video playback starts (optional override)
     */
    default void onPlaybackStarted(VideoProgress progress) {
        // Default implementation does nothing
    }
    
    /**
     * Called when video playback is paused (optional override)
     */
    default void onPlaybackPaused(VideoProgress progress) {
        // Default implementation does nothing
    }
    
    /**
     * Called when video playback is resumed (optional override)
     */
    default void onPlaybackResumed(VideoProgress progress) {
        // Default implementation does nothing
    }
    
    /**
     * Called when video playback is stopped (optional override)
     */
    default void onPlaybackStopped(VideoProgress progress) {
        // Default implementation does nothing
    }
    
    /**
     * Called when video playback reaches the end (optional override)
     */
    default void onPlaybackCompleted(VideoProgress progress) {
        // Default implementation does nothing
    }
    
    /**
     * Called when seeking to a new position (optional override)
     */
    default void onSeek(VideoProgress progress, double previousTime, double newTime) {
        // Default implementation does nothing
    }
}