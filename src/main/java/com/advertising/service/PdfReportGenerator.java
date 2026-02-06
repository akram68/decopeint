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
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.io.font.constants.StandardFonts;
import javafx.collections.ObservableList;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PdfReportGenerator {

    private static final String COMPANY_NAME = "DECOPEINT";
    private static final String COMPANY_SUBTITLE = "Services d'Impression & Publicité Professionnelle";
    private static final String COMPANY_PHONE = "+213 XX XX XX XX";
    private static final String COMPANY_EMAIL = "contact@decopeint.dz";
    private static final String COMPANY_ADDRESS = "Alger, Algérie";
    private static final String COMPANY_WEBSITE = "www.decopeint.dz";

    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(44, 62, 80);
    private static final DeviceRgb ACCENT_COLOR = new DeviceRgb(52, 152, 219);
    private static final DeviceRgb SUCCESS_COLOR = new DeviceRgb(46, 204, 113);
    private static final DeviceRgb DANGER_COLOR = new DeviceRgb(231, 76, 60);
    private static final DeviceRgb WARNING_COLOR = new DeviceRgb(243, 156, 18);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(236, 240, 241);
    private static final DeviceRgb DARK_GRAY = new DeviceRgb(127, 140, 141);

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm", Locale.FRENCH);
    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);

    private PdfFont fontNormal;
    private PdfFont fontBold;
    private PdfFont fontItalic;

    private final File outputDirectory;

    public PdfReportGenerator() {
        this(new File("factures"));
    }

    public PdfReportGenerator(File outputDirectory) {
        this.outputDirectory = outputDirectory;
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
        initializeFonts();
    }

    private void initializeFonts() {
        try {
            fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            fontItalic = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize PDF fonts", e);
        }
    }

    public File generateServiceInvoice(Service service,
                                      ObservableList<Payment> payments,
                                      ServiceManager.ClientDetails clientDetails) throws IOException {

        String fileName = String.format("FACTURE_%06d_%s.pdf",
                service.getId(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

        File pdfFile = new File(outputDirectory, fileName);

        PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        document.setMargins(30, 30, 30, 30);

        try {
            addProfessionalHeader(document, service);
            addHorizontalLine(document, ACCENT_COLOR, 3);
            document.add(new Paragraph("\n"));

            addClientInfoSection(document, clientDetails);
            document.add(new Paragraph("\n"));

            addServiceDetailsSection(document, service);
            document.add(new Paragraph("\n"));

            addFinancialSummaryTable(document, service);
            document.add(new Paragraph("\n"));

            if (payments != null && !payments.isEmpty()) {
                addPaymentHistoryTable(document, payments);
                document.add(new Paragraph("\n"));
            }

            addPaymentInstructions(document, service);
            document.add(new Paragraph("\n"));

            addFooter(document);

        } finally {
            document.close();
        }

        return pdfFile;
    }

    private void addProfessionalHeader(Document document, Service service) {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}));
        headerTable.setWidth(UnitValue.createPercentValue(100));

        Cell companyCell = new Cell();
        companyCell.setBorder(Border.NO_BORDER);
        companyCell.setVerticalAlignment(VerticalAlignment.TOP);

        Paragraph companyName = new Paragraph(COMPANY_NAME)
            .setFont(fontBold)
            .setFontSize(26)
            .setFontColor(PRIMARY_COLOR)
            .setMarginBottom(2);
        companyCell.add(companyName);

        Paragraph companySubtitle = new Paragraph(COMPANY_SUBTITLE)
            .setFont(fontItalic)
            .setFontSize(11)
            .setFontColor(DARK_GRAY)
            .setMarginBottom(8);
        companyCell.add(companySubtitle);

        Paragraph contactInfo = new Paragraph()
            .setFont(fontNormal)
            .setFontSize(9)
            .setFontColor(ColorConstants.DARK_GRAY)
            .add("Tél: " + COMPANY_PHONE + "\n")
            .add("Email: " + COMPANY_EMAIL + "\n")
            .add("Adresse: " + COMPANY_ADDRESS + "\n")
            .add("Web: " + COMPANY_WEBSITE);
        companyCell.add(contactInfo);

        headerTable.addCell(companyCell);

        Cell invoiceCell = new Cell();
        invoiceCell.setBorder(Border.NO_BORDER);
        invoiceCell.setVerticalAlignment(VerticalAlignment.TOP);
        invoiceCell.setTextAlignment(TextAlignment.RIGHT);

        Paragraph invoiceTitle = new Paragraph("FACTURE")
            .setFont(fontBold)
            .setFontSize(32)
            .setFontColor(ACCENT_COLOR)
            .setMarginBottom(5);
        invoiceCell.add(invoiceTitle);

        Paragraph invoiceNumber = new Paragraph("N° " + String.format("%06d", service.getId()))
            .setFont(fontBold)
            .setFontSize(16)
            .setFontColor(PRIMARY_COLOR)
            .setMarginBottom(3);
        invoiceCell.add(invoiceNumber);

        Paragraph invoiceDate = new Paragraph("Date: " + LocalDateTime.now().format(DATE_FORMATTER))
            .setFont(fontNormal)
            .setFontSize(10)
            .setFontColor(DARK_GRAY);
        invoiceCell.add(invoiceDate);

        headerTable.addCell(invoiceCell);

        document.add(headerTable);
    }

    private void addHorizontalLine(Document document, DeviceRgb color, float thickness) {
        Table line = new Table(1);
        line.setWidth(UnitValue.createPercentValue(100));
        Cell lineCell = new Cell();
        lineCell.setBorder(Border.NO_BORDER);
        lineCell.setBorderBottom(new SolidBorder(color, thickness));
        lineCell.setPadding(0);
        line.addCell(lineCell);
        document.add(line);
    }

    private void addClientInfoSection(Document document, ServiceManager.ClientDetails clientDetails) {
        Table clientTable = new Table(1);
        clientTable.setWidth(UnitValue.createPercentValue(60));
        clientTable.setMarginBottom(5);

        Cell headerCell = new Cell();
        headerCell.add(new Paragraph("INFORMATIONS CLIENT")
            .setFont(fontBold)
            .setFontSize(12)
            .setFontColor(ColorConstants.WHITE));
        headerCell.setBackgroundColor(PRIMARY_COLOR);
        headerCell.setPadding(8);
        headerCell.setBorder(Border.NO_BORDER);
        clientTable.addCell(headerCell);

        Cell contentCell = new Cell();
        contentCell.setBackgroundColor(LIGHT_GRAY);
        contentCell.setPadding(12);
        contentCell.setBorder(new SolidBorder(DARK_GRAY, 1));

        StringBuilder clientInfo = new StringBuilder();
        clientInfo.append(clientDetails.getNom() != null ? clientDetails.getNom() : "N/A");
        if (clientDetails.getTelephone() != null && !clientDetails.getTelephone().isEmpty()) {
            clientInfo.append("\nTél: ").append(clientDetails.getTelephone());
        }
        if (clientDetails.getEmail() != null && !clientDetails.getEmail().isEmpty()) {
            clientInfo.append("\nEmail: ").append(clientDetails.getEmail());
        }
        if (clientDetails.getAdresse() != null && !clientDetails.getAdresse().isEmpty()) {
            clientInfo.append("\nAdresse: ").append(clientDetails.getAdresse());
        }

        contentCell.add(new Paragraph(clientInfo.toString())
            .setFont(fontNormal)
            .setFontSize(10)
            .setFontColor(ColorConstants.BLACK));
        clientTable.addCell(contentCell);

        document.add(clientTable);
    }

    private void addServiceDetailsSection(Document document, Service service) {
        Table serviceTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        serviceTable.setWidth(UnitValue.createPercentValue(100));

        Cell headerCell = new Cell(1, 2);
        headerCell.add(new Paragraph("DÉTAILS DU SERVICE")
            .setFont(fontBold)
            .setFontSize(12)
            .setFontColor(ColorConstants.WHITE));
        headerCell.setBackgroundColor(PRIMARY_COLOR);
        headerCell.setPadding(8);
        headerCell.setBorder(Border.NO_BORDER);
        serviceTable.addCell(headerCell);

        addServiceDetailRow(serviceTable, "Type de service:", service.getType());
        addServiceDetailRow(serviceTable, "Description:",
            service.getDescription() != null && !service.getDescription().isEmpty()
                ? service.getDescription() : "N/A");
        addServiceDetailRow(serviceTable, "Statut:", getStatusLabel(service.getStatutService()));
        addServiceDetailRow(serviceTable, "Date création:", service.getDateFormatted());

        document.add(serviceTable);
    }

    private void addServiceDetailRow(Table table, String label, String value) {
        Cell labelCell = new Cell();
        labelCell.add(new Paragraph(label)
            .setFont(fontBold)
            .setFontSize(10)
            .setFontColor(PRIMARY_COLOR));
        labelCell.setBackgroundColor(LIGHT_GRAY);
        labelCell.setPadding(8);
        labelCell.setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
        table.addCell(labelCell);

        Cell valueCell = new Cell();
        valueCell.add(new Paragraph(value)
            .setFont(fontNormal)
            .setFontSize(10)
            .setFontColor(ColorConstants.BLACK));
        valueCell.setPadding(8);
        valueCell.setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
        table.addCell(valueCell);
    }

    private void addFinancialSummaryTable(Document document, Service service) {
        Table financialTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}));
        financialTable.setWidth(UnitValue.createPercentValue(100));

        Cell headerCell1 = new Cell();
        headerCell1.add(new Paragraph("RÉSUMÉ FINANCIER")
            .setFont(fontBold)
            .setFontSize(12)
            .setFontColor(ColorConstants.WHITE));
        headerCell1.setBackgroundColor(PRIMARY_COLOR);
        headerCell1.setPadding(8);
        headerCell1.setBorder(Border.NO_BORDER);
        financialTable.addCell(headerCell1);

        Cell headerCell2 = new Cell();
        headerCell2.add(new Paragraph("MONTANT (DZD)")
            .setFont(fontBold)
            .setFontSize(12)
            .setFontColor(ColorConstants.WHITE)
            .setTextAlignment(TextAlignment.RIGHT));
        headerCell2.setBackgroundColor(PRIMARY_COLOR);
        headerCell2.setPadding(8);
        headerCell2.setBorder(Border.NO_BORDER);
        financialTable.addCell(headerCell2);

        addFinancialRow(financialTable, "Service: " + service.getType(),
            service.getPrixTotal(), ColorConstants.WHITE, false);

        Cell spacerCell1 = new Cell();
        spacerCell1.setBorder(Border.NO_BORDER);
        spacerCell1.setHeight(5);
        financialTable.addCell(spacerCell1);

        Cell spacerCell2 = new Cell();
        spacerCell2.setBorder(Border.NO_BORDER);
        spacerCell2.setHeight(5);
        financialTable.addCell(spacerCell2);

        addFinancialRow(financialTable, "SOUS-TOTAL",
            service.getPrixTotal(), LIGHT_GRAY, true);

        DeviceRgb paidBg = new DeviceRgb(212, 237, 218);
        addFinancialRow(financialTable, "Montant déjà payé",
            service.getMontantPaye(), paidBg, false, SUCCESS_COLOR);

        boolean fullyPaid = service.getReste() < 0.01;
        DeviceRgb resteBg = fullyPaid ? paidBg : new DeviceRgb(248, 215, 218);
        DeviceRgb resteColor = fullyPaid ? SUCCESS_COLOR : DANGER_COLOR;

        Cell labelCell = new Cell();
        labelCell.add(new Paragraph("RESTE À PAYER")
            .setFont(fontBold)
            .setFontSize(14)
            .setFontColor(PRIMARY_COLOR));
        labelCell.setBackgroundColor(resteBg);
        labelCell.setPadding(10);
        labelCell.setBorder(new SolidBorder(PRIMARY_COLOR, 2));
        financialTable.addCell(labelCell);

        Cell valueCell = new Cell();
        valueCell.add(new Paragraph(formatCurrency(service.getReste()))
            .setFont(fontBold)
            .setFontSize(14)
            .setFontColor(resteColor)
            .setTextAlignment(TextAlignment.RIGHT));
        valueCell.setBackgroundColor(resteBg);
        valueCell.setPadding(10);
        valueCell.setBorder(new SolidBorder(PRIMARY_COLOR, 2));
        financialTable.addCell(valueCell);

        document.add(financialTable);

        double completionPercentage = (service.getMontantPaye() / service.getPrixTotal()) * 100;
        String statusText;
        DeviceRgb statusColor;

        if (fullyPaid) {
            statusText = String.format("Paiement complété à 100%% - PAYÉ");
            statusColor = SUCCESS_COLOR;
        } else {
            statusText = String.format("Paiement complété à %.1f%% - RESTE À PAYER", completionPercentage);
            statusColor = DANGER_COLOR;
        }

        Paragraph statusPara = new Paragraph(statusText)
            .setFont(fontBold)
            .setFontSize(11)
            .setFontColor(statusColor)
            .setTextAlignment(TextAlignment.RIGHT)
            .setMarginTop(5);
        document.add(statusPara);
    }

    private void addFinancialRow(Table table, String label, double amount,
                                 DeviceRgb backgroundColor, boolean isBold) {
        addFinancialRow(table, label, amount, backgroundColor, isBold, ColorConstants.BLACK);
    }

    private void addFinancialRow(Table table, String label, double amount,
                                 DeviceRgb backgroundColor, boolean isBold, DeviceRgb textColor) {
        Cell labelCell = new Cell();
        Paragraph labelPara = new Paragraph(label)
            .setFont(isBold ? fontBold : fontNormal)
            .setFontSize(isBold ? 11 : 10)
            .setFontColor(ColorConstants.BLACK);
        labelCell.add(labelPara);
        labelCell.setBackgroundColor(backgroundColor);
        labelCell.setPadding(8);
        labelCell.setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
        table.addCell(labelCell);

        Cell valueCell = new Cell();
        Paragraph valuePara = new Paragraph(formatCurrency(amount))
            .setFont(isBold ? fontBold : fontNormal)
            .setFontSize(isBold ? 11 : 10)
            .setFontColor(textColor)
            .setTextAlignment(TextAlignment.RIGHT);
        valueCell.add(valuePara);
        valueCell.setBackgroundColor(backgroundColor);
        valueCell.setPadding(8);
        valueCell.setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
        table.addCell(valueCell);
    }

    private void addPaymentHistoryTable(Document document, ObservableList<Payment> payments) {
        Paragraph historyTitle = new Paragraph("HISTORIQUE DES PAIEMENTS")
            .setFont(fontBold)
            .setFontSize(12)
            .setFontColor(PRIMARY_COLOR)
            .setMarginBottom(5);
        document.add(historyTitle);

        Table paymentTable = new Table(UnitValue.createPercentArray(new float[]{10, 40, 30, 20}));
        paymentTable.setWidth(UnitValue.createPercentValue(100));

        addTableHeader(paymentTable, "N°");
        addTableHeader(paymentTable, "DATE");
        addTableHeader(paymentTable, "MONTANT (DZD)");
        addTableHeader(paymentTable, "MODE");

        double totalPaid = 0;
        int index = 1;
        for (Payment payment : payments) {
            addPaymentRow(paymentTable, String.valueOf(index++),
                         payment.getDateFormatted(),
                         formatCurrency(payment.getMontant()),
                         payment.getModePaiement(),
                         index % 2 == 0);
            totalPaid += payment.getMontant();
        }

        Cell totalLabelCell = new Cell(1, 3);
        totalLabelCell.add(new Paragraph("TOTAL ENCAISSÉ")
            .setFont(fontBold)
            .setFontSize(11)
            .setFontColor(ColorConstants.WHITE)
            .setTextAlignment(TextAlignment.RIGHT));
        totalLabelCell.setBackgroundColor(SUCCESS_COLOR);
        totalLabelCell.setPadding(8);
        totalLabelCell.setBorder(Border.NO_BORDER);
        paymentTable.addCell(totalLabelCell);

        Cell totalValueCell = new Cell();
        totalValueCell.add(new Paragraph(formatCurrency(totalPaid))
            .setFont(fontBold)
            .setFontSize(11)
            .setFontColor(ColorConstants.WHITE)
            .setTextAlignment(TextAlignment.RIGHT));
        totalValueCell.setBackgroundColor(SUCCESS_COLOR);
        totalValueCell.setPadding(8);
        totalValueCell.setBorder(Border.NO_BORDER);
        paymentTable.addCell(totalValueCell);

        document.add(paymentTable);
    }

    private void addTableHeader(Table table, String text) {
        Cell cell = new Cell();
        cell.add(new Paragraph(text)
            .setFont(fontBold)
            .setFontSize(10)
            .setFontColor(ColorConstants.WHITE)
            .setTextAlignment(TextAlignment.CENTER));
        cell.setBackgroundColor(ACCENT_COLOR);
        cell.setPadding(8);
        cell.setBorder(Border.NO_BORDER);
        table.addCell(cell);
    }

    private void addPaymentRow(Table table, String num, String date,
                               String amount, String mode, boolean alternate) {
        DeviceRgb bgColor = alternate ? ColorConstants.WHITE : LIGHT_GRAY;

        addPaymentCell(table, num, TextAlignment.CENTER, bgColor);
        addPaymentCell(table, date, TextAlignment.LEFT, bgColor);
        addPaymentCell(table, amount, TextAlignment.RIGHT, bgColor);
        addPaymentCell(table, mode, TextAlignment.LEFT, bgColor);
    }

    private void addPaymentCell(Table table, String text, TextAlignment alignment, DeviceRgb bgColor) {
        Cell cell = new Cell();
        cell.add(new Paragraph(text)
            .setFont(fontNormal)
            .setFontSize(9)
            .setFontColor(ColorConstants.BLACK)
            .setTextAlignment(alignment));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        cell.setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
        table.addCell(cell);
    }

    private void addPaymentInstructions(Document document, Service service) {
        Table instructionsTable = new Table(1);
        instructionsTable.setWidth(UnitValue.createPercentValue(100));

        Cell instructionsCell = new Cell();
        instructionsCell.setBackgroundColor(new DeviceRgb(255, 250, 230));
        instructionsCell.setPadding(12);
        instructionsCell.setBorder(new SolidBorder(WARNING_COLOR, 1));

        instructionsCell.add(new Paragraph("INFORMATIONS DE PAIEMENT")
            .setFont(fontBold)
            .setFontSize(11)
            .setFontColor(PRIMARY_COLOR)
            .setMarginBottom(8));

        String[] instructions = {
            "Veuillez effectuer le paiement sous 30 jours à compter de la date de facturation.",
            "Modes de paiement acceptés: Espèces, Chèque, Virement bancaire, Carte bancaire",
            "Merci de mentionner le numéro de facture lors du paiement: " + String.format("%06d", service.getId()),
            "Pour toute question, contactez-nous au " + COMPANY_PHONE
        };

        for (String instruction : instructions) {
            instructionsCell.add(new Paragraph("   " + instruction)
                .setFont(fontNormal)
                .setFontSize(9)
                .setFontColor(ColorConstants.BLACK)
                .setMarginBottom(3));
        }

        instructionsTable.addCell(instructionsCell);
        document.add(instructionsTable);
    }

    private void addFooter(Document document) {
        addHorizontalLine(document, LIGHT_GRAY, 1);

        Paragraph thankYou = new Paragraph("Merci pour votre confiance")
            .setFont(fontBold)
            .setFontSize(13)
            .setFontColor(PRIMARY_COLOR)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(8)
            .setMarginBottom(5);
        document.add(thankYou);

        Paragraph contact = new Paragraph(
            "Pour toute question: " + COMPANY_PHONE + " | " + COMPANY_EMAIL + " | " + COMPANY_WEBSITE)
            .setFont(fontNormal)
            .setFontSize(8)
            .setFontColor(DARK_GRAY)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(10);
        document.add(contact);

        Table signatureTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        signatureTable.setWidth(UnitValue.createPercentValue(100));

        Cell emptyCell = new Cell();
        emptyCell.setBorder(Border.NO_BORDER);
        signatureTable.addCell(emptyCell);

        Cell signatureCell = new Cell();
        signatureCell.setBorder(Border.NO_BORDER);
        signatureCell.setHeight(60);
        signatureCell.add(new Paragraph("\n\n\nSignature et cachet")
            .setFont(fontItalic)
            .setFontSize(9)
            .setFontColor(DARK_GRAY)
            .setTextAlignment(TextAlignment.RIGHT));
        signatureCell.add(new Paragraph("Le Gérant")
            .setFont(fontBold)
            .setFontSize(10)
            .setFontColor(ColorConstants.BLACK)
            .setTextAlignment(TextAlignment.RIGHT));
        signatureTable.addCell(signatureCell);

        document.add(signatureTable);

        addHorizontalLine(document, LIGHT_GRAY, 0.5f);

        String legalText = String.format(
            "Document généré électroniquement le %s - Valable sans signature",
            LocalDateTime.now().format(DATE_TIME_FORMATTER));

        Paragraph legal = new Paragraph(legalText)
            .setFont(fontItalic)
            .setFontSize(7)
            .setFontColor(ColorConstants.GRAY)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(5);
        document.add(legal);
    }

    private String formatCurrency(double amount) {
        return String.format("%,.2f", amount);
    }

    private String getStatusLabel(String status) {
        switch (status) {
            case "TERMINE": return "Terminé";
            case "EN_COURS": return "En cours";
            case "EN_ATTENTE": return "En attente";
            default: return status;
        }
    }

    public void openPdfFile(File pdfFile) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(pdfFile);
        } else {
            throw new IOException("Desktop is not supported - cannot open PDF");
        }
    }
}
