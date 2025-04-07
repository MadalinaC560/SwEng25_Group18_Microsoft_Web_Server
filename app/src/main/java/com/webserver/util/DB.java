
package com.webserver.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DB.java: Row-level CRUD version, removing the destructive "TRUNCATE TABLE" approach.
 *
 */
public class DB {

    private static Root rootCache;  // Optional in-memory cache, if we still want it
    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;

    private static final Gson gson = new Gson();
    public static synchronized List<App> listAllApps() {
        loadDbConfig(); // ensure we have connection info
        List<App> results = new ArrayList<>();

        String sql = "SELECT appId, tenantId, name, runtime, status, ownerUserId, routesJson FROM Apps";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                App a = new App();
                a.appId       = rs.getInt("appId");
                a.tenantId    = rs.getInt("tenantId");
                a.name        = rs.getString("name");
                a.runtime     = rs.getString("runtime");
                a.status      = rs.getString("status");
                a.ownerUserId = rs.getInt("ownerUserId");

                String routesJson = rs.getString("routesJson");
                if (routesJson != null && !routesJson.isEmpty()) {
                    a.routes = gson.fromJson(routesJson, new TypeToken<List<String>>(){}.getType());
                } else {
                    a.routes = new ArrayList<>();
                }
                results.add(a);
            }
        } catch (Exception e) {
            System.err.println("listAllApps error: " + e.getMessage());
        }
        return results;
    }

    public static synchronized List<App> listAppsForTenant(int tenantId) {
        loadDbConfig();
        List<App> results = new ArrayList<>();

        String sql = "SELECT appId, tenantId, name, runtime, status, ownerUserId, routesJson "
                   + "FROM Apps WHERE tenantId = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    App a = new App();
                    a.appId       = rs.getInt("appId");
                    a.tenantId    = rs.getInt("tenantId");
                    a.name        = rs.getString("name");
                    a.runtime     = rs.getString("runtime");
                    a.status      = rs.getString("status");
                    a.ownerUserId = rs.getInt("ownerUserId");

                    String routesJson = rs.getString("routesJson");
                    if (routesJson != null && !routesJson.isEmpty()) {
                        a.routes = gson.fromJson(routesJson, new TypeToken<List<String>>(){}.getType());
                    } else {
                        a.routes = new ArrayList<>();
                    }
                    results.add(a);
                }
            }
        } catch (Exception e) {
            System.err.println("listAppsForTenant error: " + e.getMessage());
        }
        return results;
    }

    public static synchronized App findAppByTenant(int tenantId, int appId) {
        loadDbConfig();
        App a = null;

        String sql = "SELECT appId, tenantId, name, runtime, status, ownerUserId, routesJson "
                   + "FROM Apps WHERE tenantId = ? AND appId = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tenantId);
            ps.setInt(2, appId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    a = new App();
                    a.appId       = rs.getInt("appId");
                    a.tenantId    = rs.getInt("tenantId");
                    a.name        = rs.getString("name");
                    a.runtime     = rs.getString("runtime");
                    a.status      = rs.getString("status");
                    a.ownerUserId = rs.getInt("ownerUserId");

                    String routesJson = rs.getString("routesJson");
                    if (routesJson != null && !routesJson.isEmpty()) {
                        a.routes = gson.fromJson(routesJson, new TypeToken<List<String>>(){}.getType());
                    } else {
                        a.routes = new ArrayList<>();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("findAppByTenant error: " + e.getMessage());
        }
        return a;
    }

    public static synchronized App findAppById(int appId) {
        loadDbConfig();
        App a = null;

        String sql = "SELECT appId, tenantId, name, runtime, status, ownerUserId, routesJson "
                   + "FROM Apps WHERE appId = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    a = new App();
                    a.appId       = rs.getInt("appId");
                    a.tenantId    = rs.getInt("tenantId");
                    a.name        = rs.getString("name");
                    a.runtime     = rs.getString("runtime");
                    a.status      = rs.getString("status");
                    a.ownerUserId = rs.getInt("ownerUserId");

                    String routesJson = rs.getString("routesJson");
                    if (routesJson != null && !routesJson.isEmpty()) {
                        a.routes = gson.fromJson(routesJson, new TypeToken<List<String>>(){}.getType());
                    } else {
                        a.routes = new ArrayList<>();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("findAppById error: " + e.getMessage());
        }
        return a;
    }

    public static synchronized List<User> listAllUsers() {
    loadDbConfig();
    List<User> results = new ArrayList<>();

    String sql = "SELECT userId, tenantId, username, role, passwordHash FROM Users";
    try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
         Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery(sql)) {

        while (rs.next()) {
            User u = new User();
            u.userId       = rs.getInt("userId");
            u.tenantId     = rs.getInt("tenantId");
            u.username     = rs.getString("username");
            u.role         = rs.getString("role");
            u.passwordHash = rs.getString("passwordHash");
            results.add(u);
        }
    } catch (Exception e) {
        System.err.println("listAllUsers error: " + e.getMessage());
    }
    return results;
}

    public static synchronized List<User> findUsersByTenantId(int tenantId) {
    loadDbConfig();
    List<User> results = new ArrayList<>();

    String sql = "SELECT userId, tenantId, username, role, passwordHash FROM Users WHERE tenantId = ?";
    try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, tenantId);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User u = new User();
                u.userId       = rs.getInt("userId");
                u.tenantId     = rs.getInt("tenantId");
                u.username     = rs.getString("username");
                u.role         = rs.getString("role");
                u.passwordHash = rs.getString("passwordHash");
                results.add(u);
            }
        }
    } catch (Exception e) {
        System.err.println("findUsersByTenantId error: " + e.getMessage());
    }
    return results;
}


    /**
     * This is our in-memory model. We still have it in case
     * parts of the code (ConnectionHandler, etc.) call DB.getRoot().
     */
    public static class Root {
        public List<Tenant> tenants;
        public List<User> users;
        public List<App>   apps;
        public List<Engineer> engineers;
    }

    public static class Tenant {
        public int tenantId;
        public String tenantName;
        public String tenantEmail;
    }

    public static class User {
        public int userId;
        public int tenantId;
        public String username;
        public String role;

        // we keep the passwordHash in memory so that we don't lose it on save.
        public String passwordHash;
    }

    public static class App {
        public int appId;
        public int tenantId;
        public String name;
        public String runtime;
        public String status;
        public int ownerUserId;
        public List<String> routes;
    }

    public static class Engineer {
        public int engineerId;
        public String username;
        public String role;
    }

    // ------------------------------------------------------------------------------------
    //  AUTH HELPERS
    // ------------------------------------------------------------------------------------
    public static synchronized User findUserByUsername(String username) {
        loadDbConfig(); // ensure we have DB credentials

        User found = null;
        String sql = "SELECT userId, tenantId, username, role, passwordHash FROM Users WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    found = new User();
                    found.userId       = rs.getInt("userId");
                    found.tenantId     = rs.getInt("tenantId");
                    found.username     = rs.getString("username");
                    found.role         = rs.getString("role");
                    found.passwordHash = rs.getString("passwordHash");
                }
            }
        } catch (Exception e) {
            System.err.println("findUserByUsername error: " + e.getMessage());
        }

        return found;
    }

    public static synchronized Tenant findTenantByEmail(String tenantEmail) {
        loadDbConfig();

        Tenant tenant = null;
        // Suppose your Tenants table has a 'tenantEmail' column
        String sql = "SELECT tenantId, tenantName FROM Tenants WHERE tenantEmail = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tenantEmail);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tenant = new Tenant();
                    tenant.tenantId   = rs.getInt("tenantId");
                    tenant.tenantName = rs.getString("tenantName");
                }
            }
        } catch (Exception e) {
            System.err.println("findTenantByEmail error: " + e.getMessage());
        }
        return tenant;
    }

    public static boolean checkPassword(String plainText, String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        String hashedInput = hashPassword(plainText);
        return hashedInput.equalsIgnoreCase(storedHash);
    }

    public static String hashPassword(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }


    // ------------------------------------------------------------------------------------
    //  TENANT CRUD
    // ------------------------------------------------------------------------------------

    public static synchronized Tenant createTenant(Tenant t) {
    loadDbConfig();
    if (t == null) return null;

    // Update SQL to include tenantEmail
    String sql = "INSERT INTO Tenants (tenantId, tenantName, tenantEmail) VALUES (?, ?, ?)";

    try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, t.tenantId);
        ps.setString(2, t.tenantName);
        ps.setString(3, t.tenantEmail); // Add this line
        ps.executeUpdate();

        // Also add to in-memory
        if (rootCache != null) {
            rootCache.tenants.add(t);
        }

        return t;
    } catch (Exception e) {
        System.err.println("createTenant error: " + e.getMessage());
        return null;
    }
}

    public static synchronized Tenant updateTenant(Tenant t) {
        loadDbConfig();
        if (t == null) return null;

        String sql = "UPDATE Tenants SET tenantName = ? WHERE tenantId = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.tenantName);
            ps.setInt(2, t.tenantId);
            ps.executeUpdate();

            // Also update in memory
            if (rootCache != null) {
                for (Tenant x : rootCache.tenants) {
                    if (x.tenantId == t.tenantId) {
                        x.tenantName = t.tenantName;
                        break;
                    }
                }
            }

            return t;
        } catch (Exception e) {
            System.err.println("updateTenant error: " + e.getMessage());
            return null;
        }
    }

    public static synchronized boolean deleteTenant(int tenantId) {
        loadDbConfig();

        String sql = "DELETE FROM Tenants WHERE tenantId = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tenantId);
            ps.executeUpdate();

            if (rootCache != null) {
                rootCache.tenants.removeIf(t -> t.tenantId == tenantId);
            }
            return true;
        } catch (Exception e) {
            System.err.println("deleteTenant error: " + e.getMessage());
            return false;
        }
    }

    public static synchronized List<Tenant> listAllTenants() {
    List<Tenant> results = new ArrayList<>();
    loadDbConfig();
    String sql = "SELECT tenantId, tenantName FROM Tenants";
    try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
         Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        while (rs.next()) {
            Tenant t = new Tenant();
            t.tenantId   = rs.getInt("tenantId");
            t.tenantName = rs.getString("tenantName");
            results.add(t);
        }
    } catch (Exception e) {
        System.err.println("listAllTenants error: " + e.getMessage());
    }
    return results;
}

    public static synchronized Tenant findTenantById(int tenantId) {
        loadDbConfig();
        Tenant t = null;
        String sql = "SELECT tenantId, tenantName FROM Tenants WHERE tenantId = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    t = new Tenant();
                    t.tenantId   = rs.getInt("tenantId");
                    t.tenantName = rs.getString("tenantName");
                }
            }
        } catch (Exception e) {
            System.err.println("findTenantById error: " + e.getMessage());
        }
        return t;
    }

