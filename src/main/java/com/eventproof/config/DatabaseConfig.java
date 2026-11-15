package com.eventproof.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Credentials stay in the ignored local file, never in source code or the JAR. */
public class DatabaseConfig {
    private final String url;
    private final String user;
    private final String password;

    private DatabaseConfig(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public static DatabaseConfig load() throws IOException {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(Path.of("src/main/resources/database.properties"), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        String url = properties.getProperty("db.url", "").trim();
        String user = properties.getProperty("db.user", "").trim();
        String password = properties.getProperty("db.password");
        // Limit initialization to this project's database; never touch the old database.
        if (!url.matches("jdbc:mysql://[^/]+/eventproof_oop(?:\\?.*)?") || user.isBlank() || password == null) {
            throw new IOException("Set db.url, db.user and db.password for eventproof_oop in database.properties.");
        }
        return new DatabaseConfig(url, user, password);
    }

    public String getUrl() { return url; }
    public String getUser() { return user; }
    public String getPassword() { return password; }

    public String getServerUrl() {
        int databaseStart = url.indexOf('/', "jdbc:mysql://".length());
        int queryStart = url.indexOf('?', databaseStart);
        return url.substring(0, databaseStart + 1) + (queryStart < 0 ? "" : url.substring(queryStart));
    }
}
