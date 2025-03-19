package controller;

import entite.SeanceCode;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import service.SeanceCodeService;
import javafx.scene.control.Alert.AlertType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller for InsertSeanceCode.fxml.
 * Creates a new SeanceCode and then calls the parent's returnToSeancesPage().
 */
public class InsertSeanceCodeController {

    @FXML
    private TextField txtCandidatId;

    @FXML
    private TextField txtMoniteurId;

    @FXML
    private TextField txtSessionDatetime;

    private SecretaireSeancesController parentController;
    private final SeanceCodeService codeService = new SeanceCodeService();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void setParentController(SecretaireSeancesController parent) {
        this.parentController = parent;
    }

    @FXML
    private void handleSubmit() {
        try {
            int candidatId = Integer.parseInt(txtCandidatId.getText().trim());
            int moniteurId = Integer.parseInt(txtMoniteurId.getText().trim());
            LocalDateTime sessionDatetime = LocalDateTime.parse(txtSessionDatetime.getText().trim(), dtf);

            SeanceCode seance = new SeanceCode(candidatId, moniteurId, sessionDatetime);
            boolean created = codeService.createSeanceCode(seance);

            if (created) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Séance Code créée avec succès !");
                alert.showAndWait();

                parentController.returnToSeancesPage();
            } else {
                showError("Erreur", "Impossible de créer la séance code.");
            }
        } catch (Exception e) {
            showError("Données invalides", "Veuillez vérifier vos champs.\n" + e.getMessage());
        }
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
