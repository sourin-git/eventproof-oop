package com.eventproof;

import java.util.List;
import java.util.Objects;

/** Deterministic classroom rules, not a real-world safety prediction. */
public class ReliabilityCalculator {
    public int calculate(Event event, List<Incident> incidents) {
        int count = 0;
        for (Incident incident : incidents) {
            if (incident.getEventId() == event.getId()) { count++; }
        }
        return calculate(event, count);
    }

    public int calculate(Event event, int incidentCount) {
        Objects.requireNonNull(event, "event");
        if (incidentCount < 0) { throw new IllegalArgumentException("Incident count cannot be negative"); }
        int penalty = event.calculateReliabilityPenalty(); // Runtime polymorphism.
        if (event.getExpectedAttendance() > event.getVenue().getCapacity()) { penalty += 20; }
        if (!event.getVenue().hasBackupPower()) { penalty += 5; }
        if (!event.getVenue().hasBackupInternet()) { penalty += 5; }
        // Ten incidents already exhaust the 100-point starting score; avoid overflow.
        penalty += Math.min(incidentCount, 10) * 10;
        return Math.max(0, Math.min(100, 100 - penalty));
    }

    public RiskLevel riskLevel(int score) {
        if (score < 0 || score > 100) { throw new IllegalArgumentException("Score must be from 0 to 100"); }
        if (score >= 80) { return RiskLevel.LOW; }
        if (score >= 50) { return RiskLevel.MEDIUM; }
        return RiskLevel.HIGH;
    }

    public SimulationResult simulate(Event event, List<Incident> incidents, FailureScenario scenario) {
        Objects.requireNonNull(scenario, "scenario");
        int original = calculate(event, incidents);
        if (scenario == FailureScenario.NORMAL) {
            return new SimulationResult(original, original, riskLevel(original),
                    "Maintain backups, review recorded incidents, and monitor attendance.");
        }

        Venue venue = event.getVenue();
        boolean power = venue.hasBackupPower();
        boolean internet = venue.hasBackupInternet();
        int attendance = event.getExpectedAttendance();
        String recommendation;
        switch (scenario) {
            case INTERNET_FAILURE -> {
                internet = false;
                recommendation = "Arrange an independent internet backup and prepare offline activities.";
            }
            case POWER_FAILURE -> {
                power = false;
                recommendation = "Arrange backup power and check essential equipment before the event.";
            }
            case OVERCROWDING -> {
                long crowd = (long) venue.getCapacity() + Math.max(1, (venue.getCapacity() + 4L) / 5);
                if (crowd > Integer.MAX_VALUE) { throw new IllegalArgumentException("Capacity is too large to simulate overcrowding"); }
                attendance = Math.max(attendance, (int) crowd);
                recommendation = "Limit entry to venue capacity and assign crowd-management volunteers.";
            }
            default -> throw new IllegalArgumentException("Unsupported scenario");
        }

        // New objects only: no changes to the original Event, Venue or database.
        Venue simulatedVenue = new Venue(venue.getId(), venue.getName(), venue.getCapacity(), power, internet);
        Event simulated = switch (event.getEventType()) {
            case "TECHNICAL" -> new TechnicalEvent(event.getId(), event.getName(), event.getEventDate(), attendance, simulatedVenue);
            case "CULTURAL" -> new CulturalEvent(event.getId(), event.getName(), event.getEventDate(), attendance, simulatedVenue);
            case "SPORTS" -> new SportsEvent(event.getId(), event.getName(), event.getEventDate(), attendance, simulatedVenue);
            default -> throw new IllegalArgumentException("Unsupported event type");
        };
        int recordedCount = 0;
        for (Incident incident : incidents) {
            if (incident.getEventId() == event.getId()) { recordedCount++; }
        }
        int score = calculate(simulated, Math.min(recordedCount, 10) + 1);
        return new SimulationResult(original, score, riskLevel(score), recommendation);
    }
}
