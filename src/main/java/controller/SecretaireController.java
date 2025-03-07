package controller;

import Utils.AlertUtils;
import Utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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


    @FXML private Button btnToggleSidebar;
    @FXML private MenuButton profileMenu;


    @FXML private Button btnCandidats, btnMoniteurs, btnAnalytics, btnVehicules, btnPaiements, btnSeances, btnInscription;


    private boolean sidebarVisible = true;

    @FXML
    public void initialize() {
        setIconsAndImages();
        handleCandidats();
    }


    @FXML
    private void toggleSidebar() {
        if (sidebarVisible) {

            root.setLeft(null);
        } else {

            root.setLeft(sidebar);
        }
        sidebarVisible = !sidebarVisible;
    }

    private void loadPage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newPage = loader.load();
            contentArea.getChildren().setAll(newPage);
            pageTitle.setText(title);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert("Erreur", "Impossible de charger la page: " + fxmlPath, javafx.scene.control.Alert.AlertType.ERROR);
        }
    }


    private void setIconsAndImages() {
        // Sidebar
        btnCandidats.setGraphic(createIcon(FontAwesomeSolid.USER_GRADUATE));
        btnMoniteurs.setGraphic(createIcon(FontAwesomeSolid.CHALKBOARD_TEACHER));
        btnAnalytics.setGraphic(createIcon(FontAwesomeSolid.CHART_LINE));
        btnVehicules.setGraphic(createIcon(FontAwesomeSolid.CAR));
        btnPaiements.setGraphic(createIcon(FontAwesomeSolid.MONEY_BILL_WAVE));
        btnSeances.setGraphic(createIcon(FontAwesomeSolid.CALENDAR_ALT));
        btnInscription.setGraphic(createIcon(FontAwesomeSolid.EDIT));


        FontIcon hamburgerIcon = new FontIcon(FontAwesomeSolid.BARS);
        hamburgerIcon.setIconSize(20);
        btnToggleSidebar.setGraphic(hamburgerIcon);


        try {
            String imageUrl = "https://cdn-imgix.headout.com/tour/7064/TOUR-IMAGE/b2c74200-8da7-439a-95b6-9cad1aa18742-4445-dubai-img-worlds-of-adventure-tickets-02.jpeg";
            Image image = new Image(imageUrl, true);
            ImageView profileImageView = new ImageView(image);
            profileImageView.setFitWidth(40);
            profileImageView.setFitHeight(40);
            profileImageView.setPreserveRatio(true);
            profileImageView.getStyleClass().add("profile-image");

            profileMenu.setGraphic(profileImageView);
            profileMenu.setText("");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading profile image: " + e.getMessage());
        }
    }

    private FontIcon createIcon(FontAwesomeSolid iconType) {
        FontIcon icon = new FontIcon(iconType);
        icon.setIconSize(16);
        icon.getStyleClass().add("ikonli-font-icon");
        return icon;
    }

    private void switchToLoginPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) sidebar.getScene().getWindow();
            stage.setScene(new Scene(root, 1024, 600));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert("Erreur", "Impossible de charger la page de connexion.", javafx.scene.control.Alert.AlertType.ERROR);
        }
    }
    @FXML
    private void handleLogout() {
        SessionManager.logout();
        switchToLoginPage();
    }
    @FXML
    private void handleProfile() {
        loadPage("/org/example/Profile.fxml", "Profile");
    }
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
        loadPage("/org/example/Vehicules.fxml", "Véhicules");
        highlightSidebarButton(btnVehicules);
    }
    @FXML
    private void handlePaiements() {
        loadPage("/org/example/Paiements.fxml", "Paiements");
        highlightSidebarButton(btnPaiements);
    }
    @FXML
    private void handleSeances() {
        loadPage("/org/example/Seances.fxml", "Séances");
        highlightSidebarButton(btnSeances);
    }
    @FXML
    private void handleInscription() {
        loadPage("/org/example/Inscription.fxml", "Inscription");
        highlightSidebarButton(btnInscription);
    }

    private void highlightSidebarButton(Button selectedButton) {
        btnCandidats.getStyleClass().remove("selected");
        btnMoniteurs.getStyleClass().remove("selected");
        btnAnalytics.getStyleClass().remove("selected");
        btnVehicules.getStyleClass().remove("selected");
        btnPaiements.getStyleClass().remove("selected");
        btnSeances.getStyleClass().remove("selected");
        btnInscription.getStyleClass().remove("selected");
        selectedButton.getStyleClass().add("selected");
    }
}
