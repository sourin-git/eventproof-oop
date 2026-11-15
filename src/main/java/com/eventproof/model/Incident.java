package com.eventproof.model;

import java.util.Objects;

public class Incident {
    private final int id;
    private final int eventId;
    private final String incidentType;
    private final String description;

    public Incident(int id, int eventId, String incidentType, String description) {
        this.id = id;
        this.eventId = eventId;
        Objects.requireNonNull(incidentType, "incidentType");
        Objects.requireNonNull(description, "description");
        if (incidentType.isBlank() || incidentType.trim().length() > 50 || description.isBlank()) {
            throw new IllegalArgumentException("Enter an incident type (1 to 50 characters) and description");
        }
        this.incidentType = incidentType.trim();
        this.description = description.trim();
    }

    public int getId() { return id; }
    public int getEventId() { return eventId; }
    public String getIncidentType() { return incidentType; }
    public String getDescription() { return description; }
}
