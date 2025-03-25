
package com.webserver.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webserver.azure.AzureBlobInterface;
import com.webserver.db.DatabaseManager;
import com.webserver.http.HttpParser;
import com.webserver.http.RequestProcessor;
import com.webserver.model.HttpRequest;
import com.webserver.model.HttpResponse;
import com.webserver.util.ConfigLoader;
import com.webserver.util.FileService;
import com.webserver.util.Logger;
import com.webserver.util.Telemetry;

import com.webserver.util.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.mindrot.jbcrypt.BCrypt;


public class ConnectionHandler implements Runnable {

    private final Socket clientSocket;
    private final HttpParser parser;
    private final RequestProcessor processor;
    private final AzureBlobInterface azureInterface;
    private final DatabaseManager databaseManager;

    public ConnectionHandler(Socket socket) {
        this.clientSocket = socket;
        this.parser = new HttpParser();
        this.databaseManager = new DatabaseManager();

        // Load config and create FileService using the webroot
        ConfigLoader config = new ConfigLoader();
        String webRoot = config.getWebRoot();
        FileService fileService = new FileService(webRoot);

        // Pass FileService to RequestProcessor
        this.processor = new RequestProcessor(fileService);

        // Azure client
        this.azureInterface = new AzureBlobInterface();

        // Add some default/test routes
        processor.addRoute("/", request ->
                new HttpResponse.Builder()
                        .setStatusCode(200)
                        .setStatusMessage("OK")
                        .addHeader("Content-Type", "text/plain")
                        .setBody("Hello Group 18, Cloudle is Online!!")
                        .build()
        );

        processor.addRoute("/api/refresh", request -> {
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                return new HttpResponse.Builder()
                        .setStatusCode(200)
                        .addHeader("Access-Control-Allow-Origin", "*")
                        .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                        .addHeader("Access-Control-Allow-Headers", "Content-Type")
                        .setBody("")
                        .build();
            }
            if (!"POST".equalsIgnoreCase(request.getMethod())) {
                return createError(405, "Method Not Allowed");
            }
            DB.load();
            return new HttpResponse.Builder()
                    .setStatusCode(200)
                    .addHeader("Access-Control-Allow-Origin", "*")
                    .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    .addHeader("Access-Control-Allow-Headers", "Content-Type")
                    .setBody("DB reloaded from disk")
                    .build();
        });

        processor.addRoute("/api/applications", request -> {
            String method = request.getMethod();
            if ("OPTIONS".equalsIgnoreCase(method)) {
                return new HttpResponse.Builder()
                        .setStatusCode(200)
                        .addHeader("Access-Control-Allow-Origin", "*")
                        .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                        .addHeader("Access-Control-Allow-Headers", "Content-Type")
                        .setBody("")
                        .build();
            }


            if ("GET".equalsIgnoreCase(method)) {
                // handle listing apps
                String userIdParam = request.getQueryParam("userId").orElse(null);
                if (userIdParam == null) {
                    return createError(400, "Missing userId");
                }
                int userId = Integer.parseInt(userIdParam);
                List<DB.App> apps = AppManager.getUserApps(userId);
                return new HttpResponse.Builder()
                        .setStatusCode(200)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Access-Control-Allow-Origin", "*")      // <--- Add these
                        .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                        .addHeader("Access-Control-Allow-Headers", "Content-Type")
                        .setBody(toJson(apps))
                        .build();

            } else if ("POST".equalsIgnoreCase(method)) {
                // handle creating a new app
                String jsonBody = request.getTextBody();
                Map<String, Object> parsed = parseJsonMap(jsonBody);

                int userId = ((Double) parsed.get("userId")).intValue();
                String name = (String) parsed.get("name");
                String runtime = (String) parsed.get("runtime");

                DB.App newApp = AppManager.createApp(userId, name, runtime);
                if (newApp == null) {
                    return new HttpResponse.Builder()
                            .setStatusCode(400)
                            .setBody("User not found")
                            .build();
                }

                String responseJson = toJson(newApp);
                return new HttpResponse.Builder()
                        .setStatusCode(201)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Access-Control-Allow-Origin", "*")      // <--- Add these
                        .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                        .addHeader("Access-Control-Allow-Headers", "Content-Type")
                        .setBody(responseJson)
                        .build();

            } else {
                return createError(405, "Method Not Allowed");
            }
        });


