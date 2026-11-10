import { useCallback, useEffect } from 'react'
import { Link, useParams } from 'react-router-dom'
import PublicDataStatus from '../components/PublicDataStatus'
import ResponsiveImage from '../components/ResponsiveImage'
import RegistrationForm from '../components/RegistrationForm'
import SiteHeader from '../components/SiteHeader'
import { fetchPublicEvent } from '../lib/eventsApi'
import type { PublicEventRecord } from '../lib/eventsApi'
import { usePublicData } from '../lib/usePublicData'
import './EventsPage.css'

function EventDetailsPage() {
  const { eventId = '' } = useParams()
  const loadEvent = useCallback(() => fetchPublicEvent(eventId), [eventId])
  const eventData = usePublicData<PublicEventRecord>(`event:${eventId}`, 25_000, loadEvent)
  const event = eventData.data

  useEffect(() => {
    document.title = event ? `${event.title} | Soaring Eagles` : 'Event Details | Soaring Eagles'
    return () => {
      document.title = 'Soaring Eagles | Aerial Robotics Club'
    }
  }, [event])

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
        {!event ? (
          <section className="events-detail-state" role="alert">
            <PublicDataStatus
              isLoading={eventData.isLoading}
              isRefreshing={eventData.isRefreshing}
              error={eventData.error}
              hasData={false}
              errorMessage="This event could not be found or is not public."
              onRetry={eventData.retry}
            />
            <Link to="/events">BACK TO EVENTS →</Link>
          </section>
        ) : (
          <>
            <PublicDataStatus
              isLoading={false}
              isRefreshing={eventData.isRefreshing}
              error={eventData.error}
              hasData
              errorMessage=""
              onRetry={eventData.retry}
            />
            <article className="events-detail">
            {event.posterUrl && <ResponsiveImage className="events-detail-poster" src={event.posterUrl} alt={`${event.title} poster`} loading="eager" />}
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
                {event.archivePhotos.map((photo) => <ResponsiveImage src={photo} alt={`${event.title} archive photo`} key={photo} />)}
              </div>
            )}
            <Link to="/events">← ALL EVENTS</Link>
            </article>
          </>
        )}
      </main>
    </div>
  )
}

export default EventDetailsPage
