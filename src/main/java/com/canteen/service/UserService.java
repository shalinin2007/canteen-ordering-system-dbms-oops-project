package com.canteen.service;

import com.canteen.dao.UserDAO;
import com.canteen.exception.UserNotFoundException;
import com.canteen.model.Student;
import com.canteen.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service handling user authentication and student onboarding.
 */
@Service
public class UserService {

    private final UserDAO userDAO;

    @Autowired
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Registers a new Student account after validating uniqueness of email and collegeId.
     * @param data Registration attributes
     * @return Map containing registration result and session metadata
     */
    public Map<String, Object> register(Map<String, String> data) {
        String name = data.get("name");
        String email = data.get("email");
        String password = data.get("password");
        String phone = data.get("phone");
        String collegeId = data.get("collegeId");
        String branch = data.get("branch");
        String yearStr = data.get("year");

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address is required.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }

        email = email.trim().toLowerCase();
        if (userDAO.emailExists(email)) {
            throw new IllegalArgumentException("An account with email " + email + " already exists.");
        }

        if (collegeId != null && !collegeId.trim().isEmpty()) {
            collegeId = collegeId.trim().toUpperCase();
            if (userDAO.collegeIdExists(collegeId)) {
                throw new IllegalArgumentException("College ID " + collegeId + " is already registered.");
            }
        }

        int year = 1;
        if (yearStr != null && !yearStr.trim().isEmpty()) {
            try {
                year = Integer.parseInt(yearStr.trim());
            } catch (NumberFormatException ignored) {
                year = 1;
            }
        }

        Student student = new Student();
        student.setName(name.trim());
        student.setEmail(email);
        student.setPassword(password);
        student.setPhone(phone != null ? phone.trim() : "");
        student.setCollegeId(collegeId != null ? collegeId : "N/A");
        student.setBranch(branch != null ? branch.trim() : "General");
        student.setYear(year);

        int generatedId = userDAO.save(student);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Student registration successful!");
        response.put("userId", generatedId);
        response.put("name", student.getName());
        response.put("email", student.getEmail());
        response.put("role", student.getRole());
        response.put("userType", student.getUserType());
        response.put("redirectUrl", student.getDashboardUrl());
        return response;
    }

    /**
     * Authenticates a user with email and password.
     * @param email User email
     * @param password Raw password
     * @return Map containing auth confirmation and routing path
     */
    public Map<String, Object> login(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Both email and password are required.");
        }

        User user = userDAO.findByEmail(email.trim().toLowerCase());
        if (user == null || !user.getPassword().equals(password)) {
            throw new UserNotFoundException("Invalid email or password.");
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Login successful!");
        response.put("userId", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        response.put("userType", user.getUserType());
        response.put("redirectUrl", user.getDashboardUrl());
        return response;
    }

    /**
     * Retrieves user entity by primary key.
     * @param id User ID
     * @return User object
     */
    public User getUserById(int id) {
        User user = userDAO.findById(id);
        if (user == null) {
            throw new UserNotFoundException("User not found with ID: " + id);
        }
        return user;
    }
}
