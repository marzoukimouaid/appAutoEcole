package controller;

import entite.*;
import entite.Vehicule;
import entite.VehiculeDocument;
import entite.DossierCandidat;
import entite.Moniteur;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import service.*;
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
    private final NotificationService notificationService = new NotificationService();
    // Used to fetch candidate's dossier (which stores permis type and max sessions)
    private final DossierCandidatService dossierCandidatService = new DossierCandidatService();
    // Used to fetch the moniteur's details (including permis type)
    private final MoniteurService moniteurService = new MoniteurService();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // We'll store the chosen lat/lng in these fields:
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;

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

                // If editing an existing seance with valid lat/lng, show the marker:
                if (editingSeance != null) {
                    double lat = editingSeance.getLatitude();
                    double lng = editingSeance.getLongitude();
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

        // Load your leafletMap.html resource:
        try {
            String url = getClass().getResource("/org/example/leafletMap.html").toExternalForm();
            engine.load(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Called by the parent "SecretaireSeancesController"
    public void setParentController(SecretaireSeancesController parent) {
        this.parentController = parent;
    }

    // Used to populate the form for editing an existing seance.
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

        if (seance.getVehiculeId() > 0) {
            vehiculeService.getVehiculeById(seance.getVehiculeId())
                    .ifPresent(v -> txtVehiculeImmatriculation.setText(v.getImmatriculation()));
        }

        txtSessionDatetime.setText(seance.getSessionDatetime().format(dtf));
        btnSubmit.setText("Mettre à jour Séance Conduit");

        selectedLatitude = seance.getLatitude();
        selectedLongitude = seance.getLongitude();
    }

    @FXML
    private void handleSubmit() {
        System.out.println("InsertSeanceConduitController.handleSubmit: called.");
        try {
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
            System.out.println("Validation failed; aborting submission.");
            return;
        }

        Optional<Vehicule> optVehicule = vehiculeService.getVehiculeByImmatriculation(vehiculeImmatriculation);
        if (!optVehicule.isPresent()) {
            setFieldError(txtVehiculeImmatriculation, vehiculeError, "Véhicule non trouvé");
            return;
        }
        Vehicule vehicule = optVehicule.get();

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
        Optional<DossierCandidat> dossierOpt = dossierCandidatService.getDossierByCandidateId(candidate.getId());
        if (!dossierOpt.isPresent()) {
            setFieldError(candidateUsernameField, candidateError, "Dossier candidat introuvable");
            return;
        }
        int maxSeances = dossierOpt.get().getNombreSeancesConduite();
        if (candidateConduitSeances.size() >= maxSeances) {
            setFieldError(candidateUsernameField, candidateError, "Nombre maximum de séances de conduite atteint (" + maxSeances + ")");
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

        // New check: Ensure that the candidate's permis type, moniteur's permis type, and véhicule type all match.
        Optional<Moniteur> moniteurOpt = moniteurService.getMoniteurByUserId(moniteur.getId());
        if (!moniteurOpt.isPresent()) {
            setFieldError(moniteurUsernameField, moniteurError, "Détails du moniteur introuvables");
            return;
        }
        Moniteur moniteurEntity = moniteurOpt.get();
        String candidatePermis = dossierOpt.get().getPermisType(); // e.g., "A", "B", or "C"
        String moniteurPermis = moniteurEntity.getPermisType().name(); // e.g., "A", "B", or "C"
        if (!candidatePermis.equalsIgnoreCase(moniteurPermis)) {
            setFieldError(moniteurUsernameField, moniteurError, "Le permis du candidat et du moniteur ne correspondent pas");
            return;
        }
        Vehicule.VehicleType vehiculeType = vehicule.getType();
        boolean vehiculeMatches = false;
        switch (candidatePermis.toUpperCase()) {
            case "A":
                vehiculeMatches = vehiculeType == Vehicule.VehicleType.MOTO;
                break;
            case "B":
                vehiculeMatches = vehiculeType == Vehicule.VehicleType.VOITURE;
                break;
            case "C":
                vehiculeMatches = vehiculeType == Vehicule.VehicleType.CAMION;
                break;
            default:
                setFieldError(txtVehiculeImmatriculation, vehiculeError, "Type de permis candidat invalide");
                return;
        }
        if (!vehiculeMatches) {
            setFieldError(txtVehiculeImmatriculation, vehiculeError, "Le type du véhicule ne correspond pas au permis du candidat");
            return;
        }
        // NEW: Check if the vehicle has remaining kilometrage before maintenance.
        if (vehicule.getKmRestantEntretien() <= 0) {
            setFieldError(txtVehiculeImmatriculation, vehiculeError, "Le véhicule nécessite un entretien (kilométrage restant insuffisant).");
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
                notificationService.sendNotification(candidate.getId(),
                        "Vous Avez une nouvelle Séance Conduit le " + sessionDatetime + ".");
                notificationService.sendNotification(moniteur.getId(),
                        "Vous Avez une nouvelle Séance Conduit pour surveiller le " + sessionDatetime + ".");
                NotificationUtil.showNotification(rootPane, "Séance Conduit créée avec succès !", NotificationType.SUCCESS);
                clearForm();
            } else {
                showError("Erreur", "Impossible de créer la séance conduit.");
            }
        } else {
            editingSeance.setCandidatId(candidate.getId());
            editingSeance.setMoniteurId(moniteur.getId());
            editingSeance.setVehiculeId(vehicule.getId());
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
     * Exposed to JavaScript so that the map can call setCoordinates(...)
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
