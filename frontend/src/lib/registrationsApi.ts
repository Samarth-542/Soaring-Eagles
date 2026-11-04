import { fetchCsrfToken } from './adminAuth'

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/+$/, '')

export type RegistrationStatus =
  | 'PENDING_PAYMENT'
  | 'CONFIRMED'
  | 'PAYMENT_FAILED'
  | 'EXPIRED'
  | 'PAYMENT_REVIEW'

export type StudyYear = 'FIRST' | 'SECOND' | 'THIRD' | 'FOURTH'

export interface TeamMemberInput {
  name: string
  year: StudyYear
  department: string
  institution: string
  email: string
}

export interface RegistrationInput {
  name?: string
  phone?: string
  email?: string
  year?: StudyYear
  department?: string
  institution?: string
  teamName?: string
  captainName?: string
  captainPhone?: string
  captainEmail?: string
  members?: TeamMemberInput[]
}

export interface CheckoutStart {
  registrationId: string
  status: RegistrationStatus
  holdExpiresAt: string | null
  feeInr: number
  paymentAttemptId: string | null
  razorpayOrderId: string | null
  razorpayKeyId: string | null
  amountPaise: number | null
  currency: string | null
  retryAllowed: boolean
  message: string | null
}

export interface RegistrationStatusRecord {
  status: RegistrationStatus
  holdExpiresAt: string | null
  retryAllowed: boolean
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${apiBaseUrl}${path}`, {
    credentials: 'omit',
    cache: 'no-store',
    ...init,
  })
  if (!response.ok) {
    let detail = ''
    try {
      detail = ((await response.json()) as { detail?: string }).detail ?? ''
    } catch {
      // Keep the user-facing message useful when the API did not return problem details.
    }
    throw new Error(detail || `Registration request failed (${response.status}).`)
  }
  return (await response.json()) as T
}

async function post<T>(path: string, body?: unknown, idempotencyKey?: string) {
  const csrfToken = await fetchCsrfToken()
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    'X-XSRF-TOKEN': csrfToken,
  }
  if (idempotencyKey) headers['Idempotency-Key'] = idempotencyKey
  return request<T>(path, {
    method: 'POST',
    credentials: 'include',
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  })
}

export function startRegistration(eventId: string, idempotencyKey: string, input: RegistrationInput) {
  return post<CheckoutStart>(
    `/api/v1/events/${encodeURIComponent(eventId)}/registrations`,
    input,
    idempotencyKey,
  )
}

export function retryRegistration(registrationId: string) {
  return post<CheckoutStart>(`/api/v1/registrations/${encodeURIComponent(registrationId)}/retry`)
}

export function fetchRegistrationStatus(registrationId: string) {
  return request<RegistrationStatusRecord>(
    `/api/v1/registrations/${encodeURIComponent(registrationId)}/status`,
  )
}
