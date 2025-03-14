package controller;

import entite.Vehicule;
import entite.Vehicule.VehicleType;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import service.VehiculeService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VehiculeController {

    @FXML
    private StackPane rootPane;
    @FXML
    private Label totalVehiculesLabel;
    @FXML
    private Button btnAddVehicule;
    @FXML
    private Button btnSearch;
    @FXML
    private TextField searchField;
    @FXML
    private Label searchError; // Inline error label for search input

    @FXML
    private TableView<Vehicule> vehiculesTable;
    @FXML
    private TableColumn<Vehicule, String> colImmatriculation;
    @FXML
    private TableColumn<Vehicule, String> colMarque;
    @FXML
    private TableColumn<Vehicule, String> colDateMiseEnService;
    @FXML
    private TableColumn<Vehicule, Integer> colKilometrageTotal;
    @FXML
    private TableColumn<Vehicule, Integer> colKmRestant;
    @FXML
    private TableColumn<Vehicule, String> colType;

    private ObservableList<Vehicule> vehiculeList;
    private ObservableList<Object> searchResultsData;

    private final VehiculeService vehiculeService = new VehiculeService();

    @FXML
    public void initialize() {
        refreshVehicules();
        btnAddVehicule.setOnAction(e -> openAddVehiculePage());
        btnSearch.setOnAction(e -> performSearch());
    }

    /**
     * Loads all vehicles from the DB and sets them in the main table,
     * and displays the total count.
     */
    private void refreshVehicules() {
        List<Vehicule> allVehicules = vehiculeService.getAllVehicules();
        vehiculeList = FXCollections.observableArrayList(allVehicules);
        vehiculesTable.setItems(vehiculeList);
        totalVehiculesLabel.setText(String.valueOf(vehiculeList.size()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        colImmatriculation.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getImmatriculation())
        );
        colMarque.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getMarque())
        );
        colDateMiseEnService.setCellValueFactory(cellData -> {
            String dateStr = cellData.getValue().getDateMiseEnService().format(formatter);
            return new SimpleObjectProperty<>(dateStr);
        });
        colKilometrageTotal.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getKilometrageTotal())
        );
        colKmRestant.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getKmRestantEntretien())
        );
        colType.setCellValueFactory(cellData -> {
            VehicleType vt = cellData.getValue().getType();
            return new SimpleObjectProperty<>(vt != null ? vt.name() : "N/A");
        });
    }

    /**
     * Validates the search input (which must not be empty) and filters vehicles based on the immatriculation or marque.
     * If the search field is empty, the border is made red and an error label is shown.
     */
    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();

        // Validate that the search field is not empty.
        if (query.isEmpty()) {
            if (!searchField.getStyleClass().contains("error")) {
                searchField.getStyleClass().add("error");
            }
            searchError.setText("Veuillez saisir l'immatriculation ou la marque.");
            return;
        } else {
            // Remove error styling if present.
            searchField.getStyleClass().remove("error");
            searchError.setText("");
        }

        List<Vehicule> allVehicules = vehiculeService.getAllVehicules();
        List<Vehicule> filtered = allVehicules.stream()
                .filter(v -> {
                    String immat = v.getImmatriculation().toLowerCase();
                    String marque = v.getMarque().toLowerCase();
                    // Search by immatriculation OR marque.
                    return immat.contains(query) || marque.contains(query);
                })
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            Label noResults = new Label("Aucun véhicule ne correspond à la recherche.");
            noResults.getStyleClass().add("section-title");
            rootPane.getChildren().setAll(noResults);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/SearchResults.fxml"));
            Parent searchResultsPage = loader.load();

            // Use the existing SearchResultsController.
            SearchResultsController controller = loader.getController();
            controller.setTitle("Résultats de recherche (Véhicules)");
            controller.setMessage("Trouvé " + filtered.size() + " véhicule(s).");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            TableColumn<Object, String> colImmat = new TableColumn<>("Immatriculation");
            colImmat.setPrefWidth(120);
            colImmat.setCellValueFactory(cellData -> {
                Vehicule v = (Vehicule) cellData.getValue();
                return new SimpleObjectProperty<>(v.getImmatriculation());
            });

            TableColumn<Object, String> colMarque = new TableColumn<>("Marque");
            colMarque.setPrefWidth(100);
            colMarque.setCellValueFactory(cellData -> {
                Vehicule v = (Vehicule) cellData.getValue();
                return new SimpleObjectProperty<>(v.getMarque());
            });

            TableColumn<Object, String> colDateService = new TableColumn<>("Mise en service");
            colDateService.setPrefWidth(120);
            colDateService.setCellValueFactory(cellData -> {
                Vehicule v = (Vehicule) cellData.getValue();
                String dateStr = v.getDateMiseEnService().format(formatter);
                return new SimpleObjectProperty<>(dateStr);
            });

            TableColumn<Object, Integer> colKmTotal = new TableColumn<>("Kilométrage");
            colKmTotal.setPrefWidth(100);
            colKmTotal.setCellValueFactory(cellData -> {
                Vehicule v = (Vehicule) cellData.getValue();
                return new SimpleObjectProperty<>(v.getKilometrageTotal());
            });

            TableColumn<Object, Integer> colKmRest = new TableColumn<>("Km Restant");
            colKmRest.setPrefWidth(100);
            colKmRest.setCellValueFactory(cellData -> {
                Vehicule v = (Vehicule) cellData.getValue();
                return new SimpleObjectProperty<>(v.getKmRestantEntretien());
            });

            TableColumn<Object, String> colType = new TableColumn<>("Type");
            colType.setPrefWidth(80);
            colType.setCellValueFactory(cellData -> {
                Vehicule v = (Vehicule) cellData.getValue();
                return new SimpleObjectProperty<>(v.getType() != null ? v.getType().name() : "N/A");
            });

            // INSPECT button column.
            TableColumn<Object, Void> colInspect = new TableColumn<>("Inspecter");
            colInspect.setPrefWidth(90);
            colInspect.setCellFactory(param -> new TableCell<Object, Void>() {
                private final Button btn = new Button("Inspect");
                {
                    btn.getStyleClass().add("inspect-button");
                    btn.setOnAction(event -> {
                        Vehicule v = (Vehicule) getTableView().getItems().get(getIndex());
                        inspectVehicule(v);
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btn);
                }
            });

            // DELETE button column.
            TableColumn<Object, Void> colDelete = new TableColumn<>("Supprimer");
            colDelete.setPrefWidth(90);
            colDelete.setCellFactory(param -> new TableCell<Object, Void>() {
                private final Button btn = new Button("Delete");
                {
                    btn.getStyleClass().add("delete-button");
                    btn.setOnAction(event -> {
                        Vehicule v = (Vehicule) getTableView().getItems().get(getIndex());
                        deleteVehicule(v);
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btn);
                }
            });

            ObservableList<TableColumn<Object, ?>> columns = FXCollections.observableArrayList(
                    colImmat, colMarque, colDateService, colKmTotal, colKmRest, colType,
                    colInspect, colDelete
            );

            searchResultsData = FXCollections.observableArrayList(filtered);
            controller.setResults(columns, searchResultsData);
            rootPane.getChildren().setAll(searchResultsPage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void inspectVehicule(Vehicule v) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/VehiculeView.fxml"));
            Parent viewPage = loader.load();
            VehiculeViewController controller = loader.getController();
            controller.initData(v);
            rootPane.getChildren().setAll(viewPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteVehicule(Vehicule v) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmer la suppression");
        confirmAlert.setHeaderText("Supprimer le véhicule");
        confirmAlert.setContentText("Voulez-vous vraiment supprimer ce véhicule (ID = "
                + v.getId() + ", immatriculation = " + v.getImmatriculation() + ")?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = vehiculeService.deleteVehicule(v.getId());
            if (success) {
                if (searchResultsData != null) {
                    searchResultsData.remove(v);
                }
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Suppression effectuée");
                info.setHeaderText(null);
                info.setContentText("Le véhicule a été supprimé avec succès.");
                info.showAndWait();
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Échec de suppression");
                error.setHeaderText("Erreur lors de la suppression");
                error.setContentText("Une erreur s'est produite en supprimant le véhicule.");
                error.showAndWait();
            }
        }
    }

    private void openAddVehiculePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AddVehicule.fxml"));
            Parent addPage = loader.load();
            Parent root = btnAddVehicule.getScene().getRoot();
            StackPane contentArea = (StackPane) root.lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(addPage);
            } else {
                rootPane.getChildren().setAll(addPage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
