# Hybrid public-content snapshots

The source audit is in [hybrid-public-content-audit.md](./hybrid-public-content-audit.md).
PostgreSQL remains authoritative. Spring Boot publishes read-only public
content to Supabase Storage; the frontend reads those files without contacting
Render. Admin, registration, payment, webhook, and contact operations remain
on the existing Spring API.

## Snapshot layout and schema

The public bucket contains a mutable pointer and immutable version directories:

```text
public-content/
  manifest.json
  snapshots/<version>/
    manifest.json
    events.json
    team.json
    projects.json
    journey.json
    site-details.json
```

Each content file has this envelope:

```json
{
  "version": "20261110T120000.000Z-<uuid>",
  "generatedAt": "2026-11-10T12:00:00Z",
  "data": []
}
```

The root manifest points to the immutable files and includes their SHA-256
hashes:

```json
{
  "version": "20261110T120000.000Z-<uuid>",
  "generatedAt": "2026-11-10T12:00:00Z",
  "snapshots": {
    "events.json": {
      "path": "snapshots/<version>/events.json",
      "sha256": "<hex digest>"
    }
  }
}
```

The real manifest has entries for all five content files. Event content is
limited to public presentation fields and visibility status. It deliberately
does not contain capacity, registration eligibility, payment settings,
registration status, or user information.

## Supabase Storage setup

1. Create a dedicated bucket named `public-content` and enable anonymous
   **read** access. Do not grant anonymous uploads or updates.
2. Keep the bucket service-role-only for writes. The backend uses the existing
   server-side service-role key; never put that key in Vercel or frontend code.
3. Keep the existing image buckets (`se_data` and `se_private`) separate.
4. The frontend base URL should be the public object URL, without a trailing
   slash:
   `https://<project-ref>.supabase.co/storage/v1/object/public/public-content`

## Environment variables

Configure these on Render (backend):

| Variable | Value |
|---|---|
| `SUPABASE_URL` | Supabase project URL |
| `SUPABASE_SERVICE_ROLE_KEY` | Supabase service-role key, stored only as a Render secret |
| `SUPABASE_STORAGE_PUBLIC_CONTENT_BUCKET` | `public-content` |

Existing database and image-storage variables remain required as before.
`backend/.env.example` lists the local-development names.
`APP_SUPABASE_PUBLIC_CONTENT_PUBLISHING_ENABLED` is optional and defaults to
`true`; set it to `false` only to pause both automatic and manual snapshot
publishing while keeping other backend operations available.

Configure these on Vercel (frontend):

| Variable | Value |
|---|---|
| `VITE_PUBLIC_CONTENT_BASE_URL` | Public object URL shown above |
| `VITE_API_BASE_URL` | Existing Spring API origin, still needed for protected and operational actions |

`frontend/.env.example` lists the corresponding frontend names. The Supabase
service-role key is not a Vercel variable.

## Publishing and consistency

- Sign in to the existing admin portal and use **Publish public snapshots** on
  a content-management page to seed the first snapshot or retry a failed
  publish.
- The protected endpoint is `POST /api/v1/admin/public-content/publish`; it
  requires the existing admin session and CSRF token.
- Successful public-content mutations trigger best-effort publication after
  their database transaction commits. Upload failures do not undo the database
  change; the previous root manifest stays active and the admin retry action
  can be used after storage is available.
- Draft-only content edits and deletes do not publish snapshots; publishing is
  triggered when public content is added, updated, unpublished, or removed.
- Scheduled event publication uses a one-shot backend task, restored from
  PostgreSQL when the application starts. It does not poll or keep Render
  awake. At the scheduled time it publishes the now-public event snapshot.
- Versioned files and their matching versioned manifest are uploaded and
  verified before the root `manifest.json` is updated last. A failed stage
  therefore leaves the previous root manifest usable.
- Immutable versioned objects use long-lived cache headers. The root manifest
  uses a 20-second cache lifetime (with a 30-second stale-while-revalidate
  window) so event changes become visible promptly.

## Rollback

Previous versioned content and manifests are retained in the bucket. To roll
back, restore the selected version's `snapshots/<version>/manifest.json`
contents to the root `manifest.json` using the Supabase dashboard or an
authorized server-side Storage operation. Keep the restored manifest's paths
and hashes unchanged. Do not expose service-role credentials or remove the
current version while clients may still have it cached.

The original Spring Boot public GET endpoints remain available as a rollback
option. To return the frontend to them, deploy a frontend revision that uses
the original API fetchers; the snapshot deployment does not alter or remove
those endpoints.

## Validation boundary

Automated builds and tests can validate the publisher's manifest ordering,
authorization, and frontend behavior. A live Storage upload, anonymous bucket
read, Render-unavailable production browse, Vercel secret inspection, and
production registration/payment flows require deployment credentials and
must be verified in the target environments. No production changes are made
by this repository migration.
