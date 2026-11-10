import { useEffect } from 'react'
import { Link } from 'react-router-dom'
import PublicDataStatus from '../components/PublicDataStatus'
import ResponsiveImage from '../components/ResponsiveImage'
import SiteHeader from '../components/SiteHeader'
import EventTimeline from '../components/ui/event-timeline'
import { fetchPublicEvents } from '../lib/eventsApi'
import { usePublicData } from '../lib/usePublicData'
import './EventsPage.css'

function EventsPage() {
  const eventData = usePublicData('events', 30_000, fetchPublicEvents)
  const events = eventData.data?.data ?? []

  useEffect(() => {
    document.title = 'Events | Soaring Eagles'
    return () => {
      document.title = 'Soaring Eagles | Aerial Robotics Club'
    }
  }, [])

  const upcomingEvents = events
    .filter((event) => new Date(event.endsAt).getTime() >= Date.now())
    .sort((left, right) => left.startsAt.localeCompare(right.startsAt))
  const archivedEvents = events.filter((event) => new Date(event.endsAt).getTime() < Date.now())

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
          <PublicDataStatus
            isLoading={eventData.isLoading}
            isRefreshing={eventData.isRefreshing}
            error={eventData.error}
            hasData={eventData.data !== null}
            errorMessage="Event updates could not be loaded. Please try again."
            onRetry={eventData.retry}
          />
          {!eventData.isLoading && upcomingEvents.length > 0 ? (
            <div className="events-upcoming-list">
              {upcomingEvents.map((event) => (
                <article className="events-upcoming-state" key={event.id}>
                  <span className="events-status"><i aria-hidden="true" /> {event.status === 'CLOSED' ? 'EVENT CLOSED' : 'UPCOMING EVENT'}</span>
                  <h3>{event.title}</h3>
                  <p>{event.description}</p>
                  <p className="events-event-meta">
                    {new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium', timeStyle: 'short', timeZone: 'Asia/Kolkata' }).format(new Date(event.startsAt))}
                    {' · '}{event.venue}
                  </p>
                  <Link to={`/events/${event.id}`}>VIEW EVENT DETAILS <span aria-hidden="true">↗</span></Link>
                </article>
              ))}
            </div>
          ) : !eventData.isLoading && !eventData.error ? (
            <div className="events-upcoming-state">
              <span className="events-status"><i aria-hidden="true" /> EVENT UPDATES</span>
              <h3>No upcoming events announced</h3>
              <p>There are no upcoming events to share right now. Check back for the next Soaring Eagles announcement.</p>
              <Link to="/#contact">ASK US ABOUT EVENTS <span aria-hidden="true">↗</span></Link>
            </div>
          ) : null}
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
                        {event.archivePhotos.map((photo) => <ResponsiveImage src={photo} alt={`${event.title} archive photo`} key={photo} />)}
                      </div>
                    )}
                  </div>
                  <Link to={`/events/${event.id}`}>EVENT DETAILS ↗</Link>
                </li>
              ))}
            </ul>
          )}
          <EventTimeline events={archivedEvents} />
        </section>
      </main>

      <footer className="site-footer events-footer">
        <Link className="brand" to="/" aria-label="Back to Soaring Eagles home">
          <ResponsiveImage src="/images/brand/club-logo.png" alt="" width={40} height={40} loading="eager" />
          <span>SOARING<br />EAGLES</span>
        </Link>
        <span>STUDENT AERODESIGN · TUMAKURU, INDIA</span>
        <Link to="/#contact">GET IN TOUCH ↗</Link>
      </footer>
    </div>
  )
}

export default EventsPage
