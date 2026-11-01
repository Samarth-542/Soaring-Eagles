# Product Requirements Document — Soaring Eagles Website

**Status:** Draft for review  
**Product owner:** Soaring Eagles  
**Target launch:** 15 December 2026  
**Last updated:** 31 October 2026  
**Repository:** `https://github.com/Samarth-542/Soaring-Eagles.git`

> This PRD records requirements gathered in the conversation and uses the existing Soaring Eagles project as a content and workflow reference. It does not authorize implementation choices or a push. Items marked **Open decision** need confirmation before the affected work begins.

## 1. Product overview

The Soaring Eagles website is the club’s public home for introducing the club, showcasing its work and current members, sharing its history and events, and enabling event registration. A private admin portal lets club administrators manage public content, event registrations and payments, member submissions, and contact requests.

The first release is intended to be a complete live website by **15 December 2026**. The owner has said all requirements in this PRD are critical for that launch.

## 2. Goals

1. Help visitors find trustworthy information about Soaring Eagles, its members, projects, history, and events.
2. Let individuals and teams register for events and complete payment without admin assistance.
3. Ensure event registrations are confirmed only after successful payment.
4. Let admins manage website content, event logistics, registrations, payments, contact requests, and member-profile approvals through the portal.
5. Protect private registration, payment, and contact information from public access.

## 3. Users and permissions

| User | Needs and access |
|---|---|
| Public visitor | Browse public pages, events, approved club-member profiles, and contact information. |
| Individual event registrant | Review eligibility and event details, submit personal details, pay the event fee, and receive confirmation. |
| Team event captain | Submit team and member details, pay the event fee, and receive the registration confirmation and receipt by email. |
| Selected recruit | Use a form shared with them to submit a club-member profile. No account or login is required. |
| Administrator | Sign in to manage events, event registrations and payments, public content, contact requests, and member-profile submissions. |
| Head Admin / club president | Uses a unique account identifier and secure sign-in. The owner confirmed that all admins may add or remove admin accounts. |

### Privacy boundary

- Event-registration team/member details, payment records, contact submissions, and private member contact information are admin-only.
- Public club-member profiles are distinct from event-registration teams. Only approved club-member profiles appear on the public Team page.
- Public club-member profile fields: full name, profile image, study year, department, domain(s), and LinkedIn/X/Instagram links. Email and phone are not public.

## 4. Scope

### 4.1 In scope for the first live release

Public pages:

- Home
- About
- Team
- Projects
- Events
- Journey
- Event Registration
- Recruitment / selected-recruit profile submission
- Contact

Admin capabilities:

- Event creation, editing, scheduling, publishing, and archive management.
- Event registrations, successful payment records, detail edits, and Excel export.
- Public content updates for projects, Journey, Team, and contact information.
- Contact-request review and email replies.
- Selected-recruit profile review, approval, rejection, and deletion.
- Admin-account management: any admin may add or remove admin accounts.

### 4.2 Out of scope or explicitly excluded

- Accounts or login for event registrants, recruits, or public members.
- A public, open-to-everyone club recruitment application: profile submission is shared only with selected recruits.
- Processing cancellations or refunds inside the website. These are handled outside the portal.
- Storing or processing card details directly in the website.
- Any changes to the existing reference repository as part of this PRD.

## 5. User scenarios and acceptance

### Story 1 — Discover the club and its work (Priority: P1)

As a visitor, I want to understand the club and find current activities so I can decide whether to attend an event, explore its work, or contact the club.

**Independent test:** Browse the public site without an account and locate the club overview, an event, the member roster, a project, and contact information.

**Acceptance scenarios:**

1. **Given** a visitor opens Home, **when** the page loads, **then** it introduces the club, highlights the latest event, and links to the Journey timeline.
2. **Given** a visitor opens About, **when** the page is read, **then** the club mission, vision, and history are available.
3. **Given** a visitor opens Team, **when** the roster is displayed, **then** only approved club-member profiles appear, grouped by study year.
4. **Given** a visitor opens Projects or Journey, **when** content exists, **then** project details or dated milestones, stories, and photos are shown.

### Story 2 — Register and pay for an event (Priority: P1)

As an individual or team captain, I want to register for an eligible event and pay online so that my registration is confirmed and I receive the details.

**Independent test:** Complete a test registration for both an individual event and a team event, then verify the payment outcome and confirmation.

