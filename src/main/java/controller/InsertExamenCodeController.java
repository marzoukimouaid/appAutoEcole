package controller;

import entite.DossierCandidat;
import entite.ExamenCode;
import entite.Moniteur;
import entite.Payment;
import entite.PaymentInstallment;
import entite.SeanceCode;
import entite.SeanceConduit;
import entite.User;
import javafx.scene.layout.StackPane;
import service.DossierCandidatService;
import service.ExamenCodeService;
import service.MoniteurService;
import service.NotificationService;
import service.PaymentInstallmentService;
import service.PaymentService;
import service.SeanceCodeService;
import service.SeanceConduitService;
import service.UserService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import Utils.NotificationUtil;
import Utils.NotificationUtil.NotificationType;

/**
 * InsertExamenCodeController
 *
 * Existing verifications:
 *   - Candidate must exist, match moniteur's type, have valid payment, no overlapping schedule, etc.
 *
 * New verification:
 *   - Only allow new exam if candidate has at least 10 SeanceCode sessions.
 */
public class InsertExamenCodeController {

    @FXML
    private BorderPane rootPane;
    @FXML
    private TextField candidateUsernameField;
    @FXML
    private Label candidateError;
    @FXML
    private TextField moniteurUsernameField;
    @FXML
    private Label moniteurError;
    @FXML
    private TextField txtExamDatetime;
    @FXML
    private Label datetimeError;
    @FXML
    private TextField txtPrice;
    @FXML
    private Label priceError;
    @FXML
    private Button btnSubmit;

    // Reference to the parent controller for exam inscriptions.
    private SecretaireInscriptionExamenController parentController;
    // When editing an exam, this is non-null; if null, we're creating a new exam.
    private ExamenCode editingExam = null;

    // Service layer instances.
    private final ExamenCodeService examService = new ExamenCodeService();
    private final UserService userService = new UserService();
    private final SeanceCodeService seanceCodeService = new SeanceCodeService();
    private final SeanceConduitService seanceConduitService = new SeanceConduitService();
    private final DossierCandidatService dossierService = new DossierCandidatService();
    private final MoniteurService moniteurService = new MoniteurService();
    private final PaymentService paymentService = new PaymentService();
    private final PaymentInstallmentService paymentInstallmentService = new PaymentInstallmentService();
    private final NotificationService notificationService = new NotificationService();

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void setParentController(SecretaireInscriptionExamenController parentController) {
        this.parentController = parentController;
    }

    public void setExamenCode(ExamenCode exam) {
        this.editingExam = exam;
        User candidate = userService.getUserById(exam.getCandidatId());
        User moniteur = userService.getUserById(exam.getMoniteurId());
        if (candidate != null) {
            candidateUsernameField.setText(candidate.getUsername());
        }
        if (moniteur != null) {
            moniteurUsernameField.setText(moniteur.getUsername());
        }
        txtExamDatetime.setText(exam.getExamDatetime().format(dtf));
        txtPrice.setText(String.valueOf(exam.getPrice()));
        btnSubmit.setText("Mettre à jour Examen Code");
    }

    @FXML
    public void initialize() {
        System.out.println("InsertExamenCodeController initialized");
    }

    @FXML
    private void handleSubmit() {
        clearErrors();
        boolean valid = true;

        String candidateUsername = candidateUsernameField.getText().trim();
        String moniteurUsername = moniteurUsernameField.getText().trim();
        String datetimeStr = txtExamDatetime.getText().trim();
        String priceStr = txtPrice.getText().trim();

        if (candidateUsername.isEmpty()) {
            setFieldError(candidateUsernameField, candidateError, "Nom d'utilisateur candidat requis");
            valid = false;
        }
        if (moniteurUsername.isEmpty()) {
            setFieldError(moniteurUsernameField, moniteurError, "Nom d'utilisateur moniteur requis");
            valid = false;
        }
        if (datetimeStr.isEmpty()) {
            setFieldError(txtExamDatetime, datetimeError, "Date/Heure requise");
            valid = false;
        }
        if (priceStr.isEmpty()) {
            setFieldError(txtPrice, priceError, "Le prix est requis");
            valid = false;
        }
        if (!valid) return;

        LocalDateTime examDatetime;
        try {
            examDatetime = LocalDateTime.parse(datetimeStr, dtf);
        } catch (Exception e) {
            setFieldError(txtExamDatetime, datetimeError, "Format invalide (yyyy-MM-dd HH:mm)");
            return;
        }
        if (!examDatetime.isAfter(LocalDateTime.now())) {
            setFieldError(txtExamDatetime, datetimeError, "La date doit être dans le futur");
            return;
        }
        int hour = examDatetime.getHour();
        if (hour < 8 || hour >= 17) {
            setFieldError(txtExamDatetime, datetimeError, "L'examen doit être entre 8:00 et 17:00");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                setFieldError(txtPrice, priceError, "Le prix doit être un nombre positif");
                return;
            }
        } catch (NumberFormatException e) {
            setFieldError(txtPrice, priceError, "Le prix doit être un nombre valide");
            return;
        }

