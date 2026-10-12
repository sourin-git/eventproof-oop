package com.eventproof;

import java.time.LocalDate;

public class TechnicalEvent extends Event {
    public TechnicalEvent(int id, String name, LocalDate eventDate,
                          int expectedAttendance, Venue venue) {
        super(id, name, eventDate, expectedAttendance, venue);
    }

    @Override
    public int calculateReliabilityPenalty() {
        // Technical sessions rely on both powered equipment and internet access.
        int penalty = 0;
        if (!getVenue().hasBackupPower()) { penalty += 15; }
        if (!getVenue().hasBackupInternet()) { penalty += 20; }
        return penalty;
    }
}
