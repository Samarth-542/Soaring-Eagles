import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { fetchAdminEvents } from '../../lib/eventsApi'
import type { EventRecord } from '../../lib/eventsApi'
import {
  downloadRegistrationExport,
  fetchAdminRegistrations,
  retryRegistrationEmail,
  toRegistrationInput,
  updateAdminRegistration,
} from '../../lib/adminRegistrationsApi'
import type { AdminRegistrationRecord } from '../../lib/adminRegistrationsApi'
import type { RegistrationInput, StudyYear, TeamMemberInput } from '../../lib/registrationsApi'
import './RegistrationManagement.css'

const years: { value: StudyYear; label: string }[] = [
  { value: 'FIRST', label: '1st year' },
  { value: 'SECOND', label: '2nd year' },
  { value: 'THIRD', label: '3rd year' },
  { value: 'FOURTH', label: '4th year' },
]

function memberBlank(): TeamMemberInput {
  return { name: '', year: 'FIRST', department: '', institution: '', email: '' }
}

function studyYear(value: string): StudyYear {
  return years.find((year) => year.value === value)?.value ?? 'FIRST'
}

function displayStatus(status: string) {
  return status.split('_').join(' ').toLowerCase()
}

function RegistrationManagement() {
  const [events, setEvents] = useState<EventRecord[]>([])
  const [eventId, setEventId] = useState('')
  const [registrations, setRegistrations] = useState<AdminRegistrationRecord[]>([])
  const [selected, setSelected] = useState<AdminRegistrationRecord | null>(null)
  const [form, setForm] = useState<RegistrationInput | null>(null)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalRegistrations, setTotalRegistrations] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [isSaving, setIsSaving] = useState(false)
  const [isExporting, setIsExporting] = useState(false)
  const [isRetryingEmail, setIsRetryingEmail] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  useEffect(() => {
    let active = true
    fetchAdminEvents()
      .then((items) => {
        if (!active) return
        setEvents(items)
        setEventId(items[0]?.id ?? '')
      })
      .catch((requestError) => {
        if (active) setError(requestError instanceof Error ? requestError.message : 'Events could not be loaded.')
      })
      .finally(() => {
        if (active) setIsLoading(false)
      })
    return () => { active = false }
  }, [])

  useEffect(() => {
    if (!eventId) {
      setRegistrations([])
      setSelected(null)
      setTotalPages(0)
      setTotalRegistrations(0)
      return
    }
    let active = true
    setIsLoading(true)
    setError('')
    fetchAdminRegistrations(eventId, page)
      .then((result) => {
        if (!active) return
        setRegistrations(result.content)
        setTotalPages(result.totalPages)
        setTotalRegistrations(result.totalElements)
        setSelected((current) => result.content.find((item) => item.id === current?.id) ?? null)
      })
      .catch((requestError) => {
        if (active) setError(requestError instanceof Error ? requestError.message : 'Registrations could not be loaded.')
      })
      .finally(() => {
        if (active) setIsLoading(false)
      })
    return () => { active = false }
  }, [eventId, page])

  function selectRegistration(record: AdminRegistrationRecord) {
    setSelected(record)
    setForm(toRegistrationInput(record))
    setNotice('')
  }

  function updateField<K extends keyof RegistrationInput>(field: K, value: NonNullable<RegistrationInput[K]>) {
    setForm((current) => current ? { ...current, [field]: value } : current)
  }

  function updateMember<K extends keyof TeamMemberInput>(index: number, field: K, value: TeamMemberInput[K]) {
    setForm((current) => {
      if (!current?.members) return current
      return {
        ...current,
        members: current.members.map((member, memberIndex) =>
          memberIndex === index ? { ...member, [field]: value } : member),
      }
    })
  }

  async function saveRegistration(formEvent: FormEvent<HTMLFormElement>) {
    formEvent.preventDefault()
    if (!selected || !form) return
    setIsSaving(true)
    setError('')
    setNotice('')
    try {
      const updated = await updateAdminRegistration(selected.id, form)
      setSelected(updated)
      setRegistrations((current) => current.map((item) => item.id === updated.id ? updated : item))
      setForm(toRegistrationInput(updated))
      setNotice('Registration details saved.')
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Registration could not be updated.')
    } finally {
      setIsSaving(false)
    }
  }

  async function exportCsv() {
    setIsExporting(true)
    setError('')
    try {
      await downloadRegistrationExport(eventId)
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Export could not be created.')
    } finally {
      setIsExporting(false)
    }
  }

  async function retryEmail() {
    if (!selected) return
    setIsRetryingEmail(true)
    setError('')
    setNotice('')
    try {
      await retryRegistrationEmail(selected.id)
      setNotice('Confirmation email queued for retry.')
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Email retry could not be queued.')
      setIsRetryingEmail(false)
      return
    }

    try {
      const result = await fetchAdminRegistrations(eventId, page)
      setRegistrations(result.content)
      setSelected(result.content.find((item) => item.id === selected.id) ?? selected)
    } catch (requestError) {
      setError(requestError instanceof Error
        ? `Email retry was queued, but the registration list could not be refreshed: ${requestError.message}`
        : 'Email retry was queued, but the registration list could not be refreshed.')
    } finally {
      setIsRetryingEmail(false)
    }
  }

  const selectedEvent = events.find((item) => item.id === eventId)
  const teamForm = selected?.registrationType === 'TEAM' ? form?.members ?? [] : []

  return (
    <section className="admin-registration-management" aria-labelledby="admin-page-title">
      <div className="admin-page-heading">
        <div>
          <p className="admin-eyebrow">OPERATIONS / PRIVATE PARTICIPANT DATA</p>
          <h1 id="admin-page-title">Registrations<span>.</span></h1>
          <p>Review participant and payment records. Only signed-in admins can access this data.</p>
        </div>
        <div className="admin-system-state"><span aria-hidden="true" /> ADMIN ONLY</div>
      </div>

      <div className="registration-admin-toolbar">
        <label>
          Event
          <select value={eventId} onChange={(event) => {
            setEventId(event.target.value)
            setPage(0)
            setRegistrations([])
            setSelected(null)
            setForm(null)
            setTotalRegistrations(0)
            setTotalPages(0)
            setError('')
            setNotice('')
          }}>
            <option value="">Select an event</option>
            {events.map((event) => <option value={event.id} key={event.id}>{event.title}</option>)}
          </select>
        </label>
        <button type="button" disabled={!eventId || isExporting} onClick={() => void exportCsv()}>
          {isExporting ? 'Preparing export…' : 'Export CSV'}
        </button>
      </div>

      {error && <p className="registration-admin-error" role="alert">{error}</p>}
      {notice && <p className="registration-admin-notice" role="status">{notice}</p>}

      <div className="registration-admin-grid">
        <section className="registration-admin-list admin-panel" aria-label="Event registrations">
          <div className="admin-panel-heading">
            <div>
              <p className="admin-eyebrow">EVENT RECORDS</p>
              <h2>{selectedEvent?.title ?? 'Choose an event'}</h2>
            </div>
            <span className="admin-panel-tag">{totalRegistrations} TOTAL</span>
          </div>
          {isLoading ? (
            <p role="status">Loading registrations…</p>
          ) : registrations.length === 0 ? (
            <p className="registration-admin-empty">No registrations for this event yet.</p>
          ) : (
            <div className="registration-admin-table-wrap">
              <table className="registration-admin-table">
                <thead><tr><th>Registrant</th><th>Status</th><th>Payment</th></tr></thead>
                <tbody>
                  {registrations.map((record) => (
                    <tr key={record.id}>
                      <td>
                        <button type="button" onClick={() => selectRegistration(record)}>
                          <strong>{record.registrationType === 'TEAM' ? record.teamName : record.individualName}</strong>
                          <span>{record.registrationType === 'TEAM' ? record.captainEmail : record.individualEmail}</span>
                        </button>
                      </td>
                      <td><span className={`registration-admin-state state-${record.status.toLowerCase()}`}>{displayStatus(record.status)}</span></td>
                      <td>{displayStatus(record.paymentStatus ?? 'not started')}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          {totalPages > 1 && (
            <div className="registration-admin-pagination">
              <button type="button" disabled={page === 0} onClick={() => setPage((current) => current - 1)}>Previous</button>
              <span>Page {page + 1} of {totalPages}</span>
              <button type="button" disabled={page + 1 >= totalPages} onClick={() => setPage((current) => current + 1)}>Next</button>
            </div>
          )}
        </section>

        <section className="registration-admin-detail admin-panel" aria-label="Registration details">
          {!selected ? (
            <div className="registration-admin-empty">Select a registration to inspect or edit its details.</div>
          ) : (
            <>
              <div className="admin-panel-heading">
                <div>
                  <p className="admin-eyebrow">{selected.status.split('_').join(' ')}</p>
                  <h2>{selected.registrationType === 'TEAM' ? selected.teamName : selected.individualName}</h2>
                </div>
                <span className="admin-panel-tag">₹{Number(selected.feeInr).toLocaleString('en-IN')}</span>
              </div>
              <dl className="registration-admin-facts">
                <div><dt>Event</dt><dd>{selected.eventTitle}</dd></div>
                <div><dt>Payment status</dt><dd>{displayStatus(selected.paymentStatus ?? 'not started')}</dd></div>
                <div><dt>Payment reference</dt><dd>{selected.razorpayPaymentId ?? '—'}</dd></div>
                <div><dt>Receipt email</dt><dd>{displayStatus(selected.emailDeliveryStatus ?? 'not queued')} · {selected.emailDeliveryAttempts} attempts</dd></div>
                <div><dt>Created</dt><dd>{new Date(selected.createdAt).toLocaleString('en-IN')}</dd></div>
              </dl>
              {selected.emailDeliveryError && <p className="registration-admin-error">{selected.emailDeliveryError}</p>}
              {(selected.emailDeliveryStatus === 'FAILED' || selected.emailDeliveryStatus === 'PENDING') && (
                <button type="button" className="registration-admin-secondary" disabled={isRetryingEmail} onClick={() => void retryEmail()}>
                  {isRetryingEmail ? 'Queuing…' : 'Retry confirmation email'}
                </button>
              )}
              {selected.status === 'PAYMENT_REVIEW' && (
                <p className="registration-admin-error">Captured payment needs manual review. Verify the provider dashboard before taking action.</p>
              )}

              {form && (
                <form className="registration-admin-edit" onSubmit={(event) => void saveRegistration(event)}>
                  <p className="admin-eyebrow">EDIT PRIVATE DETAILS</p>
                  {selected.registrationType === 'TEAM' ? (
                    <>
                      <label>Team name<input required value={form.teamName ?? ''} onChange={(e) => updateField('teamName', e.target.value)} /></label>
                      <label>Captain name<input required value={form.captainName ?? ''} onChange={(e) => updateField('captainName', e.target.value)} /></label>
                      <label>Captain phone<input required value={form.captainPhone ?? ''} onChange={(e) => updateField('captainPhone', e.target.value)} /></label>
                      <label>Captain email<input required type="email" value={form.captainEmail ?? ''} onChange={(e) => updateField('captainEmail', e.target.value)} /></label>
                      {teamForm.map((member, index) => (
                        <fieldset key={index}>
                          <legend>Member {index + 1}</legend>
                          <label>Name<input required value={member.name} onChange={(e) => updateMember(index, 'name', e.target.value)} /></label>
                          <label>Study year<select value={member.year} onChange={(e) => updateMember(index, 'year', studyYear(e.target.value))}>
                            {years.map((year) => <option value={year.value} key={year.value}>{year.label}</option>)}
                          </select></label>
                          <label>Department<input required value={member.department} onChange={(e) => updateMember(index, 'department', e.target.value)} /></label>
                          <label>Institution<input required value={member.institution} onChange={(e) => updateMember(index, 'institution', e.target.value)} /></label>
                          <label>Email<input required type="email" value={member.email} onChange={(e) => updateMember(index, 'email', e.target.value)} /></label>
                        </fieldset>
                      ))}
                      {selectedEvent?.maximumTeamSize && (form.members?.length ?? 0) + 1 < selectedEvent.maximumTeamSize && (
                        <button type="button" className="registration-admin-secondary" onClick={() => setForm((current) => current ? { ...current, members: [...(current.members ?? []), memberBlank()] } : current)}>
                          + Add team member
                        </button>
                      )}
                      {selectedEvent?.minimumTeamSize && (form.members?.length ?? 0) + 1 > selectedEvent.minimumTeamSize && (
                        <button type="button" className="registration-admin-secondary" onClick={() => setForm((current) => current ? { ...current, members: current.members?.slice(0, -1) } : current)}>
                          Remove last member
                        </button>
                      )}
                    </>
                  ) : (
                    <>
                      <label>Name<input required value={form.name ?? ''} onChange={(e) => updateField('name', e.target.value)} /></label>
                      <label>Phone<input required value={form.phone ?? ''} onChange={(e) => updateField('phone', e.target.value)} /></label>
                      <label>Email<input required type="email" value={form.email ?? ''} onChange={(e) => updateField('email', e.target.value)} /></label>
                      <label>Study year<select value={form.year} onChange={(e) => updateField('year', studyYear(e.target.value))}>
                        {years.map((year) => <option value={year.value} key={year.value}>{year.label}</option>)}
                      </select></label>
                      <label>Department<input required value={form.department ?? ''} onChange={(e) => updateField('department', e.target.value)} /></label>
                      <label>Institution<input required value={form.institution ?? ''} onChange={(e) => updateField('institution', e.target.value)} /></label>
                    </>
                  )}
                  <button type="submit" className="registration-admin-save" disabled={isSaving}>
                    {isSaving ? 'Saving…' : 'Save registration'}
                  </button>
                </form>
              )}
            </>
          )}
        </section>
      </div>
    </section>
  )
}

export default RegistrationManagement
