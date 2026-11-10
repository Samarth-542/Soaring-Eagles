import { fetchPublicJson } from './publicApi'
import { loadPublicData } from './publicDataCache'

export type PublicSnapshotName = 'events.json' | 'team.json' | 'projects.json' | 'journey.json' | 'site-details.json'

export interface SnapshotDocument<T> {
  version: string
  generatedAt: string
  data: T
}

export interface SnapshotManifest {
  version: string
  generatedAt: string
  snapshots: Record<PublicSnapshotName, { path: string; sha256: string }>
}

export interface PublicEventSnapshot {
  id: string
  title: string
  description: string
  startsAt: string
  endsAt: string
  venue: string
  posterUrl: string | null
  status: 'PUBLISHED' | 'CLOSED'
  archiveStory: string | null
  archiveResults: string | null
  archivePhotos: string[]
}

export interface PublicTeamMember {
  id: string
  groupId: string
  groupTitle: string
  groupLabel: string
  name: string
  role: string
  imageUrl: string
  linkedinUrl: string | null
  instagramUrl: string | null
  xUrl: string | null
  position: number
}

export interface PublicTeamDirectory {
  groups: {
    id: string
    title: string
    label: string
    members: PublicTeamMember[]
  }[]
}

export interface PublicProject {
  id: string
  title: string
  year: number
  description: string
  imageUrl: string
  tags: string[]
  position: number
}

export interface PublicJourneyMilestone {
  id: string
  year: number
  title: string
  description: string
  imageUrl: string
  position: number
}

export interface PublicSiteDetails {
  email: string
  linkedinUrl: string
  xUrl: string
  location: string
}

const publicContentBaseUrl = (import.meta.env.VITE_PUBLIC_CONTENT_BASE_URL ?? '').replace(/\/+$/, '')
const manifestTtlMs = 20_000
let manifestCache: { expiresAt: number; request: Promise<SnapshotManifest> } | null = null

export function invalidatePublicSnapshotManifest() {
  manifestCache = null
}

async function fetchManifest() {
  if (!publicContentBaseUrl) {
    throw new Error('Public content storage is not configured. Set VITE_PUBLIC_CONTENT_BASE_URL.')
  }
  if (manifestCache && Date.now() < manifestCache.expiresAt) return manifestCache.request

  const request = fetchPublicJson<SnapshotManifest>(`${publicContentBaseUrl}/manifest.json`)
    .catch((error: unknown) => {
      if (manifestCache?.request === request) manifestCache = null
      throw error
    })
  manifestCache = { expiresAt: Date.now() + manifestTtlMs, request }
  return request
}

export async function fetchPublicSnapshot<T>(name: PublicSnapshotName): Promise<SnapshotDocument<T>> {
  const manifest = await fetchManifest()
  const reference = manifest.snapshots?.[name]
  const expectedPath = `snapshots/${manifest.version}/${name}`
  if (!manifest.version || !manifest.generatedAt || !reference || reference.path !== expectedPath) {
    throw new Error(`The public content manifest does not contain a valid ${name} snapshot.`)
  }

  const document = await fetchPublicJson<SnapshotDocument<T>>(
    `${publicContentBaseUrl}/${reference.path}`,
    'force-cache',
  )
  if (document.version !== manifest.version || document.generatedAt !== manifest.generatedAt) {
    throw new Error(`The ${name} snapshot does not match the published manifest.`)
  }
  return document
}

export function fetchPublicEventSnapshots() {
  return fetchPublicSnapshot<PublicEventSnapshot[]>('events.json')
}

export async function fetchPublicEventSnapshot(id: string) {
  const snapshot = await loadPublicData('events', 30_000, fetchPublicEventSnapshots)
  const event = snapshot.data.find((item) => item.id === id)
  if (!event) throw new Error('This event could not be found or is not public.')
  return { ...snapshot, data: event }
}

export async function fetchPublicTeamSnapshot() {
  const snapshot = await fetchPublicSnapshot<PublicTeamDirectory>('team.json')
  if (!snapshot.data || !Array.isArray(snapshot.data.groups)) {
    throw new Error('The published team snapshot has an invalid directory format.')
  }
  return snapshot
}

export function fetchPublicProjectsSnapshot() {
  return fetchPublicSnapshot<PublicProject[]>('projects.json')
}

export function fetchPublicJourneySnapshot() {
  return fetchPublicSnapshot<PublicJourneyMilestone[]>('journey.json')
}

export function fetchPublicSiteDetailsSnapshot() {
  return fetchPublicSnapshot<PublicSiteDetails>('site-details.json')
}
