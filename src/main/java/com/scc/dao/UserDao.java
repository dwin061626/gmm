package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;

public class UserDao {

    public boolean registerUser(String name, String email, String password, String roles) {

        String sql = "INSERT INTO users(name, password, email, roles, status) VALUES(?, ?, ?, ?,'PENDING')";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, name);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, roles.toUpperCase());
            pstmt.executeUpdate();
            System.out.print("-> Successful Registration, Waiting for Approval!");
            return true;

        } catch(SQLException e) {
        System.err.println("Failed: "+ e.getMessage());
        return false;

        }
    }

}