package com.advertising.controller;

import com.advertising.MainApp;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DashboardController {

    private BorderPane view;
    private Label totalServicesLabel;
    private Label totalSalesLabel;
    private Label activeClientsLabel;
    private ProgressBar servicesProgress;
    private ProgressBar satisfactionProgress;
    private ProgressBar revenueProgress;

    public DashboardController() {
        createView();
        loadDashboardData();
    }

    public BorderPane getView() {
        return view;
    }

    private void createView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("dashboard-root");

        // Top Navigation Bar
        HBox navbar = createNavbar();
        root.setTop(navbar);

        // Left Sidebar Menu
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        // Main Content Area
        VBox contentArea = createContentArea();
        root.setCenter(contentArea);

        this.view = root;
    }

    private HBox createNavbar() {
        HBox navbar = new HBox(20);
        navbar.setAlignment(Pos.CENTER_LEFT);
        navbar.setPadding(new Insets(15, 20, 15, 20));
        navbar.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");

        Text title = new Text("🎯 Advertising Management");
        title.setFont(Font.font("System Bold", 20));
        title.setStyle("-fx-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLabel = new Label("Welcome, Admin");
        userLabel.setFont(Font.font(14));
        userLabel.setStyle("-fx-text-fill: white;");

        Button logoutButton = new Button("Logout");
        logoutButton.setFont(Font.font(12));
        logoutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5;");
        logoutButton.setOnAction(e -> handleLogout());

        navbar.getChildren().addAll(title, spacer, userLabel, logoutButton);
        return navbar;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setMinWidth(200);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setStyle("-fx-background-color: #34495e;");

        Label header = new Label("NAVIGATION");
        header.setFont(Font.font("System Bold", 11));
        header.setStyle("-fx-text-fill: #95a5a6; -fx-padding: 0 0 10 0;");

        Button dashboardBtn = createMenuButton("📊 Dashboard", true);
        Button clientsBtn = createMenuButton("👥 Clients", false);
        clientsBtn.setOnAction(e -> handleClientsClick());
        
        Button servicesBtn = createMenuButton("📦 Services", false);
        servicesBtn.setOnAction(e -> handleServicesClick());
        
        Button suppliersBtn = createMenuButton("🏭 Suppliers", false);
        suppliersBtn.setOnAction(e -> handleSuppliersClick());
        
        Button paymentsBtn = createMenuButton("💰 Payments", false);
        paymentsBtn.setOnAction(e -> handlePaymentsClick());

        sidebar.getChildren().addAll(header, dashboardBtn, clientsBtn, servicesBtn, suppliersBtn, paymentsBtn);
        return sidebar;
    }

    private Button createMenuButton(String text, boolean active) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setPadding(new Insets(12, 15, 12, 15));
        
        if (active) {
            button.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; -fx-background-radius: 5;");
        } else {
            button.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-background-radius: 5;");
            button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-background-radius: 5;"));
            button.setOnMouseExited(e -> button.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-background-radius: 5;"));
        }
        
        return button;
    }

    private VBox createContentArea() {
        VBox contentArea = new VBox(20);
        contentArea.setPadding(new Insets(30));
        contentArea.setStyle("-fx-background-color: #f8f9fa;");

        // Title
        Text pageTitle = new Text("Dashboard Overview");
        pageTitle.setFont(Font.font("System Bold", 28));

        // Statistics Cards
        HBox statsBox = createStatsCards();
        
        // Performance Section
        VBox performanceSection = createPerformanceSection();

        contentArea.getChildren().addAll(pageTitle, statsBox, performanceSection);
        return contentArea;
    }

    private HBox createStatsCards() {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        // Total Services Card
        VBox servicesCard = createStatCard("📦", "Total Services", "5");
        totalServicesLabel = (Label) servicesCard.getChildren().get(1);

        // Total Sales Card
        VBox salesCard = createStatCard("💰", "Total Sales", "$15,000");
        totalSalesLabel = (Label) salesCard.getChildren().get(1);

        // Active Clients Card
        VBox clientsCard = createStatCard("👥", "Active Clients", "3");
        activeClientsLabel = (Label) clientsCard.getChildren().get(1);

        statsBox.getChildren().addAll(servicesCard, salesCard, clientsCard);
        return statsBox;
    }

    private VBox createStatCard(String icon, String label, String value) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(25, 20, 25, 20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        HBox.setHgrow(card, Priority.ALWAYS);

        Text iconText = new Text(icon);
        iconText.setStyle("-fx-font-size: 36;");

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System Bold", 32));

        Label titleLabel = new Label(label);
        titleLabel.setFont(Font.font(14));
        titleLabel.setStyle("-fx-text-fill: #666;");

        card.getChildren().addAll(iconText, valueLabel, titleLabel);
        return card;
    }

    private VBox createPerformanceSection() {
        VBox performanceSection = new VBox(15);
        performanceSection.setPadding(new Insets(20, 25, 20, 25));
        performanceSection.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Label sectionTitle = new Label("Performance Overview");
        sectionTitle.setFont(Font.font("System Bold", 18));

        // Services Progress
        VBox servicesProgressBox = createProgressBox("Services Completed", "80%");
        servicesProgress = (ProgressBar) servicesProgressBox.getChildren().get(1);

        // Client Satisfaction Progress
        VBox satisfactionProgressBox = createProgressBox("Client Satisfaction", "95%");
        satisfactionProgress = (ProgressBar) satisfactionProgressBox.getChildren().get(1);

        // Revenue Goal Progress
        VBox revenueProgressBox = createProgressBox("Revenue Goal", "65%");
        revenueProgress = (ProgressBar) revenueProgressBox.getChildren().get(1);

        performanceSection.getChildren().addAll(sectionTitle, servicesProgressBox, satisfactionProgressBox, revenueProgressBox);
        return performanceSection;
    }

    private VBox createProgressBox(String label, String percentage) {
        VBox progressBox = new VBox(8);

        HBox labelBox = new HBox(10);
        labelBox.setAlignment(Pos.CENTER_LEFT);

        Label progressLabel = new Label(label);
        progressLabel.setFont(Font.font(13));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label progressPercentage = new Label(percentage);
        progressPercentage.setFont(Font.font("System Bold", 13));

        labelBox.getChildren().addAll(progressLabel, spacer, progressPercentage);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: #3498db; -fx-pref-height: 10;");

        progressBox.getChildren().addAll(labelBox, progressBar);
        return progressBox;
    }

    private void loadDashboardData() {
        // Données statiques (comme avant)
        int totalServices = 5;
        double totalSales = 15000.0;
        int activeClients = 3;

        if (totalServicesLabel != null) {
            totalServicesLabel.setText(String.valueOf(totalServices));
            totalSalesLabel.setText(String.format("$%,.0f", totalSales));
            activeClientsLabel.setText(String.valueOf(activeClients));

            servicesProgress.setProgress(0.8);
            satisfactionProgress.setProgress(0.95);
            revenueProgress.setProgress(0.65);
        }
    }

    // Handlers pour les boutons
    private void handleLogout() {
        MainApp.showLoginPage();
    }

    private void handleClientsClick() {
        Stage currentStage = (Stage) view.getScene().getWindow();
        
        ClientController clientController = new ClientController();
        Scene clientScene = new Scene(clientController.getView(), 1200, 800);
        
        Stage clientStage = new Stage();
        clientStage.setTitle("👥 Gestion des Clients");
        clientStage.setScene(clientScene);
        configurerFenetre(clientStage);
        clientStage.show();  
    }

    private void handleServicesClick() {
        Stage currentStage = (Stage) view.getScene().getWindow();
        
        ServiceController serviceController = new ServiceController();
        Scene serviceScene = new Scene(serviceController.getView(), 1400, 800);
        
        Stage serviceStage = new Stage();
        serviceStage.setTitle("📦 Gestion des Services");
        serviceStage.setScene(serviceScene);
        configurerFenetre(serviceStage);
        serviceStage.show();
    }

    private void handleSuppliersClick() {
        Stage currentStage = (Stage) view.getScene().getWindow();
        
        FournisseurController fournisseurController = new FournisseurController();
        Scene fournisseurScene = new Scene(fournisseurController.getView(), 1200, 800);
        
        Stage fournisseurStage = new Stage();
        fournisseurStage.setTitle("🏭 Gestion des Fournisseurs");
        fournisseurStage.setScene(fournisseurScene);
        configurerFenetre(fournisseurStage);
        fournisseurStage.show();
    }
    private void configurerFenetre(Stage stage) {
    // Permettre le redimensionnement
    stage.setResizable(true);
    
    // Mettre en plein écran
    stage.setMaximized(true);
    
    // Ou si vous préférez maximisé mais pas plein écran :
    // stage.setMaximized(true);
    
    // Ajouter un gestionnaire pour fermer la fenêtre avec la croix
    stage.setOnCloseRequest(event -> {
        // Vous pouvez ajouter des vérifications ici si besoin
        // Par exemple, sauvegarder des données avant fermeture
        System.out.println("Fermeture de la fenêtre: " + stage.getTitle());
    });
}
    private void handlePaymentsClick() {
        showAlert("Payments Module", "Payments page is not yet implemented.");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}