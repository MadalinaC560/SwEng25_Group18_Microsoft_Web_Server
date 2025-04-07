package com.webserver.util;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * AppManager is a higher-level helper for creating/updating apps.
 * It is fully backed by DB.java, which now uses Azure SQL under the hood.
 */
public class AppManager {

    private static final AtomicInteger APP_ID_GENERATOR = new AtomicInteger(1000);

    /**
     * Creates an app under a given tenant, owned by a given user.
     * This is just an example “helper” method.
     */
    public static DB.App createApp(int tenantId, int ownerUserId, String name, String runtime) {
        // 1) Validate that the tenant exists
        DB.Tenant tenant = findTenantById(tenantId);
        if (tenant == null) {
            System.err.println("Tenant " + tenantId + " not found.");
            return null;
        }

        // 2) Validate that the user belongs to that tenant
        DB.User user = findUserById(ownerUserId);
        if (user == null || user.tenantId != tenantId) {
            System.err.println("User " + ownerUserId + " not found (or doesn't match tenant).");
            return null;
        }

        // 3) Create the new App
        DB.App newApp = new DB.App();
        newApp.appId = APP_ID_GENERATOR.incrementAndGet();
        newApp.tenantId = tenantId;
        newApp.ownerUserId = ownerUserId;
        newApp.name = name;
        newApp.runtime = runtime;
        newApp.status = "stopped";
        newApp.routes = new java.util.ArrayList<>();

        // 4) Add it to DB in memory
        DB.getRoot().apps.add(newApp);

        // 5) Persist to Azure
        DB.save();

        return newApp;
    }

    /**
     * Returns all apps that belong to a given tenant.
     */
    public static List<DB.App> getAppsForTenant(int tenantId) {
        // Filter in-memory
        return DB.getRoot().apps.stream()
                .filter(app -> app.tenantId == tenantId)
                .collect(Collectors.toList());
    }

    /**
     * Finds a single App by ID, or null if not found.
     */
    public static DB.App findAppById(int appId) {
        return DB.getRoot().apps.stream()
                .filter(a -> a.appId == appId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Updates the status of an app. Return true if successful.
     */
    public static boolean updateAppStatus(int appId, String newStatus) {
        DB.App theApp = findAppById(appId);
        if (theApp == null) {
            return false;
        }
        theApp.status = newStatus;
        DB.save();
        return true;
    }

    /**
     * Appends a new route to the app’s route list.
     */
    public static void addRoute(int appId, String newRoute) {
        DB.App theApp = findAppById(appId);
        if (theApp != null) {
            theApp.routes.add(newRoute);
            DB.save();
        }
    }

    /**
     * Removes an app from the DB entirely.
     */
    public static boolean deleteApp(int appId) {
        List<DB.App> apps = DB.getRoot().apps;
        DB.App toRemove = findAppById(appId);
        if (toRemove != null) {
            apps.remove(toRemove);
            DB.save();
            return true;
        }
        return false;
    }

    //--------------------------------------------------------------------------
    // Private helper methods
    //--------------------------------------------------------------------------
    private static DB.Tenant findTenantById(int tenantId) {
        return DB.getRoot().tenants.stream()
                .filter(t -> t.tenantId == tenantId)
                .findFirst()
                .orElse(null);
    }

    private static DB.User findUserById(int userId) {
        return DB.getRoot().users.stream()
                .filter(u -> u.userId == userId)
                .findFirst()
                .orElse(null);
    }
}
