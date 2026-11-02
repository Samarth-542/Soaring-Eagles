# Soaring Eagles Website — Development Phases

**Status:** Proposed execution plan, based on the current PRD and owner answers  
**Requirements source:** [prd.md](./prd.md)  
**Architecture:** [architecture.md](./architecture.md)  
**Engineering rules:** [rules.md](./rules.md)  
**Target launch:** 15 December 2026; owner selected full PRD scope before public launch

> **Drone asset decision (2026-11-01):** The owner confirms rights to display
> the complete GLB, including its weapon, and wants the original model axes.
> This decision supersedes any older weapon-free or mesh-hiding wording in the
> task notes below.

> **Implementation status:** Phase 1 frontend foundation and the Phase 2
> homepage are in progress. The Vite/React/TypeScript frontend, Home prototype,
> and initial `/team` UI scaffold exist under `frontend/`; T013, T023, T024,
> T026, and T030 are partially implemented and are not complete. The Spring
> Boot backend foundation now exists under `backend/`: Java 21/Spring Boot 3.5
> scaffold, health endpoint, structured HTTP errors, authenticated-by-default
> API routes, local H2 and Supabase PostgreSQL profiles, Flyway baseline, Maven
> Wrapper, and isolated integration tests. T014–T015 are complete for the
> foundation; T016's isolated test setup is implemented, while feature-level
> transaction/schema conventions remain in progress. Admin authentication,
> domain schemas, storage/email
> providers, and CI remain pending. Home currently
> has the existing hero, static upcoming-event empty state with a CSS
> grid-vignette background, contact details and form, and the existing footer.
> The Team route has a migrated public roster and empty states for year groups
> without entries; backend integration is pending. The separate `/projects`
> route now displays nine project entries and local photos from the previous
> public Projects page in newest-first GSAP ScrollTrigger timeline format;
> these static entries are not admin-managed. Historical
> results and other claims still require owner review. Event data and contact
> submissions are not backend-connected. Remaining public routes and
> production integrations have not started.
> Paths below may describe planned files that do not exist yet. The launch date is aggressive for the
> full scope, especially while payment provider, hosting/domain, retention
> policy, team availability, and selected technical details remain unresolved.
> Phase 0 must estimate the work and surface schedule risk; changing the date
> or scope requires owner approval.

## 1. Decisions already recorded

- **Frontend:** Vite + React + TypeScript SPA.
- **Frontend routing:** React Router 7 for client-side public routes.
- **Backend:** Java 21, Spring Boot 3.x, Maven, modular monolith.
- **Persistence:** PostgreSQL hosted by Supabase; Supabase Storage for files.
- **Admin authentication:** Spring Security username/password with managed
  credentials; email is the unique account identifier; verified-email password
  reset; password-only at launch.
- **Admin session:** Server-side sessions with secure, HttpOnly cookies are
  selected. Keep CSRF enabled; production session persistence and exact
  frontend origins must be configured before login is exposed.
- **Admin accounts:** All admins have equal website-management permissions.
  Prevent deleting the last active admin and require confirmation for
  self-removal. Owner provisions initial admins.
- **Email:** Resend for transactional email.
- **Payment:** INR provider remains undecided. Temporary capacity reservation
  during checkout is selected; hold duration/expiry mechanics are to be decided
  with the provider.
- **Selected-recruit profile access:** One shared secret link, manually rotated.
- **Design:** 21hrs.space is a high-level reference for dark aerospace visuals
  and scroll continuity, not for the Home content order or exact scene
  sequence. Home uses charcoal, warm off-white, restrained copper, Space
  Grotesk, and IBM Plex Mono; see `design.md`.
- **Homepage:** Hero → upcoming event → contact details/form → footer. Keep
  Mission, Projects, Journey, Events, and Team off Home. The existing hero is
  retained; a camera-feed or telemetry toggle is not in the approved scope.
- **Scroll experience:** The 21hrs.space reference informs how scrolling can
  guide a continuous, cinematic story; it does not prescribe the same scenes,
  order, or exact transitions. Use Soaring Eagles' content and identity.
- **Legacy material:** Reuse only owner-approved public content/media after
  fact review; do not migrate personal, registration, contact, or payment data.
- **Release:** Full PRD scope is required for launch on the target date.
- **Analytics:** No analytics or non-essential cookies at launch.
- **Accessibility:** Follow accessibility best practices; no formal conformance
  claim selected.
- **Delivery workflow:** GitHub pull requests, automated CI, and preview
  deployments.

