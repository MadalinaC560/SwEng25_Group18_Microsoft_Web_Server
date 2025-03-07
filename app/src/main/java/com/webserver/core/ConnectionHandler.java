package com.webserver.core;

import java.net.Socket;

import com.webserver.http.HttpParser;
import com.webserver.http.RequestProcessor;
import com.webserver.model.HttpRequest;
import com.webserver.model.HttpResponse;
import com.webserver.util.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConnectionHandler implements Runnable {
    private final Socket clientSocket;
    private final HttpParser parser;
    private final RequestProcessor processor;

    public ConnectionHandler(Socket socket) {
        this.clientSocket = socket;
        this.parser = new HttpParser();
        this.processor = new RequestProcessor();
        // Add a default route for testing
        processor.addRoute("/", request -> new HttpResponse.Builder()
                .setStatusCode(200)
                .setStatusMessage("OK")
                .addHeader("Content-Type", "text/plain")
                .setBody("Hello from your web server!")
                .build());

        // Add a route for the index page
         processor.addRoute("/index", request -> {
            try {
                String htmlContent = new String(Files.readAllBytes(Paths.get("uploads/index.html")));
                return new HttpResponse.Builder()
                        .setStatusCode(200)
                        .setStatusMessage("OK")
                        .addHeader("Content-Type", "text/html")
                        .setBody(htmlContent)
                        .build();
            } catch (IOException e) {
                Logger.error("Error reading index.html", e);
                return new HttpResponse.Builder()
                        .setStatusCode(500)
                        .setStatusMessage("Internal Server Error")
                        .addHeader("Content-Type", "text/html")
                        .setBody("<html><body><h1>500 Internal Server Error</h1></body></html>")
                        .build();
            }
        });
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