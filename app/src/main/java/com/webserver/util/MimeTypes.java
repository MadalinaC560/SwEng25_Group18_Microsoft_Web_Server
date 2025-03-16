package com.webserver.util;

import java.util.HashMap;
import java.util.Map;

public class MimeTypes {
    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        // Common MIME types
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("htm", "text/html");
        MIME_TYPES.put("txt", "text/plain");
        MIME_TYPES.put("css", "text/css");
        MIME_TYPES.put("js", "application/javascript");
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("xml", "application/xml");

        // Image MIME types
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("svg", "image/svg+xml");
        MIME_TYPES.put("ico", "image/x-icon");

        // Audio & Video MIME types
        MIME_TYPES.put("mp3", "audio/mpeg");
        MIME_TYPES.put("wav", "audio/wav");
        MIME_TYPES.put("mp4", "video/mp4");
        MIME_TYPES.put("avi", "video/x-msvideo");

        // Fonts
        MIME_TYPES.put("woff", "font/woff");
        MIME_TYPES.put("woff2", "font/woff2");
        MIME_TYPES.put("ttf", "font/ttf");
        MIME_TYPES.put("otf", "font/otf");

        // Application MIME types
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("zip", "application/zip");
        MIME_TYPES.put("tar", "application/x-tar");
        MIME_TYPES.put("gz", "application/gzip");
        MIME_TYPES.put("rar", "application/vnd.rar");
    }

    /**
     * Returns the MIME type based on file extension.
     * Defaults to "application/octet-stream" if unknown.
     */
    public static String getMimeType(String filename) {
        if (filename == null) {
            return "application/octet-stream"; // Default if filename is null
        }
    
        // Remove query parameters or fragments (e.g., "/image.png?size=large")
        filename = filename.split("[?#]", 2)[0];
    
        // Extract the file extension
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "application/octet-stream"; // No extension found
        }
    
        String extension = filename.substring(lastDotIndex + 1).toLowerCase();
        return MIME_TYPES.getOrDefault(extension, "application/octet-stream");
    }
    
}
