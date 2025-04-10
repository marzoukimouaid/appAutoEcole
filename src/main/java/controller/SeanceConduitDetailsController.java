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


public class SeanceConduitDetailsController {

    @FXML private Label lblTitle;
    @FXML private Label lblDate;
    @FXML private Label lblCandidate;
    @FXML private Label lblMoniteur;
    @FXML private WebView mapView;

    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private SeanceConduit seance;
    private SecretaireSeancesController parentController;

    private final SeanceConduitService conduitService = new SeanceConduitService();
    private final ProfileService profileService = new ProfileService();

    @FXML
    public void initialize() {

        btnEdit.setVisible(false);
        btnDelete.setVisible(false);


        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            switch (currentUser.getRole().toLowerCase()) {
                case "secretaire":
                    btnEdit.setVisible(true);
                    btnDelete.setVisible(true);
                    break;

                default:
                    break;
            }
        }


        WebEngine engine = mapView.getEngine();


        String mapUrl = getClass().getResource("/org/example/leafletMap.html").toExternalForm();
        engine.load(mapUrl);


        engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED && seance != null) {

                placeMarker();
            }
        });
    }

    
    public void setParentController(SecretaireSeancesController parentController) {
        this.parentController = parentController;
    }

    
    public void setSeance(SeanceConduit seance) {
        this.seance = seance;
        loadDetails();



        if (mapView.getEngine().getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            placeMarker();
        }
    }

    
    private void loadDetails() {
        lblTitle.setText("Détails de la Séance Conduit");
        lblDate.setText("Date/Heure: " + seance.getSessionDatetime());


        String candidateName = profileService.getProfileByUserId(seance.getCandidatId())
                .map(p -> p.getNom() + " " + p.getPrenom())
                .orElse("N/A");
        String moniteurName = profileService.getProfileByUserId(seance.getMoniteurId())
                .map(p -> p.getNom() + " " + p.getPrenom())
                .orElse("N/A");
        lblCandidate.setText("Candidat: " + candidateName);
        lblMoniteur.setText("Moniteur: " + moniteurName);
    }

    
    private void placeMarker() {
        double lat = seance.getLatitude();
        double lng = seance.getLongitude();


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

    
    @FXML
    private void handleEdit() {
        if (parentController != null) {

            parentController.openEditConduitPage(seance);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Parent controller non défini.");
            alert.showAndWait();
        }
    }

    
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
