# Review 2 - Step 1: packages and one interface

This step organizes the existing classes and introduces an interface for the
existing event-specific penalty method. It keeps the GUI, database schema,
reliability rules and launch commands unchanged. Later Review 2 steps are not
implemented here.

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
