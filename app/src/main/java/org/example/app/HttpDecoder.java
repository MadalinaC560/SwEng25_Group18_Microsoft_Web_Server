package org.example.app;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Optional;


public class HttpDecoder{

    public static Optional<HttpRequest> decode(final InputStream inputStream){
        return null;
    }   

    private static Optional<List<String>> readMessage(final InputStream inputStream){
        return null;
    }

    private static Optional<HttpRequest> buildRequest(List<String> message){
        return null;
    }

    public enum HttpMethod {
        GET,
        PUT,
        POST,
        PATCH
    }

    // private static HttpRequest addRequestHeaders(final List<String> message, final org.example.app.HttpRequest.Builder builder){
    //     return null;
    // }

    




}
