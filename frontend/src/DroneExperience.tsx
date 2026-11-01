import { Component, Suspense, useEffect, useMemo, useRef, useState } from 'react'
import type { ErrorInfo, ReactNode, RefObject } from 'react'
import { Canvas, useFrame, useThree } from '@react-three/fiber'
import { Html, OrbitControls, useGLTF } from '@react-three/drei'
import { Box3, Mesh, PerspectiveCamera, Vector3 } from 'three'
import type { Group } from 'three'
import useReducedMotion from './hooks/useReducedMotion'

const modelPath = '/models/assault_drone_concept.glb'
const modelScale = 1.95
type OnModelReady = (model?: Group, riseDistance?: number) => void
const viewDirection = new Vector3(0, 0.3, 1).normalize()
const viewUp = new Vector3(0, 1, 0)
  .addScaledVector(viewDirection, -viewDirection.y)
  .normalize()
const viewRight = new Vector3().crossVectors(viewUp, viewDirection).normalize()

function Model({
  modelRef,
  onModelReady,
}: {
  modelRef: RefObject<Group | null>
  onModelReady: (model: Group, riseDistance: number) => void
}) {
  const { scene } = useGLTF(modelPath)
  const { camera, size } = useThree()
  const hasReportedReady = useRef(false)
  const model = useMemo(() => {
    return scene.clone(true)
  }, [scene])
  const bounds = useMemo(() => {
    model.updateMatrixWorld(true)
    const box = new Box3()
    model.traverseVisible((object) => {
      if (object instanceof Mesh) box.union(new Box3().setFromObject(object))
    })
    if (box.isEmpty()) throw new Error('The drone model has no visible geometry.')
    const center = box.getCenter(new Vector3())
    const fitCorners: Vector3[] = []
    for (let corner = 0; corner < 8; corner += 1) {
      fitCorners.push(new Vector3(
        corner & 1 ? box.max.x : box.min.x,
        corner & 2 ? box.max.y : box.min.y,
        corner & 4 ? box.max.z : box.min.z,
      ).sub(center))
    }
    return {
      center,
      fitCorners,
    }
  }, [model])
  const perspectiveCamera = camera as PerspectiveCamera
  const frame = useMemo(() => {
    const verticalHalfFov = (perspectiveCamera.fov * Math.PI) / 360
    const horizontalHalfFov = Math.atan(Math.tan(verticalHalfFov) * (size.width / size.height))
    let distance = 0
    for (const corner of bounds.fitCorners) {
      const depth = corner.dot(viewDirection) * modelScale
      const horizontalExtent = Math.abs(corner.dot(viewRight)) * modelScale
      const verticalExtent = Math.abs(corner.dot(viewUp)) * modelScale
      distance = Math.max(
        distance,
        depth + horizontalExtent / Math.tan(horizontalHalfFov),
        depth + verticalExtent / Math.tan(verticalHalfFov),
      )
    }
    distance *= 1.12
    const centerShift = size.height * (size.width <= 760 ? 0.02 : size.width <= 1050 ? 0.12 : 0.17)
    const focalLength = size.height / (2 * Math.tan(verticalHalfFov))
    const verticalOffset = (centerShift * distance) / (focalLength * viewUp.y)
    return {
      position: viewDirection.clone().multiplyScalar(distance),
      verticalOffset,
      visibleHeight: distance * 2 * Math.tan(verticalHalfFov),
    }
  }, [bounds.fitCorners, perspectiveCamera.fov, size.height, size.width])

  useEffect(() => {
    perspectiveCamera.position.copy(frame.position)
    perspectiveCamera.lookAt(0, 0, 0)
    perspectiveCamera.updateProjectionMatrix()
  }, [camera, frame, perspectiveCamera])

  useFrame(() => {
    if (hasReportedReady.current) return
    hasReportedReady.current = true
    onModelReady(model, frame.visibleHeight * 0.72)
  })

  return (
    <primitive
      ref={modelRef}
      object={model}
      scale={modelScale}
      position={[
        -bounds.center.x * modelScale,
        -bounds.center.y * modelScale - frame.verticalOffset,
        -bounds.center.z * modelScale,
      ]}
    />
  )
}

