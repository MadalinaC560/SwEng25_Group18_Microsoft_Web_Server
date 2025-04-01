package com.webserver.serverTLS;

import javax.net.ssl.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ServerSideTLS {
    private static final String[] protocols = new String[] { "TLSv1.3" };
    private static final String[] cipher_suites = new String[] { "TLS_AES_128_GCM_SHA256" };
    private static final int PORT = 8980;

    public static void main(String[] args) {
        SSLServerSocket serverSocket = null;

        try {
            // Initialize SSL context (using default implementation)
            SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

            // Create server socket
            serverSocket = (SSLServerSocket) factory.createServerSocket(PORT);
            serverSocket.setEnabledProtocols(protocols);
            serverSocket.setEnabledCipherSuites(cipher_suites);

            System.out.println("Server started on port " + PORT + " with TLS 1.3");

            while (true) {
                try (SSLSocket sslSocket = (SSLSocket) serverSocket.accept()) {
                    System.out.println("New client connected: " +
                            sslSocket.getInetAddress().getHostAddress());

                    // Set timeout for reading (5 seconds)
                    sslSocket.setSoTimeout(5000);

                    // Get streams
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(sslSocket.getInputStream(), StandardCharsets.UTF_8));
                    PrintWriter out = new PrintWriter(
                            new OutputStreamWriter(sslSocket.getOutputStream(), StandardCharsets.UTF_8), true);

                    // Read client request
                    StringBuilder request = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null && !line.isEmpty()) {
                        request.append(line).append("\n");
                    }

                    System.out.println("Received request:\n" + request.toString());

                    // Send response
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: text/plain");
                    out.println("Connection: close");
                    out.println();
                    out.println("Hello from TLS 1.3 Server!");
                    out.flush();

                } catch (SSLException e) {
                    System.err.println("SSL handshake failed: " + e.getMessage());
                } catch (IOException e) {
                    System.err.println("Client connection error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing server: " + e.getMessage());
                }
            }
        }
    }
}
