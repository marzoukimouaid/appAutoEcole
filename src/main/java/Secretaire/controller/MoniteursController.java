package Secretaire.controller;

import Authentication.entite.Profile;
import Authentication.service.ProfileService;
import Moniteur.entite.Moniteur;
import Moniteur.service.MoniteurService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MoniteursController {

    @FXML private Label totalMoniteursLabel;
    @FXML private Button btnAddMoniteur;
    @FXML private TableView<Moniteur> recentMoniteursTable;
    @FXML private TableColumn<Moniteur, String> colNom;
    @FXML private TableColumn<Moniteur, String> colPrenom;
    @FXML private TableColumn<Moniteur, String> colEmail;
    @FXML private TableColumn<Moniteur, String> colPermis;

    private ObservableList<Moniteur> moniteurList;
    private final MoniteurService moniteurService = new MoniteurService();
    private final ProfileService profileService = new ProfileService();

    // Map userId -> Profile for moniteurs
    private Map<Integer, Profile> profileMap;

    @FXML
    public void initialize() {
        refreshMoniteurs();
        btnAddMoniteur.setOnAction(event -> openAddMoniteurPage());
    }

    private void refreshMoniteurs() {
        List<Moniteur> moniteurs = moniteurService.getAllMoniteurs();
        moniteurList = FXCollections.observableArrayList(moniteurs);
        recentMoniteursTable.setItems(moniteurList);
        totalMoniteursLabel.setText(String.valueOf(moniteurList.size()));

        profileMap = moniteurList.stream()
                .map(Moniteur::getUserId)
                .distinct()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> profileService.getProfileByUserId(id).orElse(null)
                ));

        colNom.setCellValueFactory(cellData -> {
            int userId = cellData.getValue().getUserId();
            Profile profile = profileMap.get(userId);
            String nom = (profile != null) ? profile.getNom() : "N/A";
            return new SimpleObjectProperty<>(nom);
        });

        colPrenom.setCellValueFactory(cellData -> {
            int userId = cellData.getValue().getUserId();
            Profile profile = profileMap.get(userId);
            String prenom = (profile != null) ? profile.getPrenom() : "N/A";
            return new SimpleObjectProperty<>(prenom);
        });

        colEmail.setCellValueFactory(cellData -> {
            int userId = cellData.getValue().getUserId();
            Profile profile = profileMap.get(userId);
            String email = (profile != null) ? profile.getEmail() : "N/A";
            return new SimpleObjectProperty<>(email);
        });

        colPermis.setCellValueFactory(cellData -> {
            String permis = (cellData.getValue().getPermisType() != null) ?
                    cellData.getValue().getPermisType().name() : "N/A";
            return new SimpleObjectProperty<>(permis);
        });
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
                System.out.println("Content area not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
