package controller;

import entite.SeanceConduit;
import entite.User;
import entite.Profile;
import Utils.SessionManager;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import service.SeanceConduitService;
import service.ProfileService;
import java.util.Optional;

/**
 * Updated: we now show a Leaflet map for the lat/lon.
 */
public class SeanceConduitDetailsController {

    @FXML private Label lblTitle;
    @FXML private Label lblDate;
    @FXML private Label lblCandidate;
    @FXML private Label lblMoniteur;
    @FXML private WebView mapView;  // The new map view

    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private SeanceConduit seance;
    private SecretaireSeancesController parentController;

    private final SeanceConduitService conduitService = new SeanceConduitService();
    private final ProfileService profileService = new ProfileService();

    @FXML
    public void initialize() {
        // Hide edit and delete buttons by default.
        btnEdit.setVisible(false);
        btnDelete.setVisible(false);

        // Determine button visibility based on current user's role.
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            switch (currentUser.getRole().toLowerCase()) {
                case "secretaire":
                    btnEdit.setVisible(true);
                    btnDelete.setVisible(true);
                    break;
                // Candidates and moniteurs have read-only access.
                default:
                    break;
            }
        }

        // We load the "leafletMap.html" into the WebView once, so it’s ready for a marker injection.
        WebEngine engine = mapView.getEngine();

        // Adjust the path if needed. If "leafletMap.html" is in the same package, use getResource:
        String mapUrl = getClass().getResource("/org/example/leafletMap.html").toExternalForm();
        engine.load(mapUrl);

        // Once page is done loading, we can inject a marker or do more JS manipulation.
        engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED && seance != null) {
                // If seance is already set, place the marker.
                placeMarker();
            }
        });
    }

    /**
     * Called by the parent controller to inject itself.
     */
    public void setParentController(SecretaireSeancesController parentController) {
        this.parentController = parentController;
    }

    /**
     * Sets the SeanceConduit whose details will be displayed.
     */
    public void setSeance(SeanceConduit seance) {
        this.seance = seance;
        loadDetails();

        // If the map is already loaded, place the marker now.
        // Otherwise, it will happen in the loadWorker listener above.
        if (mapView.getEngine().getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            placeMarker();
        }
    }

    /**
     * Loads the seance details into the UI.
     */
    private void loadDetails() {
        lblTitle.setText("Détails de la Séance Conduit");
        lblDate.setText("Date/Heure: " + seance.getSessionDatetime());

        // Retrieve candidate & moniteur names from their profiles.
        String candidateName = profileService.getProfileByUserId(seance.getCandidatId())
                .map(p -> p.getNom() + " " + p.getPrenom())
                .orElse("N/A");
        String moniteurName = profileService.getProfileByUserId(seance.getMoniteurId())
                .map(p -> p.getNom() + " " + p.getPrenom())
                .orElse("N/A");
        lblCandidate.setText("Candidat: " + candidateName);
        lblMoniteur.setText("Moniteur: " + moniteurName);
    }

    /**
     * Places a marker on the loaded map at the seance's latitude/longitude.
     */
    private void placeMarker() {
        double lat = seance.getLatitude();
        double lng = seance.getLongitude();

        // If coordinates are invalid or zero, you might handle that. Otherwise:
        String script = String.format(
                "var latLng = L.latLng(%f, %f);" +
                        "if (typeof marker !== 'undefined' && marker) {" +
                        "   marker.setLatLng(latLng);" +
                        "} else {" +
                        "   marker = L.marker(latLng).addTo(map);" +
                        "}" +
                        "map.setView(latLng, 14);",
                lat, lng
        );

        mapView.getEngine().executeScript(script);
    }

    /**
     * Handles the Edit button action.
     */
    @FXML
    private void handleEdit() {
        if (parentController != null) {
            // Call the parent's method to open the edit page.
            parentController.openEditConduitPage(seance);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Parent controller non défini.");
            alert.showAndWait();
        }
    }

    /**
     * Handles the Delete button action.
     */
    @FXML
    private void handleDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Voulez-vous vraiment supprimer cette séance ?");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            boolean deleted = conduitService.deleteSeanceConduit(seance.getId());
            if (deleted) {
                new Alert(Alert.AlertType.INFORMATION, "Séance supprimée avec succès.").showAndWait();
                if (parentController != null) {
                    parentController.returnToSeancesPage();
                }
            } else {
                new Alert(Alert.AlertType.ERROR, "Impossible de supprimer la séance.").showAndWait();
            }
        }
    }
}
