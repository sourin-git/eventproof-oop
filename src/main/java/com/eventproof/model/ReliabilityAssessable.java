package com.eventproof.model;

/** Common contract for an event's type-specific reliability penalty. */
public interface ReliabilityAssessable {
    int calculateReliabilityPenalty();
}
