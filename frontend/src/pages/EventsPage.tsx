import { useEffect } from 'react'
import { Link } from 'react-router-dom'
import SiteHeader from '../components/SiteHeader'
import EventTimeline from '../components/ui/event-timeline'
import './EventsPage.css'

function EventsPage() {
  useEffect(() => {
    document.title = 'Events | Soaring Eagles'
    return () => {
      document.title = 'Soaring Eagles | Aerial Robotics Club'
    }
  }, [])

  return (
    <div className="events-page">
      <SiteHeader />
      <main className="events-main">
        <section className="events-intro" aria-labelledby="events-title">
          <p className="events-kicker"><span aria-hidden="true" /> FLIGHT LOG / SOARING EAGLES</p>
          <h1 id="events-title">Meet, make<br />and <em>take flight.</em></h1>
          <p className="events-intro-copy">
            Workshops, exhibitions, and conversations that bring flight and engineering to life.
          </p>
        </section>

        <section className="events-upcoming" aria-labelledby="events-upcoming-title">
          <div>
            <p className="events-section-label">01 / WHAT'S NEXT</p>
            <h2 id="events-upcoming-title">On the <em>horizon.</em></h2>
          </div>
          <div className="events-upcoming-state">
            <span className="events-status"><i aria-hidden="true" /> EVENT UPDATES</span>
            <h3>No upcoming events announced</h3>
            <p>There are no upcoming events to share right now. Check back for the next Soaring Eagles announcement.</p>
            <Link to="/#contact">ASK US ABOUT EVENTS <span aria-hidden="true">↗</span></Link>
          </div>
        </section>

        <section className="events-archive" aria-labelledby="events-archive-title">
          <div className="events-archive-heading">
            <div>
              <p className="events-section-label">02 / PAST EVENTS</p>
              <h2 id="events-archive-title">The story so <em>far.</em></h2>
            </div>
            <p>Public event archive · newest first</p>
          </div>
          <EventTimeline />
        </section>
      </main>

      <footer className="site-footer events-footer">
        <Link className="brand" to="/" aria-label="Back to Soaring Eagles home">
          <img src="/images/brand/club-logo.png" alt="" />
          <span>SOARING<br />EAGLES</span>
        </Link>
        <span>STUDENT AERODESIGN · TUMAKURU, INDIA</span>
        <Link to="/#contact">GET IN TOUCH ↗</Link>
      </footer>
    </div>
  )
}

export default EventsPage
