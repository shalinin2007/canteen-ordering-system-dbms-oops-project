package com.canteen.dao;

import com.canteen.model.Beverage;
import com.canteen.model.Category;
import com.canteen.model.FoodItem;
import com.canteen.model.NonVegItem;
import com.canteen.model.VegItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for FoodItem and Category management using plain JDBC.
 * Implements Polymorphic mapping of food items to VegItem, NonVegItem, or Beverage.
 */
@Repository
public class FoodItemDAO {

    private final DataSource dataSource;

    @Autowired
    public FoodItemDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Retrieves all food items along with category names.
     * @return List of FoodItem objects
     */
    public List<FoodItem> findAll() {
        List<FoodItem> items = new ArrayList<>();
        String sql = "SELECT f.id, f.name, f.description, f.price, f.category_id, c.name AS category_name, " +
                     "f.item_type, f.is_cold, f.available, f.image_url, f.preparation_time " +
                     "FROM food_items f " +
                     "LEFT JOIN categories c ON f.category_id = c.id " +
                     "ORDER BY f.id ASC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in findAll food items: " + e.getMessage(), e);
        }
        return items;
    }

    /**
     * Retrieves all food items belonging to a specific category.
     * @param categoryId Category ID
     * @return List of FoodItem objects
     */
    public List<FoodItem> findByCategory(int categoryId) {
        List<FoodItem> items = new ArrayList<>();
        String sql = "SELECT f.id, f.name, f.description, f.price, f.category_id, c.name AS category_name, " +
                     "f.item_type, f.is_cold, f.available, f.image_url, f.preparation_time " +
                     "FROM food_items f " +
                     "LEFT JOIN categories c ON f.category_id = c.id " +
                     "WHERE f.category_id = ? " +
                     "ORDER BY f.id ASC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in findByCategory: " + e.getMessage(), e);
        }
        return items;
    }

    /**
     * Retrieves a single food item by ID.
     * @param id Food Item ID
     * @return FoodItem object or null
     */
    public FoodItem findById(int id) {
        String sql = "SELECT f.id, f.name, f.description, f.price, f.category_id, c.name AS category_name, " +
                     "f.item_type, f.is_cold, f.available, f.image_url, f.preparation_time " +
                     "FROM food_items f " +
                     "LEFT JOIN categories c ON f.category_id = c.id " +
                     "WHERE f.id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in findById: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Inserts a new FoodItem into the database.
     * @param item FoodItem instance
     * @return generated ID
     */
    public int save(FoodItem item) {
        String sql = "INSERT INTO food_items (name, description, price, category_id, item_type, is_cold, available, image_url, preparation_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getDescription());
            ps.setDouble(3, item.getPrice());
            ps.setInt(4, item.getCategoryId());
            ps.setString(5, item.getItemType());

            boolean isCold = false;
            if (item instanceof Beverage) {
                isCold = ((Beverage) item).isCold();
            }
            ps.setBoolean(6, isCold);
            ps.setBoolean(7, item.isAvailable());
            ps.setString(8, item.getImageUrl());
            ps.setInt(9, item.getPreparationTime());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    item.setId(generatedId);
                    return generatedId;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in save food item: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Updates an existing food item.
     * @param item FoodItem instance
     */
    public void update(FoodItem item) {
        String sql = "UPDATE food_items SET name = ?, description = ?, price = ?, category_id = ?, " +
                     "item_type = ?, is_cold = ?, available = ?, image_url = ?, preparation_time = ? " +
                     "WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getDescription());
            ps.setDouble(3, item.getPrice());
            ps.setInt(4, item.getCategoryId());
            ps.setString(5, item.getItemType());

            boolean isCold = false;
            if (item instanceof Beverage) {
                isCold = ((Beverage) item).isCold();
            }
            ps.setBoolean(6, isCold);
            ps.setBoolean(7, item.isAvailable());
            ps.setString(8, item.getImageUrl());
            ps.setInt(9, item.getPreparationTime());
            ps.setInt(10, item.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error in update food item: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a food item by ID.
     * @param id Food Item ID
     */
    public void deleteItem(int id) {
        String sql = "DELETE FROM food_items WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error in deleteItem: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all categories.
     * @return List of Category objects
     */
    public List<Category> findAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT id, name, description, image_url FROM categories ORDER BY id ASC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Category cat = new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("image_url")
                );
                categories.add(cat);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in findAllCategories: " + e.getMessage(), e);
        }
        return categories;
    }

    /**
     * Private helper method: maps ResultSet to VegItem, NonVegItem, or Beverage based on item_type.
     */
    private FoodItem mapResultSet(ResultSet rs) throws SQLException {
        String itemType = rs.getString("item_type");
        boolean isCold = rs.getBoolean("is_cold");

        FoodItem item;
        if ("BEVERAGE".equalsIgnoreCase(itemType)) {
            Beverage bev = new Beverage();
            bev.setCold(isCold);
            item = bev;
        } else if ("NON_VEG".equalsIgnoreCase(itemType)) {
            item = new NonVegItem();
        } else {
            item = new VegItem();
        }

        item.setId(rs.getInt("id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setPrice(rs.getDouble("price"));
        item.setCategoryId(rs.getInt("category_id"));
        item.setCategoryName(rs.getString("category_name"));
        item.setItemType(itemType);
        item.setAvailable(rs.getBoolean("available"));
        item.setImageUrl(rs.getString("image_url"));
        item.setPreparationTime(rs.getInt("preparation_time"));

        return item;
    }
}
