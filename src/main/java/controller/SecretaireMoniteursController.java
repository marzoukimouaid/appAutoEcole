package controller;

import entite.Moniteur;
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
import service.MoniteurService;
import service.ProfileService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SecretaireMoniteursController {

    @FXML private StackPane rootPane;
    @FXML private Label totalMoniteursLabel;
    @FXML private Button btnAddMoniteur;
    @FXML private TextField searchField;
    @FXML private Button btnSearch;
    @FXML private Label searchError;


    @FXML private VBox recentMoniteursContainer;

    private ObservableList<Moniteur> moniteurList;
    private final MoniteurService moniteurService = new MoniteurService();
    private final ProfileService profileService = new ProfileService();


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
        totalMoniteursLabel.setText(String.valueOf(moniteurList.size()));


        profileMap = moniteurList.stream()
                .map(Moniteur::getUserId)
                .distinct()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> profileService.getProfileByUserId(id).orElse(null)
                ));


        recentMoniteursContainer.getChildren().clear();
        for (Moniteur moniteur : moniteurList) {
            VBox card = createMoniteurCard(moniteur);
            recentMoniteursContainer.getChildren().add(card);
        }
    }

    
    private VBox createMoniteurCard(Moniteur moniteur) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setMinWidth(300);
        card.setMaxWidth(Double.MAX_VALUE);


        Profile profile = profileMap.get(moniteur.getUserId());
        String nom = (profile != null) ? profile.getNom() : "N/A";
        String prenom = (profile != null) ? profile.getPrenom() : "N/A";
        String email = (profile != null) ? profile.getEmail() : "N/A";
        String permis = (moniteur.getPermisType() != null) ? moniteur.getPermisType().name() : "N/A";

        Label lblFullName = new Label("Nom: " + nom + " " + prenom);
        lblFullName.getStyleClass().add("subtitle");

        Label lblEmail = new Label("Email: " + email);
        lblEmail.getStyleClass().add("subtitle");

        Label lblPermis = new Label("Permis: " + permis);
        lblPermis.getStyleClass().add("subtitle");


        Button btnInspect = new Button("Inspect");
        btnInspect.getStyleClass().add("inspect-button");
        btnInspect.setOnAction(event -> inspectMoniteur(moniteur));


        Button btnEdit = new Button("Edit");
        btnEdit.getStyleClass().add("edit-button");
        btnEdit.setOnAction(event -> editMoniteur(moniteur));


        Button btnDelete = new Button("Delete");
        btnDelete.getStyleClass().add("delete-button");
        btnDelete.setOnAction(event -> deleteMoniteur(moniteur));


        HBox buttonRow = new HBox(10);
        buttonRow.getChildren().addAll(btnInspect, btnEdit, btnDelete);


        card.getChildren().addAll(lblFullName, lblEmail, lblPermis, buttonRow);

        return card;
    }

    
    private void editMoniteur(Moniteur moniteur) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AddMoniteur.fxml"));
            Parent editMoniteurPage = loader.load();

            Profile profile = profileService.getProfileByUserId(moniteur.getUserId()).orElse(null);
            if (profile != null) {

                controller.AddMoniteurController controller = loader.getController();
                controller.initData(moniteur, profile);
            }
            rootPane.getChildren().setAll(editMoniteurPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();

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
        List<Moniteur> filtered = allMoniteurs.stream()
                .filter(m -> {
                    Optional<Profile> op = profileService.getProfileByUserId(m.getUserId());
                    if (!op.isPresent()) return false;
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
            controller.SearchResultsController controller = loader.getController();
            controller.setTitle("Moniteur Search Results");
            controller.setMessage("Found " + filtered.size() + " matching moniteur(s).");


            ObservableList<Node> cards = FXCollections.observableArrayList();
            for (Moniteur moniteur : filtered) {
                VBox card = createMoniteurCard(moniteur);
                cards.add(card);
            }
            controller.setResults(cards);

            rootPane.getChildren().setAll(searchResultsPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    private void inspectMoniteur(Moniteur moniteur) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/MoniteurView.fxml"));
            Parent moniteurView = loader.load();
            controller.MoniteurViewController controller = loader.getController();
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
                refreshMoniteurs();
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
