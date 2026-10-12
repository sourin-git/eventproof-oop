package com.eventproof;

import java.sql.SQLException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ReliabilityView extends VBox {
    private final ComboBox<Event> event = new ComboBox<>();
    private final ComboBox<FailureScenario> scenario = new ComboBox<>(FXCollections.observableArrayList(FailureScenario.values()));
    private final Label details = new Label();
    private final Label original = new Label("Original Score: —");
    private final Label simulated = new Label("Simulated Score: —");
    private final Label risk = new Label("Risk Level: —");
    private final Label recommendation = new Label("Select an event and calculate reliability.");
    private final ReliabilityCalculator calculator = new ReliabilityCalculator();

    public ReliabilityView() {
        setPadding(new Insets(24));
        setSpacing(20);
        event.setId("reliability-event");
        scenario.setId("failure-scenario");
        details.setId("reliability-details");
        original.setId("original-score");
        simulated.setId("simulated-score");
        risk.setId("risk-level");
        recommendation.setId("recommendation");
        event.setPrefWidth(380);
        scenario.getSelectionModel().select(FailureScenario.NORMAL);
        event.valueProperty().addListener((observable, oldValue, newValue) -> reset());
        scenario.valueProperty().addListener((observable, oldValue, newValue) -> {
            simulated.setText("Simulated Score: —");
            risk.setText("Risk Level: —");
            recommendation.setText("Run the selected scenario to refresh the comparison.");
        });
        Button calculate = new Button("Calculate reliability");
        calculate.setId("calculate-reliability");
        calculate.setOnAction(action -> calculate(false));
        Button simulate = new Button("Simulate failure");
        simulate.setId("simulate-failure");
        simulate.setOnAction(action -> calculate(true));
        VBox scores = new VBox(16, original, simulated, risk, recommendation);
        scores.getStyleClass().add("score-card");
        original.getStyleClass().add("score");
        simulated.getStyleClass().add("score");
        recommendation.setWrapText(true);
        details.setWrapText(true);
        Label rules = new Label("Start at 100. Common penalties: over capacity 20, no backup power 5, no backup internet 5, "
                + "each recorded incident 10. Event-specific penalties are added. Low risk: 80–100; medium: 50–79; high: 0–49.");
        rules.setWrapText(true);
        Label note = new Label("Rule-based classroom estimate, not a real-world safety prediction. Simulations do not save changes."
                + "\nFailures assume the corresponding backup is unavailable; overcrowding assumes at least 120% of capacity."
                + " Each failure adds one hypothetical incident.");
        note.setWrapText(true);
        getChildren().addAll(new HBox(12, new Label("Event"), event), details, calculate, new Separator(),
                new HBox(12, new Label("Scenario"), scenario, simulate), scores, rules, note);
    }

    public void setEvents(List<Event> events) {
        int id = event.getValue() == null ? -1 : event.getValue().getId();
        event.getItems().setAll(events);
        event.setValue(events.stream().filter(e -> e.getId() == id).findFirst()
                .orElse(events.isEmpty() ? null : events.getFirst()));
    }

    private void reset() {
        Event selected = event.getValue();
        details.setText(selected == null ? "No event selected." : selected.getEventType() + " | "
                + selected.getVenue().getName() + " | Attendance: " + selected.getExpectedAttendance()
                + " / capacity " + selected.getVenue().getCapacity() + " | Date: " + selected.getEventDate());
        original.setText("Original Score: —");
        simulated.setText("Simulated Score: —");
        risk.setText("Risk Level: —");
        recommendation.setText("Select an event and calculate reliability.");
    }

    private void calculate(boolean simulate) {
        if (event.getValue() == null) { UiMessages.inputError("Select an event first."); return; }
        try {
            // Read fresh records so newly saved incidents affect the next calculation.
            Event selected = new EventDAO().findById(event.getValue().getId())
                    .orElseThrow(() -> new IllegalArgumentException("This event no longer exists. Refresh the events list."));
            List<Incident> incidents = new IncidentDAO().listByEventId(selected.getId());
            int score = calculator.calculate(selected, incidents);
            original.setText("Original Score: " + score + " / 100");
            if (simulate) {
                SimulationResult result = calculator.simulate(selected, incidents, scenario.getValue());
                simulated.setText("Simulated Score: " + result.getSimulatedScore() + " / 100");
                risk.setText("Risk Level: " + result.getRiskLevel() + " (simulated)");
                recommendation.setText(result.getRecommendation());
            } else {
                simulated.setText("Simulated Score: —");
                risk.setText("Risk Level: " + calculator.riskLevel(score));
                recommendation.setText("Recorded incidents: " + incidents.size()
                        + ". Review incidents, check backups, and keep attendance within capacity.");
            }
        } catch (SQLException exception) { UiMessages.databaseError(exception); }
        catch (IllegalArgumentException exception) { UiMessages.inputError(exception.getMessage()); }
    }
}
