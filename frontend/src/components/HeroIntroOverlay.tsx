import { useEffect, useState } from 'react'
import type { RefObject } from 'react'

const loadingTips = [
  'Warming up the flight deck.',
  'Preparing the aircraft for takeoff.',
  'Every great flight starts with a careful check.',
]

export default function HeroIntroOverlay({ overlayRef }: { overlayRef: RefObject<HTMLDivElement> }) {
  const [currentTip, setCurrentTip] = useState(0)

  useEffect(() => {
    const timer = window.setInterval(() => {
      setCurrentTip((index) => (index + 1) % loadingTips.length)
    }, 3000)
    return () => window.clearInterval(timer)
  }, [])

  return (
    <div
      ref={overlayRef}
      className="hero-intro-overlay"
      role="status"
      aria-live="polite"
      aria-label="Preparing the Soaring Eagles experience"
    >
      <div className="hero-loading-panel">
        <span className="hero-loading-index" aria-hidden="true">SE / 01</span>
        <h2>PREPARING AIRCRAFT</h2>
        <div
          className="hero-loading-progress"
          role="progressbar"
          aria-label="Loading aircraft model"
          aria-valuetext="Loading"
        >
          <span />
        </div>
        <p className="hero-loading-caption">LOADING FLIGHT DECK</p>
        <p className="hero-loading-tip" aria-live="off">{loadingTips[currentTip]}</p>
      </div>
    </div>
  )
}
