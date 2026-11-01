import { useCallback, useEffect, useRef, useState } from 'react'
import { gsap } from 'gsap'
import type { RefObject } from 'react'
import type { Group } from 'three'
import useReducedMotion from '../hooks/useReducedMotion'

const initialPhaseMs = 1200

export function useHeroReveal(
  heroRef: RefObject<HTMLElement | null>,
  headerRef: RefObject<HTMLElement | null>,
) {
  const overlayRef = useRef<HTMLDivElement>(null)
  const mountedAt = useRef(typeof performance === 'undefined' ? 0 : performance.now())
  const started = useRef(false)
  const timer = useRef<number | null>(null)
  const timeline = useRef<gsap.core.Timeline | null>(null)
  const [introDismissed, setIntroDismissed] = useState(false)
  const reducedMotion = useReducedMotion()

  const startReveal = useCallback((model?: Group, riseDistance = 0) => {
    if (started.current) return
    started.current = true

    const play = () => {
      const hero = heroRef.current
      const header = headerRef.current
      const overlay = overlayRef.current
      if (!hero || !header || !overlay) {
        setIntroDismissed(true)
        return
      }

      const title = hero.querySelector<HTMLElement>('[data-reveal="title"]')
      const primary = hero.querySelectorAll<HTMLElement>('[data-reveal="primary"]')
      const support = hero.querySelectorAll<HTMLElement>('[data-reveal="support"]')
      const interfaceElements = hero.querySelectorAll<HTMLElement>('[data-reveal="interface"]')
      const headerElements = header.querySelectorAll<HTMLElement>('[data-reveal]')
      const allElements = [title, ...primary, ...support, ...interfaceElements, ...headerElements]
        .filter((element): element is HTMLElement => element !== null)

      if (reducedMotion) {
        gsap.set(allElements, { clearProps: 'all' })
        setIntroDismissed(true)
        return
      }

      gsap.set(allElements, { autoAlpha: 0, y: 24 })

      const originalPosition = model?.position.clone()
      const originalScale = model?.scale.clone()
      if (model && originalPosition && originalScale) {
        gsap.set(model.position, {
          x: originalPosition.x,
          y: originalPosition.y - riseDistance,
          z: originalPosition.z,
        })
        gsap.set(model.scale, {
          x: originalScale.x * 0.88,
          y: originalScale.y * 0.88,
          z: originalScale.z * 0.88,
        })
      }

      const initialDelay = Math.max(0, initialPhaseMs - (performance.now() - mountedAt.current)) / 1000
      const droneStartsAt = initialDelay
      const titleStartsAt = droneStartsAt + 1.2
      const supportStartsAt = titleStartsAt + 0.36
      const interfaceStartsAt = droneStartsAt + 2.4
      const reveal = gsap.timeline()
      timeline.current = reveal

      reveal.to(overlay, {
        autoAlpha: 0,
        duration: 0.34,
        ease: 'power1.out',
        onComplete: () => setIntroDismissed(true),
      }, droneStartsAt)

      if (model && originalPosition && originalScale) {
        reveal.to(model.position, {
          x: originalPosition.x,
          y: originalPosition.y,
          z: originalPosition.z,
          duration: 1.2,
          ease: 'power3.out',
        }, droneStartsAt)
        reveal.to(model.scale, {
          x: originalScale.x * 1.025,
          y: originalScale.y * 1.025,
          z: originalScale.z * 1.025,
          duration: 1.2,
          ease: 'power3.out',
        }, droneStartsAt)
      }

      if (title) {
        reveal.to(title, { autoAlpha: 1, y: 0, duration: 0.9, ease: 'power3.out' }, titleStartsAt)
      }
      reveal.to(primary, { autoAlpha: 1, y: 0, duration: 0.7, stagger: 0.12, ease: 'power2.out' }, titleStartsAt + 0.12)
      reveal.to(support, { autoAlpha: 1, y: 0, duration: 0.65, stagger: 0.1, ease: 'power2.out' }, supportStartsAt)
      reveal.to(interfaceElements, {
        autoAlpha: 1,
        y: 0,
        duration: 0.65,
        stagger: 0.08,
        ease: 'power2.out',
      }, interfaceStartsAt)
      reveal.to(headerElements, {
        autoAlpha: 1,
        y: 0,
        duration: 0.7,
        stagger: 0.08,
        ease: 'power2.out',
      }, interfaceStartsAt)

      if (model && originalScale) {
        reveal.to(model.scale, {
          x: originalScale.x,
          y: originalScale.y,
          z: originalScale.z,
          duration: 0.7,
          ease: 'power2.out',
        }, interfaceStartsAt)
      }
    }

    const elapsed = performance.now() - mountedAt.current
    const remainingInitialPhase = reducedMotion ? 0 : Math.max(0, initialPhaseMs - elapsed)
    if (remainingInitialPhase === 0) {
      play()
    } else {
      timer.current = window.setTimeout(play, remainingInitialPhase)
    }
  }, [headerRef, heroRef, reducedMotion])

  useEffect(() => () => {
    if (timer.current !== null) window.clearTimeout(timer.current)
    timeline.current?.kill()
  }, [])

  return { overlayRef, introDismissed, startReveal }
}
