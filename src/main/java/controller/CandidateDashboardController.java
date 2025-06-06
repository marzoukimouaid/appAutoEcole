package controller;

import entite.Notification;
import entite.Payment;
import entite.PaymentInstallment;
import entite.Profile;
import entite.User;
import entite.ExamenCode;
import entite.ExamenConduit;
import javafx.geometry.Insets;
import service.AutoEcoleService;
import service.NotificationService;
import service.PaymentInstallmentService;
import service.PaymentService;
import service.ProfileService;
import service.UserService;
import service.ExamenCodeService;
import service.ExamenConduitService;

import Utils.AlertUtils;
import Utils.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CandidateDashboardController {

    @FXML private BorderPane root;
    @FXML private VBox sidebar;
    @FXML private StackPane contentArea;
    @FXML private Label pageTitle;
    @FXML private Button btnToggleSidebar;
    @FXML private MenuButton notificationMenu;
    @FXML private Label notificationBadge;
    @FXML private MenuButton profileMenu;


    @FXML private StackPane unpaidBannerContainer;


    @FXML private Button btnEmploi, btnPaiements;
    @FXML private Label autoEcoleNameLabel;
    @FXML private ImageView profileImage;


    private final ProfileService profileService = new ProfileService();
    private final AutoEcoleService autoEcoleService = new AutoEcoleService();
    private final NotificationService notificationService = new NotificationService();
    private final PaymentService paymentService = new PaymentService();
    private final PaymentInstallmentService installmentService = new PaymentInstallmentService();
    private final UserService userService = new UserService();


    private final ExamenCodeService examenCodeService = new ExamenCodeService();
    private final ExamenConduitService examenConduitService = new ExamenConduitService();

    private User currentUser;
    private boolean sidebarVisible = true;
    private Timeline notificationTimeline;

    @FXML
    public void initialize() {

        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("No user in session. Redirecting to login...");
            switchToLoginPage();
            return;
        }

        setIconsForSidebar();
        setupSidebarClip();
        loadAutoEcoleName();
        loadUserProfilePicture();


        checkUpcomingInstallments();


        checkUpcomingExams();


        notificationMenu.setOnShowing(e -> {
            markAllNotificationsAsRead();
            updateNotifications();
        });


        updateNotifications();
        notificationTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> updateNotifications()));
        notificationTimeline.setCycleCount(Timeline.INDEFINITE);
        notificationTimeline.play();


        handleEmploi();


        checkUnpaidPayments();
    }

    
    private void checkUnpaidPayments() {
        boolean hasUnpaid = false;
        List<Payment> payments = paymentService.getPaymentsForUser(currentUser.getId());
        for (Payment p : payments) {
            if ("FULL".equalsIgnoreCase(p.getPaymentType()) && "PENDING".equalsIgnoreCase(p.getStatus())) {
                hasUnpaid = true;
                break;
            } else if ("INSTALLMENT".equalsIgnoreCase(p.getPaymentType())) {
                List<PaymentInstallment> installments = installmentService.getInstallmentsByPaymentId(p.getId());
                for (PaymentInstallment inst : installments) {

                    try {

                        if (inst.getDueDate().isBefore(LocalDate.now())
                                && inst.getStatus() == PaymentInstallment.Status.PENDING) {
                            hasUnpaid = true;
                            break;
                        }
                    } catch (IllegalArgumentException e) {


                        if (e.getMessage().contains("NOTIFIED")) {
                            hasUnpaid = true;
                            break;
                        } else {
                            throw e;
                        }
                    }
                }
            }
            if (hasUnpaid) break;
        }
        if (hasUnpaid) {
            createUnpaidWarningBanner("Vous avez des paiements impayés. Veuillez consulter la section Paiements.");
        }
    }

    
    private void createUnpaidWarningBanner(String message) {
        unpaidBannerContainer.setVisible(true);
        unpaidBannerContainer.setManaged(true);
        unpaidBannerContainer.getChildren().clear();

        HBox bannerBox = new HBox(10);
        bannerBox.setMaxWidth(Double.MAX_VALUE);
        bannerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        VBox.setMargin(bannerBox, new Insets(10, 20, 0, 20));

        bannerBox.setStyle(
                "-fx-background-color: #ffeeba;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 10;"
        );

        Label warningLabel = new Label(message);
        warningLabel.setStyle("-fx-text-fill: #856404; -fx-font-size: 14px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("X");
        closeButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #856404;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );
        closeButton.setOnAction(e -> {
            unpaidBannerContainer.setVisible(false);
            unpaidBannerContainer.setManaged(false);
        });

        bannerBox.getChildren().addAll(warningLabel, spacer, closeButton);
        unpaidBannerContainer.getChildren().add(bannerBox);
    }

    private void loadAutoEcoleName() {
        List<String[]> autoEcoleData = autoEcoleService.getAutoEcoleData();
        String autoEcoleName = !autoEcoleData.isEmpty() ? autoEcoleData.get(0)[0] : "Auto-Ecole Not Found";
        autoEcoleNameLabel.setText(autoEcoleName);
    }

    private void loadUserProfilePicture() {
        Optional<Profile> profileOptional = profileService.getProfileByUserId(currentUser.getId());
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
                sidebar.getChildren().forEach(child -> child.setOpacity(1));
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

    private void updateNotifications() {
        if (currentUser == null) return;
        List<Notification> notifications = notificationService.getNotificationsForUser(currentUser.getId());
        long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();
        if (unreadCount > 0) {
            notificationBadge.setText(String.valueOf(unreadCount));
            notificationBadge.setVisible(true);
        } else {
            notificationBadge.setVisible(false);
        }

        List<Notification> latest = notifications.stream().limit(5).collect(Collectors.toList());
        notificationMenu.getItems().clear();
        if (latest.isEmpty()) {
            MenuItem emptyItem = new MenuItem("Aucune Notification");
            emptyItem.setDisable(true);
            notificationMenu.getItems().add(emptyItem);
        } else {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


            for (Notification notif : latest) {

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

    private void markAllNotificationsAsRead() {
        List<Notification> notifications = notificationService.getNotificationsForUser(currentUser.getId());
        notifications.stream()
                .filter(n -> !n.isRead())
                .forEach(n -> notificationService.markNotificationAsRead(n.getId()));
    }

    private void loadPage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newPage = loader.load();
            contentArea.getChildren().setAll(newPage);
            pageTitle.setText(title);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert("Erreur", "Impossible de charger la page: " + fxmlPath,
                    Alert.AlertType.ERROR);
        }
    }

    private void setIconsForSidebar() {
        btnEmploi.setGraphic(createIcon(FontAwesomeSolid.CALENDAR_ALT));
        btnPaiements.setGraphic(createIcon(FontAwesomeSolid.MONEY_BILL_WAVE));

        FontIcon hamburgerIcon = new FontIcon(FontAwesomeSolid.BARS);
        hamburgerIcon.setIconSize(20);
        btnToggleSidebar.setGraphic(hamburgerIcon);
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
            Parent rootView = loader.load();
            Stage stage = (Stage) sidebar.getScene().getWindow();
            stage.setScene(new Scene(rootView, 1024, 600));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showAlert("Erreur", "Impossible de charger la page de connexion.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        switchToLoginPage();
    }

    @FXML
    private void handleProfile() {
        loadPage("/org/example/Profile.fxml", "Mon Profile");
    }

    @FXML
    private void handleEmploi() {
        loadPage("/org/example/EmploiDesSeances.fxml", "Emploi des Séances");
        highlightSidebarButton(btnEmploi);
    }

    @FXML
    private void handlePaiements() {
        loadPage("/org/example/CandidatePaiements.fxml", "Paiements");
        highlightSidebarButton(btnPaiements);
    }

    private void highlightSidebarButton(Button selectedButton) {
        btnEmploi.getStyleClass().remove("selected");
        btnPaiements.getStyleClass().remove("selected");
        selectedButton.getStyleClass().add("selected");
    }

    
    private void checkUpcomingInstallments() {

        List<Payment> payments = paymentService.getPaymentsForUser(currentUser.getId());
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfterTomorrow = today.plusDays(2);

        payments.stream()
                .filter(p -> "INSTALLMENT".equalsIgnoreCase(p.getPaymentType()))
                .forEach(payment -> {
                    List<PaymentInstallment> installments = installmentService.getInstallmentsByPaymentId(payment.getId());
                    installments.stream()
                            .filter(inst -> inst.getStatus() == PaymentInstallment.Status.PENDING
                                    && !inst.isNotified()
                                    && (inst.getDueDate().isEqual(tomorrow) || inst.getDueDate().isEqual(dayAfterTomorrow)))
                            .forEach(inst -> {
                                String message = String.format(
                                        "Rappel: Votre Tranche de paiement de %.2f TND est due le %s.",
                                        inst.getAmountDue(), inst.getDueDate()
                                );
                                notificationService.sendNotification(currentUser.getId(), message);

                                inst.setNotified(true);
                                installmentService.updateInstallment(inst);
                            });
                });
    }


    private void checkUpcomingExams() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoDaysFromNow = now.plusDays(2);


        List<ExamenCode> codeExams = examenCodeService.getExamenCodesByCandidatId(currentUser.getId());
        codeExams.stream()
                .filter(ex -> {

                    boolean upcoming = !ex.getExamDatetime().isBefore(now)
                            && ex.getExamDatetime().isBefore(twoDaysFromNow);


                    boolean unpaid = (ex.getPaiementStatus() == ExamenCode.PaymentStatus.PENDING);


                    boolean notNotified = !ex.isNotified();

                    return upcoming && unpaid && notNotified;
                })
                .forEach(ex -> {

                    String msg = String.format(
                            "Rappel: Votre Examen de Code est prévu le %s et n'est pas encore payé.",
                            ex.getExamDatetime().toLocalDate()
                    );
                    notificationService.sendNotification(currentUser.getId(), msg);


                    ex.setNotified(true);
                    examenCodeService.updateExamenCode(ex);
                });


        List<ExamenConduit> conduitExams = examenConduitService.getExamenConduitsByCandidatId(currentUser.getId());
        conduitExams.stream()
                .filter(ex -> {
                    boolean upcoming = !ex.getExamDatetime().isBefore(now)
                            && ex.getExamDatetime().isBefore(twoDaysFromNow);
                    boolean unpaid = (ex.getPaiementStatus() == ExamenConduit.PaymentStatus.PENDING);
                    boolean notNotified = !ex.isNotified();
                    return upcoming && unpaid && notNotified;
                })
                .forEach(ex -> {
                    String msg = String.format(
                            "Rappel: Votre Examen de Conduite est prévu le %s et n'est pas encore payé.",
                            ex.getExamDatetime().toLocalDate()
                    );
                    notificationService.sendNotification(currentUser.getId(), msg);

                    ex.setNotified(true);
                    examenConduitService.updateExamenConduit(ex);
                });
    }
}
