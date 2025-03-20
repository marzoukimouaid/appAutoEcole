package controller;

import entite.*;
import entite.Vehicule;
import entite.VehiculeDocument;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
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

    @FXML private BorderPane rootPane;
    @FXML private TextField candidateUsernameField;
    @FXML private Label candidateError;
    @FXML private TextField moniteurUsernameField;
    @FXML private Label moniteurError;
    @FXML private TextField txtVehiculeImmatriculation;
    @FXML private Label vehiculeError;
    @FXML private TextField txtSessionDatetime;
    @FXML private Label datetimeError;
    @FXML private WebView mapView;
    @FXML private Label mapError;
    @FXML private Button btnSubmit;

    private SecretaireSeancesController parentController;
    private SeanceConduit editingSeance = null;

    private final SeanceConduitService conduitService = new SeanceConduitService();
    private final SeanceCodeService seanceCodeService = new SeanceCodeService();
    private final UserService userService = new UserService();
    private final VehiculeService vehiculeService = new VehiculeService();
    private final VehiculeDocumentService vehiculeDocumentService = new VehiculeDocumentService();
    private final PaymentService paymentService = new PaymentService();
    private final PaymentInstallmentService paymentInstallmentService = new PaymentInstallmentService();

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // We'll store the chosen lat/lng in these fields:
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;

    private static final int MAX_CONDUIT_SEANCES = 20;

    @FXML
    public void initialize() {
        System.out.println("InsertSeanceConduitController: initialize() called.");
        WebEngine engine = mapView.getEngine();
        engine.setJavaScriptEnabled(true);

        // Listen for map load completion:
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                try {
                    // Provide a Java->JS bridge for setCoordinates(...)
                    JSObject window = (JSObject) engine.executeScript("window");
                    window.setMember("javaConnector", new JavaConnector());
                    System.out.println("JavaConnector set successfully.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                // <<< ADDED: If we are editing an existing seance and it has lat/lng, place a marker:
                if (editingSeance != null) {
                    double lat = editingSeance.getLatitude();
                    double lng = editingSeance.getLongitude();
                    // If it's not (0.0, 0.0), let's show the marker
                    if (Math.abs(lat) > 0.0001 || Math.abs(lng) > 0.0001) {
                        // For example, zoom to 13 near that location
                        String script =
                                "map.setView([" + lat + "," + lng + "], 13);" +
                                        "if (!marker) { marker = L.marker([" + lat + "," + lng + "]).addTo(map); } " +
                                        "else { marker.setLatLng([" + lat + "," + lng + "]); }";
                        engine.executeScript(script);
                    }
                }
            }
        });

        // Load your leafletMap.html resource from your /resources path:
        try {
            // Make sure the path is correct for your environment
            String url = getClass().getResource("/org/example/leafletMap.html").toExternalForm();
            engine.load(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This setter is called by the parent "SecretaireSeancesController"
    public void setParentController(SecretaireSeancesController parent) {
        this.parentController = parent;
    }

    // <<< ADDED: store the lat/lng from the existing SeanceConduit
    public void setSeance(SeanceConduit seance) {
        this.editingSeance = seance;
        User candidate = userService.getUserById(seance.getCandidatId());
        User moniteur = userService.getUserById(seance.getMoniteurId());

        if (candidate != null) {
            candidateUsernameField.setText(candidate.getUsername());
        }
        if (moniteur != null) {
            moniteurUsernameField.setText(moniteur.getUsername());
        }

        // If we have a valid vehiculeId, fill immatriculation
        if (seance.getVehiculeId() > 0) {
            vehiculeService.getVehiculeById(seance.getVehiculeId())
                    .ifPresent(v -> txtVehiculeImmatriculation.setText(v.getImmatriculation()));
        }

        // The session date/time
        txtSessionDatetime.setText(seance.getSessionDatetime().format(dtf));
        btnSubmit.setText("Mettre à jour Séance Conduit");

        // Store lat/lng so we can re-show them on the map
        selectedLatitude = seance.getLatitude();
        selectedLongitude = seance.getLongitude();
    }

    @FXML
    private void handleSubmit() {
        System.out.println("InsertSeanceConduitController.handleSubmit: called.");
        try {
            // The user might have placed a marker or re-placed it
            // We run a small script: if marker not null, getLatLng
            Object result = mapView.getEngine().executeScript("marker ? marker.getLatLng() : null");
            if (result != null && result instanceof JSObject) {
                JSObject latlng = (JSObject) result;
                Object latObj = latlng.getMember("lat");
                Object lngObj = latlng.getMember("lng");
                selectedLatitude = Double.parseDouble(latObj.toString());
                selectedLongitude = Double.parseDouble(lngObj.toString());
                System.out.println("Coordinates selected: " + selectedLatitude + ", " + selectedLongitude);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
            // This was your pattern check, you can keep or remove
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

        // Make sure user selected a location:
        if (selectedLatitude == 0.0 && selectedLongitude == 0.0) {
            setMapError("Veuillez sélectionner une position sur la carte.");
            valid = false;
        }
        if (!valid) {
            System.out.println("Validation failed; aborting submission.");
            return;
        }

        // Lookup the vehicule
        Optional<Vehicule> optVehicule = vehiculeService.getVehiculeByImmatriculation(vehiculeImmatriculation);
        if (!optVehicule.isPresent()) {
            setFieldError(txtVehiculeImmatriculation, vehiculeError, "Véhicule non trouvé");
            return;
        }
        Vehicule vehicule = optVehicule.get();

        // Check that vehicle documents are up to date
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
        List<SeanceConduit> candidateConduitSeances = conduitService.getSeancesByCandidatId(candidate.getId());
        if (candidateConduitSeances.size() >= MAX_CONDUIT_SEANCES) {
            setFieldError(candidateUsernameField, candidateError, "Nombre maximum de séances de conduite atteint");
            return;
        }
        List<SeanceCode> candidateCodeSeances = seanceCodeService.getSeancesByCandidatId(candidate.getId());
        boolean candidateBusy = Stream.concat(
                candidateConduitSeances.stream().map(sc -> sc.getSessionDatetime()),
                candidateCodeSeances.stream().map(sc -> sc.getSessionDatetime())
        ).anyMatch(dt -> Math.abs(Duration.between(sessionDatetime, dt).toMinutes()) < 60);
        if (candidateBusy) {
            setFieldError(candidateUsernameField, candidateError, "Le candidat a une autre séance à cette heure");
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
        List<SeanceConduit> moniteurConduit = conduitService.getSeancesByMoniteurId(moniteur.getId());
        List<SeanceCode> moniteurCode = seanceCodeService.getSeancesByMoniteurId(moniteur.getId());
        boolean moniteurBusy = Stream.concat(
                moniteurConduit.stream().map(sc -> sc.getSessionDatetime()),
                moniteurCode.stream().map(sc -> sc.getSessionDatetime())
        ).anyMatch(dt -> Math.abs(Duration.between(sessionDatetime, dt).toMinutes()) < 60);
        if (moniteurBusy) {
            setFieldError(moniteurUsernameField, moniteurError, "Moniteur indisponible à cette heure");
            return;
        }

        if (editingSeance == null) {
            // Creating a new seance
            SeanceConduit newSeance = new SeanceConduit(
                    candidate.getId(),
                    moniteur.getId(),
                    vehicule.getId(),
                    sessionDatetime,
                    selectedLatitude,
                    selectedLongitude
            );
            boolean created = conduitService.createSeanceConduit(newSeance);
            if (created) {
                NotificationUtil.showNotification(rootPane, "Séance Conduit créée avec succès !", NotificationType.SUCCESS);
                clearForm();
            } else {
                showError("Erreur", "Impossible de créer la séance conduit.");
            }
        } else {
            // Editing an existing seance
            editingSeance.setCandidatId(candidate.getId());
            editingSeance.setMoniteurId(moniteur.getId());
            editingSeance.setVehiculeId(vehicule.getId()); // Make sure to update the vehicle ID
            editingSeance.setSessionDatetime(sessionDatetime);
            editingSeance.setLatitude(selectedLatitude);
            editingSeance.setLongitude(selectedLongitude);

            boolean updated = conduitService.updateSeanceConduit(editingSeance);
            if (updated) {
                NotificationUtil.showNotification(rootPane, "Séance Conduit mise à jour avec succès !", NotificationType.SUCCESS);
                clearForm();
                if (parentController != null) {
                    parentController.returnToSeancesPage();
                }
            } else {
                showError("Erreur", "Impossible de mettre à jour la séance conduit.");
            }
        }
    }

    private void clearErrors() {
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
        if (!field.getStyleClass().contains("error")) {
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
        clearErrors();
        editingSeance = null;
        btnSubmit.setText("Créer Séance Conduit");
    }

    /**
     * This class is exposed to JavaScript so that the map can call setCoordinates(...)
     * whenever the user places or drags a marker.
     */
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