## 2. Phase overview and dependencies

| Phase | Outcome | Depends on |
|---|---|---|
| 0. Decisions and readiness | Resolve high-impact open choices, approve content sources, estimate schedule, and provision access. | Owner responses and provider research |
| 1. Platform foundation | Working frontend/backend applications, database migrations, auth foundation, storage/email connections, and CI. | Phase 0 gates |
| 2. Public site and content management | Responsive public pages and authenticated administration of public editorial content. | Phase 1 |
| 3. Event publishing and administration | Draft/publish/close lifecycle, event details, capacity/deadline settings, archive, and public event discovery. | Phase 1; public shell from Phase 2 |
| 4. Event registration and payment | Individual/team checkout, verified payment states, capacity protection, admin registration operations, and confirmation/receipt. | Phases 1 and 3; payment provider selected in Phase 0 |
| 5. Selected-recruit profiles | Private shared-link submissions, moderation, and approved public team profiles. | Phases 1 and 2 |
| 6. Contact inbox and replies | Private contact requests/attachments, club notifications, admin replies, and retained delivery history. | Phases 1 and 2; Resend sender verified |
| 7. Release readiness | Complete verification, content review, production configuration, preview approval, and launch/rollback readiness. | Phases 2–6 |

Critical path: provider decisions and environment access → platform foundation →
event management → verified payment integration → end-to-end testing and
production approval. Public content, profile moderation, and contact work can
proceed in parallel after the foundation where team capacity permits.

## 3. Phase 0 — Decisions, requirements, and readiness

### Goal

Remove or explicitly accept decisions that would cause rework or block release.
No feature implementation starts until the requirements and deployment boundary
are understood.

### Work items

- [ ] T001 [Plan:0.1] Compare INR payment providers for checkout, signed webhooks, idempotency, refunds handled outside the site, receipts, sandbox access, and fees; record the selected provider in `prd.md`.
- [ ] T002 [Plan:0.1] Select frontend router, component/UI library, form-validation approach, and browser-test runner; record exact choices in `architecture.md`. (React Router 7 selected; UI library, form-validation approach, and browser-test runner remain open.)
- [ ] T003 [Plan:0.1] Choose frontend/backend hosting, confirm whether the existing domain is available, and identify DNS, TLS, preview, and production access owners; update `prd.md`.
- [ ] T004 [Plan:0.1] Confirm Supabase project ownership, PostgreSQL connection method, Storage buckets/access policy, and environment separation; record required configuration names without secrets in `architecture.md`.
- [ ] T005 [Plan:0.1] Verify Resend sender domain, DNS authentication, sender identity, and production sending limits; record the required configuration names in `architecture.md`.
- [ ] T006 [Plan:0.2] Inventory old-site text/images and mark each candidate approved, needs correction, or excluded; record owner-reviewed source and factual corrections in `docs/content-review.md`.
- [ ] T007 [Plan:0.2] Confirm privacy/terms copy, contact-attachment and registration retention periods, rejected-profile deletion behavior, and payment-data retention with the owner/legal reviewer; record decisions in `prd.md`.
- [ ] T008 [Plan:0.2] Confirm the shared recruit-link distribution/rotation owner, storage method, rate limits, exposure response, and when rotation invalidates active form sessions; record the operating procedure in `architecture.md`.
- [ ] T009 [Plan:0.3] Set password policy, login throttling/lockout, email-reset token expiry, email delivery failure recovery, admin audit requirements, and last-admin safeguards; record acceptance criteria in `architecture.md`.
- [ ] T010 [Plan:0.3] Select a finite checkout-hold duration and expiry/release behavior with the payment-provider design; document deadline/capacity race handling in `architecture.md`.
- [ ] T011 [Plan:0.3] Confirm launch content and copy for the Home hero, upcoming-event and contact sections; the 21hrs.space-inspired visual direction; font licensing; attribution/license record for `assets/drone/assault_drone_concept.glb`; accessibility checklist; supported browsers/viewports; and content approval owner in `prd.md` and `design.md`.
- [ ] T012 [Plan:0.3] Estimate each phase against the 15 December 2026 launch target using the actual team/availability; raise a go/no-go or schedule/scope change for owner approval in `phase.md`.

### Exit gate

- Payment provider, hosting/domain path, router/UI/testing selections, and
  infrastructure access are known or explicitly scheduled as blocking tasks.
- Content owners and review status are recorded; no unapproved personal data is
  planned for transfer.
