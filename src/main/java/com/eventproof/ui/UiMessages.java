package com.eventproof.ui;

import java.sql.SQLException;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.util.function.Function;

/** Small shared helpers for ordinary JavaFX tables and alerts. */
public class UiMessages {
    public static void inputError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Check input");
        alert.setHeaderText("Please check the form");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void databaseError(SQLException exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database unavailable");
        alert.setHeaderText("The database operation could not be completed");
        alert.setContentText("Check that MySQL is running, the local configuration is correct, and database setup has been run."
                + "\nSQL state: " + exception.getSQLState() + "; code: " + exception.getErrorCode());
        alert.showAndWait();
    }

    public static <T> void column(TableView<T> table, String title, double width, Function<T, String> value) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        table.getColumns().add(column);
    }
}
