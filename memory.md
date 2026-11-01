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
| [design.md](./design.md) | Updated; design brief | 21hrs.space-inspired visual system, approved GLB drone hero, public feed toggle, responsive interaction, licensing, accessibility, and performance constraints. Feed source and content approvals remain open. |
| [assets/README.md](./assets/README.md) | Updated; asset guidance | Lists the drone model and its attribution/license handling alongside the existing reviewed assets. |
| [frontend/](./frontend/) | In progress; frontend prototype | Responsive homepage, interactive drone/feed switch, local media/fonts, and Vite scripts. Build, lint, and desktop/mobile browser checks pass; production readiness and owner content review remain outstanding. |
| [frontend/src/App.tsx](./frontend/src/App.tsx) | In progress | Homepage sections and navigation; removed the hero “Explore Our Work” button per owner request; 3D experience is deferred briefly to prioritize initial content rendering. |
| [frontend/src/DroneExperience.tsx](./frontend/src/DroneExperience.tsx) | In progress | Interactive 3D model using the complete original GLB and supplied axes, with visible-geometry camera bounds, slight enlargement, keyboard/drag controls, public live-feed toggle, unavailable state, and attribution. |
| [frontend/src/App.css](./frontend/src/App.css), [frontend/src/index.css](./frontend/src/index.css) | In progress | Responsive cinematic styling; title, kicker, and overline have distinct rows; toolbar sits outside the title; desktop tagline is lifted above the drone while the CTA remains in place; controls, attribution, metadata, and scroll hint are spaced. |
| `memory.md` | Active tracker | Update after each project change; do not treat as a detailed task log. |

**Under development:** Phase 1 frontend foundation and Phase 2 homepage shell/hero
(partial); no backend or production-connected services.
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
- Design: 21hrs.space is the primary high-level visual reference for a dark,
  cinematic aerospace look, oversized display typography, technical labels,
  framing lines, and scroll-led storytelling. The site should share this
  visual language without copying its source, wording, media, or unlicensed
  fonts.
- Homepage direction: replace the reference moon with a realistic,
  interactive 360-degree drone model and a live camera/telemetry experience.
  Pointer/touch drag and keyboard controls, responsive layouts, reduced-motion
  mode, and WebGL/feed-unavailable fallbacks are required. Owner selected a
  public live-feed toggle; initial frontend shows unavailable state until
  hardware/service is provided. Do not simulate the feed or show unapproved
  telemetry.
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
- First implementation scope: responsive frontend first; proposed home story
  flow from `design.md`; draft club-specific copy for owner review; use approved
  current logo/assets and propose licensed font/palette alternatives. The
  featured model/feed toggles between the 3D model and public feed; the first
  frontend milestone shows an unavailable state until the real feed source is
  provided. No telemetry until approved.
- Frontend implementation status: the homepage prototype includes navigation,
  hero, mission, projects, journey, events/team, and contact CTA sections.
  Production build and lint pass; browser checks confirmed the 3D canvas loads
  at mobile and desktop widths, the feed toggle shows the disconnected state,
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
- Homepage content: draft copy for owner approval; primary CTA still to be
  confirmed. Use the proposed section order in `design.md`, pending review.
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
   visual palette/typography, home-story sequence, and accessibility
   checklist. Confirm font/model-license records and creator credit.
10. Identify live camera hardware/service and stream/API; complete public-feed
    safety/privacy review and approve any telemetry fields before display.
11. Decide whether the drone should rotate while idle; Three.js/React Three
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
