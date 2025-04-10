package controller;

import entite.Vehicule;
import entite.VehiculeDocument;
import entite.VehiculeMaintenance;
import service.VehiculeDocumentService;
import service.VehiculeMaintenanceService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;


public class VehiculeViewController {

    @FXML private StackPane rootPane;
    @FXML private Label lblTitle;
    @FXML private Label lblImmatriculation;
    @FXML private Label lblMarque;
    @FXML private Label lblDateMiseEnService;
    @FXML private Label lblKilometrage;
    @FXML private Label lblKmRestant;
    @FXML private Label lblType;

    @FXML private Button btnAddDocument;
    @FXML private Button btnAddMaintenance;

    @FXML private VBox documentsContainer;
    @FXML private VBox maintenanceContainer;

    private Vehicule currentVehicule;

    private final VehiculeDocumentService docService = new VehiculeDocumentService();
    private final VehiculeMaintenanceService maintService = new VehiculeMaintenanceService();

    @FXML
    private void initialize() {

        btnAddDocument.setOnAction(e -> handleAddDocument());
        btnAddMaintenance.setOnAction(e -> handleAddMaintenance());
    }

    public void initData(Vehicule vehicule) {
        this.currentVehicule = vehicule;

        lblImmatriculation.setText(vehicule.getImmatriculation());
        lblMarque.setText(vehicule.getMarque());
        lblDateMiseEnService.setText(String.valueOf(vehicule.getDateMiseEnService()));
        lblKilometrage.setText(String.valueOf(vehicule.getKilometrageTotal()));
        lblKmRestant.setText(String.valueOf(vehicule.getKmRestantEntretien()));
        lblType.setText(vehicule.getType() != null ? vehicule.getType().name() : "N/A");


        List<VehiculeDocument> docs = docService.getDocumentsForVehicule(vehicule.getId());
        docs.sort(Comparator.comparing(VehiculeDocument::getDateObtention, Comparator.nullsLast(Comparator.reverseOrder())));

        List<VehiculeMaintenance> maints = maintService.getMaintenanceForVehicule(vehicule.getId());
        maints.sort(Comparator.comparing(VehiculeMaintenance::getDateMaintenance, Comparator.nullsLast(Comparator.reverseOrder())));


        populateDocuments(docs);
        populateMaintenance(maints);
    }

    private void handleAddDocument() {
        System.out.println("Add Document button was clicked. Replacing everything with AddDocument.fxml...");
        showAddDocumentForm();
    }

    private void handleAddMaintenance() {
        System.out.println("Add Maintenance button was clicked. Replacing everything with AddMaintenance.fxml...");
        showAddMaintenanceForm();
    }

    private void showAddDocumentForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AddDocument.fxml"));
            Parent formRoot = loader.load();

            AddDocumentController docController = loader.getController();
            docController.initData(currentVehicule);


            rootPane.getChildren().setAll(formRoot);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAddMaintenanceForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AddMaintenance.fxml"));
            Parent formRoot = loader.load();


            AddMaintenanceController maintController = loader.getController();
            maintController.initData(currentVehicule);


            rootPane.getChildren().setAll(formRoot);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateDocuments(List<VehiculeDocument> docs) {
        documentsContainer.getChildren().clear();

        if (docs.isEmpty()) {
            Label noData = new Label("Aucun document disponible.");
            noData.getStyleClass().add("subtitle");
            documentsContainer.getChildren().add(noData);
            return;
        }

        for (VehiculeDocument doc : docs) {
            AnchorPane card = new AnchorPane();
            card.setStyle("-fx-background-color: #FFFFFF;" +
                    "-fx-padding: 14;" +
                    "-fx-border-radius: 12;" +
                    "-fx-background-radius: 12;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.3, 0, 2);");
            card.setPrefWidth(600);

            VBox innerBox = new VBox(5);

            Label lblType = new Label("Type: " + (doc.getDocType() == null ? "N/A" : doc.getDocType().name()));
            Label lblObt = new Label("Obtention: " + formatDate(doc.getDateObtention()));
            Label lblExp = new Label("Expiration: " + formatDate(doc.getDateExpiration()));
            Label lblCost = new Label("Coût: " + doc.getCost());
            Label lblScan = new Label("Scanné: " + (doc.getScannedDocUrl() == null ? "N/A" : doc.getScannedDocUrl()));

            lblType.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
            lblObt.setStyle("-fx-font-size: 12;");
            lblExp.setStyle("-fx-font-size: 12;");
            lblCost.setStyle("-fx-font-size: 12;");
            lblScan.setStyle("-fx-font-size: 12;");

            innerBox.getChildren().addAll(lblType, lblObt, lblExp, lblCost, lblScan);
            card.getChildren().add(innerBox);

            AnchorPane.setTopAnchor(innerBox, 0.0);
            AnchorPane.setBottomAnchor(innerBox, 0.0);
            AnchorPane.setLeftAnchor(innerBox, 0.0);
            AnchorPane.setRightAnchor(innerBox, 0.0);

            documentsContainer.getChildren().add(card);
        }
    }

    private void populateMaintenance(List<VehiculeMaintenance> maints) {
        maintenanceContainer.getChildren().clear();

        if (maints.isEmpty()) {
            Label noData = new Label("Aucun entretien disponible.");
            noData.getStyleClass().add("subtitle");
            maintenanceContainer.getChildren().add(noData);
            return;
        }

        for (VehiculeMaintenance m : maints) {
            AnchorPane card = new AnchorPane();
            card.setStyle("-fx-background-color: #FFFFFF;" +
                    "-fx-padding: 14;" +
                    "-fx-border-radius: 12;" +
                    "-fx-background-radius: 12;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.3, 0, 2);");
            card.setPrefWidth(600);

            VBox innerBox = new VBox(5);

            Label lblDate = new Label("Date: " + formatDate(m.getDateMaintenance()));
            Label lblType = new Label("Type: " + (m.getTypeMaintenance() == null ? "N/A" : m.getTypeMaintenance()));
            Label lblDesc = new Label("Desc: " + (m.getDescription() == null ? "" : m.getDescription()));
            Label lblCost = new Label("Coût: " + m.getCost());
            Label lblScan = new Label("Facture: " + (m.getScannedInvoiceUrl() == null ? "N/A" : m.getScannedInvoiceUrl()));

            lblDate.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
            lblType.setStyle("-fx-font-size: 12;");
            lblDesc.setStyle("-fx-font-size: 12;");
            lblCost.setStyle("-fx-font-size: 12;");
            lblScan.setStyle("-fx-font-size: 12;");

            innerBox.getChildren().addAll(lblDate, lblType, lblDesc, lblCost, lblScan);
            card.getChildren().add(innerBox);

            AnchorPane.setTopAnchor(innerBox, 0.0);
            AnchorPane.setBottomAnchor(innerBox, 0.0);
            AnchorPane.setLeftAnchor(innerBox, 0.0);
            AnchorPane.setRightAnchor(innerBox, 0.0);

            maintenanceContainer.getChildren().add(card);
        }
    }

    private String formatDate(LocalDate date) {
        return (date == null) ? "N/A" : date.toString();
    }
}
