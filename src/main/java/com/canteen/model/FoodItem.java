package com.canteen.model;

/**
 * Abstract class representing a generic Food Item on the canteen menu.
 * Demonstrates Abstraction, Encapsulation, and Polymorphism.
 */
public abstract class FoodItem {
    private int id;
    private String name;
    private String description;
    private double price;
    private int categoryId;
    private String categoryName;
    private String itemType;
    private boolean available;
    private String imageUrl;
    private int preparationTime;

    public FoodItem() {
        this.available = true;
    }

    public FoodItem(int id, String name, String description, double price, int categoryId,
                    String categoryName, String itemType, boolean available, String imageUrl, int preparationTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.itemType = itemType;
        this.available = available;
        this.imageUrl = imageUrl;
        this.preparationTime = preparationTime;
    }

    // Abstract methods to be implemented by specific food types (Polymorphism)
    public abstract String getItemType();
    public abstract String getItemBadge();
    public abstract double calculateDiscountedPrice(double discountPercent);

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(int preparationTime) {
        this.preparationTime = preparationTime;
    }

    @Override
    public String toString() {
        return "FoodItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", categoryId=" + categoryId +
                ", itemType='" + getItemType() + '\'' +
                ", available=" + available +
                '}';
    }
}
