package controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class SearchResultsController {
    @FXML private Label lblTitle;
    @FXML private Label lblMessage;
    @FXML private TableView<Object> resultsTable;

    public void setTitle(String title) {
        lblTitle.setText(title);
    }

    public void setMessage(String message) {
        lblMessage.setText(message);
    }

    /**
     * Sets the table columns and data.
     * @param columns the list of TableColumn objects to display.
     * @param data the table data.
     */
    public void setResults(ObservableList<TableColumn<Object, ?>> columns, ObservableList<Object> data) {
        resultsTable.getColumns().setAll(columns);
        resultsTable.setItems(data);
    }
}
