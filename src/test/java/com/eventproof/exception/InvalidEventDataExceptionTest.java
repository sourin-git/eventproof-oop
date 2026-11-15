package com.eventproof.exception;

import com.eventproof.model.CulturalEvent;
import com.eventproof.model.Event;
import com.eventproof.model.SportsEvent;
import com.eventproof.model.TechnicalEvent;
import com.eventproof.model.Venue;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InvalidEventDataExceptionTest {
    private final Venue venue = new Venue(1, "Hall", 200, true, true);
    private final LocalDate date = LocalDate.of(2026, 11, 30);

    @Test
    void blankEventNameThrowsCustomExceptionWithReadableMessage() {
        InvalidEventDataException error = assertThrows(InvalidEventDataException.class,
                () -> new TechnicalEvent(1, "  ", date, 100, venue));
        assertEquals("Event name cannot be empty.", error.getMessage());
        Event event = new CulturalEvent(2, "Culture", date, 100, venue);
        assertThrows(InvalidEventDataException.class, () -> event.setName(""));
        assertEquals("Culture", event.getName());
    }

    @Test
    void missingNameOrDateUsesCustomExceptionWithoutChangingRejectionRules() {
        assertThrows(InvalidEventDataException.class,
                () -> new TechnicalEvent(1, null, date, 100, venue));
        InvalidEventDataException error = assertThrows(InvalidEventDataException.class,
                () -> new CulturalEvent(2, "Culture", null, 100, venue));
        assertEquals("Event date cannot be empty.", error.getMessage());
    }

    @Test
    void existingNameLengthLimitUsesCustomException() {
        assertThrows(InvalidEventDataException.class,
                () -> new SportsEvent(1, "X".repeat(151), date, 100, venue));
    }

    @Test
    void negativeAttendanceThrowsCustomExceptionAndRetainsPreviousValue() {
        InvalidEventDataException error = assertThrows(InvalidEventDataException.class,
                () -> new SportsEvent(1, "Sports", date, -1, venue));
        assertEquals("Expected attendance cannot be negative.", error.getMessage());
        Event event = new TechnicalEvent(2, "Tech", date, 100, venue);
        assertThrows(InvalidEventDataException.class, () -> event.setExpectedAttendance(-1));
        assertEquals(100, event.getExpectedAttendance());
    }

    @Test
    void validDataStillWorksWithoutAddingDateOrAttendanceRestrictions() {
        assertDoesNotThrow(() -> new TechnicalEvent(1, "X".repeat(150), date, 0, venue));
        assertDoesNotThrow(() -> new CulturalEvent(2, "Culture", LocalDate.of(2020, 1, 1), 100, venue));
        Event sports = assertDoesNotThrow(() -> new SportsEvent(3, " Sports ", date, 300, venue));
        assertEquals("Sports", sports.getName());
        assertEquals(300, sports.getExpectedAttendance());
    }
}
