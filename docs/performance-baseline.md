# Performance baseline

Captured 10 November 2026 before the performance changes. This is a source and
build baseline, not a Lighthouse or live-hosting benchmark; no deployed browser
or Render instance was available for network timing.

## Frontend

- Production build: `npm run build` passed; `npm run lint` passed.
- Generated assets: entry JavaScript 419.10 kB (132.91 kB gzip), drone
  experience 974.89 kB (259.67 kB gzip), CSS 94.63 kB (15.84 kB gzip).
- The drone experience is dynamically imported, but its component mounts
  immediately on Home, so the large chunk is requested during initial render.
  Other public and admin route modules are statically imported by `App.tsx`.
- Home starts two independent API requests in parallel: the public event list
  and site details. They do not block rendering the hero, but both start
  loading states on every mount. Other public routes request their own content
  when mounted. No duplicate public request or backend keep-alive polling was
  found in the initial route flow.
- Public request helpers set `cache: "no-store"`; the pages have no shared
  client cache, background refresh, or retry button. Revisiting a route within
  the SPA therefore shows its loading state while the API request completes.
- The source image inventory includes a 2.99 MB 2560x2560 logo, a 6.83 MB
  3072x2275 event image, a 2.83 MB 5712x4284 journey image, and multiple
  multi-megabyte team portraits.
- The font CSS imports all available subsets for Space Grotesk and IBM Plex
  Mono, although the site content is Latin-script.

## Backend

- Full Maven test suite: 41 tests passed.
- Public event listing loads all events, filters unpublished events in Java,
  and issues a separate registration-capacity count for each listed event.
  Accessing each event's lazy archive-photo collection can also result in
  repeated selects.
- Project listing fetches project rows and then runs a separate tags query for
  each project.
- Team, project, and journey ordering/filter indexes already exist in Flyway
  migrations. Registration capacity has a composite event/state/hold-expiry
  index. No missing high-value content indexes were identified from these
  query paths.
- No explicit Hikari pool limits or Hibernate collection batch-fetch size are
  configured. The backend has two intentional scheduled workers: registration
  hold expiry and email outbox dispatch; no keep-alive worker is present.
- Public content endpoints already set scoped HTTP cache policies: team,
  projects, journey and site details use `public, max-age=300,
  stale-while-revalidate=300`; events use `public, max-age=20,
  stale-while-revalidate=60`. The frontend previously used `no-store`, so it
  did not benefit from browser HTTP caching.

## Initial Home request baseline

| Request | Count | Starts |
|---|---:|---|
| `GET /api/v1/events` | 1 | On Home mount |
| `GET /api/v1/content/site-details` | 1 | On Home mount, independently of events |

These are separate, concurrent requests and both serve distinct Home content;
there is no safe request reduction without changing the API contract. The
optimizations target repeat visits, cache reuse, response-query counts, and
initial JavaScript/image transfer instead.

## Post-optimization verification

- Frontend lint and production build pass. The initial JavaScript chunk is
  271.05 kB (93.31 kB gzip), down from 419.10 kB (132.91 kB gzip), a 35.3%
  raw and 29.8% compressed reduction. Route-specific JavaScript and CSS are
  split into separate chunks. The initial CSS is 25.93 kB (6.09 kB gzip),
  down from 94.63 kB (15.84 kB gzip).
- The drone chunk remains about 975 kB (259.69 kB gzip), but loading is deferred
  after initial render. Vite still reports its large-chunk warning.
- All 72 bundled PNG/JPEG originals have WebP variants. Their combined size is
  8,006,984 bytes, compared with 66,039,053 bytes for the originals (87.9%
  smaller). A local browser check confirmed WebP selection, including image
  filenames containing spaces, and lazy image rendering.
- Public content now has in-memory/session client caching, stale-content
  rendering during refresh, request deduplication, and mutation invalidation.
  The first uncached Home visit still requires its two independent requests.
  In a local browser navigation check, revisiting Home while its public data
  remained fresh added zero API requests.
- Source-based query estimate for event lists fell from one event query plus
  per-event capacity and archive-photo queries to one join-fetch event query
  and one grouped capacity query. Project listing fell from one project query
  plus one tags query per project to one project query plus one grouped tags
  query. These are code-derived query counts, not production database
  measurements.
- Backend configuration now limits Hikari to four connections with no
  minimum idle connections and sets Hibernate batch-fetch size to 16. The full
  backend suite passes: 41 tests, no failures.
- No live Render/Supabase latency or query metrics were available. Cold starts
  can still delay fresh data; cached public content remains visible, and the
  UI provides section-level loading, error and retry states. No keep-alive
  traffic or additional scheduled jobs were introduced.
