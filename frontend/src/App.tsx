import { lazy, Suspense, useCallback, useEffect, useRef, useState } from 'react'
import type { RefObject } from 'react'
import type { Group } from 'three'
import { BrowserRouter, Link, Route, Routes, useLocation } from 'react-router-dom'
import './App.css'
import AdminLoginPreview from './pages/AdminLoginPreview'
import AdminPortalPreview from './pages/AdminPortalPreview'
import AdminSessionPage from './pages/AdminSessionPage'
import GridVignetteBackground from './components/GridVignetteBackground'
import HeroIntroOverlay from './components/HeroIntroOverlay'
import HomeContactForm from './components/HomeContactForm'
import SiteHeader from './components/SiteHeader'
import EventsPage from './pages/EventsPage'
import EventDetailsPage from './pages/EventDetailsPage'
import OurJourneyPage from './pages/OurJourneyPage'
import ProjectsPage from './pages/ProjectsPage'
import TeamPage from './pages/TeamPage'
import { useHeroReveal } from './components/HeroReveal'
import { fetchPublicEvents } from './lib/eventsApi'
import type { PublicEventRecord } from './lib/eventsApi'

const DroneExperience = lazy(() => import('./DroneExperience'))

function DroneExperienceLoader({
  onModelReady,
  pointerTarget,
}: {
  onModelReady: (model?: Group, riseDistance?: number) => void
  pointerTarget: RefObject<HTMLElement | null>
}) {
  return (
    <Suspense fallback={<div className="scene-loading hero-scene-placeholder" role="status">PREPARING AIRCRAFT</div>}>
      <DroneExperience onModelReady={onModelReady} pointerTarget={pointerTarget} />
    </Suspense>
  )
}

