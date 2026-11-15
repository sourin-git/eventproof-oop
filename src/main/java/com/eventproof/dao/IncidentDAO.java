package com.eventproof.dao;

import com.eventproof.config.DatabaseConnection;
import com.eventproof.model.Incident;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IncidentDAO {
    public int create(Incident incident) throws SQLException {
        String sql = "INSERT INTO incidents (event_id, incident_type, description) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, incident.getEventId());
            statement.setString(2, incident.getIncidentType());
            statement.setString(3, incident.getDescription());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) { return keys.getInt(1); }
                throw new SQLException("Incident ID was not returned.");
            }
        }
    }

    public List<Incident> listByEventId(int eventId) throws SQLException {
        List<Incident> incidents = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM incidents WHERE event_id = ? ORDER BY id")) {
            statement.setInt(1, eventId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    incidents.add(new Incident(result.getInt("id"), result.getInt("event_id"),
                            result.getString("incident_type"), result.getString("description")));
                }
            }
        }
        return incidents;
    }
}
