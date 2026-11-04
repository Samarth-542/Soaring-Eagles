# Soaring Eagles — Master Project Guide

This document combines information from the existing project's README files with the selected direction and build sequence for the new Soaring Eagles website.

> **Status:** Working guide. The new Vite/React/TypeScript frontend has a partial Home prototype; the Spring Boot backend and remaining public routes are still planned. See `phase.md` for current task status.

## 1. Project boundaries

- **New project:** `Soaring/` — separate Git repository connected to `https://github.com/Samarth-542/Soaring-Eagles.git`.
- **Reference project:** the original `SE-website` clone in the parent folder. Use it to research club content, routes, images, and existing workflows; do not make new-project commits or pushes from that repository.
- The new project was initialized separately. Its `assets/` folder currently contains a copy of the club logo and a hero image from the reference project.
- Nothing in this guide authorizes a push. Review, commit, and push only when requested.

## 2. What the reference README files say

### `README.md`

The reference site was made with:

- Vite
- React
- TypeScript
- shadcn/ui
- Tailwind CSS

It documents installing dependencies with `npm install`, starting the development server with `npm run dev`, and deploying through Lovable. It also mentions editing through an IDE, GitHub, or Codespaces.

### `README_ADMIN.md`

The reference project includes a separate Express admin server. Its documented responsibilities include privileged admin operations and member-profile moderation. The README describes:

- A server-only Supabase service-role key and admin-email allowlist.
- A client Supabase URL and publishable/anonymous key.
- Separate frontend and admin-server development processes.
- Admin requests authenticated with a Supabase access token.
- An existing-team-member profile flow that is separate from the recruitment / “Join Us” flow.
- Admin approval or rejection of member profiles; approved profiles can appear on the team page.
- Related content-card and hidden-team-member admin functionality.

These are capabilities and constraints of the reference project. Decide which ones belong in the new site before implementing them.

### `README_EMAIL_SETUP.md`

The reference describes EmailJS-based one-time-password email with service, template, and public-key configuration, plus required template variables. Treat the fallback that shows an OTP in a toast as local testing behavior only; it is not appropriate for production authentication.

## 3. Reference implementation inventory

This section was cross-checked against the reference `package.json` and `src/App.tsx`, not inferred from the README alone.

### Main technologies and libraries in the reference

| Area | Reference implementation |
|---|---|
| UI | React 18, TypeScript 5.8, Vite 5 |
| Styling and components | Tailwind CSS 3, shadcn/ui, Radix UI |
| Navigation | React Router 6 with hash-based routing |
| Server-state / data fetching | TanStack React Query |
| Forms and validation | React Hook Form, Zod, `@hookform/resolvers` |
| Database, auth, and storage | Supabase JavaScript client |
| Privileged admin API | Node.js, Express 4, CORS |
| Email OTP | EmailJS browser SDK |
| UI utilities | Framer Motion, Lucide icons, Sonner |
| Other packages | Google APIs, XLSX, Recharts, date-fns |
| Quality and build | ESLint, TypeScript, Vite build |

Reference npm scripts include `dev`, `build`, `build:dev`, `lint`, `preview`, `admin-server`, and `start:all`. The last one starts the frontend and admin server together.

### Existing routes documented in `src/App.tsx`

- `/` — home
- `/team` — team
- `/projects` — projects
- `/events` — past events
- `/join-us` — recruitment
- `/member-profile` — existing-member profile submission
- `/upcoming-events` — legacy route to events
- `/our-journey` — journey
- `/contact` — contact
- `/admin/login` and `/admin` — admin sign-in and protected dashboard

Use this as a reference inventory, not as a required final sitemap.

## 4. Selected direction and remaining stack decisions

The owner selected **Vite + React + TypeScript** for the frontend and a
**Java 21 + Spring Boot 3.x + Maven modular monolith** for the backend.
**PostgreSQL hosted by Supabase**, **Supabase Storage**, and **Resend** for
transactional email are also selected. These choices replace the old site's
Express/Supabase-JavaScript implementation; the new Spring application accesses
PostgreSQL through its backend persistence layer.

1. **Frontend:** Vite + React + TypeScript SPA; use a separate HTTP API client
   for Spring Boot REST endpoints.
