-- Run manually with a MySQL account allowed to create a database and tables.
CREATE DATABASE IF NOT EXISTS eventproof_oop CHARACTER SET utf8mb4;
USE eventproof_oop;

CREATE TABLE IF NOT EXISTS venues (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    capacity INT NOT NULL CHECK (capacity > 0),
    backup_power BOOLEAN NOT NULL DEFAULT FALSE,
    backup_internet BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS events (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    event_date DATE NOT NULL,
    expected_attendance INT NOT NULL CHECK (expected_attendance >= 0),
    venue_id INT NOT NULL,
    event_type ENUM('TECHNICAL', 'CULTURAL', 'SPORTS') NOT NULL,
    FOREIGN KEY (venue_id) REFERENCES venues(id)
);

CREATE TABLE IF NOT EXISTS incidents (
    id INT AUTO_INCREMENT PRIMARY KEY,
    event_id INT NOT NULL,
    incident_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    FOREIGN KEY (event_id) REFERENCES events(id)
);
