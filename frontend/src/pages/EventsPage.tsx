import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import SiteHeader from '../components/SiteHeader'
import EventTimeline from '../components/ui/event-timeline'
import { fetchPublicEvents } from '../lib/eventsApi'
import type { PublicEventRecord } from '../lib/eventsApi'
import './EventsPage.css'

function EventsPage() {
  const [events, setEvents] = useState<PublicEventRecord[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [loadError, setLoadError] = useState('')

  useEffect(() => {
    document.title = 'Events | Soaring Eagles'
    let active = true
    fetchPublicEvents()
      .then((records) => {
        if (active) setEvents(records)
      })
      .catch(() => {
        if (active) setLoadError('Event updates could not be loaded. Please try again later.')
      })
      .finally(() => {
        if (active) setIsLoading(false)
      })
    return () => {
      active = false
      document.title = 'Soaring Eagles | Aerial Robotics Club'
    }
  }, [])

  const upcomingEvents = events
    .filter((event) => !event.ended)
    .sort((left, right) => left.startsAt.localeCompare(right.startsAt))
  const archivedEvents = events.filter((event) => event.ended)

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
          {isLoading ? (
            <div className="events-upcoming-state" role="status">Loading published events…</div>
          ) : loadError ? (
            <div className="events-upcoming-state events-load-error" role="alert">{loadError}</div>
          ) : upcomingEvents.length > 0 ? (
            <div className="events-upcoming-list">
              {upcomingEvents.map((event) => (
                <article className="events-upcoming-state" key={event.id}>
                  <span className="events-status"><i aria-hidden="true" /> {event.status === 'CLOSED' ? 'EVENT CLOSED' : 'UPCOMING EVENT'}</span>
                  <h3>{event.title}</h3>
                  <p>{event.description}</p>
                  <p className="events-event-meta">
                    {new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium', timeStyle: 'short', timeZone: 'Asia/Kolkata' }).format(new Date(event.startsAt))}
                    {' · '}{event.venue}{' · '}₹{Number(event.feeInr).toLocaleString('en-IN')}
                  </p>
                  <Link to={`/events/${event.id}`}>VIEW EVENT DETAILS <span aria-hidden="true">↗</span></Link>
                </article>
              ))}
            </div>
          ) : (
            <div className="events-upcoming-state">
              <span className="events-status"><i aria-hidden="true" /> EVENT UPDATES</span>
              <h3>No upcoming events announced</h3>
              <p>There are no upcoming events to share right now. Check back for the next Soaring Eagles announcement.</p>
              <Link to="/#contact">ASK US ABOUT EVENTS <span aria-hidden="true">↗</span></Link>
            </div>
          )}
        </section>

        <section className="events-archive" aria-labelledby="events-archive-title">
          <div className="events-archive-heading">
            <div>
              <p className="events-section-label">02 / PAST EVENTS</p>
              <h2 id="events-archive-title">The story so <em>far.</em></h2>
            </div>
            <p>Public event archive · newest first</p>
          </div>
          {archivedEvents.length > 0 && (
            <ul className="events-live-archive">
              {archivedEvents.map((event) => (
                <li key={event.id}>
                  <div>
                    <span>{new Date(event.startsAt).toLocaleDateString('en-IN', { dateStyle: 'medium', timeZone: 'Asia/Kolkata' })}</span>
                    <h3>{event.title}</h3>
                    <p>{event.archiveStory || event.description}</p>
                    {event.archiveResults && <p><strong>Results</strong>{event.archiveResults}</p>}
                    {event.archivePhotos.length > 0 && (
                      <div className="events-detail-gallery" aria-label={`${event.title} archive photos`}>
                        {event.archivePhotos.map((photo) => <img src={photo} alt={`${event.title} archive photo`} key={photo} />)}
                      </div>
                    )}
                  </div>
                  <Link to={`/events/${event.id}`}>EVENT DETAILS ↗</Link>
                </li>
              ))}
            </ul>
          )}
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
