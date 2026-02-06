package com.advertising.service;

import com.advertising.controller.ServiceController.Service;
import com.advertising.controller.ServiceController.Payment;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.font.constants.StandardFonts;
import javafx.collections.ObservableList;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Professional PDF generator for service invoices and payment reports
 * Handles font loading, layout, and document generation with proper error handling
 */
public class PdfReportGenerator {

    // Company information - Can be externalized to configuration
    private static final String COMPANY_NAME = "DECOPEINT";
    private static final String COMPANY_SUBTITLE = "Services d'Impression & Publicité";
    private static final String COMPANY_PHONE = "+213 XX XX XX XX";
    private static final String COMPANY_EMAIL = "contact@decopeint.dz";
    private static final String COMPANY_ADDRESS = "Alger, Algérie";
    private static final String COMPANY_WEBSITE = "www.decopeint.dz";

    // Colors
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(44, 62, 80); // #2c3e50
    private static final DeviceRgb ACCENT_COLOR = new DeviceRgb(52, 152, 219); // #3498db
    private static final DeviceRgb SUCCESS_COLOR = new DeviceRgb(46, 204, 113); // #2ecc71
    private static final DeviceRgb DANGER_COLOR = new DeviceRgb(231, 76, 60); // #e74c3c

    // Formatters
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    // Fonts
    private PdfFont fontNormal;
    private PdfFont fontBold;

    // Output directory
    private final File outputDirectory;

    /**
     * Constructor with default output directory
     */
    public PdfReportGenerator() {
        this(new File("factures"));
    }

    /**
     * Constructor with custom output directory
     */
    public PdfReportGenerator(File outputDirectory) {
        this.outputDirectory = outputDirectory;
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
        initializeFonts();
    }

    /**
     * Initialize fonts with fallback to standard fonts
     */
    private void initializeFonts() {
        try {
            fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize PDF fonts", e);
        }
    }

    /**
     * Generate a complete payment report/invoice for a service
     * 
     * @param service The service to generate report for
     * @param payments Payment history
     * @param clientDetails Client detailed information
     * @return Generated PDF file
     * @throws IOException If PDF generation fails
     */
    public File generateServiceInvoice(Service service, 
                                      ObservableList<Payment> payments,
                                      ServiceManager.ClientDetails clientDetails) throws IOException {

        // Generate unique filename
        String fileName = String.format("FACTURE_Service_%d_%s.pdf",
                service.getId(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

        File pdfFile = new File(outputDirectory, fileName);

        // Initialize PDF
        PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        document.setMargins(50, 50, 50, 50);

        try {
            // Build document sections
            addHeader(document, service);
            addSeparator(document, 2);
            addClientSection(document, clientDetails);
            addSeparator(document, 1);
            addServiceDetails(document, service);
            addSeparator(document, 1);
            addFinancialSummary(document, service);
            addSeparator(document, 1);
            
            if (payments != null && !payments.isEmpty()) {
                addPaymentHistory(document, payments);
                addSeparator(document, 1);
            }
            
            addPaymentInstructions(document);
            addFooter(document);

        } finally {
            document.close();
        }

        return pdfFile;
    }

    /**
     * Add document header with company info and invoice title
     */
    private void addHeader(Document document, Service service) {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        headerTable.setWidth(UnitValue.createPercentValue(100));

        // Left column - Company info
        Cell companyCell = new Cell();
        companyCell.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        
        companyCell.add(createParagraph(COMPANY_NAME, fontBold, 22, PRIMARY_COLOR, TextAlignment.LEFT));
        companyCell.add(createParagraph(COMPANY_SUBTITLE, fontNormal, 11, PRIMARY_COLOR, TextAlignment.LEFT));
        companyCell.add(createParagraph("Tél: " + COMPANY_PHONE, fontNormal, 9, ColorConstants.DARK_GRAY, TextAlignment.LEFT)
                .setMarginTop(8));
        companyCell.add(createParagraph("Email: " + COMPANY_EMAIL, fontNormal, 9, ColorConstants.DARK_GRAY, TextAlignment.LEFT));
        companyCell.add(createParagraph("Adresse: " + COMPANY_ADDRESS, fontNormal, 9, ColorConstants.DARK_GRAY, TextAlignment.LEFT));

        headerTable.addCell(companyCell);

        // Right column - Invoice info
        Cell invoiceCell = new Cell();
        invoiceCell.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        
        invoiceCell.add(createParagraph("FACTURE", fontBold, 28, ACCENT_COLOR, TextAlignment.RIGHT));
        invoiceCell.add(createParagraph("N° " + String.format("%06d", service.getId()), 
                fontBold, 14, ColorConstants.BLACK, TextAlignment.RIGHT).setMarginTop(5));
        invoiceCell.add(createParagraph("Date: " + service.getDateFormatted(), 
                fontNormal, 10, ColorConstants.DARK_GRAY, TextAlignment.RIGHT).setMarginTop(3));

        headerTable.addCell(invoiceCell);

        document.add(headerTable);
    }

    /**
     * Add visual separator line
     */
    private void addSeparator(Document document, float thickness) {
        document.add(new Paragraph("")
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, thickness))
                .setMarginTop(15)
                .setMarginBottom(15));
    }

