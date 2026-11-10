import { useLayoutEffect, useRef } from 'react'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import type { JourneyMilestone } from '../../lib/contentApi'
import useReducedMotion from '../../hooks/useReducedMotion'
import ResponsiveImage from '../ResponsiveImage'

gsap.registerPlugin(ScrollTrigger)

export default function JourneyTimeline({ milestones }: { milestones: JourneyMilestone[] }) {
  const timelineRef = useRef<HTMLDivElement>(null)
  const reducedMotion = useReducedMotion()

  useLayoutEffect(() => {
    const timeline = timelineRef.current
    if (!timeline) return

    const images = Array.from(timeline.querySelectorAll('img'))
    const refresh = () => ScrollTrigger.refresh()
    const refreshFrame = window.requestAnimationFrame(refresh)

    const context = gsap.context(() => {
      const progressLine = timeline.querySelector<HTMLElement>('.journey-timeline-progress')
      const cards = Array.from(timeline.querySelectorAll<HTMLElement>('.journey-timeline-card'))

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
              start: 'top 68%',
              end: 'bottom 60%',
              scrub: 0.6,
              invalidateOnRefresh: true,
            },
          },
        )
      }

      cards.forEach((card, index) => {
        gsap.fromTo(
          card,
          { autoAlpha: 0, x: index % 2 === 0 ? 32 : -32, y: 20 },
          {
            autoAlpha: 1,
            x: 0,
            y: 0,
            duration: 0.7,
            ease: 'power3.out',
            scrollTrigger: {
              trigger: card,
              start: 'top 84%',
              toggleActions: 'play none none reverse',
              invalidateOnRefresh: true,
            },
          },
        )
      })

      images.forEach((image) => image.addEventListener('load', refresh, { once: true }))
    }, timeline)

    return () => {
      window.cancelAnimationFrame(refreshFrame)
      images.forEach((image) => image.removeEventListener('load', refresh))
      context.revert()
    }
  }, [reducedMotion])

  return (
    <div className="journey-timeline" ref={timelineRef}>
      <span className="journey-timeline-track" aria-hidden="true" />
      <span className="journey-timeline-progress" aria-hidden="true" />
      <ol className="journey-timeline-list" aria-label="Journey milestones, newest first">
        {milestones.map((milestone, index) => (
          <li className="journey-timeline-item" key={milestone.id}>
            <div className="journey-timeline-date">
              <span className="journey-timeline-marker" aria-hidden="true" />
              <time dateTime={String(milestone.year)}>{milestone.year}</time>
            </div>
            <article className="journey-timeline-card">
              <div className="journey-timeline-image">
                <ResponsiveImage
                  src={milestone.imageUrl}
                  alt={`${milestone.title} milestone`}
                  loading={index === 0 ? 'eager' : 'lazy'}
                />
                <span className="journey-timeline-index" aria-hidden="true">
                  {String(index + 1).padStart(2, '0')}
                </span>
              </div>
              <div className="journey-timeline-content">
                <p className="journey-timeline-label">FLIGHT LOG / {milestone.year}</p>
                <h2>{milestone.title}</h2>
                <p>{milestone.description}</p>
              </div>
            </article>
          </li>
        ))}
      </ol>
    </div>
  )
}
