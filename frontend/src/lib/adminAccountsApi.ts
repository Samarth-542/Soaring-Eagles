import { fetchCsrfToken } from './adminAuth'

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/+$/, '')
const accountsPath = '/api/v1/admin/accounts'

export interface AdminAccount {
  id: string
  email: string
  enabled: boolean
  createdAt: string
}

async function responseError(response: Response) {
  let detail = ''
  try {
    detail = ((await response.json()) as { detail?: string }).detail ?? ''
  } catch {
    // Keep a useful fallback when the API does not return problem details.
  }
  return new Error(detail || `Admin account request failed (${response.status}).`)
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${apiBaseUrl}${path}`, {
    credentials: 'include',
    cache: 'no-store',
    ...init,
  })
  if (!response.ok) throw await responseError(response)
  return (await response.json()) as T
}

export function fetchAdminAccounts() {
  return request<AdminAccount[]>(accountsPath)
}

export async function createAdminAccount(email: string, password: string) {
  const csrfToken = await fetchCsrfToken()
  return request<AdminAccount>(accountsPath, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': csrfToken,
    },
    body: JSON.stringify({ email, password }),
  })
}

export async function setAdminAccountEnabled(id: string, enabled: boolean) {
  const csrfToken = await fetchCsrfToken()
  return request<AdminAccount>(`${accountsPath}/${encodeURIComponent(id)}/enabled`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': csrfToken,
    },
    body: JSON.stringify({ enabled }),
  })
}
