package org.example.app;
import java.util.Map;
import java.util.Optional;
import java.io.*;


// responsible for handling the lifecycle of HTTP requests and responses

public class HttpHandler {

    private final Map<String, RequestRunner> routes;  /* map contains all routes and assosciated requests.*/

    public HttpHandler(final Map<String, RequestRunner> routes){
        this.routes = routes;
    }

    public void handleConnection(final InputStream inputStream, final OutputStream outputStream) throws IOException{
        /*Bufferedwriter used to write HTTP response to output stream*/
        final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));

        Optional<HttpRequest> request = HttpDecoder.decode(inputStream);
    }

    public void handleInvalidRequest(final BufferedWriter bufferedWriter){

    }

    private void handleRequest(final HttpRequest request, final BufferedWriter bufferedWriter){

    }
}

