package com.eventproof;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Read-only live checks; skipped on machines without the ignored local config. */
class DatabaseIntegrationTest {
    @BeforeAll
    static void localConfigurationExists() {
        assumeTrue(Files.exists(Path.of("src/main/resources/database.properties")), "Local MySQL config is not present");
    }

    @Test
    void connectsToTheCorrectDatabaseWithExactlyThreeTables() {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT table_name FROM information_schema.tables
                     WHERE table_schema = DATABASE() ORDER BY table_name
                     """);
             ResultSet result = statement.executeQuery()) {
            assertEquals("eventproof_oop", connection.getCatalog());
            java.util.ArrayList<String> tables = new java.util.ArrayList<>();
            while (result.next()) { tables.add(result.getString(1)); }
            assertEquals(List.of("events", "incidents", "venues"), tables);
        } catch (SQLException exception) { failSafe(exception); }
    }

    @Test
    void readsSampleRecordsAndReconstructsAllThreeSubclasses() {
        try {
            EventDAO events = new EventDAO();
            assertInstanceOf(TechnicalEvent.class, events.findById(1).orElseThrow());
            assertInstanceOf(SportsEvent.class, events.findById(2).orElseThrow());
            assertInstanceOf(CulturalEvent.class, events.findById(3).orElseThrow());
            assertTrue(events.findById(-1).isEmpty());
            assertTrue(events.listAll().size() >= 3);
            assertTrue(new VenueDAO().listAll().size() >= 2);
            assertFalse(new IncidentDAO().listByEventId(1).isEmpty());
        } catch (SQLException exception) { failSafe(exception); }
    }

    private static void failSafe(SQLException exception) {
        // SQL messages are not printed: a misconfigured URL may contain credentials.
        fail("Live MySQL check failed. SQL state=" + exception.getSQLState() + ", code=" + exception.getErrorCode());
    }
}
