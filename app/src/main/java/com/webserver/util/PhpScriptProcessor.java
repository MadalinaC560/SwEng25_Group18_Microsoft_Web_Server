package com.webserver.util;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.io.OutputStream;


public class PhpScriptProcessor extends ScriptProcessor {
    //Add the url for the flask server on the VM below
    private static final String API_URL = "";

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
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            // Send the request body by writing the JSON payload
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            //Read and return the response from the API
            //Should be a json of the processed script
            return readResponse(conn);
        } catch (Exception e)
        {
            return null;
        }
    }
}

