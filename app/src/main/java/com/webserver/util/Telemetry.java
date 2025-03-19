package com.webserver.util;

import java.util.HashMap;
import java.util.Map;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.telemetry.SeverityLevel;

public class Telemetry{
    private static final TelemetryClient client;

    static{
        TelemetryConfiguration configuration = TelemetryConfiguration.createDefault();
        configuration.setInstrumentationKey("3dd12fd3-2f83-4010-8185-96acd9ef245a");
        client = new TelemetryClient(configuration);
    }

     //tracks the response time and sends to azure insights
    public static void trackResponseTime(long startingTime){
        long duration = System.currentTimeMillis() - startingTime;
        try {
            client.trackMetric("ResponseTime", duration);
            client.flush();
            
        } catch (Exception e) {
            System.err.println("There was an error when tracking response time." + e.getMessage());
        }
    }

    //this is for the actual users, assuming we are taking user ID's and served files as input and plotted to hashmap.
    public static void trackFileMetrics(String fileName){
         //Send details to the application insights
        try {

             //Hashmap to filter metrics by filename (add more to hashmap to differentiate user files)
            Map<String, String> user  = new HashMap<>();
            user.put("fileName", fileName);

            // Track the file accesses
            client.trackEvent("accessedFile", user, null);

            // Track the memory usage for the specific file
            long totalMemoryUsage =  Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            client.trackMetric("userMemoryUsage", totalMemoryUsage, 1, totalMemoryUsage, totalMemoryUsage, user);

            long totalMemoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            client.trackMetric("MemoryUsed", totalMemoryUsed);

            client.flush();
        } catch (Exception e) {
            System.err.println("There was an error when sending the telemetry data" + e.getMessage());
        }
    }


    public static void trackLogMetric(String level, String message){
        try {
            // Convert log level to correct SeverityLevel enum
            SeverityLevel severity;
            switch(level.toUpperCase()) {
                case "ERROR":
                    severity = SeverityLevel.Error;
                    break;
                case "WARN":
                    severity = SeverityLevel.Warning;
                    break;
                case "INFO":
                    severity = SeverityLevel.Information;
                    break;
                default:
                    severity = SeverityLevel.Verbose;
            }
    
            client.trackTrace(message, severity);
            client.flush();
    } catch (Exception e) {
        System.err.println("Failed to track log metric: " + e.getMessage());
    }
    }
}