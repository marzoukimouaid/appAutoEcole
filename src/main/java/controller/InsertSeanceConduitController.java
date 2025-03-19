package controller;

import entite.SeanceCode;
import entite.SeanceConduit;
import entite.User;
import entite.Vehicule;
import entite.VehiculeDocument;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import service.SeanceCodeService;
import service.SeanceConduitService;
import service.UserService;
import service.VehiculeService;
import service.VehiculeDocumentService;
import service.PaymentService;
import service.PaymentInstallmentService;
import javafx.scene.control.Alert.AlertType;
import Utils.NotificationUtil;
import Utils.NotificationUtil.NotificationType;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class InsertSeanceConduitController {

    @FXML private StackPane rootPane;
    @FXML private TextField candidateUsernameField;
    @FXML private Label candidateError;
    @FXML private TextField moniteurUsernameField;
    @FXML private Label moniteurError;
    @FXML private TextField txtVehiculeImmatriculation; // New field for vehicle immatriculation
    @FXML private Label vehiculeError;
    @FXML private TextField txtSessionDatetime;
    @FXML private Label datetimeError;
    @FXML private WebView mapView;
    @FXML private Label mapError;

    private SecretaireSeancesController parentController;
    private final SeanceConduitService conduitService = new SeanceConduitService();
    private final SeanceCodeService codeService = new SeanceCodeService();
    private final UserService userService = new UserService();
    private final VehiculeService vehiculeService = new VehiculeService();
    private final VehiculeDocumentService vehiculeDocumentService = new VehiculeDocumentService();
    private final PaymentService paymentService = new PaymentService();
    private final PaymentInstallmentService paymentInstallmentService = new PaymentInstallmentService();

    // Date/time format: "yyyy-MM-dd HH:mm"
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Coordinates selected from the map.
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;

    // Maximum allowed conduit sessions for a candidate.
    private static final int MAX_CONDUIT_SEANCES = 20;

    @FXML
    public void initialize() {
        System.out.println("InsertSeanceConduitController: initialize() called.");
        WebEngine engine = mapView.getEngine();
        engine.setJavaScriptEnabled(true);
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            System.out.println("WebEngine state changed: " + newState);
            if (newState == Worker.State.SUCCEEDED) {
                System.out.println("Map HTML loaded successfully. Setting javaConnector...");
                try {
                    JSObject window = (JSObject) engine.executeScript("window");
                    window.setMember("javaConnector", new JavaConnector());
                    System.out.println("javaConnector set successfully!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("Failed to set javaConnector: " + ex.getMessage());
                }
            }
        });
        try {
            String url = getClass().getResource("/org/example/leafletMap.html").toExternalForm();
            System.out.println("Attempting to load map from: " + url);
            engine.load(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setParentController(SecretaireSeancesController parent) {
        this.parentController = parent;
    }

    @FXML
    private void handleSubmit() {
        // Poll the map engine for marker coordinates.
        try {
            Object result = mapView.getEngine().executeScript("marker ? marker.getLatLng() : null");
            if (result != null && result instanceof JSObject) {
                JSObject latlng = (JSObject) result;
                Object latObj = latlng.getMember("lat");
                Object lngObj = latlng.getMember("lng");
                selectedLatitude = Double.parseDouble(latObj.toString());
                selectedLongitude = Double.parseDouble(lngObj.toString());
                System.out.println("Polled coordinates: " + selectedLatitude + ", " + selectedLongitude);
            } else {
                System.out.println("No marker found via polling.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("handleSubmit() called. Current lat/lng = " + selectedLatitude + ", " + selectedLongitude);
        clearErrors();
        boolean valid = true;

        String candidateUsername = candidateUsernameField.getText().trim();
        String moniteurUsername = moniteurUsernameField.getText().trim();
        String vehiculeImmatriculation = txtVehiculeImmatriculation.getText().trim();
        String datetimeStr = txtSessionDatetime.getText().trim();

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
            // Validate immatriculation pattern: xxxtunisiaxxxx (digits only, case insensitive)
            String immatriculationPattern = "(?i)^[0-9]{3}tunis[0-9]{4}$";
            if (!vehiculeImmatriculation.matches(immatriculationPattern)) {
                setFieldError(txtVehiculeImmatriculation, vehiculeError, "Format invalide. Attendu: xxxtunisiaxxxx");
                valid = false;
            }
        }
        if (datetimeStr.isEmpty()) {
            setFieldError(txtSessionDatetime, datetimeError, "Date/Heure requise");
            valid = false;
        }
        if (selectedLatitude == 0.0 && selectedLongitude == 0.0) {
            setMapError("Veuillez sélectionner une position sur la carte.");
            valid = false;
        }
        if (!valid) {
            System.out.println("Validation failed, not creating seance.");
            return;
        }

        // Fetch vehicle by immatriculation.
        Optional<Vehicule> optVehicule = vehiculeService.getVehiculeByImmatriculation(vehiculeImmatriculation);
        if (!optVehicule.isPresent()) {
            setFieldError(txtVehiculeImmatriculation, vehiculeError, "Véhicule non trouvé");
            return;
        }
        Vehicule vehicule = optVehicule.get();

        // Check that the vehicle has non-expired documents for all required types.
        List<VehiculeDocument> docs = vehiculeDocumentService.getDocumentsForVehicule(vehicule.getId());
        boolean hasVignette = docs.stream().anyMatch(d -> d.getDocType() == VehiculeDocument.DocType.VIGNETTE
                && d.getDateExpiration() != null && d.getDateExpiration().isAfter(LocalDate.now()));
        boolean hasVisite = docs.stream().anyMatch(d -> d.getDocType() == VehiculeDocument.DocType.VISITE_TECHNIQUE
                && d.getDateExpiration() != null && d.getDateExpiration().isAfter(LocalDate.now()));
        boolean hasAssurance = docs.stream().anyMatch(d -> d.getDocType() == VehiculeDocument.DocType.ASSURANCE
                && d.getDateExpiration() != null && d.getDateExpiration().isAfter(LocalDate.now()));
        boolean hasVidange = docs.stream().anyMatch(d -> d.getDocType() == VehiculeDocument.DocType.VIDANGE
                && d.getDateExpiration() != null && d.getDateExpiration().isAfter(LocalDate.now()));
        if (!(hasVignette && hasVisite && hasAssurance && hasVidange)) {
            setFieldError(txtVehiculeImmatriculation, vehiculeError, "Documents du véhicule manquants ou expirés");
            return;
        }

        // Parse session datetime.
        LocalDateTime sessionDatetime;
        try {
            sessionDatetime = LocalDateTime.parse(datetimeStr, dtf);
        } catch (Exception e) {
            setFieldError(txtSessionDatetime, datetimeError, "Format invalide (yyyy-MM-dd HH:mm)");
            System.out.println("Date parse failed: " + e.getMessage());
            return;
        }

        // Check that the session datetime is in the future.
        if (!sessionDatetime.isAfter(LocalDateTime.now())) {
            setFieldError(txtSessionDatetime, datetimeError, "La date doit être dans le futur");
            System.out.println("Session datetime is not in the future: " + sessionDatetime);
            return;
        }

        // Enforce working hours: session must start between 08:00 and 17:00.
        int hour = sessionDatetime.getHour();
        if (hour < 8 || hour >= 17) {
            setFieldError(txtSessionDatetime, datetimeError, "La séance doit commencer entre 8:00 et 17:00");
            System.out.println("Session time out of working hours: " + sessionDatetime);
            return;
        }

        // Check candidate availability.
        User candidate = userService.getUserByUsername(candidateUsername);
        if (candidate == null) {
            setFieldError(candidateUsernameField, candidateError, "Candidat introuvable");
            System.out.println("Candidate not found.");
            return;
        }
        if (!"candidat".equalsIgnoreCase(candidate.getRole())) {
            setFieldError(candidateUsernameField, candidateError, "L'utilisateur n'est pas un candidat");
            System.out.println("User found but role != candidat");
            return;
        }
        List<SeanceConduit> candidateConduitSeances = conduitService.getSeancesByCandidatId(candidate.getId());
        if (candidateConduitSeances.size() >= MAX_CONDUIT_SEANCES) {
            setFieldError(candidateUsernameField, candidateError, "Nombre maximum de séances de conduite atteint");
            System.out.println("Candidate has reached max conduit sessions.");
            return;
        }
        List<SeanceCode> candidateCodeSeances = codeService.getSeancesByCandidatId(candidate.getId());
        boolean candidateBusy = Stream.concat(
                candidateConduitSeances.stream().map(SeanceConduit::getSessionDatetime),
                candidateCodeSeances.stream().map(SeanceCode::getSessionDatetime)
        ).anyMatch(dt -> Math.abs(Duration.between(sessionDatetime, dt).toMinutes()) < 60);
        if (candidateBusy) {
            setFieldError(candidateUsernameField, candidateError, "Le candidat a une autre séance à cette heure");
            System.out.println("Candidate is busy within 60 minutes of " + sessionDatetime);
            return;
        }

        // Check moniteur availability.
        User moniteur = userService.getUserByUsername(moniteurUsername);
        if (moniteur == null) {
            setFieldError(moniteurUsernameField, moniteurError, "Moniteur introuvable");
            System.out.println("Moniteur not found.");
            return;
        }
        if (!"moniteur".equalsIgnoreCase(moniteur.getRole())) {
            setFieldError(moniteurUsernameField, moniteurError, "L'utilisateur n'est pas un moniteur");
            System.out.println("User found but role != moniteur");
            return;
        }
        List<SeanceConduit> moniteurConduit = conduitService.getSeancesByMoniteurId(moniteur.getId());
        List<SeanceCode> moniteurCode = codeService.getSeancesByMoniteurId(moniteur.getId());
        boolean moniteurBusy = Stream.concat(
                moniteurConduit.stream().map(SeanceConduit::getSessionDatetime),
                moniteurCode.stream().map(SeanceCode::getSessionDatetime)
        ).anyMatch(dt -> Math.abs(Duration.between(sessionDatetime, dt).toMinutes()) < 60);
        if (moniteurBusy) {
            setFieldError(moniteurUsernameField, moniteurError, "Moniteur indisponible à cette heure");
            System.out.println("Moniteur is busy within 60 minutes of " + sessionDatetime);
            return;
        }

        // Check candidate payment conditions.
        List<entite.Payment> payments = paymentService.getPaymentsForUser(candidate.getId());
        for (entite.Payment p : payments) {
            if ("FULL".equalsIgnoreCase(p.getPaymentType())) {
                if (!"PAID".equalsIgnoreCase(p.getStatus())) {
                    setFieldError(candidateUsernameField, candidateError, "Le paiement complet n'a pas été réglé");
                    System.out.println("Candidate has an unpaid full payment.");
                    return;
                }
            } else if ("INSTALLMENT".equalsIgnoreCase(p.getPaymentType())) {
                List<entite.PaymentInstallment> installments = paymentInstallmentService.getInstallmentsByPaymentId(p.getId());
                boolean hasOverdueUnpaid = installments.stream().anyMatch(inst ->
                        inst.getDueDate().isBefore(LocalDate.now()) && inst.getStatus() == entite.PaymentInstallment.Status.PENDING);
                if (hasOverdueUnpaid) {
                    setFieldError(candidateUsernameField, candidateError, "Les paiements par facilités ne sont pas à jour");
                    System.out.println("Candidate has an overdue unpaid installment.");
                    return;
                }
            }
        }

        // All validations passed; create the SeanceConduit.
        System.out.println("Creating SeanceConduit with vehiculeId=" + vehicule.getId() + ", lat=" + selectedLatitude + ", lon=" + selectedLongitude);
        SeanceConduit seance = new SeanceConduit(
                candidate.getId(),
                moniteur.getId(),
                vehicule.getId(),
                sessionDatetime,
                selectedLatitude,
                selectedLongitude
        );
        boolean created = conduitService.createSeanceConduit(seance);
        if (created) {
            System.out.println("SeanceConduit created successfully.");
            // Show success notification and clear form fields (stay on same page)
            NotificationUtil.showNotification(rootPane, "Séance Conduit créée avec succès !", NotificationType.SUCCESS);
            clearForm();
        } else {
            System.out.println("Failed to create SeanceConduit in DB.");
            showError("Erreur", "Impossible de créer la séance conduit.");
        }
    }

    private void clearErrors() {
        System.out.println("clearErrors() called.");
        candidateUsernameField.getStyleClass().remove("error");
        moniteurUsernameField.getStyleClass().remove("error");
        txtVehiculeImmatriculation.getStyleClass().remove("error");
        txtSessionDatetime.getStyleClass().remove("error");
        mapView.getStyleClass().remove("error");

        candidateError.setText("");
        moniteurError.setText("");
        vehiculeError.setText("");
        datetimeError.setText("");
        mapError.setText("");
    }

    private void setFieldError(TextField field, Label errorLabel, String message) {
        System.out.println("setFieldError: " + message);
        if (!field.getStyleClass().contains("error")) {
            field.getStyleClass().add("error");
        }
        if (errorLabel != null) {
            errorLabel.setText(message);
        }
    }

    private void setMapError(String message) {
        System.out.println("setMapError: " + message);
        if (!mapView.getStyleClass().contains("error")) {
            mapView.getStyleClass().add("error");
        }
        mapError.setText(message);
    }

    private void showError(String header, String content) {
        System.out.println("showError: " + content);
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearForm() {
        candidateUsernameField.clear();
        moniteurUsernameField.clear();
        txtVehiculeImmatriculation.clear();
        txtSessionDatetime.clear();
        // Optionally leave the map marker as is; here we only clear error messages.
        clearErrors();
    }

    /**
     * Inner class exposed to JavaScript.
     * When the user clicks on the map, JavaScript calls window.javaConnector.setCoordinates(lat, lng)
     * to update the selected coordinates.
     */
    public class JavaConnector {
        public void setCoordinates(double lat, double lng) {
            System.out.println("JavaConnector.setCoordinates called with lat=" + lat + ", lng=" + lng);
            Platform.runLater(() -> {
                selectedLatitude = lat;
                selectedLongitude = lng;
                System.out.println("Coordinates selected: " + lat + ", " + lng);
                mapView.getStyleClass().remove("error");
                mapError.setText("");
            });
        }
    }
}
