package com.webserver.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    // Singleton instance
    private static ConfigLoader instance;

    private String CONFIG_FILE = System.getProperty("user.dir") + "/server.properties";
    private final Properties properties;

    // Private constructor to prevent direct instantiation
    public ConfigLoader() {
        properties = new Properties();
        loadDefaults();
        loadFromFile();
    }

    /**
     * Get the singleton instance of ConfigLoader
     *
     * @return The ConfigLoader instance
     */
    public static synchronized ConfigLoader getInstance() {
        if (instance == null) {
            instance = new ConfigLoader();
        }
        return instance;
    }

    private void loadDefaults() {
        // Set default values
        properties.setProperty("port", "8080");
        properties.setProperty("webroot", "./webroot");
        properties.setProperty("max_threads", "10");
        properties.setProperty("connection_timeout", "30000");

        // Add defaults for PHP script processing
        properties.setProperty("php.api.url", "http://20.86.80.12:5000/run-php");
        properties.setProperty("php.api.key", "");
    }

    private void loadFromFile() {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            properties.load(fis);
            Logger.info("Configuration loaded from " + CONFIG_FILE);
        } catch (IOException e) {
            Logger.warn("No configuration file found, using defaults");
        }
    }

    public String getBaseUrl() {
        return properties.getProperty("baseUrl", "localhost:8080");
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