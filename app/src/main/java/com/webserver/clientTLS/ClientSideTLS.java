package com.webserver.clientTLS;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;

public class ClientSideTLS {

    private static final String[] protocols = new String[] { "TLSv1.3" };
    private static final String[] cipher_suites = new String[] { "TLS_AES_128_GCM_SHA256" };

    public static void main(String[] args) throws Exception {

        SSLSocket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        String[] hosts = { "hostcloudle.com", "www.hostcloudle.com" };

        for (String host : hosts) {
            try {

                // Step : 1
                System.out.println("Connecting to " + host);
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();

                // Step : 2
                socket = (SSLSocket) factory.createSocket(host, 443);

                // Step : 3
                socket.setEnabledProtocols(protocols);
                socket.setEnabledCipherSuites(cipher_suites);

                // Step : 4 {optional}
                socket.startHandshake();

                // Step : 5
                out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(
                                        socket.getOutputStream())));
                // Proper HTTP/1.1 request with required headers
                out.println("GET / HTTP/1.1");
                out.println("Host: " + host);
                out.println("User-Agent: Java-TLS-Client");
                out.println("Accept: text/html");
                out.println("Connection: close");
                out.println();
                out.flush();

                if (out.checkError()) {
                    System.out.println("SSLSocketClient:  java.io.PrintWriter error");
                }

                // Step : 6
                in = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    socket.close();
                }
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            }
        }
    }
}
