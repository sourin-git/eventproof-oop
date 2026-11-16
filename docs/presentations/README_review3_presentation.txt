EVENTPROOF — FINAL REVIEW 3 POWERPOINT

The template is a fully editable 10-slide PPTX. It includes correct project information and
linkable placeholders. To embed authentic screenshots captured on your computer, place these
three package files inside <eventproof-oop>/docs/presentations/ (the ZIP folder structure
already does that) and ensure your images exist at:

  <eventproof-oop>/docs/screenshots/events.png
  <eventproof-oop>/docs/screenshots/incidents.png
  <eventproof-oop>/docs/screenshots/reliability.png
  <eventproof-oop>/docs/screenshots/simulation.png
  <eventproof-oop>/docs/screenshots/report.png

If necessary, install Python libraries once:

  py -m pip install python-pptx Pillow

From the eventproof-oop repository root, run:

  py docs/presentations/embed_eventproof_screenshots.py

The script generates:

  docs/presentations/EventProof_Review_3_Complete.pptx

The 5 PNGs are physically EMBEDDED in the final PPTX. No fake screenshots are created.
This helper does not modify Java code, MySQL data, Git tags, or the screenshots themselves.

Finally, fill in the three team member names and roll numbers in PowerPoint.
Commit the COMPLETED PPTX before creating the annotated review-3 tag.
