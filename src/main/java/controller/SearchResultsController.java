package controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SearchResultsController {
    @FXML private Label lblTitle;
    @FXML private Label lblMessage;
    @FXML private VBox resultsContainer;

    public void setTitle(String title) {
        lblTitle.setText(title);
    }

    public void setMessage(String message) {
        lblMessage.setText(message);
    }

    
    public void setResults(ObservableList<Node> cards) {
        resultsContainer.getChildren().setAll(cards);
    }
}
