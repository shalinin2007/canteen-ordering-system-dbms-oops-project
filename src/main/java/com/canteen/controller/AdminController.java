package com.canteen.controller;

import com.canteen.model.FoodItem;
import com.canteen.model.Order;
import com.canteen.service.MenuService;
import com.canteen.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller providing administrative management endpoints.
 * All endpoints verify administrator role in current session.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final OrderService orderService;
    private final MenuService menuService;

    @Autowired
    public AdminController(OrderService orderService, MenuService menuService) {
        this.orderService = orderService;
        this.menuService = menuService;
    }

    /**
     * Retrieves analytics dashboard KPIs.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(HttpSession session) {
        if (!isAdmin(session)) {
            return forbiddenResponse();
        }
        return ResponseEntity.ok(orderService.getDashboardStats());
    }

    /**
     * Retrieves all orders across all students.
     */
    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders(HttpSession session) {
        if (!isAdmin(session)) {
            return forbiddenResponse();
        }
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Updates an order's status (PENDING/CONFIRMED/PREPARING/READY/DELIVERED/CANCELLED).
     */
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable int id,
                                               @RequestBody Map<String, Object> body,
                                               HttpSession session) {
        if (!isAdmin(session)) {
            return forbiddenResponse();
        }

        if (body == null || !body.containsKey("status") || body.get("status") == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "BAD_REQUEST");
            err.put("message", "Status field is required.");
            err.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }

        String status = body.get("status").toString().trim();
        orderService.updateOrderStatus(id, status);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success", true);
        res.put("message", "Order status updated to " + status.toUpperCase());
        res.put("orderId", id);
        res.put("status", status.toUpperCase());
        return ResponseEntity.ok(res);
    }

    /**
     * Retrieves complete menu for administrative review.
     */
    @GetMapping("/menu")
    public ResponseEntity<?> getAdminMenu(HttpSession session) {
        if (!isAdmin(session)) {
            return forbiddenResponse();
        }
        return ResponseEntity.ok(menuService.getAllItems());
    }

    /**
     * Adds a new food item to the menu.
     */
    @PostMapping("/menu")
    public ResponseEntity<?> addMenuItem(@RequestBody Map<String, Object> data, HttpSession session) {
        if (!isAdmin(session)) {
            return forbiddenResponse();
        }
        FoodItem createdItem = menuService.addItem(data);
        return ResponseEntity.ok(createdItem);
    }

    /**
     * Updates an existing food item.
     */
    @PutMapping("/menu/{id}")
    public ResponseEntity<?> updateMenuItem(@PathVariable int id,
                                            @RequestBody Map<String, Object> data,
                                            HttpSession session) {
        if (!isAdmin(session)) {
            return forbiddenResponse();
        }
        FoodItem updatedItem = menuService.updateItem(id, data);
        return ResponseEntity.ok(updatedItem);
    }

    /**
     * Deletes a food item from the menu.
     */
    @DeleteMapping("/menu/{id}")
    public ResponseEntity<?> deleteMenuItem(@PathVariable int id, HttpSession session) {
        if (!isAdmin(session)) {
            return forbiddenResponse();
        }
        menuService.deleteItem(id);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success", true);
        res.put("message", "Food item deleted successfully.");
        res.put("deletedId", id);
        return ResponseEntity.ok(res);
    }

    private boolean isAdmin(HttpSession session) {
        Object role = session.getAttribute("role");
        return role != null && "ADMIN".equalsIgnoreCase(role.toString());
    }

    private ResponseEntity<Map<String, Object>> forbiddenResponse() {
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("error", "FORBIDDEN");
        err.put("message", "Access denied. Administrator privileges required.");
        err.put("status", HttpStatus.FORBIDDEN.value());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
    }
}