- Retention, privacy/legal, shared-link handling, checkout hold, and launch
  verification decisions are documented.
- The owner has reviewed the schedule risk and approved the date/scope baseline.

## 4. Phase 1 — Platform foundation

### Goal

Create separate Vite/React and Spring Boot applications with safe configuration,
database migration, admin identity, and repeatable CI.

### Work items

- [ ] T013 [Plan:1.1] Complete the Vite + React + TypeScript application under `frontend/` with the selected router, lint/type-check/build scripts, and a working local landing route. (In progress: initial app, scripts, and homepage prototype exist; router selection and full foundation remain open.)
- [x] T014 [Plan:1.2] Scaffold a Java 21/Spring Boot 3.x Maven modular monolith under `backend/` with configuration, health endpoint, structured error handling, and feature-package boundaries. (Implemented with Spring Boot 3.5.16, Maven Wrapper, health checks, RFC-style error responses, and fail-closed API security; feature modules will be added with their domain work.)
- [x] T015 [Plan:1.3] Configure backend PostgreSQL connectivity to Supabase using server-only environment variables and add Flyway migration baseline under `backend/src/main/resources/db/migration/`. (The `supabase` profile reads server-only `SUPABASE_DB_*` variables; the initial Flyway migration is intentionally schema-neutral. No Supabase connection or production database was accessed.)
- [ ] T016 [Plan:1.3] Add shared persistence conventions, transaction boundaries, schema naming, and isolated test database setup in `backend/src/main/java/<package>/config/` and `backend/src/test/java/<package>/`. (In progress: schema validation, disabled Open Session in View, local/test H2 profiles, and migration/auth/health integration tests are in place; feature transaction boundaries and schema conventions will be set with the domain modules.)
- [ ] T017 [Plan:1.4] Implement Spring Security admin email/password authentication, initial-admin provisioning, verified-email reset, login throttling, and backend authorization in `backend/src/main/java/<package>/security/`.
- [ ] T018 [Plan:1.4] Implement admin account lifecycle rules, equal admin permissions, audit events, last-active-admin protection, and confirmed self-removal in `backend/src/main/java/<package>/admin/`.
- [ ] T019 [Plan:1.4] Configure Supabase Storage access from the backend with separate private and public media policies; document bucket names and environment variables in `.env.example`.
- [ ] T020 [Plan:1.4] Add a Resend adapter in `backend/src/main/java/<package>/integrations/email/` with safe configuration, delivery outcomes, and no secret exposure to the frontend.
- [ ] T021 [Plan:1.5] Add GitHub Actions checks for frontend lint/type-check/build and backend Maven tests/build; require CI on pull requests and configure preview deployment without production secrets.
- [ ] T022 [Plan:1.5] Add safe local setup and environment documentation in `README.md`; ensure `.gitignore` excludes real environment files, build output, and local data. (Backend run/test commands, safe environment variable names, and backend target/local-data ignore rules are documented; frontend setup was already documented. Final clean-checkout setup verification remains pending.)

### Exit gate

- Fresh setup can build both apps and connect only to the intended development
  environment.
- Database schema is managed by migrations; credentials remain server-side.
- Unauthorized backend requests fail closed; admin recovery/account protections
  are tested.
- Pull-request CI runs reliably and does not deploy unreviewed changes to
  production.

## 5. Phase 2 — Public website and editorial content management

**Story:** US1 — Discover the club and its work.

### Goal

Deliver the responsive public shell and the public information pages. Allow
admins to manage approved public content without coupling page components to
database/provider details.

### Work items

