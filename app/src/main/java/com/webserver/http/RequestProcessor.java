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
        // 2. Handle duplicate routes
        // 3. Store the route handler
    }

    public HttpResponse process(HttpRequest request) {
        // Validate request
        if(!isValidRequest(request)) {
            Logger.error("Invalid request received", null);
            return createErrorResponse(400, "Bad Request");
        }

        String path = request.getBasePath();
        RouteHandler handler = routes.get(path);

        // If no route is defined for this path:
        if (handler == null) {
            Logger.info("No handler found for path: " + path);

            // For GET requests, attempt static file serving:
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                try {
                    // read the file from disk
                    byte[] fileBytes = fileService.readFile(path);
                    // determine mime type
                    String mimeType = MimeTypes.getMimeType(path);

                    // Return a 200 with the file content in rawBody
                    return new HttpResponse.Builder()
                            .setStatusCode(200)
                            .setStatusMessage("OK")
                            .addHeader("Content-Type", mimeType)
                            .setRawBody(fileBytes)
                            .build();

                } catch (IOException e) {
                    // file not found, or invalid, etc. -> 404
                    Logger.error("Error serving static file: " + path, e);
                    return createErrorResponse(404, "Not Found");
                }
            } else {
                // Not a GET -> no route found
                return createErrorResponse(404, "Not Found");
            }
        }

        // Otherwise, invoke the matching route handler
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
        // 2. Validate HTTP method
        String method = request.getMethod();
        if(!method.equals(("GET")) && !method.equals("POST") && !method.equals(("PUT")) && !method.equals("DELETE") && !method.equals("OPTIONS"))
        {
            return false;
        }
        // 3. Validate path format
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
