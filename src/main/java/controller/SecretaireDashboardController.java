package controller;

import entite.Notification;
import entite.Profile;
import entite.User;
import entite.VehiculeDocument;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import service.NotificationService;
import service.ProfileService;
import service.AutoEcoleService;
import service.VehiculeDocumentService;
import service.UserService;
import Utils.AlertUtils;
import Utils.SessionManager;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SecretaireDashboardController {

    @FXML private BorderPane root;
    @FXML private VBox sidebar;
    @FXML private StackPane contentArea;
    @FXML private Label pageTitle;
    @FXML private Button btnToggleSidebar;
    @FXML private MenuButton profileMenu;
    @FXML private MenuButton notificationMenu; // Notification MenuButton
    @FXML private Label notificationBadge;     // Badge label (red circle with unread count)

    // Sidebar buttons
    @FXML private Button btnCandidats, btnMoniteurs, btnAnalytics, btnVehicules, btnSeances, btnInscription;
    @FXML private Label autoEcoleNameLabel;  // Displays the Auto-Ecole name
    @FXML private ImageView profileImage;    // Navbar profile image

    private final ProfileService profileService = new ProfileService();
    private final AutoEcoleService autoEcoleService = new AutoEcoleService();
    private final NotificationService notificationService = new NotificationService();
    private User currentUser;
    private boolean sidebarVisible = true;
    private Timeline notificationTimeline; // For periodic updates

    @FXML
    public void initialize() {
        // Set up sidebar and icons.
        setIconsForSidebar();
        setupSidebarClip();

        // Load current user from session.
        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("No user in session. Redirecting to login...");
            switchToLoginPage();
            return;
        }
        loadAutoEcoleName();
        loadUserProfilePicture();

        // When the notifications dropdown is about to be shown, mark all notifications as read.
        notificationMenu.setOnShowing(e -> {
            markAllNotificationsAsRead();
            updateNotifications();
        });
        // Also update notifications on initialization.
        updateNotifications();

        // Create a Timeline to update notifications (badge and dropdown) every 5 seconds.
        notificationTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> updateNotifications()));
        notificationTimeline.setCycleCount(Timeline.INDEFINITE);
        notificationTimeline.play();
        handleCandidats();

        // NEW: Check for document expiry notifications upon dashboard load.
        checkDocumentExpiryNotifications();
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
     * Retrieves the user's profile and applies the picture to the navbar.
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
                ImageView profileIcon = new ImageView(userImage);
                profileIcon.setFitWidth(40);
                profileIcon.setFitHeight(40);
                profileIcon.setPreserveRatio(true);
                profileMenu.setGraphic(profileIcon);
            }
        });
    }

    private void setupSidebarClip() {
        Rectangle clipRect = new Rectangle();
        clipRect.setWidth(sidebar.getPrefWidth());
        clipRect.setHeight(sidebar.getHeight());
        sidebar.setClip(clipRect);
        sidebar.widthProperty().addListener((obs, oldVal, newVal) -> clipRect.setWidth(newVal.doubleValue()));
        sidebar.heightProperty().addListener((obs, oldVal, newVal) -> clipRect.setHeight(newVal.doubleValue()));
    }

    /**
     * Toggles the sidebar visibility.
     */
    @FXML
    private void toggleSidebar() {
        double sidebarWidth = 220;
        if (sidebarVisible) {
            Timeline slideOut = new Timeline(
                    new KeyFrame(Duration.millis(400),
                            new KeyValue(sidebar.translateXProperty(), -sidebarWidth)
                    )
            );
            ParallelTransition fadeOut = new ParallelTransition();
            for (Node child : sidebar.getChildren()) {
                Timeline fade = new Timeline(
                        new KeyFrame(Duration.millis(400),
                                new KeyValue(child.opacityProperty(), 0)
                        )
                );
                fadeOut.getChildren().add(fade);
            }
            ParallelTransition closingTransition = new ParallelTransition(slideOut, fadeOut);
            closingTransition.setOnFinished(event -> {
                root.setLeft(null);
                sidebar.getChildren().forEach(c -> c.setOpacity(1));
                sidebar.setTranslateX(0);
                sidebarVisible = false;
            });
            closingTransition.play();
        } else {
            sidebar.setTranslateX(-sidebarWidth);
            sidebar.getChildren().forEach(child -> child.setOpacity(0));
            root.setLeft(sidebar);
            Timeline slideIn = new Timeline(
                    new KeyFrame(Duration.millis(400),
                            new KeyValue(sidebar.translateXProperty(), 0)
                    )
            );
            ParallelTransition fadeIn = new ParallelTransition();
            for (Node child : sidebar.getChildren()) {
                Timeline fade = new Timeline(
                        new KeyFrame(Duration.millis(400),
                                new KeyValue(child.opacityProperty(), 1)
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
     * Updates the notification dropdown and badge.
     */
    private void updateNotifications() {
        if (currentUser == null) return;
        List<Notification> notifications = notificationService.getNotificationsForUser(currentUser.getId());
        // Update badge with count of unread notifications.
        long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();
        if (unreadCount > 0) {
            notificationBadge.setText(String.valueOf(unreadCount));
            notificationBadge.setVisible(true);
        } else {
            notificationBadge.setVisible(false);
        }
        // Populate dropdown with the latest five notifications.
        List<Notification> latestNotifications = notifications.stream().limit(5).collect(Collectors.toList());
        notificationMenu.getItems().clear();
        if (latestNotifications.isEmpty()) {
            MenuItem emptyItem = new MenuItem("Aucune Notification");
            emptyItem.setDisable(true);
            notificationMenu.getItems().add(emptyItem);
        } else {
            // Create a custom layout for each notification for better readability.
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (Notification notif : latestNotifications) {
                MenuItem item = new MenuItem();
                VBox container = new VBox(2.0);
                container.setStyle("-fx-padding: 5 10 5 10;");
                Label messageLabel = new Label(notif.getMessage());
                messageLabel.setWrapText(true);
                Label dateLabel = new Label("Reçu le " + notif.getCreatedAt().format(timeFormatter));
                dateLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
                container.getChildren().addAll(messageLabel, dateLabel);
                item.setGraphic(container);
                notificationMenu.getItems().add(item);
            }
        }
    }

    /**
     * Marks all notifications for the current user as read.
     */
    private void markAllNotificationsAsRead() {
        List<Notification> notifications = notificationService.getNotificationsForUser(currentUser.getId());
        notifications.stream().filter(n -> !n.isRead()).forEach(n -> notificationService.markNotificationAsRead(n.getId()));
    }

    /**
     * Navigates to a new view inside the content area.
     */
    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newPage = loader.load();
            contentArea.getChildren().setAll(newPage);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert("Erreur", "Impossible de charger la page: " + fxmlPath,
                    javafx.scene.control.Alert.AlertType.ERROR);
        }
    }

    /**
     * Switches to the login page.
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
        loadPage("/org/example/Profile.fxml");
    }

    /**
     * Handles the "Modifier autoecole" action.
     * Loads the auto-école configuration page with preloaded data.
     */
    @FXML
    private void handleModifierAutoEcole() {
        clearSidebarSelection();
        loadPage("/org/example/AutoEcole.fxml");
    }

    @FXML
    private void handleCandidats() {
        loadPage("/org/example/SecretaireCandidats.fxml");
        highlightSidebarButton(btnCandidats);
    }

    @FXML
    private void handleMoniteurs() {
        loadPage("/org/example/SecretaireMoniteurs.fxml");
        highlightSidebarButton(btnMoniteurs);
    }

    @FXML
    private void handleAnalytics() {
        loadPage("/org/example/Analytics.fxml");
        highlightSidebarButton(btnAnalytics);
    }

    @FXML
    private void handleVehicules() {
        loadPage("/org/example/SecretaireVehicules.fxml");
        highlightSidebarButton(btnVehicules);
    }

    @FXML
    private void handleSeances() {
        loadPage("/org/example/SecretaireSeances.fxml");
        highlightSidebarButton(btnSeances);
    }

    @FXML
    private void handleInscription() {
        loadPage("/org/example/SecretaireInscriptionExamen.fxml");
        highlightSidebarButton(btnInscription);
    }

    /**
     * Highlights the clicked sidebar button.
     */
    private void highlightSidebarButton(Button selectedButton) {
        btnCandidats.getStyleClass().remove("selected");
        btnMoniteurs.getStyleClass().remove("selected");
        btnAnalytics.getStyleClass().remove("selected");
        btnVehicules.getStyleClass().remove("selected");
        btnSeances.getStyleClass().remove("selected");
        btnInscription.getStyleClass().remove("selected");
        selectedButton.getStyleClass().add("selected");
    }

    /**
     * Clears the selection highlight from all sidebar buttons.
     */
    private void clearSidebarSelection() {
        btnCandidats.getStyleClass().remove("selected");
        btnMoniteurs.getStyleClass().remove("selected");
        btnAnalytics.getStyleClass().remove("selected");
        btnSeances.getStyleClass().remove("selected");
        btnInscription.getStyleClass().remove("selected");
    }

    /**
     * Assigns icons for sidebar buttons and the hamburger button.
     */
    private void setIconsForSidebar() {
        btnCandidats.setGraphic(createIcon(FontAwesomeSolid.USER_GRADUATE));
        btnMoniteurs.setGraphic(createIcon(FontAwesomeSolid.CHALKBOARD_TEACHER));
        btnAnalytics.setGraphic(createIcon(FontAwesomeSolid.CHART_LINE));
        btnVehicules.setGraphic(createIcon(FontAwesomeSolid.CAR));
        btnSeances.setGraphic(createIcon(FontAwesomeSolid.CALENDAR_ALT));
        btnInscription.setGraphic(createIcon(FontAwesomeSolid.EDIT));

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
     * NEW: Checks for documents expiring within the next day (that have not been notified)
     * and sends a notification to the current secretaire. After sending, the document is marked as notified.
     */
    private void checkDocumentExpiryNotifications() {
        VehiculeDocumentService documentService = new VehiculeDocumentService();
        // Retrieve documents expiring soon (within 1 day)
        List<VehiculeDocument> expiringDocs = documentService.findDocumentsExpiringSoon(1);
        if (!expiringDocs.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            expiringDocs.forEach(doc -> {
                String message = "Attention: Le document " + doc.getDocType().name() +
                        " pour le véhicule ID " + doc.getVehiculeId() +
                        " expire le " + doc.getDateExpiration().format(formatter) + ".";
                boolean sent = notificationService.sendNotification(currentUser.getId(), message);
                if (sent) {
                    System.out.println("[Dashboard] Sent document expiry notification to user id: "
                            + currentUser.getId() + " with message: " + message);
                } else {
                    System.err.println("[Dashboard] FAILED to send document expiry notification to user id: "
                            + currentUser.getId());
                }
                // Mark the document as notified to prevent duplicate notifications.
                doc.setNotified(true);
                boolean updated = documentService.updateDocument(doc);
                if (updated) {
                    System.out.println("[Dashboard] Marked document id " + doc.getDocId() + " as notified.");
                } else {
                    System.err.println("[Dashboard] FAILED to mark document id " + doc.getDocId() + " as notified.");
                }
            });
        }
    }
}
