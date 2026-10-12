package com.eventproof;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;

/** Manual desktop smoke check: runs real button handlers against the local MySQL database.
 * Saves one demo incident; a second process verifies that it survives reopening. */
public class DesktopDemoCheck {
    private static final String DEMO_DESCRIPTION = "Desktop demo: Internet Failure saved and reopened.";

    public static void main(String[] args) throws InterruptedException {
        boolean reopen = args.length > 0 && args[0].equals("reopen");
        CountDownLatch finished = new CountDownLatch(1);
        AtomicBoolean passed = new AtomicBoolean(false);
        Platform.startup(() -> {
            try {
                Platform.setImplicitExit(false);
                Stage stage = new Stage();
                new EventProofApp().start(stage);
                Platform.runLater(() -> {
                    try {
                        check(stage, reopen);
                        passed.set(true);
                    } catch (Exception | AssertionError exception) {
                        System.err.println("Desktop check failed: " + exception.getClass().getSimpleName());
                    } finally {
                        // Allow the window-close event to finish before shutting down the toolkit.
                        stage.close();
                        Platform.runLater(Platform::exit);
                        finished.countDown();
                    }
                });
            } catch (Exception exception) {
                System.err.println("Desktop startup failed: " + exception.getClass().getSimpleName());
                Platform.exit();
                finished.countDown();
            }
        });
        if (!finished.await(40, TimeUnit.SECONDS) || !passed.get()) {
            System.err.println("Desktop verification did not complete successfully.");
            System.exit(1);
        }
    }

    private static void check(Stage stage, boolean reopen) throws Exception {
        Scene scene = stage.getScene();
        require(stage.isShowing(), "Window did not open");
        if (!reopen) { checkCreateForms(scene); }
        TableView<Event> events = control(scene, "events-table");
        require(events.getItems().size() >= 3, "Sample events not loaded");
        Event technical = events.getItems().stream().filter(e -> e.getId() == 1).findFirst().orElseThrow();
        require(technical instanceof TechnicalEvent, "Incorrect loaded subtype");
        require(technical.getExpectedAttendance() == 800, "Incorrect attendance");
        require(technical.getVenue().getName().equals("College Auditorium"), "Incorrect venue");
        TabPane tabs = control(scene, "main-tabs");

        tabs.getSelectionModel().select(2);
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        ComboBox<Event> selection = control(scene, "reliability-event");
        selection.setValue(technical);
        Button calculate = control(scene, "calculate-reliability");
        calculate.fire();
        List<Incident> recorded = new IncidentDAO().listByEventId(1);
        ReliabilityCalculator calculator = new ReliabilityCalculator();
        int original = calculator.calculate(technical, recorded);
        require(controlLabel(scene, "original-score").equals("Original Score: " + original + " / 100"), "Score display mismatch");
        ComboBox<FailureScenario> scenario = control(scene, "failure-scenario");
        scenario.setValue(FailureScenario.INTERNET_FAILURE);
        Button simulate = control(scene, "simulate-failure");
        simulate.fire();
        SimulationResult comparison = calculator.simulate(technical, recorded, FailureScenario.INTERNET_FAILURE);
        require(controlLabel(scene, "simulated-score").equals("Simulated Score: " + comparison.getSimulatedScore() + " / 100"),
                "Simulation display mismatch");
        require(controlLabel(scene, "risk-level").contains(comparison.getRiskLevel().name()), "Risk mismatch");
        require(!controlLabel(scene, "recommendation").isBlank(), "Recommendation missing");
        require(new IncidentDAO().listByEventId(1).size() == recorded.size(), "Simulation wrote an incident");
        require(new EventDAO().findById(1).orElseThrow().getVenue().hasBackupInternet(), "Simulation changed stored venue");
        System.out.println("Desktop comparison: original=" + original + ", simulated=" + comparison.getSimulatedScore()
                + ", risk=" + comparison.getRiskLevel());

        tabs.getSelectionModel().select(1);
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        ComboBox<Event> incidentEvent = control(scene, "incident-event");
        incidentEvent.setValue(technical);
        TableView<Incident> incidents = control(scene, "incidents-table");
        boolean exists = incidents.getItems().stream().anyMatch(i -> i.getDescription().equals(DEMO_DESCRIPTION));
        if (!reopen && !exists) {
            TextField type = control(scene, "incident-type");
            TextArea description = control(scene, "incident-description");
            type.setText("Internet Failure");
            description.setText(DEMO_DESCRIPTION);
            Button save = control(scene, "save-incident");
            save.fire();
        }
        require(incidents.getItems().stream().anyMatch(i -> i.getDescription().equals(DEMO_DESCRIPTION)), "Saved incident missing in UI");
        require(new IncidentDAO().listByEventId(1).stream().anyMatch(i -> i.getDescription().equals(DEMO_DESCRIPTION)),
                "Saved incident missing in MySQL");
        System.out.println(reopen ? "PASS: a new application process loaded the saved incident."
                : "PASS: real JavaFX buttons calculated, simulated and saved an Internet Failure incident.");
    }

