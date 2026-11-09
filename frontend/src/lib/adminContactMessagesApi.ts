import { fetchCsrfToken } from './adminAuth'

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/+$/, '')
const contactMessagesPath = '/api/v1/admin/contact-messages'

export type ContactMessageState = 'NEW' | 'IN_PROGRESS' | 'ARCHIVED'

export interface AdminContactMessageListItem {
  id: string
  senderName: string
  senderEmail: string
  subject: string
  state: ContactMessageState
  attachmentFileName: string | null
  createdAt: string
}

export interface AdminContactMessage extends AdminContactMessageListItem {
  message: string
  attachmentContentType: string | null
  updatedAt: string
}

export interface AdminContactMessagePage {
  content: AdminContactMessageListItem[]
  totalElements: number
  totalPages: number
  number: number
  size: number
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
    throw new Error(detail || `Contact inbox request failed (${response.status}).`)
  }
  if (response.status === 204) return undefined as T
  return (await response.json()) as T
}

export function fetchAdminContactMessages(state: ContactMessageState | 'ALL', page = 0) {
  const params = new URLSearchParams({ page: String(page), size: '50' })
  if (state !== 'ALL') params.set('state', state)
  return request<AdminContactMessagePage>(`${contactMessagesPath}?${params}`)
}

export function fetchAdminContactMessage(id: string) {
  return request<AdminContactMessage>(`${contactMessagesPath}/${encodeURIComponent(id)}`)
}

export async function updateContactMessageState(id: string, state: ContactMessageState) {
  const csrfToken = await fetchCsrfToken()
  return request<AdminContactMessage>(`${contactMessagesPath}/${encodeURIComponent(id)}/state`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': csrfToken,
    },
    body: JSON.stringify({ state }),
  })
}

export async function downloadContactAttachment(id: string, fileName: string) {
  const response = await fetch(
    `${apiBaseUrl}${contactMessagesPath}/${encodeURIComponent(id)}/attachment`,
    { credentials: 'include', cache: 'no-store' },
  )
  if (!response.ok) {
    let detail = ''
    try {
      detail = ((await response.json()) as { detail?: string }).detail ?? ''
    } catch {
      // Keep errors safe and useful if the API did not return problem details.
    }
    throw new Error(detail || `Attachment download failed (${response.status}).`)
  }

  const downloadUrl = URL.createObjectURL(await response.blob())
  const link = document.createElement('a')
  link.href = downloadUrl
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(downloadUrl)
}
