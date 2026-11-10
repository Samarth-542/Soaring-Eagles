import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import EventManagement from './admin/EventManagement'
import RegistrationManagement from './admin/RegistrationManagement'
import ContentManagement from './admin/ContentManagement'
import AdminAccountManagement from './admin/AdminAccountManagement'
import ContactInbox from './admin/ContactInbox'
import SiteDetailsManagement from './admin/SiteDetailsManagement'
import ResponsiveImage from '../components/ResponsiveImage'
import './AdminPortalPreview.css'

const adminSections = [
  {
    id: 'overview',
    label: 'Overview',
    group: 'Workspace',
    icon: 'OV',
    description: 'A single starting point for the club website’s management areas.',
  },
  {
    id: 'events',
    label: 'Events',
    group: 'Content',
    icon: 'EV',
    description: 'Create drafts, edit event details, schedule publication, and close registration.',
  },
  {
    id: 'registrations',
    label: 'Registrations',
    group: 'Operations',
    icon: 'RG',
    description: 'Review private participant and team details, payment state, and exportable records.',
  },
  {
    id: 'profiles',
    label: 'Team profiles',
    group: 'Content',
    icon: 'TM',
    description: 'Review selected-recruit submissions and publish only approved public profile fields.',
  },
  {
    id: 'projects',
    label: 'Projects',
    group: 'Content',
    icon: 'PJ',
    description: 'Maintain project summaries, years, status, technologies, and media.',
  },
  {
    id: 'journey',
    label: 'Our journey',
    group: 'Content',
    icon: 'JN',
    description: 'Edit dated milestones, stories, photos, and historical results.',
  },
  {
    id: 'contacts',
    label: 'Contact inbox',
    group: 'Operations',
    icon: 'IN',
    description: 'Review website messages, mark them read or unread, and archive handled submissions.',
  },
  {
    id: 'site-details',
    label: 'Site details',
    group: 'Content',
    icon: 'SD',
    description: 'Update the official public email, social links, and club location.',
  },
  {
    id: 'admins',
    label: 'Admin accounts',
    group: 'Access',
    icon: 'AD',
    description: 'Manage admins with equal permissions and safeguards against removing the last active admin.',
  },
] as const

type AdminSectionId = (typeof adminSections)[number]['id']

interface AdminPortalPreviewProps {
  authenticatedEmail?: string
  isSigningOut?: boolean
  onSignOut?: () => void
  preview?: boolean
  signOutError?: string
}

