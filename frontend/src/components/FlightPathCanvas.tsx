import { useEffect, useRef } from 'react'

export default function FlightPathCanvas() {
  const canvasRef = useRef<HTMLCanvasElement>(null)

  useEffect(() => {
    const canvas = canvasRef.current
    const context = canvas?.getContext('2d')
    const section = canvas?.parentElement
    if (!canvas || !context || !section) return

    const draw = () => {
      const { width, height } = section.getBoundingClientRect()
      if (!width || !height) return

      const pixelRatio = Math.min(window.devicePixelRatio || 1, 2)
      canvas.width = Math.round(width * pixelRatio)
      canvas.height = Math.round(height * pixelRatio)
      context.setTransform(pixelRatio, 0, 0, pixelRatio, 0, 0)
      context.clearRect(0, 0, width, height)

      const atmosphere = context.createRadialGradient(
        width * 0.78,
        height * 0.35,
        0,
        width * 0.78,
        height * 0.35,
        Math.max(width, height) * 0.65,
      )
      atmosphere.addColorStop(0, 'rgba(221, 151, 107, 0.075)')
      atmosphere.addColorStop(1, 'rgba(221, 151, 107, 0)')
      context.fillStyle = atmosphere
      context.fillRect(0, 0, width, height)

      context.save()
      context.translate(width * 0.79, height * 0.38)
      context.strokeStyle = 'rgba(221, 151, 107, 0.11)'
      context.lineWidth = 1
      for (let ring = 0; ring < 5; ring += 1) {
        context.beginPath()
        context.ellipse(0, 0, 70 + ring * 40, 35 + ring * 21, -0.22, 0, Math.PI * 2)
        context.stroke()
      }
      context.restore()

      context.setLineDash([2, 9])
      context.strokeStyle = 'rgba(221, 151, 107, 0.2)'
      context.beginPath()
      context.moveTo(0, height * 0.77)
      context.bezierCurveTo(width * 0.2, height * 0.2, width * 0.55, height * 0.95, width, height * 0.27)
      context.stroke()
      context.setLineDash([])

      context.strokeStyle = 'rgba(245, 240, 233, 0.13)'
      context.beginPath()
      context.moveTo(0, height * 0.83)
      context.bezierCurveTo(width * 0.24, height * 0.36, width * 0.58, height * 0.86, width, height * 0.39)
      context.stroke()

      for (let point = 0; point < 32; point += 1) {
        const x = ((point * 137.5) % 997) / 997 * width
        const y = ((point * 283.1) % 991) / 991 * height
        const radius = point % 7 === 0 ? 1.5 : 0.8
        context.beginPath()
        context.arc(x, y, radius, 0, Math.PI * 2)
        context.fillStyle = point % 7 === 0
          ? 'rgba(221, 151, 107, 0.58)'
          : 'rgba(245, 240, 233, 0.3)'
        context.fill()
      }
    }

    const observer = new ResizeObserver(draw)
    observer.observe(section)
    draw()

    return () => observer.disconnect()
  }, [])

  return <canvas ref={canvasRef} className="flight-path-canvas" aria-hidden="true" />
}
