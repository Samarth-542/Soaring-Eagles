export interface ClubEvent {
  title: string
  date: string
  sortDate: string
  venue: string
  about: string
  outcome: string
  image: string
}

export const pastEvents: ClubEvent[] = [
  {
    title: 'Lift-Off Workshop',
    date: '22 October 2025',
    sortDate: '2025-10-22',
    venue: 'SIT, Tumakuru, Karnataka',
    about: 'A hands-on workshop introducing first- and second-year students to aerodynamics and RC plane design. Participants explored aerospace fundamentals, built gliders, and flight-tested them.',
    outcome: 'Helped students gain a foundation in flight mechanics and teamwork while sparking interest in aerial robotics.',
    image: '/images/events/liftoff.png',
  },
  {
    title: 'Technical Presentation with IIC SIT',
    date: '13 May 2025',
    sortDate: '2025-05-13',
    venue: 'SIT, Tumakuru, Karnataka',
    about: 'A technical presentation organized by the Institution’s Innovation Council at SIT for Innovation Day, sharing student projects with IIC members and faculty.',
    outcome: 'Presented the design, development, and applications of the club’s UAV project and received feedback from IIC members.',
    image: '/images/events/pastIIC.png',
  },
  {
    title: 'Siddaganga Agricultural and Industrial Exhibition',
    date: '17 February – 3 March 2025',
    sortDate: '2025-02-17',
    venue: 'Siddaganga Math, Tumakuru, Karnataka',
    about: 'A public exhibition showcasing innovations in agriculture and industry.',
    outcome: 'Presented the club’s UAV project and demonstrated its applications in precision farming and aerial surveying.',
    image: '/images/events/pastMutt.png',
  },
  {
    title: 'SAE Collegiate Meet',
    date: '6 November 2024',
    sortDate: '2024-11-06',
    venue: 'SIT, Tumakuru, Karnataka',
    about: 'An introductory session welcoming freshers to SAE and its collegiate clubs, with an overview of hands-on projects, competitions, and engineering opportunities.',
    outcome: 'Introduced students to SAE’s mission, club structure, technical domains, and opportunities for practical engineering experience.',
    image: '/images/events/pastCollegiate.png',
  },
]