2. **Styling, routing, and forms:** The current prototype uses project CSS,
   React Router 7, native React form controls, Space Grotesk, and IBM Plex Mono.
   No form library is selected; validate all requests again in Spring Boot.
3. **Backend:** Java 21 + Spring Boot 3.x + Maven, organized as a modular
   monolith. Spring Web, Spring Security, Spring Data JPA, and Bean Validation
   are the planned building blocks.
4. **Database and migrations:** PostgreSQL hosted by Supabase. Spring Data JPA
   and Flyway are the planned persistence and migration tools.
5. **Authentication and files:** Spring Security username/password with
   managed credentials; password-only is the selected launch policy. Store
   files in Supabase Storage, keeping pending profile images and contact
   attachments private.
6. **Privileged operations:** Payment verification, admin actions, and
   provider secrets belong in Spring Boot, never in browser-delivered
   configuration.
7. **Email and payment:** Resend is selected for transactional email. INR
   payment provider remains open and must be selected before production payment
   work.
8. **Testing and checks:** Use ESLint and TypeScript checks for the frontend,
   JUnit/Spring tests for the backend, plus browser tests for critical flows.
9. **Deployment:** Host the frontend and Spring Boot API as separate services;
   hosting provider and domain are undecided. Prefer the existing domain if
   available. Store secrets in each hosting provider's secret store.

Do not install every dependency from the reference manifest automatically. Add libraries only when a confirmed feature needs them. In particular, charting, spreadsheet export, Google APIs, animation, and email integrations should be included only if the new requirements call for them.

## 5. Suggested build sequence

Each phase should finish with reviewable behavior and a quality check before the next phase.

### Phase 0 — Agree on scope

- Confirm the audience, purpose, visual direction, target devices, launch target, and page list.
- Decide which reference content, imagery, forms, and administrative workflows should carry over.
- Separate confirmed requirements from ideas for later.
- Record any old-site behavior that must remain distinct, such as recruitment versus existing-member profile submission, if both features are retained.

**Gate:** agreed sitemap and feature list; no unresolved decisions that would change the data model or architecture.

### Phase 1 — Initialize the new application

- Initialize the frontend in the new repository; do not copy the old application's code wholesale.
- Choose and document the Node.js version and package manager.
- Add only the selected framework, styling, routing, and quality-check dependencies.
- Set up TypeScript, linting, formatting conventions, environment-variable templates, and ignore rules.
- Establish useful scripts for development, lint, build, preview, and tests.

**Gate:** a clean install, development server, lint, and production build all work.

### Phase 2 — Content, sitemap, and design foundation

- Verify club facts, names, dates, achievements, links, and recruitment details against current club-approved information.
- Define page ownership and navigation.
- Apply the owner-selected visual direction from `design.md`; use the owner-approved `assets/drone/assault_drone_concept.glb`, preserve creator attribution, and verify font/model license records.
- Keep the confirmed Home sequence: existing hero, upcoming-event section, contact details/form, footer. Use charcoal/off-white/copper styling and the current project fonts. There is no live-feed toggle in the approved scope.
- Make initial desktop and mobile layouts before building data-backed features.

**Gate:** page and content review; responsive foundation approved.

### Phase 3 — Public information pages

- Build the shared header, navigation, footer, and accessible page layout.
- Continue the Home prototype below its existing hero without changing the hero: upcoming-event placeholder/canvas, contact information and form, then footer. Build Team, Projects, Events, Journey, and Contact as separate routes in later work.
- Start with content that can be safely stored as local typed data; avoid a database when it does not yet add value.
- Add image descriptions, empty states, page titles, metadata, and accessible focus states.

**Gate:** all agreed public routes work on mobile and desktop; content and links reviewed.

### Phase 4 — Forms and operational workflows

- Add only approved forms and define their submission, validation, confirmation, and failure states.
- Keep recruitment and existing-member profile submissions as distinct flows if both are required.
- Decide the canonical data schema before connecting forms to persistence.
- Add server-side or database-side validation in addition to client-side form validation.

**Gate:** valid and invalid submissions have clear behavior; data goes to the intended destination only.

### Phase 5 — Database and authentication

