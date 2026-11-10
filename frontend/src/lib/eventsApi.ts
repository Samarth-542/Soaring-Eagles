import { fetchCsrfToken } from './adminAuth'
import { fetchPublicJson } from './publicApi'
import { invalidatePublicEventDataCache } from './publicDataCache'
import { fetchPublicEventSnapshots, invalidatePublicSnapshotManifest } from './publicContentApi'

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/+$/, '')
const publicEventsPath = '/api/v1/events'
const adminEventsPath = '/api/v1/admin/events'

export type EventStatus = 'DRAFT' | 'SCHEDULED' | 'PUBLISHED' | 'CLOSED'
export type RegistrationType = 'INDIVIDUAL' | 'TEAM'
export type PaymentMethod = 'RAZORPAY' | 'GOOGLE_FORM'

export interface EventRecord {
  id: string
  title: string
  description: string
  startsAt: string
  endsAt: string
  venue: string
  posterUrl: string | null
  eligibility: string
  feeInr: number
  registrationType: RegistrationType
  paymentMethod: PaymentMethod
  googleFormLink: string | null
  googleFormQrUrl: string | null
  registrationDeadline: string
  capacity: number
  remainingCapacity: number | null
  minimumTeamSize: number | null
  maximumTeamSize: number | null
  status: EventStatus
  scheduledPublishAt: string | null
  ended: boolean
  registrationWindowOpen: boolean
  onlineRegistrationAvailable: boolean
  archiveStory: string | null
  archiveResults: string | null
  archivePhotos: string[]
  createdAt: string
  updatedAt: string
}

export type PublicEventRecord = Omit<EventRecord, 'scheduledPublishAt' | 'createdAt' | 'updatedAt'>

export interface EventInput {
  title: string
  description: string
  startsAt: string
  endsAt: string
  venue: string
  posterUrl: string | null
  eligibility: string
  feeInr: number
  registrationType: RegistrationType
  paymentMethod: PaymentMethod
  googleFormLink: string | null
  googleFormQrUrl: string | null
  registrationDeadline: string
  capacity: number
  minimumTeamSize: number | null
  maximumTeamSize: number | null
  archiveStory: string | null
  archiveResults: string | null
  archivePhotos: string[]
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${apiBaseUrl}${path}`, {
    credentials: 'include',
    cache: 'no-store',
    ...init,
  })

  if (!response.ok) {
    let detail = ''
    try {
      const problem = (await response.json()) as { detail?: string }
      detail = problem.detail ?? ''
    } catch {
      // Use a stable message when the server does not return a problem document.
    }
    throw new Error(detail || `Event request failed (${response.status}).`)
  }

  if (response.status === 204) return undefined as T
  return (await response.json()) as T
}

async function mutate<T>(
  path: string,
  method: 'POST' | 'PUT' | 'DELETE',
  body?: unknown,
): Promise<T> {
  const csrfToken = await fetchCsrfToken()
  const result = await request<T>(path, {
    method,
    headers: {
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': csrfToken,
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  })
  invalidatePublicEventDataCache()
  invalidatePublicSnapshotManifest()
  return result
}

export function fetchPublicEvents() {
  return fetchPublicEventSnapshots()
}

export function fetchPublicEvent(id: string) {
  return fetchPublicJson<PublicEventRecord>(`${apiBaseUrl}${publicEventsPath}/${encodeURIComponent(id)}`)
}

export function fetchAdminEvents() {
  return request<EventRecord[]>(adminEventsPath)
}

export function createEvent(input: EventInput) {
  return mutate<EventRecord>(adminEventsPath, 'POST', input)
}

export function updateEvent(id: string, input: EventInput) {
  return mutate<EventRecord>(`${adminEventsPath}/${encodeURIComponent(id)}`, 'PUT', input)
}

export function scheduleEvent(id: string, publishAt: string) {
  return mutate<EventRecord>(`${adminEventsPath}/${encodeURIComponent(id)}/schedule`, 'POST', { publishAt })
}

export function publishEvent(id: string) {
  return mutate<EventRecord>(`${adminEventsPath}/${encodeURIComponent(id)}/publish`, 'POST')
}

export function closeEvent(id: string) {
  return mutate<EventRecord>(`${adminEventsPath}/${encodeURIComponent(id)}/close`, 'POST')
}

export function deleteEvent(id: string) {
  return mutate<void>(`${adminEventsPath}/${encodeURIComponent(id)}`, 'DELETE')
}
