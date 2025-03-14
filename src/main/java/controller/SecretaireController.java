package controller;

import entite.Profile;
import entite.User;
import service.ProfileService;
import service.AutoEcoleService;
import Utils.AlertUtils;
import Utils.SessionManager;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
import javafx.util.Duration;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class SecretaireController {

    @FXML private BorderPane root;
    @FXML private VBox sidebar;
    @FXML private StackPane contentArea;
    @FXML private Label pageTitle;
    @FXML private Button btnToggleSidebar;
    @FXML private MenuButton profileMenu;

    // Sidebar buttons
    @FXML private Button btnCandidats, btnMoniteurs, btnAnalytics, btnVehicules, btnPaiements, btnSeances, btnInscription;

    @FXML private Label autoEcoleNameLabel;  // Displays the Auto-Ecole name
    @FXML private ImageView profileImage;    // Navbar profile image

    private final ProfileService profileService = new ProfileService();
    private final AutoEcoleService autoEcoleService = new AutoEcoleService();
    private User currentUser;
    private boolean sidebarVisible = true;

    @FXML
    public void initialize() {
        // 1) Set up icons (hamburger, side buttons) - no static user images
        setIconsForSidebar();
        setupSidebarClip();
        // 2) Attempt to load the current user session
        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("No user in session. Redirecting to login...");
            switchToLoginPage();
            return;
        }


        loadAutoEcoleName();


        loadUserProfilePicture();
    }

    /**
     * Loads and displays the Auto-Ecole name from the DB.
     */
    private void loadAutoEcoleName() {
        List<String[]> autoEcoleData = autoEcoleService.getAutoEcoleData();
        String autoEcoleName = !autoEcoleData.isEmpty() ? autoEcoleData.get(0)[0] : "Auto-Ecole Not Found";
        autoEcoleNameLabel.setText(autoEcoleName);

    }

    /**
     * Retrieves the user's profile from the DB and applies the picture to the navbar.
     */
    private void loadUserProfilePicture() {
        Optional<Profile> profileOptional = profileService.getProfileByUserId(currentUser.getId());
        if (!profileOptional.isPresent()) {
            System.out.println("No profile found for user with ID: " + currentUser.getId());
        }
        profileOptional.ifPresent(profile -> {
            String profilePictureUrl = profile.getPictureUrl();

            if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                Image userImage = new Image(profilePictureUrl, true);


                // Profile Menu Icon
                ImageView profileIcon = new ImageView(userImage);
                profileIcon.setFitWidth(40);
                profileIcon.setFitHeight(40);
                profileIcon.setPreserveRatio(true);
                profileMenu.setGraphic(profileIcon);
            }
        });
    }
    private void setupSidebarClip() {
        // Create a rectangle that will act as the clipping shape
        Rectangle clipRect = new Rectangle();
        // Initial width & height match the sidebar's current size
        clipRect.setWidth(sidebar.getPrefWidth());
        clipRect.setHeight(sidebar.getHeight());

        // Apply the clip to the sidebar
        sidebar.setClip(clipRect);

        // Keep the clipRect in sync if the sidebar is resized
        sidebar.widthProperty().addListener((obs, oldVal, newVal) -> {
            clipRect.setWidth(newVal.doubleValue());
        });
        sidebar.heightProperty().addListener((obs, oldVal, newVal) -> {
            clipRect.setHeight(newVal.doubleValue());
        });
    }
    /**
     * Toggles the sidebar visibility (hamburger button).
     */
    @FXML
    private void toggleSidebar() {
        double sidebarWidth = 220;  // Full expanded width
        if (sidebarVisible) {
            // Animate closing: slide sidebar left and fade out its children concurrently.
            Timeline slideOut = new Timeline(
                    new KeyFrame(Duration.millis(400),
                            new KeyValue(sidebar.translateXProperty(), -sidebarWidth, Interpolator.EASE_BOTH)
                    )
            );

            ParallelTransition fadeOut = new ParallelTransition();
            for (Node child : sidebar.getChildren()) {
                Timeline fade = new Timeline(
                        new KeyFrame(Duration.millis(400),
                                new KeyValue(child.opacityProperty(), 0, Interpolator.EASE_BOTH)
                        )
                );
                fadeOut.getChildren().add(fade);
            }

            ParallelTransition closingTransition = new ParallelTransition(slideOut, fadeOut);
            closingTransition.setOnFinished(event -> {
                // Remove sidebar from the layout after animation
                root.setLeft(null);
                // Reset children opacity for next time
                for (Node child : sidebar.getChildren()) {
                    child.setOpacity(1);
                }
                sidebar.setTranslateX(0);
                sidebarVisible = false;
            });
            closingTransition.play();
        } else {
            // Prepare sidebar for opening: attach it and set initial translate and opacity.
            sidebar.setTranslateX(-sidebarWidth);
            for (Node child : sidebar.getChildren()) {
                child.setOpacity(0);
            }
            root.setLeft(sidebar);

            Timeline slideIn = new Timeline(
                    new KeyFrame(Duration.millis(400),
                            new KeyValue(sidebar.translateXProperty(), 0, Interpolator.EASE_BOTH)
                    )
            );
            ParallelTransition fadeIn = new ParallelTransition();
            for (Node child : sidebar.getChildren()) {
                Timeline fade = new Timeline(
                        new KeyFrame(Duration.millis(400),
                                new KeyValue(child.opacityProperty(), 1, Interpolator.EASE_BOTH)
                        )
                );
                fadeIn.getChildren().add(fade);
            }

            ParallelTransition openingTransition = new ParallelTransition(slideIn, fadeIn);
            openingTransition.setOnFinished(event -> sidebarVisible = true);
            openingTransition.play();
        }
    }






    /**
     * Navigates to a new view inside the content area.
     * @param fxmlPath The resource path to the FXML file.
     * @param title    A string for the page title (optional usage).
     */
    private void loadPage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newPage = loader.load();
            contentArea.getChildren().setAll(newPage);
            pageTitle.setText(title);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert("Erreur", "Impossible de charger la page: " + fxmlPath,
                    javafx.scene.control.Alert.AlertType.ERROR);
        }
    }

    /**
     * Assign icons for all sidebar buttons, plus hamburger icon.
     */
    private void setIconsForSidebar() {
        btnCandidats.setGraphic(createIcon(FontAwesomeSolid.USER_GRADUATE));
        btnMoniteurs.setGraphic(createIcon(FontAwesomeSolid.CHALKBOARD_TEACHER));
        btnAnalytics.setGraphic(createIcon(FontAwesomeSolid.CHART_LINE));
        btnVehicules.setGraphic(createIcon(FontAwesomeSolid.CAR));
        btnPaiements.setGraphic(createIcon(FontAwesomeSolid.MONEY_BILL_WAVE));
        btnSeances.setGraphic(createIcon(FontAwesomeSolid.CALENDAR_ALT));
        btnInscription.setGraphic(createIcon(FontAwesomeSolid.EDIT));

        // Hamburger Menu
        FontIcon hamburgerIcon = new FontIcon(FontAwesomeSolid.BARS);
        hamburgerIcon.setIconSize(20);
        btnToggleSidebar.setGraphic(hamburgerIcon);
    }

    /**
     * Helper to create FontAwesome icons.
     */
    private FontIcon createIcon(FontAwesomeSolid iconType) {
        FontIcon icon = new FontIcon(iconType);
        icon.setIconSize(16);
        icon.getStyleClass().add("ikonli-font-icon");
        return icon;
    }

    /**
     * Switches to the login page (used upon logout).
     */
    private void switchToLoginPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/Login.fxml"));
            Parent rootView = loader.load();
            Stage stage = (Stage) sidebar.getScene().getWindow();
            stage.setScene(new Scene(rootView, 1024, 600));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert("Erreur", "Impossible de charger la page de connexion.",
                    javafx.scene.control.Alert.AlertType.ERROR);
        }
    }

    /**
     * Handles user logout.
     */
    @FXML
    private void handleLogout() {
        SessionManager.logout();
        switchToLoginPage();
    }

    /**
     * Navigates to the profile page.
     */
    @FXML
    private void handleProfile() {
        clearSidebarSelection();
        loadPage("/org/example/Profile.fxml", "Profile");
    }

    /**
     * Example: Navigates to the "Candidats" page.
     */
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

    /**
     * Highlights the clicked sidebar button and removes highlight from others.
     */
    private void highlightSidebarButton(Button selectedButton) {
        // Clear all 'selected' styles
        btnCandidats.getStyleClass().remove("selected");
        btnMoniteurs.getStyleClass().remove("selected");
        btnAnalytics.getStyleClass().remove("selected");
        btnVehicules.getStyleClass().remove("selected");
        btnPaiements.getStyleClass().remove("selected");
        btnSeances.getStyleClass().remove("selected");
        btnInscription.getStyleClass().remove("selected");

        // Add 'selected' style to the clicked button
        selectedButton.getStyleClass().add("selected");
    }

    /**
     * Clears the selection highlight from all sidebar buttons.
     */
    private void clearSidebarSelection() {
        btnCandidats.getStyleClass().remove("selected");
        btnMoniteurs.getStyleClass().remove("selected");
        btnAnalytics.getStyleClass().remove("selected");
        btnVehicules.getStyleClass().remove("selected");
        btnPaiements.getStyleClass().remove("selected");
        btnSeances.getStyleClass().remove("selected");
        btnInscription.getStyleClass().remove("selected");
    }
}
