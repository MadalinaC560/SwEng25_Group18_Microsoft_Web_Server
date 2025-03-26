package com.webserver.util;
import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PhpScriptProcessor extends ScriptProcessor {
    //Add the url for the flask server on the VM below
    private static final String API_URL = "";

    @Override
    public String processScript(String filepath) throws Exception {
        String phpCode = readFile(filepath);
        String jsonInput = "{\"code\": \"" + phpCode.replace("\"", "\\\"") + "\"}";

        URL url = new URL(API_URL);

        //Create a URL connection to connect to the API
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setDoOutput(true);

        // Send the request body
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        //Read the response
        //Should be a json of the processed script
        return readResponse(conn);
    }
}
