# Review 2 - Steps 1 and 2

Step 1 organizes the existing classes and introduces an interface for the
existing event-specific penalty method. Step 2 adds a text report exporter using
the Java Standard Library and one Export Report button on the existing screen.
The database schema, reliability rules and launch commands are unchanged.
Review 2 is not yet fully complete; Step 3 has not been implemented.

## Package structure

All production sources are under `src/main/java/com/eventproof`.

| Package | Purpose and contents |
| --- | --- |
| com.eventproof | Entry points: EventProofApp, DatabaseSetup, PolymorphismDemo |
| com.eventproof.model | Domain objects: Event, TechnicalEvent, CulturalEvent, SportsEvent, Venue, Incident; ReliabilityAssessable contract |
| com.eventproof.dao | JDBC operations: EventDAO, VenueDAO, IncidentDAO |
| com.eventproof.config | Local database configuration and connections: DatabaseConfig, DatabaseConnection |
| com.eventproof.reliability | Scoring and simulation: ReliabilityCalculator, FailureScenario, RiskLevel, SimulationResult |
| com.eventproof.ui | Existing JavaFX views and helpers: EventsView, IncidentsView, ReliabilityView, UiMessages |
| com.eventproof.report | ReliabilityReportExporter formats and saves already-calculated results as UTF-8 text |

For example, EventDAO imports model classes and DatabaseConnection from config;
ReliabilityView imports the DAOs, domain objects and reliability classes.
Classes and operations used by other packages are public. Domain fields remain
private, and Event's constructor remains protected for its subclasses. Package
separation does not require making fields public.

The original tests remain in `com.eventproof` and import the public classes from
their new packages, exercising those package boundaries. Entry points remain in
the root package so existing Maven and console commands still work.

## Interface definition and implementation

```java
package com.eventproof.model;

public interface ReliabilityAssessable {
    int calculateReliabilityPenalty();
}
```

An interface declares an operation that implementing classes must provide.
Event now declares `implements ReliabilityAssessable` and keeps the method
abstract. TechnicalEvent, CulturalEvent and SportsEvent inherit that contract
and supply their existing public overrides. The signature, return type and
penalties are unchanged.

The calculator now calls the method through the contract:

```java
ReliabilityAssessable assessment = event;
int penalty = assessment.calculateReliabilityPenalty();
```

The remaining scoring rules still use Event's attendance and Venue. The interface
describes only the shared penalty operation; it does not introduce a second
scoring API or duplicate the calculation.

## Interface-based polymorphism example

PolymorphismDemo imports classes from model and uses interface references:

```java
import com.eventproof.model.CulturalEvent;
import com.eventproof.model.ReliabilityAssessable;
import com.eventproof.model.SportsEvent;
import com.eventproof.model.TechnicalEvent;
import com.eventproof.model.Venue;
import java.time.LocalDate;

Venue venue = new Venue(1, "College Ground", 200, false, false);
LocalDate date = LocalDate.of(2026, 11, 15);
ReliabilityAssessable[] events = {
    new TechnicalEvent(1, "Tech Fest", date, 250, venue),
    new CulturalEvent(2, "Cultural Program", date, 250, venue),
    new SportsEvent(3, "Sports Meet", date, 250, venue)
};
for (ReliabilityAssessable event : events) {
    System.out.println(event.getClass().getSimpleName() + ": "
            + event.calculateReliabilityPenalty() + " penalty points");
}
```

Java selects the concrete subclass's override at runtime. For this same venue,
the penalties are TechnicalEvent 35, CulturalEvent 20 and SportsEvent 25.

Run the existing console entry point after compilation:

```powershell
java -cp target/classes com.eventproof.PolymorphismDemo
```

## IMPLEMENTATION OF JAVA STANDARD LIBRARY

Step 2 implements **Export Reliability Report (.txt)** without external libraries.

| Java library | Actual use |
| --- | --- |
| java.util | The existing screen uses List<Event> and List<Incident>; the exporter builds a List<String> of report lines from the supplied event and results |
| java.time | LocalDateTime.now() supplies the report generation time; DateTimeFormatter formats it as yyyy-MM-dd HH:mm:ss |
| java.nio.file | Path identifies the destination; Files.writeString() saves the report |
| java.nio.charset | StandardCharsets.UTF_8 preserves names and text, including non-English characters |

ReliabilityReportExporter accepts an Event, the already-calculated original score
and RiskLevel, and an optional FailureScenario/SimulationResult pair. Pass null
for both optional values when no simulation was performed. The exporter has no
JavaFX or JDBC dependencies, does not change objects, and does not calculate scores
or classify risk. ReliabilityView supplies results from ReliabilityCalculator,
including the existing recommendation from SimulationResult.

### Export Report button

1. Select an event and Calculate reliability (or run a simulation).
2. Optionally choose a failure scenario and click Simulate failure.
3. Click Export Report. The JavaFX FileChooser suggests
   `EventProof_Reliability_Report.txt` and filters for `.txt` files.
4. Choose a destination. A successful save displays a confirmation.

The button is disabled until a calculation succeeds. Changing the event or
reloading its selection invalidates the saved result. Changing the scenario
discards the previous simulation; exporting before running the new scenario
produces an original-only report. Recalculating without simulation also clears
the earlier simulation. The export uses the last successful calculation, not a
fresh database query; calculate again after recording new incidents.

Cancelling the chooser does nothing. IOException propagates from the exporter
and is handled by the screen with a readable error dialog. It is not silently
ignored. Export does not insert or update database records.

### Example report (illustrative values only)

This example assumes a TechnicalEvent with both backups, capacity 1000,
attendance 800, and two recorded incidents. The original score is 80; the
INTERNET_FAILURE scenario gives 45. Runtime reports use the selected event,
actual calculation results and the current local generation time.

```text
========================================
EVENTPROOF — RELIABILITY REPORT
========================================

Event Name: Example Technical Fest
Event Type: TECHNICAL
Event Date: 2026-11-30

Venue: Example Auditorium
Venue Capacity: 1000
Expected Attendance: 800

----------------------------------------
RELIABILITY ANALYSIS
----------------------------------------

Original Reliability Score: 80 / 100
Risk Level: LOW

----------------------------------------
FAILURE SIMULATION
----------------------------------------

Scenario: INTERNET_FAILURE
Simulated Reliability Score: 45 / 100
Simulated Risk Level: HIGH

Recommendation:
Arrange an independent internet backup and prepare offline activities.

Rule-based classroom estimate, not a real-world safety prediction.
----------------------------------------

Report Generated:
2026-11-16 10:30:00

========================================
```

Without simulation the report says `No failure simulation selected.` and marks
the simulated score, simulated risk and recommendation as not applicable.

The exporter tests use JUnit Jupiter's `@TempDir` and ordinary in-memory model
objects; they do not connect to MySQL. They cover file creation, Unicode/UTF-8,
all event types, event/venue details, actual scores and recommendations,
timestamps, no-simulation reports, and propagated I/O errors.
