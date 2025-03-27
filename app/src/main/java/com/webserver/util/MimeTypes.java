
// MimeTypes.java - New content type mapping
package com.webserver.util;

import java.util.HashMap;
import java.util.Map;

public class MimeTypes {
    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("txt", "text/plain");
        // Add more mime types
    }

    public static String getMimeType(String filename) {
        // Implementation
        return filename;
    }
}