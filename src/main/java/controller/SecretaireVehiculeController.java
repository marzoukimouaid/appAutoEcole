package controller;

import entite.Vehicule;
import entite.Vehicule.VehicleType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import service.VehiculeService;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SecretaireVehiculeController {

    @FXML private StackPane rootPane;
    @FXML private Label totalVehiculesLabel;
    @FXML private Button btnAddVehicule;
    @FXML private Button btnSearch;
    @FXML private TextField searchField;
    @FXML private Label searchError;
    // Container for the "recently added" vehicles (default display)
    @FXML private VBox vehiculesContainer;

    private ObservableList<Vehicule> vehiculeList;
    private final VehiculeService vehiculeService = new VehiculeService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        refreshVehicules();
        btnAddVehicule.setOnAction(e -> openAddVehiculePage());
        btnSearch.setOnAction(e -> performSearch());
    }

    /**
     * Loads all vehicles and displays them as modern cards in the default view.
     */
    private void refreshVehicules() {
        List<Vehicule> allVehicules = vehiculeService.getAllVehicules();
        vehiculeList = FXCollections.observableArrayList(allVehicules);
        totalVehiculesLabel.setText(String.valueOf(vehiculeList.size()));

        // Clear existing and repopulate the container with vehicle cards
        vehiculesContainer.getChildren().clear();
        for (Vehicule vehicule : vehiculeList) {
            VBox card = createVehiculeCard(vehicule);
            vehiculesContainer.getChildren().add(card);
        }
    }

    /**
     * Creates a sleek card for a single vehicle record.
     */
    private VBox createVehiculeCard(Vehicule vehicule) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");  // Make sure your CSS defines .card
        card.setMinWidth(300);
        card.setMaxWidth(Double.MAX_VALUE);

        Label lblImmatriculation = new Label("Immatriculation: " + vehicule.getImmatriculation());
        lblImmatriculation.getStyleClass().add("subtitle");

        Label lblMarque = new Label("Marque: " + vehicule.getMarque());
        lblMarque.getStyleClass().add("subtitle");

        String dateStr = vehicule.getDateMiseEnService().format(formatter);
        Label lblDateService = new Label("Mise en service: " + dateStr);
        lblDateService.getStyleClass().add("subtitle");

        Label lblKilometrage = new Label("Kilométrage: " + vehicule.getKilometrageTotal());
        lblKilometrage.getStyleClass().add("subtitle");

        Label lblKmRestant = new Label("Km restant entretien: " + vehicule.getKmRestantEntretien());
        lblKmRestant.getStyleClass().add("subtitle");

        String typeStr = (vehicule.getType() != null) ? vehicule.getType().name() : "N/A";
        Label lblType = new Label("Type: " + typeStr);
        lblType.getStyleClass().add("subtitle");

        // Create Inspect button
        Button btnInspect = new Button("Inspect");
        btnInspect.getStyleClass().add("inspect-button");
        btnInspect.setOnAction(e -> inspectVehicule(vehicule));

        // Create Edit button
        Button btnEdit = new Button("Edit");
        btnEdit.getStyleClass().add("edit-button");
        btnEdit.setOnAction(e -> editVehicule(vehicule));

        // Create Delete button
        Button btnDelete = new Button("Delete");
        btnDelete.getStyleClass().add("delete-button");
        btnDelete.setOnAction(e -> deleteVehicule(vehicule));

        HBox buttonRow = new HBox(10);
        buttonRow.getChildren().addAll(btnInspect, btnEdit, btnDelete);

        card.getChildren().addAll(
                lblImmatriculation,
                lblMarque,
                lblDateService,
                lblKilometrage,
                lblKmRestant,
                lblType,
                buttonRow
        );

        return card;
    }

    /**
     * Opens the Add Vehicule page in edit mode and preloads the vehicle's data.
     */
    private void editVehicule(Vehicule vehicule) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AddVehicule.fxml"));
            Parent editVehiculePage = loader.load();
            AddVehiculeController controller = loader.getController();
            controller.initData(vehicule);
            rootPane.getChildren().setAll(editVehiculePage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Performs a search filtering vehicles by immatriculation or marque.
     */
    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();

        if (query.isEmpty()) {
            if (!searchField.getStyleClass().contains("error")) {
                searchField.getStyleClass().add("error");
            }
            searchError.setText("Veuillez saisir l'immatriculation ou la marque.");
            return;
        } else {
            searchField.getStyleClass().remove("error");
            searchError.setText("");
        }

        List<Vehicule> allVehicules = vehiculeService.getAllVehicules();
        List<Vehicule> filtered = allVehicules.stream()
                .filter(v -> {
                    String immat = v.getImmatriculation().toLowerCase();
                    String marque = v.getMarque().toLowerCase();
                    return immat.contains(query) || marque.contains(query);
                })
                .collect(Collectors.toList());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/SearchResults.fxml"));
            Parent searchResultsPage = loader.load();
            controller.SearchResultsController controller = loader.getController();
            controller.setTitle("Vehicules Search Results");
            controller.setMessage("Found " + filtered.size() + " matching Vehicule(s)."); // remove subtitle text

            ObservableList<Node> cards = FXCollections.observableArrayList();
            if (filtered.isEmpty()) {
                Label noResults = new Label("Aucun véhicule ne correspond à la recherche.");
                noResults.getStyleClass().add("section-title");
                VBox centerBox = new VBox(noResults);
                centerBox.setAlignment(Pos.CENTER);
                cards.add(centerBox);
            } else {
                for (Vehicule v : filtered) {
                    VBox card = createVehiculeCard(v);
                    cards.add(card);
                }
            }
            controller.setResults(cards);
            rootPane.getChildren().setAll(searchResultsPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens a detailed view for the given vehicle.
     */
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

    /**
     * Deletes the given vehicle after confirmation.
     */
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
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Suppression effectuée");
                info.setHeaderText(null);
                info.setContentText("Le véhicule a été supprimé avec succès.");
                info.showAndWait();
                refreshVehicules();
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
