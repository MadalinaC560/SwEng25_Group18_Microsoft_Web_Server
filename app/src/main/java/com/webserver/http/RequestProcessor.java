package com.webserver.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webserver.model.HttpRequest;
import com.webserver.model.HttpResponse;
import com.webserver.util.FileService;
import com.webserver.util.MimeTypes;


public class RequestProcessor {
    private final Map<String, RouteHandler> routes;
    private final FileService fileService; // Add file service for static files

    public RequestProcessor(FileService fileService) {
        this.routes = new HashMap<>();
        this.fileService = fileService; // store the file service instance

        
        // 1. Add a default route for "/"
        routes.put("/", request -> new HttpResponse.Builder()
            .setStatusCode(200)
            .setBody("Valid route")
            .build());
        // 2. Add routes for common HTTP errors (404, 500)
        routes.put("/404", request -> new HttpResponse.Builder()
            .setStatusCode(404)
            .setBody("Error 404: Not Found")
            .build());

        routes.put("/500", request -> new HttpResponse.Builder()
            .setStatusCode(500)
            .setBody("Error 500: Not Found")
            .build());
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



    public HttpResponse process(HttpRequest request) {
        // ✅ Validate Request Early
        if (!isValidRequest(request)) {
            return new HttpResponse.Builder()
                .setStatusCode(400)
                .setBody("Bad Request")
                .build();
        }
    
// ✅ Prevent Directory Traversal Attacks (BAD request)
if (!fileService.isValidPath(request.getPath())) {
    return new HttpResponse.Builder()
        .setStatusCode(400)
        .setBody("Bad Request") // Optional: Add more clarity
        .build();
}

// ✅ Handle Static Files (CHECK IF FILE EXISTS)
if (fileService.fileExists(request.getPath())) { // ✅ New method to check existence
    try {
        byte[] fileContent = fileService.readFile(request.getPath());

        String fileName = fileService.getFileName(request.getPath()); // Get file name
        String mimeType = MimeTypes.getMimeType(fileName); // Determine MIME type

        return new HttpResponse.Builder()
            .setStatusCode(200)
            .addHeader("Content-Type", mimeType)
            .setBody(new String(fileContent))
            .build();
    } catch (IOException e) {
        return new HttpResponse.Builder()
            .setStatusCode(404) // ✅ Correctly return 404 if file doesn't exist
            .setBody("Error 404: Not Found")
            .build();
    }
}

        
    
        // ✅ Handle API Routes
        RouteHandler handler = routes.getOrDefault(request.getPath(), routes.get("/404"));
    
        if (handler == null) {
            return new HttpResponse.Builder()
                .setStatusCode(404)
                .setBody("Error 404: Not Found")
                .build();
        }
    
        try {
            return handler.handle(request);
        } catch (Exception e) {
            return new HttpResponse.Builder()
                .setStatusCode(500)
                .setBody("Internal Server Error: " + e.getMessage())
                .build();
        }
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
        if(!method.equals(("GET")) && !method.equals("POST") && !method.equals(("PUT")) && !method.equals("DELETE"))
        {
            return false;
        }
        // 3. Validate path format
        if(!request.getPath().startsWith("/"))
        {
            return false;
        }
        // 4. Check required headers for specific methods (e.g., Content-Length for POST)
        if((method.equals("POST") || method.equals("PUT")) && !hasContentLengthHeader(request))
        {
            return false;
        }
        return true;
    }




    private boolean hasContentLengthHeader(HttpRequest request) {
        Map<String, List<String>> headers = request.getHeaders();
        return headers.containsKey("Content-Length") && !headers.get("Content-Length").isEmpty();
    }

    public interface RouteHandler {
        HttpResponse handle(HttpRequest request);
    }
}
