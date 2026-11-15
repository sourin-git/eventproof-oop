package com.eventproof.report;

import com.eventproof.model.CulturalEvent;
import com.eventproof.model.Event;
import com.eventproof.model.Incident;
import com.eventproof.model.SportsEvent;
import com.eventproof.model.TechnicalEvent;
import com.eventproof.model.Venue;
import com.eventproof.reliability.FailureScenario;
import com.eventproof.reliability.ReliabilityCalculator;
import com.eventproof.reliability.SimulationResult;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class ReliabilityReportExporterTest {
    @TempDir Path directory;
    private final ReliabilityReportExporter exporter = new ReliabilityReportExporter();
    private final ReliabilityCalculator calculator = new ReliabilityCalculator();
    private final Venue venue = new Venue(1, "College Auditorium", 1000, true, true);
    private final Event event = new TechnicalEvent(1, "तकनीकी उत्सव — Café", LocalDate.of(2026, 11, 30), 800, venue);
    private final List<Incident> incidents = List.of(
            new Incident(1, 1, "Internet failure", "Connection lost"),
            new Incident(2, 1, "Power failure", "Brief interruption"));

    private void exportNormal(Path output) throws IOException {
        int score = calculator.calculate(event, incidents);
        exporter.export(output, event, score, calculator.riskLevel(score), null, null);
    }

    private String read(Path output) throws IOException {
        return Files.readString(output, StandardCharsets.UTF_8);
    }

    @Test
    void createsReportFile() throws IOException {
        Path output = directory.resolve("report.txt");
        exportNormal(output);
        assertTrue(Files.isRegularFile(output));
        assertTrue(Files.size(output) > 0);
    }

    @Test
    void preservesEventNameAndUnicodeWithUtf8() throws IOException {
        Path output = directory.resolve("unicode.txt");
        exportNormal(output);
        assertTrue(read(output).contains("Event Name: तकनीकी उत्सव — Café"));
        assertTrue(read(output).contains("EVENTPROOF — RELIABILITY REPORT"));
    }

    @Test
    void includesTheActualTypeForEachEventSubclass() throws IOException {
        Event[] events = {
            event,
            new CulturalEvent(2, "Culture", event.getEventDate(), 600, venue),
            new SportsEvent(3, "Sports", event.getEventDate(), 900, venue)
        };
        for (Event selected : events) {
            Path output = directory.resolve(selected.getEventType() + ".txt");
            int score = calculator.calculate(selected, 0);
            exporter.export(output, selected, score, calculator.riskLevel(score), null, null);
            assertTrue(read(output).contains("Event Type: " + selected.getEventType()));
        }
    }

    @Test
    void includesEventDateVenueCapacityAndAttendance() throws IOException {
        Path output = directory.resolve("details.txt");
        exportNormal(output);
        String report = read(output);
        assertTrue(report.contains("Event Date: 2026-11-30"));
        assertTrue(report.contains("Venue: College Auditorium"));
        assertTrue(report.contains("Venue Capacity: 1000"));
        assertTrue(report.contains("Expected Attendance: 800"));
    }

    @Test
    void usesTheSuppliedCalculatedOriginalScoreAndRisk() throws IOException {
        Path output = directory.resolve("score.txt");
        exportNormal(output);
        assertTrue(read(output).contains("Original Reliability Score: 80 / 100"));
        assertTrue(read(output).contains("Risk Level: LOW"));
    }

    @Test
    void includesActualSimulationScenarioScoreRiskAndRecommendation() throws IOException {
        Path output = directory.resolve("simulation.txt");
        int score = calculator.calculate(event, incidents);
        SimulationResult result = calculator.simulate(event, incidents, FailureScenario.INTERNET_FAILURE);
        exporter.export(output, event, score, calculator.riskLevel(score), FailureScenario.INTERNET_FAILURE, result);
        String report = read(output);
        assertTrue(report.contains("Scenario: INTERNET_FAILURE"));
        assertTrue(report.contains("Original Reliability Score: 80 / 100"));
        assertTrue(report.contains("Simulated Reliability Score: 45 / 100"));
        assertTrue(report.contains("Simulated Risk Level: HIGH"));
        assertTrue(report.contains(result.getRecommendation()));
        assertTrue(event.getVenue().hasBackupInternet());
        assertEquals(800, event.getExpectedAttendance());
    }

    @Test
    void includesCurrentGenerationTimestamp() throws IOException {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Path output = directory.resolve("timestamp.txt");
        exportNormal(output);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        Matcher matcher = Pattern.compile("Report Generated:\\R(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})").matcher(read(output));
        assertTrue(matcher.find());
        LocalDateTime generated = LocalDateTime.parse(matcher.group(1), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        assertFalse(generated.isBefore(before));
        assertFalse(generated.isAfter(after));
    }

    @Test
    void worksWithoutSimulationAndDoesNotInventResults() throws IOException {
        Path output = directory.resolve("normal.txt");
        exportNormal(output);
        String report = read(output);
        assertTrue(report.contains("Scenario: No failure simulation selected."));
        assertTrue(report.contains("Simulated Reliability Score: Not applicable"));
        assertTrue(report.contains("Simulated Risk Level: Not applicable"));
        assertTrue(report.contains("Not applicable (no simulation performed)."));
        assertFalse(report.contains("Scenario: INTERNET_FAILURE"));
    }

    @Test
    void propagatesIoErrorForMissingParentDirectory() {
        Path output = directory.resolve("missing").resolve("report.txt");
        assertThrows(IOException.class, () -> exportNormal(output));
        assertFalse(Files.exists(output));
    }

    @Test
    void propagatesIoErrorWhenOutputIsADirectory() {
        assertThrows(IOException.class, () -> exportNormal(directory));
        assertTrue(Files.isDirectory(directory));
    }

    @Test
    void rejectsMissingOrMismatchedSimulationDataBeforeWriting() {
        Path output = directory.resolve("invalid.txt");
        int score = calculator.calculate(event, incidents);
        SimulationResult result = calculator.simulate(event, incidents, FailureScenario.INTERNET_FAILURE);
        assertThrows(IllegalArgumentException.class,
                () -> exporter.export(output, event, score, calculator.riskLevel(score), FailureScenario.INTERNET_FAILURE, null));
        assertThrows(IllegalArgumentException.class,
                () -> exporter.export(output, event, score, calculator.riskLevel(score), null, result));
        assertThrows(IllegalArgumentException.class,
                () -> exporter.export(output, event, score - 10, calculator.riskLevel(score), FailureScenario.INTERNET_FAILURE, result));
        assertFalse(Files.exists(output));
    }
}
