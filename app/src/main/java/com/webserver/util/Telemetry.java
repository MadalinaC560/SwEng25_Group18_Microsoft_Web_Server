package com.webserver.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.telemetry.SeverityLevel;

public class Telemetry{
    private static final TelemetryClient client;
    private static long numberRequests = 0;
    private static long previousCalcTime = System.currentTimeMillis();
    private static long numberFailures = 0;

    static{
        TelemetryConfiguration configuration = TelemetryConfiguration.createDefault();
        String key =  loadInstrumentationKey();
        configuration.setInstrumentationKey(key);
        client = new TelemetryClient(configuration);
    }

    public static String loadInstrumentationKey(){
        String key = null;

        try{
            Properties props = new Properties();
            props.load(Telemetry.class.getClassLoader().getResourceAsStream("config.properties"));
            key = props.getProperty("APPINSIGHTS_INSTRUMENTATION_KEY");
        }catch (IOException e){
            System.err.println("There was an error when getting the key: " + e.getMessage());
        }

        return key;
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

    public static void incrementNumberRequests(){
        numberRequests++;
    }

    public static void trackServerMetrics(long startingTime){
        trackResponseTime(startingTime);

        try{

            long currentSystemTime = System.currentTimeMillis();
            long elapsedTime = currentSystemTime - previousCalcTime;

            //calculate if a minimum of 1 second have passed
            if(elapsedTime >= 1000){
                //converts number of requests to requests per second
                double rateOfRequests = (numberRequests * 1000) / elapsedTime;
                client.trackMetric("requestsPerSecond", rateOfRequests);

                numberRequests = 0;
                previousCalcTime = currentSystemTime;
            }

            //this gets the management bean
            ThreadMXBean beanThreadManagement = ManagementFactory.getThreadMXBean();

            client.trackMetric("activeThreads", Thread.activeCount());
            client.trackMetric("peakThreadNumber", beanThreadManagement.getPeakThreadCount());
            client.trackMetric("totalCreatedThreads", beanThreadManagement.getTotalStartedThreadCount());

            client.flush();
        } catch (Exception e){
            System.err.println("There was an error when tracking the server metrics: " + e.getMessage());
        }
    }
    

    //this is for the actual users, assuming we are taking user ID's and served files as input and plotted to hashmap.
    public static void trackFileMetrics(String appID){
         //Send details to the application insights
        try {

             //Hashmap to filter metrics by filename (add more to hashmap to differentiate user files)
            Map<String, String> user  = new HashMap<>();
            user.put("appID", appID);

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

    public static void trackFailures(Exception e, String path, String failureDetails){
        try{
            numberFailures++;
            Map<String, String> failureProperties = new HashMap<>();
            failureProperties.put("path", path);
            failureProperties.put("details", failureDetails);
            failureProperties.put("typeException", e.getClass().getName());

            //tracks given exception
            client.trackException(e, failureProperties, null);

            //tracks the failure count
            client.trackMetric("FailureCount", numberFailures);

            client.flush();

        } catch(Exception telemetryFailure){
            System.err.println("There was an error when tracking the failure" + telemetryFailure.getMessage());
        }
    }

    public static void trackLogMetric(String level, String message){
        try {
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
