package com.canteen.controller;

import com.canteen.service.UserService;
import jakarta.servlet.http.HttpSession;
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
 * Controller managing student and admin authentication, registration, and sessions.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registers a new student and initializes the user session.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> data, HttpSession session) {
        Map<String, Object> result = userService.register(data);
        session.setAttribute("userId", result.get("userId"));
        session.setAttribute("name", result.get("name"));
        session.setAttribute("email", result.get("email"));
        session.setAttribute("role", result.get("role"));
        return ResponseEntity.ok(result);
    }

    /**
     * Authenticates existing user and binds user attributes to the session.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> creds, HttpSession session) {
        String email = creds.get("email");
        String password = creds.get("password");
        Map<String, Object> result = userService.login(email, password);
        session.setAttribute("userId", result.get("userId"));
        session.setAttribute("name", result.get("name"));
        session.setAttribute("email", result.get("email"));
        session.setAttribute("role", result.get("role"));
        return ResponseEntity.ok(result);
    }

    /**
     * Logs out the current user by invalidating the HTTP session.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success", true);
        res.put("message", "Logged out successfully.");
        return ResponseEntity.ok(res);
    }

    /**
     * Returns current authenticated user metadata or HTTP 401 if unauthorized.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpSession session) {
        Object userId = session.getAttribute("userId");
        if (userId == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "UNAUTHORIZED");
            err.put("message", "No active login session found.");
            err.put("status", HttpStatus.UNAUTHORIZED.value());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }

        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("userId", userId);
        userInfo.put("name", session.getAttribute("name"));
        userInfo.put("email", session.getAttribute("email"));
        userInfo.put("role", session.getAttribute("role"));
        return ResponseEntity.ok(userInfo);
    }
}
