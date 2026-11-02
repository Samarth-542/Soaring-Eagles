# Soaring Eagles Frontend

The frontend is a Vite, React, and TypeScript application using React Router.
It contains the current homepage prototype, Team page, and Projects page. The
Spring Boot feature APIs and data-backed content management are not implemented
yet.

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
2. Upcoming-event section with an empty state and CSS grid-vignette backdrop.
3. Contact details and form.
4. Existing footer.

The Home does not include Mission, Projects, Journey, or a full Events
directory; those are separate routes. Navigation links to the Home sections,
Events archive, Projects page, and Team page.

The contact form validates fields and reports submission status, but the
`/api/contact` endpoint is not implemented. Submissions are not delivered; the
form reports failure and provides an email fallback. The upcoming-event
section is not connected to event data.

## Team route

The `/team` route displays public names, roles, portraits, and social links
migrated from the previous site's team roster and the additional profiles
currently shown in its public directory. Year labels follow the previous
site's current-year grouping. Email and phone fields are excluded; downloaded
portraits are served locally from `public/images/team/`. The roster is static
for this frontend prototype; connecting approved profile data to the future
backend is still pending.

## Projects route

The `/projects` route lists the nine projects currently shown on the previous
public Projects page, using its public descriptions, tags, and project photos.
The entries are arranged newest-first in a responsive vertical timeline. GSAP
ScrollTrigger draws the timeline and reveals cards as
they enter view; reduced-motion preferences disable those animations. Photos
are served locally from `public/images/projects/`; project data is maintained
in `src/data/projects.ts`. The page is static and is not connected to the
future admin content API. Recheck historical competition placements and other
project claims with the club before production launch.

## Journey route

The `/our-journey` route presents 11 newest-first milestones from the previous
public site in a responsive GSAP timeline. Milestone copy is static in
`src/data/journey.ts`, with locally served photos in
`public/images/journey/`. Competition placements and historical details need
club verification before production; admin-managed Journey content is not
connected.

## Events route

The `/events` route shows an empty state when no upcoming events have been
announced, followed by a newest-first GSAP-animated timeline of four past
events listed on the previous public site. Event descriptions and images are
static local data in `src/data/events.ts` and `public/images/events/`; event
publishing, registration, and admin management are not connected.

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
