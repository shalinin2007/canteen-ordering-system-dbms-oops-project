package com.canteen.dao;

import com.canteen.model.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data Access Object for Payment processing and transaction records using plain JDBC.
 */
@Repository
public class PaymentDAO {

    private final DataSource dataSource;

    @Autowired
    public PaymentDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Inserts a new Payment record and returns the generated key.
     * @param payment Payment entity
     * @return generated payment ID
     */
    public int save(Payment payment) {
        String sql = "INSERT INTO payments (order_id, amount, payment_method, transaction_id, status, upi_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, payment.getOrderId());
            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "UPI");
            ps.setString(4, payment.getTransactionId());
            ps.setString(5, payment.getStatus() != null ? payment.getStatus() : "PENDING");
            ps.setString(6, payment.getUpiId() != null ? payment.getUpiId() : "canteen@sbi");

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    payment.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in save payment: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Finds the latest payment record for a given order ID.
     * @param orderId Order ID
     * @return Payment entity or null
     */
    public Payment findByOrderId(int orderId) {
        String sql = "SELECT id, order_id, amount, payment_method, transaction_id, status, upi_id, created_at " +
                     "FROM payments WHERE order_id = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Payment(
                            rs.getInt("id"),
                            rs.getInt("order_id"),
                            rs.getDouble("amount"),
                            rs.getString("payment_method"),
                            rs.getString("transaction_id"),
                            rs.getString("status"),
                            rs.getString("upi_id"),
                            rs.getString("created_at")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in findByOrderId for payment: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Updates payment status and transaction reference ID.
     * @param paymentId Payment ID
     * @param status New status (SUCCESS/FAILED)
     * @param transactionId Payment transaction ID
     */
    public void updateStatus(int paymentId, String status, String transactionId) {
        String sql = "UPDATE payments SET status = ?, transaction_id = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, transactionId);
            ps.setInt(3, paymentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error in updateStatus for payment: " + e.getMessage(), e);
        }
    }
}
