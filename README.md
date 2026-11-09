# Soaring Eagles Website

The new website for **Soaring Eagles**, the student aerodesign and aerial
robotics club at Siddaganga Institute of Technology in Tumakuru, India. This
repository is the rebuild; the older website in the parent directory is a
reference project, not the application described here.

> **Project status (5 November 2026):** Public event discovery and individual/
> team registration, authenticated event/registration management, database-
> backed Team/Projects/Journey content, image uploads, and admin-account
> management are implemented, as is the contact inbox. The backend uses cookie-based admin sessions,
> CSRF protection, login throttling, capacity holds, signed payment webhooks,
> and a Resend email outbox. The owner reports the Supabase database is
> connected and the `se_data` bucket is public; production Storage configuration
> has not been independently verified. Paid checkout remains disabled pending
> Razorpay sandbox/webhook testing, and live Resend delivery is unverified.
> Selected-recruit moderation,
> password reset, several public pages, CI, and production deployment remain
> incomplete.

## What is here now

The current Home includes:

- A cinematic hero with an interactive 3D drone model.
- An upcoming-events section backed by published event data, with explicit
  loading, error, and empty states.
- Club contact information and a validated contact form that saves submissions
  for review in the authenticated admin inbox.
- A responsive layout, reduced-motion support, and a footer.
- A separate `/projects` page with a GSAP-animated timeline of published
  database-backed project content.
- A separate `/events` page with published upcoming events, public event
  details, and a GSAP-animated timeline of four static past public events.
- A separate `/our-journey` page with newest-first database-backed milestones.
- An `/admin-preview` design-only route for reviewing the planned management
  console; it contains no live or sample records.
- An `/admin-preview/login` sign-in flow connected to Spring Security and
  `/admin`, a server-verified session status page. Authenticated Events,
  Registrations, Contact inbox, Team, Projects, Journey, and Admin accounts
  management modules are connected to protected APIs.
- A separate `/team` route backed by published Team content from the API;
  groups without entries have empty states. Private email and phone fields are
  not displayed.

Contact form submissions are stored in the database and can be reviewed,
marked read/unread, or archived in `/admin` → Contact inbox. No email is sent
when the form is submitted. Optional attachments are stored in a private
Supabase Storage bucket and can be downloaded by signed-in admins.
Event registration uses a 10-minute capacity hold and confirms paid entries
only after a valid Razorpay webhook. Remaining capacity is returned by the
event API. Paid checkout remains disabled until Razorpay test credentials and
a reachable webhook have been verified; Resend email delivery also needs a
configured, verified sender. Navigation links and the remaining public routes, including About, are still
being developed.