function Scene({
  modelRef,
  reducedMotion,
  onModelReady,
}: {
  modelRef: RefObject<Group | null>
  reducedMotion: boolean
  onModelReady: (model: Group, riseDistance: number) => void
}) {
  return (
    <>
      <ambientLight intensity={1.8} />
      <directionalLight position={[4, 5, 6]} intensity={4} />
      <directionalLight position={[-5, 1, -3]} intensity={2.8} color="#83c9e8" />
      <spotLight position={[0, 5, -4]} intensity={18} angle={0.65} penumbra={1} />
      <Suspense fallback={<Html center><span className="scene-loading">PREPARING AIRCRAFT</span></Html>}>
        <Model modelRef={modelRef} onModelReady={onModelReady} />
      </Suspense>
      <OrbitControls
        enablePan={false}
        enableZoom={false}
        minPolarAngle={Math.PI * 0.12}
        maxPolarAngle={Math.PI * 0.68}
        rotateSpeed={0.55}
        dampingFactor={0.08}
        enableDamping={!reducedMotion}
      />
    </>
  )
}

class SceneErrorBoundary extends Component<{
  children: ReactNode
  fallback: ReactNode
  onModelReady: OnModelReady
}, { failed: boolean }> {
  state = { failed: false }

  static getDerivedStateFromError() {
    return { failed: true }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('Drone scene could not be rendered.', error, info.componentStack)
    this.props.onModelReady()
  }

  render() {
    return this.state.failed ? this.props.fallback : this.props.children
  }
}

export default function DroneExperience({ onModelReady }: { onModelReady: OnModelReady }) {
  const [webglAvailable, setWebglAvailable] = useState<boolean | null>(null)
  const modelRef = useRef<Group>(null)
  const reducedMotion = useReducedMotion()
  const fallbackReported = useRef(false)

  useEffect(() => {
    try {
      const canvas = document.createElement('canvas')
      const context = canvas.getContext('webgl2') ?? canvas.getContext('webgl')
      setWebglAvailable(Boolean(context))
      context?.getExtension('WEBGL_lose_context')?.loseContext()
    } catch (error) {
      console.error('Unable to check WebGL support.', error)
      setWebglAvailable(false)
    }
  }, [])

  useEffect(() => {
    if (webglAvailable !== false || fallbackReported.current) return
    fallbackReported.current = true
    onModelReady()
  }, [onModelReady, webglAvailable])

  const rotate = (direction: -1 | 1) => {
    if (modelRef.current) modelRef.current.rotation.y += direction * 0.35
  }

  return (
    <div className="drone-experience">
      <div className="model-stage" role="group" aria-label="Interactive drone display">
        {webglAvailable === false ? (
          <div className="model-fallback" role="status">
            <img src="/images/brand/hero-drones.jpg" alt="" />
            <p>Interactive 3D is unavailable in this browser.</p>
          </div>
        ) : (
          <SceneErrorBoundary
            onModelReady={onModelReady}
            fallback={
              <div className="model-fallback" role="status">
                <img src="/images/brand/hero-drones.jpg" alt="" />
                <p>Interactive 3D is unavailable right now.</p>
              </div>
            }
          >
            {webglAvailable === null ? (
              <div className="scene-loading" role="status">CHECKING 3D SUPPORT</div>
            ) : (
              <Canvas
                camera={{ position: [0, 1.4, 7], fov: 34 }}
                dpr={[1, 1.5]}
                gl={{ antialias: true, alpha: true, powerPreference: 'low-power' }}
                aria-label="3D drone model. Drag to rotate."
              >
                <Scene modelRef={modelRef} reducedMotion={reducedMotion} onModelReady={onModelReady} />
              </Canvas>
            )}
          </SceneErrorBoundary>
        )}
      </div>

      {webglAvailable && (
        <div className="model-controls" data-reveal="interface" role="group" aria-label="Rotate drone model">
          <button type="button" onClick={() => rotate(-1)} aria-label="Rotate model left">←</button>
          <span>{reducedMotion ? 'DRAG TO ROTATE' : 'DRAG TO EXPLORE'}</span>
          <button type="button" onClick={() => rotate(1)} aria-label="Rotate model right">→</button>
        </div>
      )}
    </div>
  )
}

useGLTF.preload(modelPath)
