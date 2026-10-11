
package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserDao {

    public boolean registerUser(String name, String email, String password, String roles) {

        String sql = "INSERT INTO users (name, password, email, roles, status) " +
                "VALUES (?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, roles.toUpperCase());

            int result = pstmt.executeUpdate();

            if (result > 0) {
                System.out.println("Successful Registration, Waiting for Approval!");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Registration failed: " + e.getMessage());
        }

        return false;
    }
}
