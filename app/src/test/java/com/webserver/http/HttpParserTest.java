package com.webserver.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.webserver.model.HttpRequest;

class HttpParserTest {

    @Test
    void testValidGetRequest() throws Exception {
        String request = 
            "GET /index.html HTTP/1.1\r\n" +
            "Host: localhost\r\n" +
            "User-Agent: test-agent\r\n" +
            "\r\n";  // End of request

        InputStream inputStream = new ByteArrayInputStream(request.getBytes());
        HttpParser parser = new HttpParser();
        HttpRequest httpRequest = parser.parse(inputStream);

        assertEquals("GET", httpRequest.getMethod());
        assertEquals("/index.html", httpRequest.getPath());
        assertNotNull(httpRequest.getHeaders());
        assertEquals(List.of("localhost"), httpRequest.getHeaders().get("Host"));
        assertEquals(List.of("test-agent"), httpRequest.getHeaders().get("User-Agent"));
//        assertEquals("", httpRequest.getBody()); // No body expected
    }
    @Test
    void testValidPostRequestWithBody() throws Exception {
        String request = 
            "POST /submit HTTP/1.1\r\n" +
            "Host: localhost\r\n" +
            "Content-Length: 13\r\n" +
            "\r\n" +
            "Hello, world!"; // Body

        InputStream inputStream = new ByteArrayInputStream(request.getBytes());
        HttpParser parser = new HttpParser();
        HttpRequest httpRequest = parser.parse(inputStream);

        assertEquals("POST", httpRequest.getMethod());
        assertEquals("/submit", httpRequest.getPath());
//        assertEquals("Hello, world!", httpRequest.getBody());
        assertEquals(List.of("13"), httpRequest.getHeaders().get("Content-Length"));
    }

    @Test
    void testInvalidRequestMissingMethod() {
        String request = "/index.html HTTP/1.1\r\n" + "Host: localhost\r\n" + "\r\n";

        InputStream inputStream = new ByteArrayInputStream(request.getBytes());
        HttpParser parser = new HttpParser();

        assertThrows(IllegalArgumentException.class, () -> parser.parse(inputStream));
    }

    @Test
    void testInvalidRequestMalformedHeaders() {
        String request = 
            "GET /index.html HTTP/1.1\r\n" +
            "Host localhost\r\n" + // Missing colon (:) after "Host"
            "\r\n";

        InputStream inputStream = new ByteArrayInputStream(request.getBytes());
        HttpParser parser = new HttpParser();

        assertThrows(IllegalArgumentException.class, () -> parser.parse(inputStream));
    }
}