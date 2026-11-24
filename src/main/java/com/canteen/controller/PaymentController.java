package com.canteen.controller;

import com.canteen.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller managing UPI and payment confirmations.
 */
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final OrderService orderService;

    @Autowired
    public PaymentController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Confirms and verifies student payment with the transaction ID.
     */
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody Map<String, Object> body) {
        if (body == null || !body.containsKey("orderId") || !body.containsKey("transactionId")) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "BAD_REQUEST");
            err.put("message", "Both orderId and transactionId are required for payment verification.");
            err.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }

        int orderId = Integer.parseInt(body.get("orderId").toString());
        String transactionId = body.get("transactionId").toString();

        orderService.confirmPayment(orderId, transactionId);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success", true);
        res.put("message", "Payment confirmed successfully!");
        res.put("orderId", orderId);
        res.put("transactionId", transactionId);
        res.put("paymentStatus", "PAID");
        return ResponseEntity.ok(res);
    }

    /**
     * Provides Canteen UPI payee details for student QR or VPA payment.
     */
    @GetMapping("/upi-info")
    public ResponseEntity<Map<String, String>> getUpiInfo() {
        Map<String, String> upi = new LinkedHashMap<>();
        upi.put("upiId", "canteen@sbi");
        upi.put("name", "College Canteen");
        return ResponseEntity.ok(upi);
    }
}
