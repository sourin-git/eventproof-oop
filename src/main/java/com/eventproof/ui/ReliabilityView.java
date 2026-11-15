package com.eventproof.ui;

import com.eventproof.dao.EventDAO;
import com.eventproof.dao.IncidentDAO;
import com.eventproof.model.Event;
import com.eventproof.model.Incident;
import com.eventproof.reliability.FailureScenario;
import com.eventproof.reliability.ReliabilityCalculator;
import com.eventproof.reliability.RiskLevel;
import com.eventproof.reliability.SimulationResult;
import com.eventproof.report.ReliabilityReportExporter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

public class ReliabilityView extends VBox {
    private final ComboBox<Event> event = new ComboBox<>();
    private final ComboBox<FailureScenario> scenario = new ComboBox<>(FXCollections.observableArrayList(FailureScenario.values()));
    private final Label details = new Label();
    private final Label original = new Label("Original Score: —");
    private final Label simulated = new Label("Simulated Score: —");
    private final Label risk = new Label("Risk Level: —");
    private final Label simulatedRisk = new Label("Simulated Risk Level: —");
    private final Label recommendation = new Label("Select an event and calculate reliability.");
    private final ReliabilityCalculator calculator = new ReliabilityCalculator();
    private final Button export = new Button("Export Report");
    private Event reportEvent;
    private int reportScore;
    private RiskLevel reportRisk;
    private FailureScenario reportScenario;
    private SimulationResult reportSimulation;

    public ReliabilityView() {
        setPadding(new Insets(24));
        setSpacing(20);
        event.setId("reliability-event");
        scenario.setId("failure-scenario");
        details.setId("reliability-details");
        original.setId("original-score");
        simulated.setId("simulated-score");
        risk.setId("risk-level");
        simulatedRisk.setId("simulated-risk-level");
        recommendation.setId("recommendation");
        event.setPrefWidth(380);
        scenario.getSelectionModel().select(FailureScenario.NORMAL);
        event.valueProperty().addListener((observable, oldValue, newValue) -> reset());
        scenario.valueProperty().addListener((observable, oldValue, newValue) -> {
            reportScenario = null;
            reportSimulation = null;
            simulated.setText("Simulated Score: —");
            simulatedRisk.setText("Simulated Risk Level: —");
            recommendation.setText("Run the selected scenario to refresh the comparison.");
        });
        Button calculate = new Button("Calculate reliability");
        calculate.setId("calculate-reliability");
        calculate.setOnAction(action -> calculate(false));
        export.setId("export-report");
        export.setDisable(true);
        export.setOnAction(action -> exportReport());
        Button simulate = new Button("Simulate failure");
        simulate.setId("simulate-failure");
        simulate.setOnAction(action -> calculate(true));
        VBox scores = new VBox(16, original, simulated, risk, simulatedRisk, recommendation);
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
        getChildren().addAll(new HBox(12, new Label("Event"), event), details, new HBox(12, calculate, export), new Separator(),
                new HBox(12, new Label("Scenario"), scenario, simulate), scores, rules, note);
    }

    public void setEvents(List<Event> events) {
        int id = event.getValue() == null ? -1 : event.getValue().getId();
        event.getItems().setAll(events);
        event.setValue(events.stream().filter(e -> e.getId() == id).findFirst()
                .orElse(events.isEmpty() ? null : events.getFirst()));
    }

    private void reset() {
        invalidateReport();
        Event selected = event.getValue();
        details.setText(selected == null ? "No event selected." : selected.getEventType() + " | "
                + selected.getVenue().getName() + " | Attendance: " + selected.getExpectedAttendance()
                + " / capacity " + selected.getVenue().getCapacity() + " | Date: " + selected.getEventDate());
        original.setText("Original Score: —");
        simulated.setText("Simulated Score: —");
        risk.setText("Risk Level: —");
        simulatedRisk.setText("Simulated Risk Level: —");
        recommendation.setText("Select an event and calculate reliability.");
    }

    private void calculate(boolean simulate) {
        invalidateReport();
        if (event.getValue() == null) { UiMessages.inputError("Select an event first."); return; }
        try {
            // Read fresh records so newly saved incidents affect the next calculation.
            Event selected = new EventDAO().findById(event.getValue().getId())
                    .orElseThrow(() -> new IllegalArgumentException("This event no longer exists. Refresh the events list."));
            List<Incident> incidents = new IncidentDAO().listByEventId(selected.getId());
            int score = calculator.calculate(selected, incidents);
            SimulationResult result = null;
            original.setText("Original Score: " + score + " / 100");
            risk.setText("Risk Level: " + calculator.riskLevel(score));
            if (simulate) {
                result = calculator.simulate(selected, incidents, scenario.getValue());
                simulated.setText("Simulated Score: " + result.getSimulatedScore() + " / 100");
                simulatedRisk.setText("Simulated Risk Level: " + result.getRiskLevel());
                recommendation.setText(result.getRecommendation());
            } else {
                simulated.setText("Simulated Score: —");
                simulatedRisk.setText("Simulated Risk Level: —");
                recommendation.setText("Recorded incidents: " + incidents.size()
                        + ". Review incidents, check backups, and keep attendance within capacity.");
            }
            reportEvent = selected;
            reportScore = score;
            reportRisk = calculator.riskLevel(score);
            reportScenario = simulate ? scenario.getValue() : null;
            reportSimulation = result;
            export.setDisable(false);
        } catch (SQLException exception) { UiMessages.databaseError(exception); }
        catch (IllegalArgumentException exception) { UiMessages.inputError(exception.getMessage()); }
    }

    private void invalidateReport() {
        reportEvent = null;
        reportRisk = null;
        reportScenario = null;
        reportSimulation = null;
        export.setDisable(true);
    }

    private void exportReport() {
        if (reportEvent == null) { return; }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Reliability Report");
        chooser.setInitialFileName("EventProof_Reliability_Report.txt");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt"));
        try {
            File file = chooser.showSaveDialog(getScene().getWindow());
            saveReport(file == null ? null : file.toPath());
        } catch (SecurityException exception) {
            showExportError();
        }
    }

    /** A cancelled chooser supplies null; saving and its error handling stay together. */
    private void saveReport(Path output) {
        if (output == null || reportEvent == null) { return; }
        try {
            if (!output.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".txt")) {
                output = Path.of(output.toString() + ".txt");
            }
            new ReliabilityReportExporter().export(output, reportEvent, reportScore, reportRisk,
                    reportScenario, reportSimulation);
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.initOwner(getScene().getWindow());
            success.setTitle("Report exported");
            success.setHeaderText("Reliability report saved");
            success.setContentText(output.toAbsolutePath().toString());
            success.showAndWait();
        } catch (IOException | SecurityException exception) {
            showExportError();
        }
    }

    private void showExportError() {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.initOwner(getScene().getWindow());
        error.setTitle("Export failed");
        error.setHeaderText("Could not save the report");
        error.setContentText("Choose a writable folder and try again. The destination may be unavailable or read-only.");
        error.showAndWait();
    }
}
