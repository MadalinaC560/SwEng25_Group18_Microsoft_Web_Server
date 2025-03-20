package com.webserver.core;

import java.net.Socket;
import com.webserver.http.HttpParser;
import com.webserver.http.RequestProcessor;
import com.webserver.model.HttpRequest;
import com.webserver.model.HttpResponse;
import com.webserver.util.ConfigLoader;
import com.webserver.util.Logger;
import com.webserver.util.Telemetry;

public class ConnectionHandler implements Runnable {
    private final Socket clientSocket;
    private final HttpParser parser;
    private final RequestProcessor processor;

    public ConnectionHandler(Socket socket) {
        this.clientSocket = socket;
        this.parser = new HttpParser();


        // Load config and create FileService using the webroot
        ConfigLoader config = new ConfigLoader();
        String webRoot = config.getWebRoot();
        FileService fileService = new FileService(webRoot);

        // Pass FileService to RequestProcessor
        this.processor = new RequestProcessor(fileService);

        // Add a default route for testing
        processor.addRoute("/", request -> new HttpResponse.Builder()
                .setStatusCode(200)
                .setStatusMessage("OK")
                .addHeader("Content-Type", "text/plain")
                .setBody("Hello Group 18, Cloudle is Online!!")
                .build());
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
            //Telemetry.trackFileMetrics(fileName, startTime); //we will insert a valid file here.

            clientSocket.close();
        } catch (Exception e) {
            Logger.error("Error handling connection", e);
        }
    }
}