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
import java.util.stream.Collectors;

public class ConnectionHandler implements Runnable {

    private final Socket clientSocket;
    private final HttpParser parser;
    private final RequestProcessor processor;
    private final AzureBlobInterface azureInterface;

    public ConnectionHandler(Socket socket) {
        this.clientSocket = socket;
        this.parser = new HttpParser();

        // Create our FileService & Processor fresh each time.
        ConfigLoader config = new ConfigLoader();
        String webRoot = config.getWebRoot();
        FileService fileService = new FileService(webRoot);
        this.processor = new RequestProcessor(fileService);

        // The AzureBlobInterface
        this.azureInterface = new AzureBlobInterface();

        // Register built-in routes (/api/..., etc.)
        defineRoutes();

        // Now re-register all dynamic routes from DB for each app & route
        reRegisterAppRoutes();
    }


    @Override
    public void run() {
        handle();
    }

    private void handle() {
        long startTime = System.currentTimeMillis();
        HttpRequest request = null;
        try {
            // Parse request
            request = parser.parse(clientSocket.getInputStream());
            Telemetry.incrementNumberRequests();

            // Process
            HttpResponse response = processor.process(request);
            response.write(clientSocket.getOutputStream());

            // Telemetry
            Telemetry.trackResponseTime(startTime);

            Telemetry.trackServerMetrics(System.currentTimeMillis());

            clientSocket.close();
        } catch (Exception e) {
            Logger.error("Error handling connection", e);
            Telemetry.trackFailures(e, (request != null) ? request.getPath() : "unknown",
                    "Failed to process request: " + e.getMessage());
        }
    }

    private void reRegisterAppRoutes() {
        // Loop over each app
        for (DB.App app : DB.getRoot().apps) {
            // For each route, e.g. "/app_2003/index.html"
            for (String route : app.routes) {
                // If we already have it, skip. Otherwise, define it:
                if (!processor.hasRoute(route)) {
                    processor.addRoute(route, (req) -> {
                        try {
                            String prefix = "/app_" + app.appId;
                            String filePath = route.substring(prefix.length());
                            // => "/index.html"

                            try (InputStream is = azureInterface.download(app.appId, filePath)) {
                                if (is == null) {
                                    return new HttpResponse.Builder()
                                            .setStatusCode(404)
                                            .setBody("File not found in Azure: " + filePath)
                                            .build();
                                }
                                byte[] content = is.readAllBytes();

                                // Derive MIME type
                                // might have a utility method for this:
                                String mime = com.webserver.util.MimeTypes.getMimeType(filePath);

                                return new HttpResponse.Builder()
                                        .setStatusCode(200)
                                        .addHeader("Content-Type", mime)
                                        .setRawBody(content)
                                        .build();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return new HttpResponse.Builder()
                                    .setStatusCode(500)
                                    .setBody("Error streaming from Azure: " + e.getMessage())
                                    .build();
                        }
                    });
                }
            }
        }
    }



    private void defineRoutes() {
        // We'll also define a root route for a friendly message
        processor.addRoute("/", req -> new HttpResponse.Builder()
                .setStatusCode(200)
                .addHeader("Content-Type", "text/plain")
                .setBody("Welcome to Cloudle Web Server!")
                .build()
        );

        // For Tenants
        processor.addRoute("/api/tenants", this::handleTenantsRoute);

        // For Apps (top-level)
        processor.addRoute("/api/apps", this::handleAppsCollection);
        processor.addRoute("/api/apps/", this::handleAppSubpaths);

        // A simple refresh route
        processor.addRoute("/api/refresh", this::handleRefresh);


        processor.addRoute("/api/metrics", this::handleMetrics);


    }


    private HttpResponse handleTenantsRoute(HttpRequest request) {
        String path = request.getBasePath();  // e.g. "/api/tenants/101/apps/2002"
        String method = request.getMethod().toUpperCase();

        // 1) If exactly "/api/tenants"
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

        String subPath = path.substring("/api/tenants".length()); // => "/101/apps/2002"
        if (subPath.startsWith("/")) {
            subPath = subPath.substring(1); // => "101/apps/2002"
        }
        if (subPath.isEmpty()) {
            // means user typed /api/tenants/ with trailing slash => 404 or redirect
            return createError(404, "Not Found");
        }

        String[] parts = subPath.split("/");

        if (parts.length == 1) {
            // /api/tenants/101 => single tenant
            return handleSingleTenant(request, parts[0]);
        }
        else if (parts.length == 2 && "apps".equalsIgnoreCase(parts[1])) {
            // /api/tenants/101/apps => the collection of apps under that tenant
            return handleTenantAppsCollection(request, parts[0]);
        }
        else if (parts.length == 3 && "apps".equalsIgnoreCase(parts[1])) {
            // /api/tenants/101/apps/2002 => single app under that tenant
            String tenantIdStr = parts[0];
            String appIdStr = parts[2];
            return handleSingleAppUnderTenant(request, tenantIdStr, appIdStr);
        }
        else if (parts.length == 4
                && "apps".equalsIgnoreCase(parts[1])
                && "upload".equalsIgnoreCase(parts[3])) {
            // e.g. subPath = "101/apps/2002/upload"
            String tenantIdStr = parts[0]; // "101"
            String appIdStr    = parts[2]; // "2002"
            return handleTenantAppUpload(request, tenantIdStr, appIdStr);
        }


        // If none matched, 404
        return createError(404, "Not Found");
    }

    private HttpResponse handleMetrics(HttpRequest request) {
        // 1) Ensure it's a GET request
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return createError(405, "Method Not Allowed");
        }

        // 2) Gather real data from Telemetry
        Map<String, Object> data = new HashMap<>();
        data.put("cpuUtilization", Telemetry.getCpuUsage());
        data.put("memoryUsage", Telemetry.getMemoryUsage());
        data.put("avgResponseTime", Telemetry.getAvgResponseTime());
        data.put("errorRate", Telemetry.getErrorRate());
        data.put("systemLoad", Telemetry.getSystemLoad());
        data.put("performanceData", Telemetry.getPerformanceData());

        // 3) Return JSON
        return createJsonResponse(200, toJson(data));
    }


