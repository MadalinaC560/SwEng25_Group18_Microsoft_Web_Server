package com.webserver.http;

import com.webserver.model.HttpRequest;
import com.webserver.model.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class RequestProcessor {
    private final Map<String, RouteHandler> routes;

    public RequestProcessor() {
        this.routes = new HashMap<>();
        // TODO: Implement default route handling
        // 1. Add a default route for "/"
        // 2. Add routes for common HTTP errors (404, 500)
    }

    public void addRoute(String path, RouteHandler handler) {
        // TODO: Implement route registration
        // 1. Validate the path format
        // 2. Handle duplicate routes
        // 3. Store the route handler
    }

    public HttpResponse process(HttpRequest request) {
        // TODO: Implement request processing
        // 1. Validate the incoming request
        // 2. Find matching route handler
        // 3. Execute the handler or return appropriate error response
        // 4. Handle any exceptions during processing
        return null;
    }

    private boolean isValidRequest(HttpRequest request) {
        // TODO: Implement request validation
        // 1. Check for null request or required fields
        // 2. Validate HTTP method
        // 3. Validate path format
        // 4. Check required headers for specific methods (e.g., Content-Length for POST)
        return false;
    }

    public interface RouteHandler {
        HttpResponse handle(HttpRequest request);
    }
}
