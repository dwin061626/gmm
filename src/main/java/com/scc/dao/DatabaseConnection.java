package com.scc.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DB_NAME = "GymMEMBERSHIPDatabase.db";
    private static final File DB_FILE = findDatabase();
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE.getAbsolutePath();

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // Looks for GymMembershipDatabase.db in the folder the program started in,
    // then in its parent folders, then one level inside each of those folders.
    // This way it still finds your database even if IntelliJ starts the program
    // from a different working directory.
    private static File findDatabase() {
        File dir = new File(System.getProperty("user.dir")).getAbsoluteFile();
        File start = dir;

        for (int level = 0; level < 6 && dir != null; level++) {
            File direct = new File(dir, DB_NAME);
            if (direct.exists()) return direct;

            File[] children = dir.listFiles(File::isDirectory);
            if (children != null) {
                for (File child : children) {
                    File inside = new File(child, DB_NAME);
                    if (inside.exists()) return inside;
                }
            }
            dir = dir.getParentFile();
        }

        // Not found anywhere: fall back to the start folder (SQLite will create a new file there)
        return new File(start, DB_NAME);
    }

    // Call once at startup. Shows which file is used and makes sure the users table exists.
    public static void initialize() {
        System.out.println("Using database: " + DB_FILE.getAbsolutePath());
        if (!DB_FILE.exists()) {
            System.out.println("(Database file not found - a new empty one will be created.)");
        }

        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "u_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE, " +
                "email TEXT NOT NULL UNIQUE, " +
                "password TEXT NOT NULL, " +
                "roles TEXT NOT NULL, " +
                "status TEXT NOT NULL)";

        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.err.println("Could not initialize database: " + e.getMessage());
        }
    }
}