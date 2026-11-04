import { Component, Suspense, useEffect, useMemo, useRef, useState } from 'react'
import type { ErrorInfo, MutableRefObject, ReactNode, RefObject } from 'react'
import { Canvas, useFrame, useThree } from '@react-three/fiber'
import { Html, OrbitControls, useGLTF } from '@react-three/drei'
import { Box3, MathUtils, Mesh, PerspectiveCamera, Vector2, Vector3 } from 'three'
import type { Group } from 'three'
import useReducedMotion from './hooks/useReducedMotion'

const modelPath = '/models/assault_drone_concept.glb'
const modelScale = 1.95
const defaultDronePitch = -0.22
type OnModelReady = (model?: Group, riseDistance?: number) => void
const viewDirection = new Vector3(0, 0.3, 1).normalize()
const viewUp = new Vector3(0, 1, 0)
  .addScaledVector(viewDirection, -viewDirection.y)
  .normalize()
const viewRight = new Vector3().crossVectors(viewUp, viewDirection).normalize()

function Model({
  modelRef,
  pointerPosition,
  rotationOffset,
  onModelReady,
}: {
  modelRef: RefObject<Group | null>
  pointerPosition: MutableRefObject<Vector2>
  rotationOffset: MutableRefObject<Vector2>
  onModelReady: (model: Group, riseDistance: number) => void
}) {
  const { scene } = useGLTF(modelPath)
  const { camera, size } = useThree()
  const hasReportedReady = useRef(false)
  const model = useMemo(() => {
    const clone = scene.clone(true)
    clone.rotation.x = defaultDronePitch
    return clone
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

  useFrame((_, delta) => {
    const drone = modelRef.current
    if (!drone) return

    drone.rotation.x = MathUtils.damp(
      drone.rotation.x,
      defaultDronePitch + pointerPosition.current.y * 0.18,
      6,
      delta,
    )
    drone.rotation.y = MathUtils.damp(
      drone.rotation.y,
      rotationOffset.current.y + pointerPosition.current.x * 0.32,
      6,
      delta,
    )
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
  pointerPosition,
  rotationOffset,
  reducedMotion,
  onModelReady,
}: {
  modelRef: RefObject<Group | null>
  pointerPosition: MutableRefObject<Vector2>
  rotationOffset: MutableRefObject<Vector2>
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
        <Model
          modelRef={modelRef}
          pointerPosition={pointerPosition}
          rotationOffset={rotationOffset}
          onModelReady={onModelReady}
        />
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

export default function DroneExperience({
  onModelReady,
  pointerTarget,
}: {
  onModelReady: OnModelReady
  pointerTarget: RefObject<HTMLElement | null>
}) {
  const [webglAvailable, setWebglAvailable] = useState<boolean | null>(null)
  const modelRef = useRef<Group>(null)
  const pointerPosition = useRef(new Vector2())
  const rotationOffset = useRef(new Vector2())
  const reducedMotion = useReducedMotion()
  const fallbackReported = useRef(false)

  useEffect(() => {
    const element = pointerTarget.current
    if (!element) return

    const handlePointerMove = (event: PointerEvent) => {
      if (event.pointerType === 'touch') return

      const bounds = element.getBoundingClientRect()
      if (!bounds.width || !bounds.height) return

      pointerPosition.current.set(
        MathUtils.clamp(((event.clientX - bounds.left) / bounds.width) * 2 - 1, -1, 1),
        MathUtils.clamp(((event.clientY - bounds.top) / bounds.height) * 2 - 1, -1, 1),
      )
    }
    const handlePointerLeave = () => pointerPosition.current.set(0, 0)

    element.addEventListener('pointermove', handlePointerMove)
    element.addEventListener('pointerleave', handlePointerLeave)
    return () => {
      element.removeEventListener('pointermove', handlePointerMove)
      element.removeEventListener('pointerleave', handlePointerLeave)
    }
  }, [pointerTarget])

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
    rotationOffset.current.y += direction * 0.35
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
                <Scene
                  modelRef={modelRef}
                  pointerPosition={pointerPosition}
                  rotationOffset={rotationOffset}
                  reducedMotion={reducedMotion}
                  onModelReady={onModelReady}
                />
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
