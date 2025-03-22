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

/**
 * SeanceCodeDetailsController
 *
 *  - candidate => read only
 *  - secretaire => can edit & delete
 *  - moniteur => read only
 *
 * setParentController(...) to let secretarial parent handle navigation
 */
public class SeanceCodeDetailsController {

    @FXML private Label lblTitle;
    @FXML private Label lblDate;
    @FXML private Label lblCandidate;
    @FXML private Label lblMoniteur;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private SeanceCode seance;

    // Possibly the same parent as seances: e.g. SecretaireSeancesController
    private SecretaireSeancesController parentController;

    private final SeanceCodeService codeService = new SeanceCodeService();
    private final ProfileService profileService = new ProfileService();

    @FXML
    public void initialize() {
        // Hide all by default
        btnEdit.setVisible(false);
        btnDelete.setVisible(false);

        // role-based logic
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
                    // read only
                    break;
            }
        }
    }

    public void setParentController(SecretaireSeancesController parentController) {
        this.parentController = parentController;
    }

    public void setSeance(SeanceCode seance) {
        this.seance = seance;
        loadDetails();
    }

    private void loadDetails() {
        lblTitle.setText("Détails de la Séance Code");
        lblDate.setText("Date/Heure: " + seance.getSessionDatetime());

        String candidateName = profileService.getProfileByUserId(seance.getCandidatId())
                .map(p-> p.getNom() + " " + p.getPrenom())
                .orElse("N/A");
        String moniteurName = profileService.getProfileByUserId(seance.getMoniteurId())
                .map(p-> p.getNom() + " " + p.getPrenom())
                .orElse("N/A");

        lblCandidate.setText("Candidat: " + candidateName);
        lblMoniteur.setText("Moniteur: " + moniteurName);
    }

    @FXML
    private void handleEdit() {
        if (parentController != null) {
            // e.g. parentController.openEditSeanceCodePage(seance);
            System.out.println("handleEdit SeanceCode (secretaire)...");
        }
    }

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
                new Alert(Alert.AlertType.INFORMATION,"Séance supprimée.").showAndWait();
                if (parentController != null) {
                    // parentController.returnToSeancesPage();
                }
            } else {
                new Alert(Alert.AlertType.ERROR,"Impossible de supprimer la séance.").showAndWait();
            }
        }
    }
}
