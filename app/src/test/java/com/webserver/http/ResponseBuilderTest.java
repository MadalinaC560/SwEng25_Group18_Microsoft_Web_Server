package com.webserver.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.webserver.model.HttpResponse;

public class ResponseBuilderTest {

    private ByteArrayOutputStream outputStream;

    @BeforeEach
    public void setUp() {
        outputStream = new ByteArrayOutputStream();
    }

    @Test
    public void testWriteResponseWithBody() throws IOException {
        HttpResponse response = new HttpResponse.Builder()
                .setStatusCode(200)
                .addHeader("Content-Type", "text/plain")
                .setBody("Hello, World!")
                .build();

        ResponseBuilder.write(outputStream, response);

        String httpResponse = outputStream.toString();
        assertTrue(httpResponse.contains("HTTP/1.1 200 OK"));
        assertTrue(httpResponse.contains("Content-Type: text/plain"));
        assertTrue(httpResponse.contains("Hello, World!"));
    }

    @Test
    public void testWriteResponseWithoutBody() throws IOException {
        HttpResponse response = new HttpResponse.Builder()
                .setStatusCode(204)
                .addHeader("Content-Type", "text/plain")
                .build();

        ResponseBuilder.write(outputStream, response);

        String httpResponse = outputStream.toString();
        assertTrue(httpResponse.contains("HTTP/1.1 204 No Content"));
        assertTrue(httpResponse.contains("Content-Type: text/plain"));
        assertTrue(!httpResponse.contains("\r\n\r\n")); // No body
    }

    @Test
    public void testWriteResponseWithMultipleHeaders() throws IOException {
        HttpResponse response = new HttpResponse.Builder()
                .setStatusCode(200)
                .addHeader("Content-Type", "text/plain")
                .addHeader("X-Custom-Header", "CustomValue")
                .setBody("Hello, World!")
                .build();

        ResponseBuilder.write(outputStream, response);

        String httpResponse = outputStream.toString();
        assertTrue(httpResponse.contains("HTTP/1.1 200 OK"));
        assertTrue(httpResponse.contains("Content-Type: text/plain"));
        assertTrue(httpResponse.contains("X-Custom-Header: CustomValue"));
        assertTrue(httpResponse.contains("Hello, World!"));
    }

    @Test
    public void testWriteResponseWithEmptyHeaders() throws IOException {
        HttpResponse response = new HttpResponse.Builder()
                .setStatusCode(200)
                .setBody("Hello, World!")
                .build();

        ResponseBuilder.write(outputStream, response);

        String httpResponse = outputStream.toString();
        assertTrue(httpResponse.contains("HTTP/1.1 200 OK"));
        assertTrue(httpResponse.contains("Hello, World!"));
    }
}
