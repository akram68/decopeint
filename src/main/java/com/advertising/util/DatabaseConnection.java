package com.advertising.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/decopeint";
    private static final String USER = "root";
    private static final String PASSWORD = "";  // Vide par défaut pour WAMP
    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Pour MySQL 8.0+
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Database connected!");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL Driver not found");
            }
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
                System.out.println("🔌 Connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            boolean isOpen = conn != null && !conn.isClosed();
            System.out.println("Connection test: " + (isOpen ? "OK" : "Closed"));
            return isOpen;
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            return false;
        }
    }
}