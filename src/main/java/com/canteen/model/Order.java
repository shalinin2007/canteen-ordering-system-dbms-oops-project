package com.canteen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Order entity representing a student's placed canteen order.
 */
public class Order {
    private int id;
    private int userId;
    private String userName;
    private double totalAmount;
    private String status;         // PENDING, CONFIRMED, PREPARING, READY, DELIVERED, CANCELLED
    private String paymentStatus;  // PENDING, PAID, FAILED
    private String pickupToken;
    private String createdAt;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
        this.status = "PENDING";
        this.paymentStatus = "PENDING";
        this.items = new ArrayList<>();
    }

    public Order(int id, int userId, String userName, double totalAmount, String status,
                 String paymentStatus, String pickupToken, String createdAt, List<OrderItem> items) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.pickupToken = pickupToken;
        this.createdAt = createdAt;
        this.items = items != null ? items : new ArrayList<>();
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPickupToken() {
        return pickupToken;
    }

    public void setPickupToken(String pickupToken) {
        this.pickupToken = pickupToken;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void addItem(OrderItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", pickupToken='" + pickupToken + '\'' +
                ", itemsCount=" + (items != null ? items.size() : 0) +
                '}';
    }
}
