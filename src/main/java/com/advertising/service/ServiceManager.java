package com.advertising.service;

import com.advertising.controller.ServiceController.Service;
import com.advertising.controller.ServiceController.Payment;
import com.advertising.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;

/**
 * Service layer for handling business logic and database operations
 * Separates data access from UI concerns
 */
public class ServiceManager {

    /**
     * Load all services from database
     */
    public ObservableList<Service> loadAllServices() throws SQLException {
        ObservableList<Service> services = FXCollections.observableArrayList();

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
                services.add(new Service(
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
        }

        return services;
    }

    /**
     * Add a new service with optional initial payment
     */
    public boolean addService(int clientId, int typeId, String description,
                              double prixTotal, double montantPaye, String statutService) throws SQLException {

        String etatPaiement = calculatePaymentStatus(montantPaye, prixTotal);

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

            int serviceId;
            
            // Insert service
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, clientId);
                ps.setInt(2, typeId);
                ps.setString(3, description);
                ps.setDouble(4, prixTotal);
                ps.setDouble(5, montantPaye);
                ps.setString(6, statutService);
                ps.setString(7, etatPaiement);
                ps.executeUpdate();

                // Get generated ID
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        serviceId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to get service ID");
                    }
                }
            }

            // Record initial payment if any
            if (montantPaye > 0) {
                recordPayment(conn, serviceId, montantPaye, "Paiement initial");
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Update payment for a service
     */
    public boolean updatePayment(int serviceId, double currentPaid, double totalPrice,
                                 double additionalPayment, String paymentMode) throws SQLException {

        if (additionalPayment <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        double newTotalPaid = currentPaid + additionalPayment;
        
        if (newTotalPaid > totalPrice + 0.01) { // Small tolerance for floating point
            throw new IllegalArgumentException("Total payment cannot exceed total price");
        }

        String newStatus = calculatePaymentStatus(newTotalPaid, totalPrice);

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Update service
            String updateServiceSQL = """
                    UPDATE service
                    SET montant_paye = ?,
                        etat_paiement = ?
                    WHERE id_service = ?
                    """;

            try (PreparedStatement ps = conn.prepareStatement(updateServiceSQL)) {
                ps.setDouble(1, newTotalPaid);
                ps.setString(2, newStatus);
                ps.setInt(3, serviceId);
                ps.executeUpdate();
            }

            // Record payment
            recordPayment(conn, serviceId, additionalPayment, paymentMode);

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Update service status
     */
    public boolean updateServiceStatus(int serviceId, String newStatus) throws SQLException {
        String sql = "UPDATE service SET statut_service = ? WHERE id_service = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, serviceId);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Delete service and associated payments
     */
    public boolean deleteService(int serviceId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Delete payments first (foreign key constraint)
            String deletePaiementsSQL = "DELETE FROM paiement_vente WHERE id_service = ?";
            try (PreparedStatement ps = conn.prepareStatement(deletePaiementsSQL)) {
                ps.setInt(1, serviceId);
                ps.executeUpdate();
            }

            // Delete service
            String deleteServiceSQL = "DELETE FROM service WHERE id_service = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteServiceSQL)) {
                ps.setInt(1, serviceId);
                int rows = ps.executeUpdate();
                
                if (rows > 0) {
                    conn.commit();
                    return true;
                }
            }

            return false;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Load payment history for a service
     */
    public ObservableList<Payment> loadPaymentHistory(int serviceId) throws SQLException {
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
                payments.add(new Payment(
                        rs.getTimestamp("date_paiement"),
                        rs.getDouble("montant"),
                        rs.getString("mode_paiement")));
            }
        }

        return payments;
    }

    /**
     * Get detailed client information
     */
    public ClientDetails getClientDetails(String clientName) throws SQLException {
        String sql = """
                SELECT nom, telephone, email, adresse
                FROM client
                WHERE nom = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, clientName);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new ClientDetails(
                        rs.getString("nom"),
                        rs.getString("telephone"),
                        rs.getString("email"),
                        rs.getString("adresse")
                );
            }
        }

        return new ClientDetails(clientName, null, null, null);
    }

    // ============ PRIVATE HELPER METHODS ============

    private void recordPayment(Connection conn, int serviceId, double amount, String mode) throws SQLException {
        String sql = """
                INSERT INTO paiement_vente
                (id_service, montant, mode_paiement, date_paiement)
                VALUES (?, ?, ?, NOW())
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, serviceId);
            ps.setDouble(2, amount);
            ps.setString(3, mode);
            ps.executeUpdate();
        }
    }

    private String calculatePaymentStatus(double paid, double total) {
        if (Math.abs(paid - total) < 0.01) {
            return "PAYE";
        } else if (paid > 0) {
            return "PARTIELLEMENT_PAYE";
        } else {
            return "NON_PAYE";
        }
    }

    // ============ DATA CLASSES ============

    public static class ClientDetails {
        private final String nom;
        private final String telephone;
        private final String email;
        private final String adresse;

        public ClientDetails(String nom, String telephone, String email, String adresse) {
            this.nom = nom;
            this.telephone = telephone;
            this.email = email;
            this.adresse = adresse;
        }

        public String getNom() { return nom; }
        public String getTelephone() { return telephone; }
        public String getEmail() { return email; }
        public String getAdresse() { return adresse; }

        public String getFormattedDetails() {
            StringBuilder sb = new StringBuilder();
            sb.append(nom != null ? nom : "N/A");
            if (telephone != null && !telephone.isEmpty()) {
                sb.append("\nTél: ").append(telephone);
            }
            if (email != null && !email.isEmpty()) {
                sb.append("\nEmail: ").append(email);
            }
            if (adresse != null && !adresse.isEmpty()) {
                sb.append("\nAdresse: ").append(adresse);
            }
            return sb.toString();
        }
    }

    /**
     * Statistics aggregation
     */
    public static class ServiceStatistics {
        private final double totalAmount;
        private final double totalPaid;
        private final double totalRemaining;
        private final int serviceCount;

        public ServiceStatistics(ObservableList<Service> services) {
            this.totalAmount = services.stream().mapToDouble(Service::getPrixTotal).sum();
            this.totalPaid = services.stream().mapToDouble(Service::getMontantPaye).sum();
            this.totalRemaining = services.stream().mapToDouble(Service::getReste).sum();
            this.serviceCount = services.size();
        }

        public double getTotalAmount() { return totalAmount; }
        public double getTotalPaid() { return totalPaid; }
        public double getTotalRemaining() { return totalRemaining; }
        public int getServiceCount() { return serviceCount; }
        
        /**
         * Calculate payment completion percentage (0-100)
         */
        public double getCompletionPercentage() {
            if (totalAmount == 0) return 0;
            return (totalPaid / totalAmount) * 100;
        }
    }
}
