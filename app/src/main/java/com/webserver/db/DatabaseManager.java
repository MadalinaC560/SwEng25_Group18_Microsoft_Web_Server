package com.webserver.db;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:sample.db";
    private final QueryRunner queryRunner;

    public DatabaseManager() {
        this.queryRunner = new QueryRunner();
        try {
            initialiseDatabase();
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
        }
    }

    private void initialiseDatabase() throws SQLException {
        ArrayList<String> sql_statements = new ArrayList<>(Arrays.asList(
                "init_apps.sql", "init_users.sql", "init_tenants.sql"
        ));

        for (String sql_file: sql_statements) {
            String query = null;
            try {
                query = new String(Files.readAllBytes(Paths.get(sql_file)));
            } catch (IOException e) {
                System.out.println("Exception: " + e);
            }

            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                queryRunner.update(conn, query);
            } catch (SQLException e) {
                System.out.println("Exception: " + e);
            }
        }

    }

    public void newApp(String app_name, int tenant_id) throws SQLException {
        String query = "INSERT INTO apps (app_name, tenant_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            queryRunner.update(conn, query, app_name,tenant_id);
        }
    }

    public void newUser(String username, String email, String pw_hash, int tenant_id) throws SQLException {
        String query = "INSERT INTO users (username, email, pw_hash, tenant_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            queryRunner.update(conn, query, username, email, pw_hash, tenant_id);
        }
    }

    public void newTenant(String tenant_name) throws SQLException {
        String query = "INSERT INTO tenants (tenant_name) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            queryRunner.update(conn, query, tenant_name);
        }
    }

    public List<String> getAllTenantNames() throws SQLException {
        String query = "SELECT tenant_name FROM tenants";
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            return queryRunner.query(
                    conn,
                    query,
                    new ColumnListHandler<String>()  // Returns List<String> of tenant names
            );
        }
    }

    public int getTenantIdFromUserID(int userID) throws SQLException {
        String query = "SELECT tenant_id FROM users WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            Integer tenantId = queryRunner.query(
                    conn,
                    query,
                    new ScalarHandler<Integer>(),  // For getting a single value
                    userID  // Pass the parameter
            );

            if (tenantId == null) {
                throw new SQLException("No user found with id: " + userID);
            }
            return tenantId;
        }
    }

    public int getAppIdFromName(String appName) throws SQLException {
        String query = "SELECT id FROM apps WHERE app_name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            Integer tenantId = queryRunner.query(
                    conn,
                    query,
                    new ScalarHandler<Integer>(),  // For getting a single value
                    appName  // Pass the parameter
            );

            if (tenantId == null) {
                throw new SQLException("No app found with name: " + appName);
            }
            return tenantId;
        }
    }

    public int getUserIdFromName(String userName) throws SQLException {
        String query = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            Integer tenantId = queryRunner.query(
                    conn,
                    query,
                    new ScalarHandler<Integer>(),  // For getting a single value
                    userName  // Pass the parameter
            );

            if (tenantId == null) {
                throw new SQLException("No user found with name: " + userName);
            }
            return tenantId;
        }
    }


    public int getTenantIdFromName(String tenantName) throws SQLException {
        String query = "SELECT id FROM tenants WHERE tenant_name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            Integer tenantId = queryRunner.query(
                    conn,
                    query,
                    new ScalarHandler<Integer>(),  // For getting a single value
                    tenantName  // Pass the parameter
            );

            if (tenantId == null) {
                throw new SQLException("No tenant found with name: " + tenantName);
            }
            return tenantId;
        }
    }

//    public static void main(String[] args) {
//        DatabaseManager databaseManager = new DatabaseManager();
//
//        // test new_tenant
//        System.out.println("Testing uploading tenant");
//        try {
//            databaseManager.newTenant("TCD");
//        } catch (SQLException e) {
//            System.out.println("Failed creating new tenant" + e.getMessage());
//        }
//
//        try {
//            databaseManager.getAllTenantNames();
//        } catch (SQLException e) {
//            System.out.println("Failed getting all tenant names" + e.getMessage());
//        }
//    }

}
