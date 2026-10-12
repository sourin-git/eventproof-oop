package com.eventproof;

import java.time.LocalDate;
import java.util.Objects;

public abstract class Event {
    private int id;
    private String name;
    private LocalDate eventDate;
    private int expectedAttendance;
    private Venue venue;

    protected Event(int id, String name, LocalDate eventDate,
                    int expectedAttendance, Venue venue) {
        this.id = id;
        setName(name);
        setEventDate(eventDate);
        setExpectedAttendance(expectedAttendance);
        setVenue(venue);
    }

    /** Returns event-specific penalty points used by ReliabilityCalculator. */
    public abstract int calculateReliabilityPenalty();

    public String getEventType() {
        if (this instanceof TechnicalEvent) { return "TECHNICAL"; }
        if (this instanceof CulturalEvent) { return "CULTURAL"; }
        if (this instanceof SportsEvent) { return "SPORTS"; }
        throw new IllegalArgumentException("Unsupported event type");
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) {
        Objects.requireNonNull(name, "name");
        if (name.isBlank() || name.trim().length() > 150) {
            throw new IllegalArgumentException("Event name must contain 1 to 150 characters");
        }
        this.name = name.trim();
    }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) {
        this.eventDate = Objects.requireNonNull(eventDate, "eventDate");
    }
    public int getExpectedAttendance() { return expectedAttendance; }
    public void setExpectedAttendance(int expectedAttendance) {
        if (expectedAttendance < 0) {
            throw new IllegalArgumentException("Expected attendance cannot be negative");
        }
        this.expectedAttendance = expectedAttendance;
    }
    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = Objects.requireNonNull(venue, "venue"); }

    @Override
    public String toString() { return name; }
}
