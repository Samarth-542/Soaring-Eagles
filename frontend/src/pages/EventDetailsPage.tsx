import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import RegistrationForm from '../components/RegistrationForm'
import SiteHeader from '../components/SiteHeader'
import { fetchPublicEvent } from '../lib/eventsApi'
import type { PublicEventRecord } from '../lib/eventsApi'
import './EventsPage.css'

function EventDetailsPage() {
  const { eventId = '' } = useParams()
  const [event, setEvent] = useState<PublicEventRecord | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [loadError, setLoadError] = useState('')

  useEffect(() => {
    let active = true
    document.title = 'Event Details | Soaring Eagles'
    fetchPublicEvent(eventId)
      .then((record) => {
        if (active) {
          setEvent(record)
          document.title = `${record.title} | Soaring Eagles`
        }
      })
      .catch(() => {
        if (active) setLoadError('This event could not be found or is not public.')
      })
      .finally(() => {
        if (active) setIsLoading(false)
      })
    return () => {
      active = false
      document.title = 'Soaring Eagles | Aerial Robotics Club'
    }
  }, [eventId])

  const date = (value: string) => new Intl.DateTimeFormat('en-IN', {
    dateStyle: 'full',
    timeStyle: 'short',
    timeZone: 'Asia/Kolkata',
  }).format(new Date(value))

  return (
    <div className="events-page">
      <SiteHeader />
      <main className="events-main">
        <p className="events-kicker"><span aria-hidden="true" /> FLIGHT LOG / EVENT DETAILS</p>
        {isLoading ? (
          <p className="events-detail-state" role="status">Loading event…</p>
        ) : loadError || !event ? (
          <section className="events-detail-state" role="alert">
            <h1>Event unavailable.</h1>
            <p>{loadError || 'This event could not be found or is not public.'}</p>
            <Link to="/events">BACK TO EVENTS →</Link>
          </section>
        ) : (
          <article className="events-detail">
            {event.posterUrl && <img className="events-detail-poster" src={event.posterUrl} alt={`${event.title} poster`} />}
            <p className="events-section-label">{event.ended ? 'PAST EVENT' : event.status === 'CLOSED' ? 'EVENT CLOSED' : 'UPCOMING EVENT'}</p>
            <h1>{event.title}</h1>
            <p className="events-detail-description">{event.description}</p>
            <dl className="events-detail-facts">
              <div><dt>Starts</dt><dd>{date(event.startsAt)}</dd></div>
              <div><dt>Ends</dt><dd>{date(event.endsAt)}</dd></div>
              <div><dt>Venue</dt><dd>{event.venue}</dd></div>
              <div><dt>Eligibility</dt><dd>{event.eligibility}</dd></div>
              <div><dt>Registration</dt><dd>{event.registrationType === 'TEAM' ? `Teams · ${event.minimumTeamSize}–${event.maximumTeamSize} members` : 'Individual'}</dd></div>
              <div><dt>Fee</dt><dd>₹{Number(event.feeInr).toLocaleString('en-IN')}</dd></div>
              <div><dt>Deadline</dt><dd>{date(event.registrationDeadline)}</dd></div>
              <div><dt>Remaining capacity</dt><dd>{event.remainingCapacity ?? 'Unavailable'} {event.registrationType === 'TEAM' ? 'teams' : 'individuals'}</dd></div>
            </dl>
            {event.archiveStory && <section><h2>Event story</h2><p>{event.archiveStory}</p></section>}
            {event.archiveResults && <section><h2>Results</h2><p>{event.archiveResults}</p></section>}
            {event.registrationWindowOpen && event.onlineRegistrationAvailable && (event.remainingCapacity ?? 0) > 0 ? (
              <RegistrationForm event={event} />
            ) : (
              <p className="events-registration-note">
                {event.ended
                  ? 'This event has ended. Online registration is not available.'
                  : event.status === 'CLOSED'
                    ? 'The organizers have closed this event. Online registration is not available.'
                    : event.registrationDeadline && new Date(event.registrationDeadline) <= new Date()
                      ? 'The registration deadline has passed. Online registration is not available.'
                      : (event.remainingCapacity ?? 0) === 0
                        ? 'The event has reached its registration capacity.'
                          : event.registrationWindowOpen && !event.onlineRegistrationAvailable
                            ? 'Online payment setup is not available yet. Contact the club for participation details.'
                            : 'Online registration is not available. Contact the club for participation details.'}
              </p>
            )}
            {event.archivePhotos.length > 0 && (
              <div className="events-detail-gallery" aria-label="Event archive photos">
                {event.archivePhotos.map((photo) => <img src={photo} alt={`${event.title} archive photo`} key={photo} />)}
              </div>
            )}
            <Link to="/events">← ALL EVENTS</Link>
          </article>
        )}
      </main>
    </div>
  )
}

export default EventDetailsPage
