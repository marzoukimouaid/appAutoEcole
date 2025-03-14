package controller;

import entite.Vehicule;
import entite.VehiculeMaintenance;
import service.VehiculeMaintenanceService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

public class AddMaintenanceController {

    @FXML private DatePicker dateMaintenance;
    @FXML private TextField txtTypeMaintenance;
    @FXML private TextField txtDescription;
    @FXML private TextField txtCost;
    @FXML private TextField txtScannedInvoice;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private Vehicule currentVehicule;
    private final VehiculeMaintenanceService maintService = new VehiculeMaintenanceService();

    public void initData(Vehicule vehicule) {
        this.currentVehicule = vehicule;
    }

    @FXML
    private void initialize() {
        btnSave.setOnAction(e -> onSave());
        btnCancel.setOnAction(e -> onCancel());
    }

    private void onSave() {
        System.out.println("Save Maintenance clicked!");
        VehiculeMaintenance m = new VehiculeMaintenance();
        m.setVehiculeId(currentVehicule.getId());

        LocalDate maintDate = dateMaintenance.getValue();
        m.setDateMaintenance(maintDate);

        m.setTypeMaintenance(txtTypeMaintenance.getText().trim());
        m.setDescription(txtDescription.getText().trim());
        try {
            double costVal = Double.parseDouble(txtCost.getText().trim());
            m.setCost(costVal);
        } catch (NumberFormatException ex) {
            m.setCost(0.0);
        }
        m.setScannedInvoiceUrl(txtScannedInvoice.getText().trim());

        boolean created = maintService.createMaintenance(m);
        if (created) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Entretien enregistré avec succès!", ButtonType.OK);
            alert.showAndWait();
            // Possibly navigate back to VehiculeView
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'enregistrement de l'entretien.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void onCancel() {
        System.out.println("Cancel Maintenance clicked!");
        // Possibly go back to the VehiculeView
    }
}
