package com.canteen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Campus Canteen Online Ordering System
 * Main entry point for the Spring Boot application.
 * Demonstrates Java OOP concepts with MySQL JDBC connectivity.
 */
@SpringBootApplication
public class CanteenApplication {
    public static void main(String[] args) {
        SpringApplication.run(CanteenApplication.class, args);
        System.out.println("===========================================");
        System.out.println(" Campus Canteen System Started!");
        System.out.println(" Open: http://localhost:8080");
        System.out.println("===========================================");
    }
}
