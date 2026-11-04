import { useLayoutEffect, useRef } from 'react'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import type { Project } from '../../lib/contentApi'
import useReducedMotion from '../../hooks/useReducedMotion'

gsap.registerPlugin(ScrollTrigger)

export default function ProjectTimeline({ projects }: { projects: Project[] }) {
  const timelineRef = useRef<HTMLDivElement>(null)
  const reducedMotion = useReducedMotion()

  useLayoutEffect(() => {
    const timeline = timelineRef.current
    if (!timeline) return

    const context = gsap.context(() => {
      const progressLine = timeline.querySelector<HTMLElement>('.project-timeline-progress')
      const cards = Array.from(timeline.querySelectorAll<HTMLElement>('.project-timeline-card'))

      if (reducedMotion) {
        gsap.set([progressLine, ...cards], { clearProps: 'all' })
        return
      }

      if (progressLine) {
        gsap.fromTo(
          progressLine,
          { scaleY: 0 },
          {
            scaleY: 1,
            ease: 'none',
            scrollTrigger: {
              trigger: timeline,
              start: 'top 65%',
              end: 'bottom 65%',
              scrub: 0.6,
              invalidateOnRefresh: true,
            },
          },
        )
      }

      cards.forEach((card) => {
        const fromX = card.closest('.project-timeline-item--left') ? -36 : 36
        gsap.fromTo(
          card,
          { autoAlpha: 0, x: fromX, y: 24 },
          {
            autoAlpha: 1,
            x: 0,
            y: 0,
            duration: 0.75,
            ease: 'power3.out',
            scrollTrigger: {
              trigger: card,
              start: 'top 82%',
              toggleActions: 'play none none reverse',
              invalidateOnRefresh: true,
            },
          },
        )
      })
    }, timeline)

    return () => context.revert()
  }, [reducedMotion])

  return (
    <div className="project-timeline" ref={timelineRef}>
      <span className="project-timeline-track" aria-hidden="true" />
      <span className="project-timeline-progress" aria-hidden="true" />
      <ol className="project-timeline-list" aria-label="Projects in reverse chronological order">
        {projects.map((project, index) => {
          const side = index % 2 === 0 ? 'left' : 'right'

          return (
            <li className={`project-timeline-item project-timeline-item--${side}`} key={project.id}>
              <span className="project-timeline-marker" aria-hidden="true" />
              <article className="project-timeline-card">
                <div className="project-timeline-image">
                  <img
                    src={project.imageUrl}
                    alt={`${project.title} aircraft`}
                    loading="lazy"
                    decoding="async"
                  />
                  <span className="project-timeline-index" aria-hidden="true">
                    {String(index + 1).padStart(2, '0')}
                  </span>
                  <time className="project-timeline-year" dateTime={String(project.year)}>
                    {project.year}
                  </time>
                </div>
                <div className="project-timeline-content">
                  <h3>{project.title}</h3>
                  <p>{project.description}</p>
                  <ul className="project-timeline-tags" aria-label={`${project.title} topics`}>
                    {project.tags.map((tag) => <li key={tag}>{tag}</li>)}
                  </ul>
                </div>
              </article>
            </li>
          )
        })}
      </ol>
    </div>
  )
}
