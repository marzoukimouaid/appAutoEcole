package controller;

import Utils.SessionManager;
import entite.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.UserService;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private Label usernameError;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label passwordError;

    private final UserService userService = new UserService();

    @FXML
    private void initialize() {

        Platform.runLater(this::checkAutoLogin);


        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearFieldError(usernameField, usernameError);
        });


        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearFieldError(passwordField, passwordError);
        });
    }

    private void checkAutoLogin() {
        User savedUser = SessionManager.getCurrentUser();
        if (savedUser != null) {
            switchToDashboard(savedUser.getRole());
        }
    }

    @FXML
    private void handleLogin() {

        clearFieldError(usernameField, usernameError);
        clearFieldError(passwordField, passwordError);

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        boolean valid = true;


        if (username.isEmpty()) {
            setFieldError(usernameField, usernameError, "Veuillez entrer votre nom d'utilisateur");
            valid = false;
        }

        if (password.isEmpty()) {
            setFieldError(passwordField, passwordError, "Veuillez entrer votre mot de passe");
            valid = false;
        }
        if (!valid) {

            return;
        }


        User authenticatedUser = userService.authenticateUser(username, password);
        if (authenticatedUser == null) {
            setFieldError(usernameField, null, null);
            setFieldError(passwordField, passwordError, "Nom d'utilisateur ou mot de passe incorrect.");
            return;
        }


        SessionManager.setCurrentUser(authenticatedUser);
        switchToDashboard(authenticatedUser.getRole());
    }

    private void switchToDashboard(String role) {
        String fxmlPage;
        switch (role.toLowerCase()) {
            case "secretaire":
                fxmlPage = "/org/example/SecretaireDashboard.fxml";
                break;
            case "candidat":
                fxmlPage = "/org/example/CandidatDashboard.fxml";
                break;
            case "moniteur":
                fxmlPage = "/org/example/MoniteurDashboard.fxml";
                break;
            default:

                setFieldError(passwordField, passwordError, "Rôle utilisateur inconnu.");
                return;
        }

        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPage));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1024, 600);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.show();
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();

            setFieldError(passwordField, passwordError, "Impossible de charger la page.");
        }
    }

    
    private void setFieldError(TextField field, Label errorLabel, String errorMsg) {
        if (field != null && !field.getStyleClass().contains("error")) {
            field.getStyleClass().add("error");
        }
        if (errorLabel != null && errorMsg != null) {
            errorLabel.setText(errorMsg);
            errorLabel.setVisible(true);
        }
    }

    
    private void clearFieldError(TextField field, Label errorLabel) {
        if (field != null) {
            field.getStyleClass().remove("error");
        }
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        }
    }
}
