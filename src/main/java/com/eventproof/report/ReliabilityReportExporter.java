package com.eventproof.report;

import com.eventproof.model.Event;
import com.eventproof.reliability.FailureScenario;
import com.eventproof.reliability.RiskLevel;
import com.eventproof.reliability.SimulationResult;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/** Formats supplied results only; no database, JavaFX, or scoring logic. */
public class ReliabilityReportExporter {
    /** Pass null for both scenario and simulation when no simulation was performed.
     * File errors propagate to the caller so the UI can display a readable message. */
    public void export(Path output, Event event, int originalScore, RiskLevel originalRisk,
                       FailureScenario scenario, SimulationResult simulation) throws IOException {
        Objects.requireNonNull(output, "output");
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(originalRisk, "originalRisk");
        if (originalScore < 0 || originalScore > 100) {
            throw new IllegalArgumentException("Score must be from 0 to 100");
        }
        if ((scenario == null) != (simulation == null)) {
            throw new IllegalArgumentException("Supply both the scenario and its simulation result, or neither");
        }
        if (simulation != null && simulation.getOriginalScore() != originalScore) {
            throw new IllegalArgumentException("Simulation must belong to the supplied original score");
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        List<String> lines = List.of(
                "========================================",
                "EVENTPROOF — RELIABILITY REPORT",
                "========================================", "",
                "Event Name: " + event.getName(),
                "Event Type: " + event.getEventType(),
                "Event Date: " + event.getEventDate(), "",
                "Venue: " + event.getVenue().getName(),
                "Venue Capacity: " + event.getVenue().getCapacity(),
                "Expected Attendance: " + event.getExpectedAttendance(), "",
                "----------------------------------------",
                "RELIABILITY ANALYSIS",
                "----------------------------------------", "",
                "Original Reliability Score: " + originalScore + " / 100",
                "Risk Level: " + originalRisk, "",
                "----------------------------------------",
                "FAILURE SIMULATION",
                "----------------------------------------", "",
                "Scenario: " + (simulation == null ? "No failure simulation selected." : scenario),
                "Simulated Reliability Score: " + (simulation == null ? "Not applicable" : simulation.getSimulatedScore() + " / 100"),
                "Simulated Risk Level: " + (simulation == null ? "Not applicable" : simulation.getRiskLevel()), "",
                "Recommendation:",
                simulation == null ? "Not applicable (no simulation performed)." : simulation.getRecommendation(), "",
                "Rule-based classroom estimate, not a real-world safety prediction.",
                "----------------------------------------", "",
                "Report Generated:", timestamp, "",
                "========================================");
        Files.writeString(output, String.join(System.lineSeparator(), lines) + System.lineSeparator(),
                StandardCharsets.UTF_8);
    }
}
