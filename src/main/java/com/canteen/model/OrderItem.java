package com.canteen.model;

/**
 * OrderItem representing an individual food item entry within a placed Order.
 */
public class OrderItem {
    private int id;
    private int orderId;
    private int foodItemId;
    private String foodItemName;
    private int quantity;
    private double price;
    private String itemType;

    public OrderItem() {
    }

    public OrderItem(int id, int orderId, int foodItemId, String foodItemName, int quantity, double price, String itemType) {
        this.id = id;
        this.orderId = orderId;
        this.foodItemId = foodItemId;
        this.foodItemName = foodItemName;
        this.quantity = quantity;
        this.price = price;
        this.itemType = itemType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public double getSubtotal() {
        return price * quantity;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", foodItemId=" + foodItemId +
                ", foodItemName='" + foodItemName + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}
