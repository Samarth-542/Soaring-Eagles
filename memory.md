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

**Frontend prototype and backend foundation in progress.** The Vite/React/
TypeScript frontend and responsive homepage are implemented as a first pass.
The Spring Boot backend foundation now builds with Maven Wrapper, a health
endpoint, protected API defaults, Flyway migration setup, H2 development/test
profiles, and integration tests. Product feature endpoints and external
integrations have not started.

| File | Status | Purpose / next action |
|---|---|---|
| [prd.md](./prd.md) | Complete; living requirements | Product scope, acceptance criteria, entities, and decisions. Keep aligned with approved changes. |
| [architecture.md](./architecture.md) | Complete; living architecture | App flows, proposed structure, and selected technology direction. Update when architecture decisions change. |
| [rules.md](./rules.md) | Complete | Engineering and AI-agent guidelines. |
| [MASTER_PROJECT_GUIDE.md](./MASTER_PROJECT_GUIDE.md) | Complete; living guide | Project boundary, selected direction, and build guidance. Keep consistent with the PRD and architecture. |
| [phase.md](./phase.md) | Updated; execution plan | Phases, tasks, exit gates, testing strategy, and mapping of all 64 PRD requirements. Update task status as work progresses. |
| [design.md](./design.md) | Updated; living design brief | Current Home order, Team design direction, charcoal/off-white/copper theme, Space Grotesk/IBM Plex Mono, CSS grid vignette, accessibility, and asset guidance. |
| [README.md](./README.md) | Added; project entry point | New-site overview, current prototype status, frontend terminal setup, scripts, and links to project documentation. |
| [assets/README.md](./assets/README.md) | Updated; asset guidance | Lists the drone model and its attribution/license handling alongside the existing reviewed assets. |
| [frontend/](./frontend/) | In progress; frontend prototype | Vite/React/TypeScript app with React Router, existing interactive drone hero, Events, Journey, and Projects routes, Team route with migrated public roster, contact details/form, footer, local media/fonts, and scripts. No event/contact data integration. |
| [frontend/src/pages/ProjectsPage.tsx](./frontend/src/pages/ProjectsPage.tsx) | Implemented; static content | Separate `/projects` route with nine project entries from the previous site's public Projects page; responsive introduction and timeline heading. Historical results/content need owner verification; admin management is not connected. |
| [frontend/src/components/ui/timeline-01.tsx](./frontend/src/components/ui/timeline-01.tsx) | Implemented | Reusable project timeline component; newest-first entries, alternating desktop cards, mobile rail, GSAP ScrollTrigger progress/reveals, and reduced-motion support. |
| [frontend/src/data/projects.ts](./frontend/src/data/projects.ts) | Implemented | Typed project descriptions, years, tags, and local image references for the nine migrated public entries. |
| [frontend/src/pages/EventsPage.tsx](./frontend/src/pages/EventsPage.tsx) | Implemented; static archive | Separate `/events` route with an upcoming-events empty state and four past events migrated from the previous public site; publishing, registration, and admin management are not connected. |
| [frontend/src/components/ui/event-timeline.tsx](./frontend/src/components/ui/event-timeline.tsx) | Implemented | Responsive newest-first timeline with GSAP ScrollTrigger progress/reveals and reduced-motion support. |
| [frontend/src/data/events.ts](./frontend/src/data/events.ts) | Implemented | Typed static event dates, venues, descriptions, outcomes, and local image references. |
| [frontend/src/pages/OurJourneyPage.tsx](./frontend/src/pages/OurJourneyPage.tsx) | Implemented; static archive | Separate `/our-journey` route with 11 newest-first milestones and local photos migrated from the previous public site. Historical results require club verification; admin management is not connected. |
| [frontend/src/components/ui/journey-timeline.tsx](./frontend/src/components/ui/journey-timeline.tsx) | Implemented | Responsive milestone timeline with GSAP ScrollTrigger progress/reveals and reduced-motion support. |
| [frontend/src/data/journey.ts](./frontend/src/data/journey.ts) | Implemented | Typed Journey milestone titles, year labels, descriptions, and local image references. |
| [backend/](./backend/) | Foundation implemented | Java 21/Spring Boot 3.5 Maven app with wrapper, health endpoint, fail-closed API security, problem-detail errors, local H2 and Supabase PostgreSQL profiles, Flyway baseline, and H2 integration tests. No feature routes, admin login, Supabase connection, storage, or email integration yet. |
| [frontend/src/App.tsx](./frontend/src/App.tsx) | In progress | Retains the existing hero and presents only upcoming-event, contact, and footer content below it. Hero navigation points to the implemented event section. |
| [frontend/src/pages/TeamPage.tsx](./frontend/src/pages/TeamPage.tsx) | Implemented | Separate `/team` route showing the migrated public roster, including 19 profiles loaded from the live public Team page, empty states for groups without entries, and no private contact fields. |
| [frontend/src/pages/TeamPage.css](./frontend/src/pages/TeamPage.css) | Implemented | Responsive dark aerospace layout with simple, spacious portrait cards and a left-aligned grid. |
| [frontend/src/data/team.ts](./frontend/src/data/team.ts) | Implemented | Public roster copied from legacy team data and current approved profiles rendered on the live site; email fields excluded and profile images stored locally. |
| [frontend/src/components/SiteHeader.tsx](./frontend/src/components/SiteHeader.tsx) | Implemented | Shared site navigation; Team link opens the `/team` route. |
| [frontend/src/components/HomeContactForm.tsx](./frontend/src/components/HomeContactForm.tsx) | UI implemented; delivery pending | Required name/email/subject/message, optional PDF/JPEG/PNG attachment (10 MB), validation, submission status, and honest failure/email fallback. `/api/contact` does not exist. |
| [frontend/src/components/GridVignetteBackground.tsx](./frontend/src/components/GridVignetteBackground.tsx) | Implemented | Section-scoped grid and radial-vignette background for the upcoming-event section. |
| [frontend/src/App.css](./frontend/src/App.css), [frontend/src/index.css](./frontend/src/index.css) | In progress | Responsive existing hero and Home section styling; contact matches the landing page charcoal/off-white/copper palette and project fonts. |
| `memory.md` | Active tracker | Update after each project change; do not treat as a detailed task log. |

