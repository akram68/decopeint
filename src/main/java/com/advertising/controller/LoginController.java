package com.advertising.controller;

import com.advertising.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        if (authenticateUser(username, password)) {
            errorLabel.setVisible(false);
            MainApp.showDashboard();
        } else {
            showError("Invalid username or password. Please try again.");
        }
    }

    private boolean authenticateUser(String username, String password) {
        // ========== STATIC AUTHENTICATION (FOR DEMONSTRATION) ==========
        // This is temporary static authentication for testing purposes.
        // Replace this with database authentication when ready.

        // Static credentials:
        final String ADMIN_USERNAME = "admin";
        final String ADMIN_PASSWORD = "admin123";

        return username.equals(ADMIN_USERNAME) && password.equals(ADMIN_PASSWORD);

        // ========== DATABASE AUTHENTICATION (FUTURE IMPLEMENTATION) ==========
        // When integrating with MySQL/WAMP, replace the above static authentication
        // with the following database query:
        //
        // try {
        //     Connection conn = DatabaseConnection.getConnection();
        //     String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        //     PreparedStatement stmt = conn.prepareStatement(query);
        //     stmt.setString(1, username);
        //     stmt.setString(2, password); // Use hashed password comparison in production
        //
        //     ResultSet rs = stmt.executeQuery();
        //     boolean isAuthenticated = rs.next();
        //
        //     rs.close();
        //     stmt.close();
        //     return isAuthenticated;
        // } catch (SQLException e) {
        //     e.printStackTrace();
        //     showError("Database connection error.");
        //     return false;
        // }
        //
        // IMPORTANT: In production, passwords should be hashed using BCrypt or similar.
        // Never store plain text passwords in the database!
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
