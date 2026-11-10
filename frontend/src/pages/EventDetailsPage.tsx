import { useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import PublicDataStatus from '../components/PublicDataStatus'
import ResponsiveImage from '../components/ResponsiveImage'
import RegistrationForm from '../components/RegistrationForm'
import SiteHeader from '../components/SiteHeader'
import { fetchPublicEvent } from '../lib/eventsApi'
import type { PublicEventRecord } from '../lib/eventsApi'
import { fetchPublicEventSnapshot } from '../lib/publicContentApi'
import { usePublicData } from '../lib/usePublicData'
import './EventsPage.css'

function EventDetailsPage() {
  const { eventId = '' } = useParams()
  const loadSnapshot = useCallback(() => fetchPublicEventSnapshot(eventId), [eventId])
  const eventData = usePublicData(`event:${eventId}`, 30_000, loadSnapshot)
  const eventDocument = eventData.data
  const event = eventDocument?.data
  const [registrationDetails, setRegistrationDetails] = useState<{
    eventId: string
    event: PublicEventRecord
  } | null>(null)
  const [registrationLoading, setRegistrationLoading] = useState(false)
  const [registrationError, setRegistrationError] = useState('')
  const liveEvent = registrationDetails?.eventId === eventId ? registrationDetails.event : null

  useEffect(() => {
    document.title = event ? `${event.title} | Soaring Eagles` : 'Event Details | Soaring Eagles'
    return () => {
      document.title = 'Soaring Eagles | Aerial Robotics Club'
    }
  }, [event])

  useEffect(() => {
    setRegistrationDetails(null)
    setRegistrationError('')
  }, [eventId])

  const date = (value: string) => new Intl.DateTimeFormat('en-IN', {
    dateStyle: 'full',
    timeStyle: 'short',
    timeZone: 'Asia/Kolkata',
  }).format(new Date(value))

  async function loadRegistrationDetails() {
    setRegistrationLoading(true)
    setRegistrationError('')
    try {
      const details = await fetchPublicEvent(eventId)
      setRegistrationDetails({ eventId, event: details })
    } catch (requestError) {
      setRegistrationError(requestError instanceof Error
        ? requestError.message
        : 'Live registration details could not be loaded.')
    } finally {
      setRegistrationLoading(false)
    }
  }

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
            <p className="events-section-label">{new Date(event.endsAt).getTime() < Date.now()
              ? 'PAST EVENT'
              : event.status === 'CLOSED' ? 'EVENT CLOSED' : 'UPCOMING EVENT'}</p>
            <h1>{event.title}</h1>
            <p className="events-detail-description">{event.description}</p>
            <dl className="events-detail-facts">
              <div><dt>Starts</dt><dd>{date(event.startsAt)}</dd></div>
              <div><dt>Ends</dt><dd>{date(event.endsAt)}</dd></div>
              <div><dt>Venue</dt><dd>{event.venue}</dd></div>
            </dl>
            {event.archiveStory && <section><h2>Event story</h2><p>{event.archiveStory}</p></section>}
            {event.archiveResults && <section><h2>Results</h2><p>{event.archiveResults}</p></section>}
            <section aria-labelledby="registration-options-title">
              <h2 id="registration-options-title">Registration</h2>
              <p className="events-registration-note">
                Registration details are loaded from the live service when requested. Availability is validated by the server when you submit.
              </p>
              {!liveEvent && (
                <button
                  type="button"
                  className="registration-submit"
                  onClick={() => void loadRegistrationDetails()}
                  disabled={registrationLoading}
                >
                  {registrationLoading ? 'Loading live registration details…' : 'Check registration details'}
                </button>
              )}
              {registrationError && (
                <div className="public-data-notice" role="alert">
                  <span>{registrationError}</span>
                  <button type="button" onClick={() => void loadRegistrationDetails()}>Retry</button>
                </div>
              )}
              {liveEvent && (
                <>
                  <dl className="events-detail-facts">
                    <div><dt>Eligibility</dt><dd>{liveEvent.eligibility}</dd></div>
                    <div><dt>Registration</dt><dd>{liveEvent.registrationType === 'TEAM'
                      ? `Teams · ${liveEvent.minimumTeamSize}–${liveEvent.maximumTeamSize} members`
                      : 'Individual'}</dd></div>
                    <div><dt>Fee</dt><dd>₹{Number(liveEvent.feeInr).toLocaleString('en-IN')}</dd></div>
                    <div><dt>Deadline</dt><dd>{date(liveEvent.registrationDeadline)}</dd></div>
                    <div><dt>Reported remaining places</dt><dd>{liveEvent.remainingCapacity ?? 'Unavailable'}</dd></div>
                  </dl>
                  <RegistrationForm event={liveEvent} />
                </>
              )}
            </section>
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
