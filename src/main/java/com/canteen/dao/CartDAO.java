package com.canteen.dao;

import com.canteen.model.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for shopping cart operations using plain JDBC.
 */
@Repository
public class CartDAO {

    private final DataSource dataSource;

    @Autowired
    public CartDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Retrieves all items in the user's cart by joining with food_items table.
     * @param userId ID of the student
     * @return List of Cart items
     */
    public List<Cart> findByUserId(int userId) {
        List<Cart> cartItems = new ArrayList<>();
        String sql = "SELECT c.id, c.user_id, c.food_item_id, f.name AS food_item_name, f.price, " +
                     "c.quantity, f.item_type, f.image_url " +
                     "FROM cart c " +
                     "JOIN food_items f ON c.food_item_id = f.id " +
                     "WHERE c.user_id = ? " +
                     "ORDER BY c.id ASC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cart cart = new Cart(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getInt("food_item_id"),
                            rs.getString("food_item_name"),
                            rs.getDouble("price"),
                            rs.getInt("quantity"),
                            rs.getString("item_type"),
                            rs.getString("image_url")
                    );
                    cartItems.add(cart);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in findByUserId for cart: " + e.getMessage(), e);
        }
        return cartItems;
    }

    /**
     * Adds an item to the cart, or increments quantity if item already exists.
     * @param userId User ID
     * @param foodItemId Food Item ID
     * @param quantity Quantity to add
     */
    public void addToCart(int userId, int foodItemId, int quantity) {
        int qtyToAdd = Math.max(1, quantity);
        String sql = "INSERT INTO cart (user_id, food_item_id, quantity) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE quantity = quantity + ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, foodItemId);
            ps.setInt(3, qtyToAdd);
            ps.setInt(4, qtyToAdd);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error in addToCart: " + e.getMessage(), e);
        }
    }

    /**
     * Updates the exact quantity of an existing item in the cart.
     * @param userId User ID
     * @param foodItemId Food Item ID
     * @param quantity New quantity (if <= 0, item is removed)
     */
    public void updateQuantity(int userId, int foodItemId, int quantity) {
        if (quantity <= 0) {
            removeItem(userId, foodItemId);
            return;
        }
        String sql = "UPDATE cart SET quantity = ? WHERE user_id = ? AND food_item_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, userId);
            ps.setInt(3, foodItemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error in updateQuantity: " + e.getMessage(), e);
        }
    }

    /**
     * Removes an item from the cart.
     * @param userId User ID
     * @param foodItemId Food Item ID
     */
    public void removeItem(int userId, int foodItemId) {
        String sql = "DELETE FROM cart WHERE user_id = ? AND food_item_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, foodItemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error in removeItem from cart: " + e.getMessage(), e);
        }
    }

    /**
     * Clears all items from the user's cart.
     * @param userId User ID
     */
    public void clearCart(int userId) {
        String sql = "DELETE FROM cart WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error in clearCart: " + e.getMessage(), e);
        }
    }

    /**
     * Calculates the total item count in user's cart.
     * @param userId User ID
     * @return sum of all item quantities in the cart
     */
    public int getCartCount(int userId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM cart WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in getCartCount: " + e.getMessage(), e);
        }
        return 0;
    }
}
