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

/**
 * SeanceConduitDetailsController
 *
 * Dynamically shows/hides buttons depending on user’s role:
 *  - candidate => read-only (no buttons)
 *  - secretaire => can edit & delete
 *  - moniteur => read-only (assuming no special moniteur actions on seances)
 *
 * We have a setParentController(...) method to call back if user is secretaire.
 */
public class SeanceConduitDetailsController {

    @FXML private Label lblTitle;
    @FXML private Label lblDate;
    @FXML private Label lblLocation;
    @FXML private Label lblCandidate;
    @FXML private Label lblMoniteur;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    // We'll store the seance object
    private SeanceConduit seance;

    // Reference to a parent, if needed for secretarial actions
    private SecretaireSeancesController parentController;

    private final SeanceConduitService conduitService = new SeanceConduitService();
    private final ProfileService profileService = new ProfileService();

    @FXML
    public void initialize() {
        // Hide all by default
        btnEdit.setVisible(false);
        btnDelete.setVisible(false);

        // Check the current user's role
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            switch (currentUser.getRole()) {
                case "candidate":
                    // read-only
                    break;
                case "secretaire":
                    // show edit + delete
                    btnEdit.setVisible(true);
                    btnDelete.setVisible(true);
                    break;
                case "moniteur":
                    // read-only for seance conduit (unless you decide otherwise)
                    break;
            }
        }
    }

    /**
     * Let the parent (SecretaireSeancesController, for example) be injected
     * so we can call back after edit or delete if we want.
     */
    public void setParentController(SecretaireSeancesController parentController) {
        this.parentController = parentController;
    }

    /**
     * The calling code sets the seance we’re showing.
     */
    public void setSeance(SeanceConduit seance) {
        this.seance = seance;
        loadDetails();
    }

    private void loadDetails() {
        lblTitle.setText("Détails de la Séance Conduit");
        lblDate.setText("Date/Heure: " + seance.getSessionDatetime());
        lblLocation.setText("Lieu: (" + seance.getLatitude() + ", " + seance.getLongitude() + ")");

        // Candidate & Moniteur from profile
        String candName = profileService.getProfileByUserId(seance.getCandidatId())
                .map(p -> p.getNom() + " " + p.getPrenom())
                .orElse("N/A");
        String monName = profileService.getProfileByUserId(seance.getMoniteurId())
                .map(p -> p.getNom() + " " + p.getPrenom())
                .orElse("N/A");

        lblCandidate.setText("Candidat: " + candName);
        lblMoniteur.setText("Moniteur: " + monName);
    }

    @FXML
    private void handleEdit() {
        // Only for secretaire
        if (parentController != null) {
            // e.g. parentController.openEditSeanceConduitPage(seance);
            System.out.println("handleEdit SeanceConduit (secretaire)...");
        }
    }

    @FXML
    private void handleDelete() {
        // Only for secretaire
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer");
        confirm.setHeaderText("Supprimer cette Séance Conduit ?");
        confirm.setContentText("Cette action est irréversible.");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            boolean success = conduitService.deleteSeanceConduit(seance.getId());
            if (success) {
                new Alert(Alert.AlertType.INFORMATION, "Séance supprimée.").showAndWait();
                if (parentController != null) {
                    // e.g. parentController.returnToSeancesPage();
                }
            } else {
                new Alert(Alert.AlertType.ERROR, "Impossible de supprimer la séance.").showAndWait();
            }
        }
    }
}
