package controller;

import entite.Profile;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import service.ProfileService;
import entite.DossierCandidat;
import service.DossierCandidatService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CandidatsController {
    @FXML private StackPane rootPane;
    @FXML private Label totalCandidatesLabel;
    @FXML private Button btnAddCandidate;
    @FXML private TextField searchField;
    @FXML private Button btnSearch;
    @FXML private Label searchError; // inline error label for search input
    @FXML private TableView<DossierCandidat> recentCandidatesTable;

    private ObservableList<DossierCandidat> dossierList;
    // This field holds the search results data (used in the search results view)
    private ObservableList<Object> searchResultsData;

    private final DossierCandidatService dossierService = new DossierCandidatService();
    private final ProfileService profileService = new ProfileService();
    // Map candidateId -> Profile built using Java 8 streams.
    private Map<Integer, Profile> profileMap;

    @FXML
    public void initialize() {
        // Clear error styling as user types in search field.
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            searchField.getStyleClass().remove("error");
            searchError.setText("");
        });
        refreshCandidates();
        btnAddCandidate.setOnAction(event -> openAddCandidatePage());
        btnSearch.setOnAction(event -> performSearch());
    }

    private void refreshCandidates() {
        List<DossierCandidat> dossiers = dossierService.getAllDossiers();
        dossierList = FXCollections.observableArrayList(dossiers);
        recentCandidatesTable.setItems(dossierList);
        totalCandidatesLabel.setText(String.valueOf(dossierList.size()));

        profileMap = dossierList.stream()
                .map(DossierCandidat::getCandidateId)
                .distinct()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> profileService.getProfileByUserId(id).orElse(null)
                ));

        // Set default table columns (when not in search mode)
        TableColumn<DossierCandidat, String> colNom = (TableColumn<DossierCandidat, String>) recentCandidatesTable.getColumns().get(0);
        colNom.setCellValueFactory(cellData -> {
            int candidateId = cellData.getValue().getCandidateId();
            Profile profile = profileMap.get(candidateId);
            String nom = (profile != null) ? profile.getNom() : "N/A";
            return new SimpleObjectProperty<>(nom);
        });

        TableColumn<DossierCandidat, String> colPrenom = (TableColumn<DossierCandidat, String>) recentCandidatesTable.getColumns().get(1);
        colPrenom.setCellValueFactory(cellData -> {
            int candidateId = cellData.getValue().getCandidateId();
            Profile profile = profileMap.get(candidateId);
            String prenom = (profile != null) ? profile.getPrenom() : "N/A";
            return new SimpleObjectProperty<>(prenom);
        });

        TableColumn<DossierCandidat, String> colEmail = (TableColumn<DossierCandidat, String>) recentCandidatesTable.getColumns().get(2);
        colEmail.setCellValueFactory(cellData -> {
            int candidateId = cellData.getValue().getCandidateId();
            Profile profile = profileMap.get(candidateId);
            String email = (profile != null) ? profile.getEmail() : "N/A";
            return new SimpleObjectProperty<>(email);
        });

        TableColumn<DossierCandidat, LocalDate> colJoinDate = (TableColumn<DossierCandidat, LocalDate>) recentCandidatesTable.getColumns().get(3);
        colJoinDate.setCellValueFactory(cellData -> {
            LocalDate date = (cellData.getValue().getCreatedAt() != null)
                    ? cellData.getValue().getCreatedAt().toLocalDate() : LocalDate.now();
            return new SimpleObjectProperty<>(date);
        });
    }

    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.length() < 3) {
            if (!searchField.getStyleClass().contains("error")) {
                searchField.getStyleClass().add("error");
            }
            searchError.setText("Please enter at least 3 characters.");
            return;
        }
        searchField.getStyleClass().remove("error");
        searchError.setText("");

        List<DossierCandidat> allDossiers = dossierService.getAllDossiers();
        List<DossierCandidat> filtered = allDossiers.stream()
                .filter(d -> {
                    int candidateId = d.getCandidateId();
                    Profile profile = profileService.getProfileByUserId(candidateId).orElse(null);
                    return profile != null &&
                            (profile.getNom().toLowerCase().contains(query)
                                    || profile.getPrenom().toLowerCase().contains(query));
                })
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            Label noResults = new Label("No matching candidates found.");
            noResults.getStyleClass().add("section-title");
            rootPane.getChildren().setAll(noResults);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/SearchResults.fxml"));
            Parent searchResultsPage = loader.load();
            SearchResultsController controller = loader.getController();
            controller.setTitle("Candidate Search Results");
            controller.setMessage("Found " + filtered.size() + " candidate(s).");

            TableColumn<Object, String> dynColNom = new TableColumn<>("Nom");
            dynColNom.setPrefWidth(100);
            dynColNom.setCellValueFactory(cellData -> {
                int candidateId = ((DossierCandidat) cellData.getValue()).getCandidateId();
                Profile profile = profileService.getProfileByUserId(candidateId).orElse(null);
                String nom = (profile != null) ? profile.getNom() : "N/A";
                return new SimpleObjectProperty<>(nom);
            });

            TableColumn<Object, String> dynColPrenom = new TableColumn<>("Prénom");
            dynColPrenom.setPrefWidth(100);
            dynColPrenom.setCellValueFactory(cellData -> {
                int candidateId = ((DossierCandidat) cellData.getValue()).getCandidateId();
                Profile profile = profileService.getProfileByUserId(candidateId).orElse(null);
                String prenom = (profile != null) ? profile.getPrenom() : "N/A";
                return new SimpleObjectProperty<>(prenom);
            });

            TableColumn<Object, String> dynColEmail = new TableColumn<>("Email");
            dynColEmail.setPrefWidth(200);
            dynColEmail.setCellValueFactory(cellData -> {
                int candidateId = ((DossierCandidat) cellData.getValue()).getCandidateId();
                Profile profile = profileService.getProfileByUserId(candidateId).orElse(null);
                String email = (profile != null) ? profile.getEmail() : "N/A";
                return new SimpleObjectProperty<>(email);
            });

            TableColumn<Object, LocalDate> dynColJoinDate = new TableColumn<>("Join Date");
            dynColJoinDate.setPrefWidth(120);
            dynColJoinDate.setCellValueFactory(cellData -> {
                LocalDate date = ((DossierCandidat) cellData.getValue()).getCreatedAt() != null
                        ? ((DossierCandidat) cellData.getValue()).getCreatedAt().toLocalDate()
                        : LocalDate.now();
                return new SimpleObjectProperty<>(date);
            });

            TableColumn<Object, Void> dynColInspect = new TableColumn<>("Inspect");
            dynColInspect.setPrefWidth(100);
            dynColInspect.setCellFactory(param -> new TableCell<Object, Void>() {
                private final Button btn = new Button("Inspect");
                {
                    btn.getStyleClass().add("inspect-button");
                    btn.setOnAction(event -> {
                        DossierCandidat dossier = (DossierCandidat) getTableView().getItems().get(getIndex());
                        inspectCandidate(dossier);
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btn);
                }
            });

            TableColumn<Object, Void> dynColDelete = new TableColumn<>("Delete");
            dynColDelete.setPrefWidth(100);
            dynColDelete.setCellFactory(param -> new TableCell<Object, Void>() {
                private final Button btn = new Button("Delete");
                {
                    btn.getStyleClass().add("delete-button");
                    btn.setOnAction(event -> {
                        DossierCandidat dossier = (DossierCandidat) getTableView().getItems().get(getIndex());
                        deleteCandidate(dossier);
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btn);
                }
            });

            ObservableList<TableColumn<Object, ?>> columns = FXCollections.observableArrayList(
                    dynColNom, dynColPrenom, dynColEmail, dynColJoinDate, dynColInspect, dynColDelete
            );
            // Store the observable list so deletion updates are instant.
            searchResultsData = FXCollections.observableArrayList(filtered);
            controller.setResults(columns, searchResultsData);

            rootPane.getChildren().setAll(searchResultsPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void inspectCandidate(DossierCandidat dossier) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/CandidateView.fxml"));
            Parent candidateView = loader.load();
            CandidateViewController controller = loader.getController();
            controller.initData(dossier);
            rootPane.getChildren().setAll(candidateView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a candidate after confirmation and instantly removes the candidate from the search results table.
     */
    private void deleteCandidate(DossierCandidat dossier) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Candidate");
        confirmAlert.setContentText("Are you sure you want to delete candidate with ID: "
                + dossier.getCandidateId() + "?\nThis will also delete the candidate's associated profile and user account.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = dossierService.deleteCandidateCascade(dossier.getCandidateId());
            if (success) {
                // Remove from search results observable list for instant update
                if (searchResultsData != null) {
                    searchResultsData.remove(dossier);
                }
                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("Deletion Successful");
                infoAlert.setHeaderText(null);
                infoAlert.setContentText("Candidate deleted successfully.");
                infoAlert.showAndWait();
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Deletion Failed");
                errorAlert.setHeaderText("Error Deleting Candidate");
                errorAlert.setContentText("An error occurred while deleting the candidate.");
                errorAlert.showAndWait();
            }
        }
    }

    private void openAddCandidatePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AddCandidate.fxml"));
            Parent addCandidatePage = loader.load();
            Parent root = btnAddCandidate.getScene().getRoot();
            StackPane contentArea = (StackPane) root.lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(addCandidatePage);
            } else {
                System.out.println("Content area not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