**Acceptance scenarios:**

1. **Given** an event is published and open, **when** an eligible participant opens it, **then** the event details, fee, deadline, and remaining capacity are visible.
2. **Given** a team event, **when** the captain submits the form, **then** the team name, captain details, and required details for every team member are collected.
3. **Given** an individual event, **when** a participant submits the form, **then** their name, phone, email, year, department, and institution are collected.
4. **Given** payment succeeds before the event limit is reached, **when** the payment result is verified, **then** the registration is confirmed and the appropriate confirmation email and receipt are sent.
5. **Given** payment fails or is abandoned, **when** the participant retries, **then** they can retry with the submitted details and no confirmed registration exists until payment succeeds.
6. **Given** an event reaches its configured registration limit or deadline, **when** another person tries to register, **then** registration is closed.

### Story 3 — Manage events and registrations (Priority: P1)

As an admin, I want to schedule events, control capacity, review paid registrations, and export or correct registration data so that event operations stay accurate.

**Independent test:** Create a draft event, publish it, register a test participant, edit the registration, and export the event registrations to Excel.

**Acceptance scenarios:**

1. **Given** an admin creates an event, **when** it is saved, **then** it remains a draft until explicitly published.
2. **Given** an admin configures an event, **when** a fee, deadline, eligibility, or registration limit is set, **then** the public event page reflects the published values.
3. **Given** an admin reviews registrations, **when** they edit a registration or export the list, **then** the saved details are updated or an Excel file is produced.
4. **Given** an event has ended, **when** its end date passes, **then** it moves automatically to the past-events area and admins can update its archive details.

### Story 4 — Onboard a selected recruit (Priority: P1)

As a selected recruit, I want to submit my profile details through a shared form so the club can review and publish my profile without requiring me to create an account.

**Independent test:** Submit a profile using the shared form and verify that it remains private until an admin approves it.

**Acceptance scenarios:**

1. **Given** a selected recruit receives the submission link, **when** they submit valid profile details, **then** the submission is stored for admin review and no user account is created.
2. **Given** an admin approves a submission, **when** the approval is saved, **then** the member appears on the public Team page with approved public fields.
3. **Given** an admin rejects a submission, **when** the rejection is confirmed, **then** the request is deleted and is not shown on the Team page.
4. **Given** a recruit submits a profile, **when** the submission is received or decided, **then** the system sends no automatic email notification.

### Story 5 — Manage content and contact requests (Priority: P1)

As an admin, I want to update the public site and respond to contact messages so the website remains current and inquiries are handled.

**Independent test:** Update a project and contact detail, submit a contact message with an attachment, and send a reply from the admin portal.

**Acceptance scenarios:**

1. **Given** an admin saves a change to public content, **when** the save succeeds, **then** the change is published immediately.
2. **Given** a visitor submits a contact form, **when** it is accepted, **then** the message is stored for admin review and a notification is emailed to the club.
3. **Given** an admin replies to a contact request, **when** the reply is sent, **then** the sender receives an email and the reply is retained in the request history.

## 6. Functional requirements

### Public site and content

- **REQ-001:** The site MUST provide the public pages listed in Section 4.1.
- **REQ-002:** Home MUST introduce Soaring Eagles, highlight the latest event, and link to the Journey timeline.
- **REQ-003:** About MUST present the club’s mission, vision, and history.
- **REQ-004:** The public Team page MUST show approved club-member profiles only and group them by study year.
- **REQ-005:** An approved club-member profile MUST be able to display full name, photo, year, department, selected domains, and LinkedIn, X, and Instagram links.
- **REQ-006:** The public Team page MUST NOT expose member email addresses or phone numbers.
- **REQ-007:** The Projects page MUST support project name, summary, photos/video, year, status, technologies, and description.
- **REQ-008:** The Journey page MUST support dated milestones/events, written stories, and photos.
- **REQ-009:** Public content changes to projects, Journey, Team, and contact information MUST be visible immediately after an admin saves them.
- **REQ-010:** The current club logo and other relevant content/media from the existing Soaring Eagles project MAY be reused after club review. Existing factual content MUST be verified before launch.
- **REQ-011:** The site MUST support English at launch.
- **REQ-012:** Public pages MUST be usable on mobile, tablet, and desktop displays.

### Event publishing and registration

