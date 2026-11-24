package com.canteen.dao;

import com.canteen.model.Cart;
import com.canteen.model.Order;
import com.canteen.model.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Order lifecycle and analytical stats using plain JDBC.
 */
@Repository
public class OrderDAO {

    private final DataSource dataSource;

    @Autowired
    public OrderDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Creates a new Order record and returns the generated primary key.
     * @param order Order entity
     * @return generated order ID
     */
    public int createOrder(Order order) {
        String sql = "INSERT INTO orders (user_id, total_amount, status, payment_status, pickup_token) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getUserId());
            ps.setDouble(2, order.getTotalAmount());
            ps.setString(3, order.getStatus() != null ? order.getStatus() : "PENDING");
            ps.setString(4, order.getPaymentStatus() != null ? order.getPaymentStatus() : "PENDING");
            ps.setString(5, order.getPickupToken());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    order.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in createOrder: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Batch inserts OrderItem records converted from user's Cart items.
     * @param orderId ID of the parent order
     * @param cartItems List of cart items to add to order
     */
    public void addOrderItems(int orderId, List<Cart> cartItems) {
        String sql = "INSERT INTO order_items (order_id, food_item_id, quantity, price, item_type) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Cart item : cartItems) {
                ps.setInt(1, orderId);
                ps.setInt(2, item.getFoodItemId());
                ps.setInt(3, item.getQuantity());
                ps.setDouble(4, item.getPrice());
                ps.setString(5, item.getItemType());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Database error in addOrderItems: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all orders for a specific student, complete with item lines.
     * @param userId Student user ID
     * @return List of Order objects
     */
    public List<Order> findByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.id, o.user_id, u.name AS user_name, o.total_amount, o.status, " +
                     "o.payment_status, o.pickup_token, o.created_at " +
                     "FROM orders o " +
                     "JOIN users u ON o.user_id = u.id " +
                     "WHERE o.user_id = ? " +
                     "ORDER BY o.id DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapOrderRow(rs);
                    order.setItems(findItemsByOrderId(conn, order.getId()));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in findByUserId for orders: " + e.getMessage(), e);
        }
        return orders;
    }

    /**
     * Retrieves an order by ID, including its associated order items.
     * @param id Order ID
     * @return Order object or null if not found
     */
    public Order findById(int id) {
        String sql = "SELECT o.id, o.user_id, u.name AS user_name, o.total_amount, o.status, " +
                     "o.payment_status, o.pickup_token, o.created_at " +
                     "FROM orders o " +
                     "JOIN users u ON o.user_id = u.id " +
                     "WHERE o.id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapOrderRow(rs);
                    order.setItems(findItemsByOrderId(conn, order.getId()));
                    return order;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in findById for order: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Retrieves all orders for administrative view.
     * @return List of all Order objects
     */
    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.id, o.user_id, u.name AS user_name, o.total_amount, o.status, " +
                     "o.payment_status, o.pickup_token, o.created_at " +
                     "FROM orders o " +
                     "JOIN users u ON o.user_id = u.id " +
                     "ORDER BY o.id DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Order order = mapOrderRow(rs);
                order.setItems(findItemsByOrderId(conn, order.getId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in findAll orders: " + e.getMessage(), e);
        }
        return orders;
    }

    /**
     * Updates an order's status (PENDING/CONFIRMED/PREPARING/READY/DELIVERED/CANCELLED).
     * @param orderId Order ID
     * @param status New order status
     */
    public void updateStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error in updateStatus: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an order's payment status (PENDING/PAID/FAILED).
     * @param orderId Order ID
     * @param paymentStatus New payment status
     */
    public void updatePaymentStatus(int orderId, String paymentStatus) {
        String sql = "UPDATE orders SET payment_status = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentStatus);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error in updatePaymentStatus: " + e.getMessage(), e);
        }
    }

    /**
     * Computes dashboard statistics for administrator overview.
     * @return Map containing KPI metrics
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            // Total Orders and Total Revenue
            String sqlRevenue = "SELECT COUNT(*) AS total_orders, " +
                                "COALESCE(SUM(CASE WHEN payment_status = 'PAID' OR status != 'CANCELLED' THEN total_amount ELSE 0 END), 0) AS total_revenue " +
                                "FROM orders";
            try (PreparedStatement ps = conn.prepareStatement(sqlRevenue);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalOrders", rs.getInt("total_orders"));
                    stats.put("totalRevenue", Math.round(rs.getDouble("total_revenue") * 100.0) / 100.0);
                }
            }

            // Pending Orders
            String sqlPending = "SELECT COUNT(*) FROM orders WHERE status = 'PENDING'";
            try (PreparedStatement ps = conn.prepareStatement(sqlPending);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("pendingOrders", rs.getInt(1));
                }
            }

            // Preparing Orders
            String sqlPreparing = "SELECT COUNT(*) FROM orders WHERE status = 'PREPARING'";
            try (PreparedStatement ps = conn.prepareStatement(sqlPreparing);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("preparingOrders", rs.getInt(1));
                }
            }

            // Delivered Orders
            String sqlDelivered = "SELECT COUNT(*) FROM orders WHERE status = 'DELIVERED'";
            try (PreparedStatement ps = conn.prepareStatement(sqlDelivered);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("deliveredOrders", rs.getInt(1));
                }
            }

            // Registered Students
            String sqlUsers = "SELECT COUNT(*) FROM users WHERE role = 'STUDENT'";
            try (PreparedStatement ps = conn.prepareStatement(sqlUsers);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalStudents", rs.getInt(1));
                }
            }

            // Total Food Items
            String sqlItems = "SELECT COUNT(*) FROM food_items";
            try (PreparedStatement ps = conn.prepareStatement(sqlItems);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalFoodItems", rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database error in getDashboardStats: " + e.getMessage(), e);
        }
        return stats;
    }

    /**
     * Helper to read OrderItem entities belonging to a given order ID using an existing connection.
     */
    private List<OrderItem> findItemsByOrderId(Connection conn, int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.id, oi.order_id, oi.food_item_id, f.name AS food_item_name, " +
                     "oi.quantity, oi.price, oi.item_type " +
                     "FROM order_items oi " +
                     "LEFT JOIN food_items f ON oi.food_item_id = f.id " +
                     "WHERE oi.order_id = ? " +
                     "ORDER BY oi.id ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem(
                            rs.getInt("id"),
                            rs.getInt("order_id"),
                            rs.getInt("food_item_id"),
                            rs.getString("food_item_name") != null ? rs.getString("food_item_name") : "Item #" + rs.getInt("food_item_id"),
                            rs.getInt("quantity"),
                            rs.getDouble("price"),
                            rs.getString("item_type")
                    );
                    items.add(item);
                }
            }
        }
        return items;
    }

    /**
     * Helper to construct Order entity from ResultSet row.
     */
    private Order mapOrderRow(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setUserId(rs.getInt("user_id"));
        order.setUserName(rs.getString("user_name"));
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setStatus(rs.getString("status"));
        order.setPaymentStatus(rs.getString("payment_status"));
        order.setPickupToken(rs.getString("pickup_token"));
        order.setCreatedAt(rs.getString("created_at"));
        return order;
    }
}
