package controller;

import entite.SeanceCode;
import entite.SeanceConduit;
import entite.ExamenCode;
import entite.ExamenConduit;
import entite.Profile;
import entite.User;

import service.SeanceCodeService;
import service.SeanceConduitService;
import service.ExamenCodeService;
import service.ExamenConduitService;
import service.ProfileService;

import Utils.SessionManager;
import Utils.AlertUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * EmploiDesSeancesController – shared by candidate & moniteur.
 *
 * We gather either:
 *   - seances/exams by "CandidatId" if role = "candidate"
 *   - seances/exams by "MoniteurId" if role = "moniteur"
 * Then we display them in the same monthly calendar & appointments list.
 */
public class EmploiDesSeancesController {

    @FXML private Label titleLabel;
    @FXML private Label monthYearLabel;
    @FXML private Button prevMonthBtn;
    @FXML private Button nextMonthBtn;
    @FXML private GridPane calendarGrid;
    @FXML private VBox appointmentsContainer;

    private User currentUser;
    private YearMonth currentYearMonth;

    // Services
    private final SeanceCodeService seanceCodeService = new SeanceCodeService();
    private final SeanceConduitService seanceConduitService = new SeanceConduitService();
    private final ExamenCodeService examenCodeService = new ExamenCodeService();
    private final ExamenConduitService examenConduitService = new ExamenConduitService();
    private final ProfileService profileService = new ProfileService();

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.err.println("No user in session. Cannot load seances/exams.");
            return;
        }
        // Start with the current month
        LocalDate now = LocalDate.now();
        currentYearMonth = YearMonth.of(now.getYear(), now.getMonth());

        drawCalendar();
        buildAppointmentsList();
    }

    @FXML
    private void handlePrevMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        drawCalendar();
        buildAppointmentsList();
    }

    @FXML
    private void handleNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        drawCalendar();
        buildAppointmentsList();
    }

    // -------------------------------------------------------------
    // 1) Draw the monthly calendar with minimal "type" labels
    // -------------------------------------------------------------
    private void drawCalendar() {
        // Set the month label
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy");
        monthYearLabel.setText(currentYearMonth.atDay(1).format(monthFmt));

        // Clear grid
        calendarGrid.getChildren().clear();

        // Day-of-week headers
        String[] dayNames = {"Lun","Mar","Mer","Jeu","Ven","Sam","Dim"};
        for (int col = 0; col < 7; col++) {
            Label header = new Label(dayNames[col]);
            header.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
            calendarGrid.add(header, col, 0);
        }

        // Basic date logic
        LocalDate first = currentYearMonth.atDay(1);
        int daysInMonth = currentYearMonth.lengthOfMonth();
        int startDayOfWeek = first.getDayOfWeek().getValue(); // Monday=1..Sunday=7
        int colIndex = startDayOfWeek - 1;
        int rowIndex = 1;

        LocalDate start = first;
        LocalDate end   = currentYearMonth.atEndOfMonth();

        // We'll collect short "type" strings by day in a Map
        Map<LocalDate, List<String>> dayTypes = new HashMap<>();
        java.util.function.BiConsumer<LocalDate,String> addType = (d, t)-> dayTypes.computeIfAbsent(d, k->new ArrayList<>()).add(t);

        // Gather the seances/exams for the current user role
        List<EventInfo> events = gatherEventsForCurrentUser(start, end);
        // For each event, we add to dayTypes
        for (EventInfo ev : events) {
            LocalDate eventDay = ev.dateTime.toLocalDate();
            addType.accept(eventDay, ev.labelForCalendar);
        }

        // Fill day cells
        for (int day = 1; day <= daysInMonth; day++) {
            VBox cell = new VBox(3);
            // Make the cell pop more
            cell.setStyle(
                    "-fx-background-color: #fff;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 5, 0.2, 0, 2);" +
                            "-fx-background-radius: 12;" +
                            "-fx-padding: 8;"
            );
            cell.setPrefSize(100, 90);

            Label dayNum = new Label(String.valueOf(day));
            dayNum.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C3E50;");
            cell.getChildren().add(dayNum);

            LocalDate theDay = first.withDayOfMonth(day);
            if (dayTypes.containsKey(theDay)) {
                Set<String> uniqueTypes = new HashSet<>(dayTypes.get(theDay));
                for (String t : uniqueTypes) {
                    Label tLabel = new Label(t);
                    tLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #555;");
                    cell.getChildren().add(tLabel);
                }
            }

            calendarGrid.add(cell, colIndex, rowIndex);
            colIndex++;
            if (colIndex > 6) {
                colIndex = 0;
                rowIndex++;
            }
        }
    }

    // -------------------------------------------------------------
    // 2) Build the monthly appointments list with "Inspect" button
    // -------------------------------------------------------------
    private void buildAppointmentsList() {
        appointmentsContainer.getChildren().clear();

        // Add a heading
        Label heading = new Label("Rendez-vous ce mois-ci");
        heading.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        appointmentsContainer.getChildren().add(heading);

        LocalDate start = currentYearMonth.atDay(1);
        LocalDate end   = currentYearMonth.atEndOfMonth();

        // gather events for the current user
        List<EventInfo> events = gatherEventsForCurrentUser(start, end);

        // Sort by date/time
        events.sort(Comparator.comparing(e-> e.dateTime));

        if (events.isEmpty()) {
            Label noDataLabel = new Label("Aucun rendez-vous ce mois-ci.");
            noDataLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #888;");
            appointmentsContainer.getChildren().add(noDataLabel);
            return;
        }

        // Build minimal cards: date/time + type + Inspect
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
        for (EventInfo ev : events) {
            VBox card = new VBox(4);
            card.getStyleClass().add("card");
            card.setPrefWidth(600);
            card.setPadding(new javafx.geometry.Insets(10));

            // Date/time
            Label dtLabel = new Label(ev.dateTime.format(dtf));
            dtLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
            card.getChildren().add(dtLabel);

            // Type
            Label typeLabel = new Label(ev.labelForCalendar);
            typeLabel.setStyle("-fx-text-fill: #444;");
            card.getChildren().add(typeLabel);

            // Inspect button
            Button inspectBtn = new Button("Inspecter");
            inspectBtn.getStyleClass().add("inspect-button");
            inspectBtn.setOnAction(e-> handleInspect(ev));

            card.getChildren().add(inspectBtn);

            appointmentsContainer.getChildren().add(card);
        }
    }

    /**
     * Gathers the seances/exams for the current user’s role between [start, end].
     *
     * If user is "candidate": we do .getSeancesByCandidatId() + .getExamenCodesByCandidatId(), etc.
     * If user is "moniteur": .getSeancesByMoniteurId() + .getExamenCodesByMoniteurId(), etc.
     */
    private List<EventInfo> gatherEventsForCurrentUser(LocalDate start, LocalDate end) {
        if (currentUser == null) return Collections.emptyList();
        String role = currentUser.getRole().toLowerCase();

        List<EventInfo> result = new ArrayList<>();

        if (role.equals("candidate")) {
            // Seance Code
            seanceCodeService.getSeancesByCandidatId(currentUser.getId()).stream()
                    .filter(s-> isWithin(s.getSessionDatetime().toLocalDate(), start, end))
                    .forEach(sc-> result.add(new EventInfo(
                            sc.getSessionDatetime(),
                            "Séance Code",
                            sc, null, null, null
                    )));

            // Seance Conduit
            seanceConduitService.getSeancesByCandidatId(currentUser.getId()).stream()
                    .filter(s-> isWithin(s.getSessionDatetime().toLocalDate(), start, end))
                    .forEach(sc-> result.add(new EventInfo(
                            sc.getSessionDatetime(),
                            "Séance Conduit",
                            null, sc, null, null
                    )));

            // Examen Code
            examenCodeService.getExamenCodesByCandidatId(currentUser.getId()).stream()
                    .filter(e-> isWithin(e.getExamDatetime().toLocalDate(), start, end))
                    .forEach(ec-> result.add(new EventInfo(
                            ec.getExamDatetime(),
                            "Examen Code",
                            null, null, ec, null
                    )));

            // Examen Conduit
            examenConduitService.getExamenConduitsByCandidatId(currentUser.getId()).stream()
                    .filter(e-> isWithin(e.getExamDatetime().toLocalDate(), start, end))
                    .forEach(ec-> result.add(new EventInfo(
                            ec.getExamDatetime(),
                            "Examen Conduit",
                            null, null, null, ec
                    )));

        } else if (role.equals("moniteur")) {
            // For moniteur: .getSeancesByMoniteurId, .getExamenCodesByMoniteurId, etc.
            seanceCodeService.getSeancesByMoniteurId(currentUser.getId()).stream()
                    .filter(s-> isWithin(s.getSessionDatetime().toLocalDate(), start, end))
                    .forEach(sc-> result.add(new EventInfo(
                            sc.getSessionDatetime(),
                            "Séance Code",
                            sc, null, null, null
                    )));

            seanceConduitService.getSeancesByMoniteurId(currentUser.getId()).stream()
                    .filter(s-> isWithin(s.getSessionDatetime().toLocalDate(), start, end))
                    .forEach(sc-> result.add(new EventInfo(
                            sc.getSessionDatetime(),
                            "Séance Conduit",
                            null, sc, null, null
                    )));

            examenCodeService.getExamenCodesByMoniteurId(currentUser.getId()).stream()
                    .filter(e-> isWithin(e.getExamDatetime().toLocalDate(), start, end))
                    .forEach(ec-> result.add(new EventInfo(
                            ec.getExamDatetime(),
                            "Examen Code",
                            null, null, ec, null
                    )));

            examenConduitService.getExamenConduitsByMoniteurId(currentUser.getId()).stream()
                    .filter(e-> isWithin(e.getExamDatetime().toLocalDate(), start, end))
                    .forEach(ec-> result.add(new EventInfo(
                            ec.getExamDatetime(),
                            "Examen Conduit",
                            null, null, null, ec
                    )));
        }
        // else if (role.equals("secretaire") ... you can do something else if you want

        return result;
    }

    /**
     * Called when user clicks "Inspecter" for a given event.
     * We load a detail page depending on the event’s label/type.
     */
    private void handleInspect(EventInfo ev) {
        try {
            FXMLLoader loader = null;
            Parent detailRoot = null;

            switch (ev.labelForCalendar) {
                case "Séance Code":
                    loader = new FXMLLoader(getClass().getResource("/org/example/SeanceCodeDetails.fxml"));
                    detailRoot = loader.load();
                    SeanceCodeDetailsController scController = loader.getController();
                    scController.setSeance(ev.seanceCode);
                    break;

                case "Séance Conduit":
                    loader = new FXMLLoader(getClass().getResource("/org/example/SeanceConduitDetails.fxml"));
                    detailRoot = loader.load();
                    SeanceConduitDetailsController sconController = loader.getController();
                    sconController.setSeance(ev.seanceConduit);
                    break;

                case "Examen Code":
                    loader = new FXMLLoader(getClass().getResource("/org/example/ExamenCodeDetails.fxml"));
                    detailRoot = loader.load();
                    ExamenCodeDetailsController ecController = loader.getController();
                    ecController.setExamenCode(ev.examenCode);
                    break;

                case "Examen Conduit":
                    loader = new FXMLLoader(getClass().getResource("/org/example/ExamenConduitDetails.fxml"));
                    detailRoot = loader.load();
                    ExamenConduitDetailsController econController = loader.getController();
                    econController.setExamenConduit(ev.examenConduit);
                    break;

                default:
                    AlertUtils.showAlert("Erreur", "Type inconnu : " + ev.labelForCalendar, Alert.AlertType.ERROR);
                    return;
            }

            // Place detail page in #contentArea if possible
            Node sceneRoot = appointmentsContainer.getScene().getRoot();
            StackPane contentArea = (StackPane) sceneRoot.lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(detailRoot);
            } else {
                AlertUtils.showAlert("Erreur", "Zone de contenu introuvable (#contentArea).", Alert.AlertType.ERROR);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertUtils.showAlert("Erreur", "Impossible de charger la page de détails.", Alert.AlertType.ERROR);
        }
    }

    // Helper
    private boolean isWithin(LocalDate d, LocalDate start, LocalDate end) {
        return (!d.isBefore(start)) && (!d.isAfter(end));
    }

    // Data structure to unify all seances/exams
    private static class EventInfo {
        private final LocalDateTime dateTime;
        private final String labelForCalendar; // e.g. "Séance Code" or "Examen Conduit"

        // references to actual objects
        private SeanceCode seanceCode;
        private SeanceConduit seanceConduit;
        private ExamenCode examenCode;
        private ExamenConduit examenConduit;

        EventInfo(LocalDateTime dt, String label,
                  SeanceCode scC, SeanceConduit scD, ExamenCode eC, ExamenConduit eD) {
            this.dateTime = dt;
            this.labelForCalendar = label;
            this.seanceCode = scC;
            this.seanceConduit = scD;
            this.examenCode = eC;
            this.examenConduit = eD;
        }
    }
}
