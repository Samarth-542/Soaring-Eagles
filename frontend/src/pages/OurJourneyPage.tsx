import { useEffect } from 'react'
import { Link } from 'react-router-dom'
import PublicDataStatus from '../components/PublicDataStatus'
import ResponsiveImage from '../components/ResponsiveImage'
import SiteHeader from '../components/SiteHeader'
import JourneyTimeline from '../components/ui/journey-timeline'
import { fetchPublicJourney } from '../lib/contentApi'
import { usePublicData } from '../lib/usePublicData'
import './OurJourneyPage.css'

function OurJourneyPage() {
  const journeyData = usePublicData('journey', 300_000, fetchPublicJourney)
  const milestones = journeyData.data?.data

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
            <p>{milestones ? `${milestones.length} milestones · newest first` : 'Newest first'}</p>
          </div>
          {!milestones ? (
            <PublicDataStatus
              isLoading={journeyData.isLoading}
              isRefreshing={journeyData.isRefreshing}
              error={journeyData.error}
              hasData={false}
              errorMessage="Journey milestones could not be loaded. Please try again."
              onRetry={journeyData.retry}
            />
          ) : (
            <>
              <PublicDataStatus
                isLoading={false}
                isRefreshing={journeyData.isRefreshing}
                error={journeyData.error}
                hasData
                errorMessage=""
                onRetry={journeyData.retry}
              />
              <JourneyTimeline milestones={milestones} />
            </>
          )}
        </section>
      </main>

      <footer className="site-footer journey-footer">
        <Link className="brand" to="/" aria-label="Back to Soaring Eagles home">
          <ResponsiveImage src="/images/brand/club-logo.png" alt="" width={40} height={40} loading="eager" />
          <span>SOARING<br />EAGLES</span>
        </Link>
        <span>STUDENT AERODESIGN · TUMAKURU, INDIA</span>
        <Link to="/#contact">GET IN TOUCH ↗</Link>
      </footer>
    </div>
  )
}

export default OurJourneyPage
