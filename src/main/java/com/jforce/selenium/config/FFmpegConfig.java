package com.jforce.selenium.config;

public final class FFmpegConfig {
    private FFmpegConfig() {}

    public static final int CONNECT_TIMEOUT_MS = 15_000;
    public static final int READ_TIMEOUT_MS = 60_000;
    public static final int BUFFER_SIZE = 256 * 1024; // 256 KiB
    public static final String USER_AGENT = "Mozilla/5.0";
}
