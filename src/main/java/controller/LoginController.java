package controller;

import Utils.AlertUtils;
import Utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import entite.User;
import service.UserService;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    private final UserService userService = new UserService();

    @FXML
    private void initialize() {
        Platform.runLater(this::checkAutoLogin);
    }

    private void checkAutoLogin() {
        User savedUser = SessionManager.getCurrentUser();
        if (savedUser != null) {
            switchToDashboard(savedUser.getRole());
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            AlertUtils.showAlert("Erreur", "Veuillez entrer votre nom d'utilisateur et mot de passe.", javafx.scene.control.Alert.AlertType.ERROR);
            return;
        }

        User authenticatedUser = userService.authenticateUser(username, password);
        if (authenticatedUser != null) {
            SessionManager.setCurrentUser(authenticatedUser);
            switchToDashboard(authenticatedUser.getRole());
        } else {
            AlertUtils.showAlert("Erreur", "Nom d'utilisateur ou mot de passe incorrect.", javafx.scene.control.Alert.AlertType.ERROR);
        }
    }

    private void switchToDashboard(String role) {
        String fxmlPage;
        if ("secretaire".equals(role)) {
            fxmlPage = "/org/example/SecretaireDashboard.fxml";
        } else if ("candidat".equals(role)) {
            fxmlPage = "/org/example/CandidatDashboard.fxml";
        } else if ("ingenieur".equals(role)) {
            fxmlPage = "/org/example/IngenieurDashboard.fxml";
        } else {
            AlertUtils.showAlert("Erreur", "Rôle utilisateur inconnu.", javafx.scene.control.Alert.AlertType.ERROR);
            return;
        }

        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPage));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1024, 600);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert("Erreur", "Impossible de charger la page.", javafx.scene.control.Alert.AlertType.ERROR);
        }
    }
}
