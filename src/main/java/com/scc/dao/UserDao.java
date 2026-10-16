package com.scc.dao;

import com.scc.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    // Maps the current row of a ResultSet to a User
    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("u_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("roles"),
                rs.getString("status"));
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u_id, name, email, password, roles, status FROM users ORDER BY u_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Could not load users: " + e.getMessage());
        }
        return users;
    }

    // Returns null if not found
    public User getUserById(int id) {
        String sql = "SELECT u_id, name, email, password, roles, status FROM users WHERE u_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Could not find user: " + e.getMessage());
        }
        return null;
    }

    // Admin-created user with an explicit status (e.g. ACTIVE, no approval needed)
    public boolean createUser(String name, String email, String password, String roles, String status) {
        String sql = "INSERT INTO users (name, password, email, roles, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, roles.toUpperCase());
            pstmt.setString(5, status.toUpperCase());
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Create user failed: " + e.getMessage());
            return false;
        }
    }

    // True if another user (any id except excludeId) already uses this email.
    // Pass -1 as excludeId when creating a new user.
    public boolean emailExists(String email, int excludeId) {
        String sql = "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?) AND u_id <> ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setInt(2, excludeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Email check failed: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUser(User u) {
        String sql = "UPDATE users SET name = ?, email = ?, password = ?, roles = ?, status = ? WHERE u_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, u.name());
            pstmt.setString(2, u.email());
            pstmt.setString(3, u.password());
            pstmt.setString(4, u.roles().toUpperCase());
            pstmt.setString(5, u.status().toUpperCase());
            pstmt.setInt(6, u.u_id());
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Update user failed: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE u_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Delete user failed: " + e.getMessage());
            return false;
        }
    }

    // Returns the matching user, or null if email/password are wrong
    public User login(String email, String password) {
        String sql = "SELECT u_id, name, email, password, roles, status FROM users " +
                "WHERE LOWER(email) = LOWER(?) AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Login failed: " + e.getMessage());
        }
        return null;
    }

    // First-admin bootstrap: if NO active admin exists yet, activate this admin account
    // so someone can log in and approve everyone else. Returns true if it was activated.
    public boolean activateIfFirstAdmin(String email) {
        String sql = "UPDATE users SET status = 'ACTIVE' " +
                "WHERE LOWER(email) = LOWER(?) AND roles = 'ADMIN' " +
                "AND NOT EXISTS (SELECT 1 FROM users WHERE roles = 'ADMIN' AND status = 'ACTIVE')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Could not activate first admin: " + e.getMessage());
            return false;
        }
    }
}