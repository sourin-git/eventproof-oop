package com.eventproof.ui;

import com.eventproof.dao.IncidentDAO;
import com.eventproof.model.Event;
import com.eventproof.model.Incident;

import java.sql.SQLException;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class IncidentsView extends VBox {
    private final ComboBox<Event> event = new ComboBox<>();
    private final TableView<Incident> table = new TableView<>();
    private final TextField type = new TextField();
    private final TextArea description = new TextArea();
    private final Label status = new Label();

    public IncidentsView() {
        setPadding(new Insets(20));
        setSpacing(14);
        event.setId("incident-event");
        table.setId("incidents-table");
        type.setId("incident-type");
        description.setId("incident-description");
        status.setId("incident-status");
        event.setPrefWidth(380);
        event.valueProperty().addListener((observable, oldValue, newValue) -> {
            status.setText("");
            refresh();
        });
        table.setPlaceholder(new Label("No recorded incidents for this event."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        UiMessages.column(table, "ID", 60, i -> Integer.toString(i.getId()));
        UiMessages.column(table, "Incident type", 180, Incident::getIncidentType);
        UiMessages.column(table, "Description", 600, Incident::getDescription);
        VBox.setVgrow(table, Priority.ALWAYS);
        type.setPromptText("For example: Internet Failure");
        description.setPromptText("What happened?");
        description.setPrefRowCount(3);
        description.setWrapText(true);
        Button save = new Button("Save incident");
        save.setId("save-incident");
        save.setOnAction(action -> saveIncident());
        Button reload = new Button("Refresh");
        reload.setId("refresh-incidents");
        reload.setOnAction(action -> refresh());
        getChildren().addAll(new HBox(12, new Label("Event"), event, reload), table, new Separator(),
                new Label("Add incident"), new Label("Type"), type, new Label("Description"), description, save, status);
    }

    public void setEvents(List<Event> events) {
        int id = event.getValue() == null ? -1 : event.getValue().getId();
        event.getItems().setAll(events);
        event.setValue(events.stream().filter(e -> e.getId() == id).findFirst()
                .orElse(events.isEmpty() ? null : events.getFirst()));
    }

    private void refresh() {
        table.getItems().clear();
        if (event.getValue() == null) { return; }
        try { table.getItems().setAll(new IncidentDAO().listByEventId(event.getValue().getId())); }
        catch (SQLException exception) { UiMessages.databaseError(exception); }
    }

    private void saveIncident() {
        status.setText("");
        try {
            if (event.getValue() == null) { throw new IllegalArgumentException("Select an event first."); }
            int id = new IncidentDAO().create(new Incident(0, event.getValue().getId(), type.getText(), description.getText()));
            type.clear();
            description.clear();
            refresh();
            status.setText("Saved incident #" + id + " to MySQL.");
        } catch (IllegalArgumentException exception) { UiMessages.inputError(exception.getMessage()); }
        catch (SQLException exception) { UiMessages.databaseError(exception); }
    }
}
