package controller;

import entite.SeanceCode;
import entite.SeanceConduit;
import entite.Profile;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import service.SeanceCodeService;
import service.SeanceConduitService;
import service.UserService;
import service.ProfileService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SecretaireSeancesController {

    @FXML private BorderPane rootPane;
    @FXML private VBox mainContainer;
    @FXML private VBox seanceListContainer;
    @FXML private Button btnInsertConduit;
    @FXML private Button btnInsertCode;

    private final SeanceCodeService seanceCodeService = new SeanceCodeService();
    private final SeanceConduitService seanceConduitService = new SeanceConduitService();
    private final UserService userService = new UserService();
    private final ProfileService profileService = new ProfileService();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        loadSeances();
    }

    private void loadSeances() {
        seanceListContainer.getChildren().clear();

        List<SeanceCode> codeList = seanceCodeService.getAllSeances();
        List<SeanceConduit> conduitList = seanceConduitService.getAllSeances();

        List<Object> allSeances = new ArrayList<>();
        allSeances.addAll(codeList);
        allSeances.addAll(conduitList);


        allSeances.sort((o1, o2) -> {
            LocalDateTime dt1 = (o1 instanceof SeanceCode)
                    ? ((SeanceCode) o1).getSessionDatetime()
                    : ((SeanceConduit) o1).getSessionDatetime();
            LocalDateTime dt2 = (o2 instanceof SeanceCode)
                    ? ((SeanceCode) o2).getSessionDatetime()
                    : ((SeanceConduit) o2).getSessionDatetime();
            return dt2.compareTo(dt1);
        });


        if (allSeances.isEmpty()) {
            Label noSeanceLabel = new Label("Aucune séance trouvée");
            noSeanceLabel.getStyleClass().add("no-data-label");
            seanceListContainer.getChildren().add(noSeanceLabel);
        } else {

            for (Object seance : allSeances) {
                VBox card = createSeanceCard(seance);
                seanceListContainer.getChildren().add(card);
            }
        }
    }

    private VBox createSeanceCard(Object seance) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setMinWidth(300);
        card.setMaxWidth(Double.MAX_VALUE);

        Label lblType = new Label();
        lblType.getStyleClass().add("title");

        Label lblDate = new Label();
        lblDate.getStyleClass().add("subtitle");

        int candidateId, moniteurId;
        if (seance instanceof SeanceCode) {
            candidateId = ((SeanceCode) seance).getCandidatId();
            moniteurId = ((SeanceCode) seance).getMoniteurId();
        } else {
            candidateId = ((SeanceConduit) seance).getCandidatId();
            moniteurId = ((SeanceConduit) seance).getMoniteurId();
        }

        Optional<Profile> candidateProfileOpt = profileService.getProfileByUserId(candidateId);
        Optional<Profile> moniteurProfileOpt = profileService.getProfileByUserId(moniteurId);

        String candidateFullName = candidateProfileOpt
                .map(p -> p.getNom() + " " + p.getPrenom()).orElse("N/A");
        String moniteurFullName = moniteurProfileOpt
                .map(p -> p.getNom() + " " + p.getPrenom()).orElse("N/A");

        Label lblCandidate = new Label("Candidat: " + candidateFullName);
        lblCandidate.getStyleClass().add("subtitle");
        Label lblMoniteur = new Label("Moniteur: " + moniteurFullName);
        lblMoniteur.getStyleClass().add("subtitle");

        VBox detailsBox = new VBox(5);

        javafx.scene.control.Button btnInspect = new javafx.scene.control.Button("Inspect");
        btnInspect.getStyleClass().add("inspect-button");
        btnInspect.setOnAction(e -> openDetailsPage(seance));

        if (seance instanceof SeanceCode) {
            SeanceCode sc = (SeanceCode) seance;
            lblType.setText("Séance Code");
            lblDate.setText("Date/Heure: " + sc.getSessionDatetime().format(dtf));
        } else if (seance instanceof SeanceConduit) {
            SeanceConduit sc = (SeanceConduit) seance;
            lblType.setText("Séance Conduit");
            lblDate.setText("Date/Heure: " + sc.getSessionDatetime().format(dtf));
            Label lblLocation = new Label("Lieu: (" + sc.getLatitude() + ", " + sc.getLongitude() + ")");
            lblLocation.getStyleClass().add("subtitle");
            detailsBox.getChildren().add(lblLocation);
        }

        card.getChildren().addAll(lblType, lblDate, lblCandidate, lblMoniteur);
        if (!detailsBox.getChildren().isEmpty()) {
            card.getChildren().add(detailsBox);
        }
        HBox buttonContainer = new HBox();
        buttonContainer.setStyle("-fx-alignment: CENTER_RIGHT;");
        buttonContainer.getChildren().add(btnInspect);
        card.getChildren().add(buttonContainer);

        return card;
    }

    private void openDetailsPage(Object seance) {
        try {
            FXMLLoader loader;
            Parent detailsPage;
            if (seance instanceof SeanceCode) {
                URL resource = getClass().getResource("/org/example/SeanceCodeDetails.fxml");
                if (resource == null) {
                    throw new IllegalStateException("Cannot find FXML file: /org/example/SeanceCodeDetails.fxml");
                }
                loader = new FXMLLoader(resource);
                detailsPage = loader.load();
                SeanceCodeDetailsController codeDetailsController = loader.getController();
                codeDetailsController.setSeance((SeanceCode) seance);
                codeDetailsController.setParentController(this);
            } else if (seance instanceof SeanceConduit) {
                URL resource = getClass().getResource("/org/example/SeanceConduitDetails.fxml");
                if (resource == null) {
                    throw new IllegalStateException("Cannot find FXML file: /org/example/SeanceConduitDetails.fxml");
                }
                loader = new FXMLLoader(resource);
                detailsPage = loader.load();
                SeanceConduitDetailsController conduitDetailsController = loader.getController();
                conduitDetailsController.setSeance((SeanceConduit) seance);
                conduitDetailsController.setParentController(this);
            } else {
                return;
            }

            rootPane.setCenter(detailsPage);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleInsertConduit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/InsertSeanceConduit.fxml"));
            Parent insertPage = loader.load();
            InsertSeanceConduitController controller = loader.getController();
            controller.setParentController(this);
            btnInsertConduit.setVisible(false);
            btnInsertCode.setVisible(false);
            rootPane.setCenter(insertPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleInsertCode() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/InsertSeanceCode.fxml"));
            Parent insertPage = loader.load();
            InsertSeanceCodeController controller = loader.getController();
            controller.setParentController(this);
            rootPane.setCenter(insertPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    public void returnToSeancesPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/SecretaireSeances.fxml"));
            Parent seancesPage = loader.load();
            rootPane.setCenter(seancesPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void openEditCodePage(SeanceCode seance) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/InsertSeanceCode.fxml"));
            Parent editPage = loader.load();
            InsertSeanceCodeController controller = loader.getController();
            controller.setParentController(this);
            controller.setSeance(seance);
            rootPane.setCenter(editPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void openEditConduitPage(SeanceConduit seance) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/InsertSeanceConduit.fxml"));
            Parent editPage = loader.load();
            InsertSeanceConduitController controller = loader.getController();
            controller.setParentController(this);
            controller.setSeance(seance);
            rootPane.setCenter(editPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
