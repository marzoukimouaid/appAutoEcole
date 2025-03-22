package controller;

import entite.DossierCandidat;
import entite.ExamenConduit;
import entite.Moniteur;
import entite.SeanceCode;
import entite.SeanceConduit;
import entite.User;
import entite.Vehicule;
import entite.VehiculeDocument;
import entite.Payment;
import entite.PaymentInstallment;
import Utils.NotificationUtil;
import Utils.NotificationUtil.NotificationType;
import service.DossierCandidatService;
import service.ExamenConduitService;
import service.ExamenCodeService;
import service.SeanceCodeService;
import service.SeanceConduitService;
import service.UserService;
import service.VehiculeDocumentService;
import service.VehiculeService;
import service.PaymentService;
import service.PaymentInstallmentService;
import service.MoniteurService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class InsertExamenConduitController {

    @FXML private BorderPane rootPane;
    @FXML private TextField candidateUsernameField;
    @FXML private Label candidateError;
    @FXML private TextField moniteurUsernameField;
    @FXML private Label moniteurError;
    @FXML private TextField txtVehiculeImmatriculation;
    @FXML private Label vehiculeError;
    @FXML private TextField txtExamDatetime;
    @FXML private Label datetimeError;
    // New Price field and error label:
    @FXML private TextField txtPrice;
    @FXML private Label priceError;

    @FXML private WebView mapView;
    @FXML private Label mapError;
    @FXML private Button btnSubmit;

    // Reference to parent controller (of type SecretaireInscriptionExamenController)
    private SecretaireInscriptionExamenController parentController;
    // When editing an exam, this is non-null; if null, we are creating a new exam.
    private ExamenConduit editingExam = null;

    // Service layer instances.
    private final ExamenConduitService examenConduitService = new ExamenConduitService();
    private final ExamenCodeService examenCodeService = new ExamenCodeService();
    private final SeanceCodeService seanceCodeService = new SeanceCodeService();
    private final SeanceConduitService seanceConduitService = new SeanceConduitService();
    private final UserService userService = new UserService();
    private final VehiculeService vehiculeService = new VehiculeService();
    private final VehiculeDocumentService vehiculeDocumentService = new VehiculeDocumentService();
    private final PaymentService paymentService = new PaymentService();
    private final PaymentInstallmentService paymentInstallmentService = new PaymentInstallmentService();
    private final DossierCandidatService dossierService = new DossierCandidatService();
    private final MoniteurService moniteurService = new MoniteurService();

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Map coordinates (set via JS bridge)
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;

    @FXML
    public void initialize() {
        WebEngine engine = mapView.getEngine();
        engine.setJavaScriptEnabled(true);
        // Listen for the map load completion and set up the JavaScript bridge.
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                try {
                    JSObject window = (JSObject) engine.executeScript("window");
                    window.setMember("javaConnector", new JavaConnector());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                // If editing an exam, show the marker on the map.
                if (editingExam != null) {
                    double lat = editingExam.getLatitude();
                    double lng = editingExam.getLongitude();
                    if (Math.abs(lat) > 0.0001 || Math.abs(lng) > 0.0001) {
                        String script =
                                "map.setView([" + lat + "," + lng + "], 13);" +
                                        "if (!marker) { marker = L.marker([" + lat + "," + lng + "]).addTo(map); } " +
                                        "else { marker.setLatLng([" + lat + "," + lng + "]); }";
                        engine.executeScript(script);
                    }
                }
            }
        });
        try {
            String url = getClass().getResource("/org/example/leafletMap.html").toExternalForm();
            engine.load(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Setter for injecting the parent controller.
    public void setParentController(SecretaireInscriptionExamenController parent) {
        this.parentController = parent;
    }

    // Populate fields when editing an existing exam.
    public void setExamenConduit(ExamenConduit exam) {
        this.editingExam = exam;
        User candidate = userService.getUserById(exam.getCandidatId());
        User moniteur = userService.getUserById(exam.getMoniteurId());
        if (candidate != null) {
            candidateUsernameField.setText(candidate.getUsername());
        }
        if (moniteur != null) {
            moniteurUsernameField.setText(moniteur.getUsername());
        }
        if (exam.getVehiculeId() > 0) {
            vehiculeService.getVehiculeById(exam.getVehiculeId())
                    .ifPresent(v -> txtVehiculeImmatriculation.setText(v.getImmatriculation()));
        }
        txtExamDatetime.setText(exam.getExamDatetime().format(dtf));
        txtPrice.setText(String.valueOf(exam.getPrice()));
        btnSubmit.setText("Mettre à jour Examen Conduit");
        selectedLatitude = exam.getLatitude();
        selectedLongitude = exam.getLongitude();
    }

    @FXML
    private void handleSubmit() {
        // Try to update map coordinates in case the marker was moved.
        try {
            Object result = mapView.getEngine().executeScript("marker ? marker.getLatLng() : null");
            if (result != null && result instanceof JSObject) {
                JSObject latlng = (JSObject) result;
                selectedLatitude = Double.parseDouble(latlng.getMember("lat").toString());
                selectedLongitude = Double.parseDouble(latlng.getMember("lng").toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        clearErrors();
        boolean valid = true;

        String candidateUsername = candidateUsernameField.getText().trim();
        String moniteurUsername = moniteurUsernameField.getText().trim();
        String vehiculeImmatriculation = txtVehiculeImmatriculation.getText().trim();
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
        if (vehiculeImmatriculation.isEmpty()) {
            setFieldError(txtVehiculeImmatriculation, vehiculeError, "Immatriculation du véhicule requise");
            valid = false;
        } else {
            String immatriculationPattern = "(?i)^[0-9]{3}tunis[0-9]{4}$";
            if (!vehiculeImmatriculation.matches(immatriculationPattern)) {
                setFieldError(txtVehiculeImmatriculation, vehiculeError, "Format invalide. Attendu: xxxtunisiaxxxx");
                valid = false;
            }
        }
        if (datetimeStr.isEmpty()) {
            setFieldError(txtExamDatetime, datetimeError, "Date/Heure requise");
            valid = false;
        }
        if (priceStr.isEmpty()) {
            setFieldError(txtPrice, priceError, "Le prix est requis");
            valid = false;
        }
        if (selectedLatitude == 0.0 && selectedLongitude == 0.0) {
            setMapError("Veuillez sélectionner une position sur la carte.");
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

        // Retrieve candidate and moniteur.
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

        // Verify candidate's dossier and matching permis type.
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

        // Payment check: ensure candidate's payment is in order.
        List<Payment> payments = paymentService.getPaymentsForUser(candidate.getId());
        if (payments.isEmpty()) {
            setFieldError(candidateUsernameField, candidateError, "Aucun paiement trouvé pour ce candidat");
            return;
        }
        for (Payment p : payments) {
            if ("FULL".equalsIgnoreCase(p.getPaymentType())) {
                if (!p.getStatus().equalsIgnoreCase("PAID")) {
                    setFieldError(candidateUsernameField, candidateError, "Le paiement complet n'a pas été réglé");
                    return;
                }
            } else if ("INSTALLMENT".equalsIgnoreCase(p.getPaymentType())) {
                if (p.getStatus().equalsIgnoreCase("PENDING")) {
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

        // For new exam, ensure candidate does not already have an exam conduit with status PENDING or PASSED.
        if (editingExam == null) {
            List<ExamenConduit> candidateExamConduits = examenConduitService.getExamenConduitsByCandidatId(candidate.getId());
            boolean alreadyHasExam = candidateExamConduits.stream().anyMatch(e ->
                    e.getStatus() == ExamenConduit.ExamStatus.PENDING || e.getStatus() == ExamenConduit.ExamStatus.PASSED
            );
            if (alreadyHasExam) {
                setFieldError(candidateUsernameField, candidateError, "Le candidat a déjà un examen conduit en cours ou réussi");
                return;
            }
        }

        // Check candidate availability (across seances and exam conduit registrations) within a 60-minute window.
        boolean candidateBusy = Stream.concat(
                Stream.concat(
                        seanceCodeService.getSeancesByCandidatId(candidate.getId()).stream().map(SeanceCode::getSessionDatetime),
                        seanceConduitService.getSeancesByCandidatId(candidate.getId()).stream().map(SeanceConduit::getSessionDatetime)
                ),
                examenConduitService.getExamenConduitsByCandidatId(candidate.getId()).stream().map(e -> e.getExamDatetime())
        ).anyMatch(dt -> Math.abs(Duration.between(examDatetime, dt).toMinutes()) < 60);
        if (candidateBusy) {
            setFieldError(candidateUsernameField, candidateError, "Le candidat a une autre séance ou examen à cette heure");
            return;
        }

        // Check moniteur availability (across seances and exam conduit registrations) within a 60-minute window.
        boolean moniteurBusy = Stream.concat(
                Stream.concat(
                        seanceCodeService.getSeancesByMoniteurId(moniteur.getId()).stream().map(SeanceCode::getSessionDatetime),
                        seanceConduitService.getSeancesByMoniteurId(moniteur.getId()).stream().map(SeanceConduit::getSessionDatetime)
                ),
                examenConduitService.getExamenConduitsByMoniteurId(moniteur.getId()).stream().map(e -> e.getExamDatetime())
        ).anyMatch(dt -> Math.abs(Duration.between(examDatetime, dt).toMinutes()) < 60);
        if (moniteurBusy) {
            setFieldError(moniteurUsernameField, moniteurError, "Le moniteur a une autre séance ou examen à cette heure");
            return;
        }

        // All validations passed – proceed to create or update the exam conduit registration.
        if (editingExam == null) {
            ExamenConduit newExam = new ExamenConduit(
                    candidate.getId(),
                    moniteur.getId(),
                    vehiculeService.getVehiculeByImmatriculation(vehiculeImmatriculation)
                            .map(Vehicule::getId)
                            .orElse(0),
                    examDatetime,
                    selectedLatitude,
                    selectedLongitude
            );
            newExam.setPrice(price);
            // By default, paiement_status remains PENDING.
            boolean created = examenConduitService.createExamenConduit(newExam);
            if (created) {
                Platform.runLater(() -> {
                    StackPane notificationParent = getNotificationParent();
                    NotificationUtil.showNotification(notificationParent, "Examen Conduit créé avec succès !", NotificationType.SUCCESS);
                });
                clearForm();
            } else {
                showError("Erreur", "Impossible de créer l'examen conduit.");
            }
        } else {
            editingExam.setCandidatId(candidate.getId());
            editingExam.setMoniteurId(moniteur.getId());
            editingExam.setVehiculeId(vehiculeService.getVehiculeByImmatriculation(vehiculeImmatriculation)
                    .map(Vehicule::getId)
                    .orElse(0));
            editingExam.setExamDatetime(examDatetime);
            editingExam.setPrice(price);
            editingExam.setLatitude(selectedLatitude);
            editingExam.setLongitude(selectedLongitude);
            boolean updated = examenConduitService.updateExamenConduit(editingExam);
            if (updated) {
                Platform.runLater(() -> {
                    StackPane notificationParent = getNotificationParent();
                    NotificationUtil.showNotification(notificationParent, "Examen Conduit mis à jour avec succès !", NotificationType.SUCCESS);
                });
                clearForm();
                if (parentController != null) {
                    parentController.returnToExamInscriptionsPage();
                }
            } else {
                showError("Erreur", "Impossible de mettre à jour l'examen conduit.");
            }
        }
    }

    private StackPane getNotificationParent() {
        if (rootPane.getScene() == null) {
            System.err.println("Scene is not set. Waiting for scene to be set...");
            // Add a listener and return a fallback StackPane.
            final StackPane[] stackHolder = new StackPane[1];
            rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    Node sceneRoot = newScene.getRoot();
                    if (sceneRoot instanceof StackPane) {
                        stackHolder[0] = (StackPane) sceneRoot;
                    } else {
                        StackPane stack = new StackPane();
                        stack.getChildren().add(sceneRoot);
                        newScene.setRoot(stack);
                        stackHolder[0] = stack;
                    }
                }
            });
            return new StackPane();
        }
        Node sceneRoot = rootPane.getScene().getRoot();
        if (sceneRoot instanceof StackPane) {
            return (StackPane) sceneRoot;
        } else {
            StackPane stack = new StackPane();
            stack.getChildren().add(sceneRoot);
            rootPane.getScene().setRoot(stack);
            return stack;
        }
    }

    private void clearErrors() {
        candidateUsernameField.getStyleClass().remove("error");
        moniteurUsernameField.getStyleClass().remove("error");
        txtVehiculeImmatriculation.getStyleClass().remove("error");
        txtExamDatetime.getStyleClass().remove("error");
        txtPrice.getStyleClass().remove("error");
        mapView.getStyleClass().remove("error");

        candidateError.setText("");
        moniteurError.setText("");
        vehiculeError.setText("");
        datetimeError.setText("");
        mapError.setText("");
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

    private void setMapError(String message) {
        if (!mapView.getStyleClass().contains("error")) {
            mapView.getStyleClass().add("error");
        }
        mapError.setText(message);
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
        txtVehiculeImmatriculation.clear();
        txtExamDatetime.clear();
        txtPrice.clear();
        clearErrors();
        editingExam = null;
        btnSubmit.setText("Créer Examen Conduit");
    }

    // JavaScript bridge class for updating map coordinates.
    public class JavaConnector {
        public void setCoordinates(double lat, double lng) {
            Platform.runLater(() -> {
                selectedLatitude = lat;
                selectedLongitude = lng;
                mapView.getStyleClass().remove("error");
                mapError.setText("");
            });
        }
    }
}
