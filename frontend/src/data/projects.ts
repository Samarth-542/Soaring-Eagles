export interface Project {
  title: string
  year: number
  description: string
  image: string
  tags: string[]
}

export const projects: Project[] = [
  {
    title: 'Netra 2025',
    year: 2025,
    description: 'This H-frame quadcopter was developed for autonomous aerial scanning missions. It features a Raspberry Pi 5 for onboard image processing and a Pixhawk flight controller for stable autonomous navigation. The drone can take off, perform scans, and land autonomously within a geofenced area. Built for precision and reliability, it is ideal for mapping, inspection, and disaster assessment applications.',
    image: '/images/projects/netra-2025.jpg',
    tags: ['Quadcopter', 'Autonomous', 'Aerial scanning'],
  },
  {
    title: 'Vitrak 2025',
    year: 2025,
    description: 'This X-frame quadcopter is designed for autonomous payload delivery using GPS navigation. Equipped with a Pixhawk controller and onboard sensors, it can take off, fly to a target location, and deliver payloads with high accuracy. A Raspberry Pi module handles mission logic and communication. The system ensures fully automated operation from launch to safe return, ideal for disaster relief and logistics tasks.',
    image: '/images/projects/vitrak-2025.jpg',
    tags: ['Quadcopter', 'Autonomous', 'Payload delivery'],
  },
  {
    title: 'Gajraj 09',
    year: 2024,
    description: 'This fixed-wing RC plane was built for a prestigious TechFest competition at IIT Bombay, where the team secured 10th rank among 250+ teams. Designed for high lift, stability, and efficient flight performance, it excelled in both endurance and payload missions. The aircraft was entirely student-designed and fabricated, showcasing innovation in aerodynamics and control. It stands as a testament to the team’s engineering excellence and dedication to aeromodelling.',
    image: '/images/projects/gajraj-09-2024.png',
    tags: ['IIT Bombay', 'TechFest', 'Fixed wing'],
  },
  {
    title: 'Gajraj 72',
    year: 2024,
    description: 'This fixed-wing RC plane was built for a prestigious TechFest competition at IIT Bombay, where the team secured 15th rank among 250+ teams. Designed for high lift, stability, and efficient flight performance, it excelled in both endurance and payload missions. The aircraft was entirely student-designed and fabricated, showcasing innovation in aerodynamics and control. It stands as a testament to the team’s engineering excellence and dedication to aeromodelling.',
    image: '/images/projects/gajraj-72-2024.jpg',
    tags: ['IIT Bombay', 'TechFest', 'Fixed wing'],
  },
  {
    title: 'Prayas',
    year: 2024,
    description: 'This trainer RC plane was built to teach freshers of 2024-25 the basics of aerodynamics and maneuverability. Lightweight and stable, it helps beginners understand lift, drag, and control response through hands-on flying. The project aims to build foundational skills in aeromodelling and UAV design.',
    image: '/images/projects/prayas-2024.jpg',
    tags: ['Aerodynamics', 'Training', 'Maneuverability'],
  },
  {
    title: 'Abhyas',
    year: 2024,
    description: 'This RC plane was built by the team for the Wright Flight competition at TechFest NITK 2024. Designed and fabricated from scratch, it focused on aerodynamic efficiency and payload stability. The model showcased precise control, endurance, and innovative design. It marked a proud achievement for the Soaring Eagles Aeromodelling Team.',
    image: '/images/projects/abhyas-2024.png',
    tags: ['Fixed wing', 'Aerodynamics', 'Competition'],
  },
  {
    title: 'Ornithopter',
    year: 2021,
    description: 'A bird-like flying machine that flaps its wings to generate lift and thrust. It was built in 2021 as part of an experimental aero-design project focused on biomimetic flight. The structure uses lightweight materials such as carbon fiber and thin polymer film for efficient flapping. Its motion is powered by a compact servo-motor mechanism controlled via remote or onboard electronics. The project demonstrates how nature-inspired flight dynamics can be applied in robotics and drone technology.',
    image: '/images/projects/ornithopter-2021.jpg',
    tags: ['Biomimetic flight', 'Aero innovation', 'Aerial mechanics'],
  },
  {
    title: 'Hexacopter Drone',
    year: 2017,
    description: 'A six-rotor unmanned aerial vehicle designed for stable and powerful flight. It was built in 2017 as part of an engineering project focusing on multirotor control and payload capacity. The drone’s carbon-fiber frame and brushless DC motors provide durability and efficient lift. Its flight is managed by an onboard flight controller system integrated with GPS and IMU sensors. This project demonstrates advancements in aerial robotics and autonomous flight technology.',
    image: '/images/projects/hexacopter-2017.jpg',
    tags: ['Telemetry', 'GPS', 'Stable flight'],
  },
  {
    title: 'Fixed-Wing RC Plane - Manove Gam',
    year: 2017,
    description: 'This image shows a fixed-wing RC aircraft developed during the Manove Gam workshop in 2017. It features a monoplane design with a lightweight fuselage and a front-mounted propeller for efficient thrust. The aircraft was built to study aerodynamic stability, lift generation, and control surface dynamics. Constructed using balsa wood and composite materials, it balances strength with minimal weight. This project marked an important step in hands-on aeromodelling and flight mechanics training for participants.',
    image: '/images/projects/manove-gam-2017.jpg',
    tags: ['Workshop', 'Fixed wing', 'Aerodynamics'],
  },
]
