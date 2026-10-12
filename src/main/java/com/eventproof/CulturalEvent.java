package com.eventproof;

import java.time.LocalDate;

public class CulturalEvent extends Event {
    public CulturalEvent(int id, String name, LocalDate eventDate,
                         int expectedAttendance, Venue venue) {
        super(id, name, eventDate, expectedAttendance, venue);
    }

    @Override
    public int calculateReliabilityPenalty() {
        // Lighting and sound need backup power; internet is not essential here.
        return getVenue().hasBackupPower() ? 0 : 20;
    }
}
