package controller;

import entite.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import service.*;
import Utils.NotificationUtil;
import Utils.NotificationUtil.NotificationType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class InsertSeanceCodeController {

    @FXML private BorderPane rootPane;
    @FXML private TextField candidateUsernameField;
    @FXML private Label candidateError;
    @FXML private TextField moniteurUsernameField;
    @FXML private Label moniteurError;
    @FXML private TextField txtSessionDatetime;
    @FXML private Label datetimeError;
    @FXML private Button btnSubmit;

    private SecretaireSeancesController parentController;
    private SeanceCode editingSeance = null;

    private final SeanceCodeService seanceCodeService = new SeanceCodeService();
    private final SeanceConduitService conduitService = new SeanceConduitService();
    private final UserService userService = new UserService();
    private final PaymentService paymentService = new PaymentService();
    private final PaymentInstallmentService paymentInstallmentService = new PaymentInstallmentService();
    private final DossierCandidatService dossierService = new DossierCandidatService();
    private final MoniteurService moniteurService = new MoniteurService();
    private final NotificationService notificationService = new NotificationService();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int MAX_CODE_SEANCES = 20;

    @FXML
    public void initialize() {
        System.out.println("InsertSeanceCodeController: initialize() called.");
    }

    public void setParentController(SecretaireSeancesController parentController) {
        this.parentController = parentController;
    }

    public void setSeance(SeanceCode seance) {
        this.editingSeance = seance;
        User candidate = userService.getUserById(seance.getCandidatId());
        User moniteur = userService.getUserById(seance.getMoniteurId());
        if (candidate != null) {
            candidateUsernameField.setText(candidate.getUsername());
        }
        if (moniteur != null) {
            moniteurUsernameField.setText(moniteur.getUsername());
        }
        txtSessionDatetime.setText(seance.getSessionDatetime().format(dtf));
        btnSubmit.setText("Mettre à jour Séance Code");
    }

    @FXML
    private void handleSubmit() {
        clearErrors();
        boolean valid = true;

        String candidateUsername = candidateUsernameField.getText().trim();
        String moniteurUsername = moniteurUsernameField.getText().trim();
        String datetimeStr = txtSessionDatetime.getText().trim();

        if (candidateUsername.isEmpty()) {
            setFieldError(candidateUsernameField, candidateError, "Nom d'utilisateur candidat requis");
            valid = false;
        }
        if (moniteurUsername.isEmpty()) {
            setFieldError(moniteurUsernameField, moniteurError, "Nom d'utilisateur moniteur requis");
            valid = false;
        }
        if (datetimeStr.isEmpty()) {
            setFieldError(txtSessionDatetime, datetimeError, "Date/Heure requise");
            valid = false;
        }
        if (!valid) return;

        LocalDateTime sessionDatetime;
        try {
            sessionDatetime = LocalDateTime.parse(datetimeStr, dtf);
        } catch (Exception e) {
            setFieldError(txtSessionDatetime, datetimeError, "Format invalide (yyyy-MM-dd HH:mm)");
            return;
        }
        if (!sessionDatetime.isAfter(LocalDateTime.now())) {
            setFieldError(txtSessionDatetime, datetimeError, "La date doit être dans le futur");
            return;
        }
        int hour = sessionDatetime.getHour();
        if (hour < 8 || hour >= 17) {
            setFieldError(txtSessionDatetime, datetimeError, "La séance doit commencer entre 8:00 et 17:00");
            return;
        }

        User candidate = userService.getUserByUsername(candidateUsername);
        if (candidate == null) {
            setFieldError(candidateUsernameField, candidateError, "Candidat introuvable");
            return;
        }
        if (!"candidat".equalsIgnoreCase(candidate.getRole())) {
            setFieldError(candidateUsernameField, candidateError, "L'utilisateur n'est pas un candidat");
            return;
        }

        User moniteur = userService.getUserByUsername(moniteurUsername);
        if (moniteur == null) {
            setFieldError(moniteurUsernameField, moniteurError, "Moniteur introuvable");
            return;
        }
        if (!"moniteur".equalsIgnoreCase(moniteur.getRole())) {
            setFieldError(moniteurUsernameField, moniteurError, "L'utilisateur n'est pas un moniteur");
            return;
        }

        Optional<DossierCandidat> dossierOpt = dossierService.getDossierByCandidateId(candidate.getId());
        if (!dossierOpt.isPresent()) {
            setFieldError(candidateUsernameField, candidateError, "Dossier du candidat introuvable");
            return;
        }
        DossierCandidat dossier = dossierOpt.get();

        Optional<Moniteur> moniteurOpt = moniteurService.getMoniteurByUserId(moniteur.getId());
        if (!moniteurOpt.isPresent()) {
            setFieldError(moniteurUsernameField, moniteurError, "Moniteur introuvable");
            return;
        }
        Moniteur moniteurEntity = moniteurOpt.get();

        if (!dossier.getPermisType().equalsIgnoreCase(moniteurEntity.getPermisType().name())) {
            setFieldError(moniteurUsernameField, moniteurError, "Le permis du candidat ne correspond pas au permis du moniteur");
            return;
        }

        List<SeanceCode> candidateCodeSeances = seanceCodeService.getSeancesByCandidatId(candidate.getId());
        List<SeanceConduit> candidateConduitSeances = conduitService.getSeancesByCandidatId(candidate.getId());
        boolean candidateBusy = Stream.concat(
                candidateConduitSeances.stream().map(sc -> sc.getSessionDatetime()),
                candidateCodeSeances.stream().map(sc -> sc.getSessionDatetime())
        ).anyMatch(dt -> Math.abs(Duration.between(sessionDatetime, dt).toMinutes()) < 60);
        if (candidateBusy) {
            setFieldError(candidateUsernameField, candidateError, "Le candidat a une autre séance à cette heure");
            return;
        }
        if (candidateCodeSeances.size() >= MAX_CODE_SEANCES) {
            setFieldError(candidateUsernameField, candidateError, "Nombre maximum de séances de code atteint");
            return;
        }

        List<SeanceCode> moniteurCodeSeances = seanceCodeService.getSeancesByMoniteurId(moniteur.getId());
        List<SeanceConduit> moniteurConduitSeances = conduitService.getSeancesByMoniteurId(moniteur.getId());
        boolean moniteurBusy = Stream.concat(
                moniteurConduitSeances.stream().map(sc -> sc.getSessionDatetime()),
                moniteurCodeSeances.stream().map(sc -> sc.getSessionDatetime())
        ).anyMatch(dt -> Math.abs(Duration.between(sessionDatetime, dt).toMinutes()) < 60);
        if (moniteurBusy) {
            setFieldError(moniteurUsernameField, moniteurError, "Moniteur indisponible à cette heure");
            return;
        }

        if (editingSeance == null) {
            // Creating a brand-new seance
            SeanceCode newSeance = new SeanceCode(candidate.getId(), moniteur.getId(), sessionDatetime);
            boolean created = seanceCodeService.createSeanceCode(newSeance);
            if (created) {
                notificationService.sendNotification(candidate.getId(),
                        "Vous Avez une nouvelle Seance Code le "+sessionDatetime+".");
                notificationService.sendNotification(moniteur.getId(),
                        "Vous Avez une nouvelle Seance Code pour surveiller le "+sessionDatetime+".");
                showSuccessNotification("Séance Code créée avec succès !");

                clearForm();
                if (parentController != null) {
                    parentController.returnToSeancesPage();
                }
            } else {
                showError("Erreur", "Impossible de créer la séance code.");
            }
        } else {
            // Editing existing seance
            editingSeance.setCandidatId(candidate.getId());
            editingSeance.setMoniteurId(moniteur.getId());
            editingSeance.setSessionDatetime(sessionDatetime);

            boolean updated = seanceCodeService.updateSeanceCode(editingSeance);
            if (updated) {
                // >>> NEW: Show success notification <<<
                showSuccessNotification("Séance Code mise à jour avec succès !");

                clearForm();
                if (parentController != null) {
                    parentController.returnToSeancesPage();
                }
            } else {
                showError("Erreur", "Impossible de mettre à jour la séance code.");
            }
        }
    }

    private void showSuccessNotification(String message) {
        // Grab the StackPane from the Scene. In SecretaireDashboard.fxml, that's fx:id="contentArea".
        // We'll do a quick lookup by ID:
        StackPane contentArea = (StackPane) rootPane.getScene().lookup("#contentArea");
        if (contentArea != null) {
            NotificationUtil.showNotification(contentArea, message, NotificationType.SUCCESS);
        }
    }

    private void clearErrors() {
        candidateUsernameField.getStyleClass().remove("error");
        moniteurUsernameField.getStyleClass().remove("error");
        txtSessionDatetime.getStyleClass().remove("error");

        candidateError.setText("");
        moniteurError.setText("");
        datetimeError.setText("");
    }

    private void setFieldError(TextField field, Label errorLabel, String message) {
        if (!field.getStyleClass().contains("error")) {
            field.getStyleClass().add("error");
        }
        if (errorLabel != null) {
            errorLabel.setText(message);
        }
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearForm() {
        candidateUsernameField.clear();
        moniteurUsernameField.clear();
        txtSessionDatetime.clear();
        clearErrors();
        editingSeance = null;
        btnSubmit.setText("Créer Séance Code");
    }
}
