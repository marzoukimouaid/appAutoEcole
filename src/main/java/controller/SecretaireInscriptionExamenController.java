package controller;

import entite.ExamenCode;
import entite.ExamenConduit;
import entite.Profile;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import service.ExamenCodeService;
import service.ExamenConduitService;
import service.UserService;
import service.ProfileService;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SecretaireInscriptionExamenController {

    @FXML private BorderPane rootPane;
    @FXML private VBox mainContainer;
    @FXML private VBox examListContainer;
    @FXML private Button btnInsertExamenConduit;
    @FXML private Button btnInsertExamenCode;

    private final ExamenCodeService examenCodeService = new ExamenCodeService();
    private final ExamenConduitService examenConduitService = new ExamenConduitService();
    private final UserService userService = new UserService();
    private final ProfileService profileService = new ProfileService();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        loadExamInscriptions();
    }

    private void loadExamInscriptions() {
        examListContainer.getChildren().clear();


        List<ExamenCode> codeList = examenCodeService.getAllExamenCodes();
        List<ExamenConduit> conduitList = examenConduitService.getAllExamenConduits();

        List<Object> allExams = new ArrayList<>();
        allExams.addAll(codeList);
        allExams.addAll(conduitList);


        allExams.sort((o1, o2) -> {
            LocalDateTime dt1 = (o1 instanceof ExamenCode)
                    ? ((ExamenCode) o1).getExamDatetime()
                    : ((ExamenConduit) o1).getExamDatetime();
            LocalDateTime dt2 = (o2 instanceof ExamenCode)
                    ? ((ExamenCode) o2).getExamDatetime()
                    : ((ExamenConduit) o2).getExamDatetime();
            return dt2.compareTo(dt1);
        });


        if (allExams.isEmpty()) {
            Label noExamLabel = new Label("Aucune inscription trouvée");
            noExamLabel.getStyleClass().add("no-data-label");
            examListContainer.getChildren().add(noExamLabel);
        } else {

            allExams.forEach(exam -> {
                VBox card = createExamCard(exam);
                examListContainer.getChildren().add(card);
            });
        }
    }

    private VBox createExamCard(Object exam) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setMinWidth(300);
        card.setMaxWidth(Double.MAX_VALUE);

        Label lblType = new Label();
        lblType.getStyleClass().add("title");

        Label lblDate = new Label();
        lblDate.getStyleClass().add("subtitle");

        int candidateId, moniteurId;
        if (exam instanceof ExamenCode) {
            candidateId = ((ExamenCode) exam).getCandidatId();
            moniteurId = ((ExamenCode) exam).getMoniteurId();
        } else {
            candidateId = ((ExamenConduit) exam).getCandidatId();
            moniteurId = ((ExamenConduit) exam).getMoniteurId();
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

        Button btnInspect = new Button("Inspect");
        btnInspect.getStyleClass().add("inspect-button");
        btnInspect.setOnAction(e -> openDetailsPage(exam));

        if (exam instanceof ExamenCode) {
            ExamenCode ec = (ExamenCode) exam;
            lblType.setText("Examen Code");
            lblDate.setText("Date/Heure: " + ec.getExamDatetime().format(dtf));
        } else if (exam instanceof ExamenConduit) {
            ExamenConduit ec = (ExamenConduit) exam;
            lblType.setText("Examen Conduit");
            lblDate.setText("Date/Heure: " + ec.getExamDatetime().format(dtf));
            Label lblLocation = new Label("Lieu: (" + ec.getLatitude() + ", " + ec.getLongitude() + ")");
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

    private void openDetailsPage(Object exam) {
        try {
            FXMLLoader loader;
            Parent detailsPage;
            if (exam instanceof ExamenCode) {
                URL resource = getClass().getResource("/org/example/ExamenCodeDetails.fxml");
                if (resource == null) {
                    throw new IllegalStateException("Cannot find FXML file: /org/example/ExamenCodeDetails.fxml");
                }
                loader = new FXMLLoader(resource);
                detailsPage = loader.load();
                ExamenCodeDetailsController controller = loader.getController();
                controller.setExamenCode((ExamenCode) exam);
                controller.setParentController(this);
            } else if (exam instanceof ExamenConduit) {
                URL resource = getClass().getResource("/org/example/ExamenConduitDetails.fxml");
                if (resource == null) {
                    throw new IllegalStateException("Cannot find FXML file: /org/example/ExamenConduitDetails.fxml");
                }
                loader = new FXMLLoader(resource);
                detailsPage = loader.load();
                ExamenConduitDetailsController controller = loader.getController();
                controller.setExamenConduit((ExamenConduit) exam);
                controller.setParentController(this);
            } else {
                return;
            }
            rootPane.setCenter(detailsPage);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleInsertExamenConduit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/InsertExamenConduit.fxml"));
            Parent insertPage = loader.load();
            InsertExamenConduitController controller = loader.getController();
            controller.setParentController(this);
            btnInsertExamenConduit.setVisible(false);
            btnInsertExamenCode.setVisible(false);
            rootPane.setCenter(insertPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleInsertExamenCode() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/InsertExamenCode.fxml"));
            Parent insertPage = loader.load();
            InsertExamenCodeController controller = loader.getController();
            controller.setParentController(this);
            rootPane.setCenter(insertPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void openEditExamenCodePage(ExamenCode exam) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/InsertExamenCode.fxml"));
            Parent editPage = loader.load();
            InsertExamenCodeController controller = loader.getController();
            controller.setParentController(this);
            controller.setExamenCode(exam);
            rootPane.setCenter(editPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void openEditExamenConduitPage(ExamenConduit exam) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/InsertExamenConduit.fxml"));
            Parent editPage = loader.load();
            InsertExamenConduitController controller = loader.getController();
            controller.setParentController(this);
            controller.setExamenConduit(exam);
            rootPane.setCenter(editPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void returnToExamInscriptionsPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/SecretaireInscriptionExamen.fxml"));
            Parent examPage = loader.load();
            rootPane.setCenter(examPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
