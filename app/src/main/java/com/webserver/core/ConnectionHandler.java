
package com.webserver.core;

import com.google.gson.GsonBuilder;
import com.webserver.azure.AzureBlobInterface;
import com.webserver.http.HttpParser;
import com.webserver.http.RequestProcessor;
import com.webserver.model.HttpRequest;
import com.webserver.model.HttpResponse;
import com.webserver.util.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.*;

/**
 * ConnectionHandler - ...
 */
public class ConnectionHandler implements Runnable {

    private final Socket clientSocket;
    private final HttpParser parser;
    private final RequestProcessor processor;
    private final AzureBlobInterface azureInterface;

    public ConnectionHandler(Socket socket) {
        this.clientSocket = socket;
        this.parser = new HttpParser();

        ConfigLoader config = new ConfigLoader();
        String webRoot = config.getWebRoot();
        FileService fileService = new FileService(webRoot);
        this.processor = new RequestProcessor(fileService);

        // Azure interface
        this.azureInterface = new AzureBlobInterface();

        defineRoutes();
        reRegisterAppRoutes();
    }

    @Override
    public void run() {
        handle();
    }

    private void handle() {
        long startTime = System.currentTimeMillis();
        HttpRequest request = null;
        int appId = 0;
        long inboundBytes = 0;
        long outboundBytes = 0;

        try {
            // 1) parse request
            request = parser.parse(clientSocket.getInputStream());
            Telemetry.incrementNumberRequests();

            if (request.getRawBody() != null) {
                inboundBytes = request.getRawBody().length; // ADDED for bandwidth
            }

            // 2) extract appId from path (already in your code)
            appId = extractAppId(request.getPath());

            // 3) process request
            HttpResponse response = processor.process(request);

            // 4) measure outbound size
            byte[] rawBody = response.getRawBody();
            if (rawBody != null) {
                outboundBytes = rawBody.length;
            } else if (!response.getBody().isEmpty()) {
                outboundBytes = response.getBody().getBytes().length;
            }

            // 5) write response
            response.write(clientSocket.getOutputStream());

            // 6) track response time (global)
            Telemetry.trackResponseTime(startTime);
            Telemetry.recordRequest(appId, startTime, response.getStatusCode());

            // ADDED for bandwidth
            Telemetry.recordTraffic(appId, inboundBytes, outboundBytes);

        } catch (Exception e) {
            Logger.error("Error handling connection", e);
            Telemetry.trackFailures(
                e,
                (request != null) ? request.getPath() : "unknown",
                "Failed to process request"
            );
        } finally {
            // close
            try {
                clientSocket.close();
            } catch (Exception ignore) {}
        }
    }
    /**
     * Attempt to parse out an appId from a path like "/app_2002/somefile.html".
     * If none is found, return 0 (meaning no recognized app).
     */
    private int extractAppId(String path) {
        // For example, if the path is "/app_2002/index.html"
        // we'll find "app_2002" and parse out 2002
        String marker = "/app_";
        int idx = path.indexOf(marker);
        if (idx < 0) {
            return 0; // no app_ pattern found
        }
        // next, parse substring from idx + marker.length until next slash
        int start = idx + marker.length();
        int slashPos = path.indexOf('/', start);
        String part;
        if (slashPos == -1) {
            // maybe the path was just "/app_2002"
            // very hacky, ik, pardon me...
            part = path.substring(start);
        } else {
            part = path.substring(start, slashPos);
        }
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    /**
     * Re-register dynamic routes for each App’s known files in Azure, unchanged from before
     */
    private void reRegisterAppRoutes() {
        List<DB.App> allApps = DB.listAllApps();
        for (DB.App app : allApps) {
            for (String route : app.routes) {
                if (!processor.hasRoute(route)) {
                    processor.addRoute(route, (req) -> {
                        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
                            return createCorsOk();
                        }
                        try {
                            String prefix = "/app_" + app.appId;
                            String filePath = route.substring(prefix.length());
                            try (InputStream is = azureInterface.download(app.appId, filePath)) {
                                if (is == null) {
                                    return new HttpResponse.Builder()
                                        .setStatusCode(404)
                                        .addHeader("Access-Control-Allow-Origin", "*")
                                        .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                                        .addHeader("Access-Control-Allow-Headers", "Content-Type")
                                        .setBody("File not found in Azure: " + filePath)
                                        .build();
                                }
                                byte[] content = is.readAllBytes();
                                String mime = MimeTypes.getMimeType(filePath);
                                return new HttpResponse.Builder()
                                    .setStatusCode(200)
                                    .addHeader("Access-Control-Allow-Origin", "*")
                                    .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                                    .addHeader("Access-Control-Allow-Headers", "Content-Type")
                                    .addHeader("Content-Type", mime)
                                    .setRawBody(content)
                                    .build();
                            }
                        } catch (Exception e) {
                            return new HttpResponse.Builder()
                                .setStatusCode(500)
                                .addHeader("Access-Control-Allow-Origin", "*")
                                .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                                .addHeader("Access-Control-Allow-Headers", "Content-Type")
                                .setBody("Error streaming from Azure: " + e.getMessage())
                                .build();
                        }
                    });
                }
            }
        }
    }

