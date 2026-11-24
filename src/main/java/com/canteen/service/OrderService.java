package com.canteen.service;

import com.canteen.dao.CartDAO;
import com.canteen.dao.OrderDAO;
import com.canteen.dao.PaymentDAO;
import com.canteen.exception.CartEmptyException;
import com.canteen.exception.ItemNotFoundException;
import com.canteen.exception.PaymentFailedException;
import com.canteen.model.Cart;
import com.canteen.model.Order;
import com.canteen.model.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service managing orders, checkout flows, and payment confirmations.
 */
@Service
public class OrderService {

    private final OrderDAO orderDAO;
    private final CartDAO cartDAO;
    private final PaymentDAO paymentDAO;

    @Autowired
    public OrderService(OrderDAO orderDAO, CartDAO cartDAO, PaymentDAO paymentDAO) {
        this.orderDAO = orderDAO;
        this.cartDAO = cartDAO;
        this.paymentDAO = paymentDAO;
    }

    /**
     * Converts a student's active shopping cart into an Order, generates a unique pickup token,
     * logs a pending payment record, and clears the user's cart.
     * @param userId ID of the student
     * @param paymentMethod Selected payment method (UPI/CARD/WALLET)
     * @return Map containing order confirmation details
     */
    public Map<String, Object> placeOrder(int userId, String paymentMethod) {
        List<Cart> cartItems = cartDAO.findByUserId(userId);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new CartEmptyException("Cannot place order: your shopping cart is empty.");
        }

        double totalAmount = cartItems.stream()
                .mapToDouble(Cart::getSubtotal)
                .sum();
        totalAmount = Math.round(totalAmount * 100.0) / 100.0;

        String pickupToken = "TKN-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");
        order.setPaymentStatus("PENDING");
        order.setPickupToken(pickupToken);

        int orderId = orderDAO.createOrder(order);
        order.setId(orderId);

        // Record individual order item lines
        orderDAO.addOrderItems(orderId, cartItems);

        // Record initial payment record
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(totalAmount);
        payment.setPaymentMethod(paymentMethod != null && !paymentMethod.trim().isEmpty() ? paymentMethod.toUpperCase() : "UPI");
        payment.setStatus("PENDING");
        payment.setUpiId("canteen@sbi");
        paymentDAO.save(payment);

        // Clear cart after successful checkout
        cartDAO.clearCart(userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "Order placed successfully!");
        result.put("orderId", orderId);
        result.put("pickupToken", pickupToken);
        result.put("totalAmount", totalAmount);
        result.put("paymentMethod", payment.getPaymentMethod());
        result.put("status", "PENDING");
        result.put("paymentStatus", "PENDING");
        return result;
    }

    /**
     * Retrieves all orders placed by a specific student.
     * @param userId Student user ID
     * @return List of Order objects
     */
    public List<Order> getUserOrders(int userId) {
        return orderDAO.findByUserId(userId);
    }

    /**
     * Retrieves an order by ID.
     * @param id Order ID
     * @return Order object
     */
    public Order getOrderById(int id) {
        Order order = orderDAO.findById(id);
        if (order == null) {
            throw new ItemNotFoundException("Order not found with ID: " + id);
        }
        return order;
    }

    /**
     * Updates an order's fulfillment status (e.g. PREPARING, READY, DELIVERED).
     * @param orderId Order ID
     * @param status New status string
     */
    public void updateOrderStatus(int orderId, String status) {
        getOrderById(orderId); // Validates existence
        orderDAO.updateStatus(orderId, status.toUpperCase());
    }

    /**
     * Retrieves all orders across all students for administrative view.
     * @return List of all Order objects
     */
    public List<Order> getAllOrders() {
        return orderDAO.findAll();
    }

    /**
     * Gathers operational statistics for admin dashboard.
     * @return Map with key metrics
     */
    public Map<String, Object> getDashboardStats() {
        return orderDAO.getDashboardStats();
    }

    /**
     * Confirms successful payment verification for an order.
     * @param orderId Order ID
     * @param transactionId Payment gateway or UPI transaction reference
     */
    public void confirmPayment(int orderId, String transactionId) {
        Order order = getOrderById(orderId);

        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new PaymentFailedException("Transaction ID is required to confirm payment.");
        }

        Payment payment = paymentDAO.findByOrderId(orderId);
        if (payment != null) {
            paymentDAO.updateStatus(payment.getId(), "SUCCESS", transactionId.trim());
        } else {
            Payment newPayment = new Payment();
            newPayment.setOrderId(orderId);
            newPayment.setAmount(order.getTotalAmount());
            newPayment.setPaymentMethod("UPI");
            newPayment.setTransactionId(transactionId.trim());
            newPayment.setStatus("SUCCESS");
            newPayment.setUpiId("canteen@sbi");
            paymentDAO.save(newPayment);
        }

        orderDAO.updatePaymentStatus(orderId, "PAID");
        orderDAO.updateStatus(orderId, "CONFIRMED");
    }
}