    private HttpResponse handleTenantAppUpload(HttpRequest request, String tenantIdStr, String appIdStr) {
        // 1) Check method
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return createError(405, "Method Not Allowed");
        }

        // 2) Parse IDs
        int tenantId;
        int appId;
        try {
            tenantId = Integer.parseInt(tenantIdStr);
            appId    = Integer.parseInt(appIdStr);
        } catch (NumberFormatException e) {
            return createError(400, "Invalid tenantId or appId");
        }

        // 3) Find the app in DB, verifying tenant
        DB.App theApp = DB.getRoot().apps.stream()
                .filter(a -> a.appId == appId && a.tenantId == tenantId)
                .findFirst()
                .orElse(null);

        if (theApp == null) {
            return createError(404, "App not found for tenant " + tenantId);
        }

        // 4) Read raw body as .zip
        byte[] rawBody = request.getRawBody();
        if (rawBody == null || rawBody.length == 0) {
            return createError(400, "No ZIP data in request body");
        }

        try (InputStream zipStream = new ByteArrayInputStream(rawBody)) {
            // 5) Upload to Azure
            List<String> extractedFiles = azureInterface.upload(appId, zipStream);

            // 6) Register routes in DB
            for (String filename : extractedFiles) {
                String route = "/app_" + appId + "/" + filename;
                theApp.routes.add(route);

                // Also create a dynamic route in RequestProcessor, if you want
                processor.addRoute(route, req -> {
                    try (InputStream is = azureInterface.download(appId, "/" + filename)) {
                        if (is == null) {
                            return createError(404, "File not found in Azure: " + filename);
                        }
                        byte[] content = is.readAllBytes();
                        String mime = MimeTypes.getMimeType(filename);
                        return new HttpResponse.Builder()
                                .setStatusCode(200)
                                .addHeader("Content-Type", mime)
                                .setRawBody(content)
                                .build();
                    } catch (Exception e) {
                        return createError(500, "Azure download error: " + e.getMessage());
                    }
                });
            }

            DB.save();

            // 7) Return success response
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



    private HttpResponse handleTenantAppsCollection(HttpRequest request, String tenantIdStr) {
        // 1) Parse the tenant ID
        int tenantId;
        try {
            tenantId = Integer.parseInt(tenantIdStr);
        } catch (NumberFormatException e) {
            return createError(400, "Invalid tenantId");
        }

        // 2) Possibly check if the tenant actually exists (optional but recommended)
        DB.Tenant tenant = DB.getRoot().tenants.stream()
                .filter(t -> t.tenantId == tenantId)
                .findFirst()
                .orElse(null);
        if (tenant == null) {
            return createError(404, "Tenant not found");
        }

        // 3) Handle the HTTP method
        String method = request.getMethod().toUpperCase();
        if ("OPTIONS".equals(method)) {
            return createCorsOk();
        }

        switch (method) {
            case "GET":
                // Return all apps for that tenant
                List<DB.App> tenantApps = DB.getRoot().apps.stream()
                        .filter(a -> a.tenantId == tenantId)
                        .toList();
                return createJsonResponse(200, toJson(tenantApps));

            case "POST":
                // Create a new app under this tenant
                Map<String, Object> body = parseJsonMap(request.getTextBody());
                String name = (String) body.get("name");
                String runtime = (String) body.get("runtime");
                Double ownerIdDbl = (Double) body.get("ownerUserId");

                if (name == null || runtime == null || ownerIdDbl == null) {
                    return createError(400, "Missing fields: name, runtime, ownerUserId");
                }
                int ownerUserId = ownerIdDbl.intValue();

                // (Optional) verify that user with ownerUserId exists,
                // and that user belongs to this same tenant, etc.

                // 4) Create the new App
                DB.App newApp = new DB.App();
                newApp.appId = generateAppId();
                newApp.tenantId = tenantId;
                newApp.name = name;
                newApp.runtime = runtime;
                newApp.status = "stopped";
                newApp.ownerUserId = ownerUserId;
                newApp.routes = new ArrayList<>();

                // 5) Add to DB
                DB.getRoot().apps.add(newApp);
                DB.save();

                // 6) Return 201 Created
                return createJsonResponse(201, toJson(newApp));

            default:
                return createError(405, "Method Not Allowed");
        }
    }


    private HttpResponse handleSingleAppUnderTenant(HttpRequest request, String tenantIdStr, String appIdStr) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return createCorsOk();
        }

