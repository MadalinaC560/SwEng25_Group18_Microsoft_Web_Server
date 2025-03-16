package com.webserver.http;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.webserver.model.HttpRequest;
import com.webserver.model.HttpResponse;
import com.webserver.util.FileService;

class RequestProcessorTest {
    private RequestProcessor processor;
    private static final String TEST_WEBROOT = "test_webroot";

    @BeforeEach
    void setUp() throws IOException {
        //  Setup a test webroot directory
        Files.createDirectories(Paths.get(TEST_WEBROOT));
        Files.write(Paths.get(TEST_WEBROOT, "test.txt"), "Hello, JUnit!".getBytes());
        Files.write(Paths.get(TEST_WEBROOT, "index.html"), "<html><body>Hello HTML</body></html>".getBytes());

        FileService fileService = new FileService(TEST_WEBROOT);
        processor = new RequestProcessor(fileService);
    }

    @Test
    void testServingStaticFile() {
        HttpRequest request = new HttpRequest("GET", "/test.txt", Map.of(), "");
        HttpResponse response = processor.process(request);

        assertEquals(200, response.getStatusCode());
        assertEquals("text/plain", response.getHeaders().get("Content-Type").get(0));
        assertEquals("Hello, JUnit!", response.getBody());
    }

    @Test
    void testServingHtmlFile() {
        HttpRequest request = new HttpRequest("GET", "/index.html", Map.of(), "");
        HttpResponse response = processor.process(request);

        assertEquals(200, response.getStatusCode());
        assertEquals("text/html", response.getHeaders().get("Content-Type").get(0));
        assertEquals("<html><body>Hello HTML</body></html>", response.getBody());
    }

    // @Test
    // void testRequestForNonExistentFile() {
    //     HttpRequest request = new HttpRequest("GET", "/nonexistent.txt", Map.of(), "");
    //     HttpResponse response = processor.process(request);

    //     assertEquals(404, response.getStatusCode());
    //     assertEquals("Error 404: Not Found", response.getBody());
    // }

    @Test
    void testRequestForForbiddenPathTraversal() {
        HttpRequest request = new HttpRequest("GET", "/../etc/passwd", Map.of(), "");
        HttpResponse response = processor.process(request);
    
        // 🔍 Debugging output
        System.out.println("Response Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
    
        assertEquals(400, response.getStatusCode(), "Expected 400 Bad Request, but got: " + response.getStatusCode());
        assertEquals("Bad Request", response.getBody());
    }
    
}
