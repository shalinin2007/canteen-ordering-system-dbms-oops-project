package com.canteen.model;

/**
 * Cart entity representing an item added to the user's active shopping cart.
 */
public class Cart {
    private int id;
    private int userId;
    private int foodItemId;
    private String foodItemName;
    private double price;
    private int quantity;
    private String itemType;
    private String imageUrl;

    public Cart() {
    }

    public Cart(int id, int userId, int foodItemId, String foodItemName, double price, int quantity, String itemType, String imageUrl) {
        this.id = id;
        this.userId = userId;
        this.foodItemId = foodItemId;
        this.foodItemName = foodItemName;
        this.price = price;
        this.quantity = quantity;
        this.itemType = itemType;
        this.imageUrl = imageUrl;
    }

    public double getSubtotal() {
        return Math.round(price * quantity * 100.0) / 100.0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getFoodItemId() {
        return foodItemId;
    }

    public void setFoodItemId(int foodItemId) {
        this.foodItemId = foodItemId;
    }

    public String getFoodItemName() {
        return foodItemName;
    }

    public void setFoodItemName(String foodItemName) {
        this.foodItemName = foodItemName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "Cart{" +
                "id=" + id +
                ", userId=" + userId +
                ", foodItemId=" + foodItemId +
                ", foodItemName='" + foodItemName + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}
