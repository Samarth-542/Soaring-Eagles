# Soaring Eagles Website

The new website for **Soaring Eagles**, the student aerodesign and aerial
robotics club at Siddaganga Institute of Technology in Tumakuru, India. This
repository is the rebuild; the older website in the parent directory is a
reference project, not the application described here.

> **Project status:** The Vite frontend contains a Home-page prototype and a
> Team page populated with the previous site's public roster and profiles
> currently displayed in its public directory. A foundational Spring Boot
> backend now provides health checks, protected API defaults, a Flyway baseline,
> and an isolated local database. Feature APIs, admin login, remaining public
> pages, and production integrations are still planned.

## What is here now

The current Home prototype includes:

- A cinematic hero with an interactive 3D drone model.
- An upcoming-events section with an honest empty state (not connected to
  event data yet).
- Club contact information and a validated contact form.
- A responsive layout, reduced-motion support, and a footer.
- A separate `/projects` page with a GSAP-animated chronological timeline of
  nine projects, descriptions, tags, and locally served photos migrated from
  the previous public site.
- A separate `/events` page with an upcoming-events empty state and a
  GSAP-animated timeline of four past public events.
- A separate `/our-journey` page with 11 newest-first milestones and locally
  served photos from the previous public site.
- A separate `/team` route with the migrated faculty, alumni, and student
  roster, including currently public member profiles and portraits; groups
  without entries have empty states. Private email and phone fields are not
  displayed.

The contact form's `/api/contact` endpoint does not exist yet, so messages are
not delivered by the website. The form explains this and provides an email
fallback: [tseofficial2011@gmail.com](mailto:tseofficial2011@gmail.com).
Navigation links and the remaining public routes are still being developed.

The drone is an owner-approved third-party concept, not a Soaring Eagles
aircraft design. The page credits its creator, [Diamonddogkz on
Sketchfab](https://sketchfab.com/Diamonddogkz).

## Technology direction

**Implemented frontend:** Vite, React, TypeScript, React Router, Three.js,
React Three Fiber, Drei, and GSAP.

**Backend foundation:** Java 21, Spring Boot 3.5, and Maven Wrapper. Local
development uses an H2 database; the `supabase` Spring profile is configured
for PostgreSQL using server-side environment variables. Supabase Storage and
Resend are planned integrations and are not connected yet.

## Run the frontend locally

### Requirements

- Node.js and npm

### Start the development server

From a terminal, clone the new website repository and enter its frontend
directory:

```powershell
git clone https://github.com/Samarth-542/Soaring-Eagles.git
cd Soaring-Eagles\frontend
npm install
npm run dev
```

Open the local URL printed by Vite, usually `http://localhost:5173`.

The current frontend prototype does not require environment variables. Its
contact API and event-data connection are not implemented.

## Run the backend locally

### Requirements

- Java 21 or newer

The Maven Wrapper downloads the project's pinned Maven version on first use.
No separately installed Maven or local PostgreSQL server is required for
backend development.

### Start the API

From a terminal, enter the backend directory:

```powershell
cd Soaring-Eagles\backend
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

The API starts at `http://localhost:8080`. Its public health check is
`http://localhost:8080/actuator/health`. Local development uses a file-backed
H2 database under `backend/.local/`; this database is ignored by Git. All API
routes under `/api/` require authentication until feature-specific public and
admin routes are implemented; the actuator health check is public. Browser
authentication is planned to use server-side sessions with HttpOnly cookies
and CSRF protection; no login/session endpoints or production session store
exist yet.

### Connect to a Supabase PostgreSQL database

Set the connection values in the shell that starts the application. Copy
variable names from `backend/.env.example`; do not commit real credentials.
For PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE = "supabase"
$env:SUPABASE_DB_URL = "jdbc:postgresql://<host>:5432/postgres?sslmode=require"
$env:SUPABASE_DB_USERNAME = "<database-user>"
$env:SUPABASE_DB_PASSWORD = "<database-password>"
.\mvnw.cmd spring-boot:run
```

This profile runs the Flyway baseline against the configured database. Set
`APP_CORS_ALLOWED_ORIGINS` in deployment to a comma-separated list of exact
frontend origins. Do not use a wildcard with credentialed CORS, or point the
profile at production until the Supabase project and environment are confirmed.
Supabase Storage, email delivery, and admin login are not implemented by this
foundation.

### Other backend commands

Run these from `Soaring-Eagles\backend`:

```powershell
.\mvnw.cmd test       # Run isolated integration tests against in-memory H2
.\mvnw.cmd verify     # Run tests and verify the Maven build
.\mvnw.cmd package   # Build the executable Spring Boot JAR
```

### Other frontend commands

Run these from `Soaring-Eagles\frontend`:

```powershell
npm run lint     # Check the frontend with ESLint
npm run build    # Type-check and create a production build in dist/
npm run preview  # Preview the production build locally
```

## Repository layout

- `assets/` — reviewed brand, drone, event, project, team, and site assets.
- `frontend/` — the Vite website prototype.
- `backend/` — Spring Boot API foundation, Maven Wrapper, isolated tests, and
  local/Supabase database profiles.
- `architecture.md` — application boundaries and planned technical design.
- `design.md` — visual direction, homepage scope, accessibility, and asset use.
- `prd.md` — product requirements and acceptance criteria.
- `phase.md` — implementation phases, task status, and verification plan.
- `MASTER_PROJECT_GUIDE.md` — project context, decisions, and delivery workflow.
- `rules.md` — engineering and development guidelines.
- `memory.md` — current project status and open decisions.

## Planned scope

The full product is intended to include public About, Team, Projects, Events,
Journey, and Contact pages; event registration and payment; selected-recruit
profile submission with admin moderation; and a protected admin portal for
content and operations. These are roadmap items, not features of the current
prototype. See [prd.md](./prd.md) and [phase.md](./phase.md) for requirements
and implementation status.

## Project boundaries and security

- Work in this standalone repository for the new website. Keep the older
  website in the parent directory unchanged unless explicitly asked otherwise.
- Do not copy private member, registration, contact, or payment data from the
  reference project.
- Never commit credentials or secrets. Privileged configuration belongs on
  the future backend, not in browser-exposed frontend variables.
- Review [assets/README.md](./assets/README.md) for asset permissions and
  attribution guidance.
