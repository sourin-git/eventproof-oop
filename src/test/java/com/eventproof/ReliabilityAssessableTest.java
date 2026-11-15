package com.eventproof;

import com.eventproof.model.CulturalEvent;
import com.eventproof.model.ReliabilityAssessable;
import com.eventproof.model.SportsEvent;
import com.eventproof.model.TechnicalEvent;
import com.eventproof.model.Venue;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReliabilityAssessableTest {
    private final LocalDate date = LocalDate.of(2026, 11, 15);

    @Test
    void interfaceReferencesDispatchToAllThreeOverridesWithOriginalPenalties() {
        Venue venue = new Venue(1, "College Ground", 200, false, false);
        ReliabilityAssessable[] assessments = {
            new TechnicalEvent(1, "Tech Fest", date, 250, venue),
            new CulturalEvent(2, "Cultural Program", date, 250, venue),
            new SportsEvent(3, "Sports Meet", date, 250, venue)
        };
        int[] penalties = new int[assessments.length];
        for (int i = 0; i < assessments.length; i++) {
            penalties[i] = assessments[i].calculateReliabilityPenalty();
        }
        assertArrayEquals(new int[] {35, 20, 25}, penalties);
    }

    @Test
    void interfaceContractPreservesZeroPenaltyWhenFacilitiesAndCapacityAreAdequate() {
        Venue venue = new Venue(1, "Hall", 200, true, true);
        ReliabilityAssessable[] assessments = {
            new TechnicalEvent(1, "Tech Fest", date, 200, venue),
            new CulturalEvent(2, "Cultural Program", date, 200, venue),
            new SportsEvent(3, "Sports Meet", date, 200, venue)
        };
        for (ReliabilityAssessable assessment : assessments) {
            assertEquals(0, assessment.calculateReliabilityPenalty());
        }
    }
}
