import { useCallback, useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import {
  deleteJourneyMilestone,
  deleteProject,
  deleteTeamMember,
  fetchAdminJourney,
  fetchAdminProjects,
  fetchAdminTeam,
  saveJourneyMilestone,
  saveProject,
  saveTeamMember,
  uploadContentImage,
} from '../../lib/contentApi'
import type {
  JourneyMilestone,
  Project,
  TeamAdminDirectory,
  TeamMember,
} from '../../lib/contentApi'
import './ContentManagement.css'
import ImageUploadField from './ImageUploadField'

type ContentSection = 'profiles' | 'projects' | 'journey'
type ContentRecord = TeamMember | Project | JourneyMilestone

function recordTitle(record: ContentRecord) {
  return 'name' in record ? record.name : record.title
}

interface ContentForm {
  id: string | null
  groupId: string
  title: string
  role: string
  imageUrl: string
  description: string
  year: string
  tags: string
  linkedinUrl: string
  instagramUrl: string
  xUrl: string
  position: string
  published: boolean
}

const blankForm: ContentForm = {
  id: null,
  groupId: '',
  title: '',
  role: '',
  imageUrl: '',
  description: '',
  year: String(new Date().getFullYear()),
  tags: '',
  linkedinUrl: '',
  instagramUrl: '',
  xUrl: '',
  position: '0',
  published: false,
}

const sectionTitles: Record<ContentSection, string> = {
  profiles: 'Team profiles',
  projects: 'Projects',
  journey: 'Our journey',
}

function ContentManagement({ section }: { section: ContentSection }) {
  const [groups, setGroups] = useState<TeamAdminDirectory['groups']>([])
  const [team, setTeam] = useState<TeamMember[]>([])
  const [projects, setProjects] = useState<Project[]>([])
  const [milestones, setMilestones] = useState<JourneyMilestone[]>([])
  const [form, setForm] = useState<ContentForm>(blankForm)
  const [imageFiles, setImageFiles] = useState<File[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isSaving, setIsSaving] = useState(false)
  const [busyId, setBusyId] = useState('')
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  const loadContent = useCallback(async () => {
    setError('')
    try {
      if (section === 'profiles') {
        const directory = await fetchAdminTeam()
        setGroups(directory.groups)
        setTeam(directory.members)
        setForm((current) => current.groupId || !directory.groups[0]
          ? { ...current, position: String(Math.max(-1, ...directory.members.map((member) => member.position)) + 1) }
          : {
            ...current,
            groupId: directory.groups[0].id,
            position: String(Math.max(-1, ...directory.members.map((member) => member.position)) + 1),
          })
      } else if (section === 'projects') {
        const records = await fetchAdminProjects()
        setProjects(records)
        setForm((current) => ({
          ...current,
          position: String(Math.max(-1, ...records.map((record) => record.position)) + 1),
        }))
      } else {
        const records = await fetchAdminJourney()
        setMilestones(records)
        setForm((current) => ({
          ...current,
          position: String(Math.max(-1, ...records.map((record) => record.position)) + 1),
        }))
      }
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : `${sectionTitles[section]} could not be loaded.`)
    } finally {
      setIsLoading(false)
    }
  }, [section])

  useEffect(() => {
    setIsLoading(true)
    setForm(blankForm)
    void loadContent()
  }, [loadContent])

  const records: ContentRecord[] = section === 'profiles'
    ? team
    : section === 'projects' ? projects : milestones

  const resetForm = () => {
    setForm({ ...blankForm, groupId: groups[0]?.id ?? '' })
    setImageFiles([])
    setError('')
    setNotice('')
  }

  const editRecord = (record: ContentRecord) => {
    setImageFiles([])
    if (section === 'profiles') {
      const member = record as TeamMember
      setForm({
        ...blankForm,
        id: member.id,
        groupId: member.groupId,
        title: member.name,
        role: member.role,
        imageUrl: member.imageUrl,
        linkedinUrl: member.linkedinUrl ?? '',
        instagramUrl: member.instagramUrl ?? '',
        xUrl: member.xUrl ?? '',
        position: String(member.position),
        published: member.published,
      })
    } else if (section === 'projects') {
      const project = record as Project
      setForm({
        ...blankForm,
        id: project.id,
        title: project.title,
        imageUrl: project.imageUrl,
        description: project.description,
        year: String(project.year),
        tags: project.tags.join(', '),
        position: String(project.position),
        published: project.published,
      })
    } else {
      const milestone = record as JourneyMilestone
      setForm({
        ...blankForm,
        id: milestone.id,
        title: milestone.title,
        imageUrl: milestone.imageUrl,
        description: milestone.description,
        year: String(milestone.year),
        position: String(milestone.position),
        published: milestone.published,
      })
    }
    setError('')
    setNotice('')
  }

  const save = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setIsSaving(true)
    setError('')
    setNotice('')
    let imageUrl = form.imageUrl
    try {
      if (imageFiles[0]) {
        setNotice('Uploading image…')
        imageUrl = (await uploadContentImage(
          imageFiles[0],
          section === 'profiles' ? 'team' : section,
        )).publicUrl
      }
      setNotice('Saving content…')
      if (section === 'profiles') {
        await saveTeamMember({
          groupId: form.groupId,
          name: form.title.trim(),
          role: form.role.trim(),
          imageUrl,
          linkedinUrl: form.linkedinUrl.trim() || null,
          instagramUrl: form.instagramUrl.trim() || null,
          xUrl: form.xUrl.trim() || null,
          position: Number(form.position),
          published: form.published,
        }, form.id ?? undefined)
      } else if (section === 'projects') {
        await saveProject({
          title: form.title.trim(),
          year: Number(form.year),
          description: form.description.trim(),
          imageUrl,
          tags: [...new Set(form.tags.split(',').map((tag) => tag.trim()).filter(Boolean))],
          position: Number(form.position),
          published: form.published,
        }, form.id ?? undefined)
      } else {
        await saveJourneyMilestone({
          year: Number(form.year),
          title: form.title.trim(),
          description: form.description.trim(),
          imageUrl,
          position: Number(form.position),
          published: form.published,
        }, form.id ?? undefined)
      }
      setImageFiles([])
      setForm({ ...blankForm, groupId: groups[0]?.id ?? '' })
      await loadContent()
      setNotice(`${sectionTitles[section]} saved.`)
    } catch (saveError) {
      setError(saveError instanceof Error ? saveError.message : 'The record could not be saved.')
    } finally {
      setIsSaving(false)
    }
  }

  const removeRecord = async (record: ContentRecord) => {
    if (!window.confirm(`Permanently delete "${recordTitle(record)}"?`)) return
    setBusyId(record.id)
    setError('')
    setNotice('')
    try {
      if (section === 'profiles') await deleteTeamMember(record.id)
      else if (section === 'projects') await deleteProject(record.id)
      else await deleteJourneyMilestone(record.id)
      if (form.id === record.id) resetForm()
      setNotice('Record deleted.')
      await loadContent()
    } catch (deleteError) {
      setError(deleteError instanceof Error ? deleteError.message : 'The record could not be deleted.')
    } finally {
      setBusyId('')
    }
  }

  const field = (name: keyof ContentForm, value: string | boolean) => {
    setForm((current) => ({ ...current, [name]: value }))
  }

  return (
    <section className="admin-content-manager" aria-labelledby="admin-page-title">
      <div className="admin-page-heading">
        <div>
          <p className="admin-eyebrow">CONTENT / DATABASE RECORDS</p>
          <h1 id="admin-page-title">{sectionTitles[section]}<span>.</span></h1>
          <p>Edit drafts and publish approved content to the public website.</p>
        </div>
        <div className="admin-system-state"><span aria-hidden="true" /> DATABASE CONNECTED</div>
      </div>

      {(error || notice) && (
        <p className={error ? 'admin-content-feedback is-error' : 'admin-content-feedback'} role={error ? 'alert' : 'status'}>
          {error || notice}
        </p>
      )}

      <section className="admin-content-panel" aria-labelledby="admin-content-form-title">
        <div className="admin-event-panel-heading">
          <div>
            <p className="admin-eyebrow">{form.id ? 'UPDATE RECORD' : 'NEW RECORD'}</p>
            <h2 id="admin-content-form-title">{form.id ? 'Edit content' : 'Add content'}</h2>
          </div>
          {form.id && <button className="admin-event-secondary-button" type="button" onClick={resetForm}>CANCEL EDIT</button>}
        </div>

        <form className="admin-content-form" onSubmit={(event) => void save(event)}>
          {section === 'profiles' && (
            <>
              <label>Team group
                <select required value={form.groupId} onChange={(event) => field('groupId', event.target.value)}>
                  <option value="">Select a group</option>
                  {groups.map((group) => <option key={group.id} value={group.id}>{group.title}</option>)}
                </select>
              </label>
              <label>Name
                <input required maxLength={120} value={form.title} onChange={(event) => field('title', event.target.value)} />
              </label>
              <label>Role
                <input required maxLength={180} value={form.role} onChange={(event) => field('role', event.target.value)} />
              </label>
            </>
          )}
          {section !== 'profiles' && (
            <>
              <label>Title
                <input required maxLength={200} value={form.title} onChange={(event) => field('title', event.target.value)} />
              </label>
              <label>Year
                <input required type="number" min="1900" max="2200" value={form.year} onChange={(event) => field('year', event.target.value)} />
              </label>
            </>
          )}
          <div className="admin-content-wide">
            <ImageUploadField
              label="Image"
              files={imageFiles}
              onFilesChange={setImageFiles}
              currentUrls={form.imageUrl ? [form.imageUrl] : []}
              required={!form.imageUrl}
              disabled={isSaving}
            />
          </div>
          {section !== 'profiles' && (
            <label className="admin-content-wide">Description
              <textarea required maxLength={12000} rows={5} value={form.description} onChange={(event) => field('description', event.target.value)} />
            </label>
          )}
          {section === 'profiles' && (
            <>
              <label>LinkedIn URL
                <input type="url" maxLength={2048} value={form.linkedinUrl} onChange={(event) => field('linkedinUrl', event.target.value)} />
              </label>
              <label>Instagram URL
                <input type="url" maxLength={2048} value={form.instagramUrl} onChange={(event) => field('instagramUrl', event.target.value)} />
              </label>
              <label>X URL
                <input type="url" maxLength={2048} value={form.xUrl} onChange={(event) => field('xUrl', event.target.value)} />
              </label>
            </>
          )}
          {section === 'projects' && (
            <label className="admin-content-wide">Tags (comma-separated)
              <input maxLength={1600} value={form.tags} onChange={(event) => field('tags', event.target.value)} />
            </label>
          )}
          <label>Position
            <input required type="number" min="0" value={form.position} onChange={(event) => field('position', event.target.value)} />
          </label>
          <label className="admin-content-publish">
            <input type="checkbox" checked={form.published} onChange={(event) => field('published', event.target.checked)} />
            Published on public site
          </label>
          <div className="admin-content-actions admin-content-wide">
            <button type="submit" disabled={isSaving || (section === 'profiles' && groups.length === 0)}>
              {isSaving ? 'SAVING…' : form.id ? 'SAVE CHANGES' : 'CREATE RECORD'}
            </button>
            <span>{isSaving ? 'Uploading image and saving…' : !form.id ? 'New records are drafts until published.' : ''}</span>
          </div>
        </form>
      </section>

      <section className="admin-content-panel" aria-labelledby="admin-content-list-title">
        <div className="admin-event-panel-heading">
          <div>
            <p className="admin-eyebrow">SAVED RECORDS</p>
            <h2 id="admin-content-list-title">{isLoading ? 'Loading…' : `${records.length} records`}</h2>
          </div>
        </div>
        {isLoading ? <p role="status">Loading database records…</p> : records.length === 0 ? (
          <p className="admin-content-empty">No records yet. Add the first one above.</p>
        ) : (
          <ul className="admin-content-list">
            {records.map((record) => (
              <li key={record.id}>
                <div>
                  <strong>{recordTitle(record)}</strong>
                  <span>{section === 'profiles'
                    ? (record as TeamMember).role
                    : `${'year' in record ? record.year : ''} · ${record.published ? 'Published' : 'Draft'}`}</span>
                </div>
                <div className="admin-content-record-actions">
                  <button type="button" onClick={() => editRecord(record)}>EDIT</button>
                  <button type="button" disabled={busyId === record.id} onClick={() => void removeRecord(record)}>
                    {busyId === record.id ? '…' : 'DELETE'}
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </section>
  )
}

export default ContentManagement
