export type PublicDataKey =
  | 'events'
  | `event:${string}`
  | 'team'
  | 'projects'
  | 'journey'
  | 'site-details'

interface CacheRecord {
  savedAt: number
  data: unknown
}

const storagePrefix = 'soaring-eagles:public-data:'
const memoryCache = new Map<PublicDataKey, CacheRecord>()
const inFlightRequests = new Map<PublicDataKey, Promise<unknown>>()
const revisions = new Map<PublicDataKey, number>()
let storageWarningShown = false

function isStorageError(error: unknown) {
  return error instanceof Error
    && ['SecurityError', 'QuotaExceededError', 'NS_ERROR_DOM_QUOTA_REACHED'].includes(error.name)
}

function warnStorageUnavailable(error: unknown) {
  if (storageWarningShown) return
  storageWarningShown = true
  console.warn('Public data will use in-memory caching only because session storage is unavailable.', error)
}

function getSessionStorage(): Storage | null {
  if (typeof window === 'undefined') return null

  try {
    return window.sessionStorage
  } catch (error) {
    if (!isStorageError(error)) throw error
    warnStorageUnavailable(error)
    return null
  }
}

export function readPublicDataCache<T>(key: PublicDataKey): { data: T; savedAt: number } | null {
  const cached = memoryCache.get(key)
  if (cached) return cached as { data: T; savedAt: number }

  const storage = getSessionStorage()
  if (!storage) return null

  try {
    const serialized = storage.getItem(`${storagePrefix}${key}`)
    if (!serialized) return null

    const parsed: unknown = JSON.parse(serialized)
    if (
      typeof parsed !== 'object'
      || parsed === null
      || !('savedAt' in parsed)
      || typeof parsed.savedAt !== 'number'
      || !('data' in parsed)
    ) {
      storage.removeItem(`${storagePrefix}${key}`)
      return null
    }

    const record = parsed as CacheRecord
    memoryCache.set(key, record)
    return record as { data: T; savedAt: number }
  } catch (error) {
    if (error instanceof SyntaxError) {
      storage.removeItem(`${storagePrefix}${key}`)
      console.warn(`Discarded invalid cached public data for "${key}".`, error)
      return null
    }
    if (!isStorageError(error)) throw error
    warnStorageUnavailable(error)
    return null
  }
}

export function isPublicDataCacheFresh(key: PublicDataKey, maxAgeMs: number) {
  const cached = readPublicDataCache(key)
  return cached !== null && Date.now() - cached.savedAt < maxAgeMs
}

function writePublicDataCache(key: PublicDataKey, data: unknown) {
  const record: CacheRecord = { data, savedAt: Date.now() }
  memoryCache.set(key, record)

  const storage = getSessionStorage()
  if (!storage) return

  try {
    storage.setItem(`${storagePrefix}${key}`, JSON.stringify(record))
  } catch (error) {
    if (!isStorageError(error)) throw error
    warnStorageUnavailable(error)
  }
}

function invalidateKey(key: PublicDataKey) {
  revisions.set(key, (revisions.get(key) ?? 0) + 1)
  memoryCache.delete(key)
  inFlightRequests.delete(key)

  const storage = getSessionStorage()
  if (!storage) return

  try {
    storage.removeItem(`${storagePrefix}${key}`)
  } catch (error) {
    if (!isStorageError(error)) throw error
    warnStorageUnavailable(error)
  }
}

export function invalidatePublicDataCache(key: PublicDataKey) {
  invalidateKey(key)
}

export function invalidatePublicEventDataCache() {
  const keys = new Set<PublicDataKey>(['events'])
  for (const key of memoryCache.keys()) {
    if (key.startsWith('event:')) keys.add(key)
  }

  const storage = getSessionStorage()
  if (storage) {
    try {
      for (let index = storage.length - 1; index >= 0; index -= 1) {
        const storageKey = storage.key(index)
        if (!storageKey?.startsWith(`${storagePrefix}event:`)) continue
        keys.add(storageKey.slice(storagePrefix.length) as PublicDataKey)
      }
    } catch (error) {
      if (!isStorageError(error)) throw error
      warnStorageUnavailable(error)
    }
  }

  for (const key of keys) invalidateKey(key)
}

export function loadPublicData<T>(
  key: PublicDataKey,
  maxAgeMs: number,
  fetcher: () => Promise<T>,
  forceRefresh = false,
): Promise<T> {
  const cached = readPublicDataCache<T>(key)
  if (!forceRefresh && cached && Date.now() - cached.savedAt < maxAgeMs) {
    return Promise.resolve(cached.data)
  }

  const existingRequest = inFlightRequests.get(key)
  if (existingRequest) return existingRequest as Promise<T>

  const revision = revisions.get(key) ?? 0
  const request = fetcher()
    .then((data) => {
      if ((revisions.get(key) ?? 0) === revision) writePublicDataCache(key, data)
      return data
    })
    .finally(() => {
      if (inFlightRequests.get(key) === request) inFlightRequests.delete(key)
    })

  inFlightRequests.set(key, request)
  return request
}
