package com.webserver.util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TenantResourceMonitor - Tracks per-tenant resource usage using JMX
 */
public class TenantResourceMonitor {

    // Store tenant resource usage
    private static final Map<Integer, TenantResource> tenantResources = new ConcurrentHashMap<>();

    // JMX beans for system metrics
    private static final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private static final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private static final Runtime runtime = Runtime.getRuntime();

    /**
     * Class to hold resource usage for a tenant
     */
    public static class TenantResource {
        private int tenantId;
        private String tenantName;
        private double cpuAllocation; // Allocated CPU cores
        private double memoryAllocation; // Allocated memory in GB
        private long requestCount; // Total requests processed for this tenant
        private long lastUpdated; // Timestamp of last update

        public TenantResource(int tenantId, String tenantName) {
            this.tenantId = tenantId;
            this.tenantName = tenantName;
            this.cpuAllocation = 0.5; // Start with 0.5 core allocation
            this.memoryAllocation = 1.0; // Start with 1 GB allocation
            this.requestCount = 0;
            this.lastUpdated = System.currentTimeMillis();
        }

        public synchronized void incrementRequestCount() {
            this.requestCount++;
            this.lastUpdated = System.currentTimeMillis();

            // Scale resource allocation based on request count
            // This is a simple scaling algorithm - you can replace with more sophisticated logic
            if (requestCount <= 100) {
                cpuAllocation = 0.5; // Base CPU for small tenants
                memoryAllocation = 1.0; // Base memory for small tenants
            } else if (requestCount <= 1000) {
                cpuAllocation = 1.0; // Medium tenants
                memoryAllocation = 2.0;
            } else if (requestCount <= 10000) {
                cpuAllocation = 2.0; // Large tenants
                memoryAllocation = 4.0;
            } else {
                cpuAllocation = 4.0; // Very large tenants
                memoryAllocation = 8.0;
            }
        }

        public int getTenantId() {
            return tenantId;
        }

        public String getTenantName() {
            return tenantName;
        }

        public double getCpuAllocation() {
            return cpuAllocation;
        }

        public double getMemoryAllocation() {
            return memoryAllocation;
        }

        public long getRequestCount() {
            return requestCount;
        }

        public long getLastUpdated() {
            return lastUpdated;
        }
    }

    /**
     * Record a request for a specific tenant, updating its resource usage
     *
     * @param tenantId The tenant ID
     * @param tenantName The tenant name
     */
    public static void recordRequest(int tenantId, String tenantName) {
        TenantResource resource = tenantResources.computeIfAbsent(
            tenantId,
            id -> new TenantResource(id, tenantName)
        );
        resource.incrementRequestCount();
    }

    /**
     * Get all tenant resources
     *
     * @return Map of tenant ID to resource usage
     */
    public static Map<Integer, TenantResource> getAllTenantResources() {
        return tenantResources;
    }

    /**
     * Get JMX metrics about the system
     *
     * @return Map containing system metrics
     */
    public static Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();

        // Get available processors
        int processors = runtime.availableProcessors();
        metrics.put("availableProcessors", processors);

        // Get system CPU load
        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
            double cpuLoad = sunOsBean.getCpuLoad();
            metrics.put("systemCpuLoad", cpuLoad);

            // Get process CPU load
            double processCpuLoad = sunOsBean.getProcessCpuLoad();
            metrics.put("processCpuLoad", processCpuLoad);
        }

        // Get memory metrics
        long maxMemory = runtime.maxMemory(); // Maximum memory the JVM will attempt to use
        long totalMemory = runtime.totalMemory(); // Total memory in the JVM
        long freeMemory = runtime.freeMemory(); // Free memory in the JVM
        long usedMemory = totalMemory - freeMemory; // Used memory

        metrics.put("maxMemoryBytes", maxMemory);
        metrics.put("totalMemoryBytes", totalMemory);
        metrics.put("usedMemoryBytes", usedMemory);
        metrics.put("freeMemoryBytes", freeMemory);

        // Convert to GB for easier reading
        double maxMemoryGB = maxMemory / (1024.0 * 1024.0 * 1024.0);
        double totalMemoryGB = totalMemory / (1024.0 * 1024.0 * 1024.0);
        double usedMemoryGB = usedMemory / (1024.0 * 1024.0 * 1024.0);
        double freeMemoryGB = freeMemory / (1024.0 * 1024.0 * 1024.0);

        metrics.put("maxMemoryGB", maxMemoryGB);
        metrics.put("totalMemoryGB", totalMemoryGB);
        metrics.put("usedMemoryGB", usedMemoryGB);
        metrics.put("freeMemoryGB", freeMemoryGB);

        // Thread metrics
        metrics.put("threadCount", threadBean.getThreadCount());
        metrics.put("peakThreadCount", threadBean.getPeakThreadCount());
        metrics.put("totalStartedThreadCount", threadBean.getTotalStartedThreadCount());

        return metrics;
    }

    /**
     * Clear all tenant resources (mainly for testing)
     */
    public static void clear() {
        tenantResources.clear();
    }
}