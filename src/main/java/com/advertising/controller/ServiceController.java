package com.advertising.controller;

import com.advertising.util.DatabaseConnection;
import com.advertising.service.ServiceManager;
import com.advertising.service.PdfReportGenerator;
import com.advertising.component.ServiceStatisticsPanel;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar.ButtonData;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;

import javafx.util.StringConverter;

public class ServiceController {

    private BorderPane view;
    private TableView<Service> serviceTable;
    private ObservableList<Service> serviceList = FXCollections.observableArrayList();
    private ObservableList<Service> filteredList = FXCollections.observableArrayList();

    private ComboBox<String> filterClientCombo;
    private ComboBox<String> filterTypeCombo;
    private ComboBox<String> filterPaiementCombo;
    private ComboBox<String> filterServiceCombo;
    private DatePicker dateFromPicker;
    private DatePicker dateToPicker;

    // NEW: Refactored components for better separation of concerns
    private final ServiceManager serviceManager;
    private final PdfReportGenerator pdfGenerator;
    private final ServiceStatisticsPanel statisticsPanel;

    // Formateur de date
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_FORMATTER_PDF = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    public ServiceController() {
        // Initialize new components
        this.serviceManager = new ServiceManager();
        this.pdfGenerator = new PdfReportGenerator();
        this.statisticsPanel = new ServiceStatisticsPanel();

        createView();
        loadServices();
        loadFilterData();
        updateStatistics();
    }

    public BorderPane getView() {
        return view;
    }

    private void createView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #f5f7fa;");

        // ==========================
        // HEADER SECTION
        // ==========================
        Text title = new Text("📋 Gestion des Services");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setFill(Color.web("#2c3e50"));

        Button addButton = createStyledButton("➕ Nouveau Service", "#2ecc71");
        addButton.setOnAction(e -> showAddServiceDialog());

