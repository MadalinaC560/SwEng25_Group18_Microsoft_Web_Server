package com.webserver.util;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.telemetry.SeverityLevel;
import com.sun.management.UnixOperatingSystemMXBean;

public class Telemetry{
    private static final TelemetryClient client;
    private static long numberRequests = 0;
    private static long previousCalcTime = System.currentTimeMillis();
    private static long numberFailures = 0;
    private static long previousIOTime = System.currentTimeMillis();
    private static long lastBytesRead = 0;
    private static long lastBytesWritten = 0;

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
        trackDiskUsage();

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

            trackIORates();

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

    public static void trackIORates(){
        try {
            OperatingSystemMXBean osManagementBean = ManagementFactory.getOperatingSystemMXBean();
            if(osManagementBean instanceof UnixOperatingSystemMXBean){
                UnixOperatingSystemMXBean unixBean = (UnixOperatingSystemMXBean) osManagementBean;

                long currentTime = System.currentTimeMillis();
                long timeElapsed = currentTime - previousIOTime;

                //gather the current IO counts
                long bytesRead = unixBean.getOpenFileDescriptorCount();
                long bytesWritten = unixBean.getMaxFileDescriptorCount();

                //calculates every second
                if(timeElapsed >= 1000){
                    double bytesReadRate = ((bytesRead - lastBytesRead) * 1000) / timeElapsed;
                    double bytesWrittenRate = ((bytesWritten - lastBytesWritten) * 1000) / timeElapsed;

                    client.trackMetric("inputRate", bytesReadRate);
                    client.trackMetric("outputRate", bytesWrittenRate);

                    lastBytesRead = bytesRead;
                    lastBytesWritten = bytesWritten;
                    previousIOTime = currentTime;
                }
            }
            client.flush();
        } catch (Exception e) {
            System.err.println("Failed to track the IO rates: " + e.getMessage());
        }
    }

    public static void trackDiskUsage(){
        try{
            File[] roots = File.listRoots();

            for(File root:roots){
                long totalSpace = root.getTotalSpace();
                long spaceUsable = root.getUsableSpace();
                long spaceUsed = totalSpace - spaceUsable;

                //convert the values to gigabutes, better for readability
                double totalGB = totalSpace / (1024.0 * 1024.0 * 1024.0);
                double usedGB = spaceUsed/ (1024.0 * 1024.0 * 1024.0);
                double freeGB = spaceUsable / (1024.0 * 1024.0 * 1024.0);

                client.trackMetric("TotalGB", totalGB );
                client.trackMetric("UsedGB", usedGB);
                client.trackMetric("FreeGB", freeGB);
            }
            
            client.flush();

        }
        catch(Exception e){
            System.err.println("There was an error when tracking disk usage: " + e.getMessage());
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
