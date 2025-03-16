package controller;

import entite.Payment;
import entite.PaymentInstallment;
import entite.PaymentInstallment.Status;
import entite.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import service.PaymentInstallmentService;
import service.PaymentService;
import service.UserService;
import Utils.AlertUtils;
import Utils.SessionManager;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CandidatePaiementsController {

    @FXML private VBox paymentListContainer;
    @FXML private VBox paymentHistoryContainer;

    private final PaymentService paymentService = new PaymentService();
    private final PaymentInstallmentService installmentService = new PaymentInstallmentService();
    private final UserService userService = new UserService();
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            AlertUtils.showAlert("Erreur", "Session invalide. Veuillez vous reconnecter.", javafx.scene.control.Alert.AlertType.ERROR);
            return;
        }
        loadUnpaidPayments();
        loadPaidPayments();
    }

    private void loadUnpaidPayments() {
        paymentListContainer.getChildren().clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        boolean foundUnpaid = false;

        List<Payment> payments = paymentService.getPaymentsForUser(currentUser.getId());
        for (Payment p : payments) {
            if ("FULL".equalsIgnoreCase(p.getPaymentType())) {
                if ("PENDING".equalsIgnoreCase(p.getStatus())) {
                    foundUnpaid = true;
                    VBox card = createPaymentCard();
                    String labelText = "Paiement Comptant – Montant: " + p.getTotalAmount() + "€ - Date: " + p.getPaymentDate().format(formatter);
                    HBox row = createRow(labelText, e -> loadPaymentForm(p, null));
                    card.getChildren().add(row);
                    paymentListContainer.getChildren().add(card);
                }
            } else if ("INSTALLMENT".equalsIgnoreCase(p.getPaymentType())) {
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
                                String detailText = "Installment Échéance " + inst.getInstallmentNumber() +
                                        " – Montant: " + inst.getAmountDue() + "€ - Due: " + inst.getDueDate().format(formatter);
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

    private void loadPaidPayments() {
        paymentHistoryContainer.getChildren().clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        boolean foundPaid = false;

        List<Payment> payments = paymentService.getPaymentsForUser(currentUser.getId());
        for (Payment p : payments) {
            if ("FULL".equalsIgnoreCase(p.getPaymentType()) && "PAID".equalsIgnoreCase(p.getStatus())) {
                foundPaid = true;
                VBox card = createPaymentCard();
                String detailText = "Paiement Comptant – Montant: " + p.getTotalAmount()
                        + "€ - Payé le: " + p.getPaymentDate().format(formatter);
                HBox row = createHistoryRow(detailText);
                card.getChildren().add(row);
                paymentHistoryContainer.getChildren().add(card);
            } else if ("INSTALLMENT".equalsIgnoreCase(p.getPaymentType())) {
                List<PaymentInstallment> installments = installmentService.getInstallmentsByPaymentId(p.getId());
                List<PaymentInstallment> paidInst = installments.stream()
                        .filter(inst -> inst.getStatus() == Status.PAID)
                        .collect(Collectors.toList());
                if (!paidInst.isEmpty()) {
                    foundPaid = true;
                    VBox card = createPaymentCard();
                    for (PaymentInstallment inst : paidInst) {
                        String datePaidStr = (inst.getDatePaid() != null) ? inst.getDatePaid().format(formatter) : "inconnu";
                        String detailText = "Installment Échéance " + inst.getInstallmentNumber()
                                + " – Montant: " + inst.getAmountDue() + "€ - Payé le: " + datePaidStr;
                        HBox row = createHistoryRow(detailText);
                        card.getChildren().add(row);
                    }
                    paymentHistoryContainer.getChildren().add(card);
                }
            }
        }
        if (!foundPaid) {
            Label noPaidLabel = new Label("Aucun paiement dans l'historique.");
            noPaidLabel.getStyleClass().add("subtitle");
            paymentHistoryContainer.getChildren().add(noPaidLabel);
        }
    }

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

    /**
     * Loads the PaymentForm.fxml into the center content area.
     * Passes the Payment or PaymentInstallment to the PaymentFormController.
     */
    private void loadPaymentForm(Payment payment, PaymentInstallment installment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/PaymentForm.fxml"));
            Parent paymentForm = loader.load();

            PaymentFormController formController = loader.getController();
            // Set the payment and/or installment in the form controller.
            formController.setPayment(payment);
            formController.setInstallment(installment);

            // Get the content area (assumed to be a StackPane with fx:id "contentArea")
            StackPane contentArea = (StackPane) paymentListContainer.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(paymentForm);
            } else {
                AlertUtils.showAlert("Erreur", "Zone de contenu introuvable.", javafx.scene.control.Alert.AlertType.ERROR);
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert("Erreur", "Impossible de charger le formulaire de paiement.", javafx.scene.control.Alert.AlertType.ERROR);
        }
    }
}