        // This is our new upload endpoint
        processor.addRoute("/upload", this::handle_upload);

        processor.addRoute("/api/applications/status", request -> {

            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                return new HttpResponse.Builder()
                        .setStatusCode(200)
                        .addHeader("Access-Control-Allow-Origin", "*")
                        .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                        .addHeader("Access-Control-Allow-Headers", "Content-Type")
                        .setBody("") // body not needed for options
                        .build();
            }

            if (!"PUT".equalsIgnoreCase(request.getMethod())) {
                return createError(405, "Method Not Allowed");
            }

            String userIdParam = request.getQueryParam("userId").orElse(null);
            String appIdParam = request.getQueryParam("appId").orElse(null);
            String statusParam = request.getQueryParam("status").orElse(null);
            if (userIdParam == null || appIdParam == null || statusParam == null) {
                return createError(400, "Missing userId or appId or status");
            }

            int userId = Integer.parseInt(userIdParam);
            int appId = Integer.parseInt(appIdParam);

            boolean ok = AppManager.updateAppStatus(userId, appId, statusParam);
            if (!ok) {
                return createError(404, "App not found");
            }

            return new HttpResponse.Builder()
                    .setStatusCode(200)
                    .addHeader("Access-Control-Allow-Origin", "*")      // <--- Add these
                    .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    .addHeader("Access-Control-Allow-Headers", "Content-Type")
                    .setBody("Status updated to " + statusParam)
                    .build();
        });



        processor.addRoute("/api/applications/upload", request -> {

            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                return new HttpResponse.Builder()
                        .setStatusCode(200)
                        .addHeader("Access-Control-Allow-Origin", "*")
                        .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                        .addHeader("Access-Control-Allow-Headers", "Content-Type")
                        .setBody("") // body not needed for options
                        .build();
            }

            if (!"POST".equalsIgnoreCase(request.getMethod())) {
                return createError(405, "Method Not Allowed");
            }
            String userIdParam = request.getQueryParam("userId").orElse(null);
            String appIdParam = request.getQueryParam("appId").orElse(null);
            if (userIdParam == null || appIdParam == null) {
                return createError(400, "Missing userId or appId");
            }
            int userId = Integer.parseInt(userIdParam);
            int appId = Integer.parseInt(appIdParam);

            byte[] zipBytes = request.getRawBody();
            if (zipBytes == null || zipBytes.length == 0) {
                // Return the error with CORS headers, too
                return new HttpResponse.Builder()
                        .setStatusCode(400)
                        .addHeader("Access-Control-Allow-Origin", "*")
                        .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                        .addHeader("Access-Control-Allow-Headers", "Content-Type")
                        .setBody("No .zip data in request body")
                        .build();
            }

            // pass zip to azure
            try (InputStream zipStream = new ByteArrayInputStream(zipBytes)) {
                List<String> extractedFiles = azureInterface.upload(appId, zipStream);

                // For each extracted file, register a route with our RequestProcessor
                for (String filename : extractedFiles) {
                    String route = "/app_" + appId + "/" + filename;

                    // register route
                    processor.addRoute(route, (req) -> {
                        byte[] fileContent = azureInterface.download(appId, "/" + filename).readAllBytes();
                        String mime = MimeTypes.getMimeType(filename);
                        return new HttpResponse.Builder()
                                .setStatusCode(200)
                                .addHeader("Access-Control-Allow-Origin", "*")
                                .addHeader("Content-Type", mime)
                                .setRawBody(fileContent)
                                .build();
                    });

                    // record that route in db.json
                    AppManager.addRoute(userId, appId, route);
                }

                // success
                return new HttpResponse.Builder()
                        .setStatusCode(200)
                        .addHeader("Access-Control-Allow-Origin", "*")
                        .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                        .addHeader("Access-Control-Allow-Headers", "Content-Type")
                        .setBody("Successfully uploaded and extracted " + extractedFiles.size() + " files.")
                        .build();

            } catch (Exception e) {
                e.printStackTrace();
                return new HttpResponse.Builder()
                        .setStatusCode(500)
                        .addHeader("Access-Control-Allow-Origin", "*")
                        .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                        .addHeader("Access-Control-Allow-Headers", "Content-Type")
                        .setBody("Error uploading app: " + e.getMessage())
                        .build();
            }
        });

        processor.addRoute("/new_tenant", this::handle_new_tenant);
        processor.addRoute("/new_user", this::handle_new_user);

        // Testing:
