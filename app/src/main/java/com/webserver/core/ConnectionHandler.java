package com.webserver.core;

import java.net.Socket;
import com.webserver.http.HttpParser;
import com.webserver.http.RequestProcessor;
import com.webserver.model.HttpRequest;
import com.webserver.model.HttpResponse;
import com.webserver.util.Logger;
import com.webserver.util.ConfigLoader;
import com.webserver.util.FileService;

public class ConnectionHandler implements Runnable {
    private final Socket clientSocket;
    private final HttpParser parser;
    private final RequestProcessor processor;
    private final FileService fileService;

    public ConnectionHandler(Socket socket) {
        this.clientSocket = socket;
        this.parser = new HttpParser();

        // Load webroot from config
        ConfigLoader config = new ConfigLoader();
        String webRoot = config.getWebRoot();

        this.fileService = new FileService(webRoot); // initalise file service with web root
        this.processor = new RequestProcessor(fileService); // Pass file service to request processor
        // Add a default route for testing
        processor.addRoute("/", request -> new HttpResponse.Builder()
                .setStatusCode(200)
                .setStatusMessage("OK")
                .addHeader("Content-Type", "text/plain")
                .setBody("Hello from your web server!")
                .build());
    }

    @Override
    public void run() {
        handle();
    }

    public void handle() {
        try {
            HttpRequest request = parser.parse(clientSocket.getInputStream());
            HttpResponse response = processor.process(request);
            response.write(clientSocket.getOutputStream());
            clientSocket.close();
        } catch (Exception e) {
            Logger.error("Error handling connection", e);
        }
    }
}