package com.eventproof;

import com.eventproof.exception.InvalidEventDataException;
import com.eventproof.model.Event;
import com.eventproof.model.TechnicalEvent;
import com.eventproof.model.Venue;
import java.time.LocalDate;

/** A console-only example; no JavaFX window or database connection is required. */
public class ExceptionHandlingDemo {
    public static void main(String[] args) {
        Venue venue = new Venue(1, "College Hall", 500, true, true);
        demonstrate("", venue);
        demonstrate("College Tech Fest", venue);
    }

    private static void demonstrate(String name, Venue venue) {
        try {
            Event event = new TechnicalEvent(1, name, LocalDate.of(2026, 11, 30), 400, venue);
            System.out.println("Valid event: " + event.getName());
        } catch (InvalidEventDataException exception) {
            System.out.println("Invalid event data: " + exception.getMessage());
        } finally {
            System.out.println("Validation attempt finished.");
        }
    }
}
