import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import type { PublicEventRecord } from '../lib/eventsApi'
import {
  fetchRegistrationStatus,
  retryRegistration,
  startRegistration,
} from '../lib/registrationsApi'
import type {
  CheckoutStart,
  RegistrationInput,
  RegistrationStatus,
  StudyYear,
  TeamMemberInput,
} from '../lib/registrationsApi'
import './RegistrationForm.css'

const studyYears: { value: StudyYear; label: string }[] = [
  { value: 'FIRST', label: '1st year' },
  { value: 'SECOND', label: '2nd year' },
  { value: 'THIRD', label: '3rd year' },
  { value: 'FOURTH', label: '4th year' },
]

interface RazorpayResult {
  razorpay_payment_id: string
  razorpay_order_id: string
  razorpay_signature: string
}

interface RazorpayInstance {
  open(): void
  on(event: 'payment.failed', callback: (response: { error?: { description?: string } }) => void): void
}

interface RazorpayOptions {
  key: string
  amount: number
  currency: string
  name: string
  description: string
  order_id: string
  prefill: { name?: string; email?: string; contact?: string }
  handler: (result: RazorpayResult) => void
  modal: { ondismiss: () => void }
}

declare global {
  interface Window {
    Razorpay?: new (options: RazorpayOptions) => RazorpayInstance
  }
}

const checkoutScriptUrl = 'https://checkout.razorpay.com/v1/checkout.js'

function loadRazorpay(): Promise<void> {
  if (window.Razorpay) return Promise.resolve()
  return new Promise((resolve, reject) => {
    const existing = document.querySelector<HTMLScriptElement>(`script[src="${checkoutScriptUrl}"]`)
    if (existing) {
      existing.addEventListener('load', () => resolve(), { once: true })
      existing.addEventListener('error', () => reject(new Error('Payment checkout could not be loaded.')), { once: true })
      return
    }
    const script = document.createElement('script')
    script.src = checkoutScriptUrl
    script.async = true
    script.onload = () => resolve()
    script.onerror = () => reject(new Error('Payment checkout could not be loaded.'))
    document.body.appendChild(script)
  })
}

function newMember(): TeamMemberInput {
  return { name: '', year: 'FIRST', department: '', institution: '', email: '' }
}

function displayStatus(status: RegistrationStatus) {
  switch (status) {
    case 'PENDING_PAYMENT': return 'Payment pending'
    case 'CONFIRMED': return 'Registration confirmed'
    case 'PAYMENT_FAILED': return 'Payment not confirmed'
    case 'EXPIRED': return 'Checkout expired'
    case 'PAYMENT_REVIEW': return 'Payment needs assistance'
  }
}

