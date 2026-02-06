package com.advertising;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Stage primaryStage;
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 700;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Advertising Company Management System");
        primaryStage.setResizable(false);

        showLoginPage();

        primaryStage.show();
    }

    // Dans MainApp.java, ajoutez :
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void showLoginPage() {
        // Crée le contrôleur de login
        com.advertising.controller.LoginController loginController = new com.advertising.controller.LoginController();

        // Crée la scène avec la vue du contrôleur
        Scene scene = new Scene(loginController.getView(), WINDOW_WIDTH, WINDOW_HEIGHT);

        // ENLEVÉ la référence au CSS qui n'existe pas
        // scene.getStylesheets().add(MainApp.class.getResource("/css/style.css").toExternalForm());

        primaryStage.setScene(scene);
    }

    public static void showDashboard() {
        // Crée le contrôleur du dashboard
        com.advertising.controller.DashboardController dashboardController = new com.advertising.controller.DashboardController();

        // Crée la scène avec la vue du contrôleur
        Scene scene = new Scene(dashboardController.getView(), WINDOW_WIDTH, WINDOW_HEIGHT);

        // ENLEVÉ la référence au CSS qui n'existe pas
        // scene.getStylesheets().add(MainApp.class.getResource("/css/style.css").toExternalForm());

        primaryStage.setScene(scene);
    }

    public static void showServicePage() {
        // Crée le contrôleur des services
        com.advertising.controller.ServiceController serviceController = new com.advertising.controller.ServiceController();

        // Crée la scène avec la vue du contrôleur (plus grande pour ServiceController)
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        Scene scene = new Scene(serviceController.getView(), 1400, 900);

        primaryStage.setScene(scene);
        primaryStage.setTitle("📦 Gestion des Services - Advertising Management");
    }

    public static void main(String[] args) {
        launch(args);
    }
}