package controller;

import entite.Moniteur;
import entite.Profile;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import service.MoniteurService;
import service.ProfileService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MoniteursController {

    @FXML private StackPane rootPane;
    @FXML private Label totalMoniteursLabel;
    @FXML private Button btnAddMoniteur;
    @FXML private TextField searchField;
    @FXML private Button btnSearch;
    @FXML private Label searchError; // Inline error label for search input
    @FXML private TableView<Moniteur> recentMoniteursTable;
    @FXML private TableColumn<Moniteur, String> colNom;
    @FXML private TableColumn<Moniteur, String> colPrenom;
    @FXML private TableColumn<Moniteur, String> colEmail;
    @FXML private TableColumn<Moniteur, String> colPermis;

    private ObservableList<Moniteur> moniteurList;
    private ObservableList<Object> searchResultsData; // for dynamic search table

    private final MoniteurService moniteurService = new MoniteurService();
    private final ProfileService profileService = new ProfileService();

    // userId -> Profile
    private Map<Integer, Profile> profileMap;

    @FXML
    public void initialize() {
        refreshMoniteurs();
        btnAddMoniteur.setOnAction(event -> openAddMoniteurPage());
        btnSearch.setOnAction(event -> performSearch());
    }

    private void refreshMoniteurs() {
        List<Moniteur> moniteurs = moniteurService.getAllMoniteurs();
        moniteurList = FXCollections.observableArrayList(moniteurs);
        recentMoniteursTable.setItems(moniteurList);
        totalMoniteursLabel.setText(String.valueOf(moniteurList.size()));

        // Build map userId->Profile
        profileMap = moniteurList.stream()
                .map(Moniteur::getUserId)
                .distinct()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> profileService.getProfileByUserId(id).orElse(null)
                ));

        // Set cell factories for our existing columns:
        colNom.setCellValueFactory(cellData -> {
            Profile p = profileMap.get(cellData.getValue().getUserId());
            return new SimpleObjectProperty<>(p != null ? p.getNom() : "N/A");
        });

        colPrenom.setCellValueFactory(cellData -> {
            Profile p = profileMap.get(cellData.getValue().getUserId());
            return new SimpleObjectProperty<>(p != null ? p.getPrenom() : "N/A");
        });

        colEmail.setCellValueFactory(cellData -> {
            Profile p = profileMap.get(cellData.getValue().getUserId());
            return new SimpleObjectProperty<>(p != null ? p.getEmail() : "N/A");
        });

        colPermis.setCellValueFactory(cellData -> {
            Moniteur m = cellData.getValue();
            return new SimpleObjectProperty<>(m.getPermisType() != null ? m.getPermisType().name() : "N/A");
        });
    }

    /**
     * Validates that the search field is not empty; if empty, sets an error style and an inline error message.
     * Then, performs search filtering moniteurs by their associated profile's nom or prenom (case-insensitive).
     */
    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();

        // Validate input is not empty
        if (query.isEmpty()) {
            if (!searchField.getStyleClass().contains("error")) {
                searchField.getStyleClass().add("error");
            }
            if (searchError != null) {
                searchError.setText("Veuillez saisir le nom ou le prénom pour la recherche.");
            }
            return;
        } else {
            searchField.getStyleClass().remove("error");
            if (searchError != null) {
                searchError.setText("");
            }
        }

        List<Moniteur> allMoniteurs = moniteurService.getAllMoniteurs();
        // Filter by profile nom or prenom (or email if desired)
        List<Moniteur> filtered = allMoniteurs.stream()
                .filter(m -> {
                    Optional<Profile> op = profileService.getProfileByUserId(m.getUserId());
                    if (!op.isPresent()) {
                        return false;
                    }
                    Profile p = op.get();
                    String searchText = (p.getNom() + " " + p.getPrenom()).toLowerCase();
                    return searchText.contains(query);
                })
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            Label noResults = new Label("Aucun moniteur ne correspond à la recherche.");
            noResults.getStyleClass().add("section-title");
            rootPane.getChildren().setAll(noResults);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/SearchResults.fxml"));
            Parent searchResultsPage = loader.load();

            SearchResultsController controller = loader.getController();
            controller.setTitle("Moniteur Search Results");
            controller.setMessage("Found " + filtered.size() + " matching moniteur(s).");

            // Build dynamic columns for search results:
            TableColumn<Object, String> dynColNom = new TableColumn<>("Nom");
            dynColNom.setPrefWidth(100);
            dynColNom.setCellValueFactory(cellData -> {
                Moniteur m = (Moniteur) cellData.getValue();
                Optional<Profile> op = profileService.getProfileByUserId(m.getUserId());
                return new SimpleObjectProperty<>(op.isPresent() ? op.get().getNom() : "N/A");
            });

            TableColumn<Object, String> dynColPrenom = new TableColumn<>("Prénom");
            dynColPrenom.setPrefWidth(100);
            dynColPrenom.setCellValueFactory(cellData -> {
                Moniteur m = (Moniteur) cellData.getValue();
                Optional<Profile> op = profileService.getProfileByUserId(m.getUserId());
                return new SimpleObjectProperty<>(op.isPresent() ? op.get().getPrenom() : "N/A");
            });

            TableColumn<Object, String> dynColEmail = new TableColumn<>("Email");
            dynColEmail.setPrefWidth(200);
            dynColEmail.setCellValueFactory(cellData -> {
                Moniteur m = (Moniteur) cellData.getValue();
                Optional<Profile> op = profileService.getProfileByUserId(m.getUserId());
                return new SimpleObjectProperty<>(op.isPresent() ? op.get().getEmail() : "N/A");
            });

            TableColumn<Object, String> dynColPermis = new TableColumn<>("Permis");
            dynColPermis.setPrefWidth(120);
            dynColPermis.setCellValueFactory(cellData -> {
                Moniteur m = (Moniteur) cellData.getValue();
                return new SimpleObjectProperty<>(m.getPermisType() != null ? m.getPermisType().name() : "N/A");
            });

            // INSPECT button
            TableColumn<Object, Void> dynColInspect = new TableColumn<>("Inspect");
            dynColInspect.setPrefWidth(100);
            dynColInspect.setCellFactory(param -> new TableCell<Object, Void>() {
                private final Button btn = new Button("Inspect");
                {
                    btn.getStyleClass().add("inspect-button");
                    btn.setOnAction(event -> {
                        Moniteur item = (Moniteur) getTableView().getItems().get(getIndex());
                        inspectMoniteur(item);
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btn);
                }
            });

            // DELETE button
            TableColumn<Object, Void> dynColDelete = new TableColumn<>("Delete");
            dynColDelete.setPrefWidth(100);
            dynColDelete.setCellFactory(param -> new TableCell<Object, Void>() {
                private final Button btn = new Button("Delete");
                {
                    btn.getStyleClass().add("delete-button");
                    btn.setOnAction(event -> {
                        Moniteur item = (Moniteur) getTableView().getItems().get(getIndex());
                        deleteMoniteur(item);
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btn);
                }
            });

            ObservableList<TableColumn<Object, ?>> columns = FXCollections.observableArrayList(
                    dynColNom, dynColPrenom, dynColEmail, dynColPermis, dynColInspect, dynColDelete
            );

            // Store filtered data in an ObservableList so that deletion updates the table instantly.
            searchResultsData = FXCollections.observableArrayList(filtered);
            controller.setResults(columns, searchResultsData);

            rootPane.getChildren().setAll(searchResultsPage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void inspectMoniteur(Moniteur moniteur) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/MoniteurView.fxml"));
            Parent moniteurView = loader.load();

            MoniteurViewController controller = loader.getController();
            controller.initData(moniteur);

            rootPane.getChildren().setAll(moniteurView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteMoniteur(Moniteur moniteur) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Moniteur");
        confirmAlert.setContentText("Are you sure you want to delete this moniteur (userId = "
                + moniteur.getUserId() + ")?\nThis removes the moniteur, profile, and user record.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = moniteurService.deleteMoniteurCascade(moniteur.getUserId());
            if (success) {
                if (searchResultsData != null) {
                    searchResultsData.remove(moniteur);
                }
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Deletion Successful");
                info.setHeaderText(null);
                info.setContentText("Moniteur deleted successfully.");
                info.showAndWait();
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Deletion Failed");
                error.setHeaderText("Error Deleting Moniteur");
                error.setContentText("An error occurred while deleting.");
                error.showAndWait();
            }
        }
    }

    private void openAddMoniteurPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AddMoniteur.fxml"));
            Parent addMoniteurPage = loader.load();

            Parent root = btnAddMoniteur.getScene().getRoot();
            StackPane contentArea = (StackPane) root.lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(addMoniteurPage);
            } else {
                rootPane.getChildren().setAll(addMoniteurPage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
