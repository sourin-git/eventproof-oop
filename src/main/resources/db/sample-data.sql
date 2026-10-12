USE eventproof_oop;

-- Stable IDs make repeated loading harmless. Existing rows are not overwritten.
INSERT INTO venues (id, name, capacity, backup_power, backup_internet) VALUES
    (1, 'College Auditorium', 1000, TRUE, TRUE),
    (2, 'Sports Ground', 2000, FALSE, FALSE)
ON DUPLICATE KEY UPDATE id = venues.id;

INSERT INTO events (id, name, event_date, expected_attendance, venue_id, event_type) VALUES
    (1, 'College Tech Fest 2026', '2026-11-15', 800, 1, 'TECHNICAL'),
    (2, 'Annual Sports Meet 2026', '2026-11-20', 1500, 2, 'SPORTS'),
    (3, 'Cultural Night 2026', '2026-11-25', 600, 1, 'CULTURAL')
ON DUPLICATE KEY UPDATE id = events.id;

INSERT INTO incidents (id, event_id, incident_type, description) VALUES
    (1, 1, 'Internet failure', 'Internet service was interrupted during a technical session.'),
    (2, 3, 'Power interruption', 'Stage power briefly failed during sound testing.'),
    (3, 2, 'Crowd management issue', 'The entrance queue needed additional volunteers.')
ON DUPLICATE KEY UPDATE id = incidents.id;
