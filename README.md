# Soaring Eagles — Aerial Robotics Club

The official website for **Soaring Eagles**, the student aerodesign and aerial
robotics club at Siddaganga Institute of Technology (SIT), Tumakuru, India.

The site brings the club's events, projects, journey, and team together, with
event registration and a protected portal for club administrators.

> **Project status:** The site is under active development. Paid event checkout
> is disabled until payment and webhook testing is complete. Email delivery and
> production deployment configuration still need verification.

## Explore

- **Home** — Interactive 3D aircraft-inspired hero, upcoming events, and club
  contact information.
- **Events** — Discover published events, view event details, and register.
- **Projects** — Browse the club's project work.
- **Journey** — Explore milestones in the club's history.
- **Team** — Meet the published team members.
- **Admin portal** — Authenticated tools for managing events, registrations,
  contact messages, team and project content, and admin accounts.

## Built with

- **Frontend:** React, TypeScript, Vite, React Router, Three.js, and GSAP.
- **Backend:** Java 21, Spring Boot, and PostgreSQL.
- **Media:** Supabase Storage for managed site images and private contact
  attachments.

The interactive drone model is an owner-approved third-party concept, not a
Soaring Eagles aircraft design. It is credited to
[Diamonddogkz on Sketchfab](https://sketchfab.com/Diamonddogkz).

## Run locally

### Website

Requirements: Node.js and npm.

```powershell
cd frontend
npm install
npm run dev
```

Vite prints the local website URL when it starts. To check the frontend:

```powershell
npm run lint
npm run build
```

### Backend

Requirements: Java 21 or newer. From `backend`, run the test suite with:

```powershell
.\mvnw.cmd test
```

The API requires a locally configured admin password hash to start. See
[`backend/.env.local.example`](./backend/.env.local.example) for the local
configuration template. The frontend proxies `/api` to `http://localhost:8080`
during development.

## Development and project documentation

- [Frontend guide](./frontend/README.md) — Routes, current behavior, and UI
  notes.
- [Product requirements](./prd.md) — Scope and acceptance criteria.
- [Architecture](./architecture.md) — Application structure and security
  boundaries.
- [Design](./design.md) — Visual direction, accessibility, and asset use.
- [Development phases](./phase.md) — Progress, open work, and release checks.
- [Asset usage and attribution](./assets/README.md).

## Deployment notes

This repository is not a deployment guide or a claim that the site is
production-ready. Before a public launch, configure the production database,
HTTPS, exact frontend-origin allowlist, and storage policies. Keep server
credentials and the Supabase service-role key out of the frontend and source
control. Verify Razorpay sandbox/webhook behavior before enabling paid checkout,
and verify the Resend sender before relying on email delivery.

The older website in the parent directory is a reference project; this
standalone repository contains the new website.
