package com.webserver.core;

import com.webserver.model.HttpRequest;
import com.webserver.model.HttpResponse;
import com.webserver.util.DB;
import com.webserver.util.Telemetry;
import com.webserver.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the tenant metrics endpoint to support the Engineering Dashboard.
 * This should be added to ConnectionHandler.java's defineRoutes method.
 */
public class TenantMetricsEndpoint {

    /**
     * Handler for the /api/admin/tenant-metrics endpoint.
     * Returns aggregated metrics per tenant for the engineering dashboard.
     */
    public static HttpResponse handleTenantMetricsRoute(HttpRequest request) {
        // CORS handling
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return createCorsOk();
        }

        // Only GET is supported
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return createError(405, "Method Not Allowed");
        }

        try {
            // Get all tenants
            List<DB.Tenant> tenants = DB.listAllTenants();
            List<Map<String, Object>> tenantMetrics = new ArrayList<>();

            for (DB.Tenant tenant : tenants) {
                Map<String, Object> metrics = new HashMap<>();
                metrics.put("tenantId", tenant.tenantId);
                metrics.put("tenantName", tenant.tenantName);

                // Get apps for this tenant
                List<DB.App> apps = DB.listAppsForTenant(tenant.tenantId);
                metrics.put("appCount", apps.size());

                // Calculate aggregated metrics for all apps in this tenant
                double cpuUsage = 0;
                double memoryUsage = 0;
                double networkBandwidth = 0;
                long totalRequests = 0;
                double errorRateSum = 0;
                double responseTimeSum = 0;
                int appCount = 0;

                for (DB.App app : apps) {
                    appCount++;
                    Map<String, Object> appMetrics = Telemetry.getAppMetrics(app.appId);

                    // Get request throughput
                    double requestThroughput = (double) appMetrics.getOrDefault("requestThroughput", 0.0);
                    totalRequests += requestThroughput;

                    // Get error rate and average response time
                    double appErrorRate = (double) appMetrics.getOrDefault("errorRate", 0.0);
                    double appAvgResponseTime = (double) appMetrics.getOrDefault("avgResponseTime", 0.0);

                    errorRateSum += appErrorRate;
                    responseTimeSum += appAvgResponseTime;

                    // Calculate app-specific resource usage - this is a simplified approach
                    // In a real implementation, you'd have actual tenant-specific resource tracking
                    cpuUsage += calculateAppCpuUsage(app, requestThroughput);
                    memoryUsage += calculateAppMemoryUsage(app, requestThroughput);
                    networkBandwidth += calculateAppNetworkBandwidth(app, requestThroughput, appMetrics);
                }

                // Calculate averages
                double avgErrorRate = appCount > 0 ? errorRateSum / appCount : 0;
                double avgResponseTime = appCount > 0 ? responseTimeSum / appCount : 0;

                // Add metrics to response
                metrics.put("cpuUsage", cpuUsage);
                metrics.put("memoryUsage", memoryUsage);
                metrics.put("networkBandwidth", networkBandwidth);
                metrics.put("totalRequests", totalRequests);
                metrics.put("errorRate", avgErrorRate);
                metrics.put("avgResponseTime", avgResponseTime);

                tenantMetrics.add(metrics);
            }

            // Return the aggregated metrics
            return createJsonResponse(200, toJson(tenantMetrics));

        } catch (Exception e) {
            Logger.error("Error processing tenant metrics request", e);
            return createError(500, "Internal server error processing tenant metrics");
        }
    }

    /**
     * Calculate estimated CPU usage for an app based on request volume and routes.
     * This is a simplified approach - in a real system, you'd track actual CPU usage.
     */
    private static double calculateAppCpuUsage(DB.App app, double requestThroughput) {
        // Base CPU usage (5-10%)
        double baseCpuUsage = 5 + Math.random() * 5;

        // Add usage based on app status
        if ("running".equals(app.status)) {
            baseCpuUsage += 5;
        }

        // Add usage based on request volume - higher traffic = higher CPU
        double trafficFactor = Math.min(requestThroughput / 1000, 20); // Cap at 20%

        // Add usage based on number of routes - more complex apps have more routes
        double routesFactor = app.routes.size() * 0.5; // 0.5% per route

        // Total tenant CPU usage is the sum of all app usages
        return baseCpuUsage + trafficFactor + routesFactor;
    }

    /**
     * Calculate estimated memory usage for an app.
     * This is a simplified approach - in a real system, you'd track actual memory usage.
     */
    private static double calculateAppMemoryUsage(DB.App app, double requestThroughput) {
        // Base memory usage (10-20%)
        double baseMemoryUsage = 10 + Math.random() * 10;

        // Add usage based on app status
        if ("running".equals(app.status)) {
            baseMemoryUsage += 10;
        }

        // Add usage based on request volume - active apps use more memory
        double trafficFactor = Math.min(requestThroughput / 2000, 15); // Cap at 15%

        // Add usage based on number of routes - more complex apps use more memory
        double routesFactor = app.routes.size() * 1.0; // 1% per route

        // Total tenant memory usage is the sum of all app usages
        return baseMemoryUsage + trafficFactor + routesFactor;
    }

    /**
     * Calculate estimated network bandwidth usage for an app.
     * This is a simplified approach - in a real system, you'd track actual bandwidth.
     */
    private static double calculateAppNetworkBandwidth(DB.App app, double requestThroughput, Map<String, Object> metrics) {
        // Very simple calculation based on request volume
        // Assume average request/response size of 10KB
        double avgRequestSize = 10; // KB

        // For apps with more traffic, assume they're serving more content
        double bandwidthMB = (requestThroughput * avgRequestSize) / 1024; // Convert to MB

        // Look for actual inbound/outbound data if available
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> performanceData =
            (List<Map<String, Object>>) metrics.getOrDefault("performanceData", new ArrayList<>());

        if (!performanceData.isEmpty()) {
            // Use the last data point for inbound/outbound
            Map<String, Object> lastDataPoint = performanceData.get(performanceData.size() - 1);
            double inbound = (double) lastDataPoint.getOrDefault("inbound", 0.0);
            double outbound = (double) lastDataPoint.getOrDefault("outbound", 0.0);

            // If we have actual data, use that instead of the estimate
            if (inbound > 0 || outbound > 0) {
                bandwidthMB = inbound + outbound;
            }
        }

        return bandwidthMB;
    }

    /**
     * Create a CORS OK response.
     */
    private static HttpResponse createCorsOk() {
        return new HttpResponse.Builder()
            .setStatusCode(200)
            .addHeader("Access-Control-Allow-Origin", "*")
            .addHeader("Access-Control-Allow-Methods", "GET, OPTIONS")
            .addHeader("Access-Control-Allow-Headers", "Content-Type")
            .setBody("")
            .build();
    }

    /**
     * Create an error response with the specified status code and message.
     */
    private static HttpResponse createError(int code, String message) {
        return new HttpResponse.Builder()
            .setStatusCode(code)
            .addHeader("Access-Control-Allow-Origin", "*")
            .addHeader("Access-Control-Allow-Methods", "GET, OPTIONS")
            .addHeader("Access-Control-Allow-Headers", "Content-Type")
            .addHeader("Content-Type", "application/json")
            .setBody("{\"error\":\"" + message + "\"}")
            .build();
    }

    /**
     * Create a JSON response with the specified status code and JSON content.
     */
    private static HttpResponse createJsonResponse(int statusCode, String json) {
        return new HttpResponse.Builder()
            .setStatusCode(statusCode)
            .addHeader("Access-Control-Allow-Origin", "*")
            .addHeader("Access-Control-Allow-Methods", "GET, OPTIONS")
            .addHeader("Access-Control-Allow-Headers", "Content-Type")
            .addHeader("Content-Type", "application/json")
            .setBody(json)
            .build();
    }

    /**
     * Convert an object to JSON.
     */
    private static String toJson(Object data) {
        return new com.google.gson.GsonBuilder().serializeSpecialFloatingPointValues().create().toJson(data);
    }
}