- Model the approved entities and relationships; write versioned SQL migrations.
- Implement persistence through the Spring Boot API and PostgreSQL; select and
  configure a migration tool.
- Configure Spring Security username/password authentication with managed
  credentials and Supabase Storage access policies. Supabase Auth is not
  selected.
- Keep browser configuration separate from backend-only secrets.
- Test unauthorized, authenticated, and authorized access paths.
- Validate the user's identity and admin authorization in Spring Boot for
  every privileged action.

**Gate:** security rules and data access have been tested; no secrets are
committed. Password-only admin authentication is the selected launch policy;
confirm credential recovery and lockout controls.

### Phase 6 — Admin and member operations (if required)

- Implement admin sign-in and protected routes.
- Add only approved dashboard features, such as managing registrations, content cards, or member-profile submissions.
- Restrict approve/reject/publish actions to authorized admins.
- Make public team listings query only approved/published records, grouped by the agreed year or category.
- Keep audit-friendly error handling and confirmation states for destructive or publishing actions.

**Gate:** end-to-end member/admin/public display flow verified with representative test records.

### Phase 7 — Release readiness

- Check phone, tablet, desktop, keyboard navigation, contrast, reduced motion, and image loading.
- Run lint, type-check/build, unit/component tests, and critical browser flows.
- Review metadata, broken links, forms, data policies, logging, and production environment settings.
- Deploy a preview first; get approval before production domain changes or public launch.
- Document local setup, environment variable names (never values), migrations, deployment, and administrator procedures.

**Gate:** preview reviewed and approved; production secrets and rollback path ready.

## 6. Proposed folder structure

The structure should grow with confirmed scope. A reasonable initial layout is:

```text
Soaring/
  assets/
  frontend/
    src/
      app/
      pages/
      routes/
      components/
      features/
      data/
      lib/
      styles/
    public/
    package.json
  backend/
    src/main/java/<package>/
      config/
      security/
      events/
      registrations/
      profiles/
      contacts/
    src/main/resources/
      application.yml
      db/migration/
    src/test/java/<package>/
    pom.xml
  tests/
  .env.example
  .gitignore
  README.md
```

The framework and build tool are selected as Vite/React and Maven. Keep
privileged code in the Spring Boot backend, not in the browser app.

## 7. Local development and delivery workflow

The frontend package manifest and development scripts exist under `frontend/`.
Run these commands from that directory:

```powershell
npm install
npm run dev
npm run lint
npm run build
npm run preview
```

The Spring Boot backend has not been scaffolded and will have its own Maven
build and test commands once implemented.

For each feature:

1. Confirm the requirement and acceptance criteria.
2. Implement the smallest complete vertical slice.
3. Run focused tests and lint/build checks.
4. Review the browser behavior at mobile and desktop sizes.
5. Review the diff and confirm the reference repository was not changed.
6. Commit only when requested or agreed; push only to the new repository when explicitly ready.

## 8. Configuration and security notes

- Use placeholder values in `.env.example`; keep real local values in ignored environment files and configure production values in the host's secret store.
- Browser-exposed frontend configuration is not secret. Never put database credentials, payment/email secrets, or other privileged credentials in it.
- Enforce authorization in Spring Boot and, where supported, database policies; hiding an admin button in the UI is not security.
- Do not use a client-side toast to expose an OTP outside local-only testing.
- The reference project's `.env.example` contains credential-looking values rather than placeholders. Do not copy those values into the new project; treat any real credentials as potentially exposed and rotate them with their providers.
- Do not copy registrations, member records, private contact details, or other personal data into the new project without authorization and a migration plan.

## 9. Reference files reviewed

- `README.md` — original stack, setup, and Lovable deployment notes.
- `README_ADMIN.md` — admin server setup, member profile feature requirements, and security notes.
- `README_EMAIL_SETUP.md` — EmailJS OTP setup notes.
- `package.json` — actual dependency and npm-script inventory.
- `src/App.tsx` — existing route inventory.
- `server/admin-server.js`, `.env.example`, and the Supabase client — reference implementation and configuration/security cross-checks.

---

This guide is the working overview for planning the rebuild. Update it when the owner confirms the final features, technology decisions, deployment target, and build order.
