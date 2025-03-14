package controller;

import entite.Vehicule;
import entite.VehiculeDocument;
import entite.VehiculeMaintenance;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import service.VehiculeDocumentService;
import service.VehiculeMaintenanceService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class VehiculeViewController {

    @FXML
    private StackPane rootPane;

    // Vehicle detail labels
    @FXML private Label lblTitle;
    @FXML private Label lblImmatriculation;
    @FXML private Label lblMarque;
    @FXML private Label lblDateMiseEnService;
    @FXML private Label lblKilometrage;
    @FXML private Label lblKmRestant;
    @FXML private Label lblType;

    // Buttons
    @FXML private Button btnAddDocument;
    @FXML private Button btnAddMaintenance;

    // TableViews
    @FXML private TableView<VehiculeDocument> tableDocuments;
    @FXML private TableView<VehiculeMaintenance> tableMaintenance;

    // Container for embedded forms
    @FXML private AnchorPane docFormContainer;

    private Vehicule currentVehicule;

    private final VehiculeDocumentService docService = new VehiculeDocumentService();
    private final VehiculeMaintenanceService maintService = new VehiculeMaintenanceService();

    @FXML
    private void initialize() {
        // Placeholders if no data
        tableDocuments.setPlaceholder(new Label("No documents available"));
        tableMaintenance.setPlaceholder(new Label("No maintenance records available"));

        // Button actions
        btnAddDocument.setOnAction(e -> showAddDocumentForm());
        btnAddMaintenance.setOnAction(e -> showAddMaintenanceForm());

        // ========== Documents Table ==========
        TableColumn<VehiculeDocument, String> colDocType = new TableColumn<>("Type");
        colDocType.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDocType() != null) {
                return new SimpleObjectProperty<>(cellData.getValue().getDocType().name());
            } else {
                return new SimpleObjectProperty<>("N/A");
            }
        });
        colDocType.setPrefWidth(80);

        TableColumn<VehiculeDocument, String> colDateObt = new TableColumn<>("Date Obtention");
        colDateObt.setCellValueFactory(cellData -> {
            LocalDate dt = cellData.getValue().getDateObtention();
            return new SimpleObjectProperty<>(dt != null ? dt.toString() : "N/A");
        });
        colDateObt.setPrefWidth(110);

        TableColumn<VehiculeDocument, String> colDateExp = new TableColumn<>("Date Expiration");
        colDateExp.setCellValueFactory(cellData -> {
            LocalDate dt = cellData.getValue().getDateExpiration();
            return new SimpleObjectProperty<>(dt != null ? dt.toString() : "N/A");
        });
        colDateExp.setPrefWidth(110);

        TableColumn<VehiculeDocument, Double> colCost = new TableColumn<>("Coût");
        colCost.setCellValueFactory(new PropertyValueFactory<>("cost"));
        colCost.setPrefWidth(70);

        TableColumn<VehiculeDocument, String> colScannedUrl = new TableColumn<>("Lien Scanné");
        colScannedUrl.setCellValueFactory(new PropertyValueFactory<>("scannedDocUrl"));
        colScannedUrl.setPrefWidth(200);

        tableDocuments.getColumns().setAll(colDocType, colDateObt, colDateExp, colCost, colScannedUrl);

        // ========== Maintenance Table ==========
        TableColumn<VehiculeMaintenance, String> colMaintDate = new TableColumn<>("Date Entretien");
        colMaintDate.setPrefWidth(110);
        colMaintDate.setCellValueFactory(cellData -> {
            LocalDate dt = cellData.getValue().getDateMaintenance();
            return new SimpleObjectProperty<>(dt != null ? dt.toString() : "N/A");
        });

        TableColumn<VehiculeMaintenance, String> colMaintType = new TableColumn<>("Type");
        colMaintType.setPrefWidth(80);
        colMaintType.setCellValueFactory(new PropertyValueFactory<>("typeMaintenance"));

        TableColumn<VehiculeMaintenance, String> colMaintDesc = new TableColumn<>("Description");
        colMaintDesc.setPrefWidth(140);
        colMaintDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<VehiculeMaintenance, Double> colMaintCost = new TableColumn<>("Coût");
        colMaintCost.setPrefWidth(70);
        colMaintCost.setCellValueFactory(new PropertyValueFactory<>("cost"));

        TableColumn<VehiculeMaintenance, String> colMaintScan = new TableColumn<>("Facture Scannée");
        colMaintScan.setPrefWidth(180);
        colMaintScan.setCellValueFactory(new PropertyValueFactory<>("scannedInvoiceUrl"));

        tableMaintenance.getColumns().setAll(colMaintDate, colMaintType, colMaintDesc, colMaintCost, colMaintScan);
    }

    /**
     * Called by the previous controller after loading this view.
     * Sets vehicle details and loads the related documents and maintenance records.
     */
    public void initData(Vehicule vehicule) {
        this.currentVehicule = vehicule;

        // Display the main vehicule info
        lblImmatriculation.setText(vehicule.getImmatriculation());
        lblMarque.setText(vehicule.getMarque());
        lblDateMiseEnService.setText(String.valueOf(vehicule.getDateMiseEnService()));
        lblKilometrage.setText(String.valueOf(vehicule.getKilometrageTotal()));
        lblKmRestant.setText(String.valueOf(vehicule.getKmRestantEntretien()));
        lblType.setText(vehicule.getType() != null ? vehicule.getType().name() : "N/A");

        // Fetch documents for this vehicle
        List<VehiculeDocument> docs = docService.getDocumentsForVehicule(vehicule.getId());
        docs.sort(Comparator.comparing(VehiculeDocument::getDateObtention,
                Comparator.nullsLast(Comparator.reverseOrder())));
        tableDocuments.setItems(FXCollections.observableArrayList(docs));
        tableDocuments.refresh();

        // Fetch maintenance records
        List<VehiculeMaintenance> maintList = maintService.getMaintenanceForVehicule(vehicule.getId());
        maintList.sort(Comparator.comparing(VehiculeMaintenance::getDateMaintenance,
                Comparator.nullsLast(Comparator.reverseOrder())));
        tableMaintenance.setItems(FXCollections.observableArrayList(maintList));
        tableMaintenance.refresh();
    }

    private void showAddDocumentForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AddDocument.fxml"));
            Parent formRoot = loader.load();
            AddDocumentController docController = loader.getController();
            docController.initData(currentVehicule);

            docFormContainer.getChildren().clear();
            docFormContainer.getChildren().add(formRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAddMaintenanceForm() {
        System.out.println("TODO: Show AddMaintenance form or logic...");
    }
}
