package com.webserver.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HttpRequest {
    private final String method;
    private final String path;
    private final Map<String, List<String>> headers;

    // Keep a text body if you want it for form submissions
    private final String textBody;

    // New field to store raw bytes for binary data
    private final byte[] rawBody;

    public HttpRequest(
            String method,
            String path,
            Map<String, List<String>> headers,
            String textBody,
            byte[] rawBody
    ) {
        this.method = method;
        this.path = path;
        this.headers = Collections.unmodifiableMap(headers);
        this.textBody = textBody;
        this.rawBody = rawBody;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    /**
     * For form data or JSON, you can read it as text.
     * But if it's binary, this might be empty.
     */
    public String getTextBody() {
        return textBody;
    }

    /**
     * For binary data (e.g. a .zip), read from here.
     * Could also be non-empty even for textual requests
     * if you prefer a single approach for the body.
     */
    public byte[] getRawBody() {
        return rawBody;
    }

    // Helper methods for headers & query params and common operations
    public Optional<String> getFirstHeader(String name) {
        List<String> values = headers.get(name.toLowerCase());
        return values != null && !values.isEmpty() ? Optional.of(values.get(0)) : Optional.empty();
    }

    public List<String> getHeaders(String name) {
        return headers.getOrDefault(name.toLowerCase(), Collections.emptyList());
    }

    public Optional<String> getQueryParam(String name) {
        int queryStart = path.indexOf('?');
        if (queryStart == -1) {
            return Optional.empty();
        }

        String query = path.substring(queryStart + 1);
        for (String param : query.split("&")) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2 && keyValue[0].equals(name)) {
                return Optional.of(keyValue[1]);
            }
        }
        return Optional.empty();
    }

    public String getBasePath() {
        int queryStart = path.indexOf('?');
        return queryStart == -1 ? path : path.substring(0, queryStart);
    }

    public boolean hasBody() {
        // If you keep text plus bytes, you can decide what "hasBody" means
        return (rawBody != null && rawBody.length > 0)
                || (textBody != null && !textBody.isEmpty());
    }

    // Builder pattern (updated to include rawBody)
    public static class Builder {
        private String method;
        private String path;
        private Map<String, List<String>> headers;
        private String textBody = "";
        private byte[] rawBody = new byte[0];

        public Builder setMethod(String method) { this.method = method; return this; }
        public Builder setPath(String path) { this.path = path; return this; }
        public Builder setHeaders(Map<String, List<String>> headers) { this.headers = headers; return this; }
        public Builder setTextBody(String body) { this.textBody = body; return this; }
        public Builder setRawBody(byte[] rawBody) { this.rawBody = rawBody; return this; }

        public HttpRequest build() {
            if (method == null || path == null || headers == null) {
                throw new IllegalStateException("Method, path, and headers are required");
            }
            return new HttpRequest(method, path, headers, textBody, rawBody);
        }
    }

    @Override
    public String toString() {
        return String.format(
                "HttpRequest{method='%s', path='%s', headers=%s, textBodyLength=%d, rawBodyLength=%d}",
                method, path, headers,
                (textBody != null ? textBody.length() : 0),
                (rawBody != null ? rawBody.length : 0)
        );
    }
}
