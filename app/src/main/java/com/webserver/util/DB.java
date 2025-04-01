package com.webserver.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DB {

    private static final String DB_PATH = "db.json";
    private static final Gson gson = new Gson();
    private static Root rootCache; // In-memory copy

    public static class Root {
        public List<Tenant> tenants;
        public List<User> users;
        public List<App> apps;
        public List<Engineer> engineers;
    }

    public static class Tenant {
        public int tenantId;
        public String tenantName;
        // Add other fields as you wish (e.g., creationDate, contactEmail, etc.)
    }

    public static class User {
        public int userId;
        public int tenantId;  // references which tenant this user belongs to
        public String username;
        public String role;   // e.g. "member", "admin", etc.
    }

    public static class App {
        public int appId;
        public int tenantId;       // references which tenant it belongs to
        public String name;
        public String runtime;     // e.g. "nodejs", "php", etc.
        public String status;      // e.g. "running", "stopped"
        public int ownerUserId;    // which user “owns” the app
        public List<String> routes; // e.g. ["/app_1000/index.html"]
    }

    public static class Engineer {
        public int engineerId;
        public String username;
        public String role; // likely always "engineer" or similar
    }

    public static synchronized void load() {
        try (Reader reader = new FileReader(DB_PATH)) {
            Type rootType = new TypeToken<Root>() {}.getType();
            rootCache = gson.fromJson(reader, rootType);
            if (rootCache == null) {
                // If db.json is empty or invalid, initialize an empty structure
                rootCache = createEmptyRoot();
            }
        } catch (Exception e) {
            System.err.println("DB.load: " + e.getMessage());
            // if file missing or parse error, start fresh
            rootCache = createEmptyRoot();
        }
    }

    public static synchronized void save() {
        if (rootCache == null) {
            rootCache = createEmptyRoot();
        }
        try (Writer writer = new FileWriter(DB_PATH)) {
            gson.toJson(rootCache, writer);
        } catch (Exception e) {
            System.err.println("DB.save: " + e.getMessage());
        }
    }

    public static synchronized Root getRoot() {
        if (rootCache == null) {
            load();
        }
        return rootCache;
    }

    // Helper to create an empty Root if needed
    private static Root createEmptyRoot() {
        Root r = new Root();
        r.tenants   = new ArrayList<>();
        r.users     = new ArrayList<>();
        r.apps      = new ArrayList<>();
        r.engineers = new ArrayList<>();
        return r;
    }

}
