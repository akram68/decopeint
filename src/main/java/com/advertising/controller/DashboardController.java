package com.advertising.controller;

import com.advertising.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class DashboardController {

    @FXML
    private Label totalServicesLabel;

    @FXML
    private Label totalSalesLabel;

    @FXML
    private Label activeClientsLabel;

    @FXML
    private ProgressBar servicesProgress;

    @FXML
    private ProgressBar satisfactionProgress;

    @FXML
    private ProgressBar revenueProgress;

    @FXML
    public void initialize() {
        loadDashboardData();
    }

    private void loadDashboardData() {
        // ========== STATIC DATA (FOR DEMONSTRATION) ==========
        // This is temporary static data for testing purposes.
        // Replace this with actual database queries when ready.

        int totalServices = 5;
        double totalSales = 15000.0;
        int activeClients = 3;

        totalServicesLabel.setText(String.valueOf(totalServices));
        totalSalesLabel.setText(String.format("$%,.0f", totalSales));
        activeClientsLabel.setText(String.valueOf(activeClients));

        servicesProgress.setProgress(0.8);
        satisfactionProgress.setProgress(0.95);
        revenueProgress.setProgress(0.65);

        // ========== DATABASE INTEGRATION (FUTURE IMPLEMENTATION) ==========
        // When integrating with MySQL/WAMP, replace the above static data
        // with the following database queries:
        //
        // try {
        //     Connection conn = DatabaseConnection.getConnection();
        //
        //     // Query 1: Get total number of services
        //     String servicesQuery = "SELECT COUNT(*) as total FROM services";
        //     PreparedStatement servicesStmt = conn.prepareStatement(servicesQuery);
        //     ResultSet servicesRs = servicesStmt.executeQuery();
        //     if (servicesRs.next()) {
        //         int totalServices = servicesRs.getInt("total");
        //         totalServicesLabel.setText(String.valueOf(totalServices));
        //     }
        //     servicesRs.close();
        //     servicesStmt.close();
        //
        //     // Query 2: Get total sales amount
        //     String salesQuery = "SELECT SUM(amount) as total FROM payments WHERE status = 'completed'";
        //     PreparedStatement salesStmt = conn.prepareStatement(salesQuery);
        //     ResultSet salesRs = salesStmt.executeQuery();
        //     if (salesRs.next()) {
        //         double totalSales = salesRs.getDouble("total");
        //         totalSalesLabel.setText(String.format("$%,.0f", totalSales));
        //     }
        //     salesRs.close();
        //     salesStmt.close();
        //
        //     // Query 3: Get number of active clients
        //     String clientsQuery = "SELECT COUNT(*) as total FROM clients WHERE status = 'active'";
        //     PreparedStatement clientsStmt = conn.prepareStatement(clientsQuery);
        //     ResultSet clientsRs = clientsStmt.executeQuery();
        //     if (clientsRs.next()) {
        //         int activeClients = clientsRs.getInt("total");
        //         activeClientsLabel.setText(String.valueOf(activeClients));
        //     }
        //     clientsRs.close();
        //     clientsStmt.close();
        //
        //     // Query 4: Calculate progress percentages
        //     String completedServicesQuery =
        //         "SELECT (COUNT(CASE WHEN status = 'completed' THEN 1 END) * 1.0 / COUNT(*)) as percentage " +
        //         "FROM services";
        //     PreparedStatement progressStmt = conn.prepareStatement(completedServicesQuery);
        //     ResultSet progressRs = progressStmt.executeQuery();
        //     if (progressRs.next()) {
        //         double completionRate = progressRs.getDouble("percentage");
        //         servicesProgress.setProgress(completionRate);
        //     }
        //     progressRs.close();
        //     progressStmt.close();
        //
        //     // Add similar queries for satisfaction and revenue progress
        //
        // } catch (SQLException e) {
        //     e.printStackTrace();
        //     showAlert("Database Error", "Failed to load dashboard data.");
        // }
    }

    @FXML
    private void handleLogout() {
        MainApp.showLoginPage();
    }

    @FXML
    private void handleClientsClick() {
        showAlert("Clients Module", "Clients page is not yet implemented.\n\n" +
                "This will display client management features including:\n" +
                "- View all clients\n" +
                "- Add new clients\n" +
                "- Edit client information\n" +
                "- Track client history");
    }

    @FXML
    private void handleServicesClick() {
        showAlert("Services Module", "Services page is not yet implemented.\n\n" +
                "This will display service management features including:\n" +
                "- View all advertising services\n" +
                "- Add new services\n" +
                "- Edit service details\n" +
                "- Manage service pricing");
    }

    @FXML
    private void handleSuppliersClick() {
        showAlert("Suppliers Module", "Suppliers page is not yet implemented.\n\n" +
                "This will display supplier management features including:\n" +
                "- View all suppliers\n" +
                "- Add new suppliers\n" +
                "- Edit supplier information\n" +
                "- Track supplier contracts");
    }

    @FXML
    private void handlePaymentsClick() {
        showAlert("Payments Module", "Payments page is not yet implemented.\n\n" +
                "This will display payment management features including:\n" +
                "- View all payments\n" +
                "- Record new payments\n" +
                "- Track payment status\n" +
                "- Generate payment reports");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
