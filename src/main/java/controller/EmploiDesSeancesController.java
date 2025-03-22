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
import service.AutoEcoleService;
import Utils.SessionManager;
import Utils.AlertUtils;
import Utils.PDFGenerator;
import Utils.NotificationUtil;
import Utils.NotificationUtil.NotificationType;
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
import javafx.fxml.FXMLLoader;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private final AutoEcoleService autoEcoleService = new AutoEcoleService();

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
        LocalDate end = currentYearMonth.atEndOfMonth();

        // We'll collect short "type" strings by day in a Map
        Map<LocalDate, List<String>> dayTypes = new HashMap<>();
        java.util.function.BiConsumer<LocalDate,String> addType = (d, t) -> dayTypes.computeIfAbsent(d, k -> new ArrayList<>()).add(t);

        // Gather the seances/exams for the current user role
        List<EventInfo> events = gatherEventsForCurrentUser(start, end);
        // For each event, add its short label
        for (EventInfo ev : events) {
            LocalDate eventDay = ev.dateTime.toLocalDate();
            addType.accept(eventDay, ev.getShortLabel());
        }

        // Fill day cells
        for (int day = 1; day <= daysInMonth; day++) {
            VBox cell = new VBox(3);
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
    // 2) Build the monthly appointments list with "Inspect" and "Imprimer PDF" buttons
    // -------------------------------------------------------------
    private void buildAppointmentsList() {
        appointmentsContainer.getChildren().clear();

        // Create an HBox for the heading and the print button
        HBox headingBox = new HBox();
        headingBox.setSpacing(10);
        headingBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label heading = new Label("Rendez-vous ce mois-ci");
        heading.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        Button printButton = new Button("Imprimer PDF");
        printButton.getStyleClass().add("inspect-button");
        printButton.setOnAction(e -> handlePrintCalendar());
        headingBox.getChildren().addAll(heading, printButton);
        appointmentsContainer.getChildren().add(headingBox);

        LocalDate start = currentYearMonth.atDay(1);
        LocalDate end = currentYearMonth.atEndOfMonth();

        // Gather events for the current user
        List<EventInfo> events = gatherEventsForCurrentUser(start, end);

        // Sort by date/time
        events.sort(Comparator.comparing(e -> e.dateTime));

        if (events.isEmpty()) {
            Label noDataLabel = new Label("Aucun rendez-vous ce mois-ci.");
            noDataLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #888;");
            appointmentsContainer.getChildren().add(noDataLabel);
            return;
        }

        // Build minimal cards: date/time + type + Inspect button
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
        for (EventInfo ev : events) {
            VBox card = new VBox(4);
            card.getStyleClass().add("card");
            card.setPrefWidth(600);
            card.setPadding(new javafx.geometry.Insets(10));

            // Date/time label
            Label dtLabel = new Label(dtf.format(ev.dateTime));
            dtLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
            card.getChildren().add(dtLabel);

            // Type label
            Label typeLabel = new Label(ev.getShortLabel());
            typeLabel.setStyle("-fx-text-fill: #444;");
            card.getChildren().add(typeLabel);

            // Inspect button
            Button inspectBtn = new Button("Inspecter");
            inspectBtn.getStyleClass().add("inspect-button");
            inspectBtn.setOnAction(e -> handleInspect(ev));
            card.getChildren().add(inspectBtn);

            appointmentsContainer.getChildren().add(card);
        }
    }

    /**
     * Called when user clicks "Inspecter" for a given event.
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

    /**
     * Opens a file-save dialog and generates a PDF of the calendar and appointments
     * for the currently selected month.
     */
    private void handlePrintCalendar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer l'emploi du temps");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(appointmentsContainer.getScene().getWindow());
        if (file != null) {
            try {
                // Get auto-école info
                List<String[]> autoEcoleData = autoEcoleService.getAutoEcoleData();
                String header;
                String footer;
                if (!autoEcoleData.isEmpty()) {
                    String[] data = autoEcoleData.get(0);
                    header = "Auto-école: " + data[0] + "\nAdresse: " + data[1];
                    footer = "Contact: " + data[2] + " | Email: " + data[3];
                } else {
                    header = "Auto-école";
                    footer = "";
                }

                // Get candidate or moniteur details
                String candidateDetails;
                Optional<Profile> profileOpt = profileService.getProfileByUserId(currentUser.getId());
                if (profileOpt.isPresent()) {
                    candidateDetails = "Candidat: " + profileOpt.get().getFullName();
                } else {
                    candidateDetails = "Candidat: " + currentUser.getUsername();
                }

                // Build the events for the currently selected month
                LocalDate start = currentYearMonth.atDay(1);
                LocalDate end = currentYearMonth.atEndOfMonth();
                List<EventInfo> events = gatherEventsForCurrentUser(start, end);

                // Group by day so we can fill the calendar table
                Map<LocalDate, List<EventInfo>> eventsByDay = events.stream()
                        .collect(Collectors.groupingBy(e -> e.dateTime.toLocalDate()));

                // Transform into a map of day -> list of short label strings
                Map<LocalDate, List<String>> calendarLabelsByDay = new HashMap<>();
                for (Map.Entry<LocalDate, List<EventInfo>> entry : eventsByDay.entrySet()) {
                    List<String> labels = entry.getValue().stream()
                            .map(EventInfo::getShortLabel)
                            .collect(Collectors.toList());
                    calendarLabelsByDay.put(entry.getKey(), labels);
                }

                // Sort events by date/time for the appointments list and transform to full details strings
                List<EventInfo> sortedEvents = new ArrayList<>(events);
                sortedEvents.sort(Comparator.comparing(e -> e.dateTime));
                List<String> appointmentDetails = sortedEvents.stream()
                        .map(EventInfo::getFullLabel)
                        .collect(Collectors.toList());

                PDFGenerator.generateMonthlyCalendarAndAppointments(
                        header,
                        candidateDetails,
                        currentYearMonth,
                        (Map<LocalDate, List<?>>)(Map) calendarLabelsByDay,
                        (List<?>)(List) appointmentDetails,
                        footer,
                        file
                );

                showSuccessNotification("Emploi du temps généré avec succès !");
            } catch (Exception ex) {
                ex.printStackTrace();
                AlertUtils.showAlert("Erreur", "Impossible de générer l'emploi du temps.", Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Displays a success notification using NotificationUtil.
     */
    private void showSuccessNotification(String message) {
        StackPane contentArea = (StackPane) appointmentsContainer.getScene().lookup("#contentArea");
        if (contentArea != null) {
            NotificationUtil.showNotification(contentArea, message, NotificationType.SUCCESS);
        }
    }

    /**
     * Gathers the seances/exams for the current user’s role between [start, end].
     */
    private List<EventInfo> gatherEventsForCurrentUser(LocalDate start, LocalDate end) {
        if (currentUser == null) return Collections.emptyList();
        String role = currentUser.getRole().toLowerCase();

        List<EventInfo> result = new ArrayList<>();

        // Updated candidate branch to handle both "candidate" and "candidat"
        if (role.equals("candidate") || role.equals("candidat")) {
            seanceCodeService.getSeancesByCandidatId(currentUser.getId()).stream()
                    .filter(s -> isWithin(s.getSessionDatetime().toLocalDate(), start, end))
                    .forEach(sc -> result.add(new EventInfo(
                            sc.getSessionDatetime(),
                            "Séance Code",
                            sc, null, null, null
                    )));

            seanceConduitService.getSeancesByCandidatId(currentUser.getId()).stream()
                    .filter(s -> isWithin(s.getSessionDatetime().toLocalDate(), start, end))
                    .forEach(sc -> result.add(new EventInfo(
                            sc.getSessionDatetime(),
                            "Séance Conduit",
                            null, sc, null, null
                    )));

            examenCodeService.getExamenCodesByCandidatId(currentUser.getId()).stream()
                    .filter(e -> isWithin(e.getExamDatetime().toLocalDate(), start, end))
                    .forEach(ec -> result.add(new EventInfo(
                            ec.getExamDatetime(),
                            "Examen Code",
                            null, null, ec, null
                    )));

            examenConduitService.getExamenConduitsByCandidatId(currentUser.getId()).stream()
                    .filter(e -> isWithin(e.getExamDatetime().toLocalDate(), start, end))
                    .forEach(ec -> result.add(new EventInfo(
                            ec.getExamDatetime(),
                            "Examen Conduit",
                            null, null, null, ec
                    )));
        } else if (role.equals("moniteur")) {
            seanceCodeService.getSeancesByMoniteurId(currentUser.getId()).stream()
                    .filter(s -> isWithin(s.getSessionDatetime().toLocalDate(), start, end))
                    .forEach(sc -> result.add(new EventInfo(
                            sc.getSessionDatetime(),
                            "Séance Code",
                            sc, null, null, null
                    )));

            seanceConduitService.getSeancesByMoniteurId(currentUser.getId()).stream()
                    .filter(s -> isWithin(s.getSessionDatetime().toLocalDate(), start, end))
                    .forEach(sc -> result.add(new EventInfo(
                            sc.getSessionDatetime(),
                            "Séance Conduit",
                            null, sc, null, null
                    )));

            examenCodeService.getExamenCodesByMoniteurId(currentUser.getId()).stream()
                    .filter(e -> isWithin(e.getExamDatetime().toLocalDate(), start, end))
                    .forEach(ec -> result.add(new EventInfo(
                            ec.getExamDatetime(),
                            "Examen Code",
                            null, null, ec, null
                    )));

            examenConduitService.getExamenConduitsByMoniteurId(currentUser.getId()).stream()
                    .filter(e -> isWithin(e.getExamDatetime().toLocalDate(), start, end))
                    .forEach(ec -> result.add(new EventInfo(
                            ec.getExamDatetime(),
                            "Examen Conduit",
                            null, null, null, ec
                    )));
        }
        return result;
    }

    private boolean isWithin(LocalDate d, LocalDate start, LocalDate end) {
        return (!d.isBefore(start)) && (!d.isAfter(end));
    }

    // Inner class to unify all events
    private static class EventInfo {
        private final LocalDateTime dateTime;
        private final String labelForCalendar;
        private SeanceCode seanceCode;
        private SeanceConduit seanceConduit;
        private ExamenCode examenCode;
        private ExamenConduit examenConduit;

        EventInfo(LocalDateTime dt, String label,
                  SeanceCode sc, SeanceConduit scD, ExamenCode eC, ExamenConduit eD) {
            this.dateTime = dt;
            this.labelForCalendar = label;
            this.seanceCode = sc;
            this.seanceConduit = scD;
            this.examenCode = eC;
            this.examenConduit = eD;
        }

        // Returns a short label for use in the calendar table (only the type)
        public String getShortLabel() {
            return labelForCalendar;
        }

        // Returns full details for the appointments list
        public String getFullLabel() {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
            return dtf.format(dateTime) + " - " + labelForCalendar;
        }
    }
}
