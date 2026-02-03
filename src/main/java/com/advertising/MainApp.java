package com.advertising;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    private static Stage primaryStage;
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 700;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("Advertising Company Management System");
        primaryStage.setResizable(false);

        showLoginPage();

        primaryStage.show();
    }

    public static void showLoginPage() {
        try {
            Parent root = FXMLLoader.load(MainApp.class.getResource("/fxml/Login.fxml"));
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            scene.getStylesheets().add(MainApp.class.getResource("/css/style.css").toExternalForm());
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showDashboard() {
        try {
            Parent root = FXMLLoader.load(MainApp.class.getResource("/fxml/Dashboard.fxml"));
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            scene.getStylesheets().add(MainApp.class.getResource("/css/style.css").toExternalForm());
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
