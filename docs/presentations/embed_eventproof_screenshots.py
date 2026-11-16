"""Embed REAL screenshots from the local EventProof project into the 10-slide PPTX.

Run from any directory with:
    py docs/presentations/embed_eventproof_screenshots.py

Requires python-pptx and Pillow. Never creates or fabricates screenshots.
"""
from pathlib import Path
import sys

try:
    from pptx import Presentation
    from PIL import Image
except ImportError:
    print("Missing Python package. Install with: py -m pip install python-pptx Pillow")
    sys.exit(2)

script_path = Path(__file__).resolve()
root = script_path.parents[2]
images = root / 'docs' / 'screenshots'
template = script_path.with_name('EventProof_Final_Review_3.pptx')
output = script_path.with_name('EventProof_Review_3_Complete.pptx')

source_names = {
    'SCREENSHOT_EVENTS': 'events.png',
    'SCREENSHOT_INCIDENTS': 'incidents.png',
    'SCREENSHOT_RELIABILITY': 'reliability.png',
    'SCREENSHOT_SIMULATION': 'simulation.png',
    'SCREENSHOT_REPORT': 'report.png',
}
missing = [str(images / p) for p in source_names.values() if not (images / p).is_file()]
if missing:
    print('Missing authentic application screenshots:')
    print('\n'.join('  '+m for m in missing))
    sys.exit(1)

if not template.is_file():
    print('PPTX template not found:', template)
    sys.exit(1)

prs = Presentation(template)
if len(prs.slides) != 10:
    raise RuntimeError('Expected exactly 10 slides, found '+str(len(prs.slides)))

found = set()
for slide in prs.slides:
    shapes = list(slide.shapes)
    for index, shape in enumerate(shapes):
        # The template also has separate text labels inside each image placeholder.
        # Remove these labels so none remain visible in the picture's letterbox area.
        if shape.has_text_frame and (
            shape.text in {'REAL CAPTURE', 'Screenshot is available in the GitHub repository'}
            or shape.text.startswith('Click to open  ')
        ):
            if shape.text == 'REAL CAPTURE' and index > 0:
                badge = shapes[index - 1]
                if badge.name.startswith('Rounded Rectangle') and badge.has_text_frame and not badge.text:
                    badge._element.getparent().remove(badge._element)
            shape._element.getparent().remove(shape._element)
            continue
        if shape.name not in source_names:
            continue
        filename = source_names[shape.name]
        file = images / filename
        if shape.name in found:
            raise RuntimeError('Duplicate placeholder: '+shape.name)
        found.add(shape.name)

        # Respect aspect ratio and keep all screenshot text visible without cropping.
        with Image.open(file) as im:
            iw, ih = im.size
        max_w, max_h = shape.width, shape.height
        scale = min(max_w / iw, max_h / ih)
        w, h = int(iw * scale), int(ih * scale)
        x = shape.left + (max_w - w) // 2
        y = shape.top + (max_h - h) // 2
        # Remove only the named placeholder, not adjacent captions/callouts.
        shape._element.getparent().remove(shape._element)
        pic = slide.shapes.add_picture(str(file), x, y, width=w, height=h)
        pic.name = 'REAL_'+filename.upper().replace('.','_')
        pic.click_action.hyperlink.address = (
            'https://github.com/sourin-git/eventproof-oop/blob/main/docs/screenshots/'+filename
        )
        print('Embedded:', filename, f'({iw} × {ih})')

if found != set(source_names):
    raise RuntimeError('Missing PPTX placeholders: '+', '.join(sorted(set(source_names)-found)))

prs.save(output)
print('\nCompleted:', output)
print('PowerPoint slides:',len(prs.slides))
print('All 5 images are embedded, not externally linked.')
