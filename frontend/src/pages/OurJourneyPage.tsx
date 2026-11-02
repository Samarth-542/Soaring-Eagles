import { useEffect } from 'react'
import { Link } from 'react-router-dom'
import SiteHeader from '../components/SiteHeader'
import JourneyTimeline from '../components/ui/journey-timeline'
import { journeyMilestones } from '../data/journey'
import './OurJourneyPage.css'

function OurJourneyPage() {
  useEffect(() => {
    document.title = 'Our Journey | Soaring Eagles'
    return () => {
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
            <p>{journeyMilestones.length} milestones · newest first</p>
          </div>
          <JourneyTimeline />
          <p className="journey-content-note">
            Milestones and results are migrated from the previous public site. Competition placements and historical details should be confirmed with the club before production.
          </p>
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
