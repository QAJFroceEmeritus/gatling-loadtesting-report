package com.jforce.selenium.config;

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
import java.nio.file.StandardOpenOption;

public final class HttpVideoDownloader implements VideoDownloader {

    @Override
    public long download(VideoDownloadRequest request) throws IOException {
        URL url = request.getVideoUrl();
        Path destination = request.getDestination();
        boolean resume = request.isResume();

        Files.createDirectories(destination.toAbsolutePath().getParent());

        long existingBytes = resume && Files.exists(destination) ? Files.size(destination) : 0L;

        HttpURLConnection.setFollowRedirects(true);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(FFmpegConfig.CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(FFmpegConfig.READ_TIMEOUT_MS);
        connection.setRequestProperty("User-Agent", FFmpegConfig.USER_AGENT);
        if (existingBytes > 0) {
            connection.setRequestProperty("Range", "bytes=" + existingBytes + "-");
        }

        connection.connect();

        int status = connection.getResponseCode();
        boolean ok = status == HttpURLConnection.HTTP_OK;
        boolean partial = status == HttpURLConnection.HTTP_PARTIAL;
        if (!(ok || partial)) {
            connection.disconnect();
            throw new IOException("Server returned HTTP " + status);
        }

        if (existingBytes > 0 && ok) {
            existingBytes = 0; // server ignored Range; restart
        }

        try (InputStream in = new BufferedInputStream(connection.getInputStream(), FFmpegConfig.BUFFER_SIZE);
             OutputStream out = new BufferedOutputStream(
                     Files.newOutputStream(
                             destination,
                             existingBytes == 0
                                     ? new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE}
                                     : new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND}
                     ),
                     FFmpegConfig.BUFFER_SIZE
             )
        ) {
            byte[] buffer = new byte[FFmpegConfig.BUFFER_SIZE];
            long totalBytes = existingBytes;
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            out.flush();
            return totalBytes;
        } finally {
            connection.disconnect();
        }
    }
}