function HomePage() {
  const headerRef = useRef<HTMLElement>(null)
  const heroRef = useRef<HTMLElement>(null)
  const [nextEvent, setNextEvent] = useState<PublicEventRecord | null>(null)
  const [isLoadingEvents, setIsLoadingEvents] = useState(true)
  const [eventLoadError, setEventLoadError] = useState('')
  const { overlayRef, introDismissed, startReveal } = useHeroReveal(heroRef, headerRef)
  const handleModelReady = useCallback((model?: Group, riseDistance?: number) => {
    startReveal(model, riseDistance)
  }, [startReveal])

  useEffect(() => {
    let active = true
    fetchPublicEvents()
      .then((events) => {
        if (!active) return
        const upcoming = events
          .filter((event) => !event.ended)
          .sort((left, right) => left.startsAt.localeCompare(right.startsAt))
        setNextEvent(upcoming[0] ?? null)
      })
      .catch(() => {
        if (active) setEventLoadError('Event updates could not be loaded. Please visit the Events page or contact the club.')
      })
      .finally(() => {
        if (active) setIsLoadingEvents(false)
      })
    return () => {
      active = false
    }
  }, [])

  return (
    <>
      <SiteHeader ref={headerRef} reveal />

      <main>
        <section className="hero-section framed-section" id="home" aria-labelledby="hero-title" ref={heroRef}>
          <div className="star-field" aria-hidden="true" />
        <div className="hero-kicker" data-reveal="primary"><span /> STUDENT AERODESIGN & AERIAL ROBOTICS</div>
        <p className="hero-overline" data-reveal="primary">BUILDING WHAT FLIES NEXT</p>
        <h1 id="hero-title" className="hero-title" data-reveal="title">
            <span>SOARING</span>
            <span className="title-bottom">EAGLES</span>
          </h1>
        <div className="hero-meta hero-meta-left" data-reveal="support">
            <span>OUR FIELD</span>
            <strong>AEROSPACE<br />ENGINEERING</strong>
          </div>
        <div className="hero-meta hero-meta-right" data-reveal="support">
            <span>BASED IN</span>
          <strong>SIDDAGANGA INSTITUTE<br />OF TECHNOLOGY<br />TUMAKURU, INDIA</strong>
          </div>

        <DroneExperienceLoader onModelReady={handleModelReady} pointerTarget={heroRef} />

        <div className="hero-copy" data-reveal="support">
            <p>We design, build, and fly ideas into reality.</p>
          </div>
        <div className="hero-bottom" data-reveal="interface">
            <span>STUDENT-LED · ENGINEERED TO FLY</span>
            <a href="#events">SCROLL TO EXPLORE <span aria-hidden="true">↓</span></a>
            <span>13.34° N / 77.10° E</span>
          </div>
        </section>

        <section className="upcoming-section content-section" id="events" aria-labelledby="upcoming-title">
          <GridVignetteBackground />
          <div className="section-heading">
            <div>
              <div className="section-index">01 / WHAT'S NEXT</div>
              <h2 id="upcoming-title">On the <em>horizon.</em></h2>
            </div>
            <p>Meet us at our next event.<br />Updates appear here when announced.</p>
          </div>
          <div className="upcoming-card">
            <div className="upcoming-card-index" aria-hidden="true">
              <span>FLIGHT LOG</span>
              <strong>01</strong>
              <i />
            </div>
            <div className="upcoming-card-copy">
              <span className="event-status"><i /> {nextEvent?.status === 'CLOSED' ? 'EVENT CLOSED' : nextEvent ? 'UPCOMING EVENT' : 'EVENT UPDATES'}</span>
              {isLoadingEvents ? (
                <h3 role="status">Loading upcoming events…</h3>
              ) : eventLoadError ? (
                <>
                  <h3>Event updates unavailable</h3>
                  <p>{eventLoadError}</p>
                </>
              ) : nextEvent ? (
                <>
                  <h3>{nextEvent.title}</h3>
                  <p>
                    {new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium', timeStyle: 'short', timeZone: 'Asia/Kolkata' }).format(new Date(nextEvent.startsAt))}
                    {' · '}{nextEvent.venue}{' · '}₹{Number(nextEvent.feeInr).toLocaleString('en-IN')}
                  </p>
                </>
              ) : (
                <>
                  <h3>No upcoming events announced</h3>
                  <p>There are no upcoming events to share right now. Check back for the next Soaring Eagles announcement.</p>
                </>
              )}
            </div>
            <Link className="upcoming-card-link" to={nextEvent ? `/events/${nextEvent.id}` : '/events'}>
              {nextEvent ? 'VIEW EVENT DETAILS' : 'EXPLORE EVENT ARCHIVE'} <span aria-hidden="true">↗</span>
            </Link>
          </div>
        </section>

        <section className="contact-section content-section" id="contact" aria-labelledby="contact-title">
          <div className="contact-information">
            <div className="section-index">02 / GET IN TOUCH</div>
            <div className="contact-information-card">
              <h2 id="contact-title">Contact Information</h2>
              <div className="contact-information-list">
                <div className="contact-information-item">
                  <span className="contact-information-icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24"><rect x="3" y="5" width="18" height="14" rx="2" /><path d="m3 7 9 6 9-6" /></svg>
                  </span>
                  <div><h3>Email</h3><a href="mailto:tseofficial2011@gmail.com">tseofficial2011@gmail.com</a></div>
                </div>
                <div className="contact-information-item">
                  <span className="contact-information-icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24"><path d="M7 9v12M7 5.5v.1M11 21v-7.5a4.5 4.5 0 0 1 9 0V21M3 9h8v12H3z" /></svg>
                  </span>
                  <div><h3>LinkedIn</h3><a href="https://linkedin.com/in/soaring-eagles-a7a19831b" target="_blank" rel="noreferrer">linkedin.com/in/soaring-eagles-a7a19831b</a></div>
                </div>
                <div className="contact-information-item">
                  <span className="contact-information-icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24"><path d="M22 5.9a8 8 0 0 1-2.4.7 4.2 4.2 0 0 0 1.8-2.3 8.3 8.3 0 0 1-2.7 1 4.1 4.1 0 0 0-7 3.7 11.7 11.7 0 0 1-8.5-4.3 4.1 4.1 0 0 0 1.3 5.5 4 4 0 0 1-1.9-.5v.1a4.1 4.1 0 0 0 3.3 4 4 4 0 0 1-1.8.1 4.1 4.1 0 0 0 3.8 2.8A8.3 8.3 0 0 1 2 18.4 11.7 11.7 0 0 0 20 8.5v-.6A8.2 8.2 0 0 0 22 5.9Z" /></svg>
                  </span>
                  <div><h3>X (Twitter)</h3><a href="https://x.com/TSE_015" target="_blank" rel="noreferrer">x.com/TSE_015</a></div>
                </div>
                <div className="contact-information-item">
                  <span className="contact-information-icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24"><path d="M20 10c0 5-8 12-8 12S4 15 4 10a8 8 0 1 1 16 0Z" /><circle cx="12" cy="10" r="2.5" /></svg>
                  </span>
                  <div><h3>Location</h3><address>Soaring Eagles Aeromodelling Lab<br />Near Mechanical Block,<br />Siddaganga Institute of Technology<br />Tumkur, 572103</address></div>
                </div>
              </div>
            </div>
          </div>
          <HomeContactForm />
        </section>
      </main>

      <footer className="site-footer">
        <Link className="brand" to="/" aria-label="Back to Soaring Eagles home">
          <img src="/images/brand/club-logo.png" alt="" />
          <span>SOARING<br />EAGLES</span>
        </Link>
        <span>STUDENT AERODESIGN & AERIAL ROBOTICS · TUMAKURU, INDIA</span>
        <span className="model-credit">
          3D concept by <a href="https://sketchfab.com/Diamonddogkz" target="_blank" rel="noreferrer">Diamonddogkz</a> · Sketchfab
        </span>
        <a href="mailto:tseofficial2011@gmail.com">GET IN TOUCH ↗</a>
      </footer>
      {!introDismissed && <HeroIntroOverlay overlayRef={overlayRef} />}
    </>
  )
}

function ScrollToTop() {
  const { pathname, hash } = useLocation()

  useEffect(() => {
    if (hash) {
      document.getElementById(hash.slice(1))?.scrollIntoView()
      return
    }

    window.scrollTo(0, 0)
  }, [hash, pathname])

  return null
}

function App() {
  return (
    <BrowserRouter>
      <ScrollToTop />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/events" element={<EventsPage />} />
        <Route path="/events/:eventId" element={<EventDetailsPage />} />
        <Route path="/our-journey" element={<OurJourneyPage />} />
        <Route path="/projects" element={<ProjectsPage />} />
        <Route path="/team" element={<TeamPage />} />
        <Route path="/admin-preview/login" element={<AdminLoginPreview />} />
        <Route path="/admin-preview" element={<AdminPortalPreview />} />
        <Route path="/admin" element={<AdminSessionPage />} />
        <Route path="*" element={<main className="team-main"><h1>Page not found</h1><Link to="/">Back home</Link></main>} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
