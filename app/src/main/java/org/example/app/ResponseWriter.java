package org.example.app;

import java.io.BufferedWriter;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.List;
import java.util.Optional;

public class ResponseWriter{

    public static void writeResponse(final BufferedWriter outputStream, final HttpResponse response){

    }

    private static List<String> buildHeaderStrings(final Map<String, List<String>> responseHeaders){
        return null;
    }

    private static Optional<String> getResponseString(final Object entity){
        return null;
    }
}