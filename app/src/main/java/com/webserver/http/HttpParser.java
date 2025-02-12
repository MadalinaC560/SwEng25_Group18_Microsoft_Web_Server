package com.webserver.http;

import com.webserver.model.HttpRequest;
import com.webserver.util.Logger;
import java.io.*;
import java.util.*;

public class HttpParser {
    public enum HttpMethod {
        GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS;

        public static HttpMethod fromString(String method) {
            try {
                return valueOf(method.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public HttpRequest parse(InputStream input) {
        // TODO: Implement HTTP request parsing
        // 1. Read the input stream and extract the request line (method, path, HTTP version)
        // 2. Parse all headers until an empty line is encountered
        // 3. If Content-Length header exists, read the body
        // 4. Create and return an HttpRequest object with the parsed data
        // 5. Handle malformed requests appropriately
        return null;
    }

    private Optional<List<String>> readMessage(InputStream inputStream) {
        // TODO: Implement raw HTTP message reading
        // 1. Read the input stream line by line until an empty line
        // 2. Handle Content-Length header if present
        // 3. Read the message body if required
        // 4. Return all lines including headers and body
        return Optional.empty();
    }

    private HttpRequest buildRequest(List<String> requestLines) {
        // TODO: Implement request object construction
        // 1. Parse the first line into method, path, and HTTP version
        // 2. Extract and organize headers into a map
        // 3. Handle the body if present
        // 4. Create and return HttpRequest object
        return null;
    }
}
