# EventProof: 3-minute demonstration and viva guide

EventProof is a simple Advanced OOP desktop project for college technical fests,
cultural programs and sports meets. Java 25 and JavaFX provide the application;
JDBC connects to MySQL, Maven builds it, and JUnit checks the rules.
Reliability and simulations are classroom rules, not real-world safety predictions.

## Before the demonstration

Start MySQL and run from the project root. Keep the ignored local
`src/main/resources/database.properties` configured for `eventproof_oop`.
Database setup is already complete on this machine. On a fresh installation,
follow the README setup instructions before presenting.

```powershell
mvn "-Dmaven.repo.local=.maven-repository" clean test
mvn "-Dmaven.repo.local=.maven-repository" javafx:run
```

On Windows, double-click `launch-eventproof.cmd` to launch and reopen without typing
commands during the demonstration. Java and Maven must already be installed.
Choose a folder for the report and have a text editor ready. Complete tests beforehand.

## Three-minute procedure

| Time | Action and explanation |
| --- | --- |
| 0:00-0:25 | Launch EventProof. EVENTS displays saved MySQL records. Point out College Tech Fest 2026, its venue, capacity and attendance. |
| 0:25-0:55 | On RELIABILITY, select College Tech Fest 2026 and Calculate reliability. Select INTERNET_FAILURE and Simulate failure. Compare original/simulated scores and risk levels. Simulation changes only copies. |
| 0:55-1:20 | On INCIDENTS, select the same event. Save type `Internet Failure` with a distinct description such as `Final viva demo - date and time`. Show the saved row. This records a real incident and affects later calculations. |
| 1:20-1:50 | Close the application and reopen with the launcher. On INCIDENTS, select the same event and find your saved description to demonstrate MySQL persistence. |
| 1:50-2:20 | Return to RELIABILITY and select the event. **Calculate again because incident history changed**, then select INTERNET_FAILURE and Simulate failure again. Explain the updated comparison. |
| 2:20-2:45 | Click Export Report. In the Save dialog, choose a folder and save the `.txt` file. Open it in a text editor and compare event details, actual scores, scenario, risk and generation time with the screen. |
| 2:45-3:00 | Point to the packages, ReliabilityAssessable interface, abstract Event and its overriding subclasses. Explain runtime dispatch in ReliabilityCalculator and validation/checked exceptions. |

Scores depend on the current event and recorded incident history; read the actual
results on screen instead of memorizing a score. They are rule-based classroom
estimates. Each saved incident remains in history. Export uses the last successful
calculation, so always recalculate after recording an incident. Changing a scenario
clears the previous simulation; changing the event invalidates all previous results.

## OOP concepts in the code

| Concept | Actual class example |
| --- | --- |
| Encapsulation | Event's private fields and validating setters; Venue and Incident constructor values exposed through getters |
| Abstraction | Abstract Event declares calculateReliabilityPenalty() |
| Inheritance | TechnicalEvent, CulturalEvent and SportsEvent extend Event |
| Packages and interface | model holds ReliabilityAssessable and Event; reliability imports the contract; ui imports DAOs and report classes |
| Runtime polymorphism | ReliabilityCalculator calls calculateReliabilityPenalty() through a ReliabilityAssessable reference holding an Event; the subclass method runs |
| Object composition / has-a | Event contains a Venue reference; the calculator builds separate Event/Venue objects for a simulation |
| Collections | EventDAO returns List<Event>; IncidentDAO returns List<Incident>; JavaFX tables display these records |
| Exception handling | Event throws InvalidEventDataException; DAOs propagate SQLException; export propagates IOException; views catch errors; ExceptionHandlingDemo shows finally; JDBC uses try-with-resources |

**Terminology:** because a venue is shared and exists independently of an event,
Event-Venue is **aggregation in strict UML**, rather than exclusive lifecycle
composition. It still demonstrates a has-a relationship and object composition
in the broader programming sense. Do not claim that deleting an event deletes its venue.

## The three MySQL tables

| Table | Purpose |
| --- | --- |
| venues | Stores a venue's name, capacity and backup power/internet availability |
| events | Stores name, date, attendance, event_type and venue_id referencing venues.id |
| incidents | Stores type and description, with event_id referencing events.id |

All three have integer primary keys. Foreign keys reject missing referenced records.
EventDAO reads event_type to reconstruct the correct Java subclass.

## Five likely viva questions

1. **Why is Event abstract?** It shares event fields and validation but requires
   each concrete event type to supply its own reliability penalty.
2. **Where does runtime polymorphism occur?** The calculator calls the same
   penalty method on a ReliabilityAssessable reference. Java chooses TechnicalEvent,
   CulturalEvent or SportsEvent's override at runtime.
3. **How does JDBC safely save and reload records?** DAOs bind form values with
   PreparedStatement, read ResultSet rows, and use generated IDs. Foreign keys
   preserve relationships; event_type determines the reconstructed subclass.
4. **How is reliability calculated?** Start at 100; subtract 20 for overcapacity,
   5 each for missing power/internet backups, 10 per recorded incident, and the
   subclass penalty. Technical adds 15/20 for missing power/internet; Cultural
   adds 20 for missing power; Sports adds 25 for overcapacity. Clamp to 0-100:
   LOW 80-100, MEDIUM 50-79, HIGH 0-49.
5. **Does simulation change the database?** No. It creates new Event and Venue
   objects, changes the selected failure condition, and adds one hypothetical
   incident to the count. NORMAL changes nothing. Only the form's Save actions
   insert actual records into MySQL.
