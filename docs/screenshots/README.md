# Final presentation screenshots

No PNG screenshots have been captured yet. Native desktop capture was unavailable
during Review 3 Step 3: the computer-use helper could not connect to its native pipe.
No mockups or generated replacement images are included.

The clean build passed all 30 tests. Programmatic JavaFX checks at the normal window
size verified all three screens, real MySQL records, calculation, simulation, export
availability and accessible content. A report saved through the UI's save handler
matched current database values, calculated scores, risk levels, timestamp and UTF-8.
Existing database rows were unchanged.

**Native Save/Cancel and opening the report in a text editor: NOT VERIFIED.** These
remain manual checks; programmatic file generation is not native-dialog verification.

## Capture with Windows Snipping Tool

1. Start MySQL and double-click `launch-eventproof.cmd`. Use the normal application
   window or resize it so the relevant text is readable. Close unrelated dialogs.
2. Prepare each view below. Press **Windows + Shift + S**, choose **Window** mode
   and select only the application window. In Snipping Tool, save the PNG using the
   exact filename in this directory. Avoid desktop content and notifications.
3. Inspect each saved image: include the title, relevant controls and results with
   readable text. Preserve proportions when placing images on 16:9 slides; do not
   stretch the image or crop important information. Scroll if needed before capture.

| Planned file (currently missing) | Prepare this real view |
| --- | --- |
| `events.png` | EVENTS: show the existing College Tech Fest 2026, Annual Sports Meet 2026 and Cultural Night 2026 rows, including venue, attendance and capacity columns. |
| `incidents.png` | INCIDENTS: select College Tech Fest 2026 and show its existing persisted incident rows. Do not add an incident just for a screenshot. |
| `reliability.png` | RELIABILITY: select College Tech Fest 2026 and click Calculate reliability. Capture original score, original risk and event/venue details. |
| `simulation.png` | Keep College Tech Fest 2026 selected, choose INTERNET_FAILURE and click Simulate failure. Capture original and simulated scores, both risk levels and the recommendation. |
| `report.png` | After the simulation, click Export Report and save the actual `.txt` report. Open that file in Notepad or another text editor. Capture its event name/type, original and simulated scores, scenario, risks and timestamp. Resize the editor or adjust zoom enough to show these clearly. |

Read the current scores from the application; do not type replacement values into
the report or images. Recalculate first if incident history has changed. Scores are
rule-based classroom estimates.

## Finish the native report check

1. Save the report through FileChooser, for example to the ignored
   `target/EventProof_Reliability_Report.txt`. Confirm the success message appears.
2. Open the saved file in the text editor and compare its details and calculated
   scores with the screen. Check the heading's em dash and the generation timestamp.
3. Return to EventProof, open Export Report again and click Cancel. Confirm there is
   no error or new success message and the application remains usable.

Keep only the five intended PNGs here. Do not commit exported `.txt` reports,
configuration files or credentials. Check the images before sharing them. These
instructions do not indicate that the manual steps have already been performed.
