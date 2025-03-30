package com.webserver.util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class ScriptProcessor {

    public abstract String processScript(String filePath) throws Exception;

    public String readResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader reader = new BufferedReader( conn.getResponseCode() >= 400 ?
                new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8):
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line.trim());
            }
            //System.out.print(response.toString());
            return response.toString();
        }
    }
    //Appropriately formats a JSON input for the API
    public String createJSONInput(String code)
    {
        String codeForJSON =  code.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        return "{\"code\": \"" + codeForJSON  + "\"}";
    }

    protected String readFile(String filePath)  {
        try{
            return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        }
        catch (IOException e){
            return null;
        }
    }
}
