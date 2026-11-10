# Soaring Eagles Frontend

The frontend is a Vite, React, and TypeScript application using React Router.
Public Team, Projects, Journey, Events, and site details are read from
versioned JSON snapshots in Supabase Storage. The authenticated admin portal
and operational actions continue to use Spring Boot for event and registration
management, Team/Projects/Journey content editing, image uploads, and admin
account management. Individual/team registration and verified Razorpay payment
handling are implemented; paid checkout remains disabled until sandbox and
webhook delivery are verified.

## Run locally

From this directory:

```powershell
npm install
npm run dev
npm run lint
npm run build
npm run preview
```

Vite prints the local development URL after startup.

## Current Home

Home is ordered as:

1. Existing interactive drone hero.
2. Upcoming-event section backed by published events, with loading, error, and
   empty states, over the CSS grid-vignette backdrop.
3. Contact details and form.
4. Existing footer.

The Home does not include Mission, Projects, Journey, or a full Events
directory; those are separate routes. Navigation links to the Home sections,
Events archive, Projects page, and Team page.

The contact form validates fields and reports submission status, but the
`/api/contact` endpoint is not implemented. Submissions are not delivered; the
form reports failure and provides an email fallback.

## Team route

The `/team` route displays published names, roles, portraits, and social links
from public-content snapshots, grouped by study year. Email and phone fields
are excluded from public data. Admins can manage Team entries and upload
portraits through the authenticated portal. Selected-recruit submissions and
their review/approval workflow are not implemented.

## Projects route

The `/projects` route reads published projects and tags from public snapshots.
Entries are arranged newest-first in a responsive vertical timeline. GSAP
ScrollTrigger draws the timeline and reveals cards as they enter view;
reduced-motion preferences disable those animations. Admins can create and
edit project entries and upload images. Recheck historical competition
placements and other project claims with the club before production launch.

## Journey route

The `/our-journey` route reads published milestones from public snapshots and
displays them in a responsive GSAP timeline. Admins can create and edit
milestones and upload images. Competition placements and historical details
need club verification before production launch.

## Events route

The `/events` route reads published event presentation fields from public
snapshots, links to public event details, and retains a newest-first GSAP-animated timeline of historical
events migrated from the previous public site. The historical archive remains
local static content; review its descriptions and images with the club before
launch.

The authenticated Events module lets admins create and edit events, schedule
publication, publish immediately, and close events. New events remain drafts.
Event times use India Standard Time (UTC+05:30); admins upload event posters
and archive photos as image files. Event details load live registration
configuration from the backend when requested; submission and capacity
validation remain backend-owned. The
registrations workspace supports event selection, private participant/team
review and edits, paginated lists, CSV export, payment status, and retrying
confirmation-email delivery.

Individual and team registrations reserve one event-capacity slot for up to
10 minutes while checkout is pending. Razorpay orders are created by the
backend, and only a valid signed webhook can confirm payment. Free events can
be registered without Razorpay. Paid checkout is disabled by default and must
remain disabled until the Razorpay test flow and public webhook are verified.
Confirmation emails are queued through Resend after payment confirmation; a
verified sender and API key are needed for actual delivery.

## Admin portal design preview

Visit `/admin-preview` to review the responsive admin-console layout. This is
a design preview only: it uses no live or sample records and must not be used
to enter real information. The public navigation's ADMIN SIGN IN link opens
`/admin-preview/login`, which authenticates through the Spring Boot API. A
successful sign-in opens `/admin`; that page verifies the server session and
displays the dashboard layout with the signed-in account and sign-out action.
Events, Registrations, Team, Projects, Journey, and Admin accounts are
connected to protected APIs. Selected-recruit profiles, Contact inbox, public
site details, and analytics are not implemented. The design-only
`/admin-preview` route is public and is not an admin area.

The Admin accounts module supports listing and creating accounts, and
activating/deactivating them. Permanent account deletion and password reset
are not implemented. New account passwords must be at least 12 characters.

Local development proxies `/api` to `http://localhost:8080`. Set
`VITE_API_BASE_URL` to the HTTPS API origin when deploying the frontend and
backend separately, and set `VITE_PUBLIC_CONTENT_BASE_URL` to the public
Supabase Storage object URL for the `public-content` bucket. Both values are
browser-visible configuration, not secrets. Never configure the Supabase
service-role key in Vercel. See
[`../docs/hybrid-public-content-operations.md`](../docs/hybrid-public-content-operations.md)
for bucket setup, snapshot publishing, and rollback instructions. The
backend must separately allow the exact frontend origin and configure a
server-only initial-admin email and BCrypt password hash; see
[`../README.md`](../README.md) and [`../backend/.env.example`](../backend/.env.example).

## UI component setup

This frontend uses TypeScript and GSAP, but does not currently use Tailwind CSS
or shadcn/ui. Existing components use the project CSS; reusable UI components
belong in `src/components/ui/`, and route styles live alongside their pages.
The event timeline is implemented with this structure and does not need
shadcn's Badge or Tailwind utility classes.

If future UI work requires shadcn/ui, run `npx shadcn@latest init` from this
directory and follow its Vite/TypeScript setup prompts for Tailwind and the
`@/` import alias. TypeScript is already installed and configured, so it
should not be reinstalled. Review any generated CSS reset before enabling it
alongside the existing site styles.

## Project documentation

- [Product requirements](../prd.md)
- [Architecture](../architecture.md)
- [Design](../design.md)
- [Development phases and task status](../phase.md)
