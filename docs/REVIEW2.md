# Review 2: packages, Java libraries and exception handling

EventProof is a second-year Advanced OOP desktop project for college events.
Review 2 organizes the existing application, adds a reusable reliability contract,
exports calculated reports using built-in Java libraries, and demonstrates exception
handling. The JavaFX layout, MySQL schema and reliability scoring rules are preserved.
The final audit passed with the native FileChooser limitation noted below.

## Package structure

Production sources are under `src/main/java/com/eventproof`. Public classes and
methods allow interaction across packages; domain fields remain private and Event's
constructor is protected. For example, EventDAO imports model classes and config's
DatabaseConnection; ReliabilityView imports DAOs, models, reliability and report classes.

| Package | Purpose and actual classes |
| --- | --- |
| com.eventproof | Entry points: EventProofApp, DatabaseSetup, PolymorphismDemo, ExceptionHandlingDemo |
| com.eventproof.model | Event, TechnicalEvent, CulturalEvent, SportsEvent, Venue, Incident, ReliabilityAssessable |
| com.eventproof.dao | Simple JDBC operations: EventDAO, VenueDAO, IncidentDAO |
| com.eventproof.config | DatabaseConfig, DatabaseConnection |
| com.eventproof.reliability | ReliabilityCalculator, FailureScenario, RiskLevel, SimulationResult |
| com.eventproof.ui | EventsView, IncidentsView, ReliabilityView, UiMessages |
| com.eventproof.report | ReliabilityReportExporter |
| com.eventproof.exception | InvalidEventDataException |

## Interfaces and runtime polymorphism

The implemented contract preserves the original method signature:

```java
package com.eventproof.model;

public interface ReliabilityAssessable {
    int calculateReliabilityPenalty();
}
```

Abstract Event implements ReliabilityAssessable and keeps this method abstract.
TechnicalEvent, CulturalEvent and SportsEvent provide their existing public overrides.
The calculator uses the interface directly:

```java
ReliabilityAssessable assessment = event;
int penalty = assessment.calculateReliabilityPenalty();
```

Java chooses the actual subclass's method at runtime. PolymorphismDemo imports the
model classes and iterates over a ReliabilityAssessable array. For its same unprepared,
over-capacity venue, TechnicalEvent returns 35, CulturalEvent 20 and SportsEvent 25
penalty points. These penalties are inputs to the existing final-score calculation.

## IMPLEMENTATION OF JAVA STANDARD LIBRARY

| Built-in library | Actual use |
| --- | --- |
| java.util | DAOs/screens use List<Event> and List<Incident>; the exporter assembles List<String> report lines |
| java.time | LocalDateTime.now() and DateTimeFormatter generate readable local report timestamps |
| java.nio.file | Path identifies the destination; Files.writeString() writes the report |
| java.nio.charset | StandardCharsets.UTF_8 preserves Unicode names and report text |

ReliabilityReportExporter accepts an Event, the calculated original score and
RiskLevel, and an optional FailureScenario/SimulationResult pair. It neither queries
MySQL nor changes objects nor calculates new rules. Actual formatting/writing includes:

```java
String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
```

```java
Files.writeString(output, String.join(System.lineSeparator(), lines) + System.lineSeparator(),
        StandardCharsets.UTF_8);
```

### Report export

Select an event, Calculate reliability, optionally Simulate failure, then click
Export Report. FileChooser suggests `EventProof_Reliability_Report.txt`, filters for
`.txt`, and lets the user choose a destination. Success is shown only after writing.
The button remains disabled until calculation succeeds. Changing the event invalidates
the result; changing the scenario or recalculating clears the earlier simulation.

The report uses the last successful calculation: calculate again after adding
incidents. It contains event/venue details, original score and risk, optional simulation
score/risk/recommendation, and generation time. Without simulation, pass null for both
optional values; the report states that no simulation was selected and marks simulated
values as not applicable. Export never modifies the database.

**Illustrative report excerpt:** with a technical event, both backups available,
capacity 1000, attendance 800 and two incidents, the existing rules yield:

```text
EVENTPROOF — RELIABILITY REPORT
Event Name: Example Technical Fest
Event Type: TECHNICAL
Venue Capacity: 1000
Expected Attendance: 800
Original Reliability Score: 80 / 100
Risk Level: LOW
Scenario: INTERNET_FAILURE
Simulated Reliability Score: 45 / 100
Simulated Risk Level: HIGH
Recommendation:
Arrange an independent internet backup and prepare offline activities.
Report Generated:
2026-11-16 10:30:00
```

These are sample values. Runtime reports use selected records, actual calculation
results and the current time. They are classroom estimates, not safety predictions.

## EXCEPTION HANDLING

### Unchecked exception, throw and try-catch

InvalidEventDataException extends IllegalArgumentException. It is unchecked, so
callers are not required to declare or catch it. Event uses it for existing rules:
missing/blank or overlong names, missing dates and negative attendance. For example:

