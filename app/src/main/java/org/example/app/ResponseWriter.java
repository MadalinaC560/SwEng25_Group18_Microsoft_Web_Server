package org.example.app;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ResponseWriter{

    //sends a response via the outputStream using the httpResponse object
    public static void writeResponse(final BufferedWriter outputStream, final HttpResponse response){
        try{
            outputStream.write("HTTP/1.1 " + response.statusCode() + " " + 
            getPhrase(response.statusCode()) + "\r\n");                                                       //writes the http status line using different methods from httpresponse library

            List<String> headerStrings = buildHeaderStrings(response.headers().map());                        //formats the headers into a list of strings 
            for(String header : headerStrings){                                                               //Writes each header with an enhanced for loop line by line
                outputStream.write(header + "\r\n");
            }

            outputStream.write("\r\n");
            Optional<String> responseBody = getResponseString(response.body());                               //if there is a body present, it will be stored in the responseBody string

            if(responseBody.isPresent()){
                try{
                    outputStream.write(responseBody.get());                                                   //writes the body to stream if present
                }catch(IOException e){
                    System.out.println(e);
                }
            }

            outputStream.flush();

        }catch(IOException e){
            System.out.println(e.getMessage());
        }
        
    }


    //converts map of httpresponse headers into string list e.g(Content-Type: text/plain)
    private static List<String> buildHeaderStrings(final Map<String, List<String>> responseHeaders){
        List<String> headerStrings = new ArrayList<>();                                                      //stores formatted header strings
        
        for (Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {                           //iterates through each entry in headers map, appen                     
            String headerName = entry.getKey();
            for (String headerValue : entry.getValue()) {
                headerStrings.add(headerName + ": " + headerValue);                                          //appends the sourced header name and value to the list
            }
        }
        return headerStrings;
    }

    //converts body to string
    private static Optional<String> getResponseString(final Object response){
        if(response == null){
            return Optional.empty();
        }

        if(response instanceof String){
            return Optional.of((String) response);                                                             //if already a string, wraps it in an optionals
        }

        return Optional.of(response.toString());

    }

    //provides phrases for common HTTP status codes (will look in future for more efficient methods)
    private static String getPhrase(int statusCode){
        switch(statusCode){
            case 200: return "Success";
            case 301: return "Permanent Redirect";
            case 302: return "Temporary Redirect";
            case 304: return "Not Modified";
            case 400: return "Bad Request";
            case 401: return "Unauthorised Error";
            case 404: return "Not Found";
            case 500: return "Internal Server Error";
            default: return "Unknown State";

        }
    }
}