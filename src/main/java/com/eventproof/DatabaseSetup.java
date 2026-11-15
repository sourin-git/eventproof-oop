package com.eventproof;

import com.eventproof.config.DatabaseConnection;
import com.eventproof.dao.EventDAO;
import com.eventproof.model.Event;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Explicit setup command: imports schema and repeatable samples, then runs SELECTs. */
public class DatabaseSetup {
    public static void initialize() throws SQLException, IOException {
        try (Connection connection = DatabaseConnection.getServerConnection()) {
            executeScript(connection, "/db/schema.sql");
            executeScript(connection, "/db/sample-data.sql");
        }
    }

    private static void executeScript(Connection connection, String resource) throws SQLException, IOException {
        try (InputStream input = DatabaseSetup.class.getResourceAsStream(resource)) {
            if (input == null) { throw new IOException("SQL resource is missing."); }
            String sql = new String(input.readAllBytes(), StandardCharsets.UTF_8).replaceAll("(?m)^\\s*--.*$", "");
            // These scripts contain no procedures or semicolons inside strings.
            for (String command : sql.split(";")) {
                if (!command.isBlank()) {
                    try (Statement statement = connection.createStatement()) { statement.execute(command.trim()); }
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            initialize();
            try (Connection connection = DatabaseConnection.getConnection(); Statement statement = connection.createStatement()) {
                try (ResultSet result = statement.executeQuery("SELECT DATABASE()")) {
                    result.next();
                    System.out.println("Database: " + result.getString(1));
                }
                for (String table : new String[] {"venues", "events", "incidents"}) {
                    try (ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
                        result.next();
                        System.out.println(table + ": " + result.getInt(1));
                    }
                }
            }
            for (Event event : new EventDAO().listAll()) {
                System.out.println(event.getId() + " | " + event.getName() + " | " + event.getClass().getSimpleName()
                        + " | " + event.getVenue().getName() + " | attendees=" + event.getExpectedAttendance());
            }
        } catch (SQLException exception) {
            System.err.println("Database setup failed. Check MySQL, local credentials and CREATE privileges. SQL state: "
                    + exception.getSQLState() + "; error code: " + exception.getErrorCode());
            System.exit(1);
        } catch (IOException exception) {
            System.err.println("Database setup failed: SQL resources could not be read.");
            System.exit(1);
        }
    }
}
