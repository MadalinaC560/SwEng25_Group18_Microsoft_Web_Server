package com.webserver.util;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStream;
import java.io.IOException;

public class ScriptProcessorFactory {
    private final Map<String, String> extensionToClassMap = new HashMap<>();

    //Populate the hashmap with the ScriptProcessors from the script-processors.properties
    public ScriptProcessorFactory(){
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("script-processors.properties")) {
            Properties props = new Properties();
            props.load(input);
            for (String ext : props.stringPropertyNames()) {
                extensionToClassMap.put(ext, props.getProperty(ext));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Look up the processor class name from the extension map
    // use reflection to create an instance of that processor
    // Returns null if nothing is found or something goes wrong
    public ScriptProcessor getProcessorForExtension(String extension)
    {
        String className = extensionToClassMap.get(extension);
        if (className == null) return null;
        try {
            Class<?> clazz = Class.forName(className);
            return (ScriptProcessor) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}
