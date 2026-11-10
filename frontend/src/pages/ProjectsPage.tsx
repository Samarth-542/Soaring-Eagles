import { useEffect } from 'react'
import { Link } from 'react-router-dom'
import PublicDataStatus from '../components/PublicDataStatus'
import ResponsiveImage from '../components/ResponsiveImage'
import SiteHeader from '../components/SiteHeader'
import ProjectTimeline from '../components/ui/timeline-01'
import { fetchPublicProjects } from '../lib/contentApi'
import { usePublicData } from '../lib/usePublicData'
import './ProjectsPage.css'

function ProjectsPage() {
  const projectData = usePublicData('projects', 300_000, fetchPublicProjects)
  const projects = projectData.data?.data

  useEffect(() => {
    document.title = 'Our Projects | Soaring Eagles'
    return () => {
      document.title = 'Soaring Eagles | Aerial Robotics Club'
    }
  }, [])

  return (
    <div className="projects-page">
      <SiteHeader />
      <main className="projects-main">
        <section className="projects-intro" aria-labelledby="projects-title">
          <p className="projects-kicker"><span aria-hidden="true" /> BUILDS / SOARING EAGLES</p>
          <h1 id="projects-title">Ideas built<br />to <em>take flight.</em></h1>
          <p className="projects-intro-copy">
            From autonomous aerial systems to aircraft built by hand, explore projects from across the club's journey.
          </p>
        </section>

        <section className="projects-directory" aria-labelledby="projects-directory-title">
          <div className="projects-directory-heading">
            <div>
              <p className="projects-timeline-label">PROJECT ARCHIVE</p>
              <h2 id="projects-directory-title">Built through <em>time.</em></h2>
            </div>
            <p>{projects ? `${projects.length} builds · newest first` : 'Newest first'}</p>
          </div>
          {!projects ? (
            <PublicDataStatus
              isLoading={projectData.isLoading}
              isRefreshing={projectData.isRefreshing}
              error={projectData.error}
              hasData={false}
              errorMessage="Projects could not be loaded. Please try again."
              onRetry={projectData.retry}
            />
          ) : (
            <>
              <PublicDataStatus
                isLoading={false}
                isRefreshing={projectData.isRefreshing}
                error={projectData.error}
                hasData
                errorMessage=""
                onRetry={projectData.retry}
              />
              <ProjectTimeline projects={projects} />
            </>
          )}
        </section>
      </main>

      <footer className="site-footer projects-footer">
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

export default ProjectsPage
