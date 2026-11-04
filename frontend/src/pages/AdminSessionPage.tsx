import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { AdminApiError, fetchAdminSession, logoutAdmin } from '../lib/adminAuth'
import AdminPortalPreview from './AdminPortalPreview'
import './AdminSessionPage.css'

function AdminSessionPage() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isSigningOut, setIsSigningOut] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  useEffect(() => {
    document.title = 'Admin Workspace | Soaring Eagles'
    let active = true

    fetchAdminSession()
      .then((session) => {
        if (active) setEmail(session.email)
      })
      .catch((error: unknown) => {
        if (!active) return
        if (error instanceof AdminApiError && error.status === 401) {
          navigate('/admin-preview/login', { replace: true })
          return
        }
        setErrorMessage('The server could not verify your session. No admin data is available.')
      })
      .finally(() => {
        if (active) setIsLoading(false)
      })

    return () => {
      active = false
      document.title = 'Soaring Eagles | Aerial Robotics Club'
    }
  }, [navigate])

  const signOut = async () => {
    setIsSigningOut(true)
    setErrorMessage('')
    try {
      await logoutAdmin()
      navigate('/admin-preview/login', { replace: true })
    } catch (error) {
      if (error instanceof AdminApiError && error.status === 401) {
        navigate('/admin-preview/login', { replace: true })
        return
      }
      setErrorMessage('Sign out could not be confirmed. Retry or close this browser session.')
    } finally {
      setIsSigningOut(false)
    }
  }

  if (email) {
    return (
      <AdminPortalPreview
        authenticatedEmail={email}
        isSigningOut={isSigningOut}
        onSignOut={signOut}
        preview={false}
        signOutError={errorMessage}
      />
    )
  }

  return (
    <main className="admin-session-page">
      <div className="admin-session-topbar">
        <Link className="admin-session-brand" to="/">SOARING EAGLES <span>/ ADMIN</span></Link>
        <Link to="/">BACK TO WEBSITE ↗</Link>
      </div>
      <section className="admin-session-card" aria-labelledby="admin-session-title">
        <p className="admin-session-eyebrow">SERVER-VERIFIED WORKSPACE</p>
        {isLoading ? (
          <p role="status">Verifying your administrator session…</p>
        ) : (
          <>
            <h1 id="admin-session-title">Workspace unavailable.</h1>
            <p className="admin-session-unavailable">
              {errorMessage || 'The server could not verify your administrator session.'}
            </p>
            <Link to="/admin-preview/login">RETURN TO SIGN IN →</Link>
          </>
        )}
      </section>
    </main>
  )
}

export default AdminSessionPage