The drone is an owner-approved third-party concept, not a Soaring Eagles
aircraft design. The page credits its creator, [Diamonddogkz on
Sketchfab](https://sketchfab.com/Diamonddogkz).

## Technology direction

**Implemented frontend:** Vite, React, TypeScript, React Router, Three.js,
React Three Fiber, Drei, and GSAP.

**Backend:** Java 21, Spring Boot 3.5, and Maven Wrapper. Local development
uses an H2 database; the `supabase` Spring profile is configured for PostgreSQL
using server-side environment variables. Razorpay checkout/webhook, the Resend
delivery outbox, and Supabase Storage image uploads are implemented; provider
credentials and live delivery have not been verified.

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

The frontend uses the Vite development proxy for `/api` by default. For
production, set `VITE_API_BASE_URL` to the HTTPS Spring API origin; this value
is public browser configuration and must not contain credentials.

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

# Read the password interactively and print its BCrypt hash.
.\mvnw.cmd -q -DskipTests compile org.codehaus.mojo:exec-maven-plugin:3.5.0:java "-Dexec.mainClass=in.soaringeagles.api.security.AdminPasswordHashCli"

# Create this file once, then put the generated hash in it.
Copy-Item .env.local.example .env
# Open .env and replace the placeholder hash with the value from above.
notepad .env

.\mvnw.cmd spring-boot:run
```

The backend reads `.env` automatically when started from its directory. It is
ignored by Git, so the admin hash stays local and does not need to be entered
again on each start. Keep the `.env` file private; never commit it or use the
local-development template for production. If you already set
`APP_ADMIN_PASSWORD_HASH` in the shell, that environment value takes precedence.

The API starts at `http://localhost:8080`. Its public health check is
`http://localhost:8080/actuator/health`. Local development uses a file-backed
H2 database under `backend/.local/`; this database is ignored by Git. The
backend fails startup when the server-only initial admin email or BCrypt hash
is absent or invalid; it does not provide demo credentials or a bypass.
Sessions are persisted through the configured JDBC database. The login uses
an HttpOnly `SOARING_SESSION` cookie, a CSRF token, and exact-origin
credentialed CORS. Five failed attempts per email or source IP within 15
minutes trigger a 15-minute lockout; the limiter is currently process-local,
so run one backend instance unless a shared rate-limit store is added.
The provisioning helper requires matching passwords of at least 12 characters
and uses BCrypt with work factor 12.

On the first startup, the backend creates the configured initial admin account
in the database if it does not already exist. The email and password hash
environment variables do not overwrite an existing account. Additional admins
can be created and activated/deactivated in `/admin` → Admin accounts; new
passwords must be at least 12 characters. Deactivated accounts cannot sign in,
their existing sessions are invalidated, and the last active admin cannot be
deactivated. Password reset or changing an existing account's password is not
available in the portal yet.

`/admin` verifies the session and provides event management, registrations,
the contact inbox, Team/Projects/Journey content editing, and admin-account management.
`/admin-preview` remains a static design preview and is not an authenticated
admin area.

Flyway migration V6 adds the additive database foundation for admin accounts and
audit history, team profiles, projects and tags, journey milestones, contact
inbox messages, media metadata, and site settings. Existing event and
registration tables are unchanged. Supabase Storage objects are represented by
their bucket and object key; file bytes are not stored in PostgreSQL. Dashboard
analytics should be derived from operational records instead of duplicated in
counter tables.

Migration V7 seeds the current Team, Projects, and Journey website content
without overwriting matching records. Public reads are available at
`GET /api/v1/content/team`, `/projects`, and `/journey`; authenticated
management is available under `/api/v1/admin/content/`. The public Team,
Projects, and Journey pages and their admin editors read and write these
database records. Contact submissions are created at `POST /api/v1/contact`;
the authenticated inbox uses `/api/v1/admin/contact-messages` to list, view,
and update message status. Attachment downloads require an authenticated
admin session. Public contact details are available at
`GET /api/v1/content/site-details`; admins can read and update them under
`/api/v1/admin/site-details`. Admin image uploads use
`POST /api/v1/admin/content/images` and store image files in Supabase Storage,
with metadata recorded in PostgreSQL.

Admin account management is available to signed-in admins at
`GET` and `POST /api/v1/admin/accounts`, with account activation/deactivation
at `PATCH /api/v1/admin/accounts/{id}/enabled`. Account creation stores a
BCrypt password hash; plaintext passwords and hashes are never returned by the
API.

Event endpoints are:

- Public reads: `GET /api/v1/events` and `GET /api/v1/events/{id}`. Drafts and
  future scheduled events remain private.
- Admin writes (server session required): `GET` and `POST /api/v1/admin/events`,
  `PUT /api/v1/admin/events/{id}`, and `POST /api/v1/admin/events/{id}/schedule`,
  `/publish`, or `/close`; `DELETE /api/v1/admin/events/{id}` permanently deletes
  the event and its linked registrations, participant/team details, payment
  attempts, and confirmation-email history.
- Event times use India Standard Time (UTC+05:30). Admins can upload posters
  and archive photos as JPEG or PNG files.

Registration limits and deadlines are stored and deadlines are reflected in
public status. Registration and payment routes include:

- Public registration: `POST /api/v1/events/{id}/registrations`,
  `GET /api/v1/registrations/{id}/status`, and
  `POST /api/v1/registrations/{id}/retry`.
- Razorpay webhook: `POST /api/v1/payments/razorpay/webhook`; only a verified,
  signed webhook can confirm a paid registration.
- Admin registration data (authenticated): paginated event registration reads,
  registration detail/update, confirmation-email retry, and CSV export under
  `/api/v1/admin/`.

The checkout hold defaults to 10 minutes. Paid checkout is deliberately
disabled unless `APP_PAYMENT_ENABLED=true` and Razorpay key ID, key secret,
and webhook secret are configured. Do not enable it until the test-mode flow,
public webhook delivery, duplicate/failure events, and capacity behavior have
been verified. Resend confirmation delivery requires a valid API key and
verified sender address; see `backend/.env.example`. Real provider transactions
and email delivery have not yet been verified.

### Connect to a Supabase PostgreSQL database

Set the connection values in the shell that starts the application. Copy
variable names from `backend/.env.example`; do not commit real credentials.
For admin image uploads, create a public Supabase Storage bucket called
`se_data` and set `SUPABASE_URL` plus `SUPABASE_SERVICE_ROLE_KEY` in the
backend environment. Set `SUPABASE_STORAGE_PUBLIC_BUCKET` if you choose a
different bucket name. The service-role key must never be exposed to the
frontend or committed to source control. The admin uploader accepts JPEG and
PNG images up to 10 MB per image.
For contact form attachments, also create a **private** bucket named
`se_private` (or configure `SUPABASE_STORAGE_PRIVATE_BUCKET`). Contact
attachments are never stored in the public bucket; downloads are served only
after admin authentication.
For PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE = "supabase"
$env:SUPABASE_DB_URL = "jdbc:postgresql://<host>:5432/postgres?sslmode=require"
$env:SUPABASE_DB_USERNAME = "<database-user>"
$env:SUPABASE_DB_PASSWORD = "<database-password>"
$env:SUPABASE_URL = "https://<project-ref>.supabase.co"
$env:SUPABASE_SERVICE_ROLE_KEY = "<server-only-service-role-key>"
$env:SUPABASE_STORAGE_PUBLIC_BUCKET = "se_data"
$env:SUPABASE_STORAGE_PRIVATE_BUCKET = "se_private"
$env:APP_ADMIN_EMAIL = "<owner-provisioned-admin-email>"
$env:APP_ADMIN_PASSWORD_HASH = "<BCrypt-hash-from-secure-provisioning>"
$env:APP_CORS_ALLOWED_ORIGINS = "https://<frontend-origin>"
# Set to "none" only when frontend and API are cross-site; HTTPS is required.
# $env:APP_SESSION_COOKIE_SAME_SITE = "none"
.\mvnw.cmd spring-boot:run
```

This profile runs the Flyway baseline against the configured database. Set
`APP_CORS_ALLOWED_ORIGINS` in deployment to a comma-separated list of exact
frontend origins. Do not use a wildcard with credentialed CORS, or point the
profile at production until the Supabase project and environment are confirmed.
Create a public Supabase Storage bucket named `se_data` (or configure
`SUPABASE_STORAGE_PUBLIC_BUCKET`) before using admin image uploads. Keep
`SUPABASE_SERVICE_ROLE_KEY` on the backend only; never use it in the frontend or
commit it. Admin uploads accept JPEG and PNG images up to 10 MB and store them
in the bucket with their database metadata.
Analytics and admin password reset are not implemented.
Razorpay and Resend integrations require owner-provisioned
credentials; do not enable paid checkout until its sandbox flow has been
verified. Do not start the service until the initial admin hash and exact
frontend-origin allowlist are configured.

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
- `frontend/` — the Vite website.
- `backend/` — Spring Boot API, Maven Wrapper, isolated tests, and
  local/Supabase database profiles.
- `architecture.md` — application boundaries and planned technical design.
- `design.md` — visual direction, homepage scope, accessibility, and asset use.
- `prd.md` — product requirements and acceptance criteria.
- `phase.md` — implementation phases, task status, and verification plan.
- `MASTER_PROJECT_GUIDE.md` — project context, decisions, and delivery workflow.
- `rules.md` — engineering and development guidelines.
- `memory.md` — current project status and open decisions.

## Planned scope

The full product includes public Team, Projects, Events, and Journey pages;
event registration/payment; and a protected admin portal. Selected-recruit
profile submission, replies from the admin portal, password reset, and
production release remain roadmap items. See
[prd.md](./prd.md) and [phase.md](./phase.md) for requirements and status.

## Project boundaries and security

- Work in this standalone repository for the new website. Keep the older
  website in the parent directory unchanged unless explicitly asked otherwise.
- Do not copy private member, registration, contact, or payment data from the
  reference project.
- Never commit credentials or secrets. Privileged configuration belongs on
  the backend, not in browser-exposed frontend variables.
- Review [assets/README.md](./assets/README.md) for asset permissions and
  attribution guidance.
