export class PublicApiError extends Error {
  constructor(message: string, readonly status: number) {
    super(message)
    this.name = 'PublicApiError'
  }
}

const requestTimeoutMs = 45_000
const retryDelayMs = 500

function isRetryable(error: unknown) {
  return error instanceof TypeError
    || (error instanceof DOMException && error.name === 'AbortError')
    || (error instanceof PublicApiError && error.status >= 500)
}

async function responseError(response: Response) {
  let detail = ''
  try {
    const problem = (await response.json()) as { detail?: string }
    detail = problem.detail ?? ''
  } catch (error) {
    if (!(error instanceof SyntaxError) && !(error instanceof TypeError)) throw error
  }
  return new PublicApiError(detail || `Public content request failed (${response.status}).`, response.status)
}

export async function fetchPublicJson<T>(url: string, cache: RequestCache = 'no-cache'): Promise<T> {
  for (let attempt = 0; attempt < 2; attempt += 1) {
    const controller = new AbortController()
    const timeout = window.setTimeout(() => controller.abort(), requestTimeoutMs)

    try {
      const response = await fetch(url, {
        cache,
        credentials: 'omit',
        signal: controller.signal,
      })
      if (!response.ok) throw await responseError(response)
      return (await response.json()) as T
    } catch (error) {
      if (attempt === 1 || !isRetryable(error)) throw error
      await new Promise((resolve) => window.setTimeout(resolve, retryDelayMs))
    } finally {
      window.clearTimeout(timeout)
    }
  }

  throw new Error('Public content request did not complete.')
}
