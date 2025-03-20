package controller;

import entite.SeanceConduit;
import entite.Profile;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import service.SeanceConduitService;
import service.ProfileService;
import service.UserService;
import java.io.IOException;
import java.util.Optional;

public class SeanceConduitDetailsController {
    @FXML private Label lblTitle;
    @FXML private Label lblDate;
    @FXML private Label lblLocation;
    @FXML private Label lblCandidate;
    @FXML private Label lblMoniteur;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private SeanceConduit seance;
    private final SeanceConduitService conduitService = new SeanceConduitService();
    private final ProfileService profileService = new ProfileService();
    private final UserService userService = new UserService();

    // Add a reference to the parent controller:
    private SecretaireSeancesController parentController;

    // Add this setter so the parent can inject itself:
    public void setParentController(SecretaireSeancesController parentController) {
        this.parentController = parentController;
    }

    public void setSeance(SeanceConduit seance) {
        this.seance = seance;
        loadDetails();
    }

    private void loadDetails() {
        lblTitle.setText("Détails de la Séance Conduit");
        lblDate.setText("Date/Heure: " + seance.getSessionDatetime().toString());
        lblLocation.setText("Lieu: (" + seance.getLatitude() + ", " + seance.getLongitude() + ")");

        String candidateFullName = profileService.getProfileByUserId(seance.getCandidatId())
                .map(p -> p.getNom() + " " + p.getPrenom())
                .orElse("N/A");
        String moniteurFullName = profileService.getProfileByUserId(seance.getMoniteurId())
                .map(p -> p.getNom() + " " + p.getPrenom())
                .orElse("N/A");

        lblCandidate.setText("Candidat: " + candidateFullName);
        lblMoniteur.setText("Moniteur: " + moniteurFullName);
    }

    @FXML
    private void handleEdit() {
        // Instead of setting the center directly, call the parent's method
        // which loads InsertSeanceConduit.fxml properly.
        if (parentController != null) {
            parentController.openEditConduitPage(seance);
        }
    }

    @FXML
    private void handleDelete() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmer la suppression");
        confirmAlert.setHeaderText("Voulez-vous vraiment supprimer cette séance ?");
        confirmAlert.setContentText("Cette action est irréversible.");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = conduitService.deleteSeanceConduit(seance.getId());
            if (deleted) {
                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("Suppression réussie");
                infoAlert.setHeaderText(null);
                infoAlert.setContentText("La séance a été supprimée avec succès.");
                infoAlert.showAndWait();

                // After deletion, tell the parent to reload the list
                if (parentController != null) {
                    parentController.returnToSeancesPage();
                }
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur");
                errorAlert.setHeaderText("Suppression échouée");
                errorAlert.setContentText("La séance n'a pas pu être supprimée.");
                errorAlert.showAndWait();
            }
        }
    }
}
