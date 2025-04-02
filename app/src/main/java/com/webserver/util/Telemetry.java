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

/**
 * Telemetry class that tracks requests, failures, CPU usage, memory usage,
 * response times, and also maintains a small rolling history for performance data.
 */
public class Telemetry {

  // === Azure Application Insights Client ===
  private static final TelemetryClient client;

  static {
    TelemetryConfiguration configuration =
      TelemetryConfiguration.createDefault();
    String key = loadInstrumentationKey();
    configuration.setInstrumentationKey(key);
    client = new TelemetryClient(configuration);
  }

  /**
   * Load the instrumentation key from config.properties, if you have one.
   */
  public static String loadInstrumentationKey() {
    String key = null;
    try {
      Properties props = new Properties();
      props.load(
        Telemetry.class.getClassLoader()
          .getResourceAsStream("config.properties")
      );
      key = props.getProperty("APPINSIGHTS_INSTRUMENTATION_KEY");
    } catch (IOException e) {
      System.err.println(
        "Error loading instrumentation key: " + e.getMessage()
      );
    }
    return key;
  }

  // === Telemetry Counters ===
  private static long numberRequests = 0; // counts total requests so far
  private static long numberFailures = 0; // counts total failures/exceptions
  private static double sumResponseTimes = 0; // accumulates total response time in ms
  private static long totalResponseCount = 0; // how many requests we used to sum responseTimes

  // For the requestsPerSecond logic:
  private static long previousCalcTime = System.currentTimeMillis();

  // For tracking IO rates
  private static long previousIOTime = System.currentTimeMillis();
  private static long lastBytesRead = 0;
  private static long lastBytesWritten = 0;

  // === Rolling Performance Data ===
  // We'll store up to 50 snapshots of performance to provide for charting
  private static final int MAX_HISTORY = 50;
  private static final BlockingQueue<PerformancePoint> history =
    new ArrayBlockingQueue<>(MAX_HISTORY);

  /**
   * Called when a request is first encountered, increments request count.
   */
  public static synchronized void incrementNumberRequests() {
    numberRequests++;
  }

  /**
   * Called to measure how long a request took in ms.
   */
  public static synchronized void trackResponseTime(long startingTime) {
    long duration = System.currentTimeMillis() - startingTime;

    // Update local counters for average response-time calculation
    sumResponseTimes += duration;
    totalResponseCount++;

    // Also send to Azure
    try {
      client.trackMetric("ResponseTime", duration);
      client.flush();
    } catch (Exception e) {
      System.err.println("Error tracking response time: " + e.getMessage());
    }
  }

  /**
   * Called once every cycle (e.g. every 30s) from the Server or ConnectionHandler to track
   * server-wide metrics: requests/s, CPU usage, memory usage, etc.
   */
  public static synchronized void trackServerMetrics(long startingTime) {
    // Optionally call trackResponseTime for the overhead of the entire cycle
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
    client.trackMetric(
      "peakThreadNumber",
      (double) threadBean.getPeakThreadCount()
    );
    client.trackMetric(
      "totalCreatedThreads",
      (double) threadBean.getTotalStartedThreadCount()
    );

    // Attempt to track CPU usage, memory usage, IO rates, etc.
    trackCpuAndMemory();
    trackIORates();

    // Now store a snapshot in our local ring buffer for the line chart
    recordPerformanceSnapshot();

    // Flush to Azure
    client.flush();
  }

  /**
   * Called when we have an exception or failure to process a request.
   */
  public static synchronized void trackFailures(
    Exception e,
    String path,
    String failureDetails
  ) {
    numberFailures++;
    Map<String, String> failureProps = new HashMap<>();
    failureProps.put("path", path);
    failureProps.put("details", failureDetails);
    failureProps.put("typeException", e.getClass().getName());

    // Azure
    try {
      client.trackException(e, failureProps, null);
      client.trackMetric("FailureCount", numberFailures);
      client.flush();
    } catch (Exception telemetryFailure) {
      System.err.println(
        "Error tracking failure in telemetry: " + telemetryFailure.getMessage()
      );
    }
  }

