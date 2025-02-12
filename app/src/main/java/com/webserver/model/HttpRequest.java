package com.webserver.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HttpRequest {
    private final String method;
    private final String path;
    private final Map<String, List<String>> headers;
    private final String body;

    public HttpRequest(String method, String path, Map<String, List<String>> headers, String body) {
        this.method = method;
        this.path = path;
        this.headers = Collections.unmodifiableMap(headers);
        this.body = body;
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

    public String getBody() {
        return body;
    }

    // Helper methods for common operations
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
        return body != null && !body.isEmpty();
    }

    // Builder pattern for flexible construction
    public static class Builder {
        private String method;
        private String path;
        private Map<String, List<String>> headers;
        private String body = "";

        public Builder setMethod(String method) {
            this.method = method;
            return this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public Builder setHeaders(Map<String, List<String>> headers) {
            this.headers = headers;
            return this;
        }

        public Builder setBody(String body) {
            this.body = body;
            return this;
        }

        public HttpRequest build() {
            if (method == null || path == null || headers == null) {
                throw new IllegalStateException("Method, path, and headers are required");
            }
            return new HttpRequest(method, path, headers, body);
        }
    }

    @Override
    public String toString() {
        return String.format("HttpRequest{method='%s', path='%s', headers=%s, hasBody=%s}",
                method, path, headers, hasBody());
    }
}