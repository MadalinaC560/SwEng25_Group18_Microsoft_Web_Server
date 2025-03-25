package com.webserver.db;

import org.apache.commons.dbutils.QueryRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

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

    public void newTenant(String tenant_name) throws SQLException {
        String query = "INSERT INTO tenants (tenant_name) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            queryRunner.update(conn, query, tenant_name);
        }
    }

    public static void main(String[] args) {
        DatabaseManager databaseManager = new DatabaseManager();

        // test new_tenant
        try {
            databaseManager.newTenant("TCD");
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
        }

    }
}