    private void defineRoutes() {
        // Root route
        processor.addRoute("/", req -> {
            if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
                return createCorsOk();
            }
            return new HttpResponse.Builder()
                    .setStatusCode(200)
                    .addHeader("Access-Control-Allow-Origin", "*")
                    .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    .addHeader("Access-Control-Allow-Headers", "Content-Type")
                    .addHeader("Content-Type", "text/plain")
                    .setBody("Welcome to Cloudle Web Server!")
                    .build();
        });

        // Add a route for app-level metrics: e.g. /api/tenants/101/apps/2002/metrics
        // We'll handle it in handleTenantsRoute with a new method handleTenantAppMetrics
        processor.addRoute("/api/tenants", this::handleTenantsRoute);

//        // Apps
        processor.addRoute("/api/apps", this::handleAppsCollection);
        processor.addRoute("/api/apps/", this::handleAppSubpaths);
//
//        // Refresh
        processor.addRoute("/api/refresh", this::handleRefresh);

        // Metrics
        processor.addRoute("/api/metrics", this::handleMetrics);

        // Login
        processor.addRoute("/api/login", this::handleLogin);

processor.addRoute("/api/users", (request) -> {
    // 1) If it's CORS preflight
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
        return createCorsOk();
    }

    // 2) Must be POST
    if (!"POST".equalsIgnoreCase(request.getMethod())) {
        return createError(405, "Method Not Allowed");
    }

    // 3) Parse JSON from the request body
    Map<String, Object> body = parseJsonMap(request.getTextBody());
    Double tenantIdDbl = (Double) body.get("tenantId");
    String username = (String) body.get("username");
    String role = (String) body.get("role");
    String password = (String) body.get("password");

    if (tenantIdDbl == null || username == null || password == null || role == null) {
        return createError(400, "Missing one or more fields: tenantId, username, role, password");
    }

    int tenantId = tenantIdDbl.intValue();

    // Check if username already exists
    DB.User existingUser = DB.findUserByUsername(username);
    if (existingUser != null) {
        return createError(409, "Username already exists");
    }

    // 4) Hash password
    String hashed = DB.hashPassword(password);

    // 5) Create a new User object
    DB.User newUser = new DB.User();
    newUser.userId = generateUserId();
    newUser.tenantId = tenantId;
    newUser.username = username;

    // Check if this is the first user for this tenant - they become admin
    List<DB.User> tenantUsers = DB.findUsersByTenantId(tenantId);
    if (tenantUsers.isEmpty()) {
        // First user gets admin role
        newUser.role = "admin";
    } else {
        // Otherwise use provided role
        newUser.role = role;
    }

    newUser.passwordHash = hashed;

    // ### CHANGED: row-level insert, no DB.save()
    DB.createUser(newUser);

    // 8) Return success
    Map<String,Object> resp = new HashMap<>();
    resp.put("userId", newUser.userId);
    resp.put("tenantId", newUser.tenantId);
    resp.put("username", newUser.username);
    resp.put("role", newUser.role);
    resp.put("message", "Account created successfully");
    return createJsonResponse(201, toJson(resp));
});
        processor.addRoute("/api/tenants/lookup", (request) -> {
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
        return createCorsOk();
    }

    if (!"GET".equalsIgnoreCase(request.getMethod())) {
        return createError(405, "Method Not Allowed");
    }

    // Extract email from query parameters
    String path = request.getPath();
    String email = null;

    if (path.contains("?email=")) {
        email = path.substring(path.indexOf("?email=") + 7);
        // Handle URL encoding if needed
        if (email.contains("%")) {
            try {
                email = java.net.URLDecoder.decode(email, "UTF-8");
            } catch (Exception e) {
                // Just use as is if decoding fails
            }
        }
    }

    if (email == null || email.isEmpty()) {
        return createError(400, "Email parameter is required");
    }

    DB.Tenant tenant = DB.findTenantByEmail(email);
    if (tenant == null) {
        return createError(404, "Tenant with email " + email + " not found");
    }

    return createJsonResponse(200, toJson(tenant));
});
    }

     //--------------------------------------------------------------------------
    // /api/refresh => reload DB from Azure
    //--------------------------------------------------------------------------
    private HttpResponse handleRefresh(HttpRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return createCorsOk();
        }
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return createError(405, "Method Not Allowed");
        }

        // Just re-load the in-memory snapshot from DB
        DB.load();

        return new HttpResponse.Builder()
                .setStatusCode(200)
                .addHeader("Access-Control-Allow-Origin", "*")
                .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                .addHeader("Access-Control-Allow-Headers", "Content-Type")
                .addHeader("Content-Type", "text/plain")
                .setBody("DB reloaded from Azure SQL")
                .build();
    }

    //--------------------------------------------------------------------------
    // /api/apps
    //--------------------------------------------------------------------------
    private HttpResponse handleAppsCollection(HttpRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return createCorsOk();
        }

        String method = request.getMethod().toUpperCase();
        if ("GET".equals(method)) {
            // ### CHANGED:
            List<DB.App> allApps = DB.listAllApps();
            return createJsonResponse(200, toJson(allApps));
        }
        return createError(405, "Method Not Allowed");
    }

    private HttpResponse handleAppSubpaths(HttpRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return createCorsOk();
        }

        String path = request.getPath(); // e.g. /api/apps/1000
        String subPath = path.substring("/api/apps".length());
        if (subPath.startsWith("/")) {
            subPath = subPath.substring(1);
        }
        if (subPath.isEmpty()) {
            return createError(404, "Not Found");
        }
        String[] parts = subPath.split("/");
        if (parts.length == 1) {
            // /api/apps/{id}
            return handleSingleApp(request, parts[0]);
        } else if (parts.length == 2) {
            // /api/apps/{id}/status or /upload
            String appIdStr = parts[0];
            String action = parts[1].toLowerCase();
            if ("status".equals(action)) {
                return handleAppStatus(request, appIdStr);
            } else if ("upload".equals(action)) {
                return handleUploadZip(request, appIdStr);
            } else {
                return createError(404, "Not Found");
            }
        }
        return createError(404, "Not Found");
    }

        private HttpResponse handleSingleApp(HttpRequest request, String appIdStr) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return createCorsOk();
        }

        int appId;
        try {
            appId = Integer.parseInt(appIdStr);
        } catch (NumberFormatException e) {
            return createError(400, "Invalid appId");
        }

        DB.App theApp = DB.findAppById(appId);
        String method = request.getMethod().toUpperCase();
        switch (method) {
            case "GET":
                if (theApp == null) {
                    return createError(404, "App not found");
                }
                return createJsonResponse(200, toJson(theApp));
            case "PUT":
                if (theApp == null) {
                    return createError(404, "App not found");
                }
                Map<String, Object> body = parseJsonMap(request.getTextBody());
                String newName = (String) body.get("name");
                String newRuntime = (String) body.get("runtime");
                if (newName != null) {
                    theApp.name = newName;
                }
                if (newRuntime != null) {
                    theApp.runtime = newRuntime;
                }
                DB.updateApp(theApp);
                return createJsonResponse(200, toJson(theApp));
            case "DELETE":
                if (theApp == null) {
                    return createError(404, "App not found");
                }
                DB.deleteApp(appId);
                return createJsonResponse(204, "");
            default:
                return createError(405, "Method Not Allowed");
        }
    }

    private HttpResponse handleAppStatus(HttpRequest request, String appIdStr) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return createCorsOk();
        }
        int appId;
        try {
            appId = Integer.parseInt(appIdStr);
        } catch (NumberFormatException e) {
            return createError(400, "Invalid appId");
        }

        DB.App theApp = DB.findAppById(appId);
        if (theApp == null) {
            return createError(404, "App not found");
        }
        if (!"PUT".equalsIgnoreCase(request.getMethod())) {
            return createError(405, "Method Not Allowed");
        }

        Map<String, Object> json = parseJsonMap(request.getTextBody());
        String newStatus = (String) json.get("status");
        if (newStatus == null) {
            return createError(400, "Missing status");
        }

        theApp.status = newStatus;
        DB.updateApp(theApp);
        return createJsonResponse(200, toJson(theApp));
    }

    private HttpResponse handleUploadZip(HttpRequest request, String appIdStr) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return createCorsOk();
        }
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return createError(405, "Method Not Allowed");
        }

        int appId;
        try {
            appId = Integer.parseInt(appIdStr);
        } catch (NumberFormatException e) {
            return createError(400, "Invalid appId");
        }
        DB.App theApp = DB.findAppById(appId);
        if (theApp == null) {
            return createError(404, "App not found");
        }

        byte[] rawBody = request.getRawBody();
        if (rawBody == null || rawBody.length == 0) {
            return createError(400, "No ZIP data in request body");
        }

        try (InputStream zipStream = new ByteArrayInputStream(rawBody)) {
            List<String> extractedFiles = azureInterface.upload(appId, zipStream);

            for (String filename : extractedFiles) {
                String route = "/app_" + appId + "/" + filename;
                theApp.routes.add(route);

                processor.addRoute(route, (req) -> {
                    if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
                        return createCorsOk();
                    }
                    try (InputStream is = azureInterface.download(appId, "/" + filename)) {
                        if (is == null) {
                            return createError(404, "File not found in Azure: " + filename);
                        }
                        byte[] fileContent = is.readAllBytes();
                        String mime = MimeTypes.getMimeType(filename);
                        return new HttpResponse.Builder()
                                .setStatusCode(200)
                                .addHeader("Access-Control-Allow-Origin", "*")
                                .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                                .addHeader("Access-Control-Allow-Headers", "Content-Type")
                                .addHeader("Content-Type", mime)
                                .setRawBody(fileContent)
                                .build();
                    } catch (Exception e) {
                        return createError(500, "Azure download error: " + e.getMessage());
                    }
                });
            }
            DB.updateApp(theApp);

            Map<String, Object> resp = new HashMap<>();
            resp.put("appId", appId);
            resp.put("message", "Upload success");
            resp.put("updatedRoutes", theApp.routes);
            return createJsonResponse(200, toJson(resp));

        } catch (Exception e) {
            return createError(500, "Error uploading ZIP: " + e.getMessage());
        }
    }




    //--------------------------------------------------------------------------
    // /api/login
    //--------------------------------------------------------------------------
    private HttpResponse handleLogin(HttpRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return createCorsOk();
        }
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return createError(405, "Method Not Allowed");
        }

        // Parse JSON from body
        Map<String, Object> body = parseJsonMap(request.getTextBody());
        String tenantEmail = (String) body.get("tenantEmail");
        String username    = (String) body.get("username");
        String password    = (String) body.get("password");

        if (tenantEmail == null || username == null || password == null) {
            return createError(400, "Missing tenantEmail, username, or password");
        }

        // 1) Find tenant by email
        DB.Tenant tenant = DB.findTenantByEmail(tenantEmail);
        if (tenant == null) {
            return createError(401, "Invalid tenant email");
        }

        // 2) Find user by username
        DB.User user = DB.findUserByUsername(username);
        if (user == null) {
            return createError(401, "Invalid credentials");
        }

        // 3) Check if user is part of that tenant
        if (user.tenantId != tenant.tenantId) {
            return createError(403, "User does not belong to this tenant");
        }

        // 4) Check password
        boolean valid = DB.checkPassword(password, user.passwordHash);
        if (!valid) {
            return createError(401, "Invalid credentials");
        }

        // Success
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.userId);
        data.put("tenantId", tenant.tenantId);
        data.put("role", user.role);
        data.put("tenantName", tenant.tenantName);

        return createJsonResponse(200, toJson(data));
    }

    //--------------------------------------------------------------------------
    // /api/tenants
    //--------------------------------------------------------------------------
    private HttpResponse handleTenantsRoute(HttpRequest request) {
        String path = request.getBasePath(); // /api/tenants or /api/tenants/101/apps/2002/metrics
        String method = request.getMethod().toUpperCase();

        if ("/api/tenants".equals(path)) {
            if ("OPTIONS".equalsIgnoreCase(method)) {
                return createCorsOk();
            }
            switch (method) {
                case "GET":
                    return listTenants();
                case "POST":
                    return createTenant(request);
                default:
                    return createError(405, "Method Not Allowed");
            }
        }

        // Otherwise, subpaths. e.g. /api/tenants/101/apps/2002
        String subPath = path.substring("/api/tenants".length()); // => /101/apps/2002
        if (subPath.startsWith("/")) {
            subPath = subPath.substring(1); // => 101/apps/2002
        }
        if (subPath.isEmpty()) {
            return createError(404, "Not Found");
        }
        String[] parts = subPath.split("/");

        // If parts.length == 4 => e.g. 101/apps/2002/metrics
        // Specifically check if parts[1] == "apps" and parts[3] == "metrics"
        if (parts.length == 4
                && "apps".equalsIgnoreCase(parts[1])
                && "metrics".equalsIgnoreCase(parts[3])) {
            // so path is: /api/tenants/{tenantId}/apps/{appId}/metrics
            return handleTenantAppMetrics(request, parts[0], parts[2]);
        }

        // otherwise handle the normal logic (existing code)...

        // /api/tenants/101 => single tenant
        if (parts.length == 1) {
            return handleSingleTenant(request, parts[0]);
        }
        else if (parts.length == 2 && "apps".equalsIgnoreCase(parts[1])) {
            return handleTenantAppsCollection(request, parts[0]);
        }
        else if (parts.length == 3 && "apps".equalsIgnoreCase(parts[1])) {
            return handleSingleAppUnderTenant(request, parts[0], parts[2]);
        }
        else if (parts.length == 4 && "apps".equalsIgnoreCase(parts[1]) && "upload".equalsIgnoreCase(parts[3])) {
            return handleTenantAppUpload(request, parts[0], parts[2]);
        }

        return createError(404, "Not Found");
    }
    
    private HttpResponse handleSingleTenant(HttpRequest request, String tenantIdStr) {
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
        return createCorsOk();
    }

    // 1) Parse tenantId
    int tenantId;
    try {
        tenantId = Integer.parseInt(tenantIdStr);
    } catch (NumberFormatException e) {
        return createError(400, "Invalid tenantId");
    }

    // 2) Fetch tenant from DB
    DB.Tenant tenant = DB.findTenantById(tenantId);
    if (tenant == null) {
        return createError(404, "Tenant not found");
    }

    String method = request.getMethod().toUpperCase();
    switch (method) {
        case "GET": {
            // Return the single tenant’s info
            return createJsonResponse(200, toJson(tenant));
        }
        case "PUT": {
            // Possibly handle rename or other tenant updates
            Map<String, Object> data = parseJsonMap(request.getTextBody());
            String newName = (String) data.get("tenantName");
            if (newName != null && !newName.isEmpty()) {
                tenant.tenantName = newName;
                DB.updateTenant(tenant);
            }
            return createJsonResponse(200, toJson(tenant));
        }
        case "DELETE": {
            // If you want to delete a tenant
            boolean ok = DB.deleteTenant(tenantId);
            if (!ok) {
                return createError(500, "Failed to remove tenant " + tenantId);
            }
            // Return 204 on success
            return createJsonResponse(204, "");
        }
        default:
            return createError(405, "Method Not Allowed");
    }
}


    private HttpResponse listTenants() {
        // ### CHANGED: Instead of DB.getRoot().tenants, do something like DB.listAllTenants().
        List<DB.Tenant> tenants = DB.listAllTenants(); // you’d implement in DB
        return createJsonResponse(200, toJson(tenants));
    }


    private HttpResponse createTenant(HttpRequest request) {
    Map<String, Object> body = parseJsonMap(request.getTextBody());
    String tenantName = (String) body.get("tenantName");
    String tenantEmail = (String) body.get("tenantEmail");

    if (tenantName == null || tenantName.isEmpty()) {
        return createError(400, "tenantName is required");
    }

    if (tenantEmail == null || tenantEmail.isEmpty()) {
        return createError(400, "tenantEmail is required");
    }

    // Check if the email is already in use
    DB.Tenant existingTenant = DB.findTenantByEmail(tenantEmail);
    if (existingTenant != null) {
        return createError(409, "Tenant email already exists");
    }

    DB.Tenant newTenant = new DB.Tenant();
    newTenant.tenantId = generateTenantId();
    newTenant.tenantName = tenantName;
    newTenant.tenantEmail = tenantEmail;

    DB.createTenant(newTenant);

    // Include success message in response
    Map<String, Object> response = new HashMap<>();
    response.put("tenantId", newTenant.tenantId);
    response.put("tenantName", newTenant.tenantName);
    response.put("tenantEmail", newTenant.tenantEmail);
    response.put("message", "Tenant organization created successfully");

    return createJsonResponse(201, toJson(response));
}


    //--------------------------------------------------------------------------
    // Handling Apps Under a Tenant
    //--------------------------------------------------------------------------
    private HttpResponse handleTenantAppsCollection(HttpRequest request, String tenantIdStr) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return createCorsOk();
        }
        int tenantId;
        try {
            tenantId = Integer.parseInt(tenantIdStr);
        } catch (NumberFormatException e) {
            return createError(400, "Invalid tenantId");
        }

        DB.Tenant tenant = DB.findTenantById(tenantId);
        if (tenant == null) {
            return createError(404, "Tenant not found");
        }

        String method = request.getMethod().toUpperCase();
        switch (method) {
            case "GET":
                // ### CHANGED: use DB.listAppsForTenant(tenantId) or similar
                List<DB.App> tenantApps = DB.listAppsForTenant(tenantId);
                return createJsonResponse(200, toJson(tenantApps));
            case "POST":
                Map<String, Object> body = parseJsonMap(request.getTextBody());
                String name = (String) body.get("name");
                String runtime = (String) body.get("runtime");
                Double ownerIdDbl = (Double) body.get("ownerUserId");

                if (name == null || runtime == null || ownerIdDbl == null) {
                    return createError(400, "Missing fields: name, runtime, ownerUserId");
                }
                int ownerUserId = ownerIdDbl.intValue();

                DB.App newApp = new DB.App();
                newApp.appId = generateAppId();
                newApp.tenantId = tenantId;
                newApp.name = name;
                newApp.runtime = runtime;
                newApp.status = "stopped";
                newApp.ownerUserId = ownerUserId;
                newApp.routes = new ArrayList<>();

                // ### CHANGED
                DB.createApp(newApp);

                return createJsonResponse(201, toJson(newApp));
            default:
                return createError(405, "Method Not Allowed");
        }
    }

    private HttpResponse handleSingleAppUnderTenant(HttpRequest request, String tenantIdStr, String appIdStr) {
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
        return createCorsOk();
    }

    int tenantId, appId;
    try {
        tenantId = Integer.parseInt(tenantIdStr);
        appId    = Integer.parseInt(appIdStr);
    } catch (NumberFormatException e) {
        return createError(400, "Invalid tenantId or appId");
    }

    DB.App theApp = DB.findAppByTenant(tenantId, appId);
    if (theApp == null) {
        return createError(404, "App not found for this tenant");
    }

    String method = request.getMethod().toUpperCase();
    switch (method) {
        case "GET": {
            return createJsonResponse(200, toJson(theApp));
        }
        case "PUT": {
            // logic to update the app (newName, newRuntime, newStatus)...
            return createJsonResponse(200, toJson(theApp));
        }
        case "DELETE": {
            // 1) Unregister routes
            for (String route : theApp.routes) {
                boolean removed = processor.removeRoute(route);
                if (removed) {
                    Logger.info("Removed route: " + route);
                }
            }

            // 2) Delete from Azure
            try {
                int deletedCount = azureInterface.delete(appId);
                Logger.info("Azure Blob deletion for app " + appId + " => " + deletedCount + " files removed");
            } catch (Exception ex) {
                Logger.error("Azure file deletion failed for app " + appId, ex);
            }

            // 3) Remove from DB
            boolean success = DB.deleteApp(appId);
            if (success) {
                Logger.info("App " + appId + " successfully removed from tenant " + tenantId);
                return createJsonResponse(204, "");
            } else {
                return createError(500, "Failed to remove app in DB");
            }
        }
        default:
            return createError(405, "Method Not Allowed");
    }
}

    private HttpResponse handleTenantAppUpload(HttpRequest request, String tenantIdStr, String appIdStr) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return createCorsOk();
        }
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return createError(405, "Method Not Allowed");
        }

        int tenantId, appId;
        try {
            tenantId = Integer.parseInt(tenantIdStr);
            appId    = Integer.parseInt(appIdStr);
        } catch (NumberFormatException e) {
            return createError(400, "Invalid tenantId or appId");
        }

        DB.App theApp = DB.findAppByTenant(tenantId, appId); // or findAppById + check
        if (theApp == null) {
            return createError(404, "App not found for tenant " + tenantId);
        }

        byte[] rawBody = request.getRawBody();
        if (rawBody == null || rawBody.length == 0) {
            return createError(400, "No ZIP data in request body");
        }

        try (InputStream zipStream = new ByteArrayInputStream(rawBody)) {
            List<String> extractedFiles = azureInterface.upload(appId, zipStream);

            // Append new routes
            for (String filename : extractedFiles) {
                String route = "/app_" + appId + "/" + filename;
                theApp.routes.add(route);

                // Also define a new route in the processor
                processor.addRoute(route, (req) -> {
                    if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
                        return createCorsOk();
                    }
                    try (InputStream is = azureInterface.download(appId, "/" + filename)) {
                        if (is == null) {
                            return createError(404, "File not found in Azure: " + filename);
                        }
                        byte[] content = is.readAllBytes();
                        String mime = MimeTypes.getMimeType(filename);
                        return new HttpResponse.Builder()
                                .setStatusCode(200)
                                .addHeader("Access-Control-Allow-Origin", "*")
                                .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                                .addHeader("Access-Control-Allow-Headers", "Content-Type")
                                .addHeader("Content-Type", mime)
                                .setRawBody(content)
                                .build();
                    } catch (Exception e) {
                        return createError(500, "Azure download error: " + e.getMessage());
                    }
                });
            }
            // ### CHANGED: persist updated routes to DB
            DB.updateApp(theApp);

            Map<String, Object> resp = new HashMap<>();
            resp.put("appId", appId);
            resp.put("tenantId", tenantId);
            resp.put("message", "Upload success");
            resp.put("updatedRoutes", theApp.routes);
            return createJsonResponse(200, toJson(resp));

        } catch (Exception e) {
            e.printStackTrace();
            return createError(500, "Error uploading ZIP: " + e.getMessage());
        }
    }

    /**
     * New method to handle /api/tenants/{tenantId}/apps/{appId}/metrics
     */
    private HttpResponse handleTenantAppMetrics(HttpRequest request, String tenantIdStr, String appIdStr) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return createCorsOk();
        }
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return createError(405, "Method Not Allowed");
        }

        int tenantId, appId;
        try {
            tenantId = Integer.parseInt(tenantIdStr);
            appId    = Integer.parseInt(appIdStr);
        } catch (NumberFormatException e) {
            return createError(400, "Invalid tenantId or appId");
        }

        // ensure app belongs to that tenant
        DB.App theApp = DB.findAppByTenant(tenantId, appId);
        if (theApp == null) {
            return createError(404, "App not found or not in this tenant");
        }

        // fetch from Telemetry
        Map<String,Object> appMetrics = Telemetry.getAppMetrics(appId);

        return createJsonResponse(200, toJson(appMetrics));
    }

    // The old /api/metrics => for global server metrics
    private HttpResponse handleMetrics(HttpRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return createCorsOk();
        }
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return createError(405, "Method Not Allowed");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("cpuUtilization", Telemetry.getCpuUsage());
        data.put("memoryUsage", Telemetry.getMemoryUsage());
        data.put("avgResponseTime", Telemetry.getAvgResponseTime());
        data.put("errorRate", Telemetry.getErrorRate());
        data.put("systemLoad", Telemetry.getSystemLoad());
        data.put("performanceData", Telemetry.getPerformanceData());
        return createJsonResponse(200, toJson(data));
    }

    private HttpResponse createCorsOk() {
        return new HttpResponse.Builder()
            .setStatusCode(200)
            .addHeader("Access-Control-Allow-Origin", "*")
            .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
            .addHeader("Access-Control-Allow-Headers", "Content-Type")
            .setBody("")
            .build();
    }

    private HttpResponse createError(int code, String message) {
        return new HttpResponse.Builder()
            .setStatusCode(code)
            .addHeader("Access-Control-Allow-Origin", "*")
            .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
            .addHeader("Access-Control-Allow-Headers", "Content-Type")
            .addHeader("Content-Type", "application/json")
            .setBody("{\"error\":\"" + message + "\"}")
            .build();
    }

    private HttpResponse createJsonResponse(int statusCode, String json) {
        return new HttpResponse.Builder()
            .setStatusCode(statusCode)
            .addHeader("Access-Control-Allow-Origin", "*")
            .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
            .addHeader("Access-Control-Allow-Headers", "Content-Type")
            .addHeader("Content-Type", "application/json")
            .setBody(json)
            .build();
    }

    private String toJson(Object data) {
        return new GsonBuilder().serializeSpecialFloatingPointValues().create().toJson(data);
    }

    private Map<String,Object> parseJsonMap(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return new com.google.gson.Gson().fromJson(
                    json,
                    new com.google.gson.reflect.TypeToken<Map<String,Object>>(){}.getType()
            );
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    //--------------------------------------------------------------------------
    // ID Generation (if you’re not using auto-increment in the DB)
    //--------------------------------------------------------------------------
    private static int lastTenantId = 380;
    private static int generateTenantId() {
        int maxId = 0;
        List<DB.Tenant> allTenants = DB.listAllTenants();
        for (DB.Tenant t : allTenants) {
            if (t.tenantId > maxId) {
                maxId = t.tenantId;
            }
        }
        return (maxId >= lastTenantId) ? maxId + 1 : ++lastTenantId;
    }

    private static int lastAppId = 789;
    private static int generateAppId() {
        int maxId = 0;
        List<DB.App> allApps = DB.listAllApps();
        for (DB.App a : allApps) {
            if (a.appId > maxId) {
                maxId = a.appId;
            }
        }
        return (maxId >= lastAppId) ? maxId + 1 : ++lastAppId;
    }

    private static int lastUserId = 372;
    private static int generateUserId() {
        int maxId = 0;
        List<DB.User> allUsers = DB.listAllUsers();
        for (DB.User u : allUsers) {
            if (u.userId > maxId) {
                maxId = u.userId;
            }
        }
        return (maxId >= lastUserId) ? maxId + 1 : ++lastUserId;
    }
}

