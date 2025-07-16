package com.webserver.util;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.telemetry.SeverityLevel;
import com.sun.management.OperatingSystemMXBean;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Telemetry class that tracks both global server metrics (CPU usage, memory usage,
 * average response time, error rate, etc.) and per-app metrics (avg. response time,
 * request throughput, error rate, availability).
 */
public class Telemetry {

    // === Azure Application Insights Client ===
    private static final TelemetryClient client;

    static {
        TelemetryConfiguration configuration = TelemetryConfiguration.createDefault();
        String key = loadInstrumentationKey();
        configuration.setInstrumentationKey(key);
        client = new TelemetryClient(configuration);
    }

    // Loads instrumentation key from config, if any
    public static String loadInstrumentationKey() {
        String key = null;
        try {
            Properties props = new Properties();
            props.load(Telemetry.class.getClassLoader().getResourceAsStream("config.properties"));
            key = props.getProperty("APPINSIGHTS_INSTRUMENTATION_KEY");
        } catch (IOException e) {
            System.err.println("Error loading instrumentation key: " + e.getMessage());
        }
        return key;
    }

    // === Global Telemetry Counters ===
    private static long numberRequests = 0;
    private static long numberFailures = 0;
    private static double sumResponseTimes = 0;
    private static long totalResponseCount = 0;

    // For requestsPerSecond
    private static long previousCalcTime = System.currentTimeMillis();

    // For tracking IO rates
    private static long previousIOTime = System.currentTimeMillis();
    private static long lastBytesRead = 0;
    private static long lastBytesWritten = 0;

    // === ADDED FOR BANDWIDTH AGGREGATION (global) ===
    private static long intervalInboundBytes = 0;
    private static long intervalOutboundBytes = 0;

    // Rolling performance data for the global server:
    private static final int MAX_HISTORY = 10;
    private static final BlockingQueue<PerformancePoint> history = new ArrayBlockingQueue<>(MAX_HISTORY);

    /**
     * Per-app counters. Key = appId, Value = stats for that app.
     */
    private static final ConcurrentHashMap<Integer, AppStats> appMetricsMap = new ConcurrentHashMap<>();

    /**
     * Simple struct to hold counters for each app
     */
    private static class AppStats {
        long totalRequests;
        long totalFailures;
        long sumResponseTimeMs;

        // Bandwidth aggregator (per-app)
        long intervalInbound;
        long intervalOutbound;
    }

    // ------------------------------------------------------------------------------------
    // 1) Global (server-wide) metrics
    // ------------------------------------------------------------------------------------

    public static synchronized void incrementNumberRequests() {
        numberRequests++;
    }

    /**
     * Called for every request to track how large the request/response were (bytes).
     */
    public static synchronized void recordTraffic(int appId, long inboundBytes, long outboundBytes) {
        // Add to global aggregator
        intervalInboundBytes += inboundBytes;
        intervalOutboundBytes += outboundBytes;

        // Also track per-app
        AppStats stats = appMetricsMap.computeIfAbsent(appId, k -> new AppStats());
        synchronized (stats) {
            stats.intervalInbound  += inboundBytes;
            stats.intervalOutbound += outboundBytes;
        }
    }

    /**
     * Called once per request to measure how long that request took (ms).
     */
    public static synchronized void trackResponseTime(long startMillis) {
        long duration = System.currentTimeMillis() - startMillis;
        sumResponseTimes += duration;
        totalResponseCount++;
    }

    /**
     * Called once every cycle (e.g. every 30s) from the server to track server-wide metrics.
     */
    public static synchronized void trackServerMetrics(long startingTime) {
        // Optionally track overhead
        trackResponseTime(startingTime);

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - previousCalcTime;

        // If at least 1 second has passed, measure requests per second
        if (elapsedTime >= 1000) {
            double rateOfRequests = (numberRequests * 1000.0) / elapsedTime;
            client.trackMetric("requestsPerSecond", rateOfRequests);

            // Reset for next cycle
            numberRequests = 0;
            previousCalcTime = currentTime;
        }

        // Thread metrics
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        client.trackMetric("activeThreads", (double) Thread.activeCount());
        client.trackMetric("peakThreadNumber", (double) threadBean.getPeakThreadCount());
        client.trackMetric("totalCreatedThreads", (double) threadBean.getTotalStartedThreadCount());

        // CPU/memory
        trackCpuAndMemory();

        // IO rates
        trackIORates();

        // Rolling snapshot
        recordPerformanceSnapshot();

        // Reset counters in the aggregator for the next interval
        intervalInboundBytes  = 0;
        intervalOutboundBytes = 0;

        // Also reset each app’s aggregator
        for (AppStats stats : appMetricsMap.values()) {
            synchronized (stats) {
                stats.intervalInbound  = 0;
                stats.intervalOutbound = 0;
            }
        }

        client.flush();
    }

