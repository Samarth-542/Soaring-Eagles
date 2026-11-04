# Asset Library

This folder contains selected visual assets copied from the Soaring Eagles reference project for review and reuse in the new website.

## Folders

- `brand/` — club logo, reference hero image, and favicon.
- `drone/` — 3D drone model candidate for the interactive homepage hero.
- `events/` — event, competition, workshop, and past-event images. Selected
  event and Journey archive photos are copied into
  `frontend/public/images/events/` and `frontend/public/images/journey/`.
- `projects/` — aircraft and project images.
- `team/` — legacy roster portraits plus 19 additional portraits from approved
  profiles currently displayed on the live public Team page. The new frontend
  uses the locally copied images; it does not depend on the old Supabase URL.
- `projects/` — aircraft and project images used by the legacy public Projects
  page. The nine displayed projects' images are copied into
  `frontend/public/images/projects/` for the new route.
- `site/` — reference placeholder graphic and `robots.txt`.

## Before production use

- Confirm the club has permission to publish every photo, especially identifiable team portraits.
- The new frontend's initial Team roster includes approved profiles displayed
  by the live public site. Their public profile images were copied from the
  site's public Supabase Storage URLs into `frontend/public/images/team/`.
  Email and phone fields were not imported. Reconfirm current profile/image
  approvals before production launch.
- The selected Sketchfab drone model is `drone/assault_drone_concept.glb`.
  The owner confirms the rights to display the complete model, including its
  weapon mesh. Render the original GLB without hiding meshes or changing its
  axes; preserve the creator's requested credit. Do not describe the model as
  an original Soaring Eagles aircraft.
- Review names, dates, and descriptions against current club-approved information.
- Optimize large images and provide responsive sizes/modern formats when the design and build tooling are chosen. The originals are retained here as reference assets.
- The Vite frontend is scaffolded. Keep the favicon and `robots.txt` in
  `frontend/public/` so browsers and crawlers can access them at the expected
  root URLs.
- `robots.txt` is a reference copy; update it for the final production domain and indexing policy before launch.

## Deliberately not copied

- Payment QR image: excluded because the current QR destination and authorization to reuse it have not been verified.
- Registration export CSV: excluded because it contains operational registration data.
- Redundant HEIC original: the reference project also has a JPEG version of that photo.
- Invalid 11-byte image file: excluded because it is not a usable image.
- WhatsApp QR image: not selected for this asset set.