        // 1) Parse IDs
        int tenantId;
        int appId;
        try {
            tenantId = Integer.parseInt(tenantIdStr);
            appId    = Integer.parseInt(appIdStr);
        } catch (NumberFormatException e) {
            return createError(400, "Invalid tenantId or appId");
        }

        // 2) Check that the tenant and app exist, and that the app belongs to this tenant
        DB.App theApp = DB.getRoot().apps.stream()
                .filter(a -> a.appId == appId && a.tenantId == tenantId)
                .findFirst()
                .orElse(null);

        // 3) Switch on the HTTP method
        String method = request.getMethod().toUpperCase();
        switch (method) {
            case "GET":
                if (theApp == null) {
                    return createError(404, "App not found for this tenant");
                }
                return createJsonResponse(200, toJson(theApp));

            case "PUT":
                if (theApp == null) {
                    return createError(404, "App not found for this tenant");
                }
                // parse JSON from request body
                Map<String,Object> body = parseJsonMap(request.getTextBody());
                String newName    = (String) body.get("name");
                String newRuntime = (String) body.get("runtime");
                String newStatus  = (String) body.get("status"); // optional

                if (newName != null && !newName.isEmpty()) {
                    theApp.name = newName;
                }
                if (newRuntime != null && !newRuntime.isEmpty()) {
                    theApp.runtime = newRuntime;
                }
                if (newStatus != null && !newStatus.isEmpty()) {
                    theApp.status = newStatus; // "running","stopped"
                }
                DB.save();
                return createJsonResponse(200, toJson(theApp));

            case "DELETE":
                if (theApp == null) {
                    return createError(404, "App not found for this tenant");
                }
                DB.getRoot().apps.remove(theApp);
                DB.save();
                // 204 No Content
                return createJsonResponse(204, "");

            default:
                return createError(405, "Method Not Allowed");
        }
    }


    private HttpResponse handleTenantsCollection(HttpRequest request) {
        if (isOptions(request)) return createCorsOk();
        String method = request.getMethod();

        switch (method.toUpperCase()) {
            case "GET":
                // List all tenants
                List<DB.Tenant> tenants = DB.getRoot().tenants;
                return createJsonResponse(200, toJson(tenants));

            case "POST":
                // Create a new tenant
                Map<String, Object> tenantData = parseJsonMap(request.getTextBody());
                String tenantName = (String) tenantData.get("tenantName");
                if (tenantName == null || tenantName.isEmpty()) {
                    return createError(400, "tenantName is required");
                }
                // Generate an ID
                int newTenantId = generateNewTenantId();
                DB.Tenant newTenant = new DB.Tenant();
                newTenant.tenantId = newTenantId;
                newTenant.tenantName = tenantName;

                DB.getRoot().tenants.add(newTenant);
                DB.save();

                return createJsonResponse(201, toJson(newTenant));

            default:
                return createError(405, "Method Not Allowed");
        }
    }

    private HttpResponse handleTenantsSubpaths(HttpRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return createCorsOk();
        }

        // e.g. path = /api/tenants/123, so let’s remove "/api/tenants"
        String path = request.getPath();  // /api/tenants/123
        String subPath = path.substring("/api/tenants".length()); // => /123
        if (subPath.startsWith("/")) {
            subPath = subPath.substring(1); // => 123
        }
        if (subPath.isEmpty()) {
            // means user typed /api/tenants/ with trailing slash but no ID => 404 or redirect
            return createError(404, "Not Found");
        }

        String[] parts = subPath.split("/");
        if (parts.length == 1) {
            // /api/tenants/123 => single tenant
            return handleSingleTenant(request, parts[0]);
        } else if (parts.length == 2 && "apps".equalsIgnoreCase(parts[1])) {
            // /api/tenants/123/apps
            return handleTenantApps(request, parts[0]);
        } else {
            return createError(404, "Not Found");
        }
    }

    private HttpResponse handleSingleTenant(HttpRequest request, String tenantIdStr) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return createCorsOk();
        }

        int tenantId;
        try {
            tenantId = Integer.parseInt(tenantIdStr);
        } catch (NumberFormatException e) {
            return createError(400, "Invalid tenantId");
        }
        DB.Tenant tenant = DB.getRoot().tenants.stream()
                .filter(t -> t.tenantId == tenantId)
                .findFirst()
                .orElse(null);

        String method = request.getMethod().toUpperCase();
        switch (method) {
            case "GET":
                if (tenant == null) {
                    return createError(404, "Tenant not found");
                }
                return createJsonResponse(200, toJson(tenant));

            case "PUT":
                if (tenant == null) {
                    return createError(404, "Tenant not found");
                }
                Map<String, Object> data = parseJsonMap(request.getTextBody());
                String newName = (String) data.get("tenantName");
                if (newName != null && !newName.isEmpty()) {
                    tenant.tenantName = newName;
                    DB.save();
                }
                return createJsonResponse(200, toJson(tenant));

            case "DELETE":
                if (tenant == null) {
                    return createError(404, "Tenant not found");
                }
                DB.getRoot().tenants.remove(tenant);
                DB.save();
                return createJsonResponse(204, "");

            default:
                return createError(405, "Method Not Allowed");
        }
    }

    private HttpResponse handleTenantApps(HttpRequest request, String tenantIdStr) {
        int tenantId;
        try {
            tenantId = Integer.parseInt(tenantIdStr);
        } catch (NumberFormatException e) {
            return createError(400, "Invalid tenantId");
        }

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return createCorsOk();
        }

        switch (request.getMethod().toUpperCase()) {
            case "GET":
                List<DB.App> tenantApps = DB.getRoot().apps.stream()
                        .filter(a -> a.tenantId == tenantId)
                        .toList();
                return createJsonResponse(200, toJson(tenantApps));

            case "POST":
                Map<String, Object> body = parseJsonMap(request.getTextBody());
                String name = (String) body.get("name");
                String runtime = (String) body.get("runtime");
                Double ownerIdD = (Double) body.get("ownerUserId");
                if (name == null || runtime == null || ownerIdD == null) {
                    return createError(400, "Missing fields: name, runtime, ownerUserId");
                }
                DB.App newApp = new DB.App();
                newApp.appId = generateAppId();
                newApp.tenantId = tenantId;
                newApp.name = name;
                newApp.runtime = runtime;
                newApp.status = "stopped";
                newApp.ownerUserId = ownerIdD.intValue();
                newApp.routes = new java.util.ArrayList<>();

                DB.getRoot().apps.add(newApp);
                DB.save();
                return createJsonResponse(201, toJson(newApp));

            default:
                return createError(405, "Method Not Allowed");
        }
    }

    private int generateTenantId() {
        List<DB.Tenant> tenants = DB.getRoot().tenants;
        int maxId = 0;
        for (DB.Tenant t : tenants) {
            if (t.tenantId > maxId) {
                maxId = t.tenantId;
            }
        }
        return maxId + 1;
    }

    private int generateAppId() {
        List<DB.App> apps = DB.getRoot().apps;
        int maxId = 0;
        for (DB.App a : apps) {
            if (a.appId > maxId) {
                maxId = a.appId;
            }
        }
        return maxId + 1;
    }

    private HttpResponse listTenants() {
        List<DB.Tenant> tenants = DB.getRoot().tenants;
        return createJsonResponse(200, toJson(tenants));
    }

    private HttpResponse createTenant(HttpRequest request) {
        Map<String, Object> body = parseJsonMap(request.getTextBody());
        String tenantName = (String) body.get("tenantName");
        if (tenantName == null || tenantName.isEmpty()) {
            return createError(400, "tenantName is required");
        }
        DB.Tenant newTenant = new DB.Tenant();
        newTenant.tenantId = generateTenantId(); // your ID logic
        newTenant.tenantName = tenantName;
        DB.getRoot().tenants.add(newTenant);
        DB.save();
        return createJsonResponse(201, toJson(newTenant));
    }

    private HttpResponse handleAppsCollection(HttpRequest request) {
        if (isOptions(request)) return createCorsOk();
        String method = request.getMethod().toUpperCase();

        if ("GET".equals(method)) {
            List<DB.App> allApps = DB.getRoot().apps;
            return createJsonResponse(200, toJson(allApps));
        }
        return createError(405, "Method Not Allowed");
    }

    private HttpResponse handleAppSubpaths(HttpRequest request) {
        if (isOptions(request)) return createCorsOk();
        String path = request.getPath();
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
            String action = parts[1];
            switch (action.toLowerCase()) {
                case "status":
                    return handleAppStatus(request, appIdStr);
                case "upload":
                    return handleUploadZip(request, appIdStr);
                default:
                    return createError(404, "Not Found");
            }
        }
        return createError(404, "Not Found");
    }

    private HttpResponse handleSingleApp(HttpRequest request, String appIdStr) {
        int appId;
        try {
            appId = Integer.parseInt(appIdStr);
        } catch (NumberFormatException e) {
            return createError(400, "Invalid appId");
        }

        DB.App theApp = findAppById(appId);
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
                if (newName != null) theApp.name = newName;
                if (newRuntime != null) theApp.runtime = newRuntime;
                DB.save();
                return createJsonResponse(200, toJson(theApp));

            case "DELETE":
                if (theApp == null) {
                    return createError(404, "App not found");
                }
                DB.getRoot().apps.remove(theApp);
                DB.save();
                return createJsonResponse(204, "");

            default:
                return createError(405, "Method Not Allowed");
        }
    }

    private HttpResponse handleAppStatus(HttpRequest request, String appIdStr) {
        int appId;
        try {
            appId = Integer.parseInt(appIdStr);
        } catch (NumberFormatException e) {
            return createError(400, "Invalid appId");
        }
        DB.App theApp = findAppById(appId);
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
        DB.save();
        return createJsonResponse(200, toJson(theApp));
    }

    private HttpResponse handleUploadZip(HttpRequest request, String appIdStr) {
        int appId;
        try {
            appId = Integer.parseInt(appIdStr);
        } catch (NumberFormatException e) {
            return createError(400, "Invalid appId");
        }
        DB.App theApp = findAppById(appId);
        if (theApp == null) {
            return createError(404, "App not found");
        }

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return createError(405, "Method Not Allowed");
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
                    try (InputStream is = azureInterface.download(appId, "/" + filename)) {
                        if (is == null) {
                            return createError(404, "File not found in Azure: " + filename);
                        }
                        byte[] fileContent = is.readAllBytes();
                        String mime = MimeTypes.getMimeType(filename);
                        return new HttpResponse.Builder()
                                .setStatusCode(200)
                                .addHeader("Content-Type", mime)
                                .setRawBody(fileContent)
                                .build();
                    } catch (Exception e) {
                        return createError(500, "Azure download error: " + e.getMessage());
                    }
                });
            }
            DB.save();

            Map<String, Object> resp = new HashMap<>();
            resp.put("appId", appId);
            resp.put("message", "Upload success");
            resp.put("updatedRoutes", theApp.routes);
            return createJsonResponse(200, toJson(resp));

        } catch (Exception e) {
            e.printStackTrace();
            return createError(500, "Error uploading ZIP: " + e.getMessage());
        }
    }

    private HttpResponse handleRefresh(HttpRequest request) {
        if (isOptions(request)) return createCorsOk();
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return createError(405, "Method Not Allowed");
        }
        DB.load(); // forcibly reload from disk
        return new HttpResponse.Builder()
                .setStatusCode(200)
                .addHeader("Access-Control-Allow-Origin", "*")
                .setBody("DB reloaded from disk")
                .build();
    }

    private boolean isOptions(HttpRequest req) {
        return "OPTIONS".equalsIgnoreCase(req.getMethod());
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
//        return new com.google.gson.Gson().toJson(data);
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

    private DB.App findAppById(int appId) {
        return DB.getRoot().apps.stream()
                .filter(a -> a.appId == appId)
                .findFirst().orElse(null);
    }

    private static int lastTenantId = 100;
    private static int generateNewTenantId() {
        return ++lastTenantId;
    }
    private static int lastAppId = 1000;
    private static int generateNewAppId() {
        return ++lastAppId;
    }

}
