package controller;

import entite.Payment;
import entite.PaymentInstallment;
import entite.PaymentInstallment.Status;
import entite.User;

import entite.ExamenCode;      // For code exam
import entite.ExamenConduit;  // For conduit exam

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

import service.ExamenCodeService;
import service.ExamenConduitService;
import service.PaymentInstallmentService;
import service.PaymentService;
import service.UserService;

import Utils.AlertUtils;
import Utils.SessionManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This controller displays:
 *  - Unpaid Exams (Code & Conduit) & Unpaid Payments (Full & Installment)
 *  - Payment History (PAID items for Payment, Installment, or Examen)
 */
public class CandidatePaiementsController {

    @FXML private VBox examListContainer;       // Container for unpaid exams
    @FXML private VBox paymentListContainer;    // Container for unpaid payments
    @FXML private VBox paymentHistoryContainer; // Container for paid items (both payments & exam)

    private final PaymentService paymentService = new PaymentService();
    private final PaymentInstallmentService installmentService = new PaymentInstallmentService();
    private final UserService userService = new UserService();

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
                    javafx.scene.control.Alert.AlertType.ERROR
            );
            return;
        }
        // 1) Display exams that are unpaid
        loadUnpaidExams();
        // 2) Display payments that are unpaid
        loadUnpaidPayments();
        // 3) Display paid history (exams + payments)
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
            // “Payer” => open PaymentForm with this ExamenCode
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
            // “Payer” => open PaymentForm with this ExamenConduit
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
                    String labelText = "Paiement Comptant – Montant: " + p.getTotalAmount() +
                            "TND - Date: " + p.getPaymentDate().format(formatter);
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
                    // Find the earliest due date among pending installments
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
                                String detailText = "Échéance n°" + inst.getInstallmentNumber() +
                                        " – Montant: " + inst.getAmountDue() + "TND - Due: " + inst.getDueDate().format(formatter);
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
    //  PAID HISTORY (the main fix)
    // ------------------------------------------------------------
    private void loadPaidPayments() {
        paymentHistoryContainer.getChildren().clear();

        // We want to show 4 categories of "paid" items in the same list, sorted by date:
        //   1) Full Payment  (payment.status = "PAID")
        //   2) Installments (installment.status = PENDING vs PAID)
        //   3) ExamenCode   (paiementStatus = PAID)
        //   4) ExamenConduit (paiementStatus = PAID)
        // We'll unify them into a list of “PaidRecord”, each having a datePaid + description.

        List<PaidRecord> paidRecords = new ArrayList<>();

        // 1) Paid Full Payments
        List<Payment> allPayments = paymentService.getPaymentsForUser(currentUser.getId());
        for (Payment p : allPayments) {
            if ("FULL".equalsIgnoreCase(p.getPaymentType()) && "PAID".equalsIgnoreCase(p.getStatus())) {
                PaidRecord rec = new PaidRecord();
                rec.datePaid = p.getPaymentDate().atStartOfDay();  // Because Payment date is LocalDate
                rec.description = "Paiement Comptant – " + p.getTotalAmount() + "TND - Payé le " + p.getPaymentDate();
                paidRecords.add(rec);
            }
            else if ("INSTALLMENT".equalsIgnoreCase(p.getPaymentType()) && "PAID".equalsIgnoreCase(p.getStatus())) {
                // If the entire payment is "PAID", that means all installments are paid.
                // We'll show a single line or each paid installment separately. Up to you.

                // Approach A: single line summary for the payment
                PaidRecord rec = new PaidRecord();
                rec.datePaid = p.getPaymentDate().atStartOfDay();
                rec.description = "Paiement par Échéances – Montant Total: " + p.getTotalAmount() + "TND - Tout payé le " + p.getPaymentDate();
                paidRecords.add(rec);

                // Approach B: we could also break out each installment. If you want:
                // (We will show each installment in detail below.)
            }
        }

        // 2) Paid Installments (some partial installments may have datePaid before entire Payment is "PAID")
        for (Payment p : allPayments) {
            if ("INSTALLMENT".equalsIgnoreCase(p.getPaymentType())) {
                // fetch installments
                List<PaymentInstallment> allInstallments = installmentService.getInstallmentsByPaymentId(p.getId());
                for (PaymentInstallment inst : allInstallments) {
                    if (inst.getStatus() == Status.PAID && inst.getDatePaid() != null) {
                        PaidRecord rec = new PaidRecord();
                        // datePaid is a LocalDate, so we do atStartOfDay
                        rec.datePaid = inst.getDatePaid().atStartOfDay();
                        rec.description = "Échéance n°" + inst.getInstallmentNumber() +
                                " – Montant: " + inst.getAmountDue() + "TND - Payé le " + inst.getDatePaid();
                        paidRecords.add(rec);
                    }
                }
            }
        }

        // 3) Paid ExamenCode
        List<ExamenCode> codeExams = examenCodeService.getExamenCodesByCandidatId(currentUser.getId());
        codeExams.stream()
                .filter(ex -> ex.getPaiementStatus() == ExamenCode.PaymentStatus.PAID && ex.getPaymentDate() != null)
                .forEach(ex -> {
                    PaidRecord rec = new PaidRecord();
                    rec.datePaid = ex.getPaymentDate().atStartOfDay();  // exam payment date is a LocalDate
                    rec.description = String.format(
                            "Examen Code (Id:%d) – Montant: %.2fTND - Payé le %s",
                            ex.getId(),
                            ex.getPrice(),
                            ex.getPaymentDate()
                    );
                    paidRecords.add(rec);
                });

        // 4) Paid ExamenConduit
        List<ExamenConduit> conduitExams = examenConduitService.getExamenConduitsByCandidatId(currentUser.getId());
        conduitExams.stream()
                .filter(ex -> ex.getPaiementStatus() == ExamenConduit.PaymentStatus.PAID && ex.getPaymentDate() != null)
                .forEach(ex -> {
                    PaidRecord rec = new PaidRecord();
                    rec.datePaid = ex.getPaymentDate().atStartOfDay();
                    rec.description = String.format(
                            "Examen Conduit (Id:%d) – Montant: %.2fTND - Payé le %s",
                            ex.getId(),
                            ex.getPrice(),
                            ex.getPaymentDate()
                    );
                    paidRecords.add(rec);
                });

        // If we have no records, display a "none found" label
        if (paidRecords.isEmpty()) {
            Label noPaidLabel = new Label("Aucun paiement dans l'historique.");
            noPaidLabel.getStyleClass().add("subtitle");
            paymentHistoryContainer.getChildren().add(noPaidLabel);
            return;
        }

        // Otherwise, sort them all descending by datePaid
        paidRecords.sort(Comparator.comparing(PaidRecord::getDatePaid).reversed());

        // Display them
        for (PaidRecord rec : paidRecords) {
            VBox card = createPaymentCard();
            // We can create a row with rec.description
            HBox row = createHistoryRow(rec.description);
            card.getChildren().add(row);
            paymentHistoryContainer.getChildren().add(card);
        }
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

    private HBox createHistoryRow(String labelText) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Label detailLabel = new Label(labelText);
        detailLabel.getStyleClass().add("subtitle");
        row.getChildren().add(detailLabel);
        return row;
    }

    // ------------------------------------------------------------
    //  Inner class to unify “paid” items
    // ------------------------------------------------------------
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
