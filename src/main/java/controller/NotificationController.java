package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class NotificationController {

    @FXML
    private HBox root;

    @FXML
    private Label messageLabel;

    @FXML
    private Label closeButton; // "X" button

    public HBox getRoot() {
        return root;
    }

    public Label getMessageLabel() {
        return messageLabel;
    }

    public Label getCloseButton() {
        return closeButton;
    }
}
