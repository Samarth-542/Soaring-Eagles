import { FormEvent, useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { AdminApiError, fetchCsrfToken, loginAdmin } from '../lib/adminAuth'
import ResponsiveImage from '../components/ResponsiveImage'
import './AdminLoginPreview.css'

function AdminLoginPreview() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [fieldErrors, setFieldErrors] = useState<{ email?: string; password?: string }>({})
  const [errorMessage, setErrorMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    document.title = 'Admin Sign In | Soaring Eagles'
    return () => {
      document.title = 'Soaring Eagles | Aerial Robotics Club'
    }
  }, [])

  const submitSignIn = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const normalizedEmail = email.trim()
    const errors: { email?: string; password?: string } = {}

    if (!normalizedEmail) {
      errors.email = 'Enter your administrator email.'
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normalizedEmail)) {
      errors.email = 'Enter a valid email address.'
    } else if (normalizedEmail.length > 254) {
      errors.email = 'Email addresses must be 254 characters or fewer.'
    }

    if (!password) errors.password = 'Enter your password.'
    else if (password.length > 1024) errors.password = 'Password is too long.'

    setFieldErrors(errors)
    setErrorMessage('')
    if (Object.keys(errors).length > 0) return

    setIsSubmitting(true)
    try {
      const csrfToken = await fetchCsrfToken()
      await loginAdmin(normalizedEmail, password, csrfToken)
      setPassword('')
      navigate('/admin', { replace: true })
    } catch (error) {
      setPassword('')
      if (error instanceof AdminApiError && error.status === 401) {
        setErrorMessage('Email or password is incorrect.')
      } else if (error instanceof AdminApiError && error.status === 429) {
        setErrorMessage('Too many sign-in attempts. Wait 15 minutes before trying again.')
      } else if (error instanceof AdminApiError && error.status === 403) {
        setErrorMessage('Your sign-in request expired. Reload this page and try again.')
      } else {
        setErrorMessage('The authentication service is unavailable. Check your connection and try again.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="admin-login-preview">
      <div className="admin-login-topbar">
        <Link className="admin-login-brand" to="/" aria-label="Return to Soaring Eagles website">
          <ResponsiveImage src="/images/brand/club-logo.png" alt="" width={40} height={40} loading="eager" />
          <span>
            <strong>SOARING EAGLES</strong>
            <small>CLUB CONTROL</small>
          </span>
        </Link>
        <Link className="admin-login-public-link" to="/">BACK TO WEBSITE <span aria-hidden="true">↗</span></Link>
      </div>

      <section className="admin-login-layout" aria-labelledby="admin-login-title">
        <div className="admin-login-story">
          <p className="admin-login-eyebrow"><span aria-hidden="true" /> ADMINISTRATOR ACCESS</p>
          <h1>One team.<br />One <em>control room.</em></h1>
          <p className="admin-login-story-copy">
            A secure workspace for the people who keep Soaring Eagles moving forward.
          </p>
          <div className="admin-login-story-rule">
            <span>SOARING EAGLES</span>
            <span>13.34° N / 77.10° E</span>
          </div>
        </div>

        <section className="admin-login-card" aria-label="Administrator sign in">
          <div className="admin-login-card-heading">
            <p className="admin-login-eyebrow">SECURE WORKSPACE</p>
            <h2 id="admin-login-title">Admin sign in</h2>
            <p>Use your administrator account to continue.</p>
          </div>

          {errorMessage && (
            <div className="admin-login-preview-alert is-error" role="alert">
              <span aria-hidden="true">!</span>
              <p>{errorMessage}</p>
            </div>
          )}

          <form className="admin-login-form" onSubmit={submitSignIn} noValidate>
            <label htmlFor="admin-email">Admin email</label>
            <input
              id="admin-email"
              type="email"
              name="email"
              autoComplete="username"
              inputMode="email"
              maxLength={254}
              placeholder="name@example.com"
              value={email}
              aria-invalid={Boolean(fieldErrors.email)}
              aria-describedby={fieldErrors.email ? 'admin-email-error' : undefined}
              onChange={(event) => {
                setEmail(event.target.value)
                setFieldErrors((current) => ({ ...current, email: undefined }))
              }}
            />
            {fieldErrors.email && <span className="admin-login-field-error" id="admin-email-error">{fieldErrors.email}</span>}

            <label htmlFor="admin-password">Password</label>
            <input
              id="admin-password"
              type="password"
              name="password"
              autoComplete="current-password"
              maxLength={1024}
              placeholder="Enter your password"
              value={password}
              aria-invalid={Boolean(fieldErrors.password)}
              aria-describedby={fieldErrors.password ? 'admin-password-error' : undefined}
              onChange={(event) => {
                setPassword(event.target.value)
                setFieldErrors((current) => ({ ...current, password: undefined }))
              }}
            />
            {fieldErrors.password && <span className="admin-login-field-error" id="admin-password-error">{fieldErrors.password}</span>}

            <button type="submit" disabled={isSubmitting} aria-busy={isSubmitting}>
              {isSubmitting ? 'VERIFYING…' : 'SIGN IN'} <span aria-hidden="true">{isSubmitting ? '…' : '↗'}</span>
            </button>
            <p className="admin-login-form-note">Credentials are checked by the server. Your session uses a secure, HttpOnly cookie; this page does not store an authentication token.</p>
          </form>

          <div className="admin-login-card-footer">
            <span><i aria-hidden="true" /> SERVER-VERIFIED SIGN-IN</span>
          </div>
        </section>
      </section>

      <footer className="admin-login-footer">
        <span>SOARING EAGLES · ADMIN PORTAL DESIGN</span>
        <span>PRIVATE ACCESS · SERVER-VERIFIED</span>
      </footer>
    </main>
  )
}

export default AdminLoginPreview
