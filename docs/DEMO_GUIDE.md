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

The local-cache option can be omitted when using the normal Maven cache.
Have the launch command ready to run again after closing the window.

## Three-minute procedure

| Time | Action and explanation |
| --- | --- |
| 0:00-0:20 | Introduce EventProof. EVENTS displays saved MySQL records; point out College Tech Fest 2026, its venue and attendance. |
| 0:20-1:00 | Use New venue: name `Viva Hall`, capacity `500`, enable both backups, then OK. The new venue is selected. Create `Viva Technical Event`, type TECHNICAL, a calendar date and attendance `400`. Save event and Refresh. |
| 1:00-1:45 | On RELIABILITY, select the new event and Calculate reliability: 100, LOW with no incidents. Run INTERNET_FAILURE (65, MEDIUM), POWER_FAILURE (70, MEDIUM), and OVERCROWDING (70, MEDIUM). Explain that each scenario adds one hypothetical incident and changes only a copy. |
| 1:45-2:10 | On INCIDENTS, select the new event. Add type `Internet Failure` and description `Internet interrupted during the viva demo`. Save incident. Its actual history now reduces the baseline to 90. |
| 2:10-2:35 | Close and relaunch with the ready command. Show the new event, venue and saved incident. Calculate again: 90, LOW. |
| 2:35-3:00 | Explain Event's abstract method, the three overriding subclasses, and the foreign keys. Technical events give internet extra weight; cultural events emphasize power; sports events emphasize crowd capacity. |

Use a distinct name on repeat demonstrations. Scores above assume exactly these
inputs and no previous incidents for the newly created event. Scores are calculated,
not stored or hardcoded in the GUI. Repeated incident additions change the results.

## OOP concepts in the code

| Concept | Actual class example |
| --- | --- |
| Encapsulation | Event's private fields and validating setters; Venue and Incident constructor values exposed through getters |
| Abstraction | Abstract Event declares calculateReliabilityPenalty() |
| Inheritance | TechnicalEvent, CulturalEvent and SportsEvent extend Event |
| Runtime polymorphism | ReliabilityCalculator calls calculateReliabilityPenalty() through a ReliabilityAssessable reference holding an Event; the subclass method runs |
| Object composition / has-a | Event contains a Venue reference; the calculator builds separate Event/Venue objects for a simulation |
| Collections | EventDAO returns List<Event>; IncidentDAO returns List<Incident>; JavaFX tables display these records |
| Exception handling | Form validation catches invalid input; DAOs propagate SQLException; views display safe error dialogs; resources close with try-with-resources |

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
