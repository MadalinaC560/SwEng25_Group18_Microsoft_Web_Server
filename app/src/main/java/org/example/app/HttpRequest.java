package org.example.app;

/*httpMethod will be sourced from abdul's socket implementation */

import java.net.URI;
import java.util.List;
import java.util.Map;

public class HttpRequest {
    private final HttpDecoder.HttpMethod httpMethod;
    private final URI uri;
    private final Map<String, List<String>> requestHeaders;

    private HttpRequest(HttpDecoder.HttpMethod opCode, URI uri,
                        Map<String, List<String>> requestHeaders){
        this.httpMethod = opCode;
        this.uri = uri;
        this.requestHeaders = requestHeaders;
    }

    public URI getUri(){
        return uri;
    }

    public HttpDecoder.HttpMethod getHttpMethod(){
        return httpMethod;
    }

    public Map<String, List<String>> getRequestHeaders(){
        return requestHeaders;
    }

    public static class Builder{
        private HttpDecoder.HttpMethod httpMethod;
        private URI uri;
        private Map<String, List<String>> requestHeaders;

    public Builder(){

    }

    public void setHttpMethod(HttpDecoder.HttpMethod httpMethod){
        this.httpMethod = httpMethod;
    }

    public void seturi(URI uri){
        this.uri = uri;
    }

    public void setRequestHeaders(Map<String, List<String>> requestHeaders){
        this.requestHeaders = requestHeaders;
    }

    public HttpRequest build(){
        return new HttpRequest(httpMethod, uri, requestHeaders);

    }

}


}

