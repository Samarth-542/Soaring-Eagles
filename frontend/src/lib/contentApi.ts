import { fetchCsrfToken } from './adminAuth'

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/+$/, '')
const contentPath = '/api/v1/content'
const adminContentPath = '/api/v1/admin/content'

export interface TeamGroup {
  id: string
  title: string
  label: string
  members: TeamMember[]
}

export interface TeamMember {
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
  published: boolean
}

export interface TeamDirectory {
  groups: TeamGroup[]
}

export interface TeamAdminDirectory {
  groups: Omit<TeamGroup, 'members'>[]
  members: TeamMember[]
}

export interface TeamMemberInput {
  groupId: string
  name: string
  role: string
  imageUrl: string
  linkedinUrl: string | null
  instagramUrl: string | null
  xUrl: string | null
  position: number
  published: boolean
}

export interface Project {
  id: string
  title: string
  year: number
  description: string
  imageUrl: string
  tags: string[]
  position: number
  published: boolean
}

export type ProjectInput = Omit<Project, 'id'>

export interface JourneyMilestone {
  id: string
  year: number
  title: string
  description: string
  imageUrl: string
  position: number
  published: boolean
}

export type JourneyMilestoneInput = Omit<JourneyMilestone, 'id'>

export interface UploadedImage {
  publicUrl: string
  storageKey: string
  contentType: string
  sizeBytes: number
}

export interface SiteDetails {
  email: string
  linkedinUrl: string
  xUrl: string
  location: string
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${apiBaseUrl}${path}`, {
    cache: 'no-store',
    credentials: 'include',
    ...init,
  })

  if (!response.ok) {
    let detail = ''
    try {
      detail = ((await response.json()) as { detail?: string }).detail ?? ''
    } catch {
      // Retain a stable fallback if the API did not return problem details.
    }
    throw new Error(detail || `Content request failed (${response.status}).`)
  }

  if (response.status === 204) return undefined as T
  return (await response.json()) as T
}

async function mutate<T>(path: string, method: 'POST' | 'PUT' | 'DELETE', body?: unknown) {
  const csrfToken = await fetchCsrfToken()
  return request<T>(path, {
    method,
    headers: body === undefined ? { 'X-XSRF-TOKEN': csrfToken } : {
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': csrfToken,
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  })
}

export async function uploadContentImage(file: File, section: 'team' | 'projects' | 'journey' | 'events') {
  const csrfToken = await fetchCsrfToken()
  const body = new FormData()
  body.set('file', file)
  body.set('section', section)
  const response = await fetch(`${apiBaseUrl}${adminContentPath}/images`, {
    method: 'POST',
    credentials: 'include',
    cache: 'no-store',
    headers: { 'X-XSRF-TOKEN': csrfToken },
    body,
  })

  if (!response.ok) {
    let detail = ''
    try {
      detail = ((await response.json()) as { detail?: string }).detail ?? ''
    } catch {
      // Use a stable fallback if the server did not return problem details.
    }
    throw new Error(detail || `Image upload failed (${response.status}).`)
  }

  return (await response.json()) as UploadedImage
}

export function fetchPublicTeam() {
  return request<TeamDirectory>(`${contentPath}/team`, { credentials: 'omit' })
}

export function fetchPublicProjects() {
  return request<Project[]>(`${contentPath}/projects`, { credentials: 'omit' })
}

export function fetchPublicJourney() {
  return request<JourneyMilestone[]>(`${contentPath}/journey`, { credentials: 'omit' })
}

export function fetchPublicSiteDetails() {
  return request<SiteDetails>(`${contentPath}/site-details`, { credentials: 'omit' })
}

export function fetchAdminSiteDetails() {
  return request<SiteDetails>('/api/v1/admin/site-details')
}

export function saveSiteDetails(details: SiteDetails) {
  return mutate<SiteDetails>('/api/v1/admin/site-details', 'PUT', details)
}

export function fetchAdminTeam() {
  return request<TeamAdminDirectory>(`${adminContentPath}/team`)
}

export function saveTeamMember(input: TeamMemberInput, id?: string) {
  const path = id ? `${adminContentPath}/team/${encodeURIComponent(id)}` : `${adminContentPath}/team`
  return mutate<TeamMember>(path, id ? 'PUT' : 'POST', input)
}

export function deleteTeamMember(id: string) {
  return mutate<void>(`${adminContentPath}/team/${encodeURIComponent(id)}`, 'DELETE')
}

export function fetchAdminProjects() {
  return request<Project[]>(`${adminContentPath}/projects`)
}

export function saveProject(input: ProjectInput, id?: string) {
  const path = id ? `${adminContentPath}/projects/${encodeURIComponent(id)}` : `${adminContentPath}/projects`
  return mutate<Project>(path, id ? 'PUT' : 'POST', input)
}

export function deleteProject(id: string) {
  return mutate<void>(`${adminContentPath}/projects/${encodeURIComponent(id)}`, 'DELETE')
}

export function fetchAdminJourney() {
  return request<JourneyMilestone[]>(`${adminContentPath}/journey`)
}

export function saveJourneyMilestone(input: JourneyMilestoneInput, id?: string) {
  const path = id ? `${adminContentPath}/journey/${encodeURIComponent(id)}` : `${adminContentPath}/journey`
  return mutate<JourneyMilestone>(path, id ? 'PUT' : 'POST', input)
}

export function deleteJourneyMilestone(id: string) {
  return mutate<void>(`${adminContentPath}/journey/${encodeURIComponent(id)}`, 'DELETE')
}
