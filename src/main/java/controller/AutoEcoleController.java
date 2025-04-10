package controller;

import Utils.AlertUtils;
import Utils.NotificationUtil;
import Utils.NotificationUtil.NotificationType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.AutoEcoleService;

import java.io.IOException;
import java.util.List;

public class AutoEcoleController {

    @FXML
    private StackPane rootPane;

    @FXML
    private VBox contentArea;

    @FXML
    private TextField nameField, addressField, phoneField, emailField;
    @FXML
    private TextField prixSeanceConduitField, prixSeanceCodeField;
    @FXML
    private javafx.scene.control.Button continueButton;

    private final AutoEcoleService autoEcoleService = new AutoEcoleService();

    
    @FXML
    public void initialize() {
        List<String[]> autoData = AutoEcoleService.getAutoEcoleData();
        if (!autoData.isEmpty()) {
            String[] data = autoData.get(0);
            nameField.setText(data[0]);
            addressField.setText(data[1]);
            phoneField.setText(data[2]);
            emailField.setText(data[3]);
            prixSeanceConduitField.setText(data[4]);
            prixSeanceCodeField.setText(data[5]);
            continueButton.setText("Mettre à jour");
        }
    }

    @FXML
    private void initializeAutoEcole() {
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String prixConduitStr = prixSeanceConduitField.getText().trim();
        String prixCodeStr = prixSeanceCodeField.getText().trim();


        if (!validateInputs(name, address, phone, email, prixConduitStr, prixCodeStr)) {
            return;
        }


        double prixSeanceConduit = Double.parseDouble(prixConduitStr);
        double prixSeanceCode = Double.parseDouble(prixCodeStr);


        if (AutoEcoleService.getAutoEcoleData().isEmpty()) {

            autoEcoleService.initializeAutoEcole(name, address, phone, email, prixSeanceConduit, prixSeanceCode);
            AlertUtils.showAlert("Succès", "L'auto-école a été initialisée avec succès.", javafx.scene.control.Alert.AlertType.INFORMATION);
        } else {

            autoEcoleService.updateAutoEcole(name, address, phone, email, prixSeanceConduit, prixSeanceCode);

            showSuccessNotification("L'auto-école a été mise à jour avec succès!");
        }

    }

    private boolean validateInputs(String name, String address, String phone, String email,
                                   String prixConduitStr, String prixCodeStr) {
        if (name.length() < 3) {
            AlertUtils.showAlert("Erreur", "Le nom doit contenir au moins 3 caractères.", javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        }
        if (address.length() < 5) {
            AlertUtils.showAlert("Erreur", "L'adresse doit contenir au moins 5 caractères.", javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        }
        if (!phone.matches("\\d{8}")) {
            AlertUtils.showAlert("Erreur", "Le numéro de téléphone doit contenir exactement 8 chiffres.", javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        }
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            AlertUtils.showAlert("Erreur", "L'adresse e-mail n'est pas valide.", javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        }
        try {
            double prixConduit = Double.parseDouble(prixConduitStr);
            if (prixConduit < 0) {
                AlertUtils.showAlert("Erreur", "Le prix de la séance conduite ne peut être négatif.", javafx.scene.control.Alert.AlertType.ERROR);
                return false;
            }
        } catch (NumberFormatException e) {
            AlertUtils.showAlert("Erreur", "Le prix de la séance conduite doit être un nombre valide.", javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        }
        try {
            double prixCode = Double.parseDouble(prixCodeStr);
            if (prixCode < 0) {
                AlertUtils.showAlert("Erreur", "Le prix de la séance code ne peut être négatif.", javafx.scene.control.Alert.AlertType.ERROR);
                return false;
            }
        } catch (NumberFormatException e) {
            AlertUtils.showAlert("Erreur", "Le prix de la séance code doit être un nombre valide.", javafx.scene.control.Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }


    private void showSuccessNotification(String message) {
        StackPane contentArea = (StackPane) rootPane.getScene().lookup("#contentArea");
        if (contentArea != null) {
            NotificationUtil.showNotification(contentArea, message, NotificationUtil.NotificationType.SUCCESS);
        }
    }
}
