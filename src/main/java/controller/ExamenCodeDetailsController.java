package controller;

import entite.ExamenCode;
import entite.Profile;
import entite.User;
import Utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import service.ExamenCodeService;
import service.ProfileService;
import service.UserService;

import java.time.LocalDate;
import java.util.Optional;


public class ExamenCodeDetailsController {

    @FXML private Label lblTitle;
    @FXML private Label lblDate;
    @FXML private Label lblStatus;
    @FXML private Label lblCandidate;
    @FXML private Label lblMoniteur;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnMarkPassed;

    private ExamenCode examenCode;


    private SecretaireInscriptionExamenController parentController;

    private final ExamenCodeService examService = new ExamenCodeService();
    private final ProfileService profileService = new ProfileService();
    private final UserService userService = new UserService();

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
    }

    public void setParentController(SecretaireInscriptionExamenController parentController) {
        this.parentController = parentController;
    }

    public void setExamenCode(ExamenCode examenCode) {
        this.examenCode = examenCode;
        loadDetails();
    }

    private void loadDetails() {
        lblTitle.setText("Détails de l'Examen Code");
        lblDate.setText("Date/Heure: " + examenCode.getExamDatetime());
        lblStatus.setText("Statut: " + examenCode.getStatus().name());

        String candidateFullName = profileService.getProfileByUserId(examenCode.getCandidatId())
                .map(p -> p.getNom() + " " + p.getPrenom())
                .orElse("N/A");
        String moniteurFullName = profileService.getProfileByUserId(examenCode.getMoniteurId())
                .map(p -> p.getNom() + " " + p.getPrenom())
                .orElse("N/A");

        lblCandidate.setText("Candidat: " + candidateFullName);
        lblMoniteur.setText("Moniteur: " + moniteurFullName);
    }

    @FXML
    private void handleEdit() {

        if (parentController != null) {
            parentController.openEditExamenCodePage(examenCode);
        }
    }

    @FXML
    private void handleDelete() {

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmer la suppression");
        confirmAlert.setHeaderText("Voulez-vous vraiment supprimer cet examen ?");
        confirmAlert.setContentText("Cette action est irréversible.");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = examService.deleteExamenCode(examenCode.getId());
            if (deleted) {
                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("Suppression réussie");
                infoAlert.setHeaderText(null);
                infoAlert.setContentText("L'examen a été supprimé avec succès.");
                infoAlert.showAndWait();
                if (parentController != null) {
                    parentController.returnToExamInscriptionsPage();
                }
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur");
                errorAlert.setHeaderText("Suppression échouée");
                errorAlert.setContentText("L'examen n'a pas pu être supprimé.");
                errorAlert.showAndWait();
            }
        }
    }

    
    @FXML
    private void handleMarkPassed() {

        if (examenCode.getPaiementStatus() != ExamenCode.PaymentStatus.PAID) {
            new Alert(Alert.AlertType.WARNING,
                    "Impossible de marquer l'examen comme réussi: il n'est pas payé.")
                    .showAndWait();
            return;
        }

        LocalDate examDate = examenCode.getExamDatetime().toLocalDate();
        if (!examDate.equals(LocalDate.now())) {
            new Alert(Alert.AlertType.WARNING,
                    "Impossible de marquer l'examen comme réussi: la date d'examen n'est pas aujourd'hui.")
                    .showAndWait();
            return;
        }


        examenCode.setStatus(ExamenCode.ExamStatus.PASSED);
        boolean updated = examService.updateExamenCode(examenCode);
        if (updated) {
            lblStatus.setText("Statut: PASSED");
            new Alert(Alert.AlertType.INFORMATION, "Examen marqué comme Réussi.").showAndWait();
        } else {
            new Alert(Alert.AlertType.ERROR, "Impossible de mettre à jour l'examen.").showAndWait();
        }
    }
}
