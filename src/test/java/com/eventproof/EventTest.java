package com.eventproof;

import com.eventproof.model.CulturalEvent;
import com.eventproof.model.Event;
import com.eventproof.model.SportsEvent;
import com.eventproof.model.TechnicalEvent;
import com.eventproof.model.Venue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EventTest {
    private final LocalDate date = LocalDate.of(2026, 11, 15);

    @Test
    void eventReferencesDispatchToDifferentSubclasses() {
        Venue venue = new Venue(1, "College Ground", 200, false, false);
        Event[] events = {
            new TechnicalEvent(1, "Tech Fest", date, 250, venue),
            new CulturalEvent(2, "Cultural Program", date, 250, venue),
            new SportsEvent(3, "Sports Meet", date, 250, venue)
        };
        int[] penalties = new int[events.length];
        for (int i = 0; i < events.length; i++) {
            penalties[i] = events[i].calculateReliabilityPenalty();
        }
        assertArrayEquals(new int[] {35, 20, 25}, penalties);
    }

    @Test
    void technicalEventAssessesEachBackupSeparately() {
        Event powerOnly = new TechnicalEvent(1, "Tech", date, 100,
                new Venue(1, "Hall", 200, true, false));
        Event internetOnly = new TechnicalEvent(2, "Tech", date, 100,
                new Venue(2, "Hall", 200, false, true));
        assertEquals(20, powerOnly.calculateReliabilityPenalty());
        assertEquals(15, internetOnly.calculateReliabilityPenalty());
    }

    @Test
    void adequateFacilitiesAndCapacityHaveNoPenalty() {
        Venue venue = new Venue(1, "Hall", 200, true, true);
        assertEquals(0, new TechnicalEvent(1, "Tech", date, 200, venue)
                .calculateReliabilityPenalty());
        assertEquals(0, new CulturalEvent(2, "Culture", date, 200, venue)
                .calculateReliabilityPenalty());
        Event sports = new SportsEvent(3, "Sports", date, 200, venue);
        assertEquals(0, sports.calculateReliabilityPenalty());
        sports.setExpectedAttendance(201);
        assertEquals(25, sports.calculateReliabilityPenalty());
    }

    @Test
    void invalidAttendanceAndCapacityAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Venue(1, "Hall", 0, false, false));
        Venue venue = new Venue(1, "Hall", 200, false, false);
        assertThrows(IllegalArgumentException.class,
                () -> new SportsEvent(1, "Sports", date, -1, venue));
    }
}
