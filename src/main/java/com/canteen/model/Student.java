package com.canteen.model;

/**
 * Student entity extending User.
 * Demonstrates Object-Oriented Inheritance and Polymorphism.
 */
public class Student extends User {
    private String collegeId;
    private String branch;
    private int year;

    public Student() {
        super();
        setRole("STUDENT");
    }

    public Student(int id, String name, String email, String password, String phone,
                   String collegeId, String branch, int year) {
        super(id, name, email, password, phone, "STUDENT");
        this.collegeId = collegeId;
        this.branch = branch;
        this.year = year;
    }

    @Override
    public String getDashboardUrl() {
        return "/menu.html";
    }

    @Override
    public String getUserType() {
        return "STUDENT";
    }

    public String getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(String collegeId) {
        this.collegeId = collegeId;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", collegeId='" + collegeId + '\'' +
                ", branch='" + branch + '\'' +
                ", year=" + year +
                '}';
    }
}
