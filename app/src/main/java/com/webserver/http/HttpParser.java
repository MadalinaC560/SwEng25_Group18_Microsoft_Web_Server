package com.webserver.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.webserver.model.HttpRequest;
import com.webserver.util.Logger;

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
        // TODO: Implement HTTP request parsing
        // 1. Read the input stream and extract the request line (method, path, HTTP version)
        // 2. Parse all headers until an empty line is encountered
        // 3. If Content-Length header exists, read the body
        // 4. Create and return an HttpRequest object with the parsed data
        // 5. Handle malformed requests appropriately
        Optional<List<String>> requestLines = readMessage(input);

        if(requestLines.isEmpty()){
            throw new IOException("Invalid Http Request");
        }

        List<String> actualLines = requestLines.get();

        return buildRequest(actualLines, input);

    }

    private Optional<List<String>> readMessage(InputStream inputStream) {
        // TODO: Implement raw HTTP message reading
        // 1. Read the input stream line by line until an empty line
        // 2. Handle Content-Length header if present
        // 3. Read the message body if required
        // 4. Return all lines including headers and body

        List<String> lines = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String currentLine;

            try {
                while ((currentLine = br.readLine()) != null && !currentLine.isEmpty()) {
                    lines.add(currentLine);
                }

                // add logic to read the body if Content-Length header is present
                String contentLengthHeader = lines.stream()
                        .filter(h -> h.toLowerCase().startsWith("content-length:"))
                        .findFirst()
                        .orElse(null);

                if (contentLengthHeader != null) {
                    int contentLength = Integer.parseInt(
                            contentLengthHeader.split(":")[1].trim()
                    );
                    char[] body = new char[contentLength];
                    br.read(body, 0, contentLength);
                    lines.add(new String(body));
                }

                return Optional.of(lines);

            } catch (IOException e) {
                return Optional.empty();
            }
        } catch (Exception e) {
            Logger.error("Error reading HTTP request", e);
            return Optional.empty();
        }
    }

    private HttpRequest buildRequest(List<String> requestLines, InputStream input) throws IOException {
        // TODO: Implement request object construction
        // 1. Parse the first line into method, path, and HTTP version
        // 2. Extract and organize headers into a map
        // 3. Handle the body if present
        // 4. Create and return HttpRequest object
        try {
            String[] firstLine = requestLines.get(0).split(" "); //splits into METHOD, PATH, and http VERSION
            System.out.println("First Line: " + requestLines.get(0));   //this is for debugging ===========================
            if (firstLine.length != 3) {
                Logger.error("Invalid request line: " + requestLines.get(0), null);
                throw new IllegalArgumentException("Invalid first line");
            }

            String method = firstLine[0];
            if (method == null) {
                Logger.error("Unsupported HTTP method: " + firstLine[0], null);
                throw new IllegalArgumentException("Unsupported HTTP method");
            }

            String path = firstLine[1];
            String httpVersion = firstLine[2]; //not used with our httpRequest class, possible implementation in the future

            Map<String, List<String>> headersAndValues = new HashMap<>(); //map for storing headers and assosciated values

            for (int i = 1; i < requestLines.size(); i++) {
                String currentHeader = requestLines.get(i);
                String[] headerComponents = currentHeader.split(": ", 2);

                if (headerComponents.length == 2) {
                    String key = headerComponents[0].trim();
                    String value = headerComponents[1].trim();

                    if (!headersAndValues.containsKey(key)) {//check if the key already exists, then create a new list
                        headersAndValues.put(key, new ArrayList<>());
                    }
                    headersAndValues.get(key).add(value);
                } else {
                    System.out.println("There was an error at index: " + i);
                }
            }
            System.out.println("Request Lines: ");
            for (int i = 0; i < requestLines.size(); i++) {
                System.out.println(i + ": " + requestLines.get(i));
            }
            System.out.println("Headers: " + headersAndValues);  // also for dubugging ================================


            String body = "";
            List<String> contentLengthHeader = headersAndValues.get("Content-Length");

            if (contentLengthHeader != null) {   //handles the case when the body is a known fixed length
                try {
                    int lengthOfContent = Integer.parseInt(contentLengthHeader.get(0));
                    body = readBody(input, lengthOfContent);
                } catch (Exception e) {
                    throw new IOException("Invalid header for Content-length");
                }
            }

            return new HttpRequest(method, path, headersAndValues, body);
        } catch (Exception e) {
            Logger.error("Error building HTTP request", e);
            throw new IllegalArgumentException("Error building HTTP request");
        }
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

        System.out.println("Body Read: " + new String(bytesOfBody));    //testín delete this ================================

        return new String(bytesOfBody);
    }
}
