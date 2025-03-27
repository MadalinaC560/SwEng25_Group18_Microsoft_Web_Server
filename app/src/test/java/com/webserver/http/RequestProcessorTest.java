//package com.webserver.http;
//
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//import com.webserver.util.FileService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import com.webserver.model.HttpRequest;
//import com.webserver.model.HttpResponse;
//
//class RequestProcessorTest {
//
//    private RequestProcessor processor;
//
//    @BeforeEach
//    void setUp() {
//        FileService fileService = new FileService("./webroot");
//        processor = new RequestProcessor(fileService);
//        //  the default "/" route just as is in ConnectionHandler
//        processor.addRoute("/", request -> new HttpResponse.Builder()
//                .setStatusCode(200)
//                .setStatusMessage("OK")
//                .addHeader("Content-Type", "text/plain")
//                .setBody("Hello Group 18, Cloudle is Online!!")
//                .build()
//        );
//    }
//
//    @Test
//    void testDefaultRouteProcessing() {
//        HttpRequest request = new HttpRequest("GET", "/", Map.of(), "");
//        HttpResponse response = processor.process(request);
//
//        assertEquals(200, response.getStatusCode());
//        assertEquals("Hello Group 18, Cloudle is Online!!", response.getBody());
//    }
//
//    @Test
//    void testNotFoundRoute() {
//        HttpRequest request = new HttpRequest("GET", "/unknown", Map.of(), "");
//        HttpResponse response = processor.process(request);
//
//        assertEquals(404, response.getStatusCode());
//        assertEquals("Error 404: Not Found", response.getBody());
//    }
//
//    @Test
//    void testInternalServerError() {
//        processor.addRoute("/error", req -> { throw new RuntimeException("Something went wrong"); });
//
//        HttpRequest request = new HttpRequest("GET", "/error", Map.of(), "");
//        HttpResponse response = processor.process(request);
//
//        assertEquals(500, response.getStatusCode());
//        assertTrue(response.getBody().contains("Internal Server Error"));
//    }
//
//    @Test
//    void testAddRouteSuccessfully() {
//        processor.addRoute("/test", req -> new HttpResponse.Builder().setStatusCode(200).setBody("Test Route").build());
//
//        HttpRequest request = new HttpRequest("GET", "/test", Map.of(), "");
//        HttpResponse response = processor.process(request);
//
//        assertEquals(200, response.getStatusCode());
//        assertEquals("Test Route", response.getBody());
//    }
//
//    @Test
//    void testAddDuplicateRoute() {
//        processor.addRoute("/duplicate", req -> new HttpResponse.Builder().setStatusCode(200).setBody("First").build());
//
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
//            processor.addRoute("/duplicate", req -> new HttpResponse.Builder().setStatusCode(200).setBody("Second").build());
//        });
//
//        assertTrue(exception.getMessage().contains("Route Already Exists"));
//    }
//
//    @Test
//    void testInvalidRoutePath() {
//        assertThrows(IllegalArgumentException.class, () -> processor.addRoute("", req -> new HttpResponse.Builder().setStatusCode(200).setBody("Invalid").build()));
//        assertThrows(IllegalArgumentException.class, () -> processor.addRoute("no-slash", req -> new HttpResponse.Builder().setStatusCode(200).setBody("Invalid").build()));
//    }
//
//    @Test
//    void testRequestValidation() {
//        HttpRequest validRequest = new HttpRequest("GET", "/valid", Map.of(), "");
//        assertTrue(processor.process(validRequest).getStatusCode() != 400);
//
//        HttpRequest invalidMethodRequest = new HttpRequest("INVALID", "/valid", Map.of(), "");
//        assertEquals(400, processor.process(invalidMethodRequest).getStatusCode());
//
//        HttpRequest missingPathRequest = new HttpRequest("GET", null, Map.of(), "");
//        assertEquals(400, processor.process(missingPathRequest).getStatusCode());
//
//        HttpRequest postWithoutContentLength = new HttpRequest("POST", "/post", Map.of(), "data");
//        assertEquals(400, processor.process(postWithoutContentLength).getStatusCode());
//    }
//}
//
