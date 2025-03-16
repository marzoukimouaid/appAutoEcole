package controller;

import entite.Payment;
import entite.PaymentInstallment;
import entite.PaymentInstallment.Status;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import Utils.AlertUtils;
import Utils.NotificationUtil;
import service.PaymentService;
import service.PaymentInstallmentService;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public class PaymentFormController {

    @FXML
    private StackPane rootPane;

    @FXML
    private TextField cardholderNameField;
    @FXML
    private TextField cardNumberField;
    @FXML
    private ComboBox<String> expiryMonthCombo;
    @FXML
    private ComboBox<String> expiryYearCombo;
    @FXML
    private TextField cvvField;
    @FXML
    private TextField billingAddressField;

    @FXML
    private Label cardholderNameError;
    @FXML
    private Label cardNumberError;
    @FXML
    private Label expiryError;
    @FXML
    private Label cvvError;
    @FXML
    private Label billingAddressError;

    @FXML
    private Button confirmButton;

    // Only one of these will be non-null – the full Payment or an Installment
    private Payment payment;
    private PaymentInstallment installment;

    private final PaymentService paymentService = new PaymentService();
    private final PaymentInstallmentService installmentService = new PaymentInstallmentService();

    @FXML
    public void initialize() {
        // Populate expiry month (01-12)
        expiryMonthCombo.getItems().clear();
        for (int m = 1; m <= 12; m++) {
            expiryMonthCombo.getItems().add(String.format("%02d", m));
        }
        // Populate expiry year (current year to next 10 years)
        expiryYearCombo.getItems().clear();
        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear; y <= currentYear + 10; y++) {
            expiryYearCombo.getItems().add(String.valueOf(y));
        }
    }

    @FXML
    private void handleConfirmPayment() {
        clearErrors();
        boolean valid = true;

        String cardholderName = cardholderNameField.getText().trim();
        if (cardholderName.isEmpty()) {
            cardholderNameError.setText("Nom requis");
            valid = false;
        }

        String cardNumber = cardNumberField.getText().replaceAll("\\s+", "");
        if (cardNumber.isEmpty()) {
            cardNumberError.setText("Numéro de carte requis");
            valid = false;
        } else if (!cardNumber.matches("\\d{16}")) {  // Only check that it's exactly 16 digits
            cardNumberError.setText("Le numéro doit comporter 16 chiffres");
            valid = false;
        }

        String expiryMonth = expiryMonthCombo.getValue();
        String expiryYear = expiryYearCombo.getValue();
        if (expiryMonth == null || expiryYear == null) {
            expiryError.setText("Date d'expiration requise");
            valid = false;
        }

        String cvv = cvvField.getText().trim();
        if (cvv.isEmpty()) {
            cvvError.setText("CVV requis");
            valid = false;
        } else if (!cvv.matches("\\d{3}")) {
            cvvError.setText("Le CVV doit comporter 3 chiffres");
            valid = false;
        }

        String billingAddress = billingAddressField.getText().trim();
        if (billingAddress.isEmpty()) {
            billingAddressError.setText("Adresse de facturation requise");
            valid = false;
        }

        if (!valid) {
            return;
        }

        boolean updateSuccess = false;
        if (payment != null) {
            // For a full payment, mark the payment as PAID.
            payment.setStatus("PAID");
            updateSuccess = paymentService.updatePayment(payment);
        } else if (installment != null) {
            // For an installment payment, mark this installment as PAID.
            boolean installmentUpdated = installmentService.markInstallmentAsPaid(installment.getInstallmentId(), LocalDate.now());
            if (!installmentUpdated) {
                AlertUtils.showAlert("Erreur", "Erreur lors de la mise à jour de l'installment.", Alert.AlertType.ERROR);
                return;
            }
            int paymentId = installment.getPaymentId();
            // Check if all installments for this payment are now paid.
            var installments = installmentService.getInstallmentsByPaymentId(paymentId);
            boolean allPaid = installments.stream().allMatch(inst -> inst.getStatus() == Status.PAID);
            if (allPaid) {
                Optional<Payment> optPayment = paymentService.getPaymentById(paymentId);
                if (optPayment.isPresent()) {
                    Payment fullPayment = optPayment.get();
                    fullPayment.setStatus("PAID");
                    updateSuccess = paymentService.updatePayment(fullPayment);
                }
            } else {
                updateSuccess = true; // Installment update succeeded
            }
        }

        if (updateSuccess) {
            // Instead of an alert, show a sleek success notification.
            NotificationUtil.showNotification(rootPane, "Paiement effectué avec succès.", NotificationUtil.NotificationType.SUCCESS);
            // Delay navigation back for 3 seconds so that the notification is visible.
            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(e -> navigateBack());
            delay.play();
        } else {
            AlertUtils.showAlert("Erreur", "Erreur lors de la mise à jour du paiement.", Alert.AlertType.ERROR);
        }
    }

    private void clearErrors() {
        cardholderNameError.setText("");
        cardNumberError.setText("");
        expiryError.setText("");
        cvvError.setText("");
        billingAddressError.setText("");
    }

    // Setter methods to allow the previous view to pass Payment or PaymentInstallment objects.
    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public void setInstallment(PaymentInstallment installment) {
        this.installment = installment;
    }

    /**
     * Navigates back to the CandidatePaiements view.
     */
    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/CandidatePaiements.fxml"));
            Parent paymentsPage = loader.load();
            // Look up the content area (assumed to have fx:id "contentArea") in the current scene.
            StackPane contentArea = (StackPane) rootPane.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(paymentsPage);
            } else {
                AlertUtils.showAlert("Erreur", "Zone de contenu introuvable.", Alert.AlertType.ERROR);
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert("Erreur", "Impossible de charger la page des paiements.", Alert.AlertType.ERROR);
        }
    }
}
