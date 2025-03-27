package com.webserver.http;

import com.webserver.model.HttpRequest;
import com.webserver.util.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

    public HttpRequest parse(InputStream input) throws IOException {

        // 1) Read the request line
        String requestLine = readRawLine(input);
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Empty or invalid request line");
        }

        // parse something like: POST /api/applications HTTP/1.1
        String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            throw new IOException("Malformed request line: " + requestLine);
        }
        HttpMethod method = HttpMethod.fromString(parts[0]);
        if (method == null) {
            throw new IOException("Unsupported HTTP method: " + parts[0]);
        }
        String path = parts[1];
        String httpVersion = parts[2]; // not heavily used right now

        // 2) Read headers until a blank line
        Map<String, List<String>> headersMap = new HashMap<>();
        while (true) {
            String line = readRawLine(input);
            if (line == null || line.isEmpty()) {
                // blank line -> done reading headers
                break;
            }
            // parse "HeaderName: value"
            int colonPos = line.indexOf(':');
            if (colonPos > 0) {
                String hdrName = line.substring(0, colonPos).trim();
                String hdrValue = line.substring(colonPos + 1).trim();
                headersMap.computeIfAbsent(hdrName, k -> new ArrayList<>()).add(hdrValue);
            }
        }

        // 3) Figure out Content-Length
        int contentLength = 0;
        for (Map.Entry<String, List<String>> e : headersMap.entrySet()) {
            if ("Content-Length".equalsIgnoreCase(e.getKey())) {
                contentLength = Integer.parseInt(e.getValue().get(0));
                break;
            }
        }

        // 4) Read the body (raw bytes)
        byte[] bodyBytes = new byte[0];
        if (contentLength > 0) {
            bodyBytes = new byte[contentLength];
            int totalRead = 0;
            while (totalRead < contentLength) {
                int n = input.read(bodyBytes, totalRead, contentLength - totalRead);
                if (n == -1) {
                    // connection closed early
                    throw new IOException("Unexpected end of stream: expected " +
                            contentLength + " bytes, got " + totalRead);
                }
                totalRead += n;
            }
        }

        // 5) Build your HttpRequest
        // If it's text, you might do new String(bodyBytes, StandardCharsets.UTF_8)
        String textBody = new String(bodyBytes, StandardCharsets.UTF_8);
        return new HttpRequest.Builder()
                .setMethod(method.toString())
                .setPath(path)
                .setHeaders(headersMap)
                .setTextBody(textBody)
                .setRawBody(bodyBytes)
                .build();

    }

    private String readRawLine(InputStream in) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int prev = -1;
        while (true) {
            int c = in.read();
            if (c == -1) {
                // EOF
                break;
            }
            if (c == '\n') {
                // If the previous was '\r', we have \r\n line ending
                break;
            }
            if (c == '\r') {
                // next might be \n, so we check but do not store \r
                in.mark(1);
                prev = c;
                continue;
            }
            buf.write(c);
            prev = c;
        }
        return buf.toString("UTF-8");
    }

    private String readBody(InputStream input, int bodyLength) throws IOException{
        if(bodyLength == 0){
            return "";
        }

        byte[] bytesOfBody = new byte[bodyLength];
        int bytesReadTracker = 0;

        while(bytesReadTracker < bodyLength){ //while loop ensures the entire InputStream is read, as it may not have everything available at one time
            int bytesRead = input.read(bytesOfBody, bytesReadTracker, bodyLength - bytesReadTracker);

            if(bytesRead == -1){
                throw new IOException("Unexpected end when reading body, check for errors");
            }

            bytesReadTracker += bytesRead;
        }


        return new String(bytesOfBody);
    }
}

