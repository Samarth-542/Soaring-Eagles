# Soaring Eagles Website — Design Direction

**Status:** Updated to current owner decisions and homepage prototype
**Scope:** Site-wide visual language, interactive home hero, event and contact sections
**Requirements:** [prd.md](./prd.md)  
**Architecture:** [architecture.md](./architecture.md)  
**Reference asset notes:** [assets/README.md](./assets/README.md)

> This is a design brief, not an implementation or a claim that the reference
> has been reproduced. Use the reference for high-level visual and interaction
> direction. Do not copy its source, text, imagery, or proprietary font files.
> The named fonts below were observed in the reference site's HTML; their
> licensing must be verified before use. The selected drone model is an
> owner-approved third-party asset; the owner confirms the rights to display
> the complete GLB, including its weapon mesh. Preserve creator attribution and
> use the supplied model without changing its geometry or axes.

## 1. Design goal

Build a distinctive Soaring Eagles website inspired by the cinematic,
instrument-panel visual language of [21 Hours on the Moon](https://www.21hrs.space/).
The hero should replace the reference's lunar subject with a realistic,
interactive 3D drone. Carry the visual language—dark immersive canvas,
oversized typography, fine framing lines, compact technical labels, and
scroll-led storytelling—through the rest of the site without copying the
reference page or turning every content page into a full-screen animation.

The site remains a practical club website: visitors must be able to understand
the team and reach projects, events, recruitment, registration, and contact
without needing to operate the 3D object.

## 2. Reference inspection notes

Inspected the live home page and its "Scroll to land" transition to the
`/overview/` experience on 1 November 2026. These are visual/runtime
observations, not a complete audit of every route or state.

### Observed visual language

- Near-black full-viewport scene with a subtle starfield and restrained warm
  off-white text.
- Very large, bold, uppercase subject typography behind a large curved lunar
  surface; the foreground object overlaps and masks parts of the title.
- Thin horizontal rules, fine corner brackets/frame lines, and compact
  mission/year metadata give the composition an instrument-display quality.
- A centered prompt invites the visitor to scroll into the experience.
- The overview transitions into a long, immersive timeline with a `0HR`–`21HR`
  scale, event markers, image galleries, panoramas, audio, and labeled
  milestones.
- Small circular icon controls appear in the interface. Their full purpose and
  every interaction state were not verified in this inspection.

### Observed implementation clues

- The delivered page includes SvelteKit runtime markers, canvas elements, and
  preloaded content. This describes the reference implementation only; the
  Soaring Eagles app's planned stack remains Vite/React/TypeScript with a
  Spring Boot backend.
- The reference HTML declares `AkkuratMonoLL` and `FuturaStd` font families.
  Their exact use by every element was not verified. These are identification
  clues, not permission to reuse font files.
- The hero's underlying media, full interaction logic, and mobile/reduced-motion
  behavior have not been comprehensively inspected. Treat the observations
  above as direction, not a pixel-perfect specification.

## 3. Soaring Eagles visual system

### Mood and composition

- Dark, cinematic, aerospace-inspired; make the team's engineering feel
  tangible rather than like a generic SaaS product.
- Use near-black/deep-charcoal backgrounds, warm white text, muted gray
  secondary labels, and a restrained warm accent for framing/active states.
- Use the landing page's near-black/charcoal surfaces, warm off-white text,
  muted gray labels, and restrained copper accent throughout Home. The contact
  cards, icons, links, focus states, and submit button follow this same palette;
  do not introduce a separate cyan theme in that section.
- Use subtle grain, stars, technical grids, or telemetry-style details only
  when they support the aircraft story. Avoid visual noise and excessive neon.
- Use real, approved club photography and project imagery in supporting
  sections to balance the cinematic hero.

### Typography

- Aim for the reference's contrast between a heavy, geometric display face and
  a compact technical/monospaced label face.
- The reference declares `FuturaStd` and `AkkuratMonoLL`. Verify commercial
  licensing and embedding rights before using either. Do not download or bundle
  the reference site's font files without authorization.
- If rights are unavailable, select licensed alternatives with a similar
  character: a bold geometric display face and a readable monospaced face.
- The current frontend uses Space Grotesk for display/body text and IBM Plex
  Mono for compact technical labels. Continue using these installed,
  project-wide font variables (`--font-display`, `--font-body`, and
  `--font-mono`) rather than introducing a different contact-section font.
- Use extremely large uppercase display type selectively (hero/section
  headings); keep body copy comfortably sized and readable.
- Avoid tracking, small caps, or low contrast that harms legibility.

### Framing and interface details

- A thin viewport frame with interrupted corner brackets can frame the hero;
  simplify or remove it on small screens where it competes with content.
- Use fine rules to separate metadata, captions, and timeline/event information.
- Keep icon controls purposeful and clearly labeled for assistive technology.
- Use restrained hover/focus feedback; avoid making core controls discoverable
  only by visual hover.

## 4. Hero concept: drone in place of the moon

### Composition

- Full-viewport, dark atmospheric background with the drone as the dominant
  subject and oversized Soaring Eagles/flight-related display typography
  layered behind it.
- The drone should feel like a physical aircraft: realistic proportions,
  materials, shadows, surface detail, and lighting. Keep it well lit enough to
  read against the dark background.
- Use the approved model at `assets/drone/assault_drone_concept.glb` for the
  first hero implementation. The owner approved this Sketchfab model for use.
  The owner confirms rights to display the complete model, including its
  weapon mesh. Preserve creator attribution and do not describe the model as a
  club-designed aircraft. Render the supplied GLB without changing its axes.
- Place a concise club statement and primary action in a legible safe area.
  Keep the drone from obscuring key words, controls, or calls to action.
- Frame the composition with subtle technical marks and small metadata, such
  as aircraft name/category or project status, only when approved facts exist.
- Provide one clear scroll cue to enter the site story; do not make scrolling
  mandatory to find the primary navigation.

### Cinematic loading and reveal

- Keep the initial viewport black with a restrained starfield and a small
  circular loader while the GLB loads.
- Once the model is ready (and the initial 1.2-second opening has elapsed),
  reveal it rising from below over 1.2 seconds. Reveal the oversized heading
  behind the foreground model at 2.4 seconds, then supporting copy and labels.
- Bring in the remaining controls, navigation, and scroll cue from 3.6 seconds
  and settle the model by about 5 seconds. Use a coordinated GSAP timeline and
  Three.js object transforms without changing the source model's axes.
- Do not add a fixed wait after assets are ready. If reduced motion is enabled,
  skip the staged movement and show the final hero immediately.

### 360 interaction requirements

- Provide a real-time rendered 3D model that visitors can rotate through a full
  360-degree horizontal view using pointer drag and touch drag. A hover-only
  interaction is insufficient because it excludes touch and keyboard users.
- Desktop hover may provide subtle parallax/highlight feedback or reveal
  nonessential component labels. Do not make hover move the aircraft
  unpredictably or obstruct controls.
- Provide keyboard operation (for example, focusable rotate-left/right
  controls) and visible focus states. The canvas must not be the only way to
  discover aircraft information.
- If an idle rotation is used, keep it slow and subtle, pause it during direct
  interaction, and provide a pause/reduced-motion alternative.
- Keep all text, navigation, and calls to action as semantic HTML layered
  independently of the 3D canvas.
- The current hero presents only the interactive real-time 3D model; the
  live-feed toggle and unavailable-feed panel have been removed.
- Treat any future live camera and telemetry feature as a separately scoped
  addition after its source, access/privacy requirements, and approved data
  fields are defined. Never imply that an unconnected stream is live.
- Treat camera and telemetry as separate data sources and privacy/safety
  concerns; do not expose device identifiers, location, or other sensitive
  operational data without explicit approval.
- Treat live camera and telemetry as separate data sources and privacy/safety
  concerns; do not expose device identifiers, location, or other sensitive
  operational data without explicit approval.

### Scroll continuation

Use the reference to inform the *behavior and feel* of scrolling—continuity,
deliberate pacing, visual transitions, and depth—not as a blueprint for its
scene sequence or content. The Home page has exactly this content order:

1. **Hero:** introduce Soaring Eagles and its aircraft.
2. **Upcoming event:** highlight the next published event and provide a clear
   path to its details/registration; show a useful empty state when none exists.
3. **Contact:** include the public contact form so visitors can message the
   club without leaving Home. Reuse the site's contact-submission workflow;
   show accessible validation, submission, and error states.
4. **Footer.**

Do not add Mission, Projects, Journey, Events, or Team sections to Home. These
remain dedicated pages reachable through the site's navigation. Keep ordinary
browser scrolling; transitions should support the four-part story rather than
turning the page into a copy of the reference. Any scroll-linked movement must
be reversible, responsive, and disabled or simplified for reduced-motion
preferences. Keep content readable and useful when animation is off.

## 5. Motion and interaction

- Preserve normal browser scrolling. Do not trap visitors in a custom
  scroll-only experience.
- Tie scene transitions to clear story sections, with eased, restrained
  movement; avoid continuously changing the scene on every scroll pixel.
- Use scroll progress/section markers only where they improve orientation.
  They must not replace the site's global navigation.
- The 360 model should respond directly to drag/touch with predictable
  sensitivity and have clear limits or seamless wraparound.
- Avoid fast spinning, flashing, camera shake, and unnecessary parallax.
- Ensure that scroll, hover, drag, and controls do not interfere with each
  other. Keep clickable targets usable around the canvas.
- Honor `prefers-reduced-motion`: disable idle rotation, parallax, and
  scroll-linked object travel; use a stable, attractive poster/model view.
- Provide a static fallback when WebGL is unavailable or initialization fails.

## 6. Responsive behavior

Responsive ranges below are starting points; validate them against actual
layouts and supported device/browser requirements.

| Device class | Starting viewport range | Design adaptation |
|---|---:|---|
| Small mobile | 320–479 px | Stack hero copy and aircraft deliberately; reduce title size; use a cropped/still model poster by default or a lightweight touch-enabled model; remove fixed side controls/frame details that crowd the viewport. |
| Large mobile | 480–767 px | Keep a single-column hero; expose drag affordance and accessible rotate controls; compact the top navigation and metadata. |
| Tablet | 768–1023 px | Use balanced two-column or layered composition only when the aircraft and title remain clear; increase touch targets and retain generous text space. |
| Desktop | 1024–1439 px | Large layered display title, detailed drone render, thin frame/corner marks, and optional scroll/timeline orientation controls. |
| Wide desktop | 1440 px and above | Constrain copy and labels; make the drone larger only while preserving composition and model detail. |

- Reposition and rescale the model per viewport class; do not simply scale down
  the desktop layout.
- The model must work with touch, keyboard, and pointer input. Do not assume a
  hover-capable device.
- Keep navigation and the primary CTA visible/accessible at all sizes.
- Avoid horizontal overflow, overlap, and controls under browser/device safe
  areas. Test short landscape viewports as well as portrait phones.
- On constrained mobile devices, offer a static or reduced-detail version
  rather than requiring full-quality realtime rendering.

## 7. Accessibility, fallback, and performance

- Respect `prefers-reduced-motion`; offer a stable view and do not force
  animated transitions.
- Keep the aircraft's name, description, project link, and controls available
  as semantic HTML. Provide a concise text description of the model.
- Make rotation controls keyboard accessible with visible focus. Use
  descriptive names and state announcements where needed.
- Maintain readable contrast for text, frame lines, controls, metadata, and
  focus indicators. Do not rely on color alone for active states.
- Do not autoplay audio. Do not require sound, hover, or scroll gestures to
  understand the page.
- Treat 3D as progressive enhancement: render useful text and navigation
  without waiting for the model. Reserve scene dimensions to avoid layout shift.
- Lazy-load the 3D runtime/model after the initial page content is usable.
- Optimize geometry, textures, and render resolution; pause rendering when the
  tab is hidden or the model is offscreen.
- Prototype on mid/low-range mobile hardware before committing to a detailed
  model. Provide a reduced-detail/static fallback when performance is poor.
- Select a rendering library only after a short implementation spike.
  The current prototype uses Three.js with React Three Fiber and Drei;
  performance across the supported device matrix still needs validation.

## 8. Intellectual property and asset requirements

- Use the 21hrs.space page as a visual reference, not a source to clone.
- Do not reuse its source code, moon imagery, text, logos, site-specific
  animations, or bundled font files without explicit rights.
- Verify licenses for the named typefaces, any 3D model, textures, icons,
  sound, and photographs before production.
- Prefer club-owned CAD where available. For the selected third-party model,
  preserve attribution and do not label it as Soaring Eagles' own aircraft.
- The owner selected the [Sketchfab Assault Drone Concept](https://sketchfab.com/3d-models/assault-drone-concept-c3a6f9b644b54de4b02c4599aac632eb)
  as the model to use, with the local file at
  `assets/drone/assault_drone_concept.glb`. The listing describes a drone with
  a built-in machine gun and as an enemy asset for a game; it shows "Free
  Standard" and the creator requests credit. The owner states it is free to
  use, confirms rights to display the complete model including its weapon, and
  approved its use for this site. Preserve attribution and do not present it
  as a Soaring Eagles original design.
- The existing asset guidance in [assets/README.md](./assets/README.md)
  remains applicable to the club's own media.

## 9. Homepage continuation — current owner-approved scope

- The Home page order is **hero → upcoming event → contact information and
  contact form → footer**.
- Do not add Mission, Projects, Journey, Events, or Team content sections to
  Home. Those belong on their own public pages when those routes are built.
- The 21hrs.space reference informs the continuity and pacing of scrolling;
  it does not prescribe the same scenes or exact transitions. Keep native
  scrolling and use reversible, restrained motion. The existing page currently
  flows through normal document scrolling; a custom scroll-linked cinematic
  transition has not been implemented below the hero.
- The upcoming-event section currently shows an honest “no upcoming events”
  state. It is not yet connected to published event data.
- The event section has a decorative, static canvas with subtle flight paths,
  orbit contours, points, and atmospheric glow. It is background-only,
  responsive, and must not interfere with content or pointer input.
- The contact area presents the confirmed email, LinkedIn, X, and Tumakuru
  location beside the form. Match the Home charcoal/off-white/copper palette
  and Space Grotesk/IBM Plex Mono typography.
- The form collects full name, email, subject, message, and an optional PDF,
  JPEG, or PNG attachment up to 10 MB. Client-side validation and honest error
  messaging are implemented; `/api/contact` has no backend yet, so online
  submissions are not connected and the email fallback must remain visible.
- Do not imply that a camera feed or telemetry feature is part of the current
  approved homepage scope.

## 10. Implementation prompt

> Create a responsive Soaring Eagles website using the approved requirements
> and club assets. Use 21hrs.space as high-level visual inspiration: a dark,
> cinematic aerospace atmosphere; oversized bold display type; restrained
> monospaced technical labels; fine framing rules/corner marks; and a scroll-led
> story. Do not copy its source, wording, imagery, or unlicensed fonts. Build a
> hero centered on the approved complete drone GLB instead of a moon. Preserve
> the creator's attribution and original model axes.
> Let users rotate the model through 360 degrees with pointer and touch drag;
> provide keyboard-accessible rotate controls, meaningful hover feedback only
> on hover-capable devices, and a reduced-motion/static fallback. The owner
> should not be made into a live-feed toggle. Keep all content and actions in
> semantic HTML and the 3D model informative, never a navigation barrier. Below
> the hero, show only the upcoming-event section, the contact-information card
> and contact form, then the footer. The event section may use a subtle,
> decorative canvas backdrop. Use the reference for scroll continuity and
> atmosphere, not its exact flow. Adapt the composition for phones, tablets,
> and desktops; use the existing Space Grotesk/IBM Plex Mono fonts and
> charcoal/off-white/copper theme. Use only approved public club facts and
> licensed media.

## 11. Approval checklist and unresolved choices

- Preserve the complete model and creator attribution as confirmed by the
  owner; do not hide the weapon mesh or alter the GLB axes.
- Confirm whether idle auto-rotation is desired in addition to user-controlled
  360 rotation.
- The Home sequence and the no-extra-sections scope are confirmed. Confirm
  approved event content when an event is ready to publish.
- The prototype's font and palette are established; complete licensing/content
  review and mobile quality checks before launch.
- Select mobile quality targets and verify the existing 3D renderer on the
  supported device matrix.
- Define supported browser/device matrix and review reduced-motion/WebGL
  fallback behavior.
