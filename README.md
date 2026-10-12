# EventProof - Event Reliability Management System

A small second-year Advanced OOP project for college technical fests, cultural
programs, and sports meets. The JavaFX desktop application stores events and
incidents in MySQL, calculates a simple reliability score, and runs what-if scenarios.
It is a rule-based classroom estimate, **not a real-world safety prediction**.

## Stack and installation

Java 25, JavaFX 25.0.1, Maven, MySQL 8, JDBC (Connector/J 9.5.0), and JUnit 6.0.3.
Only JavaFX Controls, Connector/J and JUnit are direct dependencies. No FXML,
Spring, ORM, REST API, authentication, or third-party UI framework is used.

1. Install JDK 25, Maven 3.9+, and MySQL 8.0.16+; start MySQL.
2. Clone this repository and open a terminal in its root directory.
3. Copy `src/main/resources/database.properties.example` to
   `src/main/resources/database.properties` and enter your local credentials.
   Set `db.url` to `jdbc:mysql://localhost:3306/eventproof_oop`.
   Never commit this local file; Git and Maven resource copying exclude it.
4. Initialize the database using an account with CREATE privileges:

```powershell
mvn compile javafx:run "-Dapp.mainClass=com.eventproof.DatabaseSetup"
```

Setup loads `db/schema.sql` and `db/sample-data.sql` using the local credentials.
It creates only `eventproof_oop`, prints actual SELECT counts and event records,
and can be repeated without duplicating the samples or overwriting existing rows.
It does not access the old EventProof repository or database.

## Launch and test

```powershell
mvn clean test
mvn javafx:run
java -cp target/classes com.eventproof.PolymorphismDemo
```

The first Maven build needs internet access. This workspace uses a local dependency
cache; add `"-Dmaven.repo.local=.maven-repository"` to Maven commands to reuse it:

```powershell
mvn "-Dmaven.repo.local=.maven-repository" clean test
mvn "-Dmaven.repo.local=.maven-repository" javafx:run
```

Always run from the project root so the application can find the local configuration.
Close the window to exit. Input errors and database failures are shown in dialogs;
database error dialogs do not print credentials. Connection and query I/O have timeouts.

On this development machine, JDK 25.0.1, Maven 3.9.16 and MySQL 8.0.46 are installed.
Service `MySQL80` is running. The command-line client is installed at
`C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe` but is not on PATH.
The JDBC setup command does not need that client.

## Database and classes

Exactly three tables, each with an auto-increment primary key:

| Table | Contents and relationships |
| --- | --- |
| venues | Name, positive capacity, backup power, backup internet |
| events | Name, date, nonnegative attendance, venue_id FK, event_type |
| incidents | event_id FK, incident type, description |

Stored types `TECHNICAL`, `CULTURAL`, and `SPORTS` are reconstructed by `EventDAO`
as `TechnicalEvent`, `CulturalEvent`, and `SportsEvent` respectively.

All Java classes use `com.eventproof`:

- **Domain:** Event, TechnicalEvent, CulturalEvent, SportsEvent, Venue, Incident.
- **JDBC:** DatabaseConfig, DatabaseConnection, DatabaseSetup, VenueDAO, EventDAO, IncidentDAO.
- **Rules:** ReliabilityCalculator, FailureScenario, RiskLevel, SimulationResult.
- **Desktop:** EventProofApp, EventsView, IncidentsView, ReliabilityView, UiMessages.
- **Console example:** PolymorphismDemo.

Sources are in `src/main/java`; SQL and CSS are in `src/main/resources`;
tests are in `src/test/java`. DAO operations are intentionally limited:
venues create/list, events create/list/find by ID, incidents create/list by event ID.
They use DriverManager, PreparedStatement, ResultSet and try-with-resources.

## OOP examples

