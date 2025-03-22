package controller;

import entite.Vehicule;
import entite.Vehicule.VehicleType;
import service.VehiculeService;
import Utils.NotificationUtil;
import Utils.NotificationUtil.NotificationType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.time.LocalDate;
import java.util.regex.Pattern;

public class AddVehiculeController {

    @FXML private StackPane rootPane; // Injected from FXML root

    @FXML private TextField immatriculationField;
    @FXML private TextField marqueField;
    @FXML private DatePicker dateMiseEnServicePicker;
    @FXML private TextField kilometrageTotalField;
    @FXML private TextField kmRestantField;
    @FXML private ComboBox<String> vehiculeTypeComboBox;
    @FXML private Button btnSubmit;

    // Error labels
    @FXML private Label immatriculationError;
    @FXML private Label marqueError;
    @FXML private Label dateMiseEnServiceError;
    @FXML private Label kilometrageTotalError;
    @FXML private Label kmRestantError;
    @FXML private Label vehiculeTypeError;

    private final VehiculeService vehiculeService = new VehiculeService();

    // Fields for edit mode support
    private boolean isEditMode = false;
    private Vehicule editingVehicule;

    @FXML
    public void initialize() {
        vehiculeTypeComboBox.getItems().addAll("MOTO", "VOITURE", "CAMION");
        btnSubmit.setOnAction(this::handleSubmit);
    }

    /**
     * Initializes the form for editing an existing vehicle.
     * Preloads the vehicle's existing data into the inputs.
     *
     * @param vehicule The vehicle record to edit.
     */
    public void initData(Vehicule vehicule) {
        isEditMode = true;
        editingVehicule = vehicule;

        immatriculationField.setText(vehicule.getImmatriculation());
        marqueField.setText(vehicule.getMarque());
        dateMiseEnServicePicker.setValue(vehicule.getDateMiseEnService());
        kilometrageTotalField.setText(String.valueOf(vehicule.getKilometrageTotal()));
        kmRestantField.setText(String.valueOf(vehicule.getKmRestantEntretien()));
        vehiculeTypeComboBox.setValue(vehicule.getType().name());

        btnSubmit.setText("Mettre à jour Véhicule");
    }