**Under development:** Phase 1 frontend/backend foundations and Phase 2 Home
page (partial); event data, contact API, remaining public routes, admin
authentication, and provider integrations remain unimplemented.
**Completed implementation phases:** Planning/documentation only.

## Decisions recorded

- Frontend: Vite + React + TypeScript SPA.
- Frontend routing: React Router 7, selected for separate public routes.
- Backend: Java 21, Spring Boot 3.x, Maven, modular monolith.
- Persistence: PostgreSQL hosted by Supabase; Supabase Storage for files.
- Backend local/test profiles: file-backed/in-memory H2 respectively; the
  `supabase` profile uses server-only PostgreSQL connection variables. No
  Supabase credentials or external service connections are present.
- Email: Resend.
- Admin login: Spring Security username/password; email identifier, verified
  email password reset, password-only at launch. Owner provisions initial
  accounts; admins have equal permissions. Prevent deleting the last active
  admin and confirm self-removal.
- Admin session: server-side session cookies selected; cookie configuration
  and exact-origin credentialed CORS are in the backend foundation. Login
  endpoints, CSRF token flow, password/reset policy, and durable production
  session storage remain future work.
- Selected-recruit profiles: one shared secret link, manually rotated; no
  recruit account and no automatic profile workflow email.
- Capacity: temporarily hold capacity during checkout; duration and expiry
  mechanics remain undecided.
- Design: 21hrs.space informs visual atmosphere and scroll continuity, not
  the Home content sequence. Home uses charcoal/off-white/copper styling,
  Space Grotesk, and IBM Plex Mono. See `design.md`.
- Homepage: Hero → upcoming-event section → contact details/form → footer.
  Keep Mission, Projects, Journey, and a full Events directory off Home.
  Retain the existing hero. A camera-feed/telemetry toggle is not in the
  current scope. Header links target Home, the upcoming-event section, and
  Team.
- Team page: separate `/team` route; use dark aerospace styling, with Faculty
  Coordinators, Alumni, then 4th-, 3rd-, 2nd-, and 1st-year groups. The
  roster includes legacy public members and currently published profiles
  from the live site's public directory; year labels follow its current-year
  grouping. Portraits are copied into frontend public assets. Email and phone
  fields are excluded. Keep empty states for groups without roster entries;
  future backend integration remains pending.
- Projects page: separate `/projects` route linked in desktop and mobile
  navigation. Includes all nine projects displayed on the previous public
  Projects page in a newest-first GSAP ScrollTrigger timeline, with local
  copies of their images and typed static data. Competition placements and
  historical claims require club verification before production; future
  admin-managed project content remains unconnected.
- Events page: separate `/events` route linked in desktop and mobile
  navigation. Shows an upcoming-events empty state and a newest-first GSAP
  timeline for four events documented on the previous public site. Event
  publishing and registration remain unimplemented.
- Journey page: separate `/our-journey` route linked in desktop and mobile
  navigation. Includes 11 year-labeled public milestones in newest-first order
  with locally copied photos. Historical details/results need club review;
  admin-managed content remains unconnected.
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
  unchanged. The upcoming-event section shows an empty state and a section-
  scoped grid with a radial vignette, but is not connected to published events.
  The contact form has no backend; it currently reports the unavailable
  `/api/contact` request and offers the email fallback.
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
