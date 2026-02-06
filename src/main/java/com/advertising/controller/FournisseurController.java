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
import java.sql.*;
import java.util.Optional;

public class FournisseurController {

    private BorderPane view;
    private TableView<Fournisseur> fournisseurTable;
    private ObservableList<Fournisseur> fournisseurList;
    private TextField searchField;
    
    // Formulaire
    private TextField nomField;
    private TextField telephoneField;
    private TextField emailField;
    private TextArea adresseArea;
    private Button saveButton;
    private Button clearButton;
    
    private int currentFournisseurId = -1;

    public FournisseurController() {
        createView();
        loadFournisseurs();
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

        // Content
        HBox contentBox = new HBox(20);
        contentBox.setAlignment(Pos.TOP_LEFT);
        
        VBox formBox = createForm();
        VBox tableBox = createTable();
        
        contentBox.getChildren().addAll(formBox, tableBox);
        root.setCenter(contentBox);

        this.view = root;
    }

    private VBox createHeader() {
        VBox headerBox = new VBox(15);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        Text title = new Text("🏭 Gestion des Fournisseurs");
        title.setFont(Font.font("System Bold", 28));
        title.setStyle("-fx-fill: #2c3e50;");

        // Barre de recherche
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        searchField = new TextField();
        searchField.setPromptText("Rechercher par nom, téléphone ou email...");
        searchField.setPrefWidth(400);
        searchField.setStyle("-fx-padding: 10; -fx-background-radius: 5;");
        
        Button searchButton = new Button("🔍 Rechercher");
        searchButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 10 20;");
        searchButton.setOnAction(e -> searchFournisseurs());
        
        Button clearSearchButton = new Button("🔄 Réinitialiser");
        clearSearchButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 10 20;");
        clearSearchButton.setOnAction(e -> {
            searchField.clear();
            loadFournisseurs();
        });
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        searchBox.getChildren().addAll(searchField, searchButton, clearSearchButton, spacer);
        
        headerBox.getChildren().addAll(title, searchBox);
        return headerBox;
    }

    private VBox createForm() {
        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(20));
        formBox.setPrefWidth(400);
        formBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        
        Text formTitle = new Text("✏️ Informations Fournisseur");
        formTitle.setFont(Font.font("System Bold", 18));
        formTitle.setStyle("-fx-fill: #2c3e50;");
        
        VBox fieldsBox = new VBox(10);
        
        // Nom
        VBox nomBox = new VBox(5);
        Label nomLabel = new Label("Nom *");
        nomLabel.setFont(Font.font("System Bold", 12));
        nomField = new TextField();
        nomField.setPromptText("Entrez le nom du fournisseur");
        nomBox.getChildren().addAll(nomLabel, nomField);
        
        // Téléphone
        VBox phoneBox = new VBox(5);
        Label phoneLabel = new Label("Téléphone");
        phoneLabel.setFont(Font.font("System Bold", 12));
        telephoneField = new TextField();
        telephoneField.setPromptText("Ex: 0612345678");
        phoneBox.getChildren().addAll(phoneLabel, telephoneField);
        
        // Email
        VBox emailBox = new VBox(5);
        Label emailLabel = new Label("Email");
        emailLabel.setFont(Font.font("System Bold", 12));
        emailField = new TextField();
        emailField.setPromptText("fournisseur@email.com");
        emailBox.getChildren().addAll(emailLabel, emailField);
        
        // Adresse
        VBox adresseBox = new VBox(5);
        Label adresseLabel = new Label("Adresse");
        adresseLabel.setFont(Font.font("System Bold", 12));
        adresseArea = new TextArea();
        adresseArea.setPromptText("Adresse complète");
        adresseArea.setPrefRowCount(3);
        adresseArea.setStyle("-fx-padding: 8; -fx-background-radius: 5;");
        adresseBox.getChildren().addAll(adresseLabel, adresseArea);
        
        fieldsBox.getChildren().addAll(nomBox, phoneBox, emailBox, adresseBox);
        
        // Boutons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        
        saveButton = new Button("💾 Enregistrer");
        saveButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 12 30; -fx-font-weight: bold;");
        saveButton.setOnAction(e -> saveFournisseur());
        
        clearButton = new Button("🗑️ Nouveau");
        clearButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 12 30;");
        clearButton.setOnAction(e -> clearForm());
        
        buttonsBox.getChildren().addAll(saveButton, clearButton);
        
        Label infoLabel = new Label("* Champ obligatoire");
        infoLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11; -fx-font-style: italic;");
        
