import { FormEvent, useEffect, useState } from 'react'
import {
  closeEvent,
  createEvent,
  fetchAdminEvents,
  publishEvent,
  scheduleEvent,
  updateEvent,
} from '../../lib/eventsApi'
import type { EventInput, EventRecord } from '../../lib/eventsApi'
import { uploadContentImage } from '../../lib/contentApi'
import ImageUploadField from './ImageUploadField'
import './EventManagement.css'

interface EventForm {
  title: string
  description: string
  startsAt: string
  endsAt: string
  venue: string
  posterUrl: string
  eligibility: string
  feeInr: string
  registrationType: 'INDIVIDUAL' | 'TEAM'
  registrationDeadline: string
  capacity: string
  minimumTeamSize: string
  maximumTeamSize: string
  archiveStory: string
  archiveResults: string
  archivePhotos: string
  scheduledPublishAt: string
}

const emptyForm: EventForm = {
  title: '',
  description: '',
  startsAt: '',
  endsAt: '',
  venue: '',
  posterUrl: '',
  eligibility: '',
  feeInr: '0',
  registrationType: 'INDIVIDUAL',
  registrationDeadline: '',
  capacity: '1',
  minimumTeamSize: '',
  maximumTeamSize: '',
  archiveStory: '',
  archiveResults: '',
  archivePhotos: '',
  scheduledPublishAt: '',
}

function toLocalDateTime(value: string | null) {
  if (!value) return ''
  const parts = new Intl.DateTimeFormat('en-GB', {
    timeZone: 'Asia/Kolkata',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hourCycle: 'h23',
  }).formatToParts(new Date(value))
  const part = (type: Intl.DateTimeFormatPartTypes) => parts.find((item) => item.type === type)?.value ?? ''
  return `${part('year')}-${part('month')}-${part('day')}T${part('hour')}:${part('minute')}`
}

function toIstOffset(value: string) {
  return `${value}:00+05:30`
}

function eventToForm(event: EventRecord): EventForm {
  return {
    title: event.title,
    description: event.description,
    startsAt: toLocalDateTime(event.startsAt),
    endsAt: toLocalDateTime(event.endsAt),
    venue: event.venue,
    posterUrl: event.posterUrl ?? '',
    eligibility: event.eligibility,
    feeInr: String(event.feeInr),
    registrationType: event.registrationType,
    registrationDeadline: toLocalDateTime(event.registrationDeadline),
    capacity: String(event.capacity),
    minimumTeamSize: event.minimumTeamSize === null ? '' : String(event.minimumTeamSize),
    maximumTeamSize: event.maximumTeamSize === null ? '' : String(event.maximumTeamSize),
    archiveStory: event.archiveStory ?? '',
    archiveResults: event.archiveResults ?? '',
    archivePhotos: event.archivePhotos.join('\n'),
    scheduledPublishAt: toLocalDateTime(event.scheduledPublishAt),
  }
}

function toEventInput(form: EventForm): EventInput {
  return {
    title: form.title.trim(),
    description: form.description.trim(),
    startsAt: toIstOffset(form.startsAt),
    endsAt: toIstOffset(form.endsAt),
    venue: form.venue.trim(),
    posterUrl: form.posterUrl.trim() || null,
    eligibility: form.eligibility.trim(),
    feeInr: Number(form.feeInr),
    registrationType: form.registrationType,
    registrationDeadline: toIstOffset(form.registrationDeadline),
    capacity: Number(form.capacity),
    minimumTeamSize: form.registrationType === 'TEAM' ? Number(form.minimumTeamSize) : null,
    maximumTeamSize: form.registrationType === 'TEAM' ? Number(form.maximumTeamSize) : null,
    archiveStory: form.archiveStory.trim() || null,
    archiveResults: form.archiveResults.trim() || null,
    archivePhotos: form.archivePhotos.split(/\r?\n/).map((url) => url.trim()).filter(Boolean),
  }
}

