package com.webserver.core;

import com.webserver.azure.AzureBlobInterface;
import com.webserver.http.HttpParser;
import com.webserver.http.RequestProcessor;
import com.webserver.model.HttpRequest;
import com.webserver.model.HttpResponse;
import com.webserver.util.ConfigLoader;
import com.webserver.util.FileService;
import com.webserver.util.Logger;
import java.net.Socket;
import java.util.ArrayList;

public class ConnectionHandler implements Runnable {

    private final Socket clientSocket;
    private final HttpParser parser;
    private final RequestProcessor processor;
    private final AzureBlobInterface azureInterface;

    public ConnectionHandler(Socket socket) {
        this.clientSocket = socket;
        this.parser = new HttpParser();

        // Load config and create FileService using the webroot
        ConfigLoader config = new ConfigLoader();
        String webRoot = config.getWebRoot();
        FileService fileService = new FileService(webRoot);

        // Pass FileService to RequestProcessor
        this.processor = new RequestProcessor(fileService);

        this.azureInterface = new AzureBlobInterface(processor);

        // Add a default route for testing
        processor.addRoute("/", request ->
            new HttpResponse.Builder()
                .setStatusCode(200)
                .setStatusMessage("OK")
                .addHeader("Content-Type", "text/plain")
                .setBody("Hello Group 18, Cloudle is Online!!")
                .build()
        );
        // Testing:
        // test_azure_hosting();
    }

    public void test_azure_hosting() {
        System.out.println("Testing azure");
        int testAppID = 1;
        String testAppName = "MyCoolApp";
        ArrayList<String> endpointsToMake = azureInterface.test_upload(
            testAppID
        );
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

            if (fileType == "html" || fileType == "css" || fileType == "js") {
                processor.addRoute(
                    "/" + testAppName + endpointPathWithAppName,
                    request -> {
                        try {
                            return new HttpResponse.Builder()
                                .setStatusCode(200)
                                .setStatusMessage("OK")
                                .addHeader("Content-Type", fileType)
                                .setBody(
                                    new String(
                                        azureInterface
                                            .download(
                                                testAppID,
                                                endpointPathWithAppName
                                            )
                                            .readAllBytes()
                                    )
                                )
                                .build();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return new HttpResponse.Builder()
                                .setStatusCode(500)
                                .setStatusMessage("Internal Server Error")
                                .addHeader("Content-Type", "text/plain")
                                .setBody("Error: " + e.getMessage())
                                .build();
                        }
                    }
                );
            }

            System.out.println("Endpoint made for " + endpointPathWithAppName);
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

            Telemetry.trackResponseTime(startTime);//for our use
            //Logger.trackFileMetrics(fileName, startTime); //we will insert a valid file here.

            clientSocket.close();
        } catch (Exception e) {
            Logger.error("Error handling connection", e);
        }
    }
}