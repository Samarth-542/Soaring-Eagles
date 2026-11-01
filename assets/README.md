# Asset Library

This folder contains selected visual assets copied from the Soaring Eagles reference project for review and reuse in the new website.

## Folders

- `brand/` — club logo, reference hero image, and favicon.
- `drone/` — 3D drone model candidate for the interactive homepage hero.
- `events/` — event, competition, workshop, and past-event images.
- `projects/` — aircraft and project images.
- `team/` — 24 member/advisor portraits referenced by the current public team data.
- `site/` — reference placeholder graphic and `robots.txt`.

## Before production use

- Confirm the club has permission to publish every photo, especially identifiable team portraits.
- The selected Sketchfab drone model is `drone/assault_drone_concept.glb`.
  The owner confirms the rights to display the complete model, including its
  weapon mesh. Render the original GLB without hiding meshes or changing its
  axes; preserve the creator's requested credit. Do not describe the model as
  an original Soaring Eagles aircraft.
- Review names, dates, and descriptions against current club-approved information.
- Optimize large images and provide responsive sizes/modern formats when the design and build tooling are chosen. The originals are retained here as reference assets.
- When a web-app scaffold is added, put the favicon and `robots.txt` in the framework's public/static directory so browsers and crawlers can access them at the expected root URLs.
- `robots.txt` is a reference copy; update it for the final production domain and indexing policy before launch.

## Deliberately not copied

- Payment QR image: excluded because the current QR destination and authorization to reuse it have not been verified.
- Registration export CSV: excluded because it contains operational registration data.
- Redundant HEIC original: the reference project also has a JPEG version of that photo.
- Invalid 11-byte image file: excluded because it is not a usable image.
- WhatsApp QR image: not selected for this asset set.
