package com.webserver.model;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webserver.http.ResponseBuilder;

public class HttpResponse {
    private final int statusCode;
    private final String statusMessage;
    private final Map<String, List<String>> headers;
    private final String body;

    private HttpResponse(Builder builder) {
        this.statusCode = builder.statusCode;
        this.statusMessage = builder.statusMessage;
        this.headers = builder.headers;
        this.body = builder.body;
    }

    public int getStatusCode() { return statusCode; }
    public String getStatusMessage() { return statusMessage; }
    public Map<String, List<String>> getHeaders() { return headers; }
    public String getBody() { return body; }

    public void write(OutputStream outputStream) throws IOException{
        ResponseBuilder.write(outputStream, this);
    }

    public static class Builder {
        private int statusCode = 200;
        private String statusMessage = "OK";
        private Map<String, List<String>> headers = new HashMap<>();
        private String body = "";

        public Builder setStatusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder setStatusMessage(String message) {
            this.statusMessage = message;
            return this;
        }

        public Builder addHeader(String name, String value) {
            headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
            return this;
        }

        public Builder setBody(String body) {
            this.body = body;
            return this;
        }

        public HttpResponse build() {
            return new HttpResponse(this);
        }
    }

    // Common response factory methods
    public static HttpResponse ok(String body) {
        return new Builder()
                .setStatusCode(200)
                .setStatusMessage("OK")
                .setBody(body)
                .build();
    }

    public static HttpResponse notFound() {
        return new Builder()
                .setStatusCode(404)
                .setStatusMessage("Not Found")
                .setBody("404 Not Found")
                .build();
    }

    public static HttpResponse serverError() {
        return new Builder()
                .setStatusCode(500)
                .setStatusMessage("Internal Server Error")
                .setBody("500 Internal Server Error")
                .build();
    }
}