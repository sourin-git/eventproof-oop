package com.eventproof;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class EventsView extends VBox {
    private final TableView<Event> table = new TableView<>();
    private final TextField name = new TextField();
    private final ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList("TECHNICAL", "CULTURAL", "SPORTS"));
    private final ComboBox<Venue> venue = new ComboBox<>();
    private final DatePicker date = new DatePicker(LocalDate.now());
    private final TextField attendance = new TextField();
    private final Label status = new Label();
    private final Runnable refresh;

    public EventsView(Runnable refresh) {
        this.refresh = refresh;
        setSpacing(14);
        setPadding(new Insets(20));
        table.setId("events-table");
        table.setPlaceholder(new Label("No events yet. Create an event below."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        UiMessages.column(table, "ID", 45, e -> Integer.toString(e.getId()));
        UiMessages.column(table, "Event", 240, Event::getName);
        UiMessages.column(table, "Type", 110, Event::getEventType);
        UiMessages.column(table, "Date", 100, e -> e.getEventDate().toString());
        UiMessages.column(table, "Attendance", 90, e -> Integer.toString(e.getExpectedAttendance()));
        UiMessages.column(table, "Venue", 190, e -> e.getVenue().getName());
        UiMessages.column(table, "Capacity", 80, e -> Integer.toString(e.getVenue().getCapacity()));
        VBox.setVgrow(table, Priority.ALWAYS);

        name.setId("event-name");
        type.setId("event-type");
        venue.setId("event-venue");
        date.setId("event-date");
        attendance.setId("event-attendance");
        status.setId("event-status");
        name.setPromptText("Event name");
        attendance.setPromptText("Expected attendees");
        type.getSelectionModel().selectFirst();
        venue.setMaxWidth(Double.MAX_VALUE);
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.addRow(0, new Label("Name"), name, new Label("Type"), type);
        form.addRow(1, new Label("Date"), date, new Label("Attendance"), attendance);
        form.addRow(2, new Label("Venue"), venue);
        GridPane.setHgrow(name, Priority.ALWAYS);
        GridPane.setHgrow(venue, Priority.ALWAYS);
        Button save = new Button("Save event");
        save.setId("save-event");
        save.setOnAction(action -> saveEvent());
        Button reload = new Button("Refresh");
        reload.setId("refresh-events");
        reload.setOnAction(action -> refresh.run());
        Button addVenue = new Button("New venue...");
        addVenue.setId("new-venue");
        addVenue.setOnAction(action -> createVenue());
        getChildren().addAll(new Label("Stored events"), table, new Separator(),
                new Label("New event"), form, new HBox(10, save, reload, addVenue), status);
    }

    public void setData(List<Event> events, List<Venue> venues) {
        int selectedVenue = venue.getValue() == null ? -1 : venue.getValue().getId();
        table.getItems().setAll(events);
        venue.getItems().setAll(venues);
        venue.getSelectionModel().select(venues.stream().filter(v -> v.getId() == selectedVenue).findFirst()
                .orElse(venues.isEmpty() ? null : venues.getFirst()));
    }

    private void saveEvent() {
        try {
            if (venue.getValue() == null || type.getValue() == null || date.getValue() == null) {
                throw new IllegalArgumentException("Select a venue, event type and date.");
            }
            // DatePicker editor may contain a date that has not yet been committed.
            date.setValue(date.getConverter().fromString(date.getEditor().getText()));
            if (date.getValue() == null) { throw new IllegalArgumentException("Enter a valid event date."); }
            int count = Integer.parseInt(attendance.getText().trim());
            Event event = switch (type.getValue()) {
                case "TECHNICAL" -> new TechnicalEvent(0, name.getText(), date.getValue(), count, venue.getValue());
                case "CULTURAL" -> new CulturalEvent(0, name.getText(), date.getValue(), count, venue.getValue());
                case "SPORTS" -> new SportsEvent(0, name.getText(), date.getValue(), count, venue.getValue());
                default -> throw new IllegalArgumentException("Select an event type.");
            };
            new EventDAO().create(event);
            name.clear();
            attendance.clear();
            refresh.run();
            status.setText("Saved event #" + event.getId() + " to MySQL.");
        } catch (NumberFormatException exception) {
            UiMessages.inputError("Attendance must be a whole number from 0 to 2147483647.");
        } catch (java.time.format.DateTimeParseException exception) {
            UiMessages.inputError("Enter a valid date or use the calendar.");
        } catch (IllegalArgumentException exception) {
            UiMessages.inputError(exception.getMessage());
        } catch (SQLException exception) { UiMessages.databaseError(exception); }
    }

    private void createVenue() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("New venue");
        dialog.initOwner(getScene().getWindow());
        TextField venueName = new TextField();
        TextField capacity = new TextField();
        CheckBox power = new CheckBox("Backup power");
        CheckBox internet = new CheckBox("Backup internet");
        venueName.setId("venue-name");
        capacity.setId("venue-capacity");
        power.setId("venue-power");
        internet.setId("venue-internet");
        GridPane form = new GridPane();
        form.setPadding(new Insets(16));
        form.setHgap(12);
        form.setVgap(12);
        form.addRow(0, new Label("Name"), venueName);
        form.addRow(1, new Label("Capacity"), capacity);
        form.addRow(2, power, internet);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button save = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        save.addEventFilter(javafx.event.ActionEvent.ACTION, action -> {
            try {
                Venue newVenue = new Venue(0, venueName.getText(), Integer.parseInt(capacity.getText().trim()),
                        power.isSelected(), internet.isSelected());
                int id = new VenueDAO().create(newVenue);
                refresh.run();
                venue.getItems().stream().filter(v -> v.getId() == id).findFirst().ifPresent(venue::setValue);
                status.setText("Saved venue #" + id + " to MySQL.");
            } catch (NumberFormatException exception) {
                action.consume();
                UiMessages.inputError("Capacity must be a positive whole number.");
            } catch (IllegalArgumentException exception) {
                action.consume();
                UiMessages.inputError(exception.getMessage());
            } catch (SQLException exception) {
                action.consume();
                UiMessages.databaseError(exception);
            }
        });
        dialog.showAndWait();
    }
}
