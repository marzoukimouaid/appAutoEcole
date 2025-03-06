package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

// Ikonli imports
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import javafx.scene.paint.Color;

public class SecretaireController {

    @FXML private BorderPane root; // main container to manipulate sidebars

    // Sidebar
    @FXML private VBox sidebar;
    @FXML private Circle profileCircle;
    @FXML private Button btnDashboard, btnContent, btnAnalytics, btnLikes, btnComments, btnShare;
    @FXML private Button btnLogout;

    // Top Bar
    @FXML private Button btnMenu;
    @FXML private Circle avatarImageView;

    // Center Content
    @FXML private Label lblLikes, lblComments, lblShares;
    @FXML private TableView<ActivityRecord> tableActivity;
    @FXML private TableColumn<ActivityRecord, String> colName, colEmail, colType, colStatus, colJoined;

    // Track the sidebar's visibility
    private boolean isSidebarVisible = true;

    @FXML
    public void initialize() {
        // Set light gray icons
        setIkonliIcons();

        // Table setup
        setupTable();
        loadTableData();

        // Toggle the sidebar with the hamburger button
        btnMenu.setOnAction(e -> toggleSidebar());
    }

    /**
     * Show/hide the sidebar by setting left to null or the sidebar node.
     */
    private void toggleSidebar() {
        if (isSidebarVisible) {
            root.setLeft(null);
            isSidebarVisible = false;
        } else {
            root.setLeft(sidebar);
            isSidebarVisible = true;
        }
    }

    /**
     * Assign icons in light gray for a consistent look.
     */
    private void setIkonliIcons() {
        // Light gray color for icons
        Color iconColor = Color.web("#CCCCCC");

        FontIcon dashIcon = FontIcon.of(FontAwesomeSolid.HOME, 18);
        dashIcon.setIconColor(iconColor);
        btnDashboard.setGraphic(dashIcon);

        FontIcon contentIcon = FontIcon.of(FontAwesomeSolid.FOLDER, 18);
        contentIcon.setIconColor(iconColor);
        btnContent.setGraphic(contentIcon);

        FontIcon analyticsIcon = FontIcon.of(FontAwesomeSolid.CHART_BAR, 18);
        analyticsIcon.setIconColor(iconColor);
        btnAnalytics.setGraphic(analyticsIcon);

        FontIcon likesIcon = FontIcon.of(FontAwesomeSolid.THUMBS_UP, 18);
        likesIcon.setIconColor(iconColor);
        btnLikes.setGraphic(likesIcon);

        FontIcon commentsIcon = FontIcon.of(FontAwesomeSolid.COMMENT_ALT, 18);
        commentsIcon.setIconColor(iconColor);
        btnComments.setGraphic(commentsIcon);

        FontIcon shareIcon = FontIcon.of(FontAwesomeSolid.SHARE_ALT, 18);
        shareIcon.setIconColor(iconColor);
        btnShare.setGraphic(shareIcon);

        // Logout icon
        FontIcon logoutIcon = FontIcon.of(FontAwesomeSolid.SIGN_OUT_ALT, 18);
        logoutIcon.setIconColor(iconColor);
        btnLogout.setGraphic(logoutIcon);

        // Hamburger icon
        FontIcon menuIcon = FontIcon.of(FontAwesomeSolid.BARS, 18);
        menuIcon.setIconColor(iconColor);
        btnMenu.setGraphic(menuIcon);
    }

    private void setupTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colJoined.setCellValueFactory(new PropertyValueFactory<>("joined"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadTableData() {
        tableActivity.getItems().addAll(
                new ActivityRecord("Prem Shahi",    "premshahi@gmail.com",   "2022-02-12", "New",    "Liked"),
                new ActivityRecord("Deepa Chand",   "deepachand@gmail.com",  "2022-02-12", "Member", "Shared"),
                new ActivityRecord("Prakash Shahi", "prakashshahi@gmail.com","2022-02-13", "New",    "Liked"),
                new ActivityRecord("Manisha Chand", "manishachan@gmail.com", "2022-02-13", "Member", "Shared")
        );
    }

    // Data model
    public static class ActivityRecord {
        private String name;
        private String email;
        private String joined;
        private String type;
        private String status;

        public ActivityRecord(String name, String email, String joined, String type, String status) {
            this.name = name;
            this.email = email;
            this.joined = joined;
            this.type = type;
            this.status = status;
        }

        public String getName()   { return name; }
        public String getEmail()  { return email; }
        public String getJoined() { return joined; }
        public String getType()   { return type; }
        public String getStatus() { return status; }
    }
}
