package controller;

import entite.ExamenConduit;
import entite.Profile;
import entite.User;
import Utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import service.ExamenConduitService;
import service.ProfileService;

import java.time.LocalDate;
import java.util.Optional;

/**
 * ExamenConduitDetailsController
 *
 * - candidate => read only
 * - secretaire => edit + delete
 * - moniteur => "Mark as Passed" only if exam is paid AND exam date is today
 */
public class ExamenConduitDetailsController {

    @FXML private Label lblTitle;
    @FXML private Label lblDate;
    @FXML private Label lblStatus;
    @FXML private Label lblLocation;
    @FXML private Label lblCandidate;
    @FXML private Label lblMoniteur;
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
                    // read only
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

    public void setExamenConduit(ExamenConduit examenConduit) {
        this.examenConduit = examenConduit;
        loadDetails();
    }

    private void loadDetails() {
        lblTitle.setText("Détails de l'Examen Conduit");
        lblDate.setText("Date/Heure: " + examenConduit.getExamDatetime());
        lblStatus.setText("Statut: " + examenConduit.getStatus().name());
        lblLocation.setText("Lieu: (" + examenConduit.getLatitude() + ", " + examenConduit.getLongitude() + ")");

        String candidateName = profileService.getProfileByUserId(examenConduit.getCandidatId())
                .map(p -> p.getNom() + " " + p.getPrenom())
                .orElse("N/A");
        String moniteurName = profileService.getProfileByUserId(examenConduit.getMoniteurId())
                .map(p -> p.getNom() + " " + p.getPrenom())
                .orElse("N/A");

        lblCandidate.setText("Candidat: " + candidateName);
        lblMoniteur.setText("Moniteur: " + moniteurName);
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

    /**
     * moniteur: mark as passed only if exam is paid and exam date is today
     */
    @FXML
    private void handleMarkPassed() {
        // must be paid
        if (examenConduit.getPaiementStatus() != ExamenConduit.PaymentStatus.PAID) {
            new Alert(Alert.AlertType.WARNING,
                    "Impossible de marquer l'examen comme réussi: il n'est pas payé.")
                    .showAndWait();
            return;
        }
        // must be the same day
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
