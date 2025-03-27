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
import java.io.*;
import java.util.*;

public class ResponseBuilder {
    private static final Map<Integer, String> STATUS_PHRASES = new HashMap<>();

    static {
        // TODO: Initialize common HTTP status codes and phrases
        // Add mappings for 2xx, 3xx, 4xx, and 5xx status codes
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
