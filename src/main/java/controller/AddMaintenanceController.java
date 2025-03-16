package controller;

import entite.Vehicule;
import entite.VehiculeMaintenance;
import service.VehiculeMaintenanceService;
import Utils.ImgBBUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDate;

public class AddMaintenanceController {

    @FXML private StackPane rootPane;

    @FXML private DatePicker dateMaintenance;
    @FXML private TextField txtTypeMaintenance;
    @FXML private TextField txtDescription;
    @FXML private TextField txtCost;
    @FXML private Button btnChooseInvoice;  // File uploader for invoice
    @FXML private Button btnSave;

    // Error Labels for validation
    @FXML private Label dateMaintError;
    @FXML private Label typeMaintError;
    @FXML private Label descError;
    @FXML private Label costError;
    @FXML private Label invoiceError;

    private Vehicule currentVehicule;
    private final VehiculeMaintenanceService maintService = new VehiculeMaintenanceService();

    // Holds the selected file for the invoice
    private File invoiceFile;

    public void initData(Vehicule vehicule) {
        this.currentVehicule = vehicule;
    }

    @FXML
    private void initialize() {
        btnSave.setOnAction(e -> onSave());
        btnChooseInvoice.setOnAction(e -> chooseInvoiceFile());
    }

    private void chooseInvoiceFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image de la facture");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selected = fileChooser.showOpenDialog(btnChooseInvoice.getScene().getWindow());
        if (selected != null) {
            if (isImageFile(selected)) {
                invoiceFile = selected;
                btnChooseInvoice.setText(selected.getName());
                invoiceError.setText("");
                btnChooseInvoice.getStyleClass().remove("error");
            } else {
                setFieldError(btnChooseInvoice, invoiceError, "Fichier invalide (PNG/JPG/JPEG uniquement)");
            }
        }
    }

    private boolean isImageFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg");
    }

    private void onSave() {
        clearAllErrors();
        boolean valid = true;

        // Validate Date Maintenance: must be non-null
        LocalDate maintDate = dateMaintenance.getValue();
        if (maintDate == null) {
            setFieldError(dateMaintenance, dateMaintError, "Date requise");
            valid = false;
        }

        // Validate Type Maintenance: required
        String type = txtTypeMaintenance.getText().trim();
        if (type.isEmpty()) {
            setFieldError(txtTypeMaintenance, typeMaintError, "Type requis");
            valid = false;
        }

        // Validate Description: required
        String desc = txtDescription.getText().trim();
        if (desc.isEmpty()) {
            setFieldError(txtDescription, descError, "Description requise");
            valid = false;
        }

        // Validate Cost: must be provided, numeric, and > 0
        String costText = txtCost.getText().trim();
        if (costText.isEmpty()) {
            setFieldError(txtCost, costError, "Coût requis");
            valid = false;
        } else {
            try {
                double costVal = Double.parseDouble(costText);
                if (costVal <= 0) {
                    setFieldError(txtCost, costError, "Coût doit être > 0");
                    valid = false;
                }
            } catch (NumberFormatException ex) {
                setFieldError(txtCost, costError, "Format invalide");
                valid = false;
            }
        }

        // Validate invoice file: must be selected
        if (invoiceFile == null) {
            setFieldError(btnChooseInvoice, invoiceError, "Fichier image requis");
            valid = false;
        }

        if (!valid) return; // Stop if any validations failed

        // Upload the invoice file using ImgBBUtil (ensure this utility is properly implemented)
        String uploadedUrl = ImgBBUtil.uploadImageToImgBB(invoiceFile);
        if (uploadedUrl == null) {
            setFieldError(btnChooseInvoice, invoiceError, "Échec de l'upload. Réessayez.");
            return;
        }

        // Build the maintenance entity
        VehiculeMaintenance maintenance = new VehiculeMaintenance();
        maintenance.setVehiculeId(currentVehicule.getId());
        maintenance.setDateMaintenance(maintDate);
        maintenance.setTypeMaintenance(type);
        maintenance.setDescription(desc);
        maintenance.setCost(Double.parseDouble(costText));
        maintenance.setScannedInvoiceUrl(uploadedUrl);

        // Save maintenance record via the service layer
        boolean created = maintService.createMaintenance(maintenance);
        if (created) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Entretien enregistré avec succès!", ButtonType.OK);
            alert.showAndWait();
            // Optionally navigate back or refresh the view here
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'enregistrement de l'entretien.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    // Helper method to clear all previous error styles and messages
    private void clearAllErrors() {
        dateMaintenance.getStyleClass().remove("error");
        dateMaintError.setText("");

        txtTypeMaintenance.getStyleClass().remove("error");
        typeMaintError.setText("");

        txtDescription.getStyleClass().remove("error");
        descError.setText("");

        txtCost.getStyleClass().remove("error");
        costError.setText("");

        btnChooseInvoice.getStyleClass().remove("error");
        invoiceError.setText("");
    }

    // Helper method to set error style and message for a control
    private void setFieldError(Control field, Label errorLabel, String message) {
        if (!field.getStyleClass().contains("error")) {
            field.getStyleClass().add("error");
        }
        if (errorLabel != null) {
            errorLabel.setText(message);
        }
    }
}