function RegistrationForm({ event }: { event: PublicEventRecord }) {
  const firstMembers = Math.max(0, (event.minimumTeamSize ?? 1) - 1)
  const idempotencyKey = useRef(crypto.randomUUID())
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [checkout, setCheckout] = useState<CheckoutStart | null>(null)
  const [checkoutMessage, setCheckoutMessage] = useState('')
  const [individual, setIndividual] = useState({
    name: '',
    phone: '',
    email: '',
    year: 'FIRST' as StudyYear,
    department: '',
    institution: '',
  })
  const [teamName, setTeamName] = useState('')
  const [captain, setCaptain] = useState({ name: '', phone: '', email: '' })
  const [members, setMembers] = useState<TeamMemberInput[]>(
    Array.from({ length: firstMembers }, newMember),
  )

  useEffect(() => {
    if (!checkout || checkout.status !== 'PENDING_PAYMENT') return
    let active = true
    const timer = window.setInterval(() => {
      fetchRegistrationStatus(checkout.registrationId)
        .then((status) => {
          if (!active || status.status === 'PENDING_PAYMENT') return
          setCheckout((current) => current ? {
            ...current,
            status: status.status,
            holdExpiresAt: status.holdExpiresAt,
            retryAllowed: status.retryAllowed,
          } : current)
        })
        .catch(() => {
          if (active) setCheckoutMessage('Payment status could not be refreshed yet. We will keep checking.')
        })
    }, 2500)
    return () => {
      active = false
      window.clearInterval(timer)
    }
  }, [checkout])

  async function openCheckout(response: CheckoutStart, prefill: RazorpayOptions['prefill']) {
    if (!response.razorpayKeyId || !response.razorpayOrderId || !response.amountPaise || !response.currency) {
      setCheckoutMessage(response.message || 'Checkout is being prepared. Please wait a moment and retry.')
      return
    }
    await loadRazorpay()
    if (!window.Razorpay) throw new Error('Payment checkout is not available in this browser.')
    setCheckoutMessage('')
    const instance = new window.Razorpay({
      key: response.razorpayKeyId,
      amount: response.amountPaise,
      currency: response.currency,
      name: 'Soaring Eagles',
      description: event.title,
      order_id: response.razorpayOrderId,
      prefill,
      handler: () => {
        setCheckoutMessage('Payment submitted. Waiting for the server to verify it; do not close this page yet.')
      },
      modal: {
        ondismiss: () => setCheckoutMessage('Checkout was closed. Your registration is not confirmed.'),
      },
    })
    instance.on('payment.failed', (result) => {
      setCheckoutMessage(result.error?.description || 'Payment was not confirmed. You can retry while registration remains open.')
    })
    instance.open()
  }

  async function handleSubmit(formEvent: FormEvent<HTMLFormElement>) {
    formEvent.preventDefault()
    setError('')
    setIsSubmitting(true)
    const input: RegistrationInput = event.registrationType === 'TEAM'
      ? {
          teamName,
          captainName: captain.name,
          captainPhone: captain.phone,
          captainEmail: captain.email,
          members,
        }
      : individual

    try {
      const response = await startRegistration(event.id, idempotencyKey.current, input)
      setCheckout(response)
      if (response.status === 'PENDING_PAYMENT' && response.razorpayOrderId) {
        await openCheckout(response, event.registrationType === 'TEAM'
          ? { name: captain.name, email: captain.email, contact: captain.phone }
          : { name: individual.name, email: individual.email, contact: individual.phone })
      } else {
        setCheckoutMessage(response.message || displayStatus(response.status))
      }
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Registration could not be started.')
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleRetry() {
    if (!checkout) return
    setError('')
    setIsSubmitting(true)
    try {
      const response = await retryRegistration(checkout.registrationId)
      setCheckout(response)
      await openCheckout(response, event.registrationType === 'TEAM'
        ? { name: captain.name, email: captain.email, contact: captain.phone }
        : { name: individual.name, email: individual.email, contact: individual.phone })
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Payment retry could not be started.')
    } finally {
      setIsSubmitting(false)
    }
  }

  function updateMember(index: number, field: keyof TeamMemberInput, value: string) {
    setMembers((current) => current.map((member, memberIndex) =>
      memberIndex === index ? { ...member, [field]: value } : member))
  }

  const canRegister = event.registrationWindowOpen && (event.remainingCapacity ?? 0) > 0

  return (
    <section className="registration-panel" aria-labelledby="registration-heading">
      <div className="registration-panel-heading">
        <p className="events-section-label">ONLINE REGISTRATION</p>
        <h2 id="registration-heading">Join this <em>event.</em></h2>
        <p>
          {event.remainingCapacity} {event.registrationType === 'TEAM' ? 'team places' : 'individual places'} remain.
          {event.feeInr > 0 ? ' A 10-minute capacity hold starts when checkout begins.' : ' This event is free.'}
        </p>
      </div>

      {checkout ? (
        <div className="registration-status" aria-live="polite">
          <strong>{displayStatus(checkout.status)}</strong>
          <p>
            {checkout.status === 'CONFIRMED'
              ? 'Your payment was verified and your registration is confirmed.'
              : checkout.status === 'PAYMENT_REVIEW'
                ? 'We received a payment notification that needs manual review. Contact the club and include your registration reference.'
                : checkout.status === 'EXPIRED'
                  ? 'The payment window expired and your place was released.'
                  : checkout.status === 'PAYMENT_FAILED'
                    ? 'No confirmed registration exists yet. Your details are retained so you can retry.'
                    : `Your place is held until ${checkout.holdExpiresAt ? new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium', timeStyle: 'short', timeZone: 'Asia/Kolkata' }).format(new Date(checkout.holdExpiresAt)) : 'checkout completes'}.`}
          </p>
          {checkoutMessage && <p role="status">{checkoutMessage}</p>}
          {error && <p className="registration-error" role="alert">{error}</p>}
          {checkout.status === 'PENDING_PAYMENT' && checkout.razorpayOrderId && (
            <button type="button" className="registration-submit" disabled={isSubmitting} onClick={() => void openCheckout(
              checkout,
              event.registrationType === 'TEAM'
                ? { name: captain.name, email: captain.email, contact: captain.phone }
                : { name: individual.name, email: individual.email, contact: individual.phone },
            )}>
              Continue to payment
            </button>
          )}
          {checkout.retryAllowed && canRegister && (
            <button type="button" className="registration-submit" disabled={isSubmitting} onClick={() => void handleRetry()}>
              {isSubmitting ? 'Preparing checkout…' : 'Retry payment'}
            </button>
          )}
          <p className="registration-reference">Reference: {checkout.registrationId}</p>
        </div>
      ) : (
        <form className="registration-form" onSubmit={(formEvent) => void handleSubmit(formEvent)}>
          {event.registrationType === 'TEAM' ? (
            <>
              <label>
                Team name
                <input required maxLength={120} value={teamName} onChange={(e) => setTeamName(e.target.value)} />
              </label>
              <fieldset>
                <legend>Captain details</legend>
                <label>Full name<input required maxLength={120} value={captain.name} onChange={(e) => setCaptain({ ...captain, name: e.target.value })} /></label>
                <label>Phone<input required type="tel" maxLength={30} value={captain.phone} onChange={(e) => setCaptain({ ...captain, phone: e.target.value })} /></label>
                <label>Email<input required type="email" maxLength={254} value={captain.email} onChange={(e) => setCaptain({ ...captain, email: e.target.value })} /></label>
              </fieldset>
              <fieldset>
                <legend>Other team members (captain is included in team size)</legend>
                <p className="registration-hint">
                  Team size: {event.minimumTeamSize}–{event.maximumTeamSize}, including the captain.
                </p>
                {members.map((member, index) => (
                  <div className="registration-member" key={index}>
                    <h3>Member {index + 1}</h3>
                    <label>Full name<input required maxLength={120} value={member.name} onChange={(e) => updateMember(index, 'name', e.target.value)} /></label>
                    <label>Study year
                      <select value={member.year} onChange={(e) => updateMember(index, 'year', e.target.value)}>
                        {studyYears.map((year) => <option value={year.value} key={year.value}>{year.label}</option>)}
                      </select>
                    </label>
                    <label>Department<input required maxLength={160} value={member.department} onChange={(e) => updateMember(index, 'department', e.target.value)} /></label>
                    <label>Institution<input required maxLength={200} value={member.institution} onChange={(e) => updateMember(index, 'institution', e.target.value)} /></label>
                    <label>Email<input required type="email" maxLength={254} value={member.email} onChange={(e) => updateMember(index, 'email', e.target.value)} /></label>
                    {members.length + 1 > (event.minimumTeamSize ?? 1) && (
                      <button type="button" className="registration-remove" onClick={() => setMembers((current) => current.filter((_, memberIndex) => memberIndex !== index))}>
                        Remove member
                      </button>
                    )}
                  </div>
                ))}
                {members.length + 1 < (event.maximumTeamSize ?? 1) && (
                  <button type="button" className="registration-add" onClick={() => setMembers((current) => [...current, newMember()])}>
                    + Add team member
                  </button>
                )}
              </fieldset>
            </>
          ) : (
            <>
              <label>Full name<input required maxLength={120} value={individual.name} onChange={(e) => setIndividual({ ...individual, name: e.target.value })} /></label>
              <label>Phone<input required type="tel" maxLength={30} value={individual.phone} onChange={(e) => setIndividual({ ...individual, phone: e.target.value })} /></label>
              <label>Email<input required type="email" maxLength={254} value={individual.email} onChange={(e) => setIndividual({ ...individual, email: e.target.value })} /></label>
              <label>Study year
                <select value={individual.year} onChange={(e) => setIndividual({ ...individual, year: e.target.value as StudyYear })}>
                  {studyYears.map((year) => <option value={year.value} key={year.value}>{year.label}</option>)}
                </select>
              </label>
              <label>Department<input required maxLength={160} value={individual.department} onChange={(e) => setIndividual({ ...individual, department: e.target.value })} /></label>
              <label>Institution<input required maxLength={200} value={individual.institution} onChange={(e) => setIndividual({ ...individual, institution: e.target.value })} /></label>
            </>
          )}
          <p className="registration-hint">
            Contact and participant details are shared only with authorized event admins and the payment provider.
          </p>
          {error && <p className="registration-error" role="alert">{error}</p>}
          <button type="submit" className="registration-submit" disabled={isSubmitting || !canRegister}>
            {isSubmitting ? 'Starting registration…' : event.feeInr > 0
              ? `Continue to payment · ₹${Number(event.feeInr).toLocaleString('en-IN')}`
              : 'Register for free'}
          </button>
        </form>
      )}
    </section>
  )
}

export default RegistrationForm
