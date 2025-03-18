
package com.webserver.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
        // Build a 204 (No Content) response
        HttpResponse response = new HttpResponse.Builder()
                .setStatusCode(204)
                .addHeader("Content-Type", "text/plain")
                .build();

        // Write the response to a ByteArrayOutputStream for inspection
        ResponseBuilder.write(outputStream, response);
        String httpResponse = outputStream.toString();

        // 1) Confirm the status line
        assertTrue(
                httpResponse.contains("HTTP/1.1 204 No Content"),
                "Should include the correct status line for 204"
        );

        // 2) Confirm the Content-Type header is present
        assertTrue(
                httpResponse.contains("Content-Type: text/plain"),
                "Should include the Content-Type header"
        );

        // 3) Check for the blank line delimiter "\r\n\r\n"
        int idx = httpResponse.indexOf("\r\n\r\n");
        assertTrue(idx >= 0, "Should contain the \\r\\n\\r\\n delimiter after headers");

        // 4) Verify that everything after that delimiter is empty (i.e. no body)
        String bodyPortion = httpResponse.substring(idx + 4);
        assertTrue(bodyPortion.isEmpty(), "Body should be empty for 204 No Content");
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