        formBox.getChildren().addAll(formTitle, fieldsBox, buttonsBox, infoLabel);
        return formBox;
    }

    private VBox createTable() {
        VBox tableBox = new VBox(15);
        tableBox.setPadding(new Insets(0));
        
        Text tableTitle = new Text("📋 Liste des Fournisseurs");
        tableTitle.setFont(Font.font("System Bold", 18));
        tableTitle.setStyle("-fx-fill: #2c3e50;");
        
        fournisseurTable = new TableView<>();
        fournisseurTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        fournisseurTable.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");
        
        // Colonnes
        TableColumn<Fournisseur, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        
        TableColumn<Fournisseur, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nomCol.setPrefWidth(150);
        
        TableColumn<Fournisseur, String> phoneCol = new TableColumn<>("Téléphone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        phoneCol.setPrefWidth(120);
        
        TableColumn<Fournisseur, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(180);
        
        TableColumn<Fournisseur, String> adresseCol = new TableColumn<>("Adresse");
        adresseCol.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        adresseCol.setPrefWidth(200);
        
        TableColumn<Fournisseur, String> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<Fournisseur, String>() {
            private final Button editButton = new Button("✏️");
            private final Button deleteButton = new Button("🗑️");
            private final HBox pane = new HBox(5, editButton, deleteButton);
            
            {
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10;");
                
                editButton.setOnAction(event -> {
                    Fournisseur fournisseur = getTableView().getItems().get(getIndex());
                    editFournisseur(fournisseur);
                });
                
                deleteButton.setOnAction(event -> {
                    Fournisseur fournisseur = getTableView().getItems().get(getIndex());
                    deleteFournisseur(fournisseur);
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        
        fournisseurTable.getColumns().addAll(idCol, nomCol, phoneCol, emailCol, adresseCol, actionsCol);
        
        HBox tableButtons = new HBox(10);
        Button refreshButton = new Button("🔄 Actualiser");
        refreshButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 10 20;");
        refreshButton.setOnAction(e -> loadFournisseurs());
        
        tableButtons.getChildren().addAll(refreshButton);
        
        tableBox.getChildren().addAll(tableTitle, fournisseurTable, tableButtons);
        VBox.setVgrow(fournisseurTable, Priority.ALWAYS);
        
        return tableBox;
    }

    private void loadFournisseurs() {
        fournisseurList = FXCollections.observableArrayList();
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM fournisseur ORDER BY nom";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Fournisseur fournisseur = new Fournisseur(
                    rs.getInt("id_fournisseur"),
                    rs.getString("nom"),
                    rs.getString("telephone"),
                    rs.getString("email"),
                    rs.getString("adresse")
                );
                fournisseurList.add(fournisseur);
            }
            
            fournisseurTable.setItems(fournisseurList);
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des fournisseurs: " + e.getMessage());
        }
    }

    private void searchFournisseurs() {
        String searchText = searchField.getText().trim().toLowerCase();
        
        if (searchText.isEmpty()) {
            loadFournisseurs();
            return;
        }
        
        ObservableList<Fournisseur> filteredList = FXCollections.observableArrayList();
        
        for (Fournisseur fournisseur : fournisseurList) {
            if (fournisseur.getNom().toLowerCase().contains(searchText) ||
                (fournisseur.getTelephone() != null && fournisseur.getTelephone().toLowerCase().contains(searchText)) ||
                (fournisseur.getEmail() != null && fournisseur.getEmail().toLowerCase().contains(searchText))) {
                filteredList.add(fournisseur);
            }
        }
        
        fournisseurTable.setItems(filteredList);
    }

    private void saveFournisseur() {
        if (nomField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le nom est obligatoire !");
            return;
        }
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query;
            PreparedStatement stmt;
            
            if (currentFournisseurId == -1) {
                query = "INSERT INTO fournisseur (nom, telephone, email, adresse) VALUES (?, ?, ?, ?)";
                stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            } else {
                query = "UPDATE fournisseur SET nom = ?, telephone = ?, email = ?, adresse = ? WHERE id_fournisseur = ?";
                stmt = conn.prepareStatement(query);
                stmt.setInt(5, currentFournisseurId);
            }
            
            stmt.setString(1, nomField.getText().trim());
            stmt.setString(2, telephoneField.getText().trim());
            stmt.setString(3, emailField.getText().trim());
            stmt.setString(4, adresseArea.getText().trim());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                if (currentFournisseurId == -1) {
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        currentFournisseurId = rs.getInt(1);
                    }
                    rs.close();
                }
                
                showAlert("Succès", currentFournisseurId == -1 ? 
                    "Fournisseur ajouté avec succès !" : 
                    "Fournisseur modifié avec succès !");
                
                clearForm();
                loadFournisseurs();
            }
            
            stmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
        }
    }

    private void editFournisseur(Fournisseur fournisseur) {
        currentFournisseurId = fournisseur.getId();
        nomField.setText(fournisseur.getNom());
        telephoneField.setText(fournisseur.getTelephone() != null ? fournisseur.getTelephone() : "");
        emailField.setText(fournisseur.getEmail() != null ? fournisseur.getEmail() : "");
        adresseArea.setText(fournisseur.getAdresse() != null ? fournisseur.getAdresse() : "");
        
        saveButton.setText("💾 Mettre à jour");
        saveButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 12 30; -fx-font-weight: bold;");
    }

    private void deleteFournisseur(Fournisseur fournisseur) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le fournisseur ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer : " + fournisseur.getNom() + " ?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                String query = "DELETE FROM fournisseur WHERE id_fournisseur = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, fournisseur.getId());
                
                int rowsAffected = stmt.executeUpdate();
                stmt.close();
                
                if (rowsAffected > 0) {
                    showAlert("Succès", "Fournisseur supprimé avec succès !");
                    loadFournisseurs();
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    private void clearForm() {
        currentFournisseurId = -1;
        nomField.clear();
        telephoneField.clear();
        emailField.clear();
        adresseArea.clear();
        saveButton.setText("💾 Enregistrer");
        saveButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 12 30; -fx-font-weight: bold;");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class Fournisseur {
        private final int id;
        private final String nom;
        private final String telephone;
        private final String email;
        private final String adresse;
        
        public Fournisseur(int id, String nom, String telephone, String email, String adresse) {
            this.id = id;
            this.nom = nom;
            this.telephone = telephone;
            this.email = email;
            this.adresse = adresse;
        }
        
        public int getId() { return id; }
        public String getNom() { return nom; }
        public String getTelephone() { return telephone; }
        public String getEmail() { return email; }
        public String getAdresse() { return adresse; }
        
        @Override
        public String toString() {
            return String.format("Fournisseur[ID=%d, Nom=%s]", id, nom);
        }
    }
}