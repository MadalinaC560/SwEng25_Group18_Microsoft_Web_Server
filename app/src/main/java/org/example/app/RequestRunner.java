package org.example.app;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

//

public interface RequestRunner{
    HttpResponse run(HttpRequest request);
}