    private static void checkCreateForms(Scene scene) throws Exception {
        String marker = Long.toString(System.nanoTime());
        String venueName = "Verification Venue " + marker;
        String eventName = "Verification Event " + marker;
        int venueId = -1;
        int eventId = -1;
        try {
            // Fill the real modal venue dialog while its nested JavaFX event loop is active.
            Platform.runLater(() -> {
                Window dialog = Window.getWindows().stream()
                        .filter(w -> w.isShowing() && w.getScene().lookup("#venue-name") != null)
                        .findFirst().orElseThrow();
                TextField name = control(dialog.getScene(), "venue-name");
                TextField capacity = control(dialog.getScene(), "venue-capacity");
                CheckBox power = control(dialog.getScene(), "venue-power");
                CheckBox internet = control(dialog.getScene(), "venue-internet");
                name.setText(venueName);
                capacity.setText("500");
                power.setSelected(true);
                internet.setSelected(true);
                DialogPane pane = (DialogPane) dialog.getScene().getRoot();
                ((Button) pane.lookupButton(ButtonType.OK)).fire();
            });
            Button newVenue = control(scene, "new-venue");
            newVenue.fire();
            Venue createdVenue = new VenueDAO().listAll().stream()
                    .filter(v -> v.getName().equals(venueName)).findFirst().orElseThrow();
            venueId = createdVenue.getId();
            TextField name = control(scene, "event-name");
            TextField attendance = control(scene, "event-attendance");
            ComboBox<String> type = control(scene, "event-type");
            ComboBox<Venue> venue = control(scene, "event-venue");
            name.setText(eventName);
            attendance.setText("100");
            type.setValue("TECHNICAL");
            venue.setValue(createdVenue);
            Button saveEvent = control(scene, "save-event");
            saveEvent.fire();
            Event createdEvent = new EventDAO().listAll().stream()
                    .filter(e -> e.getName().equals(eventName)).findFirst().orElseThrow();
            eventId = createdEvent.getId();
            require(createdEvent.getExpectedAttendance() == 100, "Event form did not persist attendance");
            require(createdEvent.getVenue().getId() == venueId, "Event form did not persist venue");
            System.out.println("PASS: real New venue and Save event forms persisted valid MySQL records.");
        } finally {
            // Remove only records created by this check; production DAOs need no delete methods.
            try (Connection connection = DatabaseConnection.getConnection()) {
                if (eventId > 0) {
                    try (PreparedStatement statement = connection.prepareStatement("DELETE FROM events WHERE id = ? AND name = ?")) {
                        statement.setInt(1, eventId);
                        statement.setString(2, eventName);
                        statement.executeUpdate();
                    }
                }
                if (venueId > 0) {
                    try (PreparedStatement statement = connection.prepareStatement("DELETE FROM venues WHERE id = ? AND name = ?")) {
                        statement.setInt(1, venueId);
                        statement.setString(2, venueName);
                        statement.executeUpdate();
                    }
                }
            }
            Button refresh = control(scene, "refresh-events");
            refresh.fire();
        }
    }

    private static String controlLabel(Scene scene, String id) {
        Label label = control(scene, id);
        return label.getText();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Node> T control(Scene scene, String id) {
        Node node = scene.lookup("#" + id);
        if (node == null) { throw new AssertionError("Control is missing: " + id); }
        return (T) node;
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