- [ ] T023 [US1] [Plan:2.1] Build the shared site layout, navigation, footer, page metadata, responsive behavior, and accessible UI primitives using the approved visual system in `design.md` in `frontend/src/components/` and `frontend/src/app/`. (In progress: shared header and separate Team route exist; header and hero links target implemented Home sections and Team.)
- [ ] T024 [US1] [Plan:2.1] Implement Home as four ordered parts: the existing hero, an upcoming-event section, a contact section showing the confirmed contact details and form (full name, email, subject, message, optional document attachment, submission status/errors), and the footer. The form must ultimately use the shared contact-request workflow from REQ-042/REQ-043 and satisfy REQ-058, not create a separate homepage-only workflow. Do not embed Mission, Projects, Journey, a full Events directory, or Team sections on Home. Use 21hrs.space for scroll continuity and atmosphere, not its exact scene sequence; preserve native scrolling and honor reduced-motion preferences. (In progress: existing hero retained; upcoming-event empty state with a CSS grid vignette, contact details/form, and footer are in the frontend. The optional PDF/JPEG/PNG attachment is limited to 10 MB. `/api/contact` and event-data integration do not exist yet; the form reports failure and offers the email fallback rather than claiming delivery. Contact UI uses the landing-page charcoal/off-white/copper theme and Space Grotesk/IBM Plex Mono. Home navigation targets implemented sections; the task remains incomplete pending data/API integration and focused validation.)
- [ ] T025 [US1] [Plan:2.1] Implement public Projects and Journey pages, typed content rendering, responsive media, and explicit empty/loading/error states in `frontend/src/pages/`. (Projects and Journey pages are implemented with typed static content, local images, responsive newest-first GSAP ScrollTrigger timelines, and reduced-motion support; data-backed states and admin-managed Journey content remain pending. Verify historical facts before production.)
- [ ] T026 [US1] [Plan:2.2] Copy approved logo, project, event, team, and drone assets into `frontend/public/`; preserve the complete GLB and creator attribution. Optimize media and preserve attribution/license metadata. (In progress: selected assets have been copied; owner confirms rights to display the full model including its weapon; runtime uses the complete GLB with its supplied axes.)
- [ ] T027 [US1] [Plan:2.3] Create Project and Journey persistence, migration, DTOs, service, repository, and public/admin REST endpoints in `backend/src/main/java/<package>/projects/` and `journey/`.
- [ ] T028 [US1] [Plan:2.3] Create public-contact-details persistence and public/admin REST endpoints in `backend/src/main/java/<package>/contacts/`; expose only approved public contact fields.
- [ ] T029 [US1] [Plan:2.4] Build authenticated admin forms for project/Journey entries and contact-page details, with server validation and immediate publish-on-save behavior in `frontend/src/pages/admin/` and backend feature modules.
- [ ] T030 [US1] [Plan:2.4] Implement public Team structure using approved profiles only, grouped by study year, with private contact fields excluded from public DTOs in `frontend/src/pages/TeamPage.tsx` and `backend/src/main/java/<package>/profiles/`. (Frontend in progress: the separate Team route renders public names, roles, portraits, and social links from the legacy roster and the additional profiles currently visible on the live public Team page; public portrait assets are copied locally, email/phone fields are excluded, and groups without entries show empty states. Backend-backed approved-profile integration remains pending.)
- [ ] T031 [US1] [Plan:2.4] Add component/API tests for public field filtering, content publishing visibility, empty states, invalid inputs, and unauthorized admin edits under `frontend/` and `backend/src/test/`.

### Exit gate

- All PRD public pages have working routes; pages are usable on phone, tablet,
  and desktop.
- Public content is sourced from owner-approved records and public reads expose
  no private profile, registration, contact, or admin fields.
- Admin content saves become visible only after a successful backend response.
- Public pages have meaningful loading, success, failure, and empty states.

## 6. Phase 3 — Event publishing and administration

**Story:** US2 — Manage events and registrations (event setup portion).

### Goal

Create the event lifecycle and public event catalog before adding paid
registration. Ensure drafts stay private and event limits/deadlines are
consistently represented.

### Work items

- [ ] T032 [US2] [Plan:3.1] Add Event schema/migration and typed domain models for title, description, dates, venue, poster, eligibility, INR fee, registration type, deadline, capacity, team size, publication state, and archive data in `backend/src/main/resources/db/migration/` and `backend/src/main/java/<package>/events/`.
- [ ] T033 [US2] [Plan:3.1] Implement event create/edit/schedule/publish/close operations with server-side admin authorization and draft-by-default behavior in `backend/src/main/java/<package>/events/`.
- [ ] T034 [US2] [Plan:3.1] Build admin event management screens for draft/published/closed state, fee, deadline, eligibility, capacity, registration type, and team-size limits in `frontend/src/pages/admin/EventManagement.tsx`.
- [ ] T035 [US2] [Plan:3.2] Build public event listing/detail pages showing published status, eligibility, fee, deadline, and remaining capacity in `frontend/src/pages/Events.tsx` and `EventDetails.tsx`. (A static `/events` route now shows an honest no-upcoming-events state and four past public events in a GSAP timeline; published-event data, details, and registration integration remain pending.)
- [ ] T036 [US2] [Plan:3.2] Implement automatic upcoming-to-past event classification from scheduled end time and admin-editable archive story/photos/results in `backend/src/main/java/<package>/events/`.
- [ ] T037 [US2] [Plan:3.2] Add event lifecycle tests for draft invisibility, explicit publishing, deadline closure, capacity boundaries, date rollover, archive editing, and malformed event data in `backend/src/test/`.