- **REQ-013:** Admins MUST be able to create, edit, schedule, publish, and close events.
- **REQ-014:** A newly created event MUST remain a draft until an admin explicitly publishes it.
- **REQ-015:** An event MUST support title, description, date/time, venue, poster, eligibility, fee, deadline, and registration capacity.
- **REQ-016:** The fee MUST be configurable separately for each event in INR.
- **REQ-017:** Admins MUST be able to choose team or individual registration for each event.
- **REQ-018:** Team registration MUST collect team name; captain name, phone, and email; and each team member’s name, year, department, institution, and email.
- **REQ-019:** Individual registration MUST collect name, phone, email, year, department, and institution.
- **REQ-020:** Study year options MUST include 1st, 2nd, 3rd, and 4th year. Department and institution MUST be free-text fields.
- **REQ-021:** Team registration MUST support admin-configured minimum and maximum member counts per team.
- **REQ-022:** An event MUST support an admin-configured registration deadline and maximum count. Team-event capacity MUST count registered teams; individual-event capacity MUST count individual registrants.
- **REQ-023:** The system MUST close registration automatically when the deadline passes or configured capacity is reached.
- **REQ-024:** The event page MUST show the event’s eligibility, fee, deadline, and remaining teams or individual places as applicable.
- **REQ-025:** A registration MUST NOT be confirmed until a successful payment is verified.
- **REQ-026:** After a failed or abandoned payment, a registrant MUST be able to retry with the submitted details; the attempt MUST NOT be presented as a confirmed registration.
- **REQ-027:** After successful payment, the system MUST email the captain’s address for a team registration, or the registrant’s address for an individual registration. The email MUST include confirmation, event details, the team/member summary where applicable, and a receipt.
- **REQ-028:** Event-registration team details, participant details, and payment records MUST be visible only to authorized admins, not in public pages.
- **REQ-029:** Admins MUST be able to view and edit event registration details and export registrations to an Excel-compatible file.
- **REQ-030:** The website MUST NOT process cancellations or refunds; those requests are handled outside the website.
- **REQ-031:** When an event ends, it MUST automatically move into the past-events/“last year glimpses” area. Admins MUST be able to update its archived story, photos, and results.

### Selected-recruit profile onboarding

- **REQ-032:** The recruit profile form MUST be shared only with selected recruits and MUST NOT function as an open public recruitment application.
- **REQ-033:** Submitting a recruit profile MUST NOT create an account or require a login.
- **REQ-034:** The profile form MUST collect full name, profile image, study year, department, LinkedIn URL, X URL, Instagram URL, and one or more domains from Aerodynamics, Avionics, and Automation.
- **REQ-035:** Profile submissions MUST remain private and pending until an admin approves them.
- **REQ-036:** An approved profile MUST appear on the public Team page and be grouped by study year.
- **REQ-037:** Admins MUST be able to approve or reject a profile submission. Rejecting MUST delete the request.
- **REQ-038:** The system MUST NOT send automatic emails when a member profile is submitted, approved, or rejected.

### Admin and contact workflows

- **REQ-039:** The admin portal MUST require an authenticated admin sign-in.
- **REQ-040:** Admins MUST be able to manage public projects, Journey entries, club-member profiles, and contact-page details.
- **REQ-041:** Admins MUST be able to view contact submissions and their attachments.
- **REQ-042:** Contact submission MUST collect full name, email, subject, message, and an optional document attachment.
- **REQ-043:** A contact submission MUST be stored for admin review and trigger an email notification to the club.
- **REQ-044:** Admins MUST be able to reply to a contact submission from the portal. The reply MUST be emailed to the sender and retained in that request’s history.
- **REQ-045:** The Contact page MUST display the official email `tseofficial2011@gmail.com`, LinkedIn `linkedin.com/in/soaring-eagles-a7a19831b`, X `x.com/TSE_015`, and the location “Soaring Eagles Aeromodelling Lab, Near Mechanical Block, Siddaganga Institute of Technology, Tumkur, 572103.”
- **REQ-046:** Authorized admins MUST be able to update the public contact information.
- **REQ-047:** All admins MUST share the same website-management permissions. The latest owner instruction says any admin may add or remove admin accounts.
- **REQ-048:** Admin sign-in MUST use a unique account identifier and a secure authentication flow. The precise authentication method remains open.

### Uploads and notifications

