package Secretaire.controller;

import Authentication.entite.Profile;
import Authentication.service.ProfileService;
import Candidat.entite.DossierCandidat;
import Candidat.service.DossierCandidatService;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CandidatsController {

    @FXML private Label totalCandidatesLabel;
    @FXML private Button btnAddCandidate;
    @FXML private TableView<DossierCandidat> recentCandidatesTable;
    @FXML private TableColumn<DossierCandidat, String> colNom;
    @FXML private TableColumn<DossierCandidat, String> colPrenom;
    @FXML private TableColumn<DossierCandidat, String> colEmail;
    @FXML private TableColumn<DossierCandidat, LocalDate> colJoinDate;
    // Removed the colInspect column

    private ObservableList<DossierCandidat> dossierList;
    private final DossierCandidatService dossierService = new DossierCandidatService();
    private final ProfileService profileService = new ProfileService();

    // Map candidateId -> Profile
    private Map<Integer, Profile> profileMap;

    @FXML
    public void initialize() {
        refreshCandidates();

        btnAddCandidate.setOnAction(event -> openAddCandidatePage());
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

        colNom.setCellValueFactory(cellData -> {
            int candidateId = cellData.getValue().getCandidateId();
            Profile profile = profileMap.get(candidateId);
            String nom = (profile != null) ? profile.getNom() : "N/A";
            return new SimpleObjectProperty<>(nom);
        });

        colPrenom.setCellValueFactory(cellData -> {
            int candidateId = cellData.getValue().getCandidateId();
            Profile profile = profileMap.get(candidateId);
            String prenom = (profile != null) ? profile.getPrenom() : "N/A";
            return new SimpleObjectProperty<>(prenom);
        });

        colEmail.setCellValueFactory(cellData -> {
            int candidateId = cellData.getValue().getCandidateId();
            Profile profile = profileMap.get(candidateId);
            String email = (profile != null) ? profile.getEmail() : "N/A";
            return new SimpleObjectProperty<>(email);
        });

        colJoinDate.setCellValueFactory(cellData -> {
            var createdAt = cellData.getValue().getCreatedAt();
            LocalDate date = (createdAt != null) ? createdAt.toLocalDate() : LocalDate.now();
            return new SimpleObjectProperty<>(date);
        });
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
