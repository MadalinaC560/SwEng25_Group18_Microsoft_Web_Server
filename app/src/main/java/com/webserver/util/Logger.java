

package com.webserver.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String LOG_FILE = "webserver.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static void log(String level, String message, Throwable t) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[%s] [%s] %s", timestamp, level, message);

        // Print to console
        System.out.println(logMessage);
        if (t != null) {
            t.printStackTrace();
        }

        // Write to file
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println(logMessage);
            if (t != null) {
                t.printStackTrace(writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }

        try {
            Telemetry.trackLogMetric(level, logMessage);
            
        } catch (Exception e) {
            System.out.println("Failed to send telemetry: " + e.getMessage());

        }
    }
    
    public static void info(String message) {
        log("INFO", message, null);
    }

    public static void error(String message, Throwable t) {
        log("ERROR", message, t);
    }

    public static void warn(String message) {
        log("WARN", message, null);
    }
}

//
//package com.webserver.util;
//
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import com.microsoft.applicationinsights.TelemetryClient;
//import com.microsoft.applicationinsights.TelemetryConfiguration;
//import com.microsoft.applicationinsights.telemetry.SeverityLevel;
//
//public class Logger {
//    private static final String LOG_FILE = "webserver.log";
//    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//    private static final TelemetryClient client;
//
//    static{
//        TelemetryConfiguration configuration = TelemetryConfiguration.createDefault();
//        configuration.setInstrumentationKey("InstrumentationKey=3dd12fd3-2f83-4010-8185-96acd9ef245a;IngestionEndpoint=https://westeurope-5.in.applicationinsights.azure.com/;LiveEndpoint=https://westeurope.livediagnostics.monitor.azure.com/;ApplicationId=df159bc7-e776-46b3-9f0c-3261fbc8a060");
//        client = new TelemetryClient(configuration);
//    }
//
//    //tracks the response time and sends to azure insights
//    public static void trackResponseTime(long startingTime){
//        long duration = System.currentTimeMillis() - startingTime;
//        try {
//            client.trackMetric("ResponseTime", duration);
//            client.flush();
//
//        } catch (Exception e) {
//            System.err.println("There was an error when tracking response time." + e.getMessage());
//        }
//    }
//
//    private static void log(String level, String message, Throwable t) {
//        String timestamp = LocalDateTime.now().format(formatter);
//    } catch (IOException e) {
//        System.err.println("Failed to write to log file: " + e.getMessage());
//    }
//
//    //Send details to the application insights
//        try {
//        //traces the logs
//        client.trackTrace(logMessage, SeverityLevel.valueOf(level.toUpperCase()));
//
//        long totalMemoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//
//        client.trackMetric("MemoryUsed", totalMemoryUsed);
//
//        client.flush();
//    } catch (Exception e) {
//        System.err.println("There was an error when sending the telemetry data" + e.getMessage());
//    }
//}
//
////this is for the actual users, assuming we are taking user ID's and served files as input and plotted to hashmap.
//public static void trackFileMetrics(String fileName) {
//    try {
//
//        //Hashmap to filter metrics by filename (add more to hashmap to differentiate user files)
//        Map<String, String> user  = new HashMap<>();
//        user.put("fileName", fileName);
//
//        // Track the file accesses
//        client.trackEvent("accessedFile", user, null);
//
//        // Track the memory usage for the specific file
//        long totalMemoryUsage =  Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//        client.trackMetric("userMemoryUsage", totalMemoryUsage, 1, totalMemoryUsage, totalMemoryUsage, user);
//
//        //MORE TO BE ADDED
//        client.flush();
//
//    } catch (Exception e) {
//        System.err.println("Failed to get telemetry data: " + e.getMessage());
//    }
//}
//
//public static void info(String message) {
//    log("INFO", message, null);
//}
//
//public static void error(String message, Throwable t) {
//    log("ERROR", message, t);
//}
//
//public static void warn(String message) {
//    log("WARN", message, null);
//}
//}