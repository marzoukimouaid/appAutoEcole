package controller;

import entite.SeanceCode;
import entite.User;
import entite.Profile;
import Utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import service.SeanceCodeService;
import service.ProfileService;
import java.util.Optional;

public class SeanceCodeDetailsController {

    @FXML private Label lblTitle;
    @FXML private Label lblDate;
    @FXML private Label lblCandidate;
    @FXML private Label lblMoniteur;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private SeanceCode seance;
    private SecretaireSeancesController parentController;

    private final SeanceCodeService codeService = new SeanceCodeService();
    private final ProfileService profileService = new ProfileService();

    @FXML
    public void initialize() {
        // Hide edit and delete buttons by default.
        btnEdit.setVisible(false);
        btnDelete.setVisible(false);

        // Display buttons based on the current user's role.
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
    }

    /**
     * Injects the parent controller so that navigation methods can be invoked.
     */
    public void setParentController(SecretaireSeancesController parentController) {
        this.parentController = parentController;
    }

    /**
     * Sets the SeanceCode record whose details are displayed.
     */
    public void setSeance(SeanceCode seance) {
        this.seance = seance;
        loadDetails();
    }

    /**
     * Loads the details of the seance into the UI.
     */
    private void loadDetails() {
        lblTitle.setText("Détails de la Séance Code");
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

    /**
     * Handles the Edit action. Opens the InsertSeanceCode page with the seance data prefilled.
     */
    @FXML
    private void handleEdit() {
        if (parentController != null) {
            parentController.openEditCodePage(seance);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Parent controller introuvable.");
            alert.showAndWait();
        }
    }

    /**
     * Handles the Delete action.
     */
    @FXML
    private void handleDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer");
        confirm.setHeaderText("Voulez-vous vraiment supprimer cette Séance Code ?");
        confirm.setContentText("Action irréversible.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = codeService.deleteSeanceCode(seance.getId());
            if (success) {
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