- **REQ-049:** Profile-photo uploads SHOULD accept JPEG, PNG, or WebP up to 5 MB.
- **REQ-050:** Contact attachments SHOULD accept PDF, JPEG, or PNG up to 10 MB.
- **REQ-051:** Upload file type and size MUST be validated before acceptance. The preceding limits are proposed conservative defaults requested by the owner as a “standard choice.”
- **REQ-052:** The website MUST send event registration confirmation and receipt emails after verified successful payment.
- **REQ-053:** The club email MUST receive a notification for contact-form submissions.
- **REQ-054:** Email delivery provider and sender configuration MUST be selected before production email workflows are released.

## 7. Non-functional requirements

- **REQ-055:** Private event-registration, contact, and member-submission data MUST not be accessible to unauthenticated visitors.
- **REQ-056:** Admin authorization MUST be enforced for admin actions; hiding controls in the user interface alone is insufficient.
- **REQ-057:** Payment must be handled through a selected payment provider. The website MUST NOT store raw card credentials.
- **REQ-058:** Public and admin forms MUST provide clear validation errors, submission status, and recovery guidance without falsely reporting success.
- **REQ-059:** The interface MUST support keyboard navigation, visible focus, meaningful labels, readable contrast, and reduced-motion preferences. Target WCAG 2.2 AA as a proposed quality baseline.
- **REQ-060:** The website MUST provide a usable experience at common phone, tablet, and desktop viewport sizes.
- **REQ-061:** The product MUST distinguish payment success, payment failure, payment retry, registration confirmation, and registration closure in user-visible status messages.
- **REQ-062:** Production secrets MUST not be exposed in browser-delivered code or committed to the repository.
- **REQ-063:** The Home hero MUST provide an interactive 360-degree view of the approved drone model, operable with pointer/touch and keyboard controls, with a static/reduced-motion fallback.
- **REQ-064:** The Home hero MUST provide a user-controlled toggle between the drone model and a real public camera feed when the source is connected. It MUST show an honest unavailable state when no feed is connected and MUST NOT simulate a live stream. Telemetry MUST NOT be displayed until its public fields are approved.

## 8. Data entities

- **Event:** title, description, date/time, venue, poster, eligibility, event type, fee, currency, registration deadline, capacity, team-size limits where applicable, publication state, and archive details.
- **Event registration:** event, registration type, participant or team details, payment outcome/reference, registration state, and creation time.
- **Team and team member:** team name, captain details, and each participant’s name, email, year, department, and institution. These event-registration records are private.
- **Club-member profile submission:** full name, profile image, year, department, domain interests, social URLs, review state, and submission time.
- **Project:** name, summary, description, photos/video, year, status, and technologies.
- **Journey entry:** date, milestone/event, story, and photos.
- **Contact request:** sender name/email, subject, message, attachment, status, admin replies, and timestamps.
- **Admin account:** unique account identifier, authentication credentials managed securely, and account state.

## 9. Edge cases and failure behavior

- A payment provider returns failure, times out, or the visitor closes the payment flow: do not confirm registration; allow retry with the same submitted details.
- A capacity limit is reached during a payment attempt: do not overbook; show that registration is closed if the limit has been reached before confirmation.
- An event deadline passes while its registration page is open: prevent a new confirmed registration and explain that registration has closed.
- A recruit submission is rejected: delete the request and do not publish a profile or send an automatic email.
- An uploaded file has an unsupported type or exceeds the limit: reject it with a clear message and preserve other form values where possible.
- A contact-email notification or reply fails: preserve the contact request/reply state and tell the admin that delivery did not succeed.
- No approved club-member profiles or no upcoming events exist: show an intentional empty state rather than a broken page.

## 10. Success criteria

- **SC-001:** In a usability check, at least 90% of first-time test visitors can find the club’s event details and contact information within 60 seconds.
- **SC-002:** A test team can complete a successful event registration without admin assistance, including receiving its confirmation and receipt email.
- **SC-003:** 100% of verified unsuccessful or abandoned payment attempts remain unconfirmed; only verified successful payments create confirmed registrations.
- **SC-004:** Event registration automatically closes when its configured deadline or capacity is reached.
- **SC-005:** An admin can create and publish an event, edit a registration, and export the registration list without developer assistance.
- **SC-006:** An admin can update public content and contact details, review a contact request, and send an email reply from the portal.
- **SC-007:** An admin can approve a selected-recruit profile and verify it appears publicly under the correct study year; a rejected profile is deleted and never appears.
- **SC-008:** The complete approved first-release scope is available on the production domain by 15 December 2026, subject to confirmation of hosting and domain access.
- **SC-009:** The core public pages, forms, and admin portal remain usable at common phone, tablet, and desktop viewport sizes.

