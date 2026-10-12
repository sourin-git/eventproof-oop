package com.eventproof;

import java.time.LocalDate;

public class SportsEvent extends Event {
    public SportsEvent(int id, String name, LocalDate eventDate,
                       int expectedAttendance, Venue venue) {
        super(id, name, eventDate, expectedAttendance, venue);
    }

    @Override
    public int calculateReliabilityPenalty() {
        // This basic assessment focuses on crowd capacity at a sports meet.
        return getExpectedAttendance() > getVenue().getCapacity() ? 25 : 0;
    }
}
