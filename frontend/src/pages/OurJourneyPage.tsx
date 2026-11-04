import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import SiteHeader from '../components/SiteHeader'
import JourneyTimeline from '../components/ui/journey-timeline'
import { fetchPublicJourney } from '../lib/contentApi'
import type { JourneyMilestone } from '../lib/contentApi'
import './OurJourneyPage.css'

function OurJourneyPage() {
  const [milestones, setMilestones] = useState<JourneyMilestone[] | null>(null)
  const [loadError, setLoadError] = useState('')

  useEffect(() => {
    document.title = 'Our Journey | Soaring Eagles'
    let active = true
    fetchPublicJourney()
      .then((result) => {
        if (active) setMilestones(result)
      })
      .catch(() => {
        if (active) setLoadError('Journey milestones could not be loaded. Please try again later.')
      })
    return () => {
      active = false
      document.title = 'Soaring Eagles | Aerial Robotics Club'
    }
  }, [])

  return (
    <div className="journey-page">
      <SiteHeader />
      <main className="journey-main">
        <section className="journey-intro" aria-labelledby="journey-title">
          <p className="journey-kicker"><span aria-hidden="true" /> MILESTONES / SOARING EAGLES</p>
          <h1 id="journey-title">Built through<br /><em>every flight.</em></h1>
          <p className="journey-intro-copy">
            From early competition builds to autonomous aircraft, each challenge has taught us how to design, test, and fly better.
          </p>
        </section>

        <section className="journey-archive" aria-labelledby="journey-archive-title">
          <div className="journey-archive-heading">
            <div>
              <p className="journey-section-label">OUR JOURNEY</p>
              <h2 id="journey-archive-title">Progress, <em>in flight.</em></h2>
            </div>
            <p>{milestones ? `${milestones.length} milestones · newest first` : 'Newest first'}</p>
          </div>
          {loadError ? (
            <p className="journey-content-note" role="alert">{loadError}</p>
          ) : !milestones ? (
            <p className="journey-content-note" role="status">Loading milestones…</p>
          ) : <JourneyTimeline milestones={milestones} />}
        </section>
      </main>

      <footer className="site-footer journey-footer">
        <Link className="brand" to="/" aria-label="Back to Soaring Eagles home">
          <img src="/images/brand/club-logo.png" alt="" />
          <span>SOARING<br />EAGLES</span>
        </Link>
        <span>STUDENT AERODESIGN · TUMAKURU, INDIA</span>
        <Link to="/#contact">GET IN TOUCH ↗</Link>
      </footer>
    </div>
  )
}

export default OurJourneyPage
