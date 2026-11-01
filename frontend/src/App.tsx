import { lazy, Suspense, useCallback, useRef } from 'react'
import type { Group } from 'three'
import './App.css'
import HeroIntroOverlay from './components/HeroIntroOverlay'
import { useHeroReveal } from './components/HeroReveal'

const DroneExperience = lazy(() => import('./DroneExperience'))

function DroneExperienceLoader({ onModelReady }: { onModelReady: (model?: Group, riseDistance?: number) => void }) {
  return (
    <Suspense fallback={<div className="scene-loading hero-scene-placeholder" role="status">PREPARING AIRCRAFT</div>}>
      <DroneExperience onModelReady={onModelReady} />
    </Suspense>
  )
}

function App() {
  const headerRef = useRef<HTMLElement>(null)
  const heroRef = useRef<HTMLElement>(null)
  const { overlayRef, introDismissed, startReveal } = useHeroReveal(heroRef, headerRef)
  const handleModelReady = useCallback((model?: Group, riseDistance?: number) => {
    startReveal(model, riseDistance)
  }, [startReveal])

  return (
    <>
      <header className="site-header" ref={headerRef}>
        <a className="brand" href="#home" aria-label="Soaring Eagles home" data-reveal="header">
          <img src="/images/brand/club-logo.png" alt="" />
          <span>SOARING<br />EAGLES</span>
        </a>
        <nav className="main-nav" aria-label="Main navigation" data-reveal="header">
          <a href="#mission">MISSION</a>
          <a href="#projects">PROJECTS</a>
          <a href="#journey">JOURNEY</a>
          <a href="#events">EVENTS</a>
          <a href="#team">TEAM</a>
        </nav>
        <a className="header-cta" href="#team" data-reveal="header">JOIN THE FLIGHT <span aria-hidden="true">↗</span></a>
      </header>

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

        <DroneExperienceLoader onModelReady={handleModelReady} />

        <div className="hero-copy" data-reveal="support">
            <p>We design, build, and fly ideas into reality.</p>
          </div>
        <div className="hero-bottom" data-reveal="interface">
            <span>STUDENT-LED · ENGINEERED TO FLY</span>
            <a href="#mission">SCROLL TO EXPLORE <span aria-hidden="true">↓</span></a>
            <span>13.34° N / 77.10° E</span>
          </div>
        </section>

        <section className="intro-section content-section" id="mission">
          <div className="section-index">01 / OUR MISSION</div>
          <div className="intro-layout">
            <h2>Curiosity<br />takes <em>flight.</em></h2>
            <div className="intro-copy">
              <p>Soaring Eagles is a student engineering team exploring aerodesign and aerial robotics through hands-on making, testing, and collaboration.</p>
              <a className="text-link" href="#journey">GET TO KNOW US <span aria-hidden="true">↗</span></a>
            </div>
          </div>
          <div className="mission-rule"><span>DESIGN</span><i /><span>BUILD</span><i /><span>TEST</span><i /><span>FLY</span></div>
        </section>

        <section className="projects-section content-section" id="projects">
          <div className="section-heading">
            <div>
              <div className="section-index">02 / SELECTED WORK</div>
              <h2>Made to <em>take off.</em></h2>
            </div>
            <p>Ideas become aircraft through iteration,<br />teamwork, and time in the workshop.</p>
          </div>
          <div className="project-grid">
            <article className="project-card project-card-featured">
              <img src="/images/projects/gajraj.jpg" alt="Soaring Eagles aircraft project displayed outdoors" loading="lazy" />
              <div className="project-caption"><span>01 — AIRCRAFT DESIGN</span><span>EXPLORE THE PROJECT ↗</span></div>
            </article>
            <article className="project-card">
              <img src="/images/projects/netra.jpg" alt="A Soaring Eagles aerial robotics project" loading="lazy" />
              <div className="project-caption"><span>02 — AERIAL ROBOTICS</span><span>VIEW PROJECT ↗</span></div>
            </article>
          </div>
          <p className="content-note">Project names, descriptions, and image credits will be confirmed before launch.</p>
        </section>

        <section className="journey-section content-section" id="journey">
          <div className="section-index">03 / THE JOURNEY</div>
          <div className="journey-layout">
            <h2>Every flight<br />starts <em>somewhere.</em></h2>
            <p>From first sketches to competition day, each milestone is built by the people who show up, learn, and try again.</p>
          </div>
          <div className="journey-line"><span>IDEA</span><i /><span>PROTOTYPE</span><i /><span>FLIGHT TEST</span><i /><span>WHAT'S NEXT</span></div>
        </section>

        <section className="events-section content-section" id="events">
          <div className="section-heading">
            <div>
              <div className="section-index">04 / AROUND THE TEAM</div>
              <h2>Built <em>together.</em></h2>
            </div>
            <p>Workshops, competitions, and the moments<br />that bring our community together.</p>
          </div>
          <div className="event-feature">
            <img src="/images/events/design-challenge.jpg" alt="Soaring Eagles members at an aerodesign event" loading="lazy" />
            <div className="event-overlay"><span>FROM THE ARCHIVE</span><strong>Learning by<br />making and flying.</strong><a href="#team">MEET THE TEAM ↗</a></div>
          </div>
          <p className="content-note">Event details and current opportunities will be published after club review.</p>
        </section>

        <section className="join-section content-section" id="team">
          <div className="section-index">05 / YOUR NEXT CHAPTER</div>
          <h2>Ready to<br /><em>take flight?</em></h2>
          <p>Meet the people behind the projects and discover how to get involved.</p>
          <a className="primary-link" href="mailto:tseofficial2011@gmail.com">CONNECT WITH THE TEAM <span aria-hidden="true">↗</span></a>
        </section>
      </main>

      <footer className="site-footer">
        <a className="brand" href="#home" aria-label="Back to Soaring Eagles home">
          <img src="/images/brand/club-logo.png" alt="" />
          <span>SOARING<br />EAGLES</span>
        </a>
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

export default App
