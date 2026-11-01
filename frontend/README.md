# Soaring Eagles Frontend

The frontend is a Vite, React, and TypeScript application. It contains the
current homepage prototype; the Spring Boot backend and data-backed public
routes are planned but not implemented.

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
2. Upcoming-event section with an empty state and decorative canvas backdrop.
3. Contact details and form.
4. Existing footer.

The Home does not include Mission, Projects, Journey, Events, or Team sections.
Those are intended as separate routes in the full site. The navigation still
needs alignment with those routes; several existing links point to removed
Home anchors.

The contact form validates fields and reports submission status, but the
`/api/contact` endpoint is not implemented. Submissions are not delivered; the
form reports failure and provides an email fallback. The upcoming-event
section is not connected to event data.

## Project documentation

- [Product requirements](../prd.md)
- [Architecture](../architecture.md)
- [Design](../design.md)
- [Development phases and task status](../phase.md)
