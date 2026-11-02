import { useEffect } from 'react'
import { Link } from 'react-router-dom'
import SiteHeader from '../components/SiteHeader'
import { teamSections } from '../data/team'
import './TeamPage.css'

function TeamPage() {
  useEffect(() => {
    document.title = 'Our Team | Soaring Eagles'
    return () => {
      document.title = 'Soaring Eagles | Aerial Robotics Club'
    }
  }, [])

  return (
    <div className="team-page">
      <SiteHeader />
      <main className="team-main">
        <section className="team-intro" aria-labelledby="team-title">
          <p className="team-kicker"><span aria-hidden="true" /> PEOPLE / SOARING EAGLES</p>
          <h1 id="team-title">The minds<br />behind <em>flight.</em></h1>
          <p className="team-intro-copy">
            Meet the students and faculty who turn engineering ideas into aircraft.
          </p>
        </section>

        <section className="team-directory" id="team-directory" aria-label="Team members by group">
          <div className="team-directory-heading">
            <div>
              <h2>Our <em>people.</em></h2>
            </div>
          </div>

          <div className="team-groups">
            {teamSections.map((section) => (
              <section className="team-group" id={section.id} key={section.id} aria-labelledby={`${section.id}-title`}>
                <div className="team-group-heading">
                  <h3 id={`${section.id}-title`}>{section.title}</h3>
                </div>
                {section.members.length > 0 ? (
                  <div className="team-member-grid">
                    {section.members.map((member) => (
                      <article className="team-member-card" key={member.name}>
                        <div className="team-member-photo">
                          <img src={member.image} alt={member.name} loading="lazy" decoding="async" />
                        </div>
                        <div className="team-member-info">
                          <div>
                            <h4>{member.name}</h4>
                            <p>{member.role}</p>
                          </div>
                          {(member.linkedin || member.instagram || member.x) && (
                            <div className="team-member-socials" aria-label={`${member.name}'s social profiles`}>
                              {member.linkedin && (
                                <a href={member.linkedin} target="_blank" rel="noreferrer" aria-label={`${member.name} on LinkedIn`}>
                                  in
                                </a>
                              )}
                              {member.instagram && (
                                <a href={member.instagram} target="_blank" rel="noreferrer" aria-label={`${member.name} on Instagram`}>
                                  ◎
                                </a>
                              )}
                              {member.x && (
                                <a href={member.x} target="_blank" rel="noreferrer" aria-label={`${member.name} on X`}>
                                  X
                                </a>
                              )}
                            </div>
                          )}
                        </div>
                      </article>
                    ))}
                  </div>
                ) : (
                  section.id === 'first-year' ? (
                    <div className="team-empty-state team-empty-state--recruiting">
                      <h4>Recruiting soon</h4>
                      <p>New first-year member profiles will appear here after recruitment.</p>
                    </div>
                  ) : (
                    <p className="team-empty-state">
                      No profiles are listed for this year group yet.
                    </p>
                  )
                )}
              </section>
            ))}
          </div>
        </section>
      </main>

      <footer className="site-footer team-footer">
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

export default TeamPage
