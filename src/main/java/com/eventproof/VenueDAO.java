package com.eventproof;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VenueDAO {
    public int create(Venue venue) throws SQLException {
        String sql = "INSERT INTO venues (name, capacity, backup_power, backup_internet) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, venue.getName());
            statement.setInt(2, venue.getCapacity());
            statement.setBoolean(3, venue.hasBackupPower());
            statement.setBoolean(4, venue.hasBackupInternet());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) { return keys.getInt(1); }
                throw new SQLException("Venue ID was not returned.");
            }
        }
    }

    public List<Venue> listAll() throws SQLException {
        List<Venue> venues = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM venues ORDER BY id");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                venues.add(new Venue(result.getInt("id"), result.getString("name"), result.getInt("capacity"),
                        result.getBoolean("backup_power"), result.getBoolean("backup_internet")));
            }
        }
        return venues;
    }
}
