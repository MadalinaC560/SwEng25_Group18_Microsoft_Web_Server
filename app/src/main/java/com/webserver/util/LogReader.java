package com.webserver.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogReader {
    private static final String LOG_FILE = "webserver.log";

    // Pattern to match the timestamp and log level
    private static final Pattern LOG_PATTERN = Pattern.compile("\\[(.*?)\\] \\[(.*?)\\] (.*)");

    public static List<Map<String, Object>> getRecentLogs(int appId, int limit, String level) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        File logFile = new File(LOG_FILE);

        if (!logFile.exists() || !logFile.isFile()) {
            System.out.println("Log file does not exist or is not a file");
            throw new Exception("Log file not found");
        }

        // For large files, we might want to read from the end of the file
        // but for simplicity, I'll read from the beginning
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            String appMarker = appId > 0 ? "/apps/" + appId : null;

            while ((line = reader.readLine()) != null && result.size() < limit) {
                // Skip lines not relevant to this app if an app ID is specified
                if (appMarker != null && !line.contains(appMarker)) {
                    continue;
                }

                // Parse the log line
                Matcher matcher = LOG_PATTERN.matcher(line);
                if (matcher.find()) {
                    String timestamp = matcher.group(1);
                    String logLevel = matcher.group(2);
                    String message = matcher.group(3);

                    // Filter by log level if specified
                    if (level != null && !logLevel.equalsIgnoreCase(level)) {
                        continue;
                    }

                    Map<String, Object> logEntry = new HashMap<>();
                    logEntry.put("timestamp", timestamp);
                    logEntry.put("level", logLevel);
                    logEntry.put("message", message);

                    result.add(logEntry);
                }
            }
        }

        return result;
    }
}