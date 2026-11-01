# Project Memory

> Working tracker for future development sessions. Update this file after each
> project change so completed work, active files, and open decisions stay
> current. This is a status summary, not a replacement for the source documents.

## Project boundary

- `Soaring/` is the planned new website project; it currently contains planning
  documents, reviewed assets, and the in-progress new frontend.
- The repository root is the older website and is reference-only. Do not edit
  it as part of new-site work unless the owner explicitly requests that.
- Reuse only owner-approved public content and media. Do not migrate personal,
  registration, contact, or payment data.

## Current status

**Frontend prototype in progress.** The Vite/React/TypeScript frontend and
responsive homepage are implemented as a first pass. The Spring Boot backend
and remaining product features have not started.

| File | Status | Purpose / next action |
|---|---|---|
| [prd.md](./prd.md) | Complete; living requirements | Product scope, acceptance criteria, entities, and decisions. Keep aligned with approved changes. |
| [architecture.md](./architecture.md) | Complete; living architecture | App flows, proposed structure, and selected technology direction. Update when architecture decisions change. |
| [rules.md](./rules.md) | Complete | Engineering and AI-agent guidelines. |
| [MASTER_PROJECT_GUIDE.md](./MASTER_PROJECT_GUIDE.md) | Complete; living guide | Project boundary, selected direction, and build guidance. Keep consistent with the PRD and architecture. |
| [phase.md](./phase.md) | Updated; execution plan | Phases, tasks, exit gates, testing strategy, and mapping of all 64 PRD requirements. Update task status as work progresses. |
| [design.md](./design.md) | Updated; living design brief | Current Home order, charcoal/off-white/copper theme, Space Grotesk/IBM Plex Mono, decorative event canvas, accessibility, and asset guidance. |
| [assets/README.md](./assets/README.md) | Updated; asset guidance | Lists the drone model and its attribution/license handling alongside the existing reviewed assets. |
| [frontend/](./frontend/) | In progress; frontend prototype | Vite/React/TypeScript app with existing interactive drone hero, event placeholder/canvas, contact details/form, footer, local media/fonts, and scripts. No backend or event/contact data integration. |
| [frontend/src/App.tsx](./frontend/src/App.tsx) | In progress | Retains the existing hero and presents only upcoming-event, contact, and footer content below it. Existing header/hero links to removed Home anchors still need navigation alignment. |
| [frontend/src/components/HomeContactForm.tsx](./frontend/src/components/HomeContactForm.tsx) | UI implemented; delivery pending | Required name/email/subject/message, optional PDF/JPEG/PNG attachment (10 MB), validation, submission status, and honest failure/email fallback. `/api/contact` does not exist. |
| [frontend/src/components/FlightPathCanvas.tsx](./frontend/src/components/FlightPathCanvas.tsx) | Implemented | Decorative responsive canvas backdrop for the upcoming-event section. |
| [frontend/src/App.css](./frontend/src/App.css), [frontend/src/index.css](./frontend/src/index.css) | In progress | Responsive existing hero and Home section styling; contact matches the landing page charcoal/off-white/copper palette and project fonts. |
| `memory.md` | Active tracker | Update after each project change; do not treat as a detailed task log. |

**Under development:** Phase 1 frontend foundation and Phase 2 Home page
(partial); event data, contact API, remaining public routes, and backend remain
unimplemented.
**Completed implementation phases:** Planning/documentation only.

## Decisions recorded

- Frontend: Vite + React + TypeScript SPA.
- Backend: Java 21, Spring Boot 3.x, Maven, modular monolith.
- Persistence: PostgreSQL hosted by Supabase; Supabase Storage for files.
- Email: Resend.
- Admin login: Spring Security username/password; email identifier, verified
  email password reset, password-only at launch. Owner provisions initial
  accounts; admins have equal permissions. Prevent deleting the last active
  admin and confirm self-removal.
- Selected-recruit profiles: one shared secret link, manually rotated; no
  recruit account and no automatic profile workflow email.
- Capacity: temporarily hold capacity during checkout; duration and expiry
  mechanics remain undecided.
- Design: 21hrs.space informs visual atmosphere and scroll continuity, not
  the Home content sequence. Home uses charcoal/off-white/copper styling,
  Space Grotesk, and IBM Plex Mono. See `design.md`.
- Homepage: Hero → upcoming-event section → contact details/form → footer.
  Keep Mission, Projects, Journey, Events, and Team off Home. Retain the
  existing hero. A camera-feed/telemetry toggle is not in the current scope.
- Contact details: `tseofficial2011@gmail.com`,
  `linkedin.com/in/soaring-eagles-a7a19831b`, `x.com/TSE_015`, and Soaring
  Eagles Aeromodelling Lab, Near Mechanical Block, Siddaganga Institute of
  Technology, Tumkur, 572103.
