package controller;

import entite.SeanceCode;
import entite.Profile;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import service.SeanceCodeService;
import service.ProfileService;
import service.UserService;

import java.util.Optional;

public class SeanceCodeDetailsController {

    @FXML private Label lblTitle;
    @FXML private Label lblDate;
    @FXML private Label lblCandidate;
    @FXML private Label lblMoniteur;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private SeanceCode seance;
    private final SeanceCodeService codeService = new SeanceCodeService();
    private final ProfileService profileService = new ProfileService();
    private final UserService userService = new UserService();

    // >>> ADD THIS: reference to the parent controller <<<
    private SecretaireSeancesController parentController;

    // >>> Provide a setter so the parent can inject itself <<<
    public void setParentController(SecretaireSeancesController parentController) {
        this.parentController = parentController;
    }

    public void setSeance(SeanceCode seance) {
        this.seance = seance;
        loadDetails();
    }

    private void loadDetails() {
        lblTitle.setText("Détails de la Séance Code");
        lblDate.setText("Date/Heure: " + seance.getSessionDatetime().toString());

        String candidateFullName = profileService.getProfileByUserId(seance.getCandidatId())
                .map(Profile::getFullName)    // If you have getFullName, or do p -> p.getNom()+" "+p.getPrenom()
                .orElse("N/A");

        String moniteurFullName = profileService.getProfileByUserId(seance.getMoniteurId())
                .map(Profile::getFullName)
                .orElse("N/A");

        lblCandidate.setText("Candidat: " + candidateFullName);
        lblMoniteur.setText("Moniteur: " + moniteurFullName);
    }

    @FXML
    private void handleEdit() {
        // >>> Instead of rewriting the center from here,
        // >>> just tell the parent to open the edit page:
        if (parentController != null) {
            parentController.openEditCodePage(seance);
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
            boolean deleted = codeService.deleteSeanceCode(seance.getId());
            if (deleted) {
                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("Suppression réussie");
                infoAlert.setHeaderText(null);
                infoAlert.setContentText("La séance a été supprimée avec succès.");
                infoAlert.showAndWait();

                // When deleted, we can ask parent to reload the list
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
