package controller;

import Utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.AutoEcoleService;

import java.io.IOException;

public class AutoEcoleController {

    @FXML
    private TextField nameField, addressField, phoneField, emailField;
    @FXML
    private TextField prixSeanceConduitField, prixSeanceCodeField; // New fields for session prices

    private final AutoEcoleService autoEcoleService = new AutoEcoleService();

    @FXML
    private void initializeAutoEcole() {
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String prixConduitStr = prixSeanceConduitField.getText().trim();
        String prixCodeStr = prixSeanceCodeField.getText().trim();

        // Validate all fields
        if (!validateInputs(name, address, phone, email, prixConduitStr, prixCodeStr)) {
            return;
        }

        // Parse the prices
        double prixSeanceConduit = Double.parseDouble(prixConduitStr);
        double prixSeanceCode = Double.parseDouble(prixCodeStr);

        // Pass all fields to the service layer
        autoEcoleService.initializeAutoEcole(name, address, phone, email, prixSeanceConduit, prixSeanceCode);

        AlertUtils.showAlert("Succès", "L'auto-école a été initialisée avec succès.", Alert.AlertType.INFORMATION);
        switchToLoginPage();
    }

    private boolean validateInputs(String name, String address, String phone, String email,
                                   String prixConduitStr, String prixCodeStr) {
        if (name.length() < 3) {
            AlertUtils.showAlert("Erreur", "Le nom doit contenir au moins 3 caractères.", Alert.AlertType.ERROR);
            return false;
        }
        if (address.length() < 5) {
            AlertUtils.showAlert("Erreur", "L'adresse doit contenir au moins 5 caractères.", Alert.AlertType.ERROR);
            return false;
        }
        if (!phone.matches("\\d{8}")) {
            AlertUtils.showAlert("Erreur", "Le numéro de téléphone doit contenir exactement 8 chiffres.", Alert.AlertType.ERROR);
            return false;
        }
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            AlertUtils.showAlert("Erreur", "L'adresse e-mail n'est pas valide.", Alert.AlertType.ERROR);
            return false;
        }
        try {
            double prixConduit = Double.parseDouble(prixConduitStr);
            if (prixConduit < 0) {
                AlertUtils.showAlert("Erreur", "Le prix de la séance conduite ne peut être négatif.", Alert.AlertType.ERROR);
                return false;
            }
        } catch (NumberFormatException e) {
            AlertUtils.showAlert("Erreur", "Le prix de la séance conduite doit être un nombre valide.", Alert.AlertType.ERROR);
            return false;
        }
        try {
            double prixCode = Double.parseDouble(prixCodeStr);
            if (prixCode < 0) {
                AlertUtils.showAlert("Erreur", "Le prix de la séance code ne peut être négatif.", Alert.AlertType.ERROR);
                return false;
            }
        } catch (NumberFormatException e) {
            AlertUtils.showAlert("Erreur", "Le prix de la séance code doit être un nombre valide.", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private void switchToLoginPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nameField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert("Erreur", "Impossible de charger la page de connexion.", Alert.AlertType.ERROR);
        }
    }
}
