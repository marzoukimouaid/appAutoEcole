package controller;

import entite.DossierCandidat;
import entite.Profile;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import service.DossierCandidatService;
import service.ProfileService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SecretaireCandidatsController {


    @FXML private StackPane rootPane;


    @FXML private Label totalCandidatesLabel;
    @FXML private Button btnAddCandidate;


    @FXML private TextField searchField;
    @FXML private Button btnSearch;
    @FXML private Label searchError;


    @FXML private VBox recentCandidatesContainer;


    private ObservableList<DossierCandidat> dossierList;


    private ObservableList<Object> searchResultsData;

    private final DossierCandidatService dossierService = new DossierCandidatService();
    private final ProfileService profileService = new ProfileService();
    private Map<Integer, Profile> profileMap;

    @FXML
    public void initialize() {

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
        totalCandidatesLabel.setText(String.valueOf(dossierList.size()));


        profileMap = dossierList.stream()
                .map(DossierCandidat::getCandidateId)
                .distinct()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> profileService.getProfileByUserId(id).orElse(null)
                ));


        recentCandidatesContainer.getChildren().clear();
        for (DossierCandidat dossier : dossierList) {
            VBox card = createCandidateCard(dossier);
            recentCandidatesContainer.getChildren().add(card);
        }
    }

    
    private VBox createCandidateCard(DossierCandidat dossier) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setMinWidth(300);
        card.setMaxWidth(Double.MAX_VALUE);


        Profile profile = profileMap.get(dossier.getCandidateId());
        String nom = (profile != null) ? profile.getNom() : "N/A";
        String prenom = (profile != null) ? profile.getPrenom() : "N/A";
        String email = (profile != null) ? profile.getEmail() : "N/A";
        LocalDate date = (dossier.getCreatedAt() != null)
                ? dossier.getCreatedAt().toLocalDate() : LocalDate.now();

        Label lblFullName = new Label("Nom: " + nom + " " + prenom);
        lblFullName.getStyleClass().add("subtitle");

        Label lblEmail = new Label("Email: " + email);
        lblEmail.getStyleClass().add("subtitle");

        Label lblJoinDate = new Label("Join Date: " + date);
        lblJoinDate.getStyleClass().add("subtitle");


        Button btnInspect = new Button("Inspect");
        btnInspect.getStyleClass().add("inspect-button");
        btnInspect.setOnAction(event -> inspectCandidate(dossier));


        Button btnEdit = new Button("Edit");
        btnEdit.getStyleClass().add("edit-button");
        btnEdit.setOnAction(event -> editCandidate(dossier));


        Button btnDelete = new Button("Delete");
        btnDelete.getStyleClass().add("delete-button");
        btnDelete.setOnAction(event -> deleteCandidate(dossier));


        HBox buttonRow = new HBox(10);
        buttonRow.getChildren().addAll(btnInspect, btnEdit, btnDelete);


        card.getChildren().addAll(lblFullName, lblEmail, lblJoinDate, buttonRow);

        return card;
    }

    
    private void editCandidate(DossierCandidat dossier) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AddCandidate.fxml"));
            Parent editCandidatePage = loader.load();

            Profile profile = profileService.getProfileByUserId(dossier.getCandidateId()).orElse(null);
            if (profile != null) {

                AddCandidateController controller = loader.getController();
                controller.initData(dossier, profile);
            }
            rootPane.getChildren().setAll(editCandidatePage);
        } catch (IOException e) {
            e.printStackTrace();
        }
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


            ObservableList<Node> cards = FXCollections.observableArrayList();
            for (DossierCandidat dossier : filtered) {
                VBox card = createCandidateCard(dossier);
                cards.add(card);
            }
            controller.setResults(cards);

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
                refreshCandidates();
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
            rootPane.getChildren().setAll(addCandidatePage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