//        test_azure_hosting();
    }

    private HttpResponse handle_new_user(HttpRequest request) {
        // Body parameters:
        // username, email, password, tenant_name

        System.out.println("Request body: " + request.getTextBody());

        // Process the json body
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = null;
        try {
            map = mapper.readValue(request.getTextBody(), new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            System.out.println("Error parsing JSON");
            return null;
        }

        String tenant_name = (String) map.get("tenant_name");
        String username = (String) map.get("username");
        String email = (String) map.get("email");
        String password = (String) map.get("password");
        
        String pw_hash = BCrypt.hashpw(password, BCrypt.gensalt());
        System.out.println("Hashed password: " + pw_hash);

        int tenant_id;
        try {
            tenant_id = databaseManager.getTenantIdFromName(tenant_name);
        } catch (SQLException e) {
            System.out.println("Error getting tenant_id");
            return null;
        }

        try {
            databaseManager.newUser(username, email, pw_hash, tenant_id);
        } catch (Exception e) {
            System.out.println("Error creating new user: " + e.getMessage());
            return null;
        }

        System.out.println("New user (" + username + ") created");

        return new HttpResponse.Builder()
                .setStatusCode(200)
                .setStatusMessage("OK")
                .addHeader("Content-Type", "text/plain")
                .setBody("New user created")
                .build();
    }

    private HttpResponse handle_new_tenant(HttpRequest request){
        // Body parameters:
        // tenant_name

        System.out.println("Request body: " + request.getTextBody());


        // Process the json body
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = null;
        try {
            map = mapper.readValue(request.getTextBody(), new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            System.out.println("Error parsing JSON");
            return null;
        }

        String tenant_name = (String) map.get("tenant_name");
        System.out.println("Tenant name: " + tenant_name);
        try {
            databaseManager.newTenant(tenant_name);
        } catch (Exception e) {
            System.out.println("Error creating new tenant");
            return null;
        }

        System.out.println("New tenant (" + tenant_name + ") created");

        return new HttpResponse.Builder()
                .setStatusCode(200)
                .setStatusMessage("OK")
                .addHeader("Content-Type", "text/plain")
                .setBody("New tenant created")
                .build();

    }

    // Example in ConnectionHandler (or a small utility class):
    private String toJson(Object data) {
        return new com.google.gson.Gson().toJson(data);
    }


    private HttpResponse createError(int code, String message) {
        return new HttpResponse.Builder()
                .setStatusCode(code)
                .setStatusMessage(message)
                // Add these so that error responses also have CORS
                .addHeader("Access-Control-Allow-Origin", "*")
                .addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                .addHeader("Access-Control-Allow-Headers", "Content-Type")

                // The usual content type & body
                .addHeader("Content-Type", "text/plain")
                .setBody(message)
                .build();
    }


    private Map<String, Object> parseJsonMap(String json) {
        try {
            return new com.google.gson.Gson().fromJson(
                    json, new com.google.gson.reflect.TypeToken<Map<String, Object>>(){}.getType()
            );
        } catch (Exception e) {
            // handle errors, or return an empty map
            return java.util.Collections.emptyMap();
        }
    }


    /**
     * This method now reads the raw bytes from the request body (the uploaded zip)
     * and calls AzureBlobInterface.upload(...)
     */
    private HttpResponse handle_upload(HttpRequest request) {
        // query parameters:
        // username, app_name,
        System.out.println("Method: " + request.getMethod());
        System.out.println("Path: " + request.getPath());
        System.out.println("Headers: " + request.getHeaders());
        // The old code used request.getBody() (a String), but we need binary data:
        //  -> Use request.getRawBody() after you’ve modified HttpRequest to store a byte[].

//        // 1) Get appId from query param (default to 112233445 if missing)
//        String appIdParam = request.getQueryParam("appId").orElse("112233445");
//        int appId = Integer.parseInt(appIdParam);

        // Create a new app in the db
//        ObjectMapper mapper = new ObjectMapper();
//        Map<String, Object> map = null;
//        try {
//            map = mapper.readValue(request.getTextBody(), new TypeReference<Map<String, Object>>() {});
//        } catch (JsonProcessingException e) {
//            System.out.println("Error parsing JSON");
//            return null;
//        }
//

        String user_name = request.getQueryParam("username").orElse(null);
        System.out.println("User name: " + user_name);
        int tenant_id;
        try {
            tenant_id = databaseManager.getTenantIdFromUserID(databaseManager.getUserIdFromName(user_name));
        } catch (SQLException e) {
            System.out.println("Error getting tenant_id");
            return null;
        }

        System.out.println("Tenant id: " + tenant_id);
        String app_name = request.getQueryParam("app_name").orElse(null);
        System.out.println("App name: " + app_name);

        try {
            databaseManager.newApp(app_name, tenant_id);
        } catch (SQLException e) {
            System.out.println("Error creating new app");
            System.out.println("Error: " + e.getMessage());
            return null;
        }

        int appId;
        try {
            appId = databaseManager.getAppIdFromName(app_name);
        } catch (SQLException e) {
            System.out.println("Error getting app id");
            return null;
        }

        // 2) Grab the raw bytes from HttpRequest
        byte[] zipBytes = request.getRawBody();
        if (zipBytes == null || zipBytes.length == 0) {
            // No data => error
            return new HttpResponse.Builder()
                    .setStatusCode(400)
                    .setStatusMessage("Bad Request")
                    .addHeader("Content-Type", "text/plain")
                    .setBody("No ZIP file data found in request body!")
                    .build();
        }

        try (InputStream zipStream = new ByteArrayInputStream(zipBytes)) {

            // 3) Upload to Azure
            ArrayList<String> endpointsToMake = azureInterface.upload(appId, zipStream);

            // Create endpoints
            createEndpoints(endpointsToMake, app_name, appId);

            // 4) Return success
            String msg = String.format(
                    "Uploaded %d file(s) to Azure for appId=%d",
                    endpointsToMake.size(),
                    appId
            );
            System.out.println(msg);

            return new HttpResponse.Builder()
                    .setStatusCode(200)
                    .setStatusMessage("OK")
                    .addHeader("Content-Type", "text/plain")
                    .setBody(msg)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return new HttpResponse.Builder()
                    .setStatusCode(500)
                    .setStatusMessage("Internal Server Error")
                    .addHeader("Content-Type", "text/plain")
                    .setBody("Error uploading zip: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Demo method that tests deploying a zip from disk,
     * then sets up dynamic routes to serve the extracted files from Azure.
     */
    public void test_azure_hosting() {
        System.out.println("Testing azure");
        int testAppID = 1;
        String testAppName = "MyCoolApp/";
        ArrayList<String> endpointsToMake = azureInterface.test_upload(testAppID);
        createEndpoints(endpointsToMake, testAppName, testAppID);

    }

    private void createEndpoints(ArrayList<String> endpointsToMake, String appName, int appID) {
        System.out.println("Endpoints to make:[");
        for (String endpointName : endpointsToMake) {
            System.out.println(endpointName + ", ");
        }
        System.out.println("]");

        for (String endpointPath : endpointsToMake) {
            final String endpointPathWithAppName = appName + endpointPath;
            System.out.println("Making endpoint for " + endpointPathWithAppName);

            String fileExtension = endpointPathWithAppName
                    .toLowerCase()
                    .substring(endpointPathWithAppName.lastIndexOf(".") + 1);

            System.out.println("File Extension: " + fileExtension);

            String fileType = switch (fileExtension) {
                case "html", "css" -> "text/" + fileExtension;
                case "js" -> "application/javascript";
                default -> "image/" + fileExtension;
            };

            System.out.println("fileType: " + fileType);

            // For text-based files
            if ("html".equals(fileExtension) ||
                    "css".equals(fileExtension) ||
                    "js".equals(fileExtension)) {

                processor.addRoute("/" + endpointPathWithAppName, request -> {
                    try {
                        return new HttpResponse.Builder()
                                .setStatusCode(200)
                                .setStatusMessage("OK")
                                .addHeader("Content-Type", fileType)
                                .setBody(
                                        get_plain_text_file_from_azure(
                                                azureInterface,
                                                appID,
                                                endpointPathWithAppName.replaceFirst("^[^/]+", "")
                                        )
                                )
                                .build();
                    } catch (Exception e) {
                        System.out.println("exception: " + e.getMessage());
                        return new HttpResponse.Builder()
                                .setStatusCode(404)
                                .setStatusMessage("Not Found")
                                .addHeader("Content-Type", fileType)
                                .setBody("Not found")
                                .build();
                    }
                });
            }
            // For binary (images, etc.)
            else {
                processor.addRoute("/" + endpointPathWithAppName, request -> {
                    try {
                        return new HttpResponse.Builder()
                                .setStatusCode(200)
                                .setStatusMessage("OK")
                                .addHeader("Content-Type", fileType)
                                .setRawBody(
                                        get_image_from_azure(
                                                azureInterface,
                                                appID,
                                                endpointPathWithAppName.replaceFirst("^[^/]+", "")
                                        )
                                )
                                .build();
                    } catch (Exception e) {
                        System.out.println("exception: " + e.getMessage());
                        return new HttpResponse.Builder()
                                .setStatusCode(404)
                                .setStatusMessage("Not Found")
                                .addHeader("Content-Type", fileType)
                                .setBody("Not found")
                                .build();
                    }
                });
            }
            System.out.println("Endpoint made for " + endpointPathWithAppName);
        }
    }

    private byte[] get_image_from_azure(AzureBlobInterface azureInterface, int testAppID, String path) {
        try {
            return azureInterface.download(testAppID, path).readAllBytes();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            return null;
        }
    }

    private String get_plain_text_file_from_azure(AzureBlobInterface azureInterface, int testAppID, String path) {
        try {
            return new String(
                    azureInterface.download(testAppID, path).readAllBytes()
            );
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            return "not found ;(";
        }
    }

    @Override
    public void run() {
        handle();
    }

    public void handle() {
        long startTime = System.currentTimeMillis();
        try {
            // Instead of old parser, use new RawHttpParser
            HttpRequest request = new HttpParser().parse(clientSocket.getInputStream());

            HttpResponse response = processor.process(request);
            response.write(clientSocket.getOutputStream());

            Telemetry.trackResponseTime(startTime);
            clientSocket.close();
        } catch (Exception e) {
            Logger.error("Error handling connection", e);
            // optionally do a 500 response
        }
    }


}
