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

    /**
     * Replaces any existing content with the provided list of card nodes.
     * @param cards an ObservableList of Node objects (each a card) to display
     */
    public void setResults(ObservableList<Node> cards) {
        resultsContainer.getChildren().setAll(cards);
    }
}