        Optional<User> optCandidate = Optional.ofNullable(userService.getUserByUsername(candidateUsername));
        if (!optCandidate.filter(u -> "candidat".equalsIgnoreCase(u.getRole())).isPresent()) {
            setFieldError(candidateUsernameField, candidateError, "Candidat introuvable ou invalide");
            return;
        }
        User candidate = optCandidate.get();

        Optional<User> optMoniteur = Optional.ofNullable(userService.getUserByUsername(moniteurUsername));
        if (!optMoniteur.filter(u -> "moniteur".equalsIgnoreCase(u.getRole())).isPresent()) {
            setFieldError(moniteurUsernameField, moniteurError, "Moniteur introuvable ou invalide");
            return;
        }
        User moniteur = optMoniteur.get();

        // Check if candidate's dossier exists, and moniteur's data
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
            setFieldError(moniteurUsernameField, moniteurError,
                    "Le permis du candidat ne correspond pas au permis du moniteur");
            return;
        }

        // Payment check: ensure candidate's payment is in order.
        List<Payment> payments = paymentService.getPaymentsForUser(candidate.getId());
        if (payments.isEmpty()) {
            setFieldError(candidateUsernameField, candidateError, "Aucun paiement trouvé pour ce candidat");
            return;
        }
        for (Payment p : payments) {
            if ("FULL".equalsIgnoreCase(p.getPaymentType())) {
                if (!"PAID".equalsIgnoreCase(p.getStatus())) {
                    setFieldError(candidateUsernameField, candidateError, "Le paiement complet n'a pas été réglé");
                    return;
                }
            } else if ("INSTALLMENT".equalsIgnoreCase(p.getPaymentType())) {
                if ("PENDING".equalsIgnoreCase(p.getStatus())) {
                    List<PaymentInstallment> installments = paymentInstallmentService.getInstallmentsByPaymentId(p.getId());
                    boolean overdue = installments.stream().anyMatch(inst ->
                            inst.getDueDate().isBefore(LocalDate.now()) &&
                                    inst.getStatus() == PaymentInstallment.Status.PENDING
                    );
                    if (overdue) {
                        setFieldError(candidateUsernameField, candidateError, "Une ou plusieurs échéances sont en retard");
                        return;
                    }
                }
            }
        }

        // If creating a new exam, ensure candidate has no exam code with status PENDING or PASSED
        List<ExamenCode> candidateExamCodes = examService.getExamenCodesByCandidatId(candidate.getId());
        if (editingExam != null) {
            // Exclude the exam being edited
            candidateExamCodes = candidateExamCodes.stream()
                    .filter(e -> e.getId() != editingExam.getId())
                    .collect(java.util.stream.Collectors.toList());
        }
        if (editingExam == null && candidateExamCodes.stream().anyMatch(e ->
                e.getStatus() == ExamenCode.ExamStatus.PENDING || e.getStatus() == ExamenCode.ExamStatus.PASSED)) {
            setFieldError(candidateUsernameField, candidateError,
                    "Le candidat a déjà un examen code en attente ou réussi");
            return;
        }

        // Schedule check for candidate
        Stream<LocalDateTime> candidateExamStream = examService.getExamenCodesByCandidatId(candidate.getId()).stream()
                .filter(e -> editingExam == null || e.getId() != editingExam.getId())
                .map(ExamenCode::getExamDatetime);
        boolean candidateBusy = Stream.concat(
                Stream.concat(
                        seanceCodeService.getSeancesByCandidatId(candidate.getId()).stream().map(SeanceCode::getSessionDatetime),
                        seanceConduitService.getSeancesByCandidatId(candidate.getId()).stream().map(SeanceConduit::getSessionDatetime)
                ),
                candidateExamStream
        ).anyMatch(dt -> Math.abs(Duration.between(examDatetime, dt).toMinutes()) < 60);
        if (candidateBusy) {
            setFieldError(candidateUsernameField, candidateError,
                    "Le candidat a une autre séance ou examen à cette heure");
            return;
        }

        // Schedule check for moniteur
        Stream<LocalDateTime> moniteurExamStream = examService.getExamenCodesByMoniteurId(moniteur.getId()).stream()
                .filter(e -> editingExam == null || e.getId() != editingExam.getId())
                .map(ExamenCode::getExamDatetime);
        boolean moniteurBusy = Stream.concat(
                Stream.concat(
                        seanceCodeService.getSeancesByMoniteurId(moniteur.getId()).stream().map(SeanceCode::getSessionDatetime),
                        seanceConduitService.getSeancesByMoniteurId(moniteur.getId()).stream().map(SeanceConduit::getSessionDatetime)
                ),
                moniteurExamStream
        ).anyMatch(dt -> Math.abs(Duration.between(examDatetime, dt).toMinutes()) < 60);
        if (moniteurBusy) {
            setFieldError(moniteurUsernameField, moniteurError,
                    "Le moniteur a une autre séance ou examen à cette heure");
            return;
        }

        // ======================= NEW CHECK =======================
        // Only if we are creating a new exam code (editingExam == null),
        // ensure the candidate has at least 10 SeanceCode sessions.
        if (editingExam == null) {
            List<SeanceCode> candidateSeancesCode = seanceCodeService.getSeancesByCandidatId(candidate.getId());
            if (candidateSeancesCode.size() < 10) {
                setFieldError(candidateUsernameField, candidateError,
                        "Le candidat doit avoir au moins 10 séances de code avant de passer l'examen code.");
                return;
            }
        }

        // CREATE or UPDATE exam code
        if (editingExam == null) {
            ExamenCode newExam = new ExamenCode(candidate.getId(), moniteur.getId(), examDatetime);
            newExam.setPrice(price);
            boolean created = examService.createExamenCode(newExam);
            if (created) {
                notificationService.sendNotification(candidate.getId(),
                        "Votre inscription à l'examen code a été créée avec succès.");
                notificationService.sendNotification(moniteur.getId(),
                        "Vous Avez une nouvelle Examen Code pour surveiller.");
                showSuccessNotification("Examen Code créée avec succès !");
                clearForm();
                if (parentController != null) {
                    parentController.returnToExamInscriptionsPage();
                }
            } else {
                showError("Erreur", "Impossible de créer l'examen code.");
            }
        } else {
            editingExam.setCandidatId(candidate.getId());
            editingExam.setMoniteurId(moniteur.getId());
            editingExam.setExamDatetime(examDatetime);
            editingExam.setPrice(price);

            boolean updated = examService.updateExamenCode(editingExam);
            if (updated) {
                notificationService.sendNotification(candidate.getId(),
                        "Votre inscription à l'examen code a été mise à jour avec succès.");
                showSuccessNotification("Examen Code mise à jour avec succès !");
                clearForm();
                if (parentController != null) {
                    parentController.returnToExamInscriptionsPage();
                }
            } else {
                showError("Erreur", "Impossible de mettre à jour l'examen code.");
            }
        }
    }

    private void clearErrors() {
        candidateUsernameField.getStyleClass().remove("error");
        moniteurUsernameField.getStyleClass().remove("error");
        txtExamDatetime.getStyleClass().remove("error");
        txtPrice.getStyleClass().remove("error");

        candidateError.setText("");
        moniteurError.setText("");
        datetimeError.setText("");
        priceError.setText("");
    }

    private void setFieldError(TextField field, Label errorLabel, String message) {
        if (field != null && !field.getStyleClass().contains("error")) {
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

    private void showSuccessNotification(String message) {
        StackPane contentArea = (StackPane) rootPane.getScene().lookup("#contentArea");
        if (contentArea != null) {
            NotificationUtil.showNotification(contentArea, message, NotificationType.SUCCESS);
        }
    }

    private void clearForm() {
        candidateUsernameField.clear();
        moniteurUsernameField.clear();
        txtExamDatetime.clear();
        txtPrice.clear();
        clearErrors();
        editingExam = null;
        btnSubmit.setText("Créer Examen Code");
    }
}
