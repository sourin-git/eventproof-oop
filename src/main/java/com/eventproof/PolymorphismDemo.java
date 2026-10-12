package com.eventproof;

import java.time.LocalDate;

/** Run this console example to see calls dispatched to the actual event subtype. */
public class PolymorphismDemo {
    public static void main(String[] args) {
        Venue venue = new Venue(1, "College Ground", 200, false, false);
        LocalDate date = LocalDate.of(2026, 11, 15);
        Event[] events = {
            new TechnicalEvent(1, "Technical Fest", date, 250, venue),
            new CulturalEvent(2, "Cultural Program", date, 250, venue),
            new SportsEvent(3, "Sports Meet", date, 250, venue)
        };

        for (Event event : events) {
            System.out.println(event.getName() + ": "
                    + event.calculateReliabilityPenalty() + " penalty points");
        }
    }
}
