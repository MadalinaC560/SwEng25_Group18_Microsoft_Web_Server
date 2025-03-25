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

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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

        this.azureInterface = new AzureBlobInterface();

        // Add a default route for testing
        processor.addRoute("/", request ->
            new HttpResponse.Builder()
                .setStatusCode(200)
                .setStatusMessage("OK")
                .addHeader("Content-Type", "text/plain")
                .setBody("Hello Group 18, Cloudle is Online!!")
                .build()
        );

        processor.addRoute("/MyCoolApp/", request ->
            new HttpResponse.Builder()
                .setStatusCode(200)
                .setStatusMessage("OK")
                .addHeader("Content-Type", "text/plain")
                .setBody(return_hello())
                .build()
        );

        processor.addRoute("/upload", this::handle_upload);
        processor.addRoute("/new_tenant", this::handle_new_tenant);

        // Testing:
//        test_azure_hosting();
    }

    private HttpResponse handle_new_tenant(HttpRequest request){
        // Body parameters:
        // tenant_name

        System.out.println("got here");

        // Process the json body
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = null;
        try {
            map = mapper.readValue(request.getBody(), new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            System.out.println("Error parsing JSON");
            return null;
        }

        String tenant_name = (String) map.get("tenant_name");
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

    private HttpResponse handle_upload(HttpRequest request) {
        // I am reading the zip from disk and not the request
        // TODO: Replace reading it from disk with reading it from the HTTP request that has a .zip file embedded

        System.out.println("Method: " + request.getMethod());
        System.out.println("Path: " + request.getPath());
        System.out.println("Headers: [");
        for (Map.Entry<String, List<String>> entry : request
            .getHeaders()
            .entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();

            System.out.println("Key: " + key);
            for (String value : values) {
                System.out.println("Value: " + value);
            }
        }
        System.out.println("]");
        System.out.println("Body: " + request.getBody());

        return new HttpResponse.Builder()
            .setStatusCode(200)
            .setStatusMessage("OK")
            .addHeader("Content-Type", "text/plain")
            .setBody("uploaded")
            .build();
    }

    private String return_hello() {
        return "hello";
    }

    public void test_azure_hosting() {
        System.out.println("Testing azure");
        int testAppID = 1;
        String testAppName = "MyCoolApp/";
        ArrayList<String> endpointsToMake = azureInterface.test_upload(
            testAppID
        );

        System.out.println("Endpoints to make:[");
        for (String endpointName : endpointsToMake) {
            System.out.println(endpointName + ", ");
        }
        System.out.println("]");

        for (String endpointPath : endpointsToMake) {
            final String endpointPathWithAppName = testAppName + endpointPath;
            System.out.println(
                "Making endpoint for " + endpointPathWithAppName
            );

            String fileExtension = endpointPathWithAppName
                .toLowerCase()
                .substring(endpointPathWithAppName.lastIndexOf(".") + 1);

            System.out.println("File Extension: " + fileExtension);

            String fileType =
                switch (fileExtension) {
                    case "html", "css" -> "text/" + fileExtension;
                    case "js" -> "application/javascript";
                    default -> "image/" + fileExtension;
                };

            System.out.println("fileType: " + fileType);

            // Remove the first /

            if (
                fileExtension.equals("html") ||
                fileExtension.equals("css") ||
                fileExtension.equals("js")
            ) {
                processor.addRoute("/" + endpointPathWithAppName, request -> {
                    try {
                        return new HttpResponse.Builder()
                            .setStatusCode(200)
                            .setStatusMessage("OK")
                            .addHeader("Content-Type", fileType)
                            .setBody(
                                get_plain_text_file_from_azure(
                                    azureInterface,
                                    testAppID,
                                    endpointPathWithAppName.replaceFirst(
                                        "^[^/]+",
                                        ""
                                    )
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
            } else {
                processor.addRoute("/" + endpointPathWithAppName, request -> {
                    try {
                        return new HttpResponse.Builder()
                            .setStatusCode(200)
                            .setStatusMessage("OK")
                            .addHeader("Content-Type", fileType)
                            .setRawBody(
                                get_image_from_azure(
                                    azureInterface,
                                    testAppID,
                                    endpointPathWithAppName.replaceFirst(
                                        "^[^/]+",
                                        ""
                                    )
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

    private byte[] get_image_from_azure(
        AzureBlobInterface azureInterface,
        int testAppID,
        String path
    ) {
        try {
            return azureInterface.download(testAppID, path).readAllBytes();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            return null;
        }
    }

    private String get_plain_text_file_from_azure(
        AzureBlobInterface azureInterface,
        int testAppID,
        String path
    ) {
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
        long startTime = System.currentTimeMillis(); //for our use, not the system
        try {
            HttpRequest request = parser.parse(clientSocket.getInputStream());
            HttpResponse response = processor.process(request);
            response.write(clientSocket.getOutputStream());

            Telemetry.trackResponseTime(startTime); //for our use
            //Logger.trackFileMetrics(fileName, startTime); //we will insert a valid file here.

            clientSocket.close();
        } catch (Exception e) {
            Logger.error("Error handling connection", e);
        }
    }
}
