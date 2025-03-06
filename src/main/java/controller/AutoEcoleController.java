package controller;

import Utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import service.AutoEcoleService;

public class AutoEcoleController {

    @FXML
    private TextField nameField, addressField, phoneField, emailField;

    private final AutoEcoleService autoEcoleService = new AutoEcoleService();

    @FXML
    private void initializeAutoEcole() {
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();


        if (!validateInputs(name, address, phone, email)) {
            return;
        }


        autoEcoleService.initializeAutoEcole(name, address, phone, email);


        AlertUtils.showAlert("Succès", "L'auto-école a été initialisée avec succès.", Alert.AlertType.INFORMATION);
    }

    private boolean validateInputs(String name, String address, String phone, String email) {
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
        return true;
    }


}
