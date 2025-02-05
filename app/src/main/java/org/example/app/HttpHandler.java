package org.example.app;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.http.HttpRequest;
import java.util.Map;
import java.util.Optional;

// responsible for handling the lifecycle of HTTP requests and responses
public class HttpHandler{

    private final Map<String, RequestRunner> routes;                                                                 // map contains all routes and assosciated requests

    public HttpHandler(final Map<String, RequestRunner> routes){
        this.routes = routes;
    }

    
    //reads an incoming request from input stream + processes it
    public void handleConnection(final InputStream inputStream, final OutputStream outputStream) throws IOException{

        //Bufferedwriter used to write HTTP response to output stream
        final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));

        try{
            Optional<HttpRequest> request = HttpDecoder.decode(inputStream);                                        //decodes incoming http request from the inputstream, returns as an optional type. Optional to prevent nullpointer errors if there is no request data.
            if(request.isPresent()){                                                                                //checks for valid request
                HttpRequest clientRequest = request.get();                                                          //retrieves the http request object from the optional variable
                handleRequest(clientRequest, bw);                                                                   //calls the method to process the valid http request and send back client response
            }
            else{
                handleInvalidRequest(bw);                                                                           //calls the invalid method and sends a 400 bad request response to client 
            }

            bw.flush();                                                                                             //ensures all data such as error messages are sent out to client

            bw.close();

        }
        catch(Exception e){
            System.out.println(e);
        }
        }
    
    //handle cases where request is invalid, sens 400 bad request response 
    public void handleInvalidRequest(final BufferedWriter bw){
        try{
            bw.write("HTTP/1.1 400 Bad Request \r\n");                                                              //tells client that the request wasn't processed due to an error, more detail is required at later date
            bw.flush();
        }
        catch(IOException e){
            System.out.println(e);                                                                                  //log error if exception occurs
        }
    }


     //handles valid requests by calling correct requestrunner.
    private void handleRequest(final HttpRequest request, final BufferedWriter bw){
        try {
            String path = request.uri().getPath();                                                                  //extract path from incoming httprequest
            RequestRunner runner = routes.get(path);

            if(runner != null){
                runner.run(request);                                                                                //the corresponding request runner is run if the route exists
                bw.write("HTTP/1.1 200 ok \r\n");                                                                   //sends successful http 200 ok response
            }
            else{
                bw.write("HTTP/1.1 404 Not Found \r\n");                                                            //sends 404 response code, indicating the route wasn't present
            }

            bw.flush();                                                                                             //ensure all data sent
            
        } catch (Exception e) {
            System.out.println(e);                                                                                  //print any errors
        }

    }
}

