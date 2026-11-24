package com.canteen.controller;

import com.canteen.dao.CartDAO;
import com.canteen.model.Cart;
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
 * Controller managing student shopping cart operations.
 * Requires authenticated session.
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartDAO cartDAO;

    @Autowired
    public CartController(CartDAO cartDAO) {
        this.cartDAO = cartDAO;
    }

    /**
     * Retrieves all items currently in the user's shopping cart.
     */
    @GetMapping
    public ResponseEntity<?> getCart(HttpSession session) {
        Integer userId = getSessionUserId(session);
        if (userId == null) {
            return unauthorizedResponse();
        }
        List<Cart> items = cartDAO.findByUserId(userId);
        return ResponseEntity.ok(items);
    }

    /**
     * Adds a food item to user's cart.
     */
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> body, HttpSession session) {
        Integer userId = getSessionUserId(session);
        if (userId == null) {
            return unauthorizedResponse();
        }

        if (body == null || !body.containsKey("foodItemId")) {
            return badRequestResponse("Missing required parameter: foodItemId");
        }

        int foodItemId = Integer.parseInt(body.get("foodItemId").toString());
        int quantity = body.containsKey("quantity") ? Integer.parseInt(body.get("quantity").toString()) : 1;

        cartDAO.addToCart(userId, foodItemId, quantity);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Item added to cart.");
        response.put("cartCount", cartDAO.getCartCount(userId));
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the quantity of an item in the cart.
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateQuantity(@RequestBody Map<String, Object> body, HttpSession session) {
        Integer userId = getSessionUserId(session);
        if (userId == null) {
            return unauthorizedResponse();
        }

        if (body == null || !body.containsKey("foodItemId") || !body.containsKey("quantity")) {
            return badRequestResponse("Required parameters: foodItemId, quantity");
        }

        int foodItemId = Integer.parseInt(body.get("foodItemId").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());

        cartDAO.updateQuantity(userId, foodItemId, quantity);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Cart quantity updated.");
        response.put("cartCount", cartDAO.getCartCount(userId));
        return ResponseEntity.ok(response);
    }

    /**
     * Removes an item from the cart.
     */
    @DeleteMapping("/remove/{foodItemId}")
    public ResponseEntity<?> removeItem(@PathVariable int foodItemId, HttpSession session) {
        Integer userId = getSessionUserId(session);
        if (userId == null) {
            return unauthorizedResponse();
        }

        cartDAO.removeItem(userId, foodItemId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Item removed from cart.");
        response.put("cartCount", cartDAO.getCartCount(userId));
        return ResponseEntity.ok(response);
    }

    /**
     * Clears all items from user's cart.
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(HttpSession session) {
        Integer userId = getSessionUserId(session);
        if (userId == null) {
            return unauthorizedResponse();
        }

        cartDAO.clearCart(userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Cart cleared.");
        response.put("cartCount", 0);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns total item count in user's cart.
     */
    @GetMapping("/count")
    public ResponseEntity<?> getCartCount(HttpSession session) {
        Integer userId = getSessionUserId(session);
        if (userId == null) {
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("count", 0);
            return ResponseEntity.ok(res);
        }

        int count = cartDAO.getCartCount(userId);
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("count", count);
        return ResponseEntity.ok(res);
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
        err.put("message", "Please log in to manage your cart.");
        err.put("status", HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
    }

    private ResponseEntity<Map<String, Object>> badRequestResponse(String message) {
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("error", "BAD_REQUEST");
        err.put("message", message);
        err.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }
}