## 11. Assumptions and proposed defaults

- Event dates and times use India Standard Time unless a later requirement specifies otherwise.
- Approved public member profiles display the fields listed in Section 3; private email and phone remain admin-only.
- “Receipt” means a receipt or payment confirmation suitable for the registrant, supplied by or tied to the selected payment provider. Tax invoice requirements are not yet specified.
- The maximum event capacity counts confirmed paid registrations: teams for team events and individuals for individual events.
- Profile-photo and contact-attachment formats and size limits in REQ-049–051 are proposed defaults, not user-specified limits.
- Content fields and event values are entered and maintained by admins. Content edits publish immediately; events require an explicit publish action.
- Past events move automatically based on the event’s scheduled end date.
- Existing club-approved text and images may be used as starting content, with facts reviewed before launch.
- No member-submission notification emails are sent. Event confirmation and contact emails are still required.

## 12. Open decisions and risks

| Topic | Current state | Needed before |
|---|---|---|
| Payment provider | Undecided; compare providers for INR payments. | Payment integration and production testing |
| Website stack | Vite/React/TypeScript frontend; Java 21, Spring Boot 3.x, Maven modular-monolith backend; PostgreSQL hosted by Supabase. | Application scaffolding |
| Visual direction | Owner selected 21hrs.space as the primary visual reference: cinematic dark aerospace styling, oversized display type, fine technical framing, and a scroll-led story. The home hero uses the complete approved GLB, including its weapon mesh, with an interactive 360-degree view. Preserve creator attribution and the supplied model axes. | Design review and frontend prototype |
| Live drone feed | Not part of the current website interface. Consider only as a separately scoped feature after confirming the source/service, access, and privacy requirements; telemetry fields require approval. | Future scope confirmation and integration |
| Hosting and domain | Hosting undecided; prefer the existing domain if available. | Release planning and production deployment |
| Transactional email provider | Resend selected; sender/domain verification still needed. | Event confirmations and contact notifications/replies |
| Admin authentication method | Spring Security username/password; email is the unique identifier, verified-email reset, password-only at launch. Prevent deletion of last active admin; require confirmation for self-removal. | Admin implementation |
| Admin account management | Confirmed: all admins can add/remove admin accounts. This increases the impact of a compromised admin account and must be considered in authorization and audit design. | Admin authorization design |
| Selected-recruit form access | One shared secret link, manually rotated; storage and abuse controls must be designed. | Recruitment/profile form design |
| Contact-message and registration data retention | No retention period or archival policy specified. | Production data policy |
| Payment retries and capacity races | Capacity will be held temporarily during checkout; hold duration/expiry mechanics remain to be set with the payment provider. Prevent over-capacity confirmations. | Payment integration |
| Payment receipts and tax | Receipt required; tax invoice and refund policy are not specified. Refunds are outside the portal. | Payment-provider selection |
| Official additional social channels | Only the supplied email, LinkedIn, X, and location are confirmed. | Final content review |
| Accessibility target | Follow accessibility best practices; no formal conformance claim selected. | Design and release sign-off |
| Load/performance targets | No traffic or concurrency estimate was supplied. | Production capacity planning |

## 13. Content and implementation notes

- The current reference site/repository is a content and workflow reference; the new application is a separate project.
- The owner selected a Vite/React/TypeScript frontend; Java 21/Spring Boot 3.x/Maven modular-monolith backend; PostgreSQL hosted by Supabase; Supabase Storage; and Resend for transactional email. Hosting/domain and INR payment provider remain open. Reference README files describe the old site's dependencies; do not treat them as new-site requirements.
- Reuse only club-approved information and media. Do not copy private member, registration, or payment data.
- The new project’s master build guide is [MASTER_PROJECT_GUIDE.md](./MASTER_PROJECT_GUIDE.md).

## 14. Readiness summary

The product scope, primary user journeys, and launch-critical capabilities are documented. This PRD is ready to guide planning, but implementation should wait for decisions that affect payment, authentication, deployment, and selected-recruit form access. All functional requirements use unique, sequential `REQ-XXX` identifiers and are paired with user scenarios or measurable outcomes.
