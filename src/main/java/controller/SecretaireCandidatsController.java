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

    // Root pane
    @FXML private StackPane rootPane;

    // Top summary / total
    @FXML private Label totalCandidatesLabel;
    @FXML private Button btnAddCandidate;

    // Search components
    @FXML private TextField searchField;
    @FXML private Button btnSearch;
    @FXML private Label searchError;

    // Container for candidate cards
    @FXML private VBox recentCandidatesContainer;

    // Holds all candidate dossiers
    private ObservableList<DossierCandidat> dossierList;

    // For search results if needed (not used in card view)
    private ObservableList<Object> searchResultsData;

    private final DossierCandidatService dossierService = new DossierCandidatService();
    private final ProfileService profileService = new ProfileService();
    private Map<Integer, Profile> profileMap; // Maps candidateId to Profile

    @FXML
    public void initialize() {
        // Remove error styling when user types
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            searchField.getStyleClass().remove("error");
            searchError.setText("");
        });

        refreshCandidates();
        btnAddCandidate.setOnAction(event -> openAddCandidatePage());
        btnSearch.setOnAction(event -> performSearch());
    }

    /**
     * Loads all candidate dossiers and displays them as sleek cards.
     */
    private void refreshCandidates() {
        List<DossierCandidat> dossiers = dossierService.getAllDossiers();
        dossierList = FXCollections.observableArrayList(dossiers);
        totalCandidatesLabel.setText(String.valueOf(dossierList.size()));

        // Build a map: candidateId -> Profile
        profileMap = dossierList.stream()
                .map(DossierCandidat::getCandidateId)
                .distinct()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> profileService.getProfileByUserId(id).orElse(null)
                ));

        // Clear existing cards and repopulate
        recentCandidatesContainer.getChildren().clear();
        for (DossierCandidat dossier : dossierList) {
            VBox card = createCandidateCard(dossier);
            recentCandidatesContainer.getChildren().add(card);
        }
    }

    /**
     * Creates a sleek card (VBox) for a single candidate dossier.
     */
    private VBox createCandidateCard(DossierCandidat dossier) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");  // Ensure your CSS defines .card for a modern look
        card.setMinWidth(300);
        card.setMaxWidth(Double.MAX_VALUE);

        // Retrieve candidate profile
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

        // Create Inspect button
        Button btnInspect = new Button("Inspect");
        btnInspect.getStyleClass().add("inspect-button");
        btnInspect.setOnAction(event -> inspectCandidate(dossier));

        // Create Edit button
        Button btnEdit = new Button("Edit");
        btnEdit.getStyleClass().add("edit-button");
        btnEdit.setOnAction(event -> editCandidate(dossier));

        // Create Delete button
        Button btnDelete = new Button("Delete");
        btnDelete.getStyleClass().add("delete-button");
        btnDelete.setOnAction(event -> deleteCandidate(dossier));

        // Place Inspect, Edit and Delete buttons side by side
        HBox buttonRow = new HBox(10);
        buttonRow.getChildren().addAll(btnInspect, btnEdit, btnDelete);

        // Add all components to the card
        card.getChildren().addAll(lblFullName, lblEmail, lblJoinDate, buttonRow);

        return card;
    }

    /**
     * Opens the Add Candidate page in edit mode and preloads the candidate's data.
     */
    private void editCandidate(DossierCandidat dossier) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AddCandidate.fxml"));
            Parent editCandidatePage = loader.load();
            // Retrieve the candidate's profile using the candidate id from the dossier.
            Profile profile = profileService.getProfileByUserId(dossier.getCandidateId()).orElse(null);
            if (profile != null) {
                // Initialize the AddCandidateController in edit mode with existing data.
                AddCandidateController controller = loader.getController();
                controller.initData(dossier, profile);
            }
            rootPane.getChildren().setAll(editCandidatePage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Performs a search by candidate name or prenom.
     * Instead of displaying table results, builds a list of card nodes and shows them
     * in the SearchResults page (which has been updated to display cards).
     */
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

            // Build card nodes for each filtered candidate
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

    /**
     * Opens the detailed CandidateView page for the given dossier.
     */
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
     * Deletes the candidate after user confirmation.
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

    /**
     * Opens the Add Candidate page.
     */
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