### Exit gate

- Draft events are never public; publishing is explicit and admin-authorized.
- Public event data is filtered to the required fields and updates after publish.
- Deadlines and capacity are evaluated consistently by the backend, not only
  by a client timer.
- Ended events appear in the past-events view and remain editable for archive
  updates.

## 7. Phase 4 — Event registration, payment, and registration administration

**Story:** US2 — Register and pay for an event; manage registrations.

### Goal

Complete individual/team registration and verified payment. Do not mark a
registration confirmed based on the browser return URL or an unverified event.

### Work items

- [ ] T038 [US2] [Plan:4.1] Add Registration, TeamMember, and PaymentAttempt migrations/models with private participant fields, provider references, explicit states, and idempotency support in `backend/src/main/resources/db/migration/` and `backend/src/main/java/<package>/registrations/`.
- [ ] T039 [US2] [Plan:4.1] Implement server validation for event state, deadline, capacity, individual fields, team/captain fields, team-size bounds, and free-text department/institution in `backend/src/main/java/<package>/registrations/`.
- [ ] T040 [US2] [Plan:4.1] Build individual/team registration forms with retained values on recoverable failures and distinct pending, closed, failed, retry, and confirmed states in `frontend/src/features/registrations/`.
- [ ] T041 [US2] [Plan:4.2] Integrate the selected INR payment provider behind `PaymentGateway.java`; create checkout attempts and a time-limited capacity hold with expiry/release behavior in `backend/src/main/java/<package>/integrations/payment/PaymentGateway.java`.
- [ ] T042 [US2] [Plan:4.2] Implement signed payment webhook verification, idempotent event processing, transactional capacity recheck, and confirmed registration only after verified success in `backend/src/main/java/<package>/integrations/payment/PaymentWebhookController.java`.
- [ ] T043 [US2] [Plan:4.2] Add provider sandbox tests for success, failure, abandonment, duplicate webhook, invalid signature, timeout/retry, expired hold, deadline race, and concurrent capacity limit in `backend/src/test/`.
- [ ] T044 [US2] [Plan:4.3] Send event details and receipt through Resend only after verified payment; persist delivery state and surface email failures for retry in `backend/src/main/java/<package>/integrations/email/`.
- [ ] T045 [US2] [Plan:4.3] Build authorized admin registration review/edit and Excel-compatible export, protecting participant, team, and payment data in `frontend/src/pages/admin/registrations/` and `backend/src/main/java/<package>/registrations/`.
- [ ] T046 [US2] [Plan:4.3] Verify public endpoints never return registration/team/payment data and only authorized admins can view or edit those records in `backend/src/test/`.

### Exit gate

- Only verified successful payment confirms registration; browser redirects alone
  cannot confirm it.
- Failure, abandonment, retry, timeout, duplicate webhook, and capacity race
  paths are verified in the provider sandbox/test environment.
- Expired checkout holds release capacity; concurrent payments cannot overbook.
- Confirmation/receipt email is tied to verified payment and delivery failures
  remain visible.
- Registration data and exports are inaccessible to unauthenticated visitors.

## 8. Phase 5 — Selected-recruit profile submission and moderation

**Story:** US3 — Onboard a selected recruit.

### Goal

Allow only selected recruits to submit a profile using the manually rotated
shared secret link, then publish only approved public profile fields.

### Work items

- [ ] T047 [US3] [Plan:5.1] Implement the shared-link access check, secret storage/rotation configuration, abuse throttling, and security logging in `backend/src/main/java/<package>/profiles/RecruitProfileAccessService.java`.
- [ ] T048 [US3] [Plan:5.1] Create profile-submission persistence and private image upload validation for JPEG/PNG/WebP up to 5 MB in `backend/src/main/java/<package>/profiles/MemberProfileService.java` and `backend/src/main/java/<package>/integrations/storage/SupabaseStorageService.java`.
- [ ] T049 [US3] [Plan:5.1] Build the selected-recruit form for name, image, study year, department, social URLs, and one or more approved domains in `frontend/src/pages/MemberProfile.tsx`.
- [ ] T050 [US3] [Plan:5.2] Build admin profile review, approve, reject-and-delete, and delete operations with confirmation/audit behavior in `frontend/src/pages/admin/MemberProfileReview.tsx` and `backend/src/main/java/<package>/profiles/MemberProfileAdminController.java`.
- [ ] T051 [US3] [Plan:5.2] Publish only approved profile fields to the public Team page, grouped by study year, and ensure email/phone/private submission state is absent in `backend/src/main/java/<package>/profiles/` and `frontend/src/pages/Team.tsx`.
- [ ] T052 [US3] [Plan:5.2] Add tests for invalid file types/sizes, unauthorized access, pending invisibility, approval visibility, rejection deletion, shared-link rotation, and no profile-submission emails in `backend/src/test/` and `frontend/`.

