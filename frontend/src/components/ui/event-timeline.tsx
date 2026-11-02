import { useLayoutEffect, useRef } from 'react'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import { pastEvents } from '../../data/events'
import useReducedMotion from '../../hooks/useReducedMotion'

gsap.registerPlugin(ScrollTrigger)

export default function EventTimeline() {
  const timelineRef = useRef<HTMLDivElement>(null)
  const reducedMotion = useReducedMotion()

  useLayoutEffect(() => {
    const timeline = timelineRef.current
    if (!timeline) return

    const images = Array.from(timeline.querySelectorAll('img'))
    const refresh = () => ScrollTrigger.refresh()
    const refreshFrame = window.requestAnimationFrame(refresh)

    const context = gsap.context(() => {
      const progressLine = timeline.querySelector<HTMLElement>('.event-timeline-progress')
      const cards = Array.from(timeline.querySelectorAll<HTMLElement>('.event-timeline-card'))

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
    <div className="event-timeline" ref={timelineRef}>
      <span className="event-timeline-track" aria-hidden="true" />
      <span className="event-timeline-progress" aria-hidden="true" />
      <ol className="event-timeline-list" aria-label="Past events, newest first">
        {pastEvents.map((event, index) => (
          <li className="event-timeline-item" key={event.title}>
            <div className="event-timeline-date">
              <span className="event-timeline-marker" aria-hidden="true" />
              <time dateTime={event.sortDate}>
                <span>{event.sortDate.slice(0, 4)}</span>
                <span>{event.date}</span>
              </time>
            </div>
            <article className="event-timeline-card">
              <div className="event-timeline-image">
                <img
                  src={event.image}
                  alt={`${event.title} event`}
                  loading={index === 0 ? 'eager' : 'lazy'}
                  decoding="async"
                />
                <span className="event-timeline-index" aria-hidden="true">
                  {String(index + 1).padStart(2, '0')}
                </span>
              </div>
              <div className="event-timeline-content">
                <p className="event-timeline-venue">{event.venue}</p>
                <h3>{event.title}</h3>
                <p>{event.about}</p>
                <p className="event-timeline-outcome">
                  <strong>Outcome</strong>
                  {event.outcome}
                </p>
              </div>
            </article>
          </li>
        ))}
      </ol>
    </div>
  )
}
