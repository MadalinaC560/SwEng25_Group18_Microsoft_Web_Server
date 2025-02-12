package com.webserver.http;

import com.webserver.model.HttpResponse;
import java.io.*;
import java.util.*;

public class ResponseBuilder {
    private static final Map<Integer, String> STATUS_PHRASES = new HashMap<>();

    static {
        // TODO: Initialize common HTTP status codes and phrases
        // Add mappings for 2xx, 3xx, 4xx, and 5xx status codes
    }

    public static void write(OutputStream output, HttpResponse response) {
        // TODO: Implement HTTP response writing
        // 1. Write the status line (HTTP/1.1 200 OK)
        // 2. Write all headers with proper formatting
        // 3. Add Content-Length header if body exists
        // 4. Write empty line to separate headers from body
        // 5. Write the response body if present
        // 6. Flush the output stream
    }

    private static String buildStatusLine(HttpResponse response) {
        // TODO: Implement status line construction
        // 1. Format the status line with HTTP version, status code, and phrase
        // 2. Handle custom status messages
        return null;
    }

    private static List<String> buildHeaderStrings(Map<String, List<String>> headers) {
        // TODO: Implement header string formatting
        // 1. Convert header map to list of properly formatted header strings
        // 2. Handle multiple values for the same header
        return null;
    }
}
