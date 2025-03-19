package controller;

import entite.SeanceCode;
import entite.SeanceConduit;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import service.SeanceCodeService;
import service.SeanceConduitService;
import Utils.SessionManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller that manages both "Séance Conduit" and "Séance Code" in one page.
 * Clicking a button to insert a seance loads the form in the same "red area."
 */
public class SecretaireSeancesController {

    @FXML
    private StackPane rootPane;  // The top-level container from SecretaireSeances.fxml

    @FXML
    private VBox seanceListContainer; // Where we place the list of seance "cards"

    @FXML
    private Button btnInsertConduit;

    @FXML
    private Button btnInsertCode;

    private final SeanceCodeService codeService = new SeanceCodeService();
    private final SeanceConduitService conduitService = new SeanceConduitService();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        // Load and display the list of seances
        loadSeances();
    }

    /**
     * Loads both SeanceCode and SeanceConduit from the DB for the current user,
     * merges them, sorts by sessionDatetime descending, and displays them.
     */
    private void loadSeances() {
        seanceListContainer.getChildren().clear();

        int userId = SessionManager.getCurrentUser().getId();
        List<SeanceCode> codeList = codeService.getSeancesByCandidatId(userId);
        List<SeanceConduit> conduitList = conduitService.getSeancesByCandidatId(userId);

        // Merge into a single list of Objects
        List<Object> allSeances = new ArrayList<>();
        allSeances.addAll(codeList);
        allSeances.addAll(conduitList);

        // Sort newest to oldest
        allSeances.sort((o1, o2) -> {
            LocalDateTime dt1 = (o1 instanceof SeanceCode)
                    ? ((SeanceCode) o1).getSessionDatetime()
                    : ((SeanceConduit) o1).getSessionDatetime();
            LocalDateTime dt2 = (o2 instanceof SeanceCode)
                    ? ((SeanceCode) o2).getSessionDatetime()
                    : ((SeanceConduit) o2).getSessionDatetime();
            return dt2.compareTo(dt1);
        });

        // Create a simple "card" for each seance
        for (Object seance : allSeances) {
            VBox card = createSeanceCard(seance);
            seanceListContainer.getChildren().add(card);
        }
    }

    /**
     * Creates a small VBox "card" that displays the seance info.
     */
    private VBox createSeanceCard(Object seance) {
        VBox card = new VBox(5);
        card.getStyleClass().add("card");

        Label lblType = new Label();
        Label lblDate = new Label();

        if (seance instanceof SeanceCode) {
            SeanceCode sc = (SeanceCode) seance;
            lblType.setText("Séance Code");
            lblDate.setText("Date/Heure: " + sc.getSessionDatetime().format(dtf));
        } else if (seance instanceof SeanceConduit) {
            SeanceConduit sc = (SeanceConduit) seance;
            lblType.setText("Séance Conduit");
            lblDate.setText("Date/Heure: " + sc.getSessionDatetime().format(dtf)
                    + " | Lieu: (" + sc.getLatitude() + ", " + sc.getLongitude() + ")");
        }
        card.getChildren().addAll(lblType, lblDate);
        return card;
    }

    /**
     * Called when the user clicks "Insérer Séance Conduit."
     * We load InsertSeanceConduit.fxml into the same area.
     */
    @FXML
    private void handleInsertConduit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/InsertSeanceConduit.fxml"));
            Parent insertPage = loader.load();
            InsertSeanceConduitController controller = loader.getController();
            controller.setParentController(this);
            // Optionally hide the top buttons here if desired:
            btnInsertConduit.setVisible(false);
            btnInsertCode.setVisible(false);
            // Replace the rootPane content with the insertion form.
            rootPane.getChildren().setAll(insertPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when the user clicks "Insérer Séance Code."
     * We do the same approach, but load InsertSeanceCode.fxml instead.
     */
    @FXML
    private void handleInsertCode() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/InsertSeanceCode.fxml"));
            Parent insertPage = loader.load();

            InsertSeanceCodeController controller = loader.getController();
            controller.setParentController(this);

            Parent sceneRoot = btnInsertCode.getScene().getRoot();
            StackPane contentArea = (StackPane) sceneRoot.lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(insertPage);
            } else {
                rootPane.getChildren().setAll(insertPage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Public method that other controllers (the insert forms) can call
     * once a seance is created, to return to this page and refresh.
     */
    public void returnToSeancesPage() {
        try {
            // Reload this same FXML so it resets to the updated list.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/SecretaireSeances.fxml"));
            Parent seancesPage = loader.load();

            // Show the new page in the same content area
            Parent sceneRoot = rootPane.getScene().getRoot();
            StackPane contentArea = (StackPane) sceneRoot.lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(seancesPage);
            } else {
                // If no contentArea found, fallback
                rootPane.getChildren().setAll(seancesPage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
