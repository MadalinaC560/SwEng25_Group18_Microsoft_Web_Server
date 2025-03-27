package com.webserver.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.Optional;

public class AppManager {
    private static final AtomicInteger APP_ID_GENERATOR = new AtomicInteger(1000);

    public static DB.User findUserById(int userId) {
        return DB.getRoot().users.stream()
                .filter(u -> u.userId == userId)
                .findFirst()
                .orElse(null);
    }

    public static DB.App createApp(int userId, String name, String runtime) {
        DB.User user = findUserById(userId);
        if (user == null) return null;

        DB.App app = new DB.App();
        app.appId = APP_ID_GENERATOR.getAndIncrement();
        app.name = name;
        app.runtime = runtime;
        app.status = "stopped";
        app.routes = new java.util.ArrayList<>();

        user.apps.add(app);
        DB.save();
        return app;
    }

    public static void addRoute(int userId, int appId, String newRoute) {
        DB.User user = findUserById(userId);
        if (user == null) return;
        DB.App theApp = user.apps.stream()
                .filter(a -> a.appId == appId)
                .findFirst().orElse(null);
        if (theApp == null) return;

        theApp.routes.add(newRoute);
        DB.save();
    }

    public static java.util.List<DB.App> getUserApps(int userId) {
        DB.User user = findUserById(userId);
        if (user == null) return java.util.Collections.emptyList();
        return user.apps;
    }

    public static boolean updateAppStatus(int userId, int appId, String statusParam) {
        // 1) Find the user
        DB.User user = findUserById(userId);
        if (user == null) {
            return false;
        }

        // 2) Find the specific app
        for (DB.App app : user.apps) {
            if (app.appId == appId) {
                // 3) Update the status
                app.status = statusParam;

                // 4) Save changes to db.json
                DB.save();

                return true;
            }
        }
        // If we never found that appId, return false
        return false;
    }


    // etc.
}
