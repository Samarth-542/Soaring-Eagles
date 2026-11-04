const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/+$/, '')
const authPath = '/api/v1/auth'

export class AdminApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
  ) {
    super(message)
    this.name = 'AdminApiError'
  }
}

interface ProblemResponse {
  detail?: string
}

interface CsrfResponse {
  token?: string
}

interface AdminSession {
  email: string
}

function endpoint(path: string) {
  return `${apiBaseUrl}${path}`
}

async function responseError(response: Response) {
  let detail = ''
  try {
    const problem = (await response.json()) as ProblemResponse
    detail = problem.detail ?? ''
  } catch {
    // Keep errors safe and useful if the API did not return problem details.
  }
  return new AdminApiError(detail || 'The authentication request could not be completed.', response.status)
}

export async function fetchCsrfToken() {
  const response = await fetch(endpoint(`${authPath}/csrf`), {
    credentials: 'include',
    cache: 'no-store',
  })
  if (!response.ok) throw await responseError(response)

  const body = (await response.json()) as CsrfResponse
  if (!body.token) throw new Error('The server did not provide a CSRF token.')
  return body.token
}

export async function loginAdmin(email: string, password: string, csrfToken: string) {
  const body = new URLSearchParams({ email, password })
  const response = await fetch(endpoint(`${authPath}/login`), {
    method: 'POST',
    credentials: 'include',
    cache: 'no-store',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
      'X-XSRF-TOKEN': csrfToken,
    },
    body,
  })
  if (!response.ok) throw await responseError(response)
}

export async function fetchAdminSession(): Promise<AdminSession> {
  const response = await fetch(endpoint(`${authPath}/session`), {
    credentials: 'include',
    cache: 'no-store',
  })
  if (!response.ok) throw await responseError(response)
  return (await response.json()) as AdminSession
}

export async function logoutAdmin() {
  const csrfToken = await fetchCsrfToken()
  const response = await fetch(endpoint(`${authPath}/logout`), {
    method: 'POST',
    credentials: 'include',
    cache: 'no-store',
    headers: { 'X-XSRF-TOKEN': csrfToken },
  })
  if (!response.ok) throw await responseError(response)
}
