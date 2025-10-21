package com.jforce.selenium.config;

import java.io.IOException;

public interface VideoDownloader {
    /**
     * Downloads a video according to the provided request.
     * @return total bytes present on disk after completion (including any resumed bytes)
     */
    long download(VideoDownloadRequest request) throws IOException;
}
