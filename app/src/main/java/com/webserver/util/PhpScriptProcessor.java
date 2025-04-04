package com.webserver.util;
import io.github.cdimascio.dotenv.Dotenv;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.io.OutputStream;


public class PhpScriptProcessor extends ScriptProcessor {
    //Add the url for the flask server on the VM below
    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("API_KEY");
    private static final String API_URL = dotenv.get("API_URL"); //for local testing "http://localhost:5000/run-php";


    @Override
    public String processScript(String filepath) {
        try {
            //Read the contents of the PHP file
            String phpCode = readFile(filepath);
            //Construct a JSON with the PHP code
            String jsonInput = createJSONInput(phpCode);

            URL url = new URL(API_URL);

            //Create a URL connection to connect to the API
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //Load the API KEY
            conn.setRequestProperty("X-API-Key", API_KEY);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            // Send the request body by writing the JSON
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            //Read and return the response from the API
            //Should be a json of the processed script
            String jsonResponse = readResponse(conn);
            return getCodeFromResponse(jsonResponse);
        } catch (Exception e)
        {
            return null;
        }
    }
}

