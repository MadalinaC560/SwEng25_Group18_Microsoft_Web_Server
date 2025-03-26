package com.webserver.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Example structure
public class DB {

    private static final String DB_PATH = "db.json";
    private static final Gson gson = new Gson();
    private static Root rootCache; // holds all data in memory

    // e.g. top-level structure
    public static class Root {
        public List<User> users;
    }

    public static class User {
        public int userId;
        public String username;
        public List<App> apps;
    }

    public static class App {
        public int appId;
        public String name;
        public String runtime;
        public String status;  // "running" or "stopped"
        public List<String> routes;
    }

    // 1) load from disk -> rootCache
    public static synchronized void load() {
        try (Reader reader = new FileReader(DB_PATH)) {
            Type rootType = new TypeToken<Root>(){}.getType();
            rootCache = gson.fromJson(reader, rootType);
        } catch (Exception e) {
            System.err.println("DB.load: " + e.getMessage());
            rootCache = new Root();
            rootCache.users = new ArrayList<>();
        }
    }

    // 2) save rootCache -> disk
    public static synchronized void save() {
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
}
