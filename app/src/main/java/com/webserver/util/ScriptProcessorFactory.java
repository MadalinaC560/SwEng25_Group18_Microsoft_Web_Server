package com.webserver.util;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStream;

public class ScriptProcessorFactory {
    private final Map<String, String> extensionToClassMap = new HashMap<>();

    public ScriptProcessorFactory(){

    }

    public ScriptProcessor getProcessorForExtension(String ext)
    {
        return null;
    }
}