  /**
   * Measure CPU usage, memory usage from the OS bean, and track them in Azure if desired.
   */
  private static void trackCpuAndMemory() {
    try {
      OperatingSystemMXBean osBean =
        (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

      // CPU load for the whole system, 0.0 to 1.0
      double cpuLoad = osBean.getCpuLoad();
      client.trackMetric("cpuLoad", cpuLoad);

      // Physical memory usage
      long totalMem = osBean.getTotalMemorySize();
      long freeMem = osBean.getFreeMemorySize();
      long usedMem = totalMem - freeMem;
      double memUsage = (double) usedMem / (double) totalMem;
      client.trackMetric("memoryUsage", memUsage);
    } catch (Exception e) {
      System.err.println("Error trackCpuAndMemory: " + e.getMessage());
    }
  }

  /**
   * Track input and output rates using open/max file descriptors, if supported on Unix.
   */
  private static void trackIORates() {
    try {
      // 1) Cast to the base com.sun.management type
      OperatingSystemMXBean baseBean =
        (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

      // 2) Check if it's actually a UnixOperatingSystemMXBean
      if (
        baseBean instanceof
        com.sun.management.UnixOperatingSystemMXBean unixBean
      ) {
        long currentTime = System.currentTimeMillis();
        long timeElapsed = currentTime - previousIOTime;

        if (timeElapsed >= 1000) {
          long openFdCount = unixBean.getOpenFileDescriptorCount();
          long maxFdCount = unixBean.getMaxFileDescriptorCount();

          double inputRate =
            ((openFdCount - lastBytesRead) * 1000.0) / timeElapsed;
          double outputRate =
            ((maxFdCount - lastBytesWritten) * 1000.0) / timeElapsed;

          client.trackMetric("inputRate", inputRate);
          client.trackMetric("outputRate", outputRate);

          lastBytesRead = openFdCount;
          lastBytesWritten = maxFdCount;
          previousIOTime = currentTime;
        }
      }
      // else: it’s not a Unix system, so skip FD counts

    } catch (Exception e) {
      System.err.println("Failed to track IO rates: " + e.getMessage());
    }
  }

  /**
   * Snapshot current "server load," average response time, error rate, etc., and store in ring buffer.
   */
  private static synchronized void recordPerformanceSnapshot() {
    PerformancePoint snapshot = new PerformancePoint();
    snapshot.timestamp = System.currentTimeMillis();
    snapshot.serverLoad = getCpuUsage() * 100.0; // if you want "0-100" range
    snapshot.responseTime = getAvgResponseTime();
    snapshot.errorRate = getErrorRate();

    // Add to the ring buffer
    if (history.remainingCapacity() == 0) {
      history.poll(); // remove oldest if at capacity
    }
    history.offer(snapshot);
  }

  // === Public Getters for Exposing the Data in e.g. /api/metrics ===

  /**
   * Return a 0.0-1.0 fraction of CPU usage for the entire system.
   */
  public static synchronized double getCpuUsage() {
    try {
      OperatingSystemMXBean osBean =
        (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
      double load = osBean.getCpuLoad();
      if (Double.isNaN(load) || load < 0) {
        return 0.0;
      }
      return load;
    } catch (Exception e) {
      return 0.0;
    }
  }

  /**
   * Return a 0.0-1.0 fraction for memory usage of the system.
   */
  public static synchronized double getMemoryUsage() {
    try {
      OperatingSystemMXBean osBean =
        (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
      long totalMem = osBean.getTotalMemorySize();
      long freeMem = osBean.getFreeMemorySize();
      long usedMem = totalMem - freeMem;
      if (totalMem > 0) {
        return (double) usedMem / (double) totalMem;
      }
    } catch (Exception e) {
      // fallback
    }
    return 0.0;
  }

  /**
   * Return the average response time in ms since the server started,
   * or since the counters were last reset.
   */
  public static synchronized double getAvgResponseTime() {
    if (totalResponseCount == 0) return 0.0;
    return sumResponseTimes / totalResponseCount;
  }

  /**
   * Return the error rate as a percent, e.g. 1.2 means 1.2%.
   */
  public static synchronized double getErrorRate() {
    long totalReqs = totalResponseCount;
    if (totalReqs == 0) return 0.0;
    return ((double) numberFailures / (double) totalReqs) * 100.0;
  }

  /**
   * If you want to define "systemLoad" differently than CPU usage,
   * you can do so. For now: systemLoad = CPU usage * 100.
   */
  public static synchronized double getSystemLoad() {
    return getCpuUsage() * 100.0;
  }

  /**
   * Return a rolling time-series of up to 50 snapshots.
   */
  public static synchronized List<Map<String, Object>> getPerformanceData() {
    List<Map<String, Object>> result = new ArrayList<>();
    for (PerformancePoint p : history) {
      // Convert each snapshot into a map for easy JSON serialization
      Map<String, Object> row = new HashMap<>();
      row.put("time", formatTimestamp(p.timestamp));
      row.put("serverLoad", p.serverLoad);
      row.put("responseTime", p.responseTime);
      row.put("errorRate", p.errorRate);
      result.add(row);
    }
    return result;
  }

  // Placeholder method to format a timestamp into "HH:mm" or similar
  private static String formatTimestamp(long ts) {
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(ts);
    int hour = c.get(Calendar.HOUR_OF_DAY);
    int min = c.get(Calendar.MINUTE);
    return String.format("%d:%02d", hour, min);
  }

  // === Classes & Data Structures ===

  /**
   * A snapshot of performance metrics to store in our rolling history,
   * used for line charts (serverLoad, responseTime, errorRate).
   */
  private static class PerformancePoint {

    long timestamp; // ms since epoch
    double serverLoad; // 0..100
    double responseTime; // average response time
    double errorRate; // % of errors
  }

  // ==== Additional "Trackers" for file metrics or logs, unchanged from old code ====

  public static void trackFileMetrics(String appID) {
    try {
      Map<String, String> user = new HashMap<>();
      user.put("appID", appID);

      client.trackEvent("accessedFile", user, null);

      long totalMemoryUsed =
        Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
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
        long totalSpace = root.getTotalSpace();
        long spaceUsable = root.getUsableSpace();
        long spaceUsed = totalSpace - spaceUsable;

        //convert the values to gigabutes, better for readability
        double totalGB = totalSpace / (1024.0 * 1024.0 * 1024.0);
        double usedGB = spaceUsed / (1024.0 * 1024.0 * 1024.0);
        double freeGB = spaceUsable / (1024.0 * 1024.0 * 1024.0);

        client.trackMetric("TotalGB", totalGB);
        client.trackMetric("UsedGB", usedGB);
        client.trackMetric("FreeGB", freeGB);
      }

      client.flush();
    } catch (Exception e) {
      System.err.println(
        "There was an error when tracking disk usage: " + e.getMessage()
      );
    }
  }

  public static void trackLogMetric(String level, String message) {
    try {
      SeverityLevel severity =
        switch (level.toUpperCase()) {
          case "ERROR" -> SeverityLevel.Error;
          case "WARN" -> SeverityLevel.Warning;
          case "INFO" -> SeverityLevel.Information;
          default -> SeverityLevel.Verbose;
        };
      client.trackTrace(message, severity);
      client.flush();
    } catch (Exception e) {
      System.err.println("Failed to track log metric: " + e.getMessage());
    }
  }
}
