# Project Memory

> Working tracker for future development sessions. Update this file after each
> project change so completed work, active files, and open decisions stay
> current. This is a status summary, not a replacement for the source documents.

## Project boundary

- `Soaring/` is the new website project; it contains the application,
  documentation, and reviewed assets.
- The repository root is the older website and is reference-only. Do not edit
  it as part of new-site work unless the owner explicitly requests that.
- Reuse only owner-approved public content and media. Do not migrate personal,
  registration, contact, or payment data.

## Current status

**Current implementation snapshot (4 November 2026).** The Vite/React/
TypeScript frontend and Java 21/Spring Boot backend are functional. Published
Team, Projects, Journey, and Event content is database-backed; authenticated
admin workflows include events, registrations, content editing, image uploads,
and admin-account management. Individual/team registration, payment
verification logic, capacity holds, and email outbox/retry are implemented.
The backend suite passes 29 tests; frontend lint and production build pass.
The owner reports the Supabase database is connected and the `se_data` bucket
is public. Production Storage configuration, Razorpay sandbox/webhook testing,
and live Resend delivery have not been independently verified. Contact
submission/inbox, selected-recruit moderation, site settings, password reset,
About, CI, and production deployment remain incomplete.

| File | Status | Purpose / next action |
|---|---|---|
| [prd.md](./prd.md) | Complete; living requirements | Product scope, acceptance criteria, entities, and decisions. Keep aligned with approved changes. |
| [architecture.md](./architecture.md) | Complete; living architecture | App flows, proposed structure, and selected technology direction. Update when architecture decisions change. |
| [rules.md](./rules.md) | Complete | Engineering and AI-agent guidelines. |
| [MASTER_PROJECT_GUIDE.md](./MASTER_PROJECT_GUIDE.md) | Complete; living guide | Project boundary, selected direction, and build guidance. Keep consistent with the PRD and architecture. |
| [phase.md](./phase.md) | Active execution tracker | Phases, task status, exit gates, verification notes, and requirement mapping. |
| [design.md](./design.md) | Updated; living design brief | Current Home order, Team design direction, charcoal/off-white/copper theme, Space Grotesk/IBM Plex Mono, CSS grid vignette, accessibility, and asset guidance. |
| [README.md](./README.md) | Current project guide | Current implementation, local setup, API, Supabase Storage, and remaining production gates. |
| [assets/README.md](./assets/README.md) | Updated; asset guidance | Lists the drone model and its attribution/license handling alongside the existing reviewed assets. |
| [frontend/](./frontend/) | Implemented; active development | Vite/React/TypeScript public site with API-backed event/team/project/journey pages and authenticated admin workflows; contact delivery and several planned public/admin workflows remain incomplete. |
| [frontend/src/pages/ProjectsPage.tsx](./frontend/src/pages/ProjectsPage.tsx) | Implemented; database-backed | Public `/projects` page loads published content from the API; admins manage entries and images. Historical results/content need owner verification. |
| [frontend/src/components/ui/timeline-01.tsx](./frontend/src/components/ui/timeline-01.tsx) | Implemented | Reusable project timeline component; newest-first entries, alternating desktop cards, mobile rail, GSAP ScrollTrigger progress/reveals, and reduced-motion support. |
| [frontend/src/pages/EventsPage.tsx](./frontend/src/pages/EventsPage.tsx) | Implemented; API-backed | Published upcoming events and details load from the backend; local historical archive remains static. Registration and admin workflows are connected. |
| [frontend/src/components/ui/event-timeline.tsx](./frontend/src/components/ui/event-timeline.tsx) | Implemented | Responsive newest-first timeline with GSAP ScrollTrigger progress/reveals and reduced-motion support. |
| [frontend/src/pages/OurJourneyPage.tsx](./frontend/src/pages/OurJourneyPage.tsx) | Implemented; database-backed | Public `/our-journey` page loads published milestones from the API; admin editing and image upload are connected. Historical results require club verification. |
| [frontend/src/components/ui/journey-timeline.tsx](./frontend/src/components/ui/journey-timeline.tsx) | Implemented | Responsive milestone timeline with GSAP ScrollTrigger progress/reveals and reduced-motion support. |
| [frontend/src/pages/AdminPortalPreview.tsx](./frontend/src/pages/AdminPortalPreview.tsx) | Connected admin dashboard | `/admin` verifies the backend session and exposes connected Events, Registrations, Team, Projects, Journey, and Admin accounts modules; `/admin-preview` remains a public design preview. |
| [frontend/src/pages/AdminLoginPreview.tsx](./frontend/src/pages/AdminLoginPreview.tsx) | Connected sign-in | Admin login uses the Spring Boot session API; the initial account is bootstrapped from server-only configuration when absent. |
| [backend/](./backend/) | Feature implementation in place | Java 21/Spring Boot 3.5 app with event, registration, content, payment, email, authentication, account-management, and image-upload modules; local H2 and Supabase PostgreSQL profiles; Flyway migrations; and integration tests. |
| [frontend/src/App.tsx](./frontend/src/App.tsx) | Implemented | Public routes include Home, Team, Projects, Journey, Events, and registration flows; contact submission is not connected. |
| [frontend/src/pages/TeamPage.tsx](./frontend/src/pages/TeamPage.tsx) | Implemented; database-backed | Public Team content loads from the API; private contact fields are excluded and empty states are supported. |
| [frontend/src/pages/TeamPage.css](./frontend/src/pages/TeamPage.css) | Implemented | Responsive dark aerospace layout with simple, spacious portrait cards and a left-aligned grid. |
| [frontend/src/pages/admin/AdminAccountManagement.tsx](./frontend/src/pages/admin/AdminAccountManagement.tsx) | Implemented | Admins can create accounts and activate/deactivate them; password reset and permanent deletion are not exposed. |
| [frontend/src/components/SiteHeader.tsx](./frontend/src/components/SiteHeader.tsx) | Implemented | Shared site navigation; Team link opens the `/team` route. |
| [frontend/src/components/HomeContactForm.tsx](./frontend/src/components/HomeContactForm.tsx) | UI implemented; delivery pending | Required name/email/subject/message, optional PDF/JPEG/PNG attachment (10 MB), validation, submission status, and honest failure/email fallback. `/api/contact` does not exist. |
| [frontend/src/components/GridVignetteBackground.tsx](./frontend/src/components/GridVignetteBackground.tsx) | Implemented | Section-scoped grid and radial-vignette background for the upcoming-event section. |
| [frontend/src/App.css](./frontend/src/App.css), [frontend/src/index.css](./frontend/src/index.css) | Implemented | Responsive hero and Home section styling; contact matches the landing-page palette and project fonts. |
| `memory.md` | Active tracker | Update after each project change; do not treat as a detailed task log. |

