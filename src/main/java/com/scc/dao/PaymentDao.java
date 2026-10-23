package com.scc.dao;

import com.scc.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PaymentDao {

    public record Payment(int subId, int uId, String name, String plan,
                          double fee, double paid, String payDate, String expDate) {
    }

    // Summary of payments: how many and total income
    public record Summary(int count, double total) {
    }

    /**
     * Saves the payment AND activates the member in one transaction,
     * so you never get a payment without activation (or vice versa).
     * Returns the saved Payment, or null on failure.
     */
    public Payment processPayment(User member, String plan, double fee, double paid) {
        return processPayment(member, plan, fee, paid, "ACTIVE");
    }

    /**
     * Same as above, but lets the caller choose the member's new status.
     */
    public Payment processPayment(User member, String plan, double fee, double paid, String newStatus) {
        LocalDate now = LocalDate.now();
        String payDate = now.toString();                 // yyyy-MM-dd
        String expDate = now.plusDays(30).toString();    // valid for 30 days

        String insertSql = "INSERT INTO payments " +
                "(u_id, name, plan, fee, amount_paid, payment_date, expiry_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String activateSql = "UPDATE users SET status = ? WHERE u_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ins = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement act = conn.prepareStatement(activateSql)) {

                ins.setInt(1, member.u_id());
                ins.setString(2, member.name());
                ins.setString(3, plan);
                ins.setDouble(4, fee);
                ins.setDouble(5, paid);
                ins.setString(6, payDate);
                ins.setString(7, expDate);
                ins.executeUpdate();

                int subId = 0;
                try (ResultSet keys = ins.getGeneratedKeys()) {
                    if (keys.next()) subId = keys.getInt(1);
                }

                act.setString(1, newStatus);
                act.setInt(2, member.u_id());
                act.executeUpdate();

                conn.commit();
                return new Payment(subId, member.u_id(), member.name(), plan, fee, paid, payDate, expDate);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Payment failed: " + e.getMessage());
            return null;
        }
    }

    public List<Payment> getAllPayments() {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT sub_id, u_id, name, plan, fee, amount_paid, payment_date, expiry_date " +
                "FROM payments ORDER BY sub_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Payment(
                        rs.getInt("sub_id"), rs.getInt("u_id"),
                        rs.getString("name"), rs.getString("plan"),
                        rs.getDouble("fee"), rs.getDouble("amount_paid"),
                        rs.getString("payment_date"), rs.getString("expiry_date")));
            }
        } catch (SQLException e) {
            System.err.println("Could not load payments: " + e.getMessage());
        }
        return list;
    }

    // Income = fee (change given back is not income)
    public Summary getSummaryForDate(String date) {
        return querySummary("SELECT COUNT(*), COALESCE(SUM(fee), 0) FROM payments WHERE payment_date = ?", date);
    }

    // month format: yyyy-MM
    public Summary getSummaryForMonth(String month) {
        return querySummary("SELECT COUNT(*), COALESCE(SUM(fee), 0) FROM payments WHERE payment_date LIKE ?",
                month + "%");
    }

    private Summary querySummary(String sql, String param) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, param);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return new Summary(rs.getInt(1), rs.getDouble(2));
            }
        } catch (SQLException e) {
            System.err.println("Could not load summary: " + e.getMessage());
        }
        return new Summary(0, 0);
    }
}