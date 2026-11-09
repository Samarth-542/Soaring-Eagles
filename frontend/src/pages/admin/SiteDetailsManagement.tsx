import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { fetchAdminSiteDetails, saveSiteDetails } from '../../lib/contentApi'
import type { SiteDetails } from '../../lib/contentApi'
import './SiteDetailsManagement.css'

const emptyDetails: SiteDetails = {
  email: '',
  linkedinUrl: '',
  xUrl: '',
  location: '',
}

function SiteDetailsManagement() {
  const [details, setDetails] = useState<SiteDetails>(emptyDetails)
  const [isLoading, setIsLoading] = useState(true)
  const [isSaving, setIsSaving] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  useEffect(() => {
    let active = true
    fetchAdminSiteDetails()
      .then((result) => {
        if (active) setDetails(result)
      })
      .catch((requestError: unknown) => {
        if (active) {
          setError(requestError instanceof Error ? requestError.message : 'Site details could not be loaded.')
        }
      })
      .finally(() => {
        if (active) setIsLoading(false)
      })
    return () => { active = false }
  }, [])

  function updateField(field: keyof SiteDetails, value: string) {
    setDetails((current) => ({ ...current, [field]: value }))
    setNotice('')
  }

  async function save(formEvent: FormEvent<HTMLFormElement>) {
    formEvent.preventDefault()
    setIsSaving(true)
    setError('')
    setNotice('')
    try {
      const updated = await saveSiteDetails(details)
      setDetails(updated)
      setNotice('Public site details saved.')
    } catch (requestError: unknown) {
      setError(requestError instanceof Error ? requestError.message : 'Site details could not be saved.')
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <section className="admin-site-details" aria-labelledby="admin-page-title">
      <div className="admin-page-heading">
        <div>
          <p className="admin-eyebrow">CONTENT / PUBLIC INFORMATION</p>
          <h1 id="admin-page-title">Site details<span>.</span></h1>
          <p>Update the contact information displayed on the public website.</p>
        </div>
        <div className="admin-system-state"><span aria-hidden="true" /> DATABASE CONNECTED</div>
      </div>

      <form className="admin-panel site-details-form" onSubmit={(event) => void save(event)}>
        {isLoading ? (
          <p className="site-details-status" role="status">Loading saved site details…</p>
        ) : (
          <>
            <div className="site-details-form-grid">
              <label>
                Public contact email
                <input
                  type="email"
                  name="email"
                  autoComplete="email"
                  maxLength={254}
                  required
                  value={details.email}
                  onChange={(event) => updateField('email', event.target.value)}
                />
              </label>
              <label>
                LinkedIn URL
                <input
                  type="url"
                  name="linkedinUrl"
                  maxLength={2048}
                  placeholder="https://linkedin.com/…"
                  value={details.linkedinUrl}
                  onChange={(event) => updateField('linkedinUrl', event.target.value)}
                />
              </label>
              <label>
                X (Twitter) URL
                <input
                  type="url"
                  name="xUrl"
                  maxLength={2048}
                  placeholder="https://x.com/…"
                  value={details.xUrl}
                  onChange={(event) => updateField('xUrl', event.target.value)}
                />
              </label>
              <label className="site-details-location">
                Club location
                <textarea
                  name="location"
                  rows={5}
                  maxLength={500}
                  required
                  value={details.location}
                  onChange={(event) => updateField('location', event.target.value)}
                />
              </label>
            </div>

            {error && <p className="site-details-status is-error" role="alert">{error}</p>}
            {notice && <p className="site-details-status" role="status">{notice}</p>}

            <div className="site-details-form-actions">
              <p>Changes are published immediately to the public contact section and footer.</p>
              <button type="submit" disabled={isSaving}>
                {isSaving ? 'SAVING…' : 'SAVE SITE DETAILS'}
              </button>
            </div>
          </>
        )}
      </form>
    </section>
  )
}

export default SiteDetailsManagement