    private void handleSubmit(ActionEvent event) {
        clearAllErrors();
        boolean valid = true;

        // Validate immatriculation: must match pattern xxxTunisxxxx (Tunis case insensitive)
        String immatriculation = immatriculationField.getText().trim();
        String immatriculationPattern = "^[0-9]{3}[Tt][Uu][Nn][Ii][Ss][0-9]{4}$";
        if (immatriculation.isEmpty()) {
            setFieldError(immatriculationField, immatriculationError, "Immatriculation required");
            valid = false;
        } else if (!Pattern.matches(immatriculationPattern, immatriculation)) {
            setFieldError(immatriculationField, immatriculationError, "Invalid format. Expected: xxxTunisxxxx");
            valid = false;
        }
        // In create mode, check if immatriculation exists
        if (!isEditMode && vehiculeService.immatriculationExists(immatriculation)) {
            setFieldError(immatriculationField, immatriculationError, "Ce numéro d'immatriculation existe déjà");
            valid = false;
        }

        // Validate marque
        String marque = marqueField.getText().trim();
        if (marque.isEmpty()) {
            setFieldError(marqueField, marqueError, "Marque required");
            valid = false;
        }
        // Validate date de mise en service
        LocalDate dateMiseEnService = dateMiseEnServicePicker.getValue();
        if (dateMiseEnService == null) {
            setFieldError(dateMiseEnServicePicker, dateMiseEnServiceError, "Date de mise en service required");
            valid = false;
        }
        // Validate kilometrage total
        String kmTotalStr = kilometrageTotalField.getText().trim();
        int kmTotal = 0;
        if (kmTotalStr.isEmpty() || !kmTotalStr.matches("\\d+")) {
            setFieldError(kilometrageTotalField, kilometrageTotalError, "Valid kilométrage required");
            valid = false;
        } else {
            kmTotal = Integer.parseInt(kmTotalStr);
        }
        // Validate km restant
        String kmRestantStr = kmRestantField.getText().trim();
        int kmRestant = 0;
        if (kmRestantStr.isEmpty() || !kmRestantStr.matches("\\d+")) {
            setFieldError(kmRestantField, kmRestantError, "Valid km restant required");
            valid = false;
        } else {
            kmRestant = Integer.parseInt(kmRestantStr);
        }
        // Validate vehicule type
        if (vehiculeTypeComboBox.getValue() == null) {
            setFieldError(vehiculeTypeComboBox, vehiculeTypeError, "Select vehicule type");
            valid = false;
        }

        if (!valid) {
            return;
        }

        // If in edit mode, update the existing record; otherwise, create a new one.
        if (isEditMode) {
            editingVehicule.setImmatriculation(immatriculation);
            editingVehicule.setMarque(marque);
            editingVehicule.setDateMiseEnService(dateMiseEnService);
            editingVehicule.setKilometrageTotal(kmTotal);
            editingVehicule.setKmRestantEntretien(kmRestant);
            try {
                editingVehicule.setType(VehicleType.valueOf(vehiculeTypeComboBox.getValue()));
            } catch (IllegalArgumentException e) {
                setFieldError(vehiculeTypeComboBox, vehiculeTypeError, "Invalid vehicle type");
                return;
            }
            boolean updated = vehiculeService.updateVehicule(editingVehicule);
            if (updated) {
                NotificationUtil.showNotification(rootPane, "Véhicule mis à jour avec succès!", NotificationType.SUCCESS);
                clearForm();
            } else {
                showInlineError("Error updating vehicle");
            }
        } else {
            Vehicule vehicule = new Vehicule();
            vehicule.setImmatriculation(immatriculation);
            vehicule.setMarque(marque);
            vehicule.setDateMiseEnService(dateMiseEnService);
            vehicule.setKilometrageTotal(kmTotal);
            vehicule.setKmRestantEntretien(kmRestant);
            try {
                vehicule.setType(VehicleType.valueOf(vehiculeTypeComboBox.getValue()));
            } catch (IllegalArgumentException e) {
                setFieldError(vehiculeTypeComboBox, vehiculeTypeError, "Invalid vehicle type");
                return;
            }
            boolean created = vehiculeService.createVehicule(vehicule);
            if (created) {
                NotificationUtil.showNotification(rootPane, "Véhicule ajouté avec succès!", NotificationType.SUCCESS);
                clearForm();
            } else {
                showInlineError("Error creating vehicle");
            }
        }
    }

    private void clearForm() {
        immatriculationField.clear();
        marqueField.clear();
        dateMiseEnServicePicker.setValue(null);
        kilometrageTotalField.clear();
        kmRestantField.clear();
        vehiculeTypeComboBox.getSelectionModel().clearSelection();
        clearAllErrors();
        // Reset edit mode if applicable.
        isEditMode = false;
        editingVehicule = null;
        btnSubmit.setText("Ajouter Véhicule");
    }

    private void clearAllErrors() {
        immatriculationField.getStyleClass().remove("error");
        marqueField.getStyleClass().remove("error");
        dateMiseEnServicePicker.getStyleClass().remove("error");
        kilometrageTotalField.getStyleClass().remove("error");
        kmRestantField.getStyleClass().remove("error");
        vehiculeTypeComboBox.getStyleClass().remove("error");

        immatriculationError.setText("");
        marqueError.setText("");
        dateMiseEnServiceError.setText("");
        kilometrageTotalError.setText("");
        kmRestantError.setText("");
        vehiculeTypeError.setText("");
    }

    private void setFieldError(Control field, Label errorLabel, String message) {
        if (!field.getStyleClass().contains("error")) {
            field.getStyleClass().add("error");
        }
        if (errorLabel != null) {
            errorLabel.setText(message);
        }
    }

    private void showInlineError(String message) {
        System.err.println(message);
    }
}
