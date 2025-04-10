package controller;

import entite.ExamenConduit;
import entite.Profile;
import entite.User;
import Utils.SessionManager;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import service.ExamenConduitService;
import service.ProfileService;

import java.time.LocalDate;
import java.util.Optional;


public class ExamenConduitDetailsController {

    @FXML private Label lblTitle;
    @FXML private Label lblDate;
    @FXML private Label lblStatus;

    @FXML private Label lblCandidate;
    @FXML private Label lblMoniteur;


    @FXML private WebView mapView;

    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnMarkPassed;

    private ExamenConduit examenConduit;
    private final ExamenConduitService examService = new ExamenConduitService();
    private final ProfileService profileService = new ProfileService();

    private SecretaireInscriptionExamenController parentController;

    @FXML
    public void initialize() {
        btnEdit.setVisible(false);
        btnDelete.setVisible(false);
        btnMarkPassed.setVisible(false);

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            switch (currentUser.getRole()) {
                case "candidate":

                    break;
                case "secretaire":
                    btnEdit.setVisible(true);
                    btnDelete.setVisible(true);
                    break;
                case "moniteur":
                    btnMarkPassed.setVisible(true);
                    break;
            }
        }


        WebEngine engine = mapView.getEngine();

        String mapUrl = getClass().getResource("/org/example/leafletMap.html").toExternalForm();
        engine.load(mapUrl);


        engine.getLoadWorker().stateProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == Worker.State.SUCCEEDED && examenConduit != null) {
                placeMarker();
            }
        });
    }

    public void setParentController(SecretaireInscriptionExamenController parentController) {
        this.parentController = parentController;
    }

    public void setExamenConduit(ExamenConduit examenConduit) {
        this.examenConduit = examenConduit;
        loadDetails();


        if (mapView.getEngine().getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            placeMarker();
        }
    }

    private void loadDetails() {
        lblTitle.setText("Détails de l'Examen Conduit");
        lblDate.setText("Date/Heure: " + examenConduit.getExamDatetime());
        lblStatus.setText("Statut: " + examenConduit.getStatus().name());


        String candidateName = profileService.getProfileByUserId(examenConduit.getCandidatId())
                .map(p -> p.getNom() + " " + p.getPrenom())
                .orElse("N/A");
        String moniteurName = profileService.getProfileByUserId(examenConduit.getMoniteurId())
                .map(p -> p.getNom() + " " + p.getPrenom())
                .orElse("N/A");

        lblCandidate.setText("Candidat: " + candidateName);
        lblMoniteur.setText("Moniteur: " + moniteurName);



    }

    
    private void placeMarker() {
        double lat = examenConduit.getLatitude();
        double lng = examenConduit.getLongitude();


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
            parentController.openEditExamenConduitPage(examenConduit);
        }
    }

    @FXML
    private void handleDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Voulez-vous vraiment supprimer cet examen ?");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            boolean deleted = examService.deleteExamenConduit(examenConduit.getId());
            if (deleted) {
                new Alert(Alert.AlertType.INFORMATION, "Examen supprimé avec succès.").showAndWait();
                if (parentController != null) {
                    parentController.returnToExamInscriptionsPage();
                }
            } else {
                new Alert(Alert.AlertType.ERROR, "Impossible de supprimer l'examen.").showAndWait();
            }
        }
    }

    
    @FXML
    private void handleMarkPassed() {

        if (examenConduit.getPaiementStatus() != ExamenConduit.PaymentStatus.PAID) {
            new Alert(Alert.AlertType.WARNING,
                    "Impossible de marquer l'examen comme réussi: il n'est pas payé.")
                    .showAndWait();
            return;
        }

        if (!examenConduit.getExamDatetime().toLocalDate().equals(LocalDate.now())) {
            new Alert(Alert.AlertType.WARNING,
                    "Impossible de marquer l'examen comme réussi: la date d'examen n'est pas aujourd'hui.")
                    .showAndWait();
            return;
        }

        examenConduit.setStatus(ExamenConduit.ExamStatus.PASSED);
        boolean updated = examService.updateExamenConduit(examenConduit);
        if (updated) {
            lblStatus.setText("Statut: PASSED");
            new Alert(Alert.AlertType.INFORMATION, "Examen conduit marqué comme Réussi.").showAndWait();
        } else {
            new Alert(Alert.AlertType.ERROR, "Impossible de mettre à jour l'examen conduit.").showAndWait();
        }
    }
}
