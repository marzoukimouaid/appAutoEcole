package controller;

import entite.Payment;
import entite.PaymentInstallment;
import entite.PaymentInstallment.Status;
import entite.ExamenCode;
import entite.ExamenConduit;

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
import service.ExamenCodeService;
import service.ExamenConduitService;

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


    private Payment payment;
    private PaymentInstallment installment;

    private ExamenCode examCode;
    private ExamenConduit examConduit;


    private final PaymentService paymentService = new PaymentService();
    private final PaymentInstallmentService installmentService = new PaymentInstallmentService();

    private final ExamenCodeService examenCodeService = new ExamenCodeService();
    private final ExamenConduitService examenConduitService = new ExamenConduitService();

    @FXML
    public void initialize() {

        expiryMonthCombo.getItems().clear();
        for (int m = 1; m <= 12; m++) {
            expiryMonthCombo.getItems().add(String.format("%02d", m));
        }

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
        } else if (!cardNumber.matches("\\d{16}")) {
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
            payment.setStatus("PAID");
            updateSuccess = paymentService.updatePayment(payment);
        }

        else if (installment != null) {
            boolean installmentUpdated = installmentService.markInstallmentAsPaid(installment.getInstallmentId(), LocalDate.now());
            if (!installmentUpdated) {
                AlertUtils.showAlert("Erreur", "Erreur lors de la mise à jour de l'installment.", Alert.AlertType.ERROR);
                return;
            }

            int paymentId = installment.getPaymentId();
            var installments = installmentService.getInstallmentsByPaymentId(paymentId);
            boolean allPaid = installments.stream().allMatch(inst -> inst.getStatus() == Status.PAID);
            if (allPaid) {
                Optional<Payment> optPayment = paymentService.getPaymentById(paymentId);
                if (optPayment.isPresent()) {
                    Payment fullPayment = optPayment.get();
                    fullPayment.setStatus("PAID");
                    updateSuccess = paymentService.updatePayment(fullPayment);
                } else {
                    updateSuccess = false;
                }
            } else {
                updateSuccess = true;
            }
        }

        else if (examCode != null) {
            examCode.setPaiementStatus(ExamenCode.PaymentStatus.PAID);
            examCode.setPaymentDate(LocalDate.now());
            updateSuccess = examenCodeService.updateExamenCode(examCode);
        }

        else if (examConduit != null) {
            examConduit.setPaiementStatus(ExamenConduit.PaymentStatus.PAID);
            examConduit.setPaymentDate(LocalDate.now());
            updateSuccess = examenConduitService.updateExamenConduit(examConduit);
        }

        if (updateSuccess) {
            NotificationUtil.showNotification(
                    rootPane,
                    "Paiement effectué avec succès.",
                    NotificationUtil.NotificationType.SUCCESS
            );

            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(e -> navigateBack());
            delay.play();
        } else {
            AlertUtils.showAlert(
                    "Erreur",
                    "Erreur lors de la mise à jour du paiement / examen.",
                    Alert.AlertType.ERROR
            );
        }
    }

    private void clearErrors() {
        cardholderNameError.setText("");
        cardNumberError.setText("");
        expiryError.setText("");
        cvvError.setText("");
        billingAddressError.setText("");
    }


    public void setPayment(Payment payment) {
        this.payment = payment;
    }
    public void setInstallment(PaymentInstallment installment) {
        this.installment = installment;
    }


    public void setExamenCode(ExamenCode examCode) {
        this.examCode = examCode;
    }
    public void setExamenConduit(ExamenConduit examConduit) {
        this.examConduit = examConduit;
    }

    
    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/CandidatePaiements.fxml"));
            Parent paymentsPage = loader.load();
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