| Concept | Actual implementation |
| --- | --- |
| Encapsulation | Private domain fields; Event setters validate changes; Venue/Incident constructor values have getters |
| Abstraction | Abstract Event declares calculateReliabilityPenalty() |
| Inheritance | TechnicalEvent, CulturalEvent and SportsEvent extend Event |
| Polymorphism | ReliabilityCalculator calls the overridden method through an Event reference |
| Composition / has-a | Event contains a Venue object; venues can be shared by events |
| Collections | DAOs return List<Event>, List<Venue> and List<Incident> |
| Exception handling | Invalid forms are rejected; SQL failures produce safe error dialogs |

`PolymorphismDemo` calls the same method on an Event array holding all three
subclasses. Its penalties are 35, 20, and 25 for the same unprepared, over-capacity venue.

## Reliability and simulation rules

Start at 100 and subtract these common penalties:

| Condition | Penalty |
| --- | ---: |
| Attendance exceeds capacity | 20 |
| No backup power | 5 |
| No backup internet | 5 |
| Each recorded incident for this event | 10 |

Then add the penalty returned by the actual event subclass:

| Subclass | Additional penalty |
| --- | --- |
| TechnicalEvent | 15 without backup power; 20 without backup internet |
| CulturalEvent | 20 without backup power for sound and lighting |
| SportsEvent | 25 when attendance exceeds capacity |

The common and subclass penalties intentionally add together to give each type
different priorities. Scores are clamped to 0-100. LOW risk is 80-100,
MEDIUM is 50-79, and HIGH is 0-49. Incidents remain part of the recorded history;
there is no resolution/status workflow in this small project.

Simulation creates new Event and Venue objects and uses the same calculator:

| Scenario | What changes in the copy |
| --- | --- |
| NORMAL | Nothing; original and simulated scores match |
| INTERNET_FAILURE | Backup internet is unavailable; one hypothetical incident |
| POWER_FAILURE | Backup power is unavailable; one hypothetical incident |
| OVERCROWDING | Attendance is at least capacity + 20% (rounded up); one hypothetical incident |

Failures assume the backup is also unavailable. Existing higher attendance is
preserved. No simulation writes to MySQL or changes the selected event.
The result contains original score, simulated score, simulated risk, and a short recommendation.

## Demo procedure

1. Run database setup, then launch EventProof. EVENTS shows the three sample events.
2. Select College Tech Fest 2026 on RELIABILITY. Its venue is College Auditorium,
   capacity 1000, expected attendance 800, with both backups available.
3. Calculate reliability. With its initial one recorded incident, the score is 90 (LOW).
4. Select INTERNET_FAILURE and simulate: 55 (MEDIUM), with an internet-backup recommendation.
5. On INCIDENTS, select the same event, enter Internet Failure and a description, and Save incident.
6. Close and reopen the application. The incident is still listed; calculation is now 80 (LOW),
   and the same simulation gives 45 (HIGH).
7. Try Sports and Cultural events to see their different overridden behavior.
8. On EVENTS, use New venue or enter an event with a type, venue, date and attendance;
   Save event writes it to MySQL, and Refresh reloads stored records.

These example scores assume the initial sample history plus exactly one added
incident. Subsequent incident additions change the score through the implemented rules.

## Verified results

- Real MySQL setup: 2 venues, 3 events, 3 initial incidents; SELECT queries succeeded.
- After the desktop persistence demo: 2 venues, 3 events, 4 incidents.
- `mvn clean test`: 12 passed, 0 failed, 0 skipped, including the original four tests
  and two read-only live JDBC tests. Live tests skip only when local config is absent.
- Maven JavaFX launch opened normally and exited with BUILD SUCCESS.
- Real desktop button checks verified calculation, simulation, incident saving and persistence
  in a second application process, plus venue and event creation. Temporary form-check records
  are removed; the clearly marked Internet Failure demo incident remains.

To repeat the Windows desktop check after `mvn test`:

```powershell
./scripts/verify-desktop.ps1
```

This opens and closes two application processes and saves at most one incident
with the marked demo description. It does not duplicate that incident on repeat runs.
