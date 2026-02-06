package com.advertising.component;

import com.advertising.service.ServiceManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Dedicated component for displaying service statistics
 * with clean, direct access to labels (no nested navigation)
 * 
 * Features:
 * - Total amount display
 * - Amount paid display
 * - Remaining amount display
 * - Visual progress bar showing payment completion
 */
public class ServiceStatisticsPanel extends VBox {
    
    // Direct references to labels - NO nested access needed
    private final Label totalLabel;
    private final Label paidLabel;
    private final Label remainingLabel;
    private final ProgressBar paymentProgressBar;
    private final Label progressPercentageLabel;
    
    public ServiceStatisticsPanel() {
        setupLayout();

        // Create statistics boxes
        VBox totalBox = createStatBox("💰 TOTAL", "#3498db");
        VBox paidBox = createStatBox("💵 PAYÉ", "#2ecc71");
        VBox remainingBox = createStatBox("⚖️ RESTE", "#e74c3c");

        // Store direct references to value labels
        // These are at index 1 in each VBox (title is at 0, value label at 1)
        totalLabel = (Label) totalBox.getChildren().get(1);
        paidLabel = (Label) paidBox.getChildren().get(1);
        remainingLabel = (Label) remainingBox.getChildren().get(1);

        // Initialize progress bar and label directly in constructor (required for final fields)
        paymentProgressBar = new ProgressBar(0);
        paymentProgressBar.setPrefWidth(450);
        paymentProgressBar.setPrefHeight(20);
        paymentProgressBar.setStyle("-fx-accent: #e74c3c;"); // Default to red (no payment)

        progressPercentageLabel = new Label("0%");
        progressPercentageLabel.setStyle("-fx-font-size: 11px; " +
                                        "-fx-font-weight: bold; " +
                                        "-fx-text-fill: #2c3e50;");

        // Create progress bar section
        VBox progressBox = createProgressBarSection();

        // Arrange statistics boxes horizontally
        HBox statsRow = new HBox(15, totalBox, paidBox, remainingBox);
        statsRow.setAlignment(Pos.CENTER);

        // Add all components
        getChildren().addAll(statsRow, progressBox);

        // CRITICAL FIX: Set height constraints AFTER children are added
        // This ensures the panel maintains its size and stays visible
        setMinHeight(140);
        setPrefHeight(140);
        setMaxHeight(160);
    }
    
    private void setupLayout() {
        setSpacing(12);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: white; " +
                 "-fx-background-radius: 8; " +
                 "-fx-border-color: #ddd; " +
                 "-fx-border-width: 1; " +
                 "-fx-border-radius: 8;");
    }
    
    private VBox createStatBox(String title, String color) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: " + color + "20; " +
                     "-fx-background-radius: 6; " +
                     "-fx-border-color: " + color + "; " +
                     "-fx-border-width: 2; " +
                     "-fx-border-radius: 6;");
        box.setMinWidth(160);
        box.setPrefWidth(160);
        
        // Title label
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; " +
                           "-fx-font-size: 12px; " +
                           "-fx-text-fill: " + color + ";");
        
        // Value label (will be updated dynamically)
        Label valueLabel = new Label("0 DZD");
        valueLabel.setStyle("-fx-font-weight: bold; " +
                           "-fx-font-size: 16px; " +
                           "-fx-text-fill: " + color + ";");
        
        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }
    
    private VBox createProgressBarSection() {
        VBox progressBox = new VBox(6);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.setPadding(new Insets(10, 0, 0, 0));
        
        // Progress label
        Label progressLabel = new Label("📊 Progression des paiements");
        progressLabel.setStyle("-fx-font-size: 11px; " +
                              "-fx-font-weight: bold; " +
                              "-fx-text-fill: #7f8c8d;");
        
        // Add components (progress bar and percentage label already initialized in constructor)
        progressBox.getChildren().addAll(progressLabel, paymentProgressBar, progressPercentageLabel);
        return progressBox;
    }
    
    /**
     * Update statistics display with new data
     * This is the ONLY method that needs to be called to update the entire panel
     * 
     * @param stats Statistics object containing calculated totals
     */
    public void updateStatistics(ServiceManager.ServiceStatistics stats) {
        if (stats == null) {
            resetStatistics();
            return;
        }
        
        // Update labels with formatted currency
        totalLabel.setText(formatCurrency(stats.getTotalAmount()));
        paidLabel.setText(formatCurrency(stats.getTotalPaid()));
        remainingLabel.setText(formatCurrency(stats.getTotalRemaining()));
        
        // Update progress bar
        double completionPercentage = stats.getCompletionPercentage();
        double progress = completionPercentage / 100.0;
        
        // Clamp progress between 0 and 1
        progress = Math.max(0, Math.min(1, progress));
        
        paymentProgressBar.setProgress(progress);
        progressPercentageLabel.setText(String.format("%.1f%%", completionPercentage));
        
        // Update progress bar color based on completion
        updateProgressBarStyle(completionPercentage);
    }
    
    /**
     * Reset all statistics to zero
     */
    public void resetStatistics() {
        totalLabel.setText("0 DZD");
        paidLabel.setText("0 DZD");
        remainingLabel.setText("0 DZD");
        paymentProgressBar.setProgress(0);
        progressPercentageLabel.setText("0%");
        paymentProgressBar.setStyle("-fx-accent: #e74c3c;");
    }
    
    /**
     * Update progress bar color based on completion percentage
     * - Red (< 30%): Minimal payment
     * - Orange (30-70%): Partial payment
     * - Yellow-Green (70-99%): Almost complete
     * - Green (100%): Fully paid
     */
    private void updateProgressBarStyle(double percentage) {
        String color;
        
        if (percentage >= 100) {
            color = "#2ecc71"; // Green - fully paid
        } else if (percentage >= 70) {
            color = "#f39c12"; // Orange - almost there
        } else if (percentage >= 30) {
            color = "#e67e22"; // Dark orange - partial
        } else {
            color = "#e74c3c"; // Red - minimal payment
        }
        
        paymentProgressBar.setStyle("-fx-accent: " + color + ";");
    }
    
    /**
     * Format currency amount for display
     * Uses French/Algerian format with no decimals for DZD
     */
    private String formatCurrency(double amount) {
        return String.format("%,.0f DZD", amount);
    }
    
    // Getters for individual components (if needed for advanced customization)
    
    public Label getTotalLabel() {
        return totalLabel;
    }
    
    public Label getPaidLabel() {
        return paidLabel;
    }
    
    public Label getRemainingLabel() {
        return remainingLabel;
    }
    
    public ProgressBar getPaymentProgressBar() {
        return paymentProgressBar;
    }
    
    public Label getProgressPercentageLabel() {
        return progressPercentageLabel;
    }
}
