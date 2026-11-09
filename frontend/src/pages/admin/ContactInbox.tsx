import { useEffect, useState } from 'react'
import {
  downloadContactAttachment,
  fetchAdminContactMessage,
  fetchAdminContactMessages,
  updateContactMessageState,
} from '../../lib/adminContactMessagesApi'
import type {
  AdminContactMessage,
  AdminContactMessageListItem,
  ContactMessageState,
} from '../../lib/adminContactMessagesApi'
import './ContactInbox.css'

type ContactFilter = ContactMessageState | 'ALL'

const filters: { value: ContactFilter; label: string }[] = [
  { value: 'ALL', label: 'All' },
  { value: 'NEW', label: 'Unread' },
  { value: 'IN_PROGRESS', label: 'Read' },
  { value: 'ARCHIVED', label: 'Archived' },
]

function formattedDate(value: string) {
  return new Intl.DateTimeFormat('en-IN', {
    dateStyle: 'medium',
    timeStyle: 'short',
    timeZone: 'Asia/Kolkata',
  }).format(new Date(value))
}

function ContactInbox() {
  const [filter, setFilter] = useState<ContactFilter>('ALL')
  const [page, setPage] = useState(0)
  const [messages, setMessages] = useState<AdminContactMessageListItem[]>([])
  const [selectedId, setSelectedId] = useState('')
  const [selectedMessage, setSelectedMessage] = useState<AdminContactMessage | null>(null)
  const [totalElements, setTotalElements] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [isLoadingDetail, setIsLoadingDetail] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [isDownloading, setIsDownloading] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  useEffect(() => {
    let active = true
    setIsLoading(true)
    setError('')
    fetchAdminContactMessages(filter, page)
      .then((result) => {
        if (!active) return
        setMessages(result.content)
        setTotalElements(result.totalElements)
        setTotalPages(result.totalPages)
      })
      .catch((requestError: unknown) => {
        if (active) {
          setError(requestError instanceof Error ? requestError.message : 'Contact messages could not be loaded.')
        }
      })
      .finally(() => {
        if (active) setIsLoading(false)
      })
    return () => { active = false }
  }, [filter, page])

  useEffect(() => {
    if (!selectedId) {
      setSelectedMessage(null)
      return
    }
    let active = true
    setIsLoadingDetail(true)
    fetchAdminContactMessage(selectedId)
      .then((message) => {
        if (active) setSelectedMessage(message)
      })
      .catch((requestError: unknown) => {
        if (active) {
          setError(requestError instanceof Error ? requestError.message : 'The selected message could not be loaded.')
        }
      })
      .finally(() => {
        if (active) setIsLoadingDetail(false)
      })
    return () => { active = false }
  }, [selectedId])

  function chooseFilter(nextFilter: ContactFilter) {
    setFilter(nextFilter)
    setPage(0)
    setSelectedId('')
    setSelectedMessage(null)
    setError('')
    setNotice('')
  }

  async function changeState(state: ContactMessageState) {
    if (!selectedMessage) return
    setIsSaving(true)
    setError('')
    setNotice('')
    let updated: AdminContactMessage
    try {
      updated = await updateContactMessageState(selectedMessage.id, state)
      setSelectedMessage(updated)
      setNotice(state === 'ARCHIVED'
        ? 'Message archived.'
        : state === 'IN_PROGRESS' ? 'Message marked as read.' : 'Message marked as unread.')
    } catch (requestError: unknown) {
      setError(requestError instanceof Error ? requestError.message : 'Message status could not be updated.')
      setIsSaving(false)
      return
    }

    try {
      const result = await fetchAdminContactMessages(filter, page)
      setMessages(result.content)
      setTotalElements(result.totalElements)
      setTotalPages(result.totalPages)
      if (!result.content.some((item) => item.id === updated.id) && filter !== 'ALL') {
        setSelectedId('')
        setSelectedMessage(null)
      }
    } catch (requestError: unknown) {
      setError(requestError instanceof Error
        ? `Message status was updated, but the inbox could not be refreshed: ${requestError.message}`
        : 'Message status was updated, but the inbox could not be refreshed.')
    } finally {
      setIsSaving(false)
    }
  }

  async function downloadAttachment() {
    if (!selectedMessage?.attachmentFileName) return
    setIsDownloading(true)
    setError('')
    try {
      await downloadContactAttachment(selectedMessage.id, selectedMessage.attachmentFileName)
    } catch (requestError: unknown) {
      setError(requestError instanceof Error ? requestError.message : 'The attachment could not be downloaded.')
    } finally {
      setIsDownloading(false)
    }
  }

  const hasPreviousPage = page > 0
  const hasNextPage = page + 1 < totalPages

  return (
    <section className="admin-contact-inbox" aria-labelledby="admin-page-title">
      <div className="admin-page-heading">
        <div>
          <p className="admin-eyebrow">OPERATIONS / PRIVATE SUBMISSIONS</p>
          <h1 id="admin-page-title">Contact inbox<span>.</span></h1>
          <p>Review website messages, mark them read or unread, and archive handled submissions.</p>
        </div>
        <div className="admin-system-state"><span aria-hidden="true" /> ADMIN ONLY</div>
      </div>

      <div className="contact-inbox-toolbar">
        <div className="contact-inbox-filters" aria-label="Filter contact messages">
          {filters.map((item) => (
            <button
              type="button"
              key={item.value}
              className={filter === item.value ? 'is-active' : ''}
              aria-pressed={filter === item.value}
              onClick={() => chooseFilter(item.value)}
            >
              {item.label}
            </button>
          ))}
        </div>
        <span>{totalElements} {totalElements === 1 ? 'message' : 'messages'}</span>
      </div>

      {error && <p className="contact-inbox-message is-error" role="alert">{error}</p>}
      {notice && <p className="contact-inbox-message" role="status">{notice}</p>}

      <div className="contact-inbox-grid">
        <section className="admin-panel contact-inbox-list" aria-label="Contact messages">
          <div className="admin-panel-heading">
            <div>
              <p className="admin-eyebrow">INBOX</p>
              <h2>{filter === 'ALL' ? 'All messages' : filters.find((item) => item.value === filter)?.label}</h2>
            </div>
            <span className="admin-panel-tag">{totalElements} TOTAL</span>
          </div>

          {isLoading ? (
            <p className="contact-inbox-empty" role="status">Loading messages…</p>
          ) : messages.length === 0 ? (
            <p className="contact-inbox-empty">No messages in this view.</p>
          ) : (
            <div className="contact-inbox-items">
              {messages.map((message) => (
                <button
                  type="button"
                  className={`contact-inbox-item${selectedId === message.id ? ' is-selected' : ''}${message.state === 'NEW' ? ' is-unread' : ''}`}
                  key={message.id}
                  onClick={() => {
                    setSelectedId(message.id)
                    setSelectedMessage(null)
                    setError('')
                    setNotice('')
                  }}
                  aria-current={selectedId === message.id ? 'true' : undefined}
                >
                  <span className="contact-inbox-item-top">
                    <strong>{message.senderName}</strong>
                    <time dateTime={message.createdAt}>{formattedDate(message.createdAt)}</time>
                  </span>
                  <span className="contact-inbox-item-subject">{message.subject}</span>
                  <span className="contact-inbox-item-bottom">
                    <span>{message.senderEmail}</span>
                    <span className={`contact-inbox-state state-${message.state.toLowerCase()}`}>
                      {message.state === 'NEW' ? 'UNREAD' : message.state === 'IN_PROGRESS' ? 'READ' : 'ARCHIVED'}
                    </span>
                  </span>
                  {message.attachmentFileName && <span className="contact-inbox-attachment-label">ATTACHMENT INCLUDED</span>}
                </button>
              ))}
            </div>
          )}

          <div className="contact-inbox-pagination">
            <button type="button" disabled={!hasPreviousPage || isLoading} onClick={() => {
              setSelectedId('')
              setSelectedMessage(null)
              setPage((value) => value - 1)
            }}>
              ← Newer
            </button>
            <span>{totalPages === 0 ? '0 / 0' : `${page + 1} / ${totalPages}`}</span>
            <button type="button" disabled={!hasNextPage || isLoading} onClick={() => {
              setSelectedId('')
              setSelectedMessage(null)
              setPage((value) => value + 1)
            }}>
              Older →
            </button>
          </div>
        </section>

        <section className="admin-panel contact-inbox-detail" aria-label="Selected message">
          {!selectedId ? (
            <div className="contact-inbox-empty-state">
              <span className="admin-empty-icon" aria-hidden="true">IN</span>
              <p className="admin-eyebrow">MESSAGE DETAILS</p>
              <h2>Select a message</h2>
              <p>Choose an item from the inbox to read its full contents.</p>
            </div>
          ) : isLoadingDetail || !selectedMessage ? (
            <p className="contact-inbox-empty" role="status">Loading message…</p>
          ) : (
            <>
              <div className="admin-panel-heading">
                <div>
                  <p className="admin-eyebrow">{selectedMessage.state === 'NEW' ? 'UNREAD MESSAGE' : 'MESSAGE DETAILS'}</p>
                  <h2>{selectedMessage.subject}</h2>
                </div>
                <span className={`contact-inbox-state state-${selectedMessage.state.toLowerCase()}`}>
                  {selectedMessage.state === 'NEW' ? 'UNREAD' : selectedMessage.state === 'IN_PROGRESS' ? 'READ' : 'ARCHIVED'}
                </span>
              </div>
              <div className="contact-inbox-sender">
                <strong>{selectedMessage.senderName}</strong>
                <a href={`mailto:${selectedMessage.senderEmail}`}>{selectedMessage.senderEmail}</a>
                <time dateTime={selectedMessage.createdAt}>{formattedDate(selectedMessage.createdAt)}</time>
              </div>
              <div className="contact-inbox-body">{selectedMessage.message}</div>
              {selectedMessage.attachmentFileName && (
                <div className="contact-inbox-file">
                  <span>{selectedMessage.attachmentFileName}</span>
                  <button type="button" disabled={isDownloading} onClick={() => void downloadAttachment()}>
                    {isDownloading ? 'Downloading…' : 'Download'}
                  </button>
                </div>
              )}
              <div className="contact-inbox-actions">
                {selectedMessage.state !== 'IN_PROGRESS' && selectedMessage.state !== 'ARCHIVED' && (
                  <button type="button" disabled={isSaving} onClick={() => void changeState('IN_PROGRESS')}>
                    MARK AS READ
                  </button>
                )}
                {selectedMessage.state !== 'NEW' && selectedMessage.state !== 'ARCHIVED' && (
                  <button type="button" disabled={isSaving} onClick={() => void changeState('NEW')}>
                    MARK UNREAD
                  </button>
                )}
                {selectedMessage.state !== 'ARCHIVED' ? (
                  <button type="button" disabled={isSaving} onClick={() => void changeState('ARCHIVED')}>
                    ARCHIVE
                  </button>
                ) : (
                  <button type="button" disabled={isSaving} onClick={() => void changeState('NEW')}>
                    RESTORE TO UNREAD
                  </button>
                )}
              </div>
            </>
          )}
        </section>
      </div>
    </section>
  )
}

export default ContactInbox
