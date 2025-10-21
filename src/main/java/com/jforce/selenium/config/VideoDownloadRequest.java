package com.jforce.selenium.config;

import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

public final class VideoDownloadRequest {
    private final URL videoUrl;
    private final Path destination;
    private final boolean resume;

    private VideoDownloadRequest(Builder builder) {
        this.videoUrl = builder.videoUrl;
        this.destination = builder.destination;
        this.resume = builder.resume;
    }

    public URL getVideoUrl() {
        return videoUrl;
    }

    public Path getDestination() {
        return destination;
    }

    public boolean isResume() {
        return resume;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private URL videoUrl;
        private Path destination;
        private boolean resume = true; // default to resume to save bandwidth

        public Builder videoUrl(URL videoUrl) {
            this.videoUrl = videoUrl;
            return this;
        }

        public Builder destination(Path destination) {
            this.destination = destination;
            return this;
        }

        public Builder resume(boolean resume) {
            this.resume = resume;
            return this;
        }

        public VideoDownloadRequest build() {
            Objects.requireNonNull(videoUrl, "videoUrl must not be null");
            Objects.requireNonNull(destination, "destination must not be null");
            return new VideoDownloadRequest(this);
        }
    }
}