### Exit gate

- The form is not an open recruitment application and creates no account.
- Pending/rejected submissions are not public; approval exposes only approved
  fields; rejection deletes the request.
- Shared-link rotation invalidates the old link and has an owner-documented
  operational procedure.
- File type and size are validated server-side; this workflow sends no
  automatic email.

## 9. Phase 6 — Contact requests, attachments, and replies

**Story:** US4 — Manage contact requests.

### Goal

Store contact inquiries safely, notify the club, and support admin replies with
delivery history and recovery.

### Work items

- [ ] T053 [US4] [Plan:6.1] Add ContactRequest, private attachment metadata, and ContactReply persistence/migrations in `backend/src/main/resources/db/migration/` and `backend/src/main/java/<package>/contacts/`.
- [ ] T054 [US4] [Plan:6.1] Implement public contact submission validation and private PDF/JPEG/PNG attachment handling up to 10 MB in `backend/src/main/java/<package>/contacts/ContactRequestService.java` and `backend/src/main/java/<package>/integrations/storage/SupabaseStorageService.java`.
- [ ] T055 [US4] [Plan:6.1] Build the public contact form with validation, upload progress, submission result, and recovery states in `frontend/src/pages/Contact.tsx`.
- [ ] T056 [US4] [Plan:6.2] Notify the club through Resend after storing the request and persist notification delivery status without losing the request on failure in `backend/src/main/java/<package>/integrations/email/`.
- [ ] T057 [US4] [Plan:6.2] Build the admin contact inbox, authorized attachment access, replies, retained request history, and retryable delivery failures in `frontend/src/pages/admin/ContactInbox.tsx` and `backend/src/main/java/<package>/contacts/ContactAdminController.java`.
- [ ] T058 [US4] [Plan:6.2] Test attachment validation/privacy, notification failure recovery, reply persistence/delivery status, and unauthorized access in `backend/src/test/`.

### Exit gate

- Contact requests persist before notification; mail failure does not lose the
  inquiry.
- Attachments are private, size/type checked server-side, and available only
  to authorized admins.
- Admin replies are retained in history; failed delivery is visible and
  retryable.

## 10. Phase 7 — Integrated verification and launch

### Goal

Verify every required journey and failure mode, review real content, and prepare
production/rollback without weakening quality gates to meet the date.

### Work items

- [ ] T059 [Plan:7.1] Add integration tests for database transactions, admin authorization, storage access, and provider adapters using the selected test infrastructure under `backend/src/test/`.
- [ ] T060 [Plan:7.1] Add browser end-to-end coverage for public discovery, event registration/payment, admin event/registration management, profile approval, and contact reply in `tests/e2e/`.
- [ ] T061 [Plan:7.1] Run security verification for unauthenticated private data access, admin role enforcement, shared-link abuse, secret exposure, upload validation, payment webhook verification, and log redaction; record results in `docs/security-launch-review.md`.
- [ ] T062 [Plan:7.2] Verify keyboard-only operation, focus visibility, semantic labels, contrast, reduced motion, error feedback, and layouts at agreed mobile/tablet/desktop viewports; record results in `docs/accessibility-launch-review.md`.
- [ ] T063 [Plan:7.2] Review page metadata, image optimization, broken links, empty states, loading behavior, and no-analytics/no-nonessential-cookie behavior across supported browsers in `tests/e2e/`.
- [ ] T064 [Plan:7.3] Verify all migrated public text, names, dates, achievements, links, and images with the owner; remove unapproved or outdated content from `frontend/public/` and content records.
- [ ] T065 [Plan:7.3] Configure staging and production environment variables, Supabase database/storage policies, Resend sender, payment-provider production credentials, TLS/domain, backups, logs, and rollback steps in deployment configuration and `README.md`.
- [ ] T066 [Plan:7.3] Deploy a preview, run the complete critical-flow acceptance checklist, obtain owner approval, and launch only after all blocking gates pass.

