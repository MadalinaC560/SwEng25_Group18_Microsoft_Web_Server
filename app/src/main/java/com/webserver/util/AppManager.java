package com.webserver.util;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AppManager {

    private static final AtomicInteger APP_ID_GENERATOR = new AtomicInteger(1000);

    public static DB.App createApp(int tenantId, int ownerUserId, String name, String runtime) {
        // 1) Validate that the tenant exists
        DB.Tenant tenant = findTenantById(tenantId);
        if (tenant == null) {
            System.err.println("Tenant " + tenantId + " not found.");
            return null;
        }

        // 2) Validate that the user exists (and belongs to that tenant)
        DB.User user = findUserById(ownerUserId);
        if (user == null || user.tenantId != tenantId) {
            System.err.println("User " + ownerUserId + " not found (or doesn't match tenant).");
            return null;
        }

        // 3) Create the new App
        DB.App newApp = new DB.App();
        newApp.appId = APP_ID_GENERATOR.getAndIncrement();
        newApp.tenantId = tenantId;
        newApp.ownerUserId = ownerUserId;
        newApp.name = name;
        newApp.runtime = runtime;
        newApp.status = "stopped";
        newApp.routes = new java.util.ArrayList<>();

        // 4) Add it to DB
        DB.getRoot().apps.add(newApp);
        DB.save();

        return newApp;
    }

    public static List<DB.App> getAppsForTenant(int tenantId) {
        return DB.getRoot().apps.stream()
                .filter(app -> app.tenantId == tenantId)
                .collect(Collectors.toList());
    }

    public static DB.App findAppById(int appId) {
        return DB.getRoot().apps.stream()
                .filter(a -> a.appId == appId)
                .findFirst()
                .orElse(null);
    }

    public static boolean updateAppStatus(int appId, String newStatus) {
        DB.App theApp = findAppById(appId);
        if (theApp == null) {
            return false;
        }
        theApp.status = newStatus;
        DB.save();
        return true;
    }

    public static void addRoute(int appId, String newRoute) {
        DB.App theApp = findAppById(appId);
        if (theApp != null) {
            theApp.routes.add(newRoute);
            DB.save();
        }
    }

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
