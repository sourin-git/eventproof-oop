package com.eventproof.config;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    public static Connection getConnection() throws SQLException { return open(false); }
    public static Connection getServerConnection() throws SQLException { return open(true); }

    private static Connection open(boolean serverOnly) throws SQLException {
        try {
            DatabaseConfig config = DatabaseConfig.load();
            Properties properties = new Properties();
            properties.setProperty("user", config.getUser());
            properties.setProperty("password", config.getPassword());
            properties.setProperty("connectTimeout", "5000");
            properties.setProperty("socketTimeout", "10000");
            return DriverManager.getConnection(serverOnly ? config.getServerUrl() : config.getUrl(), properties);
        } catch (IOException exception) {
            // Do not include configuration contents or credentials in error messages.
            throw new SQLException("Cannot load local database configuration.", exception);
        }
    }
}