function formatEventDate(value: string) {
  return new Intl.DateTimeFormat('en-IN', {
    dateStyle: 'medium',
    timeStyle: 'short',
    timeZone: 'Asia/Kolkata',
  }).format(new Date(value))
}

function EventManagement() {
  const [events, setEvents] = useState<EventRecord[]>([])
  const [form, setForm] = useState<EventForm>(emptyForm)
  const [posterFile, setPosterFile] = useState<File[]>([])
  const [archiveFiles, setArchiveFiles] = useState<File[]>([])
  const [editingId, setEditingId] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [busyAction, setBusyAction] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [statusMessage, setStatusMessage] = useState('')

  const loadEvents = async () => {
    setErrorMessage('')
    try {
      setEvents(await fetchAdminEvents())
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : 'Events could not be loaded.')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    void loadEvents()
  }, [])

  const resetForm = () => {
    setEditingId(null)
    setForm(emptyForm)
    setPosterFile([])
    setArchiveFiles([])
    setErrorMessage('')
    setStatusMessage('')
  }

  const saveEvent = async (action: 'draft' | 'schedule') => {
    setErrorMessage('')
    setStatusMessage('')
    if (action === 'schedule' && !form.scheduledPublishAt) {
      setErrorMessage('Choose a publication date and time in India Standard Time.')
      return
    }

    setBusyAction(action)
    try {
      const wasEditing = Boolean(editingId)
      let posterUrl = form.posterUrl.trim() || null
      if (posterFile[0]) {
        posterUrl = (await uploadContentImage(posterFile[0], 'events')).publicUrl
      }
      const archivePhotoUrls = form.archivePhotos.split(/\r?\n/).map((url) => url.trim()).filter(Boolean)
      for (const file of archiveFiles) {
        archivePhotoUrls.push((await uploadContentImage(file, 'events')).publicUrl)
      }
      const input = toEventInput({ ...form, posterUrl: posterUrl ?? '', archivePhotos: archivePhotoUrls.join('\n') })
      const saved = editingId
        ? await updateEvent(editingId, input)
        : await createEvent(input)
      if (!editingId) setEditingId(saved.id)
      if (action === 'schedule') {
        await scheduleEvent(saved.id, toIstOffset(form.scheduledPublishAt))
      }
      setStatusMessage(action === 'schedule'
        ? 'Event saved and scheduled.'
        : wasEditing ? 'Event changes saved.' : 'Event saved as a draft.')
      setEditingId(null)
      setForm(emptyForm)
      setPosterFile([])
      setArchiveFiles([])
      await loadEvents()
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : 'The event could not be saved.')
    } finally {
      setBusyAction('')
    }
  }

  const runEventAction = async (id: string, action: 'publish' | 'close') => {
    setErrorMessage('')
    setStatusMessage('')
    setBusyAction(`${action}:${id}`)
    try {
      if (action === 'publish') await publishEvent(id)
      else await closeEvent(id)
      setStatusMessage(action === 'publish' ? 'Event published.' : 'Event closed.')
      await loadEvents()
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : `The event could not be ${action}ed.`)
    } finally {
      setBusyAction('')
    }
  }

  const submitDraft = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    void saveEvent('draft')
  }

  return (
    <section className="admin-module-view admin-events-module" aria-labelledby="admin-page-title">
      <div className="admin-page-heading">
        <div>
          <p className="admin-eyebrow">CONTENT / MANAGEMENT</p>
          <h1 id="admin-page-title">Events<span>.</span></h1>
          <p>Create events as private drafts, then publish or schedule them for the public site.</p>
        </div>
        <div className="admin-system-state"><span aria-hidden="true" /> EVENT API CONNECTED</div>
      </div>

      <div className="admin-event-disclaimer" role="status">
        Registration capacity and participant tracking are connected. Paid checkout remains disabled until Razorpay is configured and enabled.
      </div>

      {(errorMessage || statusMessage) && (
        <p className={errorMessage ? 'admin-event-feedback is-error' : 'admin-event-feedback'} role={errorMessage ? 'alert' : 'status'}>
          {errorMessage || statusMessage}
        </p>
      )}

      <section className="admin-event-form-panel" aria-labelledby="admin-event-form-title">
        <div className="admin-event-panel-heading">
          <div>
            <p className="admin-eyebrow">{editingId ? 'UPDATE RECORD' : 'NEW RECORD'}</p>
            <h2 id="admin-event-form-title">{editingId ? 'Edit event' : 'Create an event'}</h2>
          </div>
          {editingId && <button className="admin-event-secondary-button" type="button" onClick={resetForm}>CANCEL EDIT</button>}
        </div>

        <form className="admin-event-form" onSubmit={submitDraft}>
          <label>
            Event title
            <input required maxLength={180} value={form.title} onChange={(event) => setForm({ ...form, title: event.target.value })} />
          </label>
          <label className="admin-event-field-wide">
            Description
            <textarea required maxLength={12000} rows={3} value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} />
          </label>
          <label>
            Venue
            <input required maxLength={240} value={form.venue} onChange={(event) => setForm({ ...form, venue: event.target.value })} />
          </label>
          <div className="admin-event-field-wide">
            <ImageUploadField
              label="Event poster"
              files={posterFile}
              onFilesChange={setPosterFile}
              currentUrls={form.posterUrl ? [form.posterUrl] : []}
              disabled={Boolean(busyAction)}
            />
          </div>
          <label>
            Starts (IST)
            <input required type="datetime-local" value={form.startsAt} onChange={(event) => setForm({ ...form, startsAt: event.target.value })} />
          </label>
          <label>
            Ends (IST)
            <input required type="datetime-local" value={form.endsAt} onChange={(event) => setForm({ ...form, endsAt: event.target.value })} />
          </label>
          <label>
            Registration deadline (IST)
            <input required type="datetime-local" value={form.registrationDeadline} onChange={(event) => setForm({ ...form, registrationDeadline: event.target.value })} />
          </label>
          <label>
            Eligibility
            <input required maxLength={3000} value={form.eligibility} onChange={(event) => setForm({ ...form, eligibility: event.target.value })} />
          </label>
          <label>
            Fee (INR)
            <input required type="number" min="0" step="0.01" value={form.feeInr} onChange={(event) => setForm({ ...form, feeInr: event.target.value })} />
          </label>
          <label>
            Registration type
            <select value={form.registrationType} onChange={(event) => setForm({ ...form, registrationType: event.target.value as EventForm['registrationType'] })}>
              <option value="INDIVIDUAL">Individual</option>
              <option value="TEAM">Team</option>
            </select>
          </label>
          <label>
            Maximum places {form.registrationType === 'TEAM' ? '(teams)' : '(individuals)'}
            <input required type="number" min="1" step="1" value={form.capacity} onChange={(event) => setForm({ ...form, capacity: event.target.value })} />
          </label>
          {form.registrationType === 'TEAM' && (
            <>
              <label>
                Minimum team size
                <input required type="number" min="1" step="1" value={form.minimumTeamSize} onChange={(event) => setForm({ ...form, minimumTeamSize: event.target.value })} />
              </label>
              <label>
                Maximum team size
                <input required type="number" min="1" step="1" value={form.maximumTeamSize} onChange={(event) => setForm({ ...form, maximumTeamSize: event.target.value })} />
              </label>
            </>
          )}
          <label className="admin-event-field-wide">
            Schedule publication (IST, optional)
            <input type="datetime-local" value={form.scheduledPublishAt} onChange={(event) => setForm({ ...form, scheduledPublishAt: event.target.value })} />
          </label>
          <label className="admin-event-field-wide">
            Archive story
            <textarea maxLength={12000} rows={3} value={form.archiveStory} onChange={(event) => setForm({ ...form, archiveStory: event.target.value })} />
          </label>
          <label>
            Results
            <textarea maxLength={12000} rows={3} value={form.archiveResults} onChange={(event) => setForm({ ...form, archiveResults: event.target.value })} />
          </label>
          <div className="admin-event-field-wide">
            <ImageUploadField
              label="Archive photos"
              files={archiveFiles}
              onFilesChange={setArchiveFiles}
              multiple
              currentUrls={form.archivePhotos.split(/\r?\n/).map((url) => url.trim()).filter(Boolean)}
              onRemoveCurrent={(url) => setForm({
                ...form,
                archivePhotos: form.archivePhotos.split(/\r?\n/).map((photo) => photo.trim()).filter((photo) => photo && photo !== url).join('\n'),
              })}
              disabled={Boolean(busyAction)}
            />
          </div>
          <div className="admin-event-form-actions admin-event-field-wide">
            <button type="submit" disabled={Boolean(busyAction)}>
              {busyAction === 'draft' ? 'SAVING…' : editingId ? 'SAVE CHANGES' : 'SAVE AS DRAFT'}
            </button>
            <button type="button" disabled={Boolean(busyAction)} onClick={() => void saveEvent('schedule')}>
              {busyAction === 'schedule' ? 'SCHEDULING…' : 'SAVE & SCHEDULE'}
            </button>
          </div>
        </form>
      </section>

      <section className="admin-event-list" aria-labelledby="admin-event-list-title">
        <div className="admin-event-panel-heading">
          <div>
            <p className="admin-eyebrow">EVENT REGISTER</p>
            <h2 id="admin-event-list-title">All events <span>{events.length}</span></h2>
          </div>
          <button className="admin-event-secondary-button" type="button" onClick={() => void loadEvents()} disabled={isLoading}>REFRESH</button>
        </div>

        {isLoading ? (
          <p className="admin-event-empty" role="status">Loading events…</p>
        ) : events.length === 0 ? (
          <p className="admin-event-empty">No events yet. Create the first draft above.</p>
        ) : (
          <ul className="admin-event-list-items">
            {events.map((event) => (
              <li className="admin-event-record" key={event.id}>
                <div className="admin-event-record-main">
                  <span className={`admin-event-state is-${event.status.toLowerCase()}`}>{event.status}</span>
                  <h3>{event.title}</h3>
                  <p>{formatEventDate(event.startsAt)} · {event.venue}</p>
                  <small>
                    ₹{Number(event.feeInr).toLocaleString('en-IN')} · {event.registrationType.toLowerCase()} · capacity {event.capacity}
                    {event.status === 'SCHEDULED' && event.scheduledPublishAt ? ` · publishes ${formatEventDate(event.scheduledPublishAt)}` : ''}
                    {event.ended ? ' · ended' : ''}
                    {!event.registrationWindowOpen && event.status === 'PUBLISHED' && !event.ended ? ' · registration window closed' : ''}
                  </small>
                </div>
                <div className="admin-event-record-actions">
                  <button type="button" onClick={() => {
                    setEditingId(event.id)
                    setForm(eventToForm(event))
                    setPosterFile([])
                    setArchiveFiles([])
                    setErrorMessage('')
                    setStatusMessage('')
                  }}>EDIT</button>
                  {event.status !== 'PUBLISHED' && (
                    <button type="button" disabled={Boolean(busyAction)} onClick={() => void runEventAction(event.id, 'publish')}>
                      {busyAction === `publish:${event.id}` ? '…' : 'PUBLISH NOW'}
                    </button>
                  )}
                  {event.status === 'PUBLISHED' && (
                    <button type="button" disabled={Boolean(busyAction)} onClick={() => void runEventAction(event.id, 'close')}>
                      {busyAction === `close:${event.id}` ? '…' : 'CLOSE'}
                    </button>
                  )}
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </section>
  )
}

export default EventManagement
