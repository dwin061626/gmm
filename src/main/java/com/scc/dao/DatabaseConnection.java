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
        Connection conn = DriverManager.getConnection(DB_URL);
        // SQLite ignores foreign keys unless this is turned on for EACH connection
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    public static File getDatabaseFile() {
        return DB_FILE;
    }

    // Looks for the database in the folder the program started in,
    // then in its parent folders, then one level inside each of those folders.
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
        return new File(start, DB_NAME);
    }
}