### Exit gate

- Every PRD requirement has implementation and verification evidence.
- Full CI passes on the exact release candidate; no failed tests are hidden or
  skipped to create a green result.
- Production secrets are configured outside source control; production access
  and data policies are verified.
- The owner approves the preview, content, and release checklist before public
  launch. A rollback path is available.

## 11. Testing and verification strategy

Critical user journeys:

1. A visitor finds About, Team, a project, an upcoming event, and contact
   information without signing in.
2. An individual registers and pays successfully; only verified payment
   confirms the registration and sends the receipt.
3. A team captain registers a valid team, retries after a failed/abandoned
   payment, and sees accurate capacity/closure states.
4. An admin creates/publishes an event, edits a registration, and exports its
   records.
5. A selected recruit submits a profile; it stays private until approval and
   does not send automated email.
6. A visitor submits a contact request with an attachment; an admin replies
   and can see delivery status/history.

Validation layers:

- **Backend:** JUnit and Spring test support; use the selected real PostgreSQL
  test strategy for transaction/constraint behavior. Select Testcontainers or
  an equivalent during Phase 0; do not rely on an in-memory substitute for
  database-specific concurrency guarantees without documenting the coverage gap.
- **Frontend:** ESLint, TypeScript, unit/component tests for form states and
  public-data filtering.
- **End-to-end:** Browser automation for critical user journeys, including
  negative payment and authorization paths.
- **CI:** Pull-request checks must run lint, type-check, tests, and production
  builds for both applications. Preview deployments must not use production
  secrets/data.

For each workflow, verify success, invalid input, unauthorized access, provider
failure, retry/recovery, and empty-state behavior as relevant. Record test
commands and evidence in the pull request; do not claim a journey is verified
from mocks alone when provider/database behavior is required.

## 12. Requirement mapping

