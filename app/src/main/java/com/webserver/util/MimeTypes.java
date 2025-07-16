
// MimeTypes.java - New content type mapping
package com.webserver.util;

import java.util.HashMap;
import java.util.Map;

public class MimeTypes {
    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        //Text formats
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("htm", "text/html");
        MIME_TYPES.put("txt", "text/plain");
        MIME_TYPES.put("css", "text/css");
        MIME_TYPES.put("csv", "text/csv");
        MIME_TYPES.put("js", "text/javascript");
        MIME_TYPES.put("xml", "text/xml");

        //Video formats
        MIME_TYPES.put("mp4", "video/mp4");
        MIME_TYPES.put("mpeg", "video/mpeg");
        MIME_TYPES.put("webm", "video/webm");
        MIME_TYPES.put("avi", "video/x-msvideo");

        // Image formats
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("svg", "image/svg+xml");
        MIME_TYPES.put("ico", "image/x-icon");
        MIME_TYPES.put("webp", "image/webp");

        // Audio formats
        MIME_TYPES.put("mp3", "audio/mpeg");
        MIME_TYPES.put("wav", "audio/wav");
        MIME_TYPES.put("ogg", "audio/ogg");

        // Application formats
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("zip", "application/zip");
        MIME_TYPES.put("doc", "application/msword");

        // Font formats
        MIME_TYPES.put("ttf", "font/ttf");
        MIME_TYPES.put("otf", "font/otf");
        MIME_TYPES.put("woff", "font/woff");
        MIME_TYPES.put("woff2", "font/woff2");


        // Add more mime types
    }

    public static String getMimeType(String filename) {
        // Implementation
        String defaultMimeType = "application/octet-stream";
        if (filename == null || filename.isEmpty())
        {
            return defaultMimeType;
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == filename.length() - 1)
        {
            return defaultMimeType;
        }
        String mimeTypeSubstring = filename.substring(lastDotIndex + 1).toLowerCase();
        String mimeType = MIME_TYPES.get(mimeTypeSubstring);
        return (mimeType != null)? mimeType : "application/octet-stream";
    }
}