    /**
     * Add client information section
     */
    private void addClientSection(Document document, ServiceManager.ClientDetails clientDetails) {
        // Section title
        document.add(createParagraph("INFORMATIONS CLIENT", fontBold, 14, PRIMARY_COLOR, TextAlignment.LEFT)
                .setMarginBottom(10));

        // Client details box
        Table clientTable = new Table(1);
        clientTable.setWidth(UnitValue.createPercentValue(100));
        
        Cell clientCell = new Cell();
        clientCell.setBackgroundColor(new DeviceRgb(245, 247, 250));
        clientCell.setPadding(12);
        clientCell.setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1));
        
        // Format client details
        String details = clientDetails.getFormattedDetails();
        for (String line : details.split("\n")) {
            clientCell.add(createParagraph(line, fontNormal, 11, ColorConstants.BLACK, TextAlignment.LEFT)
                    .setMarginBottom(3));
        }
        
        clientTable.addCell(clientCell);
        document.add(clientTable);
    }

    /**
     * Add service details section
     */
    private void addServiceDetails(Document document, Service service) {
        document.add(createParagraph("DÉTAILS DU SERVICE", fontBold, 14, PRIMARY_COLOR, TextAlignment.LEFT)
                .setMarginBottom(10));

        Table serviceTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        serviceTable.setWidth(UnitValue.createPercentValue(100));

        // Service type
        addDetailRow(serviceTable, "Type de service:", service.getType());
        
        // Description
        addDetailRow(serviceTable, "Description:", service.getDescription() != null && !service.getDescription().isEmpty() 
                ? service.getDescription() : "N/A");
        
        // Status
        String statusLabel = getServiceStatusLabel(service.getStatutService());
        addDetailRow(serviceTable, "Statut:", statusLabel);

        document.add(serviceTable);
    }

    /**
     * Add financial summary with totals and payment status
     */
    private void addFinancialSummary(Document document, Service service) {
        document.add(createParagraph("RÉSUMÉ FINANCIER", fontBold, 14, PRIMARY_COLOR, TextAlignment.LEFT)
                .setMarginBottom(10));

        Table financialTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}));
        financialTable.setWidth(UnitValue.createPercentValue(100));

        // Header
        financialTable.addHeaderCell(createTableHeaderCell("DÉSIGNATION"));
        financialTable.addHeaderCell(createTableHeaderCell("MONTANT (DZD)"));

        // Service line
        financialTable.addCell(createTableCell("Service: " + service.getType(), fontNormal, 11, TextAlignment.LEFT));
        financialTable.addCell(createTableCell(formatCurrency(service.getPrixTotal()), fontNormal, 11, TextAlignment.RIGHT));

        // Spacer
        financialTable.addCell(createEmptyCell());
        financialTable.addCell(createEmptyCell());

        // Subtotal
        financialTable.addCell(createTableCell("SOUS-TOTAL", fontBold, 12, TextAlignment.LEFT));
        financialTable.addCell(createTableCell(formatCurrency(service.getPrixTotal()), fontBold, 12, TextAlignment.RIGHT));

        // Amount paid
        Cell paidLabelCell = createTableCell("Montant déjà payé", fontBold, 11, TextAlignment.LEFT);
        paidLabelCell.setBackgroundColor(new DeviceRgb(240, 240, 240));
        financialTable.addCell(paidLabelCell);
        
        Cell paidValueCell = createTableCell(formatCurrency(service.getMontantPaye()), fontNormal, 11, TextAlignment.RIGHT);
        paidValueCell.setBackgroundColor(new DeviceRgb(240, 240, 240));
        paidValueCell.setFontColor(SUCCESS_COLOR);
        financialTable.addCell(paidValueCell);

        // Remaining to pay (highlighted)
        Cell resteLabelCell = createTableCell("RESTE À PAYER", fontBold, 14, TextAlignment.LEFT);
        resteLabelCell.setBackgroundColor(new DeviceRgb(250, 250, 250));
        resteLabelCell.setBorderTop(new SolidBorder(ColorConstants.BLACK, 2));
        resteLabelCell.setBorderBottom(new SolidBorder(ColorConstants.BLACK, 2));
        financialTable.addCell(resteLabelCell);

        Cell resteValueCell = createTableCell(formatCurrency(service.getReste()), fontBold, 14, TextAlignment.RIGHT);
        resteValueCell.setBackgroundColor(new DeviceRgb(250, 250, 250));
        resteValueCell.setBorderTop(new SolidBorder(ColorConstants.BLACK, 2));
        resteValueCell.setBorderBottom(new SolidBorder(ColorConstants.BLACK, 2));
        
        if (service.getReste() > 0.01) {
            resteValueCell.setFontColor(DANGER_COLOR);
        } else {
            resteValueCell.setFontColor(SUCCESS_COLOR);
        }
        
        financialTable.addCell(resteValueCell);

        document.add(financialTable);

        // Add payment completion indicator
        double completionPercentage = (service.getMontantPaye() / service.getPrixTotal()) * 100;
        String statusText = String.format("Paiement complété à %.1f%%", completionPercentage);
        document.add(createParagraph(statusText, fontNormal, 10, ColorConstants.DARK_GRAY, TextAlignment.RIGHT)
                .setMarginTop(5));
    }

    /**
     * Add payment history table
     */
    private void addPaymentHistory(Document document, ObservableList<Payment> payments) {
        document.add(createParagraph("HISTORIQUE DES PAIEMENTS", fontBold, 14, PRIMARY_COLOR, TextAlignment.LEFT)
                .setMarginBottom(10));

        Table paymentTable = new Table(UnitValue.createPercentArray(new float[]{15, 40, 25, 20}));
        paymentTable.setWidth(UnitValue.createPercentValue(100));

        // Headers
        paymentTable.addHeaderCell(createTableHeaderCell("N°"));
        paymentTable.addHeaderCell(createTableHeaderCell("DATE"));
        paymentTable.addHeaderCell(createTableHeaderCell("MONTANT (DZD)"));
        paymentTable.addHeaderCell(createTableHeaderCell("MODE"));

        // Payment rows
        double totalPaid = 0;
        int index = 1;
        for (Payment payment : payments) {
            paymentTable.addCell(createTableCell(String.valueOf(index++), fontNormal, 10, TextAlignment.CENTER));
            paymentTable.addCell(createTableCell(payment.getDateFormatted(), fontNormal, 10, TextAlignment.LEFT));
            paymentTable.addCell(createTableCell(formatCurrency(payment.getMontant()), fontNormal, 10, TextAlignment.RIGHT));
            paymentTable.addCell(createTableCell(payment.getModePaiement(), fontNormal, 10, TextAlignment.LEFT));
            totalPaid += payment.getMontant();
        }

        // Total row - without colspan, just merge visually
        Cell emptyCell1 = createEmptyCell();
        emptyCell1.setBackgroundColor(new DeviceRgb(230, 245, 255));
        paymentTable.addCell(emptyCell1);
        
        Cell totalLabelCell = createTableCell("TOTAL ENCAISSÉ", fontBold, 11, TextAlignment.RIGHT);
        totalLabelCell.setBackgroundColor(new DeviceRgb(230, 245, 255));
        paymentTable.addCell(totalLabelCell);

        Cell totalValueCell = createTableCell(formatCurrency(totalPaid), fontBold, 11, TextAlignment.RIGHT);
        totalValueCell.setBackgroundColor(new DeviceRgb(230, 245, 255));
        totalValueCell.setFontColor(SUCCESS_COLOR);
        paymentTable.addCell(totalValueCell);

        Cell emptyCell2 = createEmptyCell();
        emptyCell2.setBackgroundColor(new DeviceRgb(230, 245, 255));
        paymentTable.addCell(emptyCell2);

        document.add(paymentTable);
    }

    /**
     * Add payment instructions
     */
    private void addPaymentInstructions(Document document) {
        Table instructionsTable = new Table(1);
        instructionsTable.setWidth(UnitValue.createPercentValue(100));

        Cell instructionsCell = new Cell();
        instructionsCell.setBackgroundColor(new DeviceRgb(255, 250, 230));
        instructionsCell.setPadding(12);
        instructionsCell.setBorder(new SolidBorder(new DeviceRgb(255, 193, 7), 1));

        instructionsCell.add(createParagraph("INFORMATIONS DE PAIEMENT", fontBold, 12, PRIMARY_COLOR, TextAlignment.LEFT)
                .setMarginBottom(8));
        instructionsCell.add(createParagraph("• Veuillez effectuer le paiement sous 30 jours à compter de la date de facturation.", 
                fontNormal, 10, ColorConstants.BLACK, TextAlignment.LEFT).setMarginBottom(3));
        instructionsCell.add(createParagraph("• Modes de paiement acceptés: Espèces, Chèque, Virement bancaire, Carte bancaire", 
                fontNormal, 10, ColorConstants.BLACK, TextAlignment.LEFT).setMarginBottom(3));
        instructionsCell.add(createParagraph("• Merci de mentionner le numéro de facture lors du paiement.", 
                fontNormal, 10, ColorConstants.BLACK, TextAlignment.LEFT));

        instructionsTable.addCell(instructionsCell);
        document.add(instructionsTable);
    }

    /**
     * Add footer with signature and legal mentions
     */
    private void addFooter(Document document) {
        document.add(new Paragraph("\n"));

        // Thank you message
        document.add(createParagraph("Merci pour votre confiance !", fontBold, 12, PRIMARY_COLOR, TextAlignment.CENTER)
                .setMarginBottom(5));

        // Contact info
        document.add(createParagraph("Pour toute question, contactez-nous au " + COMPANY_PHONE, 
                fontNormal, 9, ColorConstants.DARK_GRAY, TextAlignment.CENTER).setMarginBottom(3));
        document.add(createParagraph("Email: " + COMPANY_EMAIL + " | Site web: " + COMPANY_WEBSITE, 
                fontNormal, 9, ColorConstants.DARK_GRAY, TextAlignment.CENTER).setMarginBottom(15));

        // Signature area
        Table signatureTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        signatureTable.setWidth(UnitValue.createPercentValue(100));

        Cell emptyCell = new Cell();
        emptyCell.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        signatureTable.addCell(emptyCell);

        Cell signatureCell = new Cell();
        signatureCell.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        signatureCell.add(createParagraph("\n\n\n", fontNormal, 10, ColorConstants.BLACK, TextAlignment.RIGHT));
        signatureCell.add(createParagraph("Signature et cachet", fontNormal, 10, ColorConstants.DARK_GRAY, TextAlignment.RIGHT));
        signatureCell.add(createParagraph("Le Gérant", fontBold, 10, ColorConstants.BLACK, TextAlignment.RIGHT));

        signatureTable.addCell(signatureCell);
        document.add(signatureTable);

        // Legal mentions
        addSeparator(document, 0.5f);
        String legalText = String.format("Facture générée électroniquement le %s - Valable sans signature",
                LocalDateTime.now().format(DATE_TIME_FORMATTER));
        document.add(createParagraph(legalText, fontNormal, 8, ColorConstants.GRAY, TextAlignment.CENTER)
                .setMarginTop(10));
    }

    // ============ HELPER METHODS ============

    private void addDetailRow(Table table, String label, String value) {
        table.addCell(createTableCell(label, fontBold, 11, TextAlignment.LEFT));
        table.addCell(createTableCell(value, fontNormal, 11, TextAlignment.LEFT));
    }

    private Cell createTableHeaderCell(String text) {
        Cell cell = new Cell();
        cell.add(createParagraph(text, fontBold, 11, ColorConstants.WHITE, TextAlignment.CENTER));
        cell.setBackgroundColor(PRIMARY_COLOR);
        cell.setPadding(8);
        cell.setBorder(new SolidBorder(ColorConstants.BLACK, 1));
        return cell;
    }

    private Cell createTableCell(String text, PdfFont font, int fontSize, TextAlignment alignment) {
        Cell cell = new Cell();
        cell.add(createParagraph(text, font, fontSize, ColorConstants.BLACK, alignment));
        cell.setPadding(6);
        cell.setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
        return cell;
    }

    private Cell createEmptyCell() {
        Cell cell = new Cell();
        cell.add(new Paragraph(" "));
        cell.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        return cell;
    }

    private Paragraph createParagraph(String text, PdfFont font, int fontSize, com.itextpdf.kernel.colors.Color color, TextAlignment alignment) {
        return new Paragraph(text)
                .setFont(font)
                .setFontSize(fontSize)
                .setFontColor(color)
                .setTextAlignment(alignment)
                .setMargin(0);
    }

    private String formatCurrency(double amount) {
        return String.format("%,.2f", amount);
    }

    private String getServiceStatusLabel(String status) {
        switch (status) {
            case "TERMINE": return "✓ Terminé";
            case "EN_COURS": return "⟳ En cours";
            case "EN_ATTENTE": return "⏳ En attente";
            default: return status;
        }
    }

    /**
     * Open PDF file with default system viewer
     */
    public void openPdfFile(File pdfFile) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(pdfFile);
        } else {
            throw new IOException("Desktop is not supported - cannot open PDF");
        }
    }
}
