package com.webserver.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

public class Logger {
    private static final String LOG_FILE = "webserver.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static void log(String level, String message, Throwable t) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[%s] [%s] %s", timestamp, level, message);

        // Print to console
        System.out.println(logMessage);
        if (t != null) {
            t.printStackTrace();
        }

        // Write to file
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println(logMessage);
            if (t != null) {
                t.printStackTrace(writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    public static void info(String message) {
        log("INFO", message, null);
    }

    public static void error(String message, Throwable t) {
        log("ERROR", message, t);
    }

    public static void warn(String message) {
        log("WARN", message, null);
    }
}