import { useState } from 'react'
import type { FormEvent } from 'react'

const maxAttachmentSize = 10 * 1024 * 1024
const allowedAttachmentTypes = new Set(['application/pdf', 'image/jpeg', 'image/png'])
const allowedAttachmentExtensions = /\.(pdf|jpe?g|png)$/i

export default function HomeContactForm() {
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [submissionMessage, setSubmissionMessage] = useState('')
  const [submissionFailed, setSubmissionFailed] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = event.currentTarget
    const formData = new FormData(form)
    const attachment = formData.get('attachment')

    if (attachment instanceof File && attachment.size > 0) {
      if (attachment.size > maxAttachmentSize) {
        setSubmissionFailed(true)
        setSubmissionMessage('Attachments must be 10 MB or smaller.')
        return
      }

      const hasAllowedType = allowedAttachmentTypes.has(attachment.type)
        || (!attachment.type && allowedAttachmentExtensions.test(attachment.name))
      if (!hasAllowedType) {
        setSubmissionFailed(true)
        setSubmissionMessage('Attach a PDF, JPEG, or PNG file.')
        return
      }
    }

    setIsSubmitting(true)
    setSubmissionFailed(false)
    setSubmissionMessage('')

    try {
      const response = await fetch('/api/contact', {
        method: 'POST',
        body: formData,
      })

      if (!response.ok) {
        throw new Error(`Contact submission failed with status ${response.status}.`)
      }

      form.reset()
      setSubmissionMessage('Your message has been sent. Thank you for getting in touch.')
    } catch (error) {
      console.error('Unable to submit the contact form.', error)
      setSubmissionFailed(true)
      setSubmissionMessage('We could not send your message right now. Please email the team using the address beside this form.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <form className="home-contact-form" onSubmit={handleSubmit}>
      <div className="contact-form-heading">
        <span>MESSAGE THE TEAM</span>
        <span>FIELDS MARKED * ARE REQUIRED</span>
      </div>

      <div className="contact-form-row">
        <label>
          <span>FULL NAME <i aria-hidden="true">*</i></span>
          <input autoComplete="name" name="name" maxLength={120} required />
        </label>
        <label>
          <span>EMAIL ADDRESS <i aria-hidden="true">*</i></span>
          <input autoComplete="email" name="email" type="email" maxLength={254} required />
        </label>
      </div>

      <label>
        <span>SUBJECT <i aria-hidden="true">*</i></span>
        <input name="subject" maxLength={180} required />
      </label>

      <label>
        <span>MESSAGE <i aria-hidden="true">*</i></span>
        <textarea name="message" rows={5} maxLength={5000} required />
      </label>

      <label className="contact-attachment">
        <span>ATTACH A DOCUMENT <small>OPTIONAL · PDF, JPEG OR PNG · MAX 10 MB</small></span>
        <input name="attachment" type="file" accept=".pdf,.jpg,.jpeg,.png,application/pdf,image/jpeg,image/png" />
      </label>

      <div className="contact-form-submit">
        <p
          className={`contact-form-status${submissionFailed ? ' is-error' : ''}`}
          role={submissionFailed ? 'alert' : 'status'}
          aria-live="polite"
        >
          {submissionMessage}
        </p>
        <button className="primary-link" type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'SENDING MESSAGE…' : 'SEND MESSAGE'}
          <span aria-hidden="true">{isSubmitting ? '…' : '↗'}</span>
        </button>
      </div>
      <p className="contact-form-note">
        Online submissions are not connected yet. In the meantime, email{' '}
        <a href="mailto:tseofficial2011@gmail.com">tseofficial2011@gmail.com</a>.
      </p>
    </form>
  )
}