- Drone asset: owner approved
  `assets/drone/assault_drone_concept.glb` for use and says it is free to use.
  The Sketchfab listing describes a built-in machine gun/enemy game concept,
  shows "Free Standard", and requests creator credit. Inspection confirmed the
  GLB has separate `Drone_Attack_Drone_M_1_0` airframe and
  `Drone_Attack_Drone_M_2_0` weapon meshes. The owner confirms rights to display
  the complete model, including the weapon. The prototype displays the full
  GLB without changing its source geometry or axes. Camera fit uses all visible
  geometry, and model scale is 1.95. Preserve attribution; do not represent it
  as Soaring Eagles' original aircraft design.
- Frontend status: Home has the four agreed parts. The existing hero remains
  unchanged. The upcoming-event section shows an empty state and decorative
  canvas, but is not connected to published events. The contact form has no
  backend; it currently reports the unavailable `/api/contact` request and
  offers the email fallback.
- Frontend implementation history: earlier versions included mission,
  projects, journey, events/team, and a live-feed toggle; these are not in the
  current Home prototype. Home now has the hero, upcoming-event section,
  contact details/form, and footer.
  Production build and lint pass; browser checks confirmed the 3D canvas loads
  at mobile and desktop widths; the then-existing feed toggle showed the
  disconnected state and was later removed per owner direction,
  and reduced-motion preference is honored on reload. `npm audit --omit=dev`
  reports no production dependency vulnerabilities. A Three.js deprecation
  warning and a large lazy-loaded 3D chunk remain known optimization items.
  Camera framing now fits visible drone geometry to the available
  viewport and centers it. Desktop/tablet title size and placement are reduced,
  with a stronger downward model offset so the drone is below rather than
  behind the name. Kicker/overline/title are separated; model-view controls sit
  beside the title; CTA, rotation controls, attribution, metadata, and scroll
  hint no longer collide. The desktop tagline was shifted up 2rem independently
  from the Explore button; subsequent adjustments moved the desktop tagline
  upward by another 4rem (6rem total from its base position). Mobile retains
  its original tagline placement.
  Mobile metadata is moved below the CTA. Tested 390px, 768px, 1024px, 1365px,
  and 1440px viewports without horizontal overflow; feed toggle and Explore
  navigation were exercised before the owner requested removal of the hero
  “Explore Our Work” button. It is now removed while the tagline remains; the
  3D canvas still renders. The complete model, including its weapon mesh, now
  renders with its original GLB axes; latest build/lint pass and desktop/mobile hero checks confirm the
  interactive canvas renders without horizontal overflow. Homepage copy/media
  and production model-license verification remain open.
- Homepage content: the Home section order and contact details are confirmed.
  The event area remains an empty state until approved event data is available.
- Launch: full PRD scope targeted for 15 December 2026; schedule feasibility
  needs an estimate and owner confirmation.
- Analytics: no analytics or non-essential cookies at launch.
- Accessibility: follow best practices; no formal conformance claim selected.
- Delivery: GitHub pull requests, automated CI, and preview deployments.

## Open decisions / gates

1. Select the INR payment provider; determine checkout hold duration, expiry,
   webhook handling details, receipt requirements, and sandbox/production
   access.
2. Select frontend/backend hosting, confirm domain availability, and identify
   deployment/DNS/TLS owners.
3. Confirm data-retention periods and privacy/terms requirements with the
   owner/legal reviewer.
4. Choose frontend router, component/UI library, form-validation approach, and
   browser-test runner.
5. Confirm the shared recruit-link secret storage, rotation procedure, rate
   limits, and exposure response.
6. Confirm admin password policy, login lockout/throttling, reset-token expiry,
   email failure recovery, and audit details.
7. Verify Supabase project ownership, database connectivity, Storage buckets,
   access policy, and environment separation.
8. Verify Resend sender-domain authentication and production sending limits.
9. Approve migrated public content, facts, media, supported browser matrix,
   accessibility checklist, and creator-credit records. The Home sequence,
   palette, and typography are recorded in `design.md`.
10. Decide whether the drone should rotate while idle; Three.js/React Three
    Fiber is selected for the prototype, but mobile performance and final
    responsive breakpoints still need broader device testing.
12. Estimate all phases against the launch target using actual team capacity;
    any scope/date change requires owner approval.

The detailed decision tasks are in [phase.md](./phase.md), Phase 0.

## Update protocol

After each requested project change:

1. Update the changed source document(s) and implementation as appropriate.
2. Update this file's current status, file table, decisions, open gates, and
   phase/task status if affected.
3. Record what was actually verified; do not mark work complete based on
   planned or untested behavior.
4. Keep this summary concise and link to the authoritative project documents.
