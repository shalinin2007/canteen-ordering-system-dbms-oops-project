package com.canteen.model;

/**
 * Admin entity extending User.
 * Demonstrates Object-Oriented Inheritance.
 */
public class Admin extends User {
    private String staffId;

    public Admin() {
        super();
        setRole("ADMIN");
    }

    public Admin(int id, String name, String email, String password, String phone, String staffId) {
        super(id, name, email, password, phone, "ADMIN");
        this.staffId = staffId;
    }

    @Override
    public String getDashboardUrl() {
        return "/admin/dashboard.html";
    }

    @Override
    public String getUserType() {
        return "ADMIN";
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", staffId='" + staffId + '\'' +
                '}';
    }
}
