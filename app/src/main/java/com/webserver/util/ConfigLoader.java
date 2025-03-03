package com.webserver.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private static final String CONFIG_FILE = "server.properties";
    private final Properties properties;

    public ConfigLoader() {
        properties = new Properties();
        loadDefaults();
        loadFromFile();
    }

    private void loadDefaults() {
        // Set default values
        properties.setProperty("port", "8080");
        properties.setProperty("webroot", "./webroot");
        properties.setProperty("max_threads", "10");
        properties.setProperty("connection_timeout", "30000");
    }

    private void loadFromFile() {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            properties.load(fis);
            Logger.info("Configuration loaded from " + CONFIG_FILE);
        } catch (IOException e) {
            Logger.warn("No configuration file found, using defaults");
        }
    }

    public int getPort() {
        return Integer.parseInt(properties.getProperty("port"));
    }

    public String getWebRoot() {
        return properties.getProperty("webroot");
    }

    public int getMaxThreads() {
        return Integer.parseInt(properties.getProperty("max_threads"));
    }

    public int getConnectionTimeout() {
        return Integer.parseInt(properties.getProperty("connection_timeout"));
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}