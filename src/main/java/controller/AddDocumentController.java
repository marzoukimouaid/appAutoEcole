package controller;

import entite.Vehicule;
import entite.VehiculeDocument;
import entite.VehiculeDocument.DocType;
import service.VehiculeDocumentService;
import Utils.ImgBBUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;

public class AddDocumentController {

    @FXML private StackPane rootPane;

    @FXML private ComboBox<DocType> comboDocType;
    @FXML private DatePicker dateObtention;
    @FXML private Label dateObtError;
    @FXML private DatePicker dateExpiration;
    @FXML private Label dateExpError;

    // File pick button + docError label
    @FXML private Button btnChooseDoc;
    @FXML private Label docError;

    // Cost field + error label
    @FXML private TextField txtCost;
    @FXML private Label costError;

    @FXML private Button btnSubmit;

    private final VehiculeDocumentService documentService = new VehiculeDocumentService();
    private Vehicule currentVehicule;
    private File docFile; // the chosen image file

    @FXML
    private void initialize() {
        // Populate doc types
        comboDocType.getItems().setAll(Arrays.asList(DocType.values()));
        comboDocType.getSelectionModel().selectFirst();

        // Default date constraints
        dateObtention.setValue(LocalDate.now());
        dateExpiration.setValue(LocalDate.now().plusMonths(3));

        // Choose doc file
        btnChooseDoc.setOnAction(e -> chooseDocFile());

        // "Valider"
        btnSubmit.setOnAction(e -> handleSubmit());
    }

    public void initData(Vehicule vehicule) {
        this.currentVehicule = vehicule;
    }

    private void chooseDocFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selected = fileChooser.showOpenDialog(btnChooseDoc.getScene().getWindow());
        if (selected != null) {
            if (isImageFile(selected)) {
                docFile = selected;
                btnChooseDoc.setText(selected.getName());
                docError.setText("");
                btnChooseDoc.getStyleClass().remove("error");
            } else {
                setFieldError(btnChooseDoc, docError, "Fichier invalide (PNG/JPG/JPEG uniquement)");
            }
        }
    }

    private boolean isImageFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg");
    }

    private void handleSubmit() {
        clearAllErrors();
        boolean valid = true;

        // ============ Validate Date Obtention =============
        LocalDate obt = dateObtention.getValue();
        if (obt == null) {
            setFieldError(dateObtention, dateObtError, "Date requise");
            valid = false;
        } else {
            LocalDate today = LocalDate.now();
            if (obt.isAfter(today)) {
                setFieldError(dateObtention, dateObtError, "Doit être aujourd'hui ou passé");
                valid = false;
            }
        }

        // ============ Validate Date Expiration =============
        LocalDate exp = dateExpiration.getValue();
        if (exp == null) {
            setFieldError(dateExpiration, dateExpError, "Date requise");
            valid = false;
        } else {
            LocalDate today = LocalDate.now();
            if (!exp.isAfter(today)) {
                setFieldError(dateExpiration, dateExpError, "Doit être dans le futur");
                valid = false;
            }
        }

        // ============ Validate docFile =============
        if (docFile == null) {
            setFieldError(btnChooseDoc, docError, "Fichier image requis");
            valid = false;
        }

        // ============ Validate cost =============
        String costText = txtCost.getText().trim();
        if (costText.isEmpty()) {
            setFieldError(txtCost, costError, "Le coût est requis");
            valid = false;
        } else {
            try {
                double costValue = Double.parseDouble(costText);
                if (costValue <= 0) {
                    setFieldError(txtCost, costError, "Le coût doit être > 0");
                    valid = false;
                }
            } catch (NumberFormatException ex) {
                setFieldError(txtCost, costError, "Format invalide (ex: 100.00)");
                valid = false;
            }
        }

        if (!valid) return; // stop if any invalid input

        // If everything is valid => upload docFile to ImgBB:
        String uploadedUrl = ImgBBUtil.uploadImageToImgBB(docFile);
        if (uploadedUrl == null) {
            // The upload failed
            setFieldError(btnChooseDoc, docError, "Échec de l'upload de l'image. Réessayez.");
            return;
        }

        // Now build the doc entity
        VehiculeDocument doc = new VehiculeDocument();
        doc.setVehiculeId(currentVehicule.getId());
        doc.setDocType(comboDocType.getValue());
        doc.setDateObtention(obt);
        doc.setDateExpiration(exp);
        doc.setScannedDocUrl(uploadedUrl); // store the online link
        double finalCost = Double.parseDouble(costText);
        doc.setCost(finalCost);

        // Insert in DB
        boolean success = documentService.createDocument(doc);
        if (!success) {
            // Possibly show an alert or inline error
            System.err.println("Insertion en DB échouée");
            return;
        }

        // If you want a success notification, you can do so with a method like:
        // NotificationUtil.showNotification(rootPane, "Document ajouté avec succès!", NotificationUtil.NotificationType.SUCCESS);

        // Then go back to VehiculeView or refresh
        goBackToVehiculeView();
    }

    private void goBackToVehiculeView() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/org/example/vehiculeView.fxml"));
            javafx.scene.Parent vehiculeView = loader.load();
            VehiculeViewController vehController = loader.getController();
            vehController.initData(currentVehicule);

            javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) btnSubmit.getScene().getRoot().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(vehiculeView);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void clearAllErrors() {
        dateObtention.getStyleClass().remove("error");
        dateObtError.setText("");

        dateExpiration.getStyleClass().remove("error");
        dateExpError.setText("");

        btnChooseDoc.getStyleClass().remove("error");
        docError.setText("");

        txtCost.getStyleClass().remove("error");
        costError.setText("");
    }

    private void setFieldError(Control field, Label errorLabel, String message) {
        if (!field.getStyleClass().contains("error")) {
            field.getStyleClass().add("error");
        }
        if (errorLabel != null) {
            errorLabel.setText(message);
        }
    }
}
