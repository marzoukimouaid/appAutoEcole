package controller;

import entite.SeanceConduit;
import entite.User;
import entite.Profile;
import Utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import service.SeanceConduitService;
import service.ProfileService;
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
    private SecretaireSeancesController parentController;

    private final SeanceConduitService conduitService = new SeanceConduitService();
    private final ProfileService profileService = new ProfileService();

    @FXML
    public void initialize() {
        // Hide edit and delete buttons by default.
        btnEdit.setVisible(false);
        btnDelete.setVisible(false);

        // Determine button visibility based on the current user's role.
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            switch (currentUser.getRole().toLowerCase()) {
                case "secretaire":
                    btnEdit.setVisible(true);
                    btnDelete.setVisible(true);
                    break;
                // Candidates and moniteurs have read-only access in this view.
                default:
                    break;
            }
        }
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
    }

    /**
     * Loads the seance details into the UI.
     */
    private void loadDetails() {
        lblTitle.setText("Détails de la Séance Conduit");
        lblDate.setText("Date/Heure: " + seance.getSessionDatetime());
        lblLocation.setText("Lieu: (" + seance.getLatitude() + ", " + seance.getLongitude() + ")");

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
     * Handles the Edit button action.
     * Opens the InsertSeanceConduit page with the current seance data prefilled.
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
