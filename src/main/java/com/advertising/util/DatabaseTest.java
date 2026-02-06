package com.advertising.util;

public class DatabaseTest {
    
    public static void main(String[] args) {
        System.out.println("🔌 Testing database connection...");
        
        try {
            // Test simple de connexion
            boolean isConnected = DatabaseConnection.testConnection();
            
            if (isConnected) {
                System.out.println("✅ CONNECTION SUCCESSFUL!");
                System.out.println("   Database: advertising_db");
                System.out.println("   Host: localhost:3306");
                System.out.println("   User: root");
            } else {
                System.out.println("❌ CONNECTION FAILED!");
                System.out.println("\nTroubleshooting steps:");
                System.out.println("1. Start WAMP/XAMPP (make sure it's green)");
                System.out.println("2. Open phpMyAdmin: http://localhost/phpmyadmin");
                System.out.println("3. Create database: advertising_db");
            }
            
        } catch (Exception e) {
            System.err.println("💥 ERROR: " + e.getMessage());
            System.out.println("\n🔧 Check DatabaseConnection.java:");
            System.out.println("   URL: jdbc:mysql://localhost:3306/advertising_db");
            System.out.println("   USER: root");
            System.out.println("   PASSWORD: (empty)");
        }
    }
}