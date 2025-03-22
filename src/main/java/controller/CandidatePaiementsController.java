package controller;

import entite.Payment;
import entite.PaymentInstallment;
import entite.PaymentInstallment.Status;
import entite.User;
import entite.ExamenCode;
import entite.ExamenConduit;
import entite.Profile;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import service.ExamenCodeService;
import service.ExamenConduitService;
import service.PaymentInstallmentService;
import service.PaymentService;
import service.UserService;
import service.AutoEcoleService;
import service.ProfileService;

import Utils.AlertUtils;
import Utils.SessionManager;
import Utils.PDFGenerator;
import Utils.NotificationUtil;
import Utils.NotificationUtil.NotificationType;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CandidatePaiementsController {

    @FXML private VBox examListContainer;
    @FXML private VBox paymentListContainer;
    @FXML private VBox paymentHistoryContainer;

    private final PaymentService paymentService = new PaymentService();
    private final PaymentInstallmentService installmentService = new PaymentInstallmentService();
    private final UserService userService = new UserService();
    private final AutoEcoleService autoEcoleService = new AutoEcoleService();
    private final ProfileService profileService = new ProfileService();

    // For exam code & conduit
    private final ExamenCodeService examenCodeService = new ExamenCodeService();
    private final ExamenConduitService examenConduitService = new ExamenConduitService();

    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            AlertUtils.showAlert(
                    "Erreur",
                    "Session invalide. Veuillez vous reconnecter.",
                    Alert.AlertType.ERROR
            );
            return;
        }
        loadUnpaidExams();
        loadUnpaidPayments();
        loadPaidPayments();
    }

    // ------------------------------------------------------------
    //  Unpaid EXAMS
    // ------------------------------------------------------------
    private void loadUnpaidExams() {
        examListContainer.getChildren().clear();
        boolean foundUnpaidExam = false;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // ExamenCode with paiementStatus = PENDING
        List<ExamenCode> codeExams = examenCodeService.getExamenCodesByCandidatId(currentUser.getId());
        List<ExamenCode> unpaidCodeExams = codeExams.stream()
                .filter(ex -> ex.getPaiementStatus() == ExamenCode.PaymentStatus.PENDING)
                .collect(Collectors.toList());
        for (ExamenCode exam : unpaidCodeExams) {
            foundUnpaidExam = true;
            VBox card = createPaymentCard();
            String labelText = String.format(
                    "Examen Code (Id: %d) – Date: %s – Montant: %.2fTND",
                    exam.getId(),
                    exam.getExamDatetime().format(formatter),
                    exam.getPrice()
            );
            HBox row = createRow(labelText, e -> loadExamPaymentForm(exam, null));
            card.getChildren().add(row);
            examListContainer.getChildren().add(card);
        }

        // ExamenConduit with paiementStatus = PENDING
        List<ExamenConduit> conduitExams = examenConduitService.getExamenConduitsByCandidatId(currentUser.getId());
        List<ExamenConduit> unpaidConduitExams = conduitExams.stream()
                .filter(ex -> ex.getPaiementStatus() == ExamenConduit.PaymentStatus.PENDING)
                .collect(Collectors.toList());
        for (ExamenConduit exam : unpaidConduitExams) {
            foundUnpaidExam = true;
            VBox card = createPaymentCard();
            String labelText = String.format(
                    "Examen Conduit (Id: %d) – Date: %s – Montant: %.2fTND",
                    exam.getId(),
                    exam.getExamDatetime().format(formatter),
                    exam.getPrice()
            );
            HBox row = createRow(labelText, e -> loadExamPaymentForm(null, exam));
            card.getChildren().add(row);
            examListContainer.getChildren().add(card);
        }

        if (!foundUnpaidExam) {
            Label noExamLabel = new Label("Vous n'avez aucun examen impayé.");
            noExamLabel.getStyleClass().add("subtitle");
            examListContainer.getChildren().add(noExamLabel);
        }
    }

    private void loadExamPaymentForm(ExamenCode examCode, ExamenConduit examConduit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/PaymentForm.fxml"));
            Parent paymentForm = loader.load();

            PaymentFormController formController = loader.getController();
            if (examCode != null) {
                formController.setExamenCode(examCode);
            } else if (examConduit != null) {
                formController.setExamenConduit(examConduit);
            }

            StackPane contentArea = (StackPane) examListContainer.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(paymentForm);
            } else {
                AlertUtils.showAlert("Erreur", "Zone de contenu introuvable.", Alert.AlertType.ERROR);
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert("Erreur", "Impossible de charger le formulaire de paiement.", Alert.AlertType.ERROR);
        }
    }

    // ------------------------------------------------------------
    //  Unpaid PAYMENTS
    // ------------------------------------------------------------
    private void loadUnpaidPayments() {
        paymentListContainer.getChildren().clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        boolean foundUnpaid = false;

        List<Payment> payments = paymentService.getPaymentsForUser(currentUser.getId());
        for (Payment p : payments) {
            // 1) Full Payment
            if ("FULL".equalsIgnoreCase(p.getPaymentType())) {
                if ("PENDING".equalsIgnoreCase(p.getStatus())) {
                    foundUnpaid = true;
                    VBox card = createPaymentCard();
                    // Show only type and amount
                    String labelText = "Paiement Comptant - " + p.getTotalAmount() + "TND";
                    HBox row = createRow(labelText, e -> loadPaymentForm(p, null));
                    card.getChildren().add(row);
                    paymentListContainer.getChildren().add(card);
                }
            }
            // 2) Installment Payment
            else if ("INSTALLMENT".equalsIgnoreCase(p.getPaymentType())) {
                List<PaymentInstallment> installments = installmentService.getInstallmentsByPaymentId(p.getId());
                List<PaymentInstallment> pending = installments.stream()
                        .filter(inst -> inst.getStatus() == Status.PENDING)
                        .collect(Collectors.toList());

                if (!pending.isEmpty()) {
                    Optional<LocalDate> minDueDateOpt = pending.stream()
                            .map(PaymentInstallment::getDueDate)
                            .min(Comparator.naturalOrder());
                    if (minDueDateOpt.isPresent()) {
                        LocalDate minDueDate = minDueDateOpt.get();
                        List<PaymentInstallment> nearest = pending.stream()
                                .filter(inst -> inst.getDueDate().equals(minDueDate))
                                .collect(Collectors.toList());
                        if (!nearest.isEmpty()) {
                            foundUnpaid = true;
                            VBox card = createPaymentCard();
                            for (PaymentInstallment inst : nearest) {
                                // Show only type and amount
                                String detailText = "Échéance n°" + inst.getInstallmentNumber()
                                        + " - " + inst.getAmountDue() + "TND";
                                HBox row = createRow(detailText, e -> loadPaymentForm(null, inst));
                                card.getChildren().add(row);
                            }
                            paymentListContainer.getChildren().add(card);
                        }
                    }
                }
            }
        }
        if (!foundUnpaid) {
            Label noUnpaidLabel = new Label("Vous n'avez aucun paiement impayé.");
            noUnpaidLabel.getStyleClass().add("subtitle");
            paymentListContainer.getChildren().add(noUnpaidLabel);
        }
    }

    private void loadPaymentForm(Payment payment, PaymentInstallment installment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/PaymentForm.fxml"));
            Parent paymentForm = loader.load();

            PaymentFormController formController = loader.getController();
            formController.setPayment(payment);
            formController.setInstallment(installment);

            StackPane contentArea = (StackPane) paymentListContainer.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(paymentForm);
            } else {
                AlertUtils.showAlert("Erreur", "Zone de contenu introuvable.", Alert.AlertType.ERROR);
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert("Erreur", "Impossible de charger le formulaire de paiement.", Alert.AlertType.ERROR);
        }
    }

    // ------------------------------------------------------------
    //  PAID HISTORY (with "Imprimer Facture" button)
    // ------------------------------------------------------------
    private void loadPaidPayments() {
        paymentHistoryContainer.getChildren().clear();

        // Unify paid records from different payment sources.
        List<PaidRecord> paidRecords = new ArrayList<>();

        // 1) Paid Full Payments and Installment Payments (global)
        List<Payment> allPayments = paymentService.getPaymentsForUser(currentUser.getId());
        for (Payment p : allPayments) {
            if ("FULL".equalsIgnoreCase(p.getPaymentType()) && "PAID".equalsIgnoreCase(p.getStatus())) {
                PaidRecord rec = new PaidRecord();
                rec.datePaid = p.getPaymentDate().atStartOfDay();
                // Show only type and amount
                rec.description = "Paiement Comptant - " + p.getTotalAmount() + "TND";
                paidRecords.add(rec);
            } else if ("INSTALLMENT".equalsIgnoreCase(p.getPaymentType()) && "PAID".equalsIgnoreCase(p.getStatus())) {
                PaidRecord rec = new PaidRecord();
                rec.datePaid = p.getPaymentDate().atStartOfDay();
                // Show only type and amount
                rec.description = "Paiement par Échéances - " + p.getTotalAmount() + "TND";
                paidRecords.add(rec);
            }
        }

        // 2) Paid Installments details
        for (Payment p : allPayments) {
            if ("INSTALLMENT".equalsIgnoreCase(p.getPaymentType())) {
                List<PaymentInstallment> allInstallments = installmentService.getInstallmentsByPaymentId(p.getId());
                allInstallments.stream()
                        .filter(inst -> inst.getStatus() == Status.PAID && inst.getDatePaid() != null)
                        .forEach(inst -> {
                            PaidRecord rec = new PaidRecord();
                            rec.datePaid = inst.getDatePaid().atStartOfDay();
                            // Show only type and amount
                            rec.description = "Échéance n°" + inst.getInstallmentNumber()
                                    + " - " + inst.getAmountDue() + "TND";
                            paidRecords.add(rec);
                        });
            }
        }

        // 3) Paid ExamenCode (keep as-is)
        List<ExamenCode> codeExams = examenCodeService.getExamenCodesByCandidatId(currentUser.getId());
        codeExams.stream()
                .filter(ex -> ex.getPaiementStatus() == ExamenCode.PaymentStatus.PAID && ex.getPaymentDate() != null)
                .forEach(ex -> {
                    PaidRecord rec = new PaidRecord();
                    rec.datePaid = ex.getPaymentDate().atStartOfDay();
                    // Keep exam details as they are
                    rec.description = String.format(
                            "Examen Code (Id:%d) – Montant: %.2fTND - Payé le %s",
                            ex.getId(),
                            ex.getPrice(),
                            ex.getPaymentDate()
                    );
                    paidRecords.add(rec);
                });

        // 4) Paid ExamenConduit (keep as-is)
        List<ExamenConduit> conduitExams = examenConduitService.getExamenConduitsByCandidatId(currentUser.getId());
        conduitExams.stream()
                .filter(ex -> ex.getPaiementStatus() == ExamenConduit.PaymentStatus.PAID && ex.getPaymentDate() != null)
                .forEach(ex -> {
                    PaidRecord rec = new PaidRecord();
                    rec.datePaid = ex.getPaymentDate().atStartOfDay();
                    // Keep exam details as they are
                    rec.description = String.format(
                            "Examen Conduit (Id:%d) – Montant: %.2fTND - Payé le %s",
                            ex.getId(),
                            ex.getPrice(),
                            ex.getPaymentDate()
                    );
                    paidRecords.add(rec);
                });

        if (paidRecords.isEmpty()) {
            Label noPaidLabel = new Label("Aucun paiement dans l'historique.");
            noPaidLabel.getStyleClass().add("subtitle");
            paymentHistoryContainer.getChildren().add(noPaidLabel);
            return;
        }

        // Sort the records in descending order by datePaid.
        paidRecords.sort(Comparator.comparing(PaidRecord::getDatePaid).reversed());

        // Display each record with an "Imprimer Facture" button.
        paidRecords.forEach(rec -> {
            VBox card = createPaymentCard();
            HBox row = createHistoryRow(rec.description, rec.description);
            card.getChildren().add(row);
            paymentHistoryContainer.getChildren().add(card);
        });
    }

    // ------------------------------------------------------------
    //  Utility methods
    // ------------------------------------------------------------
    private VBox createPaymentCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        return card;
    }

    private HBox createRow(String labelText, javafx.event.EventHandler<javafx.event.ActionEvent> payHandler) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Label detailLabel = new Label(labelText);
        detailLabel.getStyleClass().add("subtitle");
        Button payButton = new Button("Payer");
        payButton.getStyleClass().add("inspect-button");
        payButton.setOnAction(payHandler);
        row.getChildren().addAll(detailLabel, payButton);
        return row;
    }

    private HBox createHistoryRow(String labelText, String invoiceContent) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Label detailLabel = new Label(labelText);
        detailLabel.getStyleClass().add("subtitle");
        Button printButton = new Button("Imprimer Facture");
        printButton.getStyleClass().add("inspect-button");
        printButton.setOnAction(e -> printInvoice(invoiceContent));
        row.getChildren().addAll(detailLabel, printButton);
        return row;
    }

    private void printInvoice(String invoiceContent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer la facture");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(paymentHistoryContainer.getScene().getWindow());
        if (file != null) {
            try {
                List<String[]> autoEcoleData = autoEcoleService.getAutoEcoleData();
                String header;
                String footer;
                if (!autoEcoleData.isEmpty()) {
                    String[] data = autoEcoleData.get(0);
                    header = "Auto-école: " + data[0] + "\nAdresse: " + data[1];
                    footer = "Contact: " + data[2] + " | Email: " + data[3];
                } else {
                    header = "Auto-école";
                    footer = "";
                }
                Optional<Profile> profileOpt = profileService.getProfileByUserId(currentUser.getId());
                String candidateDetails = profileOpt.map(p -> "Candidat: " + p.getFullName())
                        .orElse("Candidat: " + currentUser.getUsername());

                PDFGenerator.generateInvoice(header, candidateDetails, invoiceContent, footer, file);
                showSuccessNotification("Facture générée avec succès !");
            } catch (Exception ex) {
                ex.printStackTrace();
                AlertUtils.showAlert("Erreur", "Impossible de générer la facture.", Alert.AlertType.ERROR);
            }
        }
    }

    private void showSuccessNotification(String message) {
        StackPane contentArea = (StackPane) paymentHistoryContainer.getScene().lookup("#contentArea");
        if (contentArea != null) {
            NotificationUtil.showNotification(contentArea, message, NotificationType.SUCCESS);
        }
    }

    // Inner class to unify “paid” items.
    private static class PaidRecord {
        private LocalDateTime datePaid;
        private String description;

        public LocalDateTime getDatePaid() {
            return datePaid;
        }

        public String getDescription() {
            return description;
        }
    }
}
