package com.eventproof;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReliabilityCalculatorTest {
    private final ReliabilityCalculator calculator = new ReliabilityCalculator();
    private final LocalDate date = LocalDate.of(2026, 11, 15);
    private final Venue ready = new Venue(1, "Auditorium", 1000, true, true);

    @Test
    void readyEventStartsAt100AndIncidentsReduceScore() {
        Event event = new TechnicalEvent(1, "Tech Fest", date, 800, ready);
        assertEquals(100, calculator.calculate(event, 0));
        assertEquals(90, calculator.calculate(event, List.of(new Incident(1, 1, "Internet failure", "Interrupted"))));
        assertEquals(100, calculator.calculate(event, List.of(new Incident(2, 2, "Other event", "Ignored"))));
    }

    @Test
    void subclassPenaltiesAffectFinalScores() {
        Venue unprepared = new Venue(1, "Ground", 200, false, false);
        Event[] events = {
            new TechnicalEvent(1, "Tech", date, 250, unprepared),
            new CulturalEvent(2, "Culture", date, 250, unprepared),
            new SportsEvent(3, "Sports", date, 250, unprepared)
        };
        assertEquals(35, calculator.calculate(events[0], 0));
        assertEquals(50, calculator.calculate(events[1], 0));
        assertEquals(45, calculator.calculate(events[2], 0));
    }

    @Test
    void scoresClampAtZeroAndRiskThresholdsAreExplicit() {
        Event event = new TechnicalEvent(1, "Tech", date, 800, ready);
        assertEquals(0, calculator.calculate(event, Integer.MAX_VALUE));
        assertEquals(RiskLevel.LOW, calculator.riskLevel(100));
        assertEquals(RiskLevel.LOW, calculator.riskLevel(80));
        assertEquals(RiskLevel.MEDIUM, calculator.riskLevel(79));
        assertEquals(RiskLevel.MEDIUM, calculator.riskLevel(50));
        assertEquals(RiskLevel.HIGH, calculator.riskLevel(49));
        assertEquals(RiskLevel.HIGH, calculator.riskLevel(0));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(event, -1));
    }

    @Test
    void normalSimulationKeepsScoreAndDoesNotChangeObjects() {
        Event event = new TechnicalEvent(1, "Tech", date, 800, ready);
        SimulationResult result = calculator.simulate(event, List.of(), FailureScenario.NORMAL);
        assertEquals(100, result.getOriginalScore());
        assertEquals(100, result.getSimulatedScore());
        assertSame(ready, event.getVenue());
    }

    @Test
    void internetFailureHasGreaterImpactOnTechnicalEvents() {
        Event technical = new TechnicalEvent(1, "Tech", date, 800, ready);
        Event cultural = new CulturalEvent(2, "Culture", date, 800, ready);
        SimulationResult tech = calculator.simulate(technical, List.of(), FailureScenario.INTERNET_FAILURE);
        SimulationResult culture = calculator.simulate(cultural, List.of(), FailureScenario.INTERNET_FAILURE);
        assertEquals(65, tech.getSimulatedScore());
        assertEquals(85, culture.getSimulatedScore());
        assertTrue(technical.getVenue().hasBackupInternet());
        assertEquals(800, technical.getExpectedAttendance());
        assertFalse(tech.getRecommendation().isBlank());
    }

    @Test
    void powerAndOvercrowdingUseActualRules() {
        Event cultural = new CulturalEvent(1, "Culture", date, 800, ready);
        Event sports = new SportsEvent(2, "Sports", date, 800, ready);
        assertEquals(65, calculator.simulate(cultural, List.of(), FailureScenario.POWER_FAILURE).getSimulatedScore());
        SimulationResult crowd = calculator.simulate(sports, List.of(), FailureScenario.OVERCROWDING);
        assertEquals(45, crowd.getSimulatedScore());
        assertEquals(RiskLevel.HIGH, crowd.getRiskLevel());
        assertEquals(800, sports.getExpectedAttendance());
    }
}
