package com.canteen.service;

import com.canteen.dao.FoodItemDAO;
import com.canteen.exception.ItemNotFoundException;
import com.canteen.model.Beverage;
import com.canteen.model.Category;
import com.canteen.model.FoodItem;
import com.canteen.model.NonVegItem;
import com.canteen.model.VegItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service managing menu items, categories, and item customizations.
 */
@Service
public class MenuService {

    private final FoodItemDAO foodItemDAO;

    @Autowired
    public MenuService(FoodItemDAO foodItemDAO) {
        this.foodItemDAO = foodItemDAO;
    }

    /**
     * Retrieves all available food items on the menu.
     * @return List of FoodItem objects
     */
    public List<FoodItem> getAllItems() {
        return foodItemDAO.findAll();
    }

    /**
     * Retrieves food items filtered by category.
     * @param categoryId Category ID
     * @return List of FoodItem objects
     */
    public List<FoodItem> getItemsByCategory(int categoryId) {
        return foodItemDAO.findByCategory(categoryId);
    }

    /**
     * Retrieves a single food item by ID, throwing ItemNotFoundException if missing.
     * @param id Food Item ID
     * @return FoodItem object
     */
    public FoodItem getItemById(int id) {
        FoodItem item = foodItemDAO.findById(id);
        if (item == null) {
            throw new ItemNotFoundException("Food item not found with ID: " + id);
        }
        return item;
    }

    /**
     * Creates and persists a new food item with polymorphic type handling.
     * @param data Item parameters
     * @return persisted FoodItem entity
     */
    public FoodItem addItem(Map<String, Object> data) {
        String name = data.get("name") != null ? data.get("name").toString().trim() : "";
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Food item name cannot be empty.");
        }

        String description = data.get("description") != null ? data.get("description").toString() : "";
        double price = parseDouble(data.get("price"), 0.0);
        int categoryId = parseInt(data.get("categoryId"), 1);
        String itemType = data.get("itemType") != null ? data.get("itemType").toString().trim().toUpperCase() : "VEG";
        boolean available = parseBoolean(data.get("available"), true);
        String imageUrl = data.get("imageUrl") != null ? data.get("imageUrl").toString() : "";
        int prepTime = parseInt(data.get("preparationTime"), 15);
        boolean isCold = parseBoolean(data.get("isCold"), false);

        FoodItem item;
        if ("BEVERAGE".equals(itemType)) {
            Beverage bev = new Beverage();
            bev.setCold(isCold);
            item = bev;
        } else if ("NON_VEG".equals(itemType)) {
            item = new NonVegItem();
        } else {
            item = new VegItem();
        }

        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setCategoryId(categoryId);
        item.setItemType(itemType);
        item.setAvailable(available);
        item.setImageUrl(imageUrl);
        item.setPreparationTime(prepTime);

        int generatedId = foodItemDAO.save(item);
        return getItemById(generatedId);
    }

    /**
     * Updates an existing food item.
     * @param id Food item ID
     * @param data Updated parameters
     * @return updated FoodItem entity
     */
    public FoodItem updateItem(int id, Map<String, Object> data) {
        FoodItem existing = getItemById(id);

        String name = data.containsKey("name") ? data.get("name").toString() : existing.getName();
        String description = data.containsKey("description") ? data.get("description").toString() : existing.getDescription();
        double price = data.containsKey("price") ? parseDouble(data.get("price"), existing.getPrice()) : existing.getPrice();
        int categoryId = data.containsKey("categoryId") ? parseInt(data.get("categoryId"), existing.getCategoryId()) : existing.getCategoryId();
        String itemType = data.containsKey("itemType") ? data.get("itemType").toString().toUpperCase() : existing.getItemType();
        boolean available = data.containsKey("available") ? parseBoolean(data.get("available"), existing.isAvailable()) : existing.isAvailable();
        String imageUrl = data.containsKey("imageUrl") ? data.get("imageUrl").toString() : existing.getImageUrl();
        int prepTime = data.containsKey("preparationTime") ? parseInt(data.get("preparationTime"), existing.getPreparationTime()) : existing.getPreparationTime();
        boolean isCold = data.containsKey("isCold") ? parseBoolean(data.get("isCold"), false) : false;

        FoodItem updatedItem;
        if ("BEVERAGE".equals(itemType)) {
            Beverage bev = new Beverage();
            bev.setCold(isCold);
            updatedItem = bev;
        } else if ("NON_VEG".equals(itemType)) {
            updatedItem = new NonVegItem();
        } else {
            updatedItem = new VegItem();
        }

        updatedItem.setId(id);
        updatedItem.setName(name);
        updatedItem.setDescription(description);
        updatedItem.setPrice(price);
        updatedItem.setCategoryId(categoryId);
        updatedItem.setItemType(itemType);
        updatedItem.setAvailable(available);
        updatedItem.setImageUrl(imageUrl);
        updatedItem.setPreparationTime(prepTime);

        foodItemDAO.update(updatedItem);
        return getItemById(id);
    }

    /**
     * Deletes a food item from the menu.
     * @param id Food item ID
     */
    public void deleteItem(int id) {
        getItemById(id); // Ensures item exists or throws 404
        foodItemDAO.deleteItem(id);
    }

    /**
     * Retrieves all categories.
     * @return List of Category entities
     */
    public List<Category> getCategories() {
        return foodItemDAO.findAllCategories();
    }

    private double parseDouble(Object val, double fallback) {
        if (val == null) return fallback;
        try {
            return Double.parseDouble(val.toString());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private int parseInt(Object val, int fallback) {
        if (val == null) return fallback;
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private boolean parseBoolean(Object val, boolean fallback) {
        if (val == null) return fallback;
        return Boolean.parseBoolean(val.toString());
    }
}