// Similarly for Apps, Users, etc.


    // ------------------------------------------------------------------------------------
    //  USER CRUD
    // ------------------------------------------------------------------------------------
    public static synchronized User createUser(User u) {
        loadDbConfig();
        if (u == null) return null;

        // Insert includes the passwordHash now
        String sql = "INSERT INTO Users (userId, tenantId, username, role, passwordHash) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, u.userId);
            ps.setInt(2, u.tenantId);
            ps.setString(3, u.username);
            ps.setString(4, u.role);
            ps.setString(5, u.passwordHash);

            ps.executeUpdate();

            // Also add to in-memory
            if (rootCache != null) {
                rootCache.users.add(u);
            }
            return u;
        } catch (Exception e) {
            System.err.println("createUser error: " + e.getMessage());
            return null;
        }
    }

    public static synchronized User updateUser(User u) {
        loadDbConfig();
        if (u == null) return null;

        String sql = "UPDATE Users SET tenantId = ?, username = ?, role = ?, passwordHash = ? "
                   + "WHERE userId = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, u.tenantId);
            ps.setString(2, u.username);
            ps.setString(3, u.role);
            ps.setString(4, u.passwordHash);
            ps.setInt(5, u.userId);
            ps.executeUpdate();

            // Update in-memory
            if (rootCache != null) {
                for (User x : rootCache.users) {
                    if (x.userId == u.userId) {
                        x.tenantId     = u.tenantId;
                        x.username     = u.username;
                        x.role         = u.role;
                        x.passwordHash = u.passwordHash;
                        break;
                    }
                }
            }
            return u;
        } catch (Exception e) {
            System.err.println("updateUser error: " + e.getMessage());
            return null;
        }
    }

    public static synchronized boolean deleteUser(int userId) {
        loadDbConfig();

        String sql = "DELETE FROM Users WHERE userId = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();

            if (rootCache != null) {
                rootCache.users.removeIf(u -> u.userId == userId);
            }
            return true;
        } catch (Exception e) {
            System.err.println("deleteUser error: " + e.getMessage());
            return false;
        }
    }

    // ------------------------------------------------------------------------------------
    //  APP CRUD
    // ------------------------------------------------------------------------------------
    public static synchronized App createApp(App a) {
        loadDbConfig();
        if (a == null) return null;

        String sql = "INSERT INTO Apps (appId, tenantId, name, runtime, status, ownerUserId, routesJson) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, a.appId);
            ps.setInt(2, a.tenantId);
            ps.setString(3, a.name);
            ps.setString(4, a.runtime);
            ps.setString(5, a.status);
            ps.setInt(6, a.ownerUserId);
            ps.setString(7, gson.toJson(a.routes));

            ps.executeUpdate();

            // Also add to in-memory
            if (rootCache != null) {
                rootCache.apps.add(a);
            }
            return a;
        } catch (Exception e) {
            System.err.println("createApp error: " + e.getMessage());
            return null;
        }
    }

    public static synchronized App updateApp(App a) {
        loadDbConfig();
        if (a == null) return null;

        String sql = "UPDATE Apps SET tenantId = ?, name = ?, runtime = ?, status = ?, ownerUserId = ?, routesJson = ? "
                   + "WHERE appId = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, a.tenantId);
            ps.setString(2, a.name);
            ps.setString(3, a.runtime);
            ps.setString(4, a.status);
            ps.setInt(5, a.ownerUserId);
            ps.setString(6, gson.toJson(a.routes));
            ps.setInt(7, a.appId);
            ps.executeUpdate();

            // Update in-memory
            if (rootCache != null) {
                for (App x : rootCache.apps) {
                    if (x.appId == a.appId) {
                        x.tenantId    = a.tenantId;
                        x.name        = a.name;
                        x.runtime     = a.runtime;
                        x.status      = a.status;
                        x.ownerUserId = a.ownerUserId;
                        x.routes      = a.routes; // same list or newly replaced
                        break;
                    }
                }
            }
            return a;
        } catch (Exception e) {
            System.err.println("updateApp error: " + e.getMessage());
            return null;
        }
    }

    public static synchronized boolean deleteApp(int appId) {
        loadDbConfig();

        String sql = "DELETE FROM Apps WHERE appId = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appId);
            ps.executeUpdate();

            if (rootCache != null) {
                rootCache.apps.removeIf(a -> a.appId == appId);
            }
            return true;
        } catch (Exception e) {
            System.err.println("deleteApp error: " + e.getMessage());
            return false;
        }
    }

    // ------------------------------------------------------------------------------------
    //  ENGINEER CRUD
    // ------------------------------------------------------------------------------------
    public static synchronized Engineer createEngineer(Engineer e) {
        loadDbConfig();
        if (e == null) return null;

        String sql = "INSERT INTO Engineers (engineerId, username, role) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, e.engineerId);
            ps.setString(2, e.username);
            ps.setString(3, e.role);
            ps.executeUpdate();

            if (rootCache != null) {
                rootCache.engineers.add(e);
            }
            return e;
        } catch (Exception ex) {
            System.err.println("createEngineer error: " + ex.getMessage());
            return null;
        }
    }

    public static synchronized Engineer updateEngineer(Engineer e) {
        loadDbConfig();
        if (e == null) return null;

        String sql = "UPDATE Engineers SET username = ?, role = ? WHERE engineerId = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, e.username);
            ps.setString(2, e.role);
            ps.setInt(3, e.engineerId);
            ps.executeUpdate();

            if (rootCache != null) {
                for (Engineer x : rootCache.engineers) {
                    if (x.engineerId == e.engineerId) {
                        x.username = e.username;
                        x.role     = e.role;
                        break;
                    }
                }
            }
            return e;
        } catch (Exception ex) {
            System.err.println("updateEngineer error: " + ex.getMessage());
            return null;
        }
    }

    public static synchronized boolean deleteEngineer(int engineerId) {
        loadDbConfig();

        String sql = "DELETE FROM Engineers WHERE engineerId = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, engineerId);
            ps.executeUpdate();

            if (rootCache != null) {
                rootCache.engineers.removeIf(en -> en.engineerId == engineerId);
            }
            return true;
        } catch (Exception ex) {
            System.err.println("deleteEngineer error: " + ex.getMessage());
            return false;
        }
    }

    // ------------------------------------------------------------------------------------
    //  LOAD / GETROOT
    // ------------------------------------------------------------------------------------
    /**
     * We keep a load() method that reads from the DB and populates rootCache.
     * This is done once at startup or whenever you need to refresh in-memory.
     */
    public static synchronized void load() {
        System.out.println("Loading DB with new row-level approach...");
        loadDbConfig();

        rootCache = new Root();
        rootCache.tenants   = new ArrayList<>();
        rootCache.users     = new ArrayList<>();
        rootCache.apps      = new ArrayList<>();
        rootCache.engineers = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            // Tenants
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT tenantId, tenantName FROM Tenants")) {
                while (rs.next()) {
                    Tenant t = new Tenant();
                    t.tenantId   = rs.getInt("tenantId");
                    t.tenantName = rs.getString("tenantName");
                    rootCache.tenants.add(t);
                }
            }

            // Users, now including passwordHash
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT userId, tenantId, username, role, passwordHash FROM Users")
            ) {
                while (rs.next()) {
                    User u = new User();
                    u.userId       = rs.getInt("userId");
                    u.tenantId     = rs.getInt("tenantId");
                    u.username     = rs.getString("username");
                    u.role         = rs.getString("role");
                    u.passwordHash = rs.getString("passwordHash");
                    rootCache.users.add(u);
                }
            }

            // Apps
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT appId, tenantId, name, runtime, status, ownerUserId, routesJson FROM Apps")
            ) {
                while (rs.next()) {
                    App a = new App();
                    a.appId       = rs.getInt("appId");
                    a.tenantId    = rs.getInt("tenantId");
                    a.name        = rs.getString("name");
                    a.runtime     = rs.getString("runtime");
                    a.status      = rs.getString("status");
                    a.ownerUserId = rs.getInt("ownerUserId");

                    String routesJson = rs.getString("routesJson");
                    if (routesJson != null && !routesJson.isEmpty()) {
                        a.routes = gson.fromJson(routesJson, new TypeToken<List<String>>(){}.getType());
                    } else {
                        a.routes = new ArrayList<>();
                    }

                    rootCache.apps.add(a);
                }
            }

            // Engineers
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT engineerId, username, role FROM Engineers")
            ) {
                while (rs.next()) {
                    Engineer e = new Engineer();
                    e.engineerId = rs.getInt("engineerId");
                    e.username   = rs.getString("username");
                    e.role       = rs.getString("role");
                    rootCache.engineers.add(e);
                }
            }

            System.out.println("DB.load: successfully loaded from Azure SQL.");

        } catch (Exception e) {
            System.err.println("DB.load error: " + e.getMessage());
        }
    }

    public static synchronized Root getRoot() {
        if (rootCache == null) {
            load();
        }
        return rootCache;
    }

    // ------------------------------------------------------------------------------------
    //  SAVE (NOW A NO-OP)
    // ------------------------------------------------------------------------------------
    /**
     * Legacy method which used to TRUNCATE all tables and re-insert from memory.
     * We keep it so we don't break older code, but it no longer does destructive writes.
     * Instead, we recommend you use createX/updateX/deleteX for row-level changes.
     */
    public static synchronized void save() {
        System.out.println("DB.save() is now a NO-OP. Use row-level CRUD instead.");
        // If you wanted to sync in-memory changes to DB, you could do it here,
        // but the old destructive approach is removed.
    }

    // ------------------------------------------------------------------------------------
    //  HELPER: LOAD DB CONFIG
    // ------------------------------------------------------------------------------------
    private static void loadDbConfig() {
        if (dbUrl != null) {
            return; // already loaded
        }
        ConfigLoader config = new ConfigLoader(); //
        dbUrl      = config.get("db.url");
        dbUser     = config.get("db.user");
        dbPassword = config.get("db.password");

        System.out.println("DB config loaded: " + dbUrl);
    }
}
