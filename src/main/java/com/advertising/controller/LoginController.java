package com.advertising.controller;

import com.advertising.MainApp;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class LoginController {

    private VBox view;
    private TextField usernameField;
    private PasswordField passwordField;
    private Label errorLabel;

    public LoginController() {
        createView();
    }

    public VBox getView() {
        return view;
    }

    private void createView() {
        // Container principal
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(0));
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Container de login
        VBox loginContainer = new VBox(20);
        loginContainer.setAlignment(Pos.CENTER);
        loginContainer.setMaxWidth(400);
        loginContainer.setPadding(new Insets(40));
        loginContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // Logo et titre
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        
        Text logo = new Text("🎯");
        logo.setStyle("-fx-font-size: 48;");
        
        Text title = new Text("Advertising Management System");
        title.setFont(Font.font("System Bold", 24));
        
        Text subtitle = new Text("Welcome back! Please login to your account.");
        subtitle.setFont(Font.font(13));
        
        headerBox.getChildren().addAll(logo, title, subtitle);

        // Formulaire
        VBox formBox = new VBox(15);
        
        // Champ username
        VBox usernameBox = new VBox(5);
        Label usernameLabel = new Label("Username");
        usernameLabel.setFont(Font.font("System Bold", 12));
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-padding: 10;");
        usernameBox.getChildren().addAll(usernameLabel, usernameField);

        // Champ password
        VBox passwordBox = new VBox(5);
        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(Font.font("System Bold", 12));
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-padding: 10;");
        passwordBox.getChildren().addAll(passwordLabel, passwordField);

        // Label d'erreur
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 12;");
        errorLabel.setVisible(false);

        // Bouton login
        Button loginButton = new Button("Login");
        loginButton.setFont(Font.font("System Bold", 14));
        loginButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 12;");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(e -> handleLogin());

        // Info credentials
        Text infoText = new Text("Default credentials: admin / admin123");
        infoText.setFont(Font.font(11));
        infoText.setStyle("-fx-fill: #666;");

        formBox.getChildren().addAll(usernameBox, passwordBox, errorLabel, loginButton);
        
        // Assemblage
        loginContainer.getChildren().addAll(headerBox, formBox, infoText);
        root.getChildren().add(loginContainer);
        
        this.view = root;
    }

    private void handleLogin() {
        System.out.println("handleLogin() called!");
        
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        if (authenticateUser(username, password)) {
            errorLabel.setVisible(false);
            // Navigate directly to ServiceController instead of Dashboard
            MainApp.showServicePage();
        } else {
            showError("Invalid username or password. Please try again.");
        }
    }

    private boolean authenticateUser(String username, String password) {
        final String ADMIN_USERNAME = "admin";
        final String ADMIN_PASSWORD = "admin123";

        return username.equals(ADMIN_USERNAME) && password.equals(ADMIN_PASSWORD);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}