**Active gaps:** Contact request delivery/inbox, selected-recruit submission
and moderation, public site settings, password reset/change, About page,
provider sandbox/live-delivery verification, CI, and production release.

## Decisions recorded

- Frontend: Vite + React + TypeScript SPA.
- Frontend routing: React Router 7, selected for separate public routes.
- Backend: Java 21, Spring Boot 3.x, Maven, modular monolith.
- Persistence: PostgreSQL hosted by Supabase; Supabase Storage for files.
- Backend local/test profiles: file-backed/in-memory H2 respectively; the
  `supabase` profile uses server-only PostgreSQL connection variables. The
  owner reports the Supabase database is connected; local production
  credentials are not available for independent verification.
- Email: Resend.
- Admin login: Spring Security username/password; email identifier; initial
  account bootstrapped from server configuration; admins have equal
  permissions. Database-backed sign-in, session cookies, CSRF, throttling,
  account creation/activation/deactivation, audit events, session invalidation,
  and last-active-admin protection are implemented. Password reset/change,
  self-removal, and permanent account deletion remain incomplete against the
  full PRD requirement.
- Admin session: server-side JDBC sessions, CSRF token flow, exact-origin
  credentialed CORS, and login throttling are implemented. Production cookie,
  database, and origin configuration still requires deployment verification.
- Selected-recruit profiles: one shared secret link, manually rotated; no
  recruit account and no automatic profile workflow email.
- Capacity: temporarily hold capacity during checkout for 10 minutes, then
  release on expiry; application behavior is implemented. Provider and
  concurrency verification remains pending.
- Design: 21hrs.space informs visual atmosphere and scroll continuity, not
  the Home content sequence. Home uses charcoal/off-white/copper styling,
  Space Grotesk, and IBM Plex Mono. See `design.md`.
- Homepage: Hero → upcoming-event section → contact details/form → footer.
  Keep Mission, Projects, Journey, and a full Events directory off Home.
  Retain the existing hero. A camera-feed/telemetry toggle is not in the
  current scope. Header links target Home, the upcoming-event section, and
  Team.
- Team page: separate `/team` route backed by published database content;
  admins manage records and images. Private email/phone fields are excluded.
  Selected-recruit submissions and moderation remain pending.
- Projects page: separate `/projects` route backed by published database
  content, with authenticated admin editing and image uploads. Competition
  placements and historical claims require club verification before launch.
- Events page: separate `/events` route backed by published event APIs, with
  registration and authenticated event/registration administration. Historical
  archive entries remain static and require content review.
- Journey page: separate `/our-journey` route backed by published database
  milestones, with authenticated admin editing and image uploads. Historical
  details/results need club review.
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
- Frontend status: Home has the four agreed parts, with published upcoming
  events. The contact form has no backend; it reports the unavailable
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
  The event area displays published events or an explicit empty state.
- Launch: full PRD scope targeted for 15 December 2026; schedule feasibility
  needs an estimate and owner confirmation.
- Analytics: no analytics or non-essential cookies at launch.
- Accessibility: follow best practices; no formal conformance claim selected.
- Delivery: GitHub pull requests, automated CI, and preview deployments.

## Open decisions / gates

1. Verify Razorpay sandbox/webhook behavior, expiry/retry and concurrency
   cases, and keep paid checkout disabled until these pass.
2. Select frontend/backend hosting, confirm domain availability, and identify
   deployment/DNS/TLS owners.
3. Confirm data-retention periods and privacy/terms requirements with the
   owner/legal reviewer.
4. Define selected-recruit shared-link handling, profile submission, and
   moderation workflow.
5. Implement contact submission, inbox/replies, public site settings, and
   password recovery.
6. Verify Supabase production environment variables and Storage uploads; the
   owner reports the database is connected and `se_data` is public.
7. Verify Resend sender-domain authentication and live delivery.
8. Approve migrated public content, facts, media, supported browser matrix,
   accessibility checklist, and creator-credit records. The Home sequence,
   palette, and typography are recorded in `design.md`.
9. Add CI and production deployment/release checks; estimate remaining scope
   against the launch target with actual team capacity.

The detailed decision tasks are in [phase.md](./phase.md), Phase 0.

## Update protocol

After each requested project change:

1. Update the changed source document(s) and implementation as appropriate.
2. Update this file's current status, file table, decisions, open gates, and
   phase/task status if affected.
3. Record what was actually verified; do not mark work complete based on
   planned or untested behavior.
4. Keep this summary concise and link to the authoritative project documents.
