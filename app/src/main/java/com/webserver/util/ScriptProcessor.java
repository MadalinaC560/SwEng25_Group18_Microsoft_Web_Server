package com.webserver.util;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class ScriptProcessor {
    public static final Map<String, String> SUPPORTED_SCRIPTS = new HashMap<>();

    static {
        SUPPORTED_SCRIPTS.put("js", "graal.js");
    }

    public static String executeScript(String scriptPath, Map<String, Object> bindings)
    {
        return "";
    }

    // Helper Functions
    public static boolean isScript(String fileExtension) {
        if (SUPPORTED_SCRIPTS.containsKey(fileExtension))
        {
            return true;
        }
        return false;
    }

    public static String getFileExtension(String scriptPath) {
        int lastDotIndex = scriptPath.lastIndexOf('.');
        String fileExtension = lastDotIndex == -1 ?"" : scriptPath.substring(lastDotIndex+1);
        return fileExtension;
    }

    public static ScriptEngine getScriptEngineByExtension(String extension) {
        ScriptEngineManager manager = new ScriptEngineManager();
        String engineName = SUPPORTED_SCRIPTS.get(extension);
        ScriptEngine engine = manager.getEngineByName(engineName);
        if (engine != null)
        {
            return engine;
        }
        return null;
    }

    public static String loadScriptFromFile(String scriptPath) throws IOException {
        try (InputStream input = new FileInputStream(new File(scriptPath));
             ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = input.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString("UTF-8");
        } catch (IOException e) {
            return "Error";
        }
    }
}




