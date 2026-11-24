package com.canteen.controller;

import com.canteen.model.Order;
import com.canteen.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller managing order placement, order history, and status tracking for students.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Places a new order from current user's cart.
     */
    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(@RequestBody(required = false) Map<String, Object> body, HttpSession session) {
        Integer userId = getSessionUserId(session);
        if (userId == null) {
            return unauthorizedResponse();
        }

        String paymentMethod = (body != null && body.containsKey("paymentMethod") && body.get("paymentMethod") != null)
                ? body.get("paymentMethod").toString()
                : "UPI";

        Map<String, Object> result = orderService.placeOrder(userId, paymentMethod);
        return ResponseEntity.ok(result);
    }

    /**
     * Retrieves all orders placed by the logged-in student.
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyOrders(HttpSession session) {
        Integer userId = getSessionUserId(session);
        if (userId == null) {
            return unauthorizedResponse();
        }

        List<Order> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Retrieves a single order details by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable int id, HttpSession session) {
        Integer userId = getSessionUserId(session);
        if (userId == null) {
            return unauthorizedResponse();
        }

        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    private Integer getSessionUserId(HttpSession session) {
        Object userId = session.getAttribute("userId");
        if (userId == null) return null;
        if (userId instanceof Integer) return (Integer) userId;
        try {
            return Integer.parseInt(userId.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private ResponseEntity<Map<String, Object>> unauthorizedResponse() {
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("error", "UNAUTHORIZED");
        err.put("message", "Please log in to manage your orders.");
        err.put("status", HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
    }
}
