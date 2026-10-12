package com.eventproof;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventDAO {
    private static final String SELECT_EVENTS = """
            SELECT e.*, v.name AS venue_name, v.capacity, v.backup_power, v.backup_internet
            FROM events e JOIN venues v ON e.venue_id = v.id
            """;

    public int create(Event event) throws SQLException {
        String sql = "INSERT INTO events (name, event_date, expected_attendance, venue_id, event_type) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, event.getName());
            statement.setDate(2, Date.valueOf(event.getEventDate()));
            statement.setInt(3, event.getExpectedAttendance());
            statement.setInt(4, event.getVenue().getId());
            statement.setString(5, event.getEventType());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    event.setId(id);
                    return id;
                }
                throw new SQLException("Event ID was not returned.");
            }
        }
    }

    public List<Event> listAll() throws SQLException {
        List<Event> events = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_EVENTS + " ORDER BY e.id");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) { events.add(readEvent(result)); }
        }
        return events;
    }

    public Optional<Event> findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_EVENTS + " WHERE e.id = ?")) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(readEvent(result)) : Optional.empty();
            }
        }
    }

    private Event readEvent(ResultSet result) throws SQLException {
        Venue venue = new Venue(result.getInt("venue_id"), result.getString("venue_name"), result.getInt("capacity"),
                result.getBoolean("backup_power"), result.getBoolean("backup_internet"));
        int id = result.getInt("id");
        String name = result.getString("name");
        LocalDate date = result.getDate("event_date").toLocalDate();
        int attendance = result.getInt("expected_attendance");
        return switch (result.getString("event_type")) {
            case "TECHNICAL" -> new TechnicalEvent(id, name, date, attendance, venue);
            case "CULTURAL" -> new CulturalEvent(id, name, date, attendance, venue);
            case "SPORTS" -> new SportsEvent(id, name, date, attendance, venue);
            default -> throw new SQLException("Unknown stored event type.");
        };
    }
}
