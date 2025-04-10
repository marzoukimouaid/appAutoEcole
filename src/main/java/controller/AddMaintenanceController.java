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
    @FXML private Button btnChooseInvoice;
    @FXML private Button btnSave;


    @FXML private Label dateMaintError;
    @FXML private Label typeMaintError;
    @FXML private Label descError;
    @FXML private Label costError;
    @FXML private Label invoiceError;

    private Vehicule currentVehicule;
    private final VehiculeMaintenanceService maintService = new VehiculeMaintenanceService();


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


        LocalDate maintDate = dateMaintenance.getValue();
        if (maintDate == null) {
            setFieldError(dateMaintenance, dateMaintError, "Date requise");
            valid = false;
        }


        String type = txtTypeMaintenance.getText().trim();
        if (type.isEmpty()) {
            setFieldError(txtTypeMaintenance, typeMaintError, "Type requis");
            valid = false;
        }


        String desc = txtDescription.getText().trim();
        if (desc.isEmpty()) {
            setFieldError(txtDescription, descError, "Description requise");
            valid = false;
        }


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


        if (invoiceFile == null) {
            setFieldError(btnChooseInvoice, invoiceError, "Fichier image requis");
            valid = false;
        }

        if (!valid) return;


        String uploadedUrl = ImgBBUtil.uploadImageToImgBB(invoiceFile);
        if (uploadedUrl == null) {
            setFieldError(btnChooseInvoice, invoiceError, "Échec de l'upload. Réessayez.");
            return;
        }


        VehiculeMaintenance maintenance = new VehiculeMaintenance();
        maintenance.setVehiculeId(currentVehicule.getId());
        maintenance.setDateMaintenance(maintDate);
        maintenance.setTypeMaintenance(type);
        maintenance.setDescription(desc);
        maintenance.setCost(Double.parseDouble(costText));
        maintenance.setScannedInvoiceUrl(uploadedUrl);


        boolean created = maintService.createMaintenance(maintenance);
        if (created) {

            goBackToVehiculeView();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'enregistrement de l'entretien.", ButtonType.OK);
            alert.showAndWait();
        }

    }


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


    private void setFieldError(Control field, Label errorLabel, String message) {
        if (!field.getStyleClass().contains("error")) {
            field.getStyleClass().add("error");
        }
        if (errorLabel != null) {
            errorLabel.setText(message);
        }
    }

    private void goBackToVehiculeView() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/org/example/vehiculeView.fxml"));
            javafx.scene.Parent vehiculeView = loader.load();
            VehiculeViewController vehController = loader.getController();
            vehController.initData(currentVehicule);

            javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) btnSave.getScene().getRoot().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(vehiculeView);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
