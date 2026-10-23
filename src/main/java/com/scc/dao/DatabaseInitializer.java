package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private static final String USERS_TABLE =
            "CREATE TABLE IF NOT EXISTS users (" +
                    "u_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL UNIQUE, " +
                    "email TEXT NOT NULL UNIQUE, " +
                    "password TEXT NOT NULL, " +
                    "roles TEXT NOT NULL, " +
                    "status TEXT NOT NULL)";

    private static final String PAYMENTS_TABLE =
            "CREATE TABLE IF NOT EXISTS payments (" +
                    "sub_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "u_id INTEGER NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "plan TEXT NOT NULL, " +
                    "fee REAL NOT NULL, " +
                    "amount_paid REAL NOT NULL, " +
                    "payment_date TEXT NOT NULL, " +
                    "expiry_date TEXT NOT NULL, " +
                    "FOREIGN KEY (u_id) REFERENCES users(u_id) ON DELETE CASCADE)";

    // Call once at startup, before any DAO is used. Order matters: users first, then payments.
    public static void initialize() {
        System.out.println("Using database: " + DatabaseConnection.getDatabaseFile().getAbsolutePath());
        if (!DatabaseConnection.getDatabaseFile().exists()) {
            System.out.println("(Database file not found - a new empty one will be created.)");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(USERS_TABLE);
            st.execute(PAYMENTS_TABLE);
        } catch (SQLException e) {
            System.err.println("Could not initialize database: " + e.getMessage());
        }
    }
}