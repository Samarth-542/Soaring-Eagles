import { forwardRef, useEffect, useRef, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'

interface SiteHeaderProps {
  reveal?: boolean
}

const SiteHeader = forwardRef<HTMLElement, SiteHeaderProps>(({ reveal = false }, ref) => {
  const revealProps = reveal ? { 'data-reveal': 'header' } : {}
  const { pathname } = useLocation()
  const [menuOpen, setMenuOpen] = useState(false)
  const menuButtonRef = useRef<HTMLButtonElement>(null)

  useEffect(() => {
    if (!menuOpen) return

    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setMenuOpen(false)
        menuButtonRef.current?.focus()
      }
    }

    document.addEventListener('keydown', closeOnEscape)
    return () => document.removeEventListener('keydown', closeOnEscape)
  }, [menuOpen])

  const closeMenu = () => setMenuOpen(false)

  return (
    <header className={`site-header${reveal ? '' : ' site-header--page'}`} ref={ref}>
      <Link className="brand" to="/" aria-label="Soaring Eagles home" {...revealProps}>
        <img src="/images/brand/club-logo.png" alt="" />
        <span>SOARING<br />EAGLES</span>
      </Link>
      <button
        ref={menuButtonRef}
        className="mobile-menu-toggle"
        type="button"
        aria-expanded={menuOpen}
        aria-controls="mobile-site-menu"
        aria-label={menuOpen ? 'Close navigation menu' : 'Open navigation menu'}
        onClick={() => setMenuOpen((open) => !open)}
      >
        <span aria-hidden="true" />
        <span aria-hidden="true" />
      </button>
      <nav className="main-nav" aria-label="Main navigation" {...revealProps}>
        <Link to="/#home">HOME</Link>
        <Link to="/events" aria-current={pathname === '/events' ? 'page' : undefined}>EVENTS</Link>
        <Link to="/our-journey" aria-current={pathname === '/our-journey' ? 'page' : undefined}>JOURNEY</Link>
        <Link to="/projects" aria-current={pathname === '/projects' ? 'page' : undefined}>PROJECTS</Link>
        <Link to="/team" aria-current={pathname === '/team' ? 'page' : undefined}>TEAM</Link>
        <Link to="/admin-preview/login" aria-current={pathname.startsWith('/admin-preview') ? 'page' : undefined}>ADMIN SIGN IN</Link>
      </nav>
      <Link className="header-cta" to="/#contact" {...revealProps}>
        JOIN THE FLIGHT <span aria-hidden="true">↗</span>
      </Link>
      <div
        className={`mobile-site-menu${menuOpen ? ' is-open' : ''}`}
        id="mobile-site-menu"
        aria-hidden={!menuOpen}
      >
        <nav aria-label="Mobile navigation">
          <Link to="/#home" onClick={closeMenu}>HOME</Link>
          <Link to="/events" aria-current={pathname === '/events' ? 'page' : undefined} onClick={closeMenu}>EVENTS</Link>
          <Link to="/our-journey" aria-current={pathname === '/our-journey' ? 'page' : undefined} onClick={closeMenu}>JOURNEY</Link>
          <Link to="/projects" aria-current={pathname === '/projects' ? 'page' : undefined} onClick={closeMenu}>PROJECTS</Link>
          <Link to="/team" aria-current={pathname === '/team' ? 'page' : undefined} onClick={closeMenu}>TEAM</Link>
          <Link to="/admin-preview/login" aria-current={pathname.startsWith('/admin-preview') ? 'page' : undefined} onClick={closeMenu}>ADMIN SIGN IN</Link>
          <Link className="mobile-menu-cta" to="/#contact" onClick={closeMenu}>
            JOIN THE FLIGHT <span aria-hidden="true">↗</span>
          </Link>
        </nav>
      </div>
    </header>
  )
})

SiteHeader.displayName = 'SiteHeader'

export default SiteHeader
