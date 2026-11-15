package com.eventproof;

import com.eventproof.model.CulturalEvent;
import com.eventproof.model.ReliabilityAssessable;
import com.eventproof.model.SportsEvent;
import com.eventproof.model.TechnicalEvent;
import com.eventproof.model.Venue;

import java.time.LocalDate;

/** Imports model classes and calls their shared contract through interface references. */
public class PolymorphismDemo {
    public static void main(String[] args) {
        Venue venue = new Venue(1, "College Ground", 200, false, false);
        LocalDate date = LocalDate.of(2026, 11, 15);
        ReliabilityAssessable[] events = {
            new TechnicalEvent(1, "Technical Fest", date, 250, venue),
            new CulturalEvent(2, "Cultural Program", date, 250, venue),
            new SportsEvent(3, "Sports Meet", date, 250, venue)
        };

        for (ReliabilityAssessable event : events) {
            System.out.println(event.getClass().getSimpleName() + ": "
                    + event.calculateReliabilityPenalty() + " penalty points");
        }
    }
}