    /**
     * If a request fails, record in global counters + send to Azure.
     */
    public static synchronized void trackFailures(Exception e, String path, String failureDetails) {
        numberFailures++;
        Map<String, String> failureProps = new HashMap<>();
        failureProps.put("path", path);
        failureProps.put("details", failureDetails);
        failureProps.put("typeException", e.getClass().getName());

        try {
            client.trackException(e, failureProps, null);
            client.trackMetric("FailureCount", numberFailures);
            client.flush();
        } catch (Exception telemetryFailure) {
            System.err.println("Error tracking failure: " + telemetryFailure.getMessage());
        }
    }

    private static void trackCpuAndMemory() {
        try {
            OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            double cpuLoad = osBean.getCpuLoad();
            client.trackMetric("cpuLoad", cpuLoad);

            long totalMem = osBean.getTotalMemorySize();
            long freeMem  = osBean.getFreeMemorySize();
            long usedMem  = totalMem - freeMem;
            double memUsage = (double) usedMem / (double) totalMem;
            client.trackMetric("memoryUsage", memUsage);
        } catch (Exception e) {
            System.err.println("Error trackCpuAndMemory: " + e.getMessage());
        }
    }

    private static void trackIORates() {
        try {
            OperatingSystemMXBean baseBean =
                (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            if (baseBean instanceof com.sun.management.UnixOperatingSystemMXBean unixBean) {
                long currentTime = System.currentTimeMillis();
                long timeElapsed = currentTime - previousIOTime;
                if (timeElapsed >= 1000) {
                    long openFdCount = unixBean.getOpenFileDescriptorCount();
                    long maxFdCount  = unixBean.getMaxFileDescriptorCount();

                    double inputRate  = ((openFdCount - lastBytesRead) * 1000.0) / timeElapsed;
                    double outputRate = ((maxFdCount - lastBytesWritten) * 1000.0) / timeElapsed;

                    client.trackMetric("inputRate", inputRate);
                    client.trackMetric("outputRate", outputRate);

                    lastBytesRead    = openFdCount;
                    lastBytesWritten = maxFdCount;
                    previousIOTime   = currentTime;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to track IO rates: " + e.getMessage());
        }
    }

    private static synchronized void recordPerformanceSnapshot() {
        PerformancePoint p = new PerformancePoint();
        p.timestamp    = System.currentTimeMillis();
        p.serverLoad   = getCpuUsage() * 100.0;
        p.responseTime = getAvgResponseTime();
        p.errorRate    = getErrorRate();

        // record the inbound/outbound from this interval
        p.inboundBytes  = intervalInboundBytes;
        p.outboundBytes = intervalOutboundBytes;

        if (history.remainingCapacity() == 0) {
            history.poll();
        }
        history.offer(p);
    }

    /**
     * Return a fraction of CPU usage for the entire system: 0..1
     */
    public static synchronized double getCpuUsage() {
        try {
            OperatingSystemMXBean osBean = (OperatingSystemMXBean)
                ManagementFactory.getOperatingSystemMXBean();
            double load = osBean.getCpuLoad();
            if (Double.isNaN(load) || load < 0) return 0.0;
            return load;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static synchronized double getMemoryUsage() {
        try {
            OperatingSystemMXBean osBean = (OperatingSystemMXBean)
                ManagementFactory.getOperatingSystemMXBean();
            long totalMem = osBean.getTotalMemorySize();
            long freeMem  = osBean.getFreeMemorySize();
            long usedMem  = totalMem - freeMem;
            if (totalMem > 0) {
                return (double) usedMem / (double) totalMem;
            }
        } catch (Exception e) {
            // ignore
        }
        return 0.0;
    }

    public static synchronized double getAvgResponseTime() {
        if (totalResponseCount == 0) return 0.0;
        return sumResponseTimes / totalResponseCount;
    }

    public static synchronized double getErrorRate() {
        long totalReqs = totalResponseCount;
        if (totalReqs == 0) return 0.0;
        return ((double) numberFailures / (double) totalReqs) * 100.0;
    }

    public static synchronized double getSystemLoad() {
        return getCpuUsage() * 100.0;
    }

    /**
     * Return the rolling performance data (serverLoad, responseTime, errorRate, inbound/outbound).
     */
    public static synchronized List<Map<String, Object>> getPerformanceData() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (PerformancePoint p : history) {
            Map<String, Object> row = new HashMap<>();
            row.put("time",          formatTimestamp(p.timestamp));
            row.put("serverLoad",    p.serverLoad);
            row.put("responseTime",  p.responseTime);
            row.put("errorRate",     p.errorRate);

            // Convert inbound/outbound to MB for the front-end chart
            double inboundMB  = p.inboundBytes  / (1024.0 * 1024.0);
            double outboundMB = p.outboundBytes / (1024.0 * 1024.0);
            row.put("inbound",  inboundMB);
            row.put("outbound", outboundMB);

            result.add(row);
        }
        return result;
    }

    private static String formatTimestamp(long ts) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ts);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min  = c.get(Calendar.MINUTE);
        return String.format("%d:%02d", hour, min);
    }

    private static class PerformancePoint {
        long timestamp;
        double serverLoad;
        double responseTime;
        double errorRate;

        long inboundBytes;
        long outboundBytes;
    }

    // ------------------------------------------------------------------------------------
    // 2) Per-App Metrics
    // ------------------------------------------------------------------------------------

    /**
     * Record a single request for the specified app, measuring how long it took
     * and whether it was a failure.
     */
    public static void recordRequest(int appId, long startTimeMs, int statusCode) {
        long duration = System.currentTimeMillis() - startTimeMs;

        // Retrieve or create the stats struct
        AppStats stats = appMetricsMap.computeIfAbsent(appId, k -> new AppStats());
        synchronized (stats) {
            stats.totalRequests++;
            stats.sumResponseTimeMs += duration;
            if (statusCode >= 400) {
                stats.totalFailures++;
            }
        }
    }

    /**
     * Return a map with "avgResponseTime", "requestThroughput", "errorRate", "availability",
     * plus "performanceData" referencing the global ring buffer if you want to reuse it.
     */
    public static Map<String, Object> getAppMetrics(int appId) {
        AppStats stats = appMetricsMap.get(appId);
        if (stats == null) {
            // no data => zero
            Map<String, Object> empty = new HashMap<>();
            empty.put("avgResponseTime",   0.0);
            empty.put("requestThroughput", 0.0);
            empty.put("errorRate",         0.0);
            empty.put("availability",      100.0);
            empty.put("performanceData",   Collections.emptyList());
            return empty;
        }

        long totalReq, failures, sumTimeMs;
        synchronized (stats) {
            totalReq   = stats.totalRequests;
            failures   = stats.totalFailures;
            sumTimeMs  = stats.sumResponseTimeMs;
        }
        double avgResp  = (totalReq == 0) ? 0.0 : (sumTimeMs / (double) totalReq);
        double eRate    = (totalReq == 0) ? 0.0 : (failures / (double) totalReq);
        double availPct = (1.0 - eRate) * 100.0;

        // requestThroughput is totalRequests (lifetime).
        double requestThroughput = totalReq;

        // For simplicity, reusing the global ring buffer for bandwidth as well
        List<Map<String, Object>> samePerfData = getPerformanceData();

        Map<String, Object> result = new HashMap<>();
        result.put("avgResponseTime",   avgResp);
        result.put("requestThroughput", requestThroughput);
        result.put("errorRate",         eRate * 100.0);
        result.put("availability",      availPct);
        result.put("performanceData",   samePerfData);
        return result;
    }

    // ------------------------------------------------------------------------------------
    // Additional older code for logs & file usage
    // ------------------------------------------------------------------------------------

    public static void trackFileMetrics(String appID) {
        try {
            Map<String, String> user = new HashMap<>();
            user.put("appID", appID);
            client.trackEvent("accessedFile", user, null);

            long totalMemoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            client.trackMetric("MemoryUsed", totalMemoryUsed);
            client.flush();
        } catch (Exception e) {
            System.err.println("Error tracking file metrics: " + e.getMessage());
        }
    }

    public static void trackDiskUsage() {
        try {
            File[] roots = File.listRoots();
            for (File root : roots) {
                long totalSpace   = root.getTotalSpace();
                long spaceUsable  = root.getUsableSpace();
                long spaceUsed    = totalSpace - spaceUsable;

                double totalGB    = totalSpace   / (1024.0 * 1024.0 * 1024.0);
                double usedGB     = spaceUsed    / (1024.0 * 1024.0 * 1024.0);
                double freeGB     = spaceUsable  / (1024.0 * 1024.0 * 1024.0);

                client.trackMetric("TotalGB", totalGB);
                client.trackMetric("UsedGB",  usedGB);
                client.trackMetric("FreeGB",  freeGB);
            }
            client.flush();
        } catch (Exception e) {
            System.err.println("Error tracking disk usage: " + e.getMessage());
        }
    }

    public static void trackLogMetric(String level, String message) {
        try {
            SeverityLevel severity = switch (level.toUpperCase()) {
                case "ERROR" -> SeverityLevel.Error;
                case "WARN"  -> SeverityLevel.Warning;
                case "INFO"  -> SeverityLevel.Information;
                default      -> SeverityLevel.Verbose;
            };
            client.trackTrace(message, severity);
            client.flush();
        } catch (Exception e) {
            System.err.println("Failed to track log metric: " + e.getMessage());
        }
    }
}
