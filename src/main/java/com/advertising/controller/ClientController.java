package com.advertising.controller;

import com.advertising.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.*;
import java.util.Optional;

public class ClientController {

    private BorderPane view;
    private TableView<Client> clientTable;
    private ObservableList<Client> clientList;
    private TextField searchField;
    private ComboBox<String> filterComboBox;
    
    // Bouton d'ajout
    private Button addButton;
    
    // Pour les filtres
    private static final String ALL = "Tous les clients";
    private static final String WITH_SERVICES = "Avec services";
    private static final String WITHOUT_SERVICES = "Sans services";

    public ClientController() {
        createView();
        loadClients();
    }

    public BorderPane getView() {
        return view;
    }

    private void createView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header
        VBox headerBox = createHeader();
        root.setTop(headerBox);

        // Table
        VBox tableBox = createTable();
        root.setCenter(tableBox);

        this.view = root;
    }

    private VBox createHeader() {
        VBox headerBox = new VBox(15);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        // Titre et bouton d'ajout
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Text title = new Text("👥 Gestion des Clients");
        title.setFont(Font.font("System Bold", 28));
        title.setStyle("-fx-fill: #2c3e50;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        addButton = new Button("➕ Ajouter Client");
        addButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 10 20; -fx-font-weight: bold;");
        addButton.setOnAction(e -> showAddClientDialog());
        
        titleBox.getChildren().addAll(title, spacer, addButton);
        
        // Barre de recherche intelligente
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        searchField = new TextField();
        searchField.setPromptText("Rechercher par nom, téléphone ou email... (recherche en temps réel)");
        searchField.setPrefWidth(400);
        searchField.setStyle("-fx-padding: 10; -fx-background-radius: 5;");
        
        // Écouteur pour recherche en temps réel
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchClients();
        });
        
        Button clearSearchButton = new Button("🗑️ Effacer");
        clearSearchButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 10 20;");
        clearSearchButton.setOnAction(e -> {
            searchField.clear();
            filterComboBox.setValue(ALL);
            loadClients();
        });
        
        // Filtres avancés
        Label filterLabel = new Label("Filtrer:");
        filterLabel.setFont(Font.font("System Bold", 12));
        
        filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll(ALL, WITH_SERVICES, WITHOUT_SERVICES);
        filterComboBox.setValue(ALL);
        filterComboBox.setStyle("-fx-padding: 8; -fx-background-radius: 5;");
        filterComboBox.setOnAction(e -> filterClients());
        
        searchBox.getChildren().addAll(searchField, clearSearchButton, filterLabel, filterComboBox);
        
        // Statistiques
        HBox statsBox = createStatsBox();
        
        headerBox.getChildren().addAll(titleBox, searchBox, statsBox);
        return headerBox;
    }

    private HBox createStatsBox() {
        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Total clients
            String totalQuery = "SELECT COUNT(*) as total FROM client";
            PreparedStatement totalStmt = conn.prepareStatement(totalQuery);
            ResultSet totalRs = totalStmt.executeQuery();
            int totalClients = totalRs.next() ? totalRs.getInt("total") : 0;
            
            // Clients avec services
            String withServicesQuery = "SELECT COUNT(DISTINCT id_client) as count FROM service";
            PreparedStatement servicesStmt = conn.prepareStatement(withServicesQuery);
            ResultSet servicesRs = servicesStmt.executeQuery();
            int clientsWithServices = servicesRs.next() ? servicesRs.getInt("count") : 0;
            
            totalRs.close();
            totalStmt.close();
            servicesRs.close();
            servicesStmt.close();
            
            Label totalLabel = createStatLabel("👥 Total Clients", String.valueOf(totalClients), "#3498db");
            Label activeLabel = createStatLabel("📊 Avec Services", String.valueOf(clientsWithServices), "#2ecc71");
            Label inactiveLabel = createStatLabel("⏳ Sans Services", String.valueOf(totalClients - clientsWithServices), "#e74c3c");
            
            statsBox.getChildren().addAll(totalLabel, activeLabel, inactiveLabel);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return statsBox;
    }

    private Label createStatLabel(String title, String value, String color) {
        VBox statBox = new VBox(5);
        statBox.setAlignment(Pos.CENTER_LEFT);
        statBox.setPadding(new Insets(10, 15, 10, 15));
        statBox.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 8;");
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font(11));
        titleLabel.setStyle("-fx-text-fill: white;");
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System Bold", 18));
        valueLabel.setStyle("-fx-text-fill: white;");
        
        statBox.getChildren().addAll(titleLabel, valueLabel);
        
        Label container = new Label();
        container.setGraphic(statBox);
        return container;
    }

    private VBox createTable() {
        VBox tableBox = new VBox(10);
        tableBox.setPadding(new Insets(0));
        
        // Création du tableau
        clientTable = new TableView<>();
        clientTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        clientTable.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");
        clientTable.setPrefHeight(600);
        
        // Colonnes
        TableColumn<Client, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        
        TableColumn<Client, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nomCol.setPrefWidth(200);
        
        TableColumn<Client, String> phoneCol = new TableColumn<>("Téléphone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        phoneCol.setPrefWidth(150);
        
        TableColumn<Client, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(250);
        
        TableColumn<Client, String> adresseCol = new TableColumn<>("Adresse");
        adresseCol.setCellValueFactory(new PropertyValueFactory<>("adresseShort"));
        adresseCol.setPrefWidth(200);
        
        TableColumn<Client, String> servicesCol = new TableColumn<>("Services");
        servicesCol.setCellValueFactory(new PropertyValueFactory<>("nombreServices"));
        servicesCol.setPrefWidth(100);
        
        TableColumn<Client, String> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<Client, String>() {
            private final Button detailsButton = new Button("👁️ Détails");
            private final Button editButton = new Button("✏️ Modifier");
            private final Button deleteButton = new Button("🗑️ Supprimer");
            private final HBox pane = new HBox(5, detailsButton, editButton, deleteButton);
            
            {
                // Style des boutons
                detailsButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10;");
                editButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10;");
                
                // Actions
                detailsButton.setOnAction(event -> {
                    Client client = getTableView().getItems().get(getIndex());
                    showClientDetails(client);
                });
                
                editButton.setOnAction(event -> {
                    Client client = getTableView().getItems().get(getIndex());
                    showEditClientDialog(client);
                });
                
                deleteButton.setOnAction(event -> {
                    Client client = getTableView().getItems().get(getIndex());
                    deleteClient(client);
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        
        clientTable.getColumns().addAll(idCol, nomCol, phoneCol, emailCol, adresseCol, servicesCol, actionsCol);
        
        // Bouton d'actualisation
        HBox tableButtons = new HBox(10);
        
        Button refreshButton = new Button("🔄 Actualiser");
        refreshButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 10 20;");
        refreshButton.setOnAction(e -> loadClients());
        
        Button exportButton = new Button("📊 Exporter CSV");
        exportButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 10 20;");
        exportButton.setOnAction(e -> exportClients());
        
        tableButtons.getChildren().addAll(refreshButton, exportButton);
        
        tableBox.getChildren().addAll(clientTable, tableButtons);
        VBox.setVgrow(clientTable, Priority.ALWAYS);
        
        return tableBox;
    }

    private void loadClients() {
        clientList = FXCollections.observableArrayList();
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT c.*, COUNT(s.id_service) as nombre_services " +
                          "FROM client c " +
                          "LEFT JOIN service s ON c.id_client = s.id_client " +
                          "GROUP BY c.id_client " +
                          "ORDER BY c.nom";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Client client = new Client(
                    rs.getInt("id_client"),
                    rs.getString("nom"),
                    rs.getString("telephone"),
                    rs.getString("email"),
                    rs.getString("adresse"),
                    rs.getInt("nombre_services")
                );
                clientList.add(client);
            }
            
            clientTable.setItems(clientList);
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des clients: " + e.getMessage());
        }
    }

    private void searchClients() {
        String searchText = searchField.getText().trim().toLowerCase();
        
        if (searchText.isEmpty()) {
            filterClients();
            return;
        }
        
        ObservableList<Client> filteredList = FXCollections.observableArrayList();
        
        for (Client client : clientList) {
            // Recherche intelligente : recherche dans tous les champs
            if (client.getNom().toLowerCase().contains(searchText) ||
                (client.getTelephone() != null && client.getTelephone().toLowerCase().contains(searchText)) ||
                (client.getEmail() != null && client.getEmail().toLowerCase().contains(searchText)) ||
                (client.getAdresse() != null && client.getAdresse().toLowerCase().contains(searchText))) {
                filteredList.add(client);
            }
        }
        
        clientTable.setItems(filteredList);
    }

    private void filterClients() {
        String filter = filterComboBox.getValue();
        
        if (filter.equals(ALL)) {
            searchClients(); // Applique la recherche si elle existe
            return;
        }
        
        ObservableList<Client> filteredList = FXCollections.observableArrayList();
        
        for (Client client : clientList) {
            if (filter.equals(WITH_SERVICES) && client.getNombreServices() > 0) {
                filteredList.add(client);
            } else if (filter.equals(WITHOUT_SERVICES) && client.getNombreServices() == 0) {
                filteredList.add(client);
            }
        }
        
        clientTable.setItems(filteredList);
    }

    // ===== DIALOGUES =====

    private void showAddClientDialog() {
        Dialog<Client> dialog = new Dialog<>();
        dialog.setTitle("➕ Ajouter un Nouveau Client");
        dialog.setHeaderText("Remplissez les informations du client");
        
        // Boutons
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nomField = new TextField();
        nomField.setPromptText("Nom complet");
        TextField telephoneField = new TextField();
        telephoneField.setPromptText("0612345678");
        TextField emailField = new TextField();
        emailField.setPromptText("exemple@email.com");
        TextArea adresseArea = new TextArea();
        adresseArea.setPromptText("Adresse complète");
        adresseArea.setPrefRowCount(3);
        
        grid.add(new Label("Nom *:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Téléphone:"), 0, 1);
        grid.add(telephoneField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Adresse:"), 0, 3);
        grid.add(adresseArea, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        // Validation
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nomField.getText().trim().isEmpty()) {
                    showAlert("Erreur", "Le nom est obligatoire !");
                    return null;
                }
                return new Client(-1, nomField.getText().trim(), 
                    telephoneField.getText().trim(), 
                    emailField.getText().trim(), 
                    adresseArea.getText().trim(), 0);
            }
            return null;
        });
        
        Optional<Client> result = dialog.showAndWait();
        
        result.ifPresent(client -> {
            saveClient(client);
        });
    }

    private void showEditClientDialog(Client client) {
        Dialog<Client> dialog = new Dialog<>();
        dialog.setTitle("✏️ Modifier le Client");
        dialog.setHeaderText("Modifiez les informations du client");
        
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nomField = new TextField(client.getNom());
        TextField telephoneField = new TextField(client.getTelephone() != null ? client.getTelephone() : "");
        TextField emailField = new TextField(client.getEmail() != null ? client.getEmail() : "");
        TextArea adresseArea = new TextArea(client.getAdresse() != null ? client.getAdresse() : "");
        adresseArea.setPrefRowCount(3);
        
        grid.add(new Label("ID:"), 0, 0);
        grid.add(new Label(String.valueOf(client.getId())), 1, 0);
        grid.add(new Label("Nom *:"), 0, 1);
        grid.add(nomField, 1, 1);
        grid.add(new Label("Téléphone:"), 0, 2);
        grid.add(telephoneField, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Adresse:"), 0, 4);
        grid.add(adresseArea, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nomField.getText().trim().isEmpty()) {
                    showAlert("Erreur", "Le nom est obligatoire !");
                    return null;
                }
                return new Client(client.getId(), nomField.getText().trim(), 
                    telephoneField.getText().trim(), 
                    emailField.getText().trim(), 
                    adresseArea.getText().trim(), client.getNombreServices());
            }
            return null;
        });
        
        Optional<Client> result = dialog.showAndWait();
        
        result.ifPresent(updatedClient -> {
            updateClient(updatedClient);
        });
    }

    private void showClientDetails(Client client) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("👁️ Détails du Client");
        dialog.setHeaderText("Informations détaillées du client");
        
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");
        
        // Informations principales
        VBox infoBox = new VBox(10);
        infoBox.setStyle("-fx-padding: 15; -fx-background-color: #ecf0f1; -fx-background-radius: 10;");
        
        Label idLabel = new Label("ID: " + client.getId());
        idLabel.setFont(Font.font("System Bold", 14));
        
        Label nomLabel = new Label("Nom: " + client.getNom());
        nomLabel.setFont(Font.font("System Bold", 16));
        nomLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        Label telLabel = new Label("Téléphone: " + (client.getTelephone() != null ? client.getTelephone() : "Non renseigné"));
        Label emailLabel = new Label("Email: " + (client.getEmail() != null ? client.getEmail() : "Non renseigné"));
        Label servicesLabel = new Label("Nombre de services: " + client.getNombreServices());
        servicesLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        
        infoBox.getChildren().addAll(idLabel, nomLabel, telLabel, emailLabel, servicesLabel);
        
        // Adresse
        if (client.getAdresse() != null && !client.getAdresse().isEmpty()) {
            VBox adresseBox = new VBox(5);
            adresseBox.setStyle("-fx-padding: 15; -fx-background-color: #f8f9fa; -fx-background-radius: 10;");
            
            Label adresseTitle = new Label("Adresse:");
            adresseTitle.setFont(Font.font("System Bold", 14));
            
            TextArea adresseText = new TextArea(client.getAdresse());
            adresseText.setEditable(false);
            adresseText.setWrapText(true);
            adresseText.setPrefRowCount(4);
            adresseText.setStyle("-fx-background-color: transparent; -fx-border-color: #bdc3c7;");
            
            adresseBox.getChildren().addAll(adresseTitle, adresseText);
            content.getChildren().add(adresseBox);
        }
        
        // Liste des services du client
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT s.*, ts.nom_type FROM service s " +
                          "JOIN type_service ts ON s.id_type_service = ts.id_type_service " +
                          "WHERE s.id_client = ? ORDER BY s.date_creation DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, client.getId());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                VBox servicesBox = new VBox(10);
                servicesBox.setStyle("-fx-padding: 15; -fx-background-color: #fff8e1; -fx-background-radius: 10;");
                
                Label servicesTitle = new Label("Services du client:");
                servicesTitle.setFont(Font.font("System Bold", 14));
                servicesTitle.setStyle("-fx-text-fill: #f39c12;");
                
                servicesBox.getChildren().add(servicesTitle);
                
                do {
                    HBox serviceItem = new HBox(10);
                    serviceItem.setStyle("-fx-padding: 8; -fx-background-color: white; -fx-background-radius: 5;");
                    
                    Label serviceDesc = new Label(rs.getString("description"));
                    Label serviceType = new Label(rs.getString("nom_type"));
                    serviceType.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                    Label servicePrix = new Label(String.format("%,.0f MAD", rs.getDouble("prix_total")));
                    servicePrix.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    Label serviceStatut = new Label(rs.getString("statut"));
                    
                    // Couleur selon statut
                    switch (rs.getString("statut")) {
                        case "EN_ATTENTE":
                            serviceStatut.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        case "EN_COURS":
                            serviceStatut.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                        case "TERMINE":
                            serviceStatut.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            break;
                    }
                    
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    
                    serviceItem.getChildren().addAll(serviceDesc, spacer, serviceType, servicePrix, serviceStatut);
                    servicesBox.getChildren().add(serviceItem);
                } while (rs.next());
                
                content.getChildren().add(servicesBox);
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        content.getChildren().add(0, infoBox);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        
        dialog.getDialogPane().setContent(scrollPane);
        
        // Redimensionner la fenêtre
        dialog.getDialogPane().setPrefSize(600, 500);
        
        dialog.showAndWait();
    }

    private void saveClient(Client client) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "INSERT INTO client (nom, telephone, email, adresse) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setString(1, client.getNom());
            stmt.setString(2, client.getTelephone());
            stmt.setString(3, client.getEmail());
            stmt.setString(4, client.getAdresse());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    showAlert("Succès", "Client ajouté avec succès ! ID: " + rs.getInt(1));
                }
                rs.close();
                
                loadClients();
            }
            
            stmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    private void updateClient(Client client) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "UPDATE client SET nom = ?, telephone = ?, email = ?, adresse = ? WHERE id_client = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            
            stmt.setString(1, client.getNom());
            stmt.setString(2, client.getTelephone());
            stmt.setString(3, client.getEmail());
            stmt.setString(4, client.getAdresse());
            stmt.setInt(5, client.getId());
            
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            if (rowsAffected > 0) {
                showAlert("Succès", "Client modifié avec succès !");
                loadClients();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    private void deleteClient(Client client) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("🗑️ Confirmation de suppression");
        alert.setHeaderText("Supprimer le client ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer le client :\n\n" +
                           "Nom: " + client.getNom() + "\n" +
                           "ID: " + client.getId() + "\n\n" +
                           "Cette action est irréversible !");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                String query = "DELETE FROM client WHERE id_client = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, client.getId());
                
                int rowsAffected = stmt.executeUpdate();
                stmt.close();
                
                if (rowsAffected > 0) {
                    showAlert("Succès", "Client supprimé avec succès !");
                    loadClients();
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    private void exportClients() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM client ORDER BY nom";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            StringBuilder csv = new StringBuilder();
            csv.append("ID,Nom,Téléphone,Email,Adresse\n");
            
            while (rs.next()) {
                csv.append(rs.getInt("id_client")).append(",");
                csv.append("\"").append(rs.getString("nom")).append("\",");
                csv.append("\"").append(rs.getString("telephone") != null ? rs.getString("telephone") : "").append("\",");
                csv.append("\"").append(rs.getString("email") != null ? rs.getString("email") : "").append("\",");
                csv.append("\"").append(rs.getString("adresse") != null ? rs.getString("adresse").replace("\"", "\"\"") : "").append("\"\n");
            }
            
            System.out.println("=== Export CSV des Clients ===");
            System.out.println(csv.toString());
            
            showAlert("Export Réussi", "Les données ont été exportées dans la console.\n" +
                                     "Total clients: " + clientList.size());
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'export: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Classe interne Client améliorée
    public static class Client {
        private final int id;
        private final String nom;
        private final String telephone;
        private final String email;
        private final String adresse;
        private final int nombreServices;
        
        public Client(int id, String nom, String telephone, String email, String adresse, int nombreServices) {
            this.id = id;
            this.nom = nom;
            this.telephone = telephone;
            this.email = email;
            this.adresse = adresse;
            this.nombreServices = nombreServices;
        }
        
        public int getId() { return id; }
        public String getNom() { return nom; }
        public String getTelephone() { return telephone; }
        public String getEmail() { return email; }
        public String getAdresse() { return adresse; }
        public int getNombreServices() { return nombreServices; }
        
        // Méthode pour afficher l'adresse raccourcie dans le tableau
        public String getAdresseShort() {
            if (adresse == null || adresse.isEmpty()) return "Non renseignée";
            if (adresse.length() > 30) {
                return adresse.substring(0, 27) + "...";
            }
            return adresse;
        }
        
        @Override
        public String toString() {
            return String.format("Client[ID=%d, Nom=%s, Services=%d]", id, nom, nombreServices);
        }
    }
}