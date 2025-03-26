package com.webserver.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.webserver.util.Logger;
import com.webserver.util.ConfigLoader;
import com.webserver.util.Telemetry;

public class Server {
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private final ConfigLoader config;
    private boolean running = true;

    public Server() throws IOException {
        // Load configuration
        this.config = new ConfigLoader();

        // Initialize server socket with configured port
        this.serverSocket = new ServerSocket(config.getPort());
        this.threadPool = Executors.newFixedThreadPool(config.getMaxThreads());

        Logger.info("Server initialized with:");
        Logger.info("Port: " + config.getPort());
        Logger.info("Web root: " + config.getWebRoot());
        Logger.info("Max threads: " + config.getMaxThreads());
    }

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        Logger.info("Server started on port " + config.getPort());
        //thread that tracks server metrics with handled interruption 
        Thread metricsThread = new Thread(() -> {
            while(running){
                Telemetry.trackServerMetrics(System.currentTimeMillis());
                try {
                    //tracks the threads every 30 seconds
                    Thread.sleep(30000); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

            }
        });
        metricsThread.setDaemon(true);
        metricsThread.start();

        while(running) {
            try {
                Socket clientSocket = serverSocket.accept();
                Logger.info("Connection received from " + clientSocket.getInetAddress());

                ConnectionHandler handler = new ConnectionHandler(clientSocket);
                threadPool.execute(handler);

            } catch (IOException e) {
                if(running) {
                    Logger.error("Error accepting connection", e);
                }
            }
        }

        Logger.info("Server has stopped");
    }

    public void stop() {
        Logger.info("Shutting down gracefully...");
        running = false;
        threadPool.shutdown();
        try {
            //flushes remaining metrics before the server shuts down 
            Telemetry.trackServerMetrics(System.currentTimeMillis());
            serverSocket.close();
        } catch (IOException e) {
            Logger.error("Error closing server socket", e);
        }
    }

    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.start();
        } catch (IOException e) {
            Logger.error("Server failed to start", e);
        }
    }
}