```java
if (expectedAttendance < 0) {
    throw new InvalidEventDataException("Expected attendance cannot be negative.");
}
```

`throw` raises the exception. Validation runs before field assignment; accepted
values are unchanged, including past dates, zero attendance and overcapacity.
Venue and Incident keep their existing IllegalArgumentException validation.
EventsView's existing superclass catch also handles the custom exception:

```java
} catch (IllegalArgumentException exception) {
    UiMessages.inputError(exception.getMessage());
} catch (SQLException exception) { UiMessages.databaseError(exception); }
```

Number/date parsing has specific catch blocks. Failed save attempts clear any earlier
success message. Dialogs show readable messages without stack traces.

### Checked exceptions and throws

IOException and SQLException are checked: methods must catch them or declare them
using `throws`. ReliabilityReportExporter.export() declares `throws IOException` and
lets file errors propagate. ReliabilityView's save helper catches them:

```java
} catch (IOException | SecurityException exception) {
    showExportError();
}
```

A cancelled chooser supplies null, and the helper returns without any dialog.
On write failure, it shows an error instead of a success message; the user can retry.

DAO methods and DatabaseConnection.getConnection() declare `throws SQLException`.
Screens catch it and display setup advice, SQL state and vendor code. Raw driver
messages, database credentials and stack traces are not displayed. A configuration
read failure retains its cause for debugging using standard SQLException:

```java
throw new SQLException("Cannot load local database configuration.", exception);
```

### finally

ExceptionHandlingDemo is independent of JavaFX/MySQL. Its actual validation block is:

```java
try {
    Event event = new TechnicalEvent(1, name, LocalDate.of(2026, 11, 30), 400, venue);
    System.out.println("Valid event: " + event.getName());
} catch (InvalidEventDataException exception) {
    System.out.println("Invalid event data: " + exception.getMessage());
} finally {
    System.out.println("Validation attempt finished.");
}
```

The demo tries a blank and a valid name; finally runs after both attempts.

### try-with-resources

JDBC resources are still managed automatically. VenueDAO.listAll(), for example, uses:

```java
try (Connection connection = DatabaseConnection.getConnection();
     PreparedStatement statement = connection.prepareStatement("SELECT * FROM venues ORDER BY id");
     ResultSet result = statement.executeQuery()) {
```

Resources close in reverse order, including when an operation throws. No manual
connection-closing finally blocks or exception framework were introduced.

## Testing and demonstration

From the project root with MySQL running and the ignored local configuration present:

```powershell
mvn "-Dmaven.repo.local=.maven-repository" clean test
mvn "-Dmaven.repo.local=.maven-repository" javafx:run
java -cp target/classes com.eventproof.PolymorphismDemo
java -cp target/classes com.eventproof.ExceptionHandlingDemo
```

The final clean build passed **30 tests, 0 failures, 0 skips**: domain 4, interface 2,
calculator/simulation 6, exporter 11, custom-exception 5 and live read-only JDBC 2.
Exporter tests use `@TempDir` and in-memory objects, without database connections.

For a short demonstration:

1. Show stored MySQL events. Create a uniquely named venue and event to demonstrate
   the existing Save/Refresh actions; keep sample records intact.
2. On RELIABILITY, calculate and compare INTERNET_FAILURE, POWER_FAILURE and
   OVERCROWDING. Point to the interface invocation in the calculator.
3. Export a report and compare its contents with the displayed results. Change the
   event to show that export is disabled until recalculation.
4. Add an incident to your demonstration event, close/reopen and show persistence.
5. Try blank names, negative attendance or empty incident descriptions; explain the
   warning and continued usability. Run the console demo to show finally.

### Final audit results and remaining manual check

The normal Maven launch and application-level JavaFX checks passed: real event
loading, venue/event/incident creation, persistence in a second process, all three
simulations, UTF-8 reports, result invalidation, form warnings, file-error/retry and
cancellation handling. An isolated temporary configuration verified unavailable-MySQL
errors without stopping or reconfiguring the real server.

Read-only metadata confirmed MySQL 8.0.46 and exactly venues, events and incidents,
each with primary key id. Foreign keys are events.venue_id -> venues.id and
incidents.event_id -> events.id. JDBC reconstructed all three event subclasses.
Only uniquely identified audit records were removed after persistence checks;
existing records remained at **2 venues, 3 events and 4 incidents**.

**Native FileChooser limitation:** the computer-use helper could not connect to its
native pipe, including after retry/reset. Actual native save/cancel interaction was
not automated or manually verified. Programmatic save, cancellation-result, failure
and retry checks passed. Before presenting, manually open Export Report, save a `.txt`
file, then reopen and Cancel; confirm the saved content and absence of a cancel error.

No blocking application defect was found. The audit changed documentation only;
the verified checkpoint is preserved by the annotated `review-2` tag.