        HBox header = new HBox(15, title, addButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));

        // ==========================
        // FILTERS SECTION
        // ==========================
        VBox filtersSection = createFiltersSection();

        // ==========================
        // TABLE SECTION
        // ==========================
        VBox tableSection = createTableSection();

        // IMPORTANT: Supprimer la hauteur fixe ou la rendre flexible
        serviceTable.setPrefHeight(Region.USE_COMPUTED_SIZE); // Remplacez la hauteur fixe

        // ==========================
        // MAIN LAYOUT
        // ==========================
        VBox mainContent = new VBox(10, header, filtersSection, tableSection);
        VBox.setVgrow(tableSection, Priority.ALWAYS); // Permet à la table de s'étendre

        // Créer un conteneur principal avec le contenu
        VBox centerContent = new VBox(10, mainContent, statisticsPanel);
        VBox.setVgrow(mainContent, Priority.ALWAYS); // Le contenu principal prend l'espace
        VBox.setVgrow(statisticsPanel, Priority.NEVER); // Les statistiques gardent leur taille

        root.setCenter(centerContent);

        view = root;
    }

    private VBox createFiltersSection() {
        // Première ligne de filtres
        HBox firstFilterRow = new HBox(10); // Espacement réduit

        // Filtre client
        filterClientCombo = new ComboBox<>();
        filterClientCombo.setPromptText("Client");
        filterClientCombo.setPrefWidth(150); // Largeur réduite
        filterClientCombo.setOnAction(e -> {
            applyFilters();
            updateStatistics();
        });

        // Filtre type de service
        filterTypeCombo = new ComboBox<>();
        filterTypeCombo.setPromptText("Service");
        filterTypeCombo.setPrefWidth(150);
        filterTypeCombo.setOnAction(e -> {
            applyFilters();
            updateStatistics();
        });

        // Filtre statut paiement
        filterPaiementCombo = new ComboBox<>();
        filterPaiementCombo.getItems().addAll(
                "Tous les statuts paiement",
                "NON_PAYE",
                "PARTIELLEMENT_PAYE",
                "PAYE");
        filterPaiementCombo.setValue("Tous les statuts paiement");
        filterPaiementCombo.setPrefWidth(180); // Largeur réduite
        filterPaiementCombo.setOnAction(e -> {
            applyFilters();
            updateStatistics();
        });

        firstFilterRow.getChildren().addAll(
                new Label("Client:"),
                filterClientCombo,
                new Label("Service:"),
                filterTypeCombo,
                new Label("Paiement:"),
                filterPaiementCombo);
        firstFilterRow.setAlignment(Pos.CENTER_LEFT);

        // Deuxième ligne de filtres
        HBox secondFilterRow = new HBox(10);

        // Filtre statut service
        filterServiceCombo = new ComboBox<>();
        filterServiceCombo.getItems().addAll(
                "Tous les statuts service",
                "EN_ATTENTE",
                "EN_COURS",
                "TERMINE");
        filterServiceCombo.setValue("Tous les statuts service");
        filterServiceCombo.setPrefWidth(150);
        filterServiceCombo.setOnAction(e -> {
            applyFilters();
            updateStatistics();
        });

        // Filtre date (du)
        dateFromPicker = new DatePicker();
        dateFromPicker.setPromptText("Du");
        dateFromPicker.setPrefWidth(130); // Largeur réduite
        dateFromPicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? DATE_ONLY_FORMATTER.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty() ? LocalDate.parse(string, DATE_ONLY_FORMATTER) : null;
            }
        });

        // Filtre date (au)
        dateToPicker = new DatePicker();
        dateToPicker.setPromptText("Au");
        dateToPicker.setPrefWidth(130);
        dateToPicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? DATE_ONLY_FORMATTER.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty() ? LocalDate.parse(string, DATE_ONLY_FORMATTER) : null;
            }
        });

        // Bouton réinitialiser
        Button resetButton = createSmallButton("🔄 Réinitialiser", "#95a5a6");
        resetButton.setOnAction(e -> {
            resetFilters();
            updateStatistics();
        });

        secondFilterRow.getChildren().addAll(
                new Label("Statut service:"),
                filterServiceCombo,
                new Label("Date:"),
                dateFromPicker,
                new Label("à"),
                dateToPicker,
                resetButton);
        secondFilterRow.setAlignment(Pos.CENTER_LEFT);

        // Contrôle des dates
        setupDateControls();

        VBox filtersBox = new VBox(8, firstFilterRow, secondFilterRow); // Espacement réduit
        filtersBox.setPadding(new Insets(10));
        filtersBox.setStyle("-fx-background-color: white; -fx-background-radius: 8;"); // Radius réduit

        return filtersBox;
    }

    private void setupDateControls() {
        // Validation lors de la sélection de date
        dateFromPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dateToPicker.getValue() != null) {
                if (newVal.isAfter(dateToPicker.getValue())) {
                    showError("Erreur de date", "La date 'Du' ne peut pas être après la date 'Au'");
                    dateFromPicker.setValue(oldVal);
                    return;
                }
            }
            applyFilters();
            updateStatistics();
        });

        dateToPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dateFromPicker.getValue() != null) {
                if (newVal.isBefore(dateFromPicker.getValue())) {
                    showError("Erreur de date", "La date 'Au' ne peut pas être avant la date 'Du'");
                    dateToPicker.setValue(oldVal);
                    return;
                }
            }
            applyFilters();
            updateStatistics();
        });
    }

    private VBox createTableSection() {
        serviceTable = new TableView<>();
        serviceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        serviceTable.setStyle("-fx-background-color: white; -fx-background-radius: 8;"); // Hauteur fixe raisonnable

        // Colonnes
        TableColumn<Service, Integer> idCol = new TableColumn<>("#ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50); // Largeur réduite
        idCol.setMinWidth(50);
        idCol.setMaxWidth(60);

        TableColumn<Service, String> clientCol = new TableColumn<>("👤 Client");
        clientCol.setCellValueFactory(new PropertyValueFactory<>("client"));
        clientCol.setPrefWidth(120);
        clientCol.setMinWidth(100);
        clientCol.setMaxWidth(150);

        TableColumn<Service, String> typeCol = new TableColumn<>("🛠️ Service");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(120);
        typeCol.setMinWidth(100);
        typeCol.setMaxWidth(150);

        TableColumn<Service, String> descCol = new TableColumn<>("📝 Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(150);
        descCol.setMinWidth(120);
        descCol.setMaxWidth(200);

        TableColumn<Service, Double> prixCol = new TableColumn<>("💰 Total");
        prixCol.setCellValueFactory(new PropertyValueFactory<>("prixTotal"));
        prixCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f DZD", item)); // Pas de décimales
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });
        prixCol.setPrefWidth(100);
        prixCol.setMinWidth(80);
        prixCol.setMaxWidth(120);

        TableColumn<Service, Double> payeCol = new TableColumn<>("💵 Payé");
        payeCol.setCellValueFactory(new PropertyValueFactory<>("montantPaye"));
        payeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f DZD", item));
                }
            }
        });
        payeCol.setPrefWidth(100);
        payeCol.setMinWidth(80);
        payeCol.setMaxWidth(120);

        TableColumn<Service, Double> resteCol = new TableColumn<>("⚖️ Reste");
        resteCol.setCellValueFactory(new PropertyValueFactory<>("reste"));
        resteCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f DZD", item));
                    if (item > 0) {
                        setTextFill(Color.RED);
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        setTextFill(Color.GREEN);
                    }
                }
            }
        });
        resteCol.setPrefWidth(100);
        resteCol.setMinWidth(80);
        resteCol.setMaxWidth(120);

        TableColumn<Service, String> statutPaiementCol = new TableColumn<>("💳 Paiement");
        statutPaiementCol.setCellValueFactory(new PropertyValueFactory<>("statutPaiement"));
        statutPaiementCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "PAYE":
                            setStyle(
                                    "-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-font-weight: bold; -fx-font-size: 11px;");
                            break;
                        case "PARTIELLEMENT_PAYE":
                            setStyle(
                                    "-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-font-weight: bold; -fx-font-size: 11px;");
                            break;
                        case "NON_PAYE":
                            setStyle(
                                    "-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-font-weight: bold; -fx-font-size: 11px;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
        statutPaiementCol.setPrefWidth(100);
        statutPaiementCol.setMinWidth(90);
        statutPaiementCol.setMaxWidth(120);

        TableColumn<Service, String> statutServiceCol = new TableColumn<>("📊 Statut");
        statutServiceCol.setCellValueFactory(new PropertyValueFactory<>("statutService"));
        statutServiceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "TERMINE":
                            setStyle(
                                    "-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-font-weight: bold; -fx-font-size: 11px;");
                            break;
                        case "EN_COURS":
                            setStyle(
                                    "-fx-background-color: #cce5ff; -fx-text-fill: #004085; -fx-font-weight: bold; -fx-font-size: 11px;");
                            break;
                        case "EN_ATTENTE":
                            setStyle(
                                    "-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-font-weight: bold; -fx-font-size: 11px;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
        statutServiceCol.setPrefWidth(100);
        statutServiceCol.setMinWidth(80);
        statutServiceCol.setMaxWidth(120);

        TableColumn<Service, String> dateCol = new TableColumn<>("📅 Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateFormatted"));
        dateCol.setPrefWidth(120);
        dateCol.setMinWidth(100);
        dateCol.setMaxWidth(150);
        dateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });

        TableColumn<Service, Void> actionsCol = new TableColumn<>("⚙️ Actions");
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final HBox pane = new HBox(6); // Espacement réduit
            private final Button editBtn = createTinyButton("💰", "#1f7c33");
            private final Button deleteBtn = createTinyButton("❌", "#e74c3c");
            private final Button statusBtn = createTinyButton("🔄", "#9b59b6");
            private final Button historyBtn = createTinyButton("📋", "#f39c12");
            private final Button pdfBtn = createTinyButton("📄", "#3498db"); // Nouveau bouton PDF

            {
                pane.getChildren().addAll(editBtn, statusBtn, historyBtn, pdfBtn, deleteBtn);
                pane.setStyle("-fx-padding: 2 0;");
                pane.setAlignment(Pos.CENTER); // ✅ centre les boutons
                pane.setFillHeight(true);
                setAlignment(Pos.CENTER); // ✅ centre le contenu de la cellule
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                editBtn.setOnAction(e -> {
                    Service service = getTableView().getItems().get(getIndex());
                    showUpdatePaymentDialog(service);
                });

                deleteBtn.setOnAction(e -> {
                    Service service = getTableView().getItems().get(getIndex());
                    showDeleteConfirmation(service);
                });

                statusBtn.setOnAction(e -> {
                    Service service = getTableView().getItems().get(getIndex());
                    showUpdateServiceStatusDialog(service);
                });

                historyBtn.setOnAction(e -> {
                    Service service = getTableView().getItems().get(getIndex());
                    showPaymentHistoryDialog(service);
                });

                pdfBtn.setOnAction(e -> {
                    Service service = getTableView().getItems().get(getIndex());
                    generatePaymentReport(service);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
        actionsCol.setPrefWidth(180); // Légèrement augmenté pour le bouton supplémentaire
        actionsCol.setMinWidth(170);
        actionsCol.setMaxWidth(190);

        serviceTable.getColumns().addAll(
                idCol, clientCol, typeCol,
                prixCol, payeCol, resteCol,
                statutPaiementCol, statutServiceCol, dateCol, actionsCol);

        ScrollPane scrollPane = new ScrollPane(serviceTable);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background-radius: 8;");
        scrollPane.setPrefHeight(350); // Hauteur raisonnable

        VBox tableSection = new VBox(5, scrollPane); // Une seule déclaration ici
        tableSection.setPadding(new Insets(5));

        return tableSection;
    }

    // REMOVED: Old createStatisticsSection() and createStatBox() - now using
    // ServiceStatisticsPanel
    // The statistics panel is created once in constructor and updated via
    // updateStatistics()

    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 13px; " + // Taille réduite
                "-fx-padding: 8 15; " + // Padding réduit
                "-fx-background-radius: 6; " + // Radius réduit
                "-fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: derive(" + color + ", -20%); " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 13px; " +
                "-fx-padding: 8 15; " +
                "-fx-background-radius: 6; " +
                "-fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 13px; " +
                "-fx-padding: 8 15; " +
                "-fx-background-radius: 6; " +
                "-fx-cursor: hand;"));
        return btn;
    }

    private Button createSmallButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 11px; " + // Taille réduite
                "-fx-background-radius: 4; " + // Radius réduit
                "-fx-padding: 4 8; " + // Padding réduit
                "-fx-cursor: hand;");
        btn.setMinWidth(60); // Largeur réduite
        return btn;
    }

    private Button createTinyButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 14px; " + // Très petite taille
                "-fx-background-radius: 5; " +
                "-fx-padding: 2 2; " + // Très petit padding
                "-fx-min-width: 30; " + // Largeur minimale réduite
                "-fx-max-width: 30; " + // Largeur maximale réduite
                "-fx-cursor: hand;");
        return btn;
    }

    // ==========================
    // NOUVELLE FONCTIONNALITÉ : GÉNÉRATION PDF
    // ==========================

    private void generatePaymentReport(Service service) {
        try {
            // Load payment history
            ObservableList<Payment> payments = loadPaymentHistory(service.getId());

            // Get client details
            ServiceManager.ClientDetails clientDetails = serviceManager.getClientDetails(service.getClient());

            // Generate professional PDF invoice
            File pdfFile = pdfGenerator.generateServiceInvoice(service, payments, clientDetails);

            // Open the PDF file
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            } else {
                showInfo("Fichier PDF", "Le fichier PDF a été créé : " + pdfFile.getAbsolutePath());
            }

            showInfo("Facture générée", "✅ La facture a été générée avec succès !\nFichier : " + pdfFile.getName());

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de générer la facture : " + e.getMessage());
        }
    }

    // ==========================
    // CHARGEMENT DES DONNÉES
    // ==========================
    private void loadServices() {
        serviceList.clear();

        String sql = """
                    SELECT s.id_service, c.nom AS client,
                           ts.nom_type AS type_service,
                           s.description,
                           s.prix_total, s.montant_paye,
                           s.reste_a_payer, s.etat_paiement,
                           s.statut_service,
                           s.date_creation
                    FROM service s
                    JOIN client c ON s.id_client = c.id_client
                    JOIN type_service ts ON s.id_type_service = ts.id_type_service
                    ORDER BY s.date_creation DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                serviceList.add(new Service(
                        rs.getInt("id_service"),
                        rs.getString("client"),
                        rs.getString("type_service"),
                        rs.getString("description"),
                        rs.getDouble("prix_total"),
                        rs.getDouble("montant_paye"),
                        rs.getDouble("reste_a_payer"),
                        rs.getString("etat_paiement"),
                        rs.getString("statut_service"),
                        rs.getTimestamp("date_creation")));
            }

            filteredList.setAll(serviceList);
            serviceTable.setItems(filteredList);
            updateStatistics();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les services: " + e.getMessage());
        }
    }

    private void loadFilterData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Charger les clients
            ResultSet rs = conn.prepareStatement(
                    "SELECT DISTINCT nom FROM client ORDER BY nom").executeQuery();
            filterClientCombo.getItems().clear();
            filterClientCombo.getItems().add("Tous les clients");
            while (rs.next()) {
                filterClientCombo.getItems().add(rs.getString("nom"));
            }
            filterClientCombo.setValue("Tous les clients");

            // Charger les types de service
            rs = conn.prepareStatement(
                    "SELECT DISTINCT nom_type FROM type_service ORDER BY nom_type").executeQuery();
            filterTypeCombo.getItems().clear();
            filterTypeCombo.getItems().add("Tous les services");
            while (rs.next()) {
                filterTypeCombo.getItems().add(rs.getString("nom_type"));
            }
            filterTypeCombo.setValue("Tous les services");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==========================
    // FILTRES
    // ==========================
    private void applyFilters() {
        String selectedClient = filterClientCombo.getValue();
        String selectedType = filterTypeCombo.getValue();
        String selectedPaiement = filterPaiementCombo.getValue();
        String selectedService = filterServiceCombo.getValue();
        LocalDate dateFrom = dateFromPicker.getValue();
        LocalDate dateTo = dateToPicker.getValue();

        filteredList.setAll(serviceList.filtered(service -> {
            // Filtre client
            if (selectedClient != null && !selectedClient.equals("Tous les clients")) {
                if (!service.getClient().equals(selectedClient))
                    return false;
            }

            // Filtre type de service
            if (selectedType != null && !selectedType.equals("Tous les services")) {
                if (!service.getType().equals(selectedType))
                    return false;
            }

            // Filtre statut paiement
            if (selectedPaiement != null && !selectedPaiement.equals("Tous les statuts paiement")) {
                if (!service.getStatutPaiement().equals(selectedPaiement))
                    return false;
            }

            // Filtre statut service
            if (selectedService != null && !selectedService.equals("Tous les statuts service")) {
                if (!service.getStatutService().equals(selectedService))
                    return false;
            }

            // Filtre par date
            if (dateFrom != null || dateTo != null) {
                Timestamp dateCreation = service.getDateCreation();
                if (dateCreation == null)
                    return false;

                LocalDateTime serviceDate = dateCreation.toLocalDateTime();
                LocalDate serviceLocalDate = serviceDate.toLocalDate();

                if (dateFrom != null && serviceLocalDate.isBefore(dateFrom)) {
                    return false;
                }

                if (dateTo != null && serviceLocalDate.isAfter(dateTo)) {
                    return false;
                }
            }

            return true;
        }));
    }

    private void resetFilters() {
        filterClientCombo.setValue("Tous les clients");
        filterTypeCombo.setValue("Tous les services");
        filterPaiementCombo.setValue("Tous les statuts paiement");
        filterServiceCombo.setValue("Tous les statuts service");
        dateFromPicker.setValue(null);
        dateToPicker.setValue(null);
        applyFilters();
    }

    // ==========================
    // STATISTIQUES - NEW: Using ServiceStatisticsPanel with Progress Bar
    // ==========================
    private void updateStatistics() {
        // Use filtered data if available, otherwise use full dataset
        ObservableList<Service> dataToAnalyze = (filteredList != null && !filteredList.isEmpty()) ? filteredList
                : serviceList;

        // Create statistics object (calculates totals and percentages)
        ServiceManager.ServiceStatistics stats = new ServiceManager.ServiceStatistics(dataToAnalyze);

        // Update UI panel (includes progress bar animation)
        statisticsPanel.updateStatistics(stats);
    }

    // ==========================
    // DIALOG AJOUT SERVICE
    // ==========================
    private void showAddServiceDialog() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("➕ Ajouter un Nouveau Service");
        dialog.setHeaderText("Remplissez les informations du service");

        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        ComboBox<Client> clientBox = new ComboBox<>();
        ComboBox<TypeService> typeBox = new ComboBox<>();
        TextArea descField = new TextArea();
        TextField prixField = new TextField();
        TextField montantPayeField = new TextField("0");
        ComboBox<String> statutServiceCombo = new ComboBox<>();

        // Ajouter les statuts possibles
        statutServiceCombo.getItems().addAll("EN_ATTENTE", "EN_COURS", "TERMINE");
        statutServiceCombo.setValue("EN_ATTENTE");

        descField.setPromptText("Description détaillée du service...");
        prixField.setPromptText("Prix total (DZD)");
        montantPayeField.setPromptText("Montant payé (DZD)");

        descField.setPrefRowCount(2); // Réduit le nombre de lignes
        descField.setMaxHeight(60); // Hauteur maximale réduite

        // Bouton pour ajouter un nouveau client
        Button addClientBtn = new Button("➕");
        addClientBtn.setStyle(
                "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5;");
        addClientBtn.setOnAction(e -> showAddClientDialog(clientBox));

        // Bouton pour ajouter un nouveau type de service
        Button addServiceTypeBtn = new Button("➕");
        addServiceTypeBtn.setStyle(
                "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5;");
        addServiceTypeBtn.setOnAction(e -> showAddServiceTypeDialog(typeBox));

        // Charger les clients
        loadClients(clientBox);

        // Charger les types de service
        loadServiceTypes(typeBox);

        GridPane grid = new GridPane();
        grid.setHgap(10); // Espacement réduit
        grid.setVgap(8); // Espacement réduit
        grid.setPadding(new Insets(15)); // Padding réduit

        grid.add(new Label("👤 Client:"), 0, 0);
        HBox clientBoxContainer = new HBox(5, clientBox, addClientBtn);
        clientBoxContainer.setAlignment(Pos.CENTER_LEFT);
        grid.add(clientBoxContainer, 1, 0);

        grid.add(new Label("🛠️ Service:"), 0, 1);
        HBox serviceBoxContainer = new HBox(5, typeBox, addServiceTypeBtn);
        serviceBoxContainer.setAlignment(Pos.CENTER_LEFT);
        grid.add(serviceBoxContainer, 1, 1);

        grid.add(new Label("📝 Description:"), 0, 2);
        grid.add(descField, 1, 2);

        grid.add(new Label("💰 Prix total:"), 0, 3);
        grid.add(prixField, 1, 3);

        grid.add(new Label("💵 Montant payé:"), 0, 4);
        grid.add(montantPayeField, 1, 4);

        grid.add(new Label("📊 Statut:"), 0, 5);
        grid.add(statutServiceCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Validation
        Node saveButton = dialog.getDialogPane().lookupButton(saveBtn);
        saveButton.setDisable(true);

        clientBox.valueProperty()
                .addListener((obs, oldVal, newVal) -> validateFields(saveButton, clientBox, typeBox, prixField));
        typeBox.valueProperty()
                .addListener((obs, oldVal, newVal) -> validateFields(saveButton, clientBox, typeBox, prixField));
        prixField.textProperty()
                .addListener((obs, oldVal, newVal) -> validateFields(saveButton, clientBox, typeBox, prixField));
        montantPayeField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                double montant = newVal.isEmpty() ? 0 : Double.parseDouble(newVal);
                double prix = prixField.getText().isEmpty() ? 0 : Double.parseDouble(prixField.getText());
                if (montant > prix) {
                    montantPayeField.setStyle("-fx-border-color: red;");
                } else {
                    montantPayeField.setStyle("");
                }
            } catch (NumberFormatException e) {
                montantPayeField.setStyle("-fx-border-color: red;");
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                try {
                    double prix = Double.parseDouble(prixField.getText());
                    double montantPaye = montantPayeField.getText().isEmpty() ? 0
                            : Double.parseDouble(montantPayeField.getText());

                    if (montantPaye > prix) {
                        showError("Erreur", "Le montant payé ne peut pas dépasser le prix total");
                        return false;
                    }

                    boolean success = insertService(
                            clientBox.getValue().getId(),
                            typeBox.getValue().getId(),
                            descField.getText().trim(),
                            prix,
                            montantPaye,
                            statutServiceCombo.getValue());

                    return success;

                } catch (NumberFormatException e) {
                    showError("Erreur", "Format de prix invalide");
                } catch (Exception e) {
                    showError("Erreur", "Une erreur est survenue: " + e.getMessage());
                }
            }
            return false;
        });

        dialog.showAndWait().ifPresent(success -> {
            if (success) {
                loadServices();
                showInfo("Succès", "✅ Service ajouté avec succès");
            }
        });
    }

    private void loadClients(ComboBox<Client> clientBox) {
        clientBox.getItems().clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            ResultSet rs = conn.prepareStatement(
                    "SELECT id_client, nom FROM client ORDER BY nom").executeQuery();
            while (rs.next()) {
                clientBox.getItems().add(new Client(rs.getInt("id_client"), rs.getString("nom")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadServiceTypes(ComboBox<TypeService> typeBox) {
        typeBox.getItems().clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            ResultSet rs = conn.prepareStatement(
                    "SELECT id_type_service, nom_type FROM type_service ORDER BY nom_type").executeQuery();
            while (rs.next()) {
                typeBox.getItems().add(new TypeService(rs.getInt("id_type_service"), rs.getString("nom_type")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void validateFields(Node saveButton, ComboBox<Client> clientBox,
            ComboBox<TypeService> typeBox, TextField prixField) {
        boolean valid = clientBox.getValue() != null
                && typeBox.getValue() != null
                && !prixField.getText().trim().isEmpty();

        try {
            if (!prixField.getText().trim().isEmpty()) {
                double prix = Double.parseDouble(prixField.getText());
                valid = valid && prix > 0;
            }
        } catch (NumberFormatException e) {
            valid = false;
        }

        saveButton.setDisable(!valid);
    }

    private boolean insertService(int clientId, int typeId, String description,
            double prix, double montantPaye, String statutService) {

        String etatPaiement = montantPaye == 0 ? "NON_PAYE" : montantPaye == prix ? "PAYE" : "PARTIELLEMENT_PAYE";

        String sql = """
                    INSERT INTO service
                    (id_client, id_type_service, description,
                     prix_total, montant_paye, statut_service, etat_paiement)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, clientId);
                ps.setInt(2, typeId);
                ps.setString(3, description);
                ps.setDouble(4, prix);
                ps.setDouble(5, montantPaye);
                ps.setString(6, statutService);
                ps.setString(7, etatPaiement);
                ps.executeUpdate();
            }

            // Si un montant initial est payé, enregistrer le paiement
            if (montantPaye > 0) {
                // Récupérer l'ID du service créé
                int serviceId;
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) {
                    rs.next();
                    serviceId = rs.getInt(1);
                }

                // Enregistrer le paiement initial
                String insertPaiementSQL = """
                            INSERT INTO paiement_vente
                            (id_service, montant, mode_paiement, date_paiement)
                            VALUES (?, ?, 'Paiement initial', NOW())
                        """;

                try (PreparedStatement ps = conn.prepareStatement(insertPaiementSQL)) {
                    ps.setInt(1, serviceId);
                    ps.setDouble(2, montantPaye);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            showError("Erreur base de données", e.getMessage());
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ==========================
    // DIALOG AJOUT CLIENT
    // ==========================
    private void showAddClientDialog(ComboBox<Client> clientBoxToUpdate) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("➕ Ajouter un Nouveau Client");
        dialog.setHeaderText("Remplissez les informations du client");

        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        TextField nomField = new TextField();
        TextField telephoneField = new TextField();
        TextField emailField = new TextField();
        TextArea adresseField = new TextArea();

        nomField.setPromptText("Nom complet");
        telephoneField.setPromptText("Téléphone");
        emailField.setPromptText("Email");
        adresseField.setPromptText("Adresse");
        adresseField.setPrefRowCount(2); // Réduit
        adresseField.setMaxHeight(50); // Hauteur maximale réduite

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(15));

        grid.add(new Label("Nom *:"), 0, 0);
        grid.add(nomField, 1, 0);

        grid.add(new Label("Téléphone:"), 0, 1);
        grid.add(telephoneField, 1, 1);

        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);

        grid.add(new Label("Adresse:"), 0, 3);
        grid.add(adresseField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Validation
        Node saveButton = dialog.getDialogPane().lookupButton(saveBtn);
        saveButton.setDisable(true);

        nomField.textProperty().addListener((obs, oldVal, newVal) -> {
            saveButton.setDisable(newVal.trim().isEmpty());
        });

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                try {
                    boolean success = insertClient(
                            nomField.getText().trim(),
                            telephoneField.getText().trim(),
                            emailField.getText().trim(),
                            adresseField.getText().trim());

                    if (success) {
                        // Recharger la liste des clients
                        loadClients(clientBoxToUpdate);
                        // Charger également pour les filtres
                        loadFilterData();
                    }

                    return success;

                } catch (Exception e) {
                    showError("Erreur", "Une erreur est survenue: " + e.getMessage());
                }
            }
            return false;
        });

        dialog.showAndWait().ifPresent(success -> {
            if (success) {
                showInfo("Succès", "✅ Client ajouté avec succès");
            }
        });
    }

    private boolean insertClient(String nom, String telephone, String email, String adresse) {
        String sql = """
                    INSERT INTO client (nom, telephone, email, adresse)
                    VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nom);
            ps.setString(2, telephone.isEmpty() ? null : telephone);
            ps.setString(3, email.isEmpty() ? null : email);
            ps.setString(4, adresse.isEmpty() ? null : adresse);

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur base de données", e.getMessage());
            return false;
        }
    }

    // ==========================
    // DIALOG AJOUT TYPE DE SERVICE
    // ==========================
    private void showAddServiceTypeDialog(ComboBox<TypeService> typeBoxToUpdate) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("➕ Ajouter un Nouveau Type de Service");
        dialog.setHeaderText("Entrez le nom du nouveau type de service");

        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        TextField nomTypeField = new TextField();
        nomTypeField.setPromptText("Nom du type de service");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(15));

        grid.add(new Label("Nom du type *:"), 0, 0);
        grid.add(nomTypeField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        // Validation
        Node saveButton = dialog.getDialogPane().lookupButton(saveBtn);
        saveButton.setDisable(true);

        nomTypeField.textProperty().addListener((obs, oldVal, newVal) -> {
            saveButton.setDisable(newVal.trim().isEmpty());
        });

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                try {
                    boolean success = insertServiceType(nomTypeField.getText().trim());

                    if (success) {
                        // Recharger la liste des types de service
                        loadServiceTypes(typeBoxToUpdate);
                        // Charger également pour les filtres
                        loadFilterData();
                    }

                    return success;

                } catch (Exception e) {
                    showError("Erreur", "Une erreur est survenue: " + e.getMessage());
                }
            }
            return false;
        });

        dialog.showAndWait().ifPresent(success -> {
            if (success) {
                showInfo("Succès", "✅ Type de service ajouté avec succès");
            }
        });
    }

    private boolean insertServiceType(String nomType) {
        String sql = "INSERT INTO type_service (nom_type) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nomType);
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur base de données", e.getMessage());
            return false;
        }
    }

    // ==========================
    // MISE À JOUR PAIEMENT
    // ==========================
    private void showUpdatePaymentDialog(Service service) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("💳 Mise à jour du Paiement");
        dialog.setHeaderText("Service #" + service.getId() + " - " + service.getClient());

        ButtonType updateBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateBtn, ButtonType.CANCEL);

        Label totalLabel = new Label(String.format("Prix total: %,.2f DZD", service.getPrixTotal()));
        Label payeLabel = new Label(String.format("Déjà payé: %,.2f DZD", service.getMontantPaye()));
        Label resteLabel = new Label(String.format("Reste à payer: %,.2f DZD", service.getReste()));

        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
        payeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
        resteLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: red;");

        TextField nouveauPaiementField = new TextField();
        nouveauPaiementField.setPromptText("Montant à ajouter (DZD)");

        ComboBox<String> modePaiementBox = new ComboBox<>();
        modePaiementBox.getItems().addAll("Espèces", "Chèque", "Virement", "Carte", "Paiement initial");
        modePaiementBox.setValue("Espèces");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(15));

        grid.add(totalLabel, 0, 0, 2, 1);
        grid.add(payeLabel, 0, 1, 2, 1);
        grid.add(resteLabel, 0, 2, 2, 1);

        grid.add(new Separator(), 0, 3, 2, 1);

        grid.add(new Label("Montant à ajouter:"), 0, 4);
        grid.add(nouveauPaiementField, 1, 4);

        grid.add(new Label("Mode de paiement:"), 0, 5);
        grid.add(modePaiementBox, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == updateBtn) {
                try {
                    double nouveauPaiement = Double.parseDouble(nouveauPaiementField.getText());
                    String modePaiement = modePaiementBox.getValue();

                    if (nouveauPaiement <= 0) {
                        showError("Erreur", "Le montant doit être positif");
                        return false;
                    }

                    if (modePaiement == null || modePaiement.isEmpty()) {
                        showError("Erreur", "Veuillez sélectionner un mode de paiement");
                        return false;
                    }

                    if (nouveauPaiement > service.getReste()) {
                        showError("Erreur", "Le montant ne peut pas dépasser le reste à payer");
                        return false;
                    }

                    return updatePayment(service, nouveauPaiement, modePaiement);

                } catch (NumberFormatException e) {
                    showError("Erreur", "Format de montant invalide");
                }
            }
            return false;
        });

        dialog.showAndWait().ifPresent(success -> {
            if (success) {
                loadServices();
                showInfo("Succès", "✅ Paiement enregistré avec succès");
            }
        });
    }

    private boolean updatePayment(Service service, double montant, String modePaiement) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Calculer le nouveau montant total payé
            double nouveauMontantPaye = service.getMontantPaye() + montant;

            // 2. Déterminer le nouvel état de paiement
            String nouvelEtatPaiement;
            if (Math.abs(nouveauMontantPaye - service.getPrixTotal()) < 0.01) {
                nouvelEtatPaiement = "PAYE";
            } else if (nouveauMontantPaye > 0) {
                nouvelEtatPaiement = "PARTIELLEMENT_PAYE";
            } else {
                nouvelEtatPaiement = "NON_PAYE";
            }

            // 3. Mettre à jour le service
            String updateServiceSQL = """
                        UPDATE service
                        SET montant_paye = ?,
                            etat_paiement = ?
                        WHERE id_service = ?
                    """;

            try (PreparedStatement ps = conn.prepareStatement(updateServiceSQL)) {
                ps.setDouble(1, nouveauMontantPaye);
                ps.setString(2, nouvelEtatPaiement);
                ps.setInt(3, service.getId());
                ps.executeUpdate();
            }

            // 4. Enregistrer le paiement dans paiement_vente
            String insertPaiementSQL = """
                        INSERT INTO paiement_vente
                        (id_service, montant, mode_paiement, date_paiement)
                        VALUES (?, ?, ?, NOW())
                    """;

            try (PreparedStatement ps = conn.prepareStatement(insertPaiementSQL)) {
                ps.setInt(1, service.getId());
                ps.setDouble(2, montant);
                ps.setString(3, modePaiement);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            showError("Erreur", "Erreur lors de la mise à jour: " + e.getMessage());
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ==========================
    // HISTORIQUE DES PAIEMENTS
    // ==========================
    private void showPaymentHistoryDialog(Service service) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("📋 Historique des Paiements");
        dialog.setHeaderText("Service #" + service.getId() + " - " + service.getClient() +
                "\nType: " + service.getType() +
                "\nPrix total: " + String.format("%,.2f DZD", service.getPrixTotal()));

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Créer le tableau pour l'historique
        TableView<Payment> historyTable = new TableView<>();
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        historyTable.setPrefHeight(300);

        TableColumn<Payment, String> dateCol = new TableColumn<>("📅 Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateFormatted"));
        dateCol.setPrefWidth(150);

        TableColumn<Payment, Double> montantCol = new TableColumn<>("💰 Montant");
        montantCol.setCellValueFactory(new PropertyValueFactory<>("montant"));
        montantCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.2f DZD", item));
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });
        montantCol.setPrefWidth(120);

        TableColumn<Payment, String> modeCol = new TableColumn<>("💳 Mode");
        modeCol.setCellValueFactory(new PropertyValueFactory<>("modePaiement"));
        modeCol.setPrefWidth(120);

        historyTable.getColumns().addAll(dateCol, montantCol, modeCol);

        // Charger les paiements
        ObservableList<Payment> payments = loadPaymentHistory(service.getId());
        historyTable.setItems(payments);

        // Résumé des paiements
        double totalPaye = payments.stream().mapToDouble(Payment::getMontant).sum();
        double resteAPayer = service.getPrixTotal() - totalPaye;

        Label resumeLabel = new Label(String.format(
                "📊 Résumé: %d paiement(s) | Total payé: %,.2f DZD | Reste: %,.2f DZD",
                payments.size(), totalPaye, resteAPayer));
        resumeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #2c3e50;");
        resumeLabel.setPadding(new Insets(0, 0, 10, 0));

        VBox content = new VBox(8, resumeLabel, historyTable);
        content.setPadding(new Insets(10));
        content.setPrefSize(450, 350); // Taille réduite

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private ObservableList<Payment> loadPaymentHistory(int serviceId) {
        ObservableList<Payment> payments = FXCollections.observableArrayList();

        String sql = """
                    SELECT date_paiement, montant, mode_paiement
                    FROM paiement_vente
                    WHERE id_service = ?
                    ORDER BY date_paiement DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, serviceId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Timestamp date = rs.getTimestamp("date_paiement");
                payments.add(new Payment(
                        date,
                        rs.getDouble("montant"),
                        rs.getString("mode_paiement")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return payments;
    }

    // ==========================
    // MISE À JOUR STATUT SERVICE
    // ==========================
    private void showUpdateServiceStatusDialog(Service service) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("🔄 Changer le Statut du Service");
        dialog.setHeaderText("Service #" + service.getId() + " - " + service.getClient());

        ButtonType updateBtn = new ButtonType("Mettre à jour", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateBtn, ButtonType.CANCEL);

        Label currentStatusLabel = new Label("Statut actuel: " + service.getStatutService());
        currentStatusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("EN_ATTENTE", "EN_COURS", "TERMINE");
        statusCombo.setValue(service.getStatutService());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(15));

        grid.add(currentStatusLabel, 0, 0, 2, 1);
        grid.add(new Label("Nouveau statut:"), 0, 1);
        grid.add(statusCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == updateBtn) {
                String newStatus = statusCombo.getValue();
                if (newStatus == null || newStatus.equals(service.getStatutService())) {
                    return false;
                }
                return updateServiceStatus(service, newStatus);
            }
            return false;
        });

        dialog.showAndWait().ifPresent(success -> {
            if (success) {
                loadServices();
                showInfo("Succès", "✅ Statut du service mis à jour avec succès");
            }
        });
    }

    private boolean updateServiceStatus(Service service, String newStatus) {
        String sql = "UPDATE service SET statut_service = ? WHERE id_service = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, service.getId());

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de mettre à jour le statut: " + e.getMessage());
            return false;
        }
    }

    // ==========================
    // SUPPRESSION SERVICE
    // ==========================
    private void showDeleteConfirmation(Service service) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le service #" + service.getId());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce service ?\n" +
                "Client: " + service.getClient() + "\n" +
                "Service: " + service.getType() + "\n" +
                "Montant: " + String.format("%,.2f DZD", service.getPrixTotal()));

        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                deleteService(service);
            }
        });
    }

    private void deleteService(Service service) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Supprimer d'abord les paiements associés
            String deletePaiementsSQL = "DELETE FROM paiement_vente WHERE id_service = ?";
            try (PreparedStatement ps = conn.prepareStatement(deletePaiementsSQL)) {
                ps.setInt(1, service.getId());
                ps.executeUpdate();
            }

            // 2. Supprimer le service
            String deleteServiceSQL = "DELETE FROM service WHERE id_service = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteServiceSQL)) {
                ps.setInt(1, service.getId());
                int rows = ps.executeUpdate();

                if (rows > 0) {
                    conn.commit();
                    loadServices();
                    showInfo("Succès", "✅ Service et paiements associés supprimés avec succès");
                    return;
                }
            }

        } catch (SQLException e) {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            showError("Erreur", "Impossible de supprimer le service: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ==========================
    // MÉTHODES UTILITAIRES
    // ==========================
    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // ==========================
    // MODELS
    // ==========================
    public static class Service {
        private final int id;
        private final String client;
        private final String type;
        private final String description;
        private final double prixTotal;
        private final double montantPaye;
        private final double reste;
        private final String statutPaiement;
        private final String statutService;
        private final Timestamp dateCreation;

        public Service(int id, String client, String type, String description,
                double prixTotal, double montantPaye, double reste,
                String statutPaiement, String statutService, Timestamp dateCreation) {
            this.id = id;
            this.client = client;
            this.type = type;
            this.description = description;
            this.prixTotal = prixTotal;
            this.montantPaye = montantPaye;
            this.reste = reste;
            this.statutPaiement = statutPaiement;
            this.statutService = statutService;
            this.dateCreation = dateCreation;
        }

        public int getId() {
            return id;
        }

        public String getClient() {
            return client;
        }

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public double getPrixTotal() {
            return prixTotal;
        }

        public double getMontantPaye() {
            return montantPaye;
        }

        public double getReste() {
            return reste;
        }

        public String getStatutPaiement() {
            return statutPaiement;
        }

        public String getStatutService() {
            return statutService;
        }

        public Timestamp getDateCreation() {
            return dateCreation;
        }

        public String getDateFormatted() {
            if (dateCreation == null)
                return "";
            return DATE_FORMATTER.format(dateCreation.toLocalDateTime());
        }
    }

    public static class Client {
        private final int id;
        private final String nom;

        public Client(int id, String nom) {
            this.id = id;
            this.nom = nom;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return nom;
        }
    }

    public static class TypeService {
        private final int id;
        private final String nom;

        public TypeService(int id, String nom) {
            this.id = id;
            this.nom = nom;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return nom;
        }
    }

    public static class Payment {
        private final Timestamp date;
        private final double montant;
        private final String modePaiement;

        public Payment(Timestamp date, double montant, String modePaiement) {
            this.date = date;
            this.montant = montant;
            this.modePaiement = modePaiement;
        }

        public String getDateFormatted() {
            if (date == null)
                return "";
            return DATE_FORMATTER.format(date.toLocalDateTime());
        }

        public double getMontant() {
            return montant;
        }

        public String getModePaiement() {
            return modePaiement;
        }
    }
}
