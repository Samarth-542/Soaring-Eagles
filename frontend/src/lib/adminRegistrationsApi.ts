import { fetchCsrfToken } from './adminAuth'
import type { RegistrationInput } from './registrationsApi'
import type { RegistrationStatus, StudyYear } from './registrationsApi'
import type { RegistrationType } from './eventsApi'

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/+$/, '')
const adminRegistrationsPath = '/api/v1/admin'

export interface AdminRegistrationMember {
  name: string
  year: number
  department: string
  institution: string
  email: string
}

export interface AdminRegistrationRecord {
  id: string
  eventId: string
  eventTitle: string
  registrationType: RegistrationType
  status: RegistrationStatus
  feeInr: number
  individualName: string | null
  individualPhone: string | null
  individualEmail: string | null
  individualYear: number | null
  individualDepartment: string | null
  individualInstitution: string | null
  teamName: string | null
  captainName: string | null
  captainPhone: string | null
  captainEmail: string | null
  paymentProofUrl: string | null
  members: AdminRegistrationMember[]
  paymentStatus: 'CREATING' | 'READY' | 'FAILED' | 'CAPTURED' | null
  emailDeliveryStatus: 'PENDING' | 'SENDING' | 'DELIVERED' | 'FAILED' | null
  emailDeliveryAttempts: number
  emailDeliveryError: string | null
  razorpayOrderId: string | null
  razorpayPaymentId: string | null
  holdExpiresAt: string | null
  createdAt: string
  updatedAt: string
}

export interface AdminRegistrationPage {
  content: AdminRegistrationRecord[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

const studyYears: StudyYear[] = ['FIRST', 'SECOND', 'THIRD', 'FOURTH']

function toStudyYear(year: number | null): StudyYear {
  return year !== null && Number.isInteger(year) && year >= 1 && year <= studyYears.length
    ? studyYears[year - 1] ?? 'FIRST'
    : 'FIRST'
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
      detail = ((await response.json()) as { detail?: string }).detail ?? ''
    } catch {
      // Use a stable error if the server response is not a problem document.
    }
    throw new Error(detail || `Registration request failed (${response.status}).`)
  }
  if (response.status === 204) return undefined as T
  return (await response.json()) as T
}

async function mutate<T>(path: string, method: 'PUT' | 'POST', body?: unknown) {
  const csrfToken = await fetchCsrfToken()
  return request<T>(path, {
    method,
    headers: {
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': csrfToken,
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  })
}

export function fetchAdminRegistrations(eventId: string, page = 0) {
  return request<AdminRegistrationPage>(
    `${adminRegistrationsPath}/events/${encodeURIComponent(eventId)}/registrations?page=${page}&size=50`,
  )
}

export function updateAdminRegistration(registrationId: string, input: RegistrationInput) {
  return mutate<AdminRegistrationRecord>(
    `${adminRegistrationsPath}/registrations/${encodeURIComponent(registrationId)}`,
    'PUT',
    input,
  )
}

export function retryRegistrationEmail(registrationId: string) {
  return mutate<void>(
    `${adminRegistrationsPath}/registrations/${encodeURIComponent(registrationId)}/email/retry`,
    'POST',
  )
}

export async function downloadRegistrationExport(eventId: string) {
  const response = await fetch(
    `${apiBaseUrl}${adminRegistrationsPath}/events/${encodeURIComponent(eventId)}/registrations/export.csv`,
    { credentials: 'include', cache: 'no-store' },
  )
  if (!response.ok) {
    let detail = ''
    try {
      detail = ((await response.json()) as { detail?: string }).detail ?? ''
    } catch {
      // Use a stable message if the server response is not a problem document.
    }
    throw new Error(detail || `Registration export failed (${response.status}).`)
  }
  const downloadUrl = URL.createObjectURL(await response.blob())
  const link = document.createElement('a')
  link.href = downloadUrl
  link.download = `registrations-${eventId}.csv`
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(downloadUrl)
}

export function toRegistrationInput(record: AdminRegistrationRecord): RegistrationInput {
  if (record.registrationType === 'TEAM') {
    return {
      teamName: record.teamName ?? '',
      captainName: record.captainName ?? '',
      captainPhone: record.captainPhone ?? '',
      captainEmail: record.captainEmail ?? '',
      members: record.members.map((member) => ({
        name: member.name,
        year: toStudyYear(member.year),
        department: member.department,
        institution: member.institution,
        email: member.email,
      })),
    }
  }
  return {
    name: record.individualName ?? '',
    phone: record.individualPhone ?? '',
    email: record.individualEmail ?? '',
    year: toStudyYear(record.individualYear),
    department: record.individualDepartment ?? '',
    institution: record.individualInstitution ?? '',
  }
}
