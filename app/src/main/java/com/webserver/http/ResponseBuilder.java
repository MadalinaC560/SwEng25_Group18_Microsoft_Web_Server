package com.webserver.http;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webserver.model.HttpResponse;

public class ResponseBuilder {
    private static final Map<Integer, String> STATUS_PHRASES = new HashMap<>();

    static {
        // TODO: Initialize common HTTP status codes and phrases
        // Add mappings for 2xx, 3xx, 4xx, and 5xx status codes
        
        //Informational status codes(1xx)
        STATUS_PHRASES.put(100, "Continue");
        STATUS_PHRASES.put(101, "Switching Protocols");
        STATUS_PHRASES.put(102, "Processing");

        //Successful status codes(2xx)
        STATUS_PHRASES.put(200, "OK");
        STATUS_PHRASES.put(201, "Created");
        STATUS_PHRASES.put(202, "Accepted");
        STATUS_PHRASES.put(203, "Non-authoritative Information");
        STATUS_PHRASES.put(204, "No Content");
        STATUS_PHRASES.put(205, "Reset Content");
        STATUS_PHRASES.put(206, "Partial Content");

        //Redirection status codes(3xx)
        STATUS_PHRASES.put(300, "Multiple Choices");
        STATUS_PHRASES.put(301, "Moved Permanently");
        STATUS_PHRASES.put(302, "Found");
        STATUS_PHRASES.put(303, "See Other");
        STATUS_PHRASES.put(304, "Not Modified");
        STATUS_PHRASES.put(305, "Use Proxy");

        //Client error status codes(4xx)
        STATUS_PHRASES.put(400, "Bad Request");
        STATUS_PHRASES.put(401, "Unauthorised");
        STATUS_PHRASES.put(402, "Payment Required");
        STATUS_PHRASES.put(403, "Forbidden");
        STATUS_PHRASES.put(404, "Not Found");
        STATUS_PHRASES.put(405, "Method Not Allowed");
        STATUS_PHRASES.put(406, "Not Acceptable");
        STATUS_PHRASES.put(407, "Proxy Authentication Required");
        STATUS_PHRASES.put(408, "Request Timeout");
        STATUS_PHRASES.put(409, "Conflict");
        STATUS_PHRASES.put(410, "Gone");

        //Server error status codes(5xx)
        STATUS_PHRASES.put(500, "Internal Server Error");
        STATUS_PHRASES.put(501, "Not Implemented");
        STATUS_PHRASES.put(502, "Bad Gateway");
        STATUS_PHRASES.put(503, "Service Unavailable");
        STATUS_PHRASES.put(504, "Gateway Timeout");
        STATUS_PHRASES.put(505, "HTTP Version Not Supported");
        STATUS_PHRASES.put(508, "Loop Detected");
        STATUS_PHRASES.put(511, "Network Authentication Required");

    }

    public static void write(OutputStream output, HttpResponse response) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));

        // 1) Write the status line
        bw.write(buildStatusLine(response));
        bw.write("\r\n");

        // 2) Determine Content-Length
        byte[] rawBody = response.getRawBody();
        int contentLength = 0;
        if (rawBody != null) {
            contentLength = rawBody.length;
        } else if (!response.getBody().isEmpty()) {
            // measure how many bytes the text body would occupy
            contentLength = response.getBody().getBytes(StandardCharsets.UTF_8).length;
        }

        // 3) Build the header lines
        List<String> headersAsStrings = buildHeaderStrings(response.getHeaders());
        boolean foundContentLength = false;

        // Check if user already set Content-Length
        for (String line : headersAsStrings) {
            if (line.toLowerCase().startsWith("content-length:")) {
                foundContentLength = true;
                break;
            }
        }

        // We'll write out the Content-Length ourselves if not present
        if (!foundContentLength) {
            bw.write("Content-Length: " + contentLength);
            bw.write("\r\n");
        }

        // Now write the other headers
        for (String headerLine : headersAsStrings) {
            bw.write(headerLine);
            bw.write("\r\n");
        }

        // 4) Blank line to separate headers from body
        bw.write("\r\n");
        bw.flush();

        // 5) Write the body (binary or text)
        if (rawBody != null) {
            // Binary data -> write directly to the OutputStream
            output.write(rawBody);
            output.flush();
        } else if (!response.getBody().isEmpty()) {
            // Text body
            bw.write(response.getBody());
            bw.flush();
        }
    }



    private static String buildStatusLine(HttpResponse response) {
        int statCode = response.getStatusCode();                                        //getting the status code via the built in HttpResponse method
        String phrase = STATUS_PHRASES.getOrDefault(statCode, response.getStatusMessage());


        return String.format("HTTP/1.1 %d %s", statCode, phrase);                       //returns the concatenated status lone
    }

    private static List<String> buildHeaderStrings(Map<String, List<String>> headers) {

        List<String> headerStrings = new ArrayList<>();                                 //List of Strings to hold results
        for (Map.Entry<String, List<String>> currentEntry : headers.entrySet()) {       //iterates over each map entry as a key value pair
            String hName = currentEntry.getKey();                                       //gets the current header name from the current entry
            for (String hValue : currentEntry.getValue()) {                             //iterates over each value thats assosciated to the current header
                headerStrings.add(hName + ": " + hValue);                               //adds the concatenated body to the list of headers
            }
        }
        return headerStrings;

    }   
}
