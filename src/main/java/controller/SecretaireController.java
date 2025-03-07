package controller;

import Utils.AlertUtils;
import Utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;

public class SecretaireController {

    @FXML private BorderPane root;
    @FXML private VBox sidebar;
    @FXML private StackPane contentArea;
    @FXML private Label pageTitle;

    // Sidebar buttons
    @FXML private Button btnCandidats, btnMoniteurs, btnAnalytics, btnVehicules, btnPaiements, btnSeances,btnInscription;
    @FXML private Button btnLogout;

    @FXML
    public void initialize() {
        setSidebarIcons();
        handleCandidats();
    }

    // ===================================
    // SIDEBAR BUTTON HANDLERS
    // ===================================
    @FXML
    private void handleCandidats() {
        loadPage("/org/example/Candidats.fxml", "Candidats");
        highlightSidebarButton(btnCandidats);
    }

    @FXML
    private void handleMoniteurs() {
        loadPage("/org/example/Moniteurs.fxml", "Moniteurs");
        highlightSidebarButton(btnMoniteurs);
    }

    @FXML
    private void handleAnalytics() {
        loadPage("/org/example/Analytics.fxml", "Analytics");
        highlightSidebarButton(btnAnalytics);
    }

    @FXML
    private void handleVehicules() {
        loadPage("/org/example/Vehicules.fxml", "Vehicules");
        highlightSidebarButton(btnVehicules);
    }

    @FXML
    private void handlePaiements() {
        loadPage("/org/example/Paiements.fxml", "Paiements");
        highlightSidebarButton(btnPaiements);
    }

    @FXML
    private void handleSeances() {
        loadPage("/org/example/Seances.fxml", "Seances");
        highlightSidebarButton(btnSeances);
    }

    @FXML
    private void handleInscription() {
        loadPage("/org/example/Inscription.fxml", "Inscription");
        highlightSidebarButton(btnInscription);
    }

    // ===================================
    // LOGOUT
    // ===================================
    @FXML
    private void handleLogout() {
        SessionManager.logout();
        switchToLoginPage();
    }

    private void switchToLoginPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(new Scene(root, 1024, 600));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert("Erreur", "Impossible de charger la page de connexion.", javafx.scene.control.Alert.AlertType.ERROR);
        }
    }

    // ===================================
    // DYNAMIC PAGE LOADING
    // ===================================
    private void loadPage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newPage = loader.load();

            // Replace contentArea's children with the new page
            contentArea.getChildren().setAll(newPage);

            // Set page title
            pageTitle.setText(title);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert("Erreur", "Impossible de charger la page: " + fxmlPath, javafx.scene.control.Alert.AlertType.ERROR);
        }
    }

    private void setSidebarIcons() {
        btnCandidats.setGraphic(createIcon(FontAwesomeSolid.USER_GRADUATE));
        btnMoniteurs.setGraphic(createIcon(FontAwesomeSolid.CHALKBOARD_TEACHER));
        btnAnalytics.setGraphic(createIcon(FontAwesomeSolid.CHART_LINE));
        btnVehicules.setGraphic(createIcon(FontAwesomeSolid.CAR));
        btnPaiements.setGraphic(createIcon(FontAwesomeSolid.MONEY_BILL_WAVE));
        btnSeances.setGraphic(createIcon(FontAwesomeSolid.CALENDAR_ALT));
        btnInscription.setGraphic(createIcon(FontAwesomeSolid.EDIT));
        btnLogout.setGraphic(createIcon(FontAwesomeSolid.SIGN_OUT_ALT));
    }

    private FontIcon createIcon(FontAwesomeSolid iconType) {
        FontIcon icon = new FontIcon(iconType);
        icon.setIconSize(16);
        icon.getStyleClass().add("ikonli-font-icon"); // Uses CSS for color
        return icon;
    }

    // ===================================
    // SIDEBAR STYLING
    // ===================================
    private void highlightSidebarButton(Button selectedButton) {
        // Remove "selected" style from all buttons
        btnCandidats.getStyleClass().remove("selected");
        btnMoniteurs.getStyleClass().remove("selected");
        btnAnalytics.getStyleClass().remove("selected");
        btnVehicules.getStyleClass().remove("selected");
        btnPaiements.getStyleClass().remove("selected");
        btnSeances.getStyleClass().remove("selected");
        btnInscription.getStyleClass().remove("selected");

        // Add "selected" style to the clicked button
        selectedButton.getStyleClass().add("selected");
    }

}
