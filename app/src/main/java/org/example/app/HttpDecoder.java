package org.example.app;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//HttpDecoder => convert the input stream into a Http Request object


public class HttpDecoder{

    public static Optional<HttpRequest> decode(final InputStream inputStream){
        Optional<List<String>> request = readMessage(inputStream);

        Optional<HttpRequest> finalRequest = buildRequest(request);
        
        return finalRequest;

    }   

    
    //reads raw HTTP request from the input stream and returns it as a list of strings
    private static Optional<List<String>> readMessage(final InputStream inputStream){
        List<String> requestLines = new ArrayList<>();                                                      //creates new list, each element will represent one line of the http request
        try{
            BufferedReader bw = new BufferedReader(new InputStreamReader(inputStream));                     //creates a BufferedReader, which reads from the input stream. the inputStreamReader converts bytes into readable characters

            String line;

            while((line = bw.readLine()) != null && !line.isEmpty()){                                       //read each line one by one, empty line will terminate this loop
                requestLines.add(line);
            }

            return Optional.of(requestLines);                                                               //return the request as an optional to prevent errors if its empty

        } 
        catch(IOException e){
            return Optional.empty();                                                                                                                                
        }
    }

    //extracts the http method and URI from the string list
    private static Optional<HttpRequest> buildRequest(Optional<List<String>> message){
        if(message != null && !message.isEmpty()){
            List<String> unwrappedMessage = message.get();
            String methodAndUri = unwrappedMessage.get(0);                                                  //first line should contain the http method and uri
            
            String[] separated = methodAndUri.split(" ");                                                   //the message is split by spaces (i.e GET /index.html)

            if(separated.length == 3){
                String methodType = separated[0];                                                           //extract the type of request from the array
                String uri = separated[1];                                                                  //extract the uri from the array

                HttpRequest request = builder(methodType, uri);                                             //you can't create a httpRequest object, so instead we call the builder method

                return Optional.of(request);
            }
            else{
                System.out.println("There was an error with the parsing.");
            }
        }
        
        return Optional.empty();
    }

    private static HttpRequest builder(String method, String uri){
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()   
        .uri(URI.create(uri));

        switch(method.toUpperCase()){                                                                      //switch statement to handle all request cases
            case "GET":                                                                                    //The GET request is the currently only supported at this time.
                requestBuilder.GET();
                break;
            default:
                System.out.println("There was an error building your request");
                break;
        }

        return requestBuilder.build();                                                                     //returns the constructed HttpRequest

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