function AdminPortalPreview({
  authenticatedEmail,
  isSigningOut = false,
  onSignOut,
  preview = true,
  signOutError = '',
}: AdminPortalPreviewProps) {
  const [activeSection, setActiveSection] = useState<AdminSectionId>('overview')
  const activeItem = adminSections.find((section) => section.id === activeSection) ?? adminSections[0]
  const sectionGroups = [...new Set(adminSections.map((section) => section.group))]

  useEffect(() => {
    document.title = preview ? 'Admin Portal Preview | Soaring Eagles' : 'Admin Dashboard | Soaring Eagles'
    return () => {
      document.title = 'Soaring Eagles | Aerial Robotics Club'
    }
  }, [preview])

  return (
    <div className="admin-preview">
      <aside className={`admin-sidebar${preview ? '' : ' admin-sidebar-authenticated'}`}>
        <Link className="admin-brand" to="/" aria-label="Return to Soaring Eagles website">
          <ResponsiveImage src="/images/brand/club-logo.png" alt="" width={40} height={40} loading="eager" />
          <span>
            <strong>SOARING EAGLES</strong>
            <small>CLUB CONTROL</small>
          </span>
        </Link>

        <div className="admin-sidebar-label">MANAGEMENT</div>
        <nav className="admin-navigation" aria-label={preview ? 'Admin preview sections' : 'Admin workspace sections'}>
          {sectionGroups.map((group) => (
            <div className="admin-nav-group" key={group}>
              <p>{group}</p>
              {adminSections.filter((section) => section.group === group).map((section) => (
                <button
                  className={`admin-nav-item${activeSection === section.id ? ' is-active' : ''}`}
                  type="button"
                  key={section.id}
                  aria-current={activeSection === section.id ? 'page' : undefined}
                  onClick={() => setActiveSection(section.id)}
                >
                  <span className="admin-nav-icon" aria-hidden="true">{section.icon}</span>
                  <span>{section.label}</span>
                  {activeSection === section.id && <span className="admin-nav-indicator" aria-hidden="true" />}
                </button>
              ))}
            </div>
          ))}
        </nav>

        <div className="admin-sidebar-bottom">
          <span className="admin-avatar" aria-hidden="true">
            {authenticatedEmail ? authenticatedEmail.slice(0, 2).toUpperCase() : 'SE'}
          </span>
          <span>
            <strong title={authenticatedEmail}>{authenticatedEmail ?? 'Portal design'}</strong>
            <small>{authenticatedEmail ? 'Signed in' : 'Preview only'}</small>
          </span>
          {preview ? (
            <span className="admin-preview-dot" aria-hidden="true" />
          ) : (
            <button
              className="admin-signout-button"
              type="button"
              onClick={onSignOut}
              disabled={isSigningOut}
            >
              {isSigningOut ? '…' : 'OUT'}
              <span className="admin-signout-label">SIGN OUT</span>
            </button>
          )}
        </div>
      </aside>

      <main className="admin-main">
        <header className="admin-topbar">
          <div className="admin-breadcrumb">
            <span>CONTROL ROOM</span>
            <span aria-hidden="true">/</span>
            <strong>{activeItem.label.toUpperCase()}</strong>
          </div>
          <div className="admin-topbar-actions">
            {!preview && <span className="admin-session-badge">SESSION VERIFIED</span>}
            <Link className="admin-view-site" to="/">VIEW WEBSITE <span aria-hidden="true">↗</span></Link>
          </div>
        </header>

        <div className="admin-content">
          {preview ? (
            <div className="admin-preview-notice" role="status">
              <span className="admin-notice-mark" aria-hidden="true">!</span>
              <p><strong>Design preview only.</strong> Authentication, backend APIs, and live records are not connected. Do not enter or store real information here.</p>
            </div>
          ) : (
            <div className="admin-preview-notice admin-authenticated-notice" role="status">
              <span className="admin-notice-mark" aria-hidden="true">✓</span>
              <p><strong>Signed in as {authenticatedEmail}.</strong> Events, registrations, contact messages, site details, content, and admin accounts are connected to the database.</p>
            </div>
          )}

          {activeSection === 'overview' ? (
            <section aria-labelledby="admin-page-title">
              <div className="admin-page-heading">
                <div>
                  <p className="admin-eyebrow">SOARING EAGLES / ADMINISTRATION</p>
                  <h1 id="admin-page-title">Control room<span>.</span></h1>
                  <p>Manage the public site and club operations from one place.</p>
                </div>
                <div className="admin-system-state">
                  <span aria-hidden="true" />
                  {preview ? 'DESIGN PREVIEW' : 'SESSION VERIFIED'}
                </div>
              </div>

              <div className="admin-overview-grid">
                {adminSections.filter((section) => section.id !== 'overview').map((section) => (
                  <button
                    className="admin-module-card"
                    type="button"
                    key={section.id}
                    onClick={() => setActiveSection(section.id)}
                  >
                    <span className="admin-module-icon" aria-hidden="true">{section.icon}</span>
                    <span className="admin-module-copy">
                      <strong>{section.label}</strong>
                      <span>{section.description}</span>
                    </span>
                    <span className="admin-module-arrow" aria-hidden="true">↗</span>
                  </button>
                ))}
              </div>

              <div className="admin-bottom-grid">
                <section className="admin-panel" aria-labelledby="admin-workflow-title">
                  <div className="admin-panel-heading">
                    <div>
                      <p className="admin-eyebrow">WORKFLOW</p>
                      <h2 id="admin-workflow-title">Built around safe publishing.</h2>
                    </div>
                    <span className="admin-panel-tag">PRD / 01</span>
                  </div>
                  <div className="admin-workflow-list">
                    <div><span>01</span><p><strong>Draft privately</strong>New events start unpublished.</p></div>
                    <div><span>02</span><p><strong>Review before publishing</strong>Approved public fields stay separate from private submissions.</p></div>
                    <div><span>03</span><p><strong>Save through the API</strong>Publishing and private reads require server-side authorization.</p></div>
                  </div>
                </section>

                <section className="admin-panel admin-access-panel" aria-labelledby="admin-access-title">
                  <p className="admin-eyebrow">ACCESS &amp; SAFETY</p>
                  <h2 id="admin-access-title">The server remains the gatekeeper.</h2>
                  <p>Hiding controls is not authorization. Admin sign-in, permissions, audit history, and protected data access must be enforced by the backend.</p>
                  <div className="admin-access-status">
                    <span aria-hidden="true" />
                    {preview ? 'BACKEND FOUNDATION ONLY' : 'ADMIN APIS CONNECTED'}
                  </div>
                </section>
              </div>
              {signOutError && <p className="admin-signout-error" role="alert">{signOutError}</p>}
            </section>
          ) : !preview && activeSection === 'events' ? (
            <EventManagement />
          ) : !preview && activeSection === 'registrations' ? (
            <RegistrationManagement />
          ) : !preview && activeSection === 'contacts' ? (
            <ContactInbox />
          ) : !preview && activeSection === 'site-details' ? (
            <SiteDetailsManagement />
          ) : !preview && (activeSection === 'profiles' || activeSection === 'projects' || activeSection === 'journey') ? (
            <ContentManagement section={activeSection} />
          ) : !preview && activeSection === 'admins' && authenticatedEmail ? (
            <AdminAccountManagement currentEmail={authenticatedEmail} />
          ) : (
            <section className="admin-module-view" aria-labelledby="admin-page-title">
              <div className="admin-page-heading">
                <div>
                  <p className="admin-eyebrow">{activeItem.group.toUpperCase()} / MANAGEMENT</p>
                  <h1 id="admin-page-title">{activeItem.label}<span>.</span></h1>
                  <p>{activeItem.description}</p>
                </div>
                <div className="admin-system-state"><span aria-hidden="true" /> NOT CONNECTED</div>
              </div>
              <div className="admin-empty-panel">
                <span className="admin-empty-icon" aria-hidden="true">{activeItem.icon}</span>
                <p className="admin-eyebrow">SECURE DATA REQUIRED</p>
                <h2>This workspace is not connected yet.</h2>
                <p>Live records and management actions will appear here after authenticated backend services are implemented. No sample records are shown in this preview.</p>
                <button type="button" className="admin-back-button" onClick={() => setActiveSection('overview')}>
                  BACK TO OVERVIEW <span aria-hidden="true">↗</span>
                </button>
              </div>
              {signOutError && <p className="admin-signout-error" role="alert">{signOutError}</p>}
            </section>
          )}
        </div>
      </main>
    </div>
  )
}

export default AdminPortalPreview
