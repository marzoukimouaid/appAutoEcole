package controller;

import Utils.AlertUtils;
import javafx.scene.Parent;
import service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import java.net.URL;
import java.sql.ClientInfoStatus;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    private final UserService userService = new UserService();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            AlertUtils.showAlert("Erreur", "Veuillez entrer votre nom d'utilisateur et mot de passe.", javafx.scene.control.Alert.AlertType.ERROR);
            return;
        }

        String role = userService.authenticateUser(username, password);
        if (role != null) {
            AlertUtils.showAlert("Succès", "Connexion réussie!", javafx.scene.control.Alert.AlertType.INFORMATION);
            switchToDashboard(role);
        } else {
            AlertUtils.showAlert("Erreur", "Nom d'utilisateur ou mot de passe incorrect.", javafx.scene.control.Alert.AlertType.ERROR);
        }
    }

    private void switchToDashboard(String role) {
        // Build the path
        String fxmlPage;
        if ("secretaire".equals(role)) {
            fxmlPage = "/org/example/SecretaireDashboard.fxml";
        } else if ("candidat".equals(role)) {
            fxmlPage = "/org/example/CandidatDashboard.fxml";
        } else if ("moniteur".equals(role)) {
            fxmlPage = "/org/example/MoniteurDashboard.fxml";
        } else {
            AlertUtils.showAlert("Erreur", "Rôle utilisateur inconnu.", javafx.scene.control.Alert.AlertType.ERROR);
            return;
        }

        try {
            System.out.println("Trying to load: " + fxmlPage);

            Stage stage = (Stage) usernameField.getScene().getWindow();

            URL resource = getClass().getResource(fxmlPage);
            System.out.println("Resource for " + fxmlPage + ": " + resource);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPage));

            if (loader.getLocation() == null) {
                throw new IOException("FXML file not found: " + fxmlPage);
            }

            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert(
                    "Erreur",
                    "Impossible de charger la page: " + fxmlPage,
                    javafx.scene.control.Alert.AlertType.ERROR
            );
        }
    }








}
