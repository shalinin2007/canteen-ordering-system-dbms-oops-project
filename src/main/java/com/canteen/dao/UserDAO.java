package com.canteen.dao;

import com.canteen.model.Admin;
import com.canteen.model.Student;
import com.canteen.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data Access Object for User operations using plain JDBC.
 * Demonstrates Polymorphic object construction based on database records.
 */
@Repository
public class UserDAO {

    private final DataSource dataSource;

    @Autowired
    public UserDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Finds a user by email and returns a Student or Admin instance based on role.
     * @param email User email
     * @return User (Student or Admin), or null if not found
     */
    public User findByEmail(String email) {
        String sql = "SELECT id, name, email, password, phone, role, college_id, branch, year, staff_id " +
                     "FROM users WHERE email = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUserResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in findByEmail: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Finds a user by primary key ID and returns a Student or Admin instance based on role.
     * @param id User ID
     * @return User (Student or Admin), or null if not found
     */
    public User findById(int id) {
        String sql = "SELECT id, name, email, password, phone, role, college_id, branch, year, staff_id " +
                     "FROM users WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUserResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in findById: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Inserts a new Student into the users table and returns the generated ID.
     * @param student Student entity
     * @return generated auto-increment ID
     */
    public int save(Student student) {
        String sql = "INSERT INTO users (name, email, password, phone, role, college_id, branch, year) " +
                     "VALUES (?, ?, ?, ?, 'STUDENT', ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, student.getName());
            ps.setString(2, student.getEmail());
            ps.setString(3, student.getPassword());
            ps.setString(4, student.getPhone());
            ps.setString(5, student.getCollegeId());
            ps.setString(6, student.getBranch());
            ps.setInt(7, student.getYear());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating student failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    student.setId(generatedId);
                    return generatedId;
                } else {
                    throw new SQLException("Creating student failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in save student: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a user already exists with the given email address.
     * @param email Email to check
     * @return true if exists, false otherwise
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in emailExists: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Checks if a user already exists with the given college roll/registration number.
     * @param collegeId College ID to check
     * @return true if exists, false otherwise
     */
    public boolean collegeIdExists(String collegeId) {
        String sql = "SELECT COUNT(*) FROM users WHERE college_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, collegeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in collegeIdExists: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Helper to construct polymorphic Student or Admin instance from JDBC ResultSet.
     */
    private User mapUserResultSet(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String password = rs.getString("password");
        String phone = rs.getString("phone");

        if ("ADMIN".equalsIgnoreCase(role)) {
            String staffId = rs.getString("staff_id");
            return new Admin(id, name, email, password, phone, staffId);
        } else {
            String collegeId = rs.getString("college_id");
            String branch = rs.getString("branch");
            int year = rs.getInt("year");
            return new Student(id, name, email, password, phone, collegeId, branch, year);
        }
    }
}