| REQ ID | Requirement summary | Planned phase/item | Completion evidence |
|---|---|---|---|
| REQ-001 | Public pages | P2.1 | Public route/page tests |
| REQ-002 | Home hero, upcoming event, contact, footer; no extra content sections | P2.1, P3.2 | Home order/section browser test |
| REQ-003 | About mission, vision, history | P2.1 | About content review |
| REQ-004 | Approved Team profiles grouped by year | P5.2 | Team API/UI tests |
| REQ-005 | Approved public profile fields and social links | P5.2 | Profile DTO and browser tests |
| REQ-006 | No member email/phone on public Team | P5.2, P7.1 | Public response security test |
| REQ-007 | Project fields/media | P2.3 | Project schema/API/UI tests |
| REQ-008 | Dated Journey entries/stories/photos | P2.3 | Journey schema/API/UI tests |
| REQ-009 | Immediate public content updates | P2.4 | Publish/save integration test |
| REQ-010 | Reuse only reviewed content/media | P0.2, P7.3 | Approved-content inventory |
| REQ-011 | English at launch | P2.1 | Content and navigation review |
| REQ-012 | Responsive public pages | P2.1, P7.2 | Viewport test evidence |
| REQ-013 | Admin event lifecycle management | P3.1 | Event admin acceptance test |
| REQ-014 | New events remain drafts | P3.1 | Draft-not-public test |
| REQ-015 | Event details and registration settings | P3.1 | Event model/API validation |
| REQ-016 | Per-event INR fee | P3.1 | Admin/API event tests |
| REQ-017 | Individual or team registration option | P3.1, P4.1 | Event form tests |
| REQ-018 | Team and captain/member fields | P4.1 | Team registration validation tests |
| REQ-019 | Individual participant fields | P4.1 | Individual registration tests |
| REQ-020 | Year options and free-text department/institution | P4.1 | Form/API validation tests |
| REQ-021 | Configured team-size bounds | P3.1, P4.1 | Boundary validation tests |
| REQ-022 | Deadline/capacity counting policy | P3.1, P4.2 | Capacity transaction tests |
| REQ-023 | Automatic closure at deadline/capacity | P3.2, P4.2 | Deadline/capacity tests |
| REQ-024 | Public event eligibility/fee/deadline/remaining places | P3.2 | Public event browser test |
| REQ-025 | No confirmation before verified payment | P4.2 | Signed webhook/payment integration tests |
| REQ-026 | Failed/abandoned payment retry, no false confirmation | P4.2 | Payment failure/retry tests |
| REQ-027 | Success email with event/team details and receipt | P4.3 | Resend sandbox delivery test |
| REQ-028 | Private event/team/payment records | P4.3, P7.1 | Authorization/API response tests |
| REQ-029 | Admin registration edits and Excel-compatible export | P4.3 | Admin acceptance/export test |
| REQ-030 | No in-site refund/cancellation processing | P0.1, P4.2 | Scope review and UI/API check |
| REQ-031 | Ended events archive automatically and remain editable | P3.2 | Event date/archive integration test |
| REQ-032 | Selected-only profile link; not open recruitment | P0.3, P5.1 | Shared-link access tests |
| REQ-033 | Profile submission needs no account | P5.1 | Anonymous valid submission test |
| REQ-034 | Required profile fields/domains | P5.1 | Form/schema tests |
| REQ-035 | Profile private and pending until approved | P5.1, P5.2 | Pending profile access test |
| REQ-036 | Approved profile appears by study year | P5.2 | Team publication test |
| REQ-037 | Admin approval/rejection; rejection deletes | P5.2 | Moderation tests |
| REQ-038 | No automatic profile-submission emails | P5.2 | Email adapter invocation test |
| REQ-039 | Authenticated admin portal | P1.4 | Unauthorized route/API tests |
| REQ-040 | Admin public-content/profile/contact details management | P2.4, P5.2, P6.2 | Admin feature acceptance tests |
| REQ-041 | Admin contact submissions and attachments | P6.2 | Private inbox/attachment tests |
| REQ-042 | Contact fields and optional document attachment | P6.1 | Contact form/API validation |
| REQ-043 | Persist inquiry and notify club | P6.2 | Persistence plus Resend sandbox test |
| REQ-044 | Admin reply sent and retained | P6.2 | Reply history/delivery test |
| REQ-045 | Required official contact details | P2.3 | Owner content review |
| REQ-046 | Admin updates public contact information | P2.4 | Authorized update/public read test |
| REQ-047 | Equal admin permissions; admins add/remove accounts | P1.4 | Admin authorization/account tests |
| REQ-048 | Unique account identifier and secure sign-in | P1.4 | Email identity/reset/auth tests |
| REQ-049 | Profile photo file types and 5 MB limit | P5.1 | Upload type/size boundary tests |
| REQ-050 | Contact attachment types and 10 MB limit | P6.1 | Upload type/size boundary tests |
| REQ-051 | Server-side upload type/size validation | P5.1, P6.1 | Backend upload rejection tests |
| REQ-052 | Event confirmation and receipt after verified payment | P4.3 | Payment-to-email integration test |
| REQ-053 | Club notification on contact submissions | P6.2 | Resend sandbox test |
| REQ-054 | Email provider/sender selected before production | P0.1, P1.5 | Verified Resend sender/config evidence |
| REQ-055 | Private submissions/contact/registration data | P1.4, P4.3, P5.2, P6.2 | Unauthenticated-access security tests |
| REQ-056 | Backend authorization, not UI-only controls | P1.4, P7.1 | Endpoint authorization tests |
| REQ-057 | Selected payment provider; no raw card data | P0.1, P4.2 | Provider integration and data review |
| REQ-058 | Clear form validation/status/recovery | P2.1, P4.1, P5.1, P6.1 | Form error/success/failure tests |
| REQ-059 | Keyboard, focus, labels, contrast, reduced motion | P7.2 | Accessibility review evidence |
| REQ-060 | Usable on common viewport sizes | P2.1, P7.2 | Responsive browser test matrix |
| REQ-061 | Distinct payment and registration states | P4.1, P4.2 | UI state-transition tests |
| REQ-062 | No production secrets in client or repository | P1.5, P7.1, P7.3 | Secret scan/configuration review |
| REQ-063 | Interactive 360-degree drone with accessible controls and fallback | P2.1, P7.2 | Hero interaction, keyboard, reduced-motion, and WebGL-fallback tests |

## 13. Phase completion and change control

- Complete the exit gate before treating a phase as finished.
- Keep each pull request focused on a phase item and link it to relevant
  requirement IDs.
- If an unresolved choice changes data shape, security, cost, or launch scope,
  stop the affected work and ask the owner before proceeding.
- Do not reduce required payment, authorization, privacy, or failure-path
  verification to preserve the target date. Raise schedule or scope changes
  for explicit approval.
- Update this plan, the PRD, and architecture when an approved decision changes
  requirements or technical boundaries.
