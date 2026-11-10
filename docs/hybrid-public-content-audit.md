# Hybrid public-content architecture audit

Audit captured 10 November 2026 from the current source tree. No source was
changed during the audit.

## Architecture and public read routes

| Public GET route | Source service / response | Current frontend use |
|---|---|---|
| `GET /api/v1/events` | `EventService.listPublic()` / `PublicEventView[]` | Home event summary and Events archive |
| `GET /api/v1/events/{id}` | `EventService.getPublic()` / `PublicEventView` | Event details and currently the registration form configuration |
| `GET /api/v1/content/team` | `ContentService.listGroups()` + `listTeam(true)` / `TeamDirectory` | Team page |
| `GET /api/v1/content/projects` | `ContentService.listProjects(true)` / `Project[]` | Projects page |
| `GET /api/v1/content/journey` | `ContentService.listJourney(true)` / `JourneyMilestone[]` | Journey page |
| `GET /api/v1/content/site-details` | `SiteDetailsService.get()` / `SiteDetails` | Home contact section |

These are public GETs in `SecurityConfiguration`; existing controllers and
routes remain in place for compatibility and operational use.

## Mutations and protected operations

Published content is changed through:

- `/api/v1/admin/content/team` create, update and delete
- `/api/v1/admin/content/projects` create, update and delete
- `/api/v1/admin/content/journey` create, update and delete
- `PUT /api/v1/admin/site-details`
- `/api/v1/admin/events` create, update, schedule, publish, close and delete

The security filter requires `ROLE_ADMIN` for these operations. The
`/api/v1/admin/**` matcher is protected by the final `anyRequest().hasRole("ADMIN")`
rule; session authentication and CSRF remain configured.

Registration submission, status/retry, payment/webhook, contact submission,
admin authentication/session, admin registration, and contact inbox routes
are operational or private endpoints and are not snapshot sources.

## Existing Supabase integration

`ContentImageStorageService` uploads public images to Supabase Storage using
`SUPABASE_URL` and `SUPABASE_SERVICE_ROLE_KEY` on the backend. The current
`se_data` bucket is for image assets, and `se_private` is used for contact
attachments. `SUPABASE_STORAGE_PUBLIC_BUCKET` and
`SUPABASE_STORAGE_PRIVATE_BUCKET` are bound from `application.yml`. A separate
public-content bucket/property is required; no Supabase client or service-role
credential is present in the frontend.

## Frontend client and cache

There is no TanStack/React Query dependency. Public data currently uses the
project's `usePublicData` hook with in-memory/session storage caching,
in-flight deduplication, stale rendering and mutation invalidation. Public
requests use `fetchPublicJson`; admin and operational requests use separate
credentialed API clients. Vite proxies `/api` to the local Spring server in
development.

The migration can preserve the existing hook/cache and switch only public
fetchers to a configured, anonymous Supabase Storage base URL. Admin and
operational calls continue using the Spring API base URL.

## Event fields and registration safety

`PublicEventView` currently contains static presentation fields and mutable or
operational fields in one DTO. `remainingCapacity`,
`registrationWindowOpen`, `onlineRegistrationAvailable`, `ended`, and current
publication status can change independently of content. Registration form
behavior also consumes fee/payment configuration and team-size rules from this
response. `RegistrationReservationService.reserve` locks and reloads the event,
checks its current publication/deadline state, validates fields and capacity,
and creates payment/registration records in PostgreSQL.

Event snapshots therefore contain only public event presentation content and
published visibility (id, title, description, dates, venue, poster and archive
story/results/photos). The event-details page obtains current registration
configuration only after a visitor explicitly requests registration; the
backend remains authoritative when the registration is submitted. The
snapshot will not include capacity usage, payment configuration or
registration outcomes.

## Migration boundary

Only the six public read consumers move to anonymous object reads from Storage.
The existing Spring endpoints, all admin/session/CSRF flows, registration,
payment/webhook, contact, and email paths remain unchanged. The Vercel build
receives only a public object base URL and never a service-role key. The
manifest is the publication pointer; versioned objects remain immutable so a
failed publish leaves the last committed manifest usable.
