package com.webserver.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.webserver.model.HttpRequest;
import com.webserver.model.HttpResponse;
import com.webserver.util.Logger;
import com.webserver.util.FileService;
import com.webserver.util.MimeTypes;


public class RequestProcessor {
    private final Map<String, RouteHandler> routes;
    private final FileService fileService;

    public RequestProcessor(FileService fileService) {
        this.routes = new HashMap<>();
        this.fileService = fileService;
    }

    public void addRoute(String path, RouteHandler handler) {
        // TODO: Implement route registration
        // 1. Validate the path format
        if(path == null || path.isEmpty() || !path.startsWith("/"))
        {
            throw new IllegalArgumentException("Invalid route path: Must start with '/' and cannot be empty.");
        }
        // 2. Handle duplicate routes
        if(routes.containsKey(path))
        {
            throw new IllegalArgumentException("Route Already Exists: " + path);
        }
        // 3. Store the route handler
        routes.put(path, handler);
    }

    // in RequestProcessor.java
    public boolean hasRoute(String path) {
        return routes.containsKey(path);
    }


    public HttpResponse process(HttpRequest request) {
        if (!isValidRequest(request)) {
            Logger.error("Invalid request received", null);
            return createErrorResponse(400, "Bad Request");
        }

        String path = request.getBasePath();  // e.g. "/api/tenants/101"

//        if ("GET".equalsIgnoreCase(request.getMethod()) && path.endsWith("/")) {
//            // e.g. "/app_4002/" -> redirect to "/app_4002/index.html"
//
//            // Build a 302 response with a "Location" header.
//            return new HttpResponse.Builder()
//                    .setStatusCode(302)  // 302 Found (temporary redirect)
//                    .addHeader("Location", path + "index.html")
//                    .build();
//        }
//         If GET and the path is exactly "/", return a default page
        if ("GET".equalsIgnoreCase(request.getMethod()) && "/".equals(path)) {
            return new HttpResponse.Builder()
                    .setStatusCode(200)
                    .addHeader("Content-Type", "text/html")
                    .setBody("<html><h1>Welcome to Cloudle!</h1><p>This is the default root page!</p></html>")
                    .build();
        }

// Otherwise, if GET and the path ends in "/", redirect to "index.html"
        if ("GET".equalsIgnoreCase(request.getMethod()) && path.endsWith("/")) {
            return new HttpResponse.Builder()
                    .setStatusCode(302)
                    .addHeader("Location", path + "index.html")
                    .build();
        }


        // 1) First try exact route
        RouteHandler handler = routes.get(path);

        // 2) If no exact match, check if it starts with "/api/tenants"
        //    and we already have a route for "/api/tenants"
        if (handler == null && path.startsWith("/api/tenants")) {
            handler = routes.get("/api/tenants");
        }

        if (handler == null) {
            Logger.info("No handler found for path: " + path);



            // Attempt static file if GET
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                try {
                    byte[] fileBytes = fileService.readFile(path);
                    String mimeType = MimeTypes.getMimeType(path);
                    return new HttpResponse.Builder()
                            .setStatusCode(200)
                            .setStatusMessage("OK")
                            .addHeader("Content-Type", mimeType)
                            .setRawBody(fileBytes)
                            .build();
                } catch (IOException e) {
                    Logger.error("Error serving static file: " + path, e);
                    return createErrorResponse(404, "Not Found");
                }
            } else {
                return createErrorResponse(404, "Not Found");
            }
        }

        // If we found a handler, call it
        try {
            HttpResponse response = handler.handle(request);
            if (response == null) {
                Logger.error("Handler returned null response for path: " + path, null);
                return createErrorResponse(500, "Internal Server Error");
            }
            return response;
        } catch (Exception e) {
            Logger.error("Error processing request for path: " + path, e);
            return createErrorResponse(500, "Internal Server Error");
        }
    }
    private HttpResponse createErrorResponse(int statusCode, String message) {
        // If 404, the test specifically wants "Error 404: Not Found"
        String body = message;
        if (statusCode == 404) {
            body = "Error 404: Not Found";
        }
        return new HttpResponse.Builder()
                .setStatusCode(statusCode)
                .setStatusMessage(message)
                .addHeader("Content-Type", "text/plain")
                .setBody(body)
                .build();
    }

    private boolean isValidRequest(HttpRequest request) {
        // TODO: Implement request validation
        // 1. Check for null request or required fields
        if(request == null || request.getMethod() == null || request.getPath() == null)
        {
            return false;
        }
        // 2. Validate HTTP method
        String method = request.getMethod();
        if(!method.equals(("GET")) && !method.equals("POST") && !method.equals(("PUT")) && !method.equals("DELETE") && !method.equals("OPTIONS"))
        {
            return false;
        }
        // 3. Validate path format
        if(!request.getPath().startsWith("/"))
        {
            return false;
        }
        // 4. Check required headers for specific methods (e.g., Content-Length for POST)
//        if((method.equals("POST") || method.equals("PUT")) && !hasContentLengthHeader(request))
//        {
//            return false;
//        }
        return true;
    }

    private boolean hasContentLengthHeader(HttpRequest request)
    {
        Map<String, List<String>> headers = request.getHeaders();
        return headers.containsKey("Content-Length") && !headers.get("Content-Length").isEmpty();
    }

    public interface RouteHandler {
        HttpResponse handle(HttpRequest request) throws IOException;
    }
}
