export interface TeamMember {
  name: string
  role: string
  image: string
  linkedin?: string
  instagram?: string
  x?: string
}

interface TeamSection {
  id: string
  title: string
  label: string
  members: TeamMember[]
}

export const teamSections: TeamSection[] = [
  {
    id: 'faculty',
    title: 'Faculty Coordinators',
    label: 'FACULTY',
    members: [
      {
        name: 'Dr Shivasiddaramaiah A G',
        role: 'Department of Mechanical Engineering',
        image: '/images/team/fcAGS.jpg',
      },
      {
        name: 'Dr Sunitha. N R',
        role: 'Department of Computer Science and Engineering',
        image: '/images/team/fcSNR.jpeg',
      },
    ],
  },
  {
    id: 'alumni',
    title: 'Alumni',
    label: 'ALUMNI',
    members: [
      {
        name: 'Shivam Deo',
        role: 'President',
        image: '/images/team/4Shivam Deo.jpg',
        linkedin: 'https://www.linkedin.com/in/shivam-deo',
        x: 'https://x.com/_shivam_deo_',
      },
      {
        name: 'Mohammad Yasir Abbasi',
        role: 'Vice-President',
        image: '/images/team/4Yasir Abbasi.jpeg',
        linkedin: 'https://www.linkedin.com/in/mohammad-yasir-abbasi-27b918259',
      },
      {
        name: 'Mihika Pallavi',
        role: 'Treasurer',
        image: '/images/team/4Mihika.jpg',
        linkedin: 'https://www.linkedin.com/in/mihika-pallavi22',
        x: 'https://x.com/Mihika_022',
      },
      {
        name: 'Aditya D V',
        role: 'Automation Head',
        image: '/images/team/4Aditya D V.jpeg',
        linkedin: 'https://www.linkedin.com/in/aditya-d-v-740ba7259',
        x: 'https://x.com/AdityaDV2',
      },
      {
        name: 'Sumanth Gowda',
        role: 'Design Head',
        image: '/images/team/4Sumanth (1).jpg',
        linkedin: 'https://www.linkedin.com/in/sumanth-gowda-s-l-a23b5b200',
        x: 'https://x.com/Sumanth_10101',
      },
    ],
  },
  {
    id: 'fourth-year',
    title: '4th Year',
    label: '04 / YEAR',
    members: [
      {
        name: 'Siddith R Shetty',
        role: 'Automation',
        image: '/images/team/3SiddithRShetty.jpg',
        linkedin: 'https://www.linkedin.com/in/siddith01',
        x: 'https://x.com/Siddith3',
      },
      {
        name: 'Prasun',
        role: 'Automation',
        image: '/images/team/3Prasun.jpg',
        linkedin: 'https://www.linkedin.com/in/prasun-nandikol-400359292',
        x: 'https://x.com/prasun015',
      },
      {
        name: 'H S Shreyas',
        role: 'Automation',
        image: '/images/team/3hsshreyas.jpg',
        linkedin: 'https://www.linkedin.com/in/hsshreyas',
        x: 'https://x.com/hsshreyas11',
      },
      {
        name: 'Jagadeep Reddy',
        role: 'Automation',
        image: '/images/team/3 jagadeep reddy.jpg',
        linkedin: 'https://www.linkedin.com/in/devireddy-venkata-jagadeep-reddy-760029284',
        x: 'https://x.com/Itzz_jagadeep',
      },
      {
        name: 'Yemula VenkatKarthikeyan',
        role: 'Automation',
        image: '/images/team/3 Yemula VenkatKarthikeyan.jpg',
        linkedin: 'https://www.linkedin.com/in/venkat-karthikeyan-587a1b272',
        x: 'https://x.com/VenkatKart39190',
      },
      {
        name: 'Himanshu Aditya',
        role: 'Avionics',
        image: '/images/team/3Himanshu.jpg',
        linkedin: 'https://www.linkedin.com/in/himanshu-aditya-ba018b339',
        x: 'https://x.com/HIMANSHUADITY10',
      },
      {
        name: 'Pratham Srivastava',
        role: 'Avionics',
        image: '/images/team/3.pratham.jpg',
        linkedin: 'https://www.linkedin.com/in/pratham-srivastava-a49712283',
        x: 'https://x.com/PrathamSri19264',
      },
      {
        name: 'Shivansh Saurav',
        role: 'Avionics',
        image: '/images/team/3ShivanshSaurav.jpg',
        x: 'https://x.com/PropnoaZoro',
      },
      {
        name: 'Yashas N C',
        role: 'Avionics',
        image: '/images/team/3Yashas .N.C.jpg',
        linkedin: 'https://www.linkedin.com/in/yashas-n-c',
        x: 'https://x.com/yashas_ync',
      },
      {
        name: 'Lekha Kumaraswamy',
        role: 'Aerodynamics',
        image: '/images/team/3Lekha.jpg',
        linkedin: 'https://www.linkedin.com/in/lekha-kumaraswamy-2a6b79334',
        x: 'https://x.com/Lekha1511',
      },
      {
        name: 'Pranav A Uppoor',
        role: 'Aerodynamics',
        image: '/images/team/3Pranav.jpeg',
      },
      {
        name: 'Shreyas C S',
        role: 'Aerodynamics',
        image: '/images/team/3ShreyasCS.jpg',
        x: 'https://x.com/ShreyasCS12',
      },
    ],
  },
  {
    id: 'third-year',
    title: '3rd Year',
    label: '03 / YEAR',
    members: [
      {
        name: 'Adhirath Kyatarmal',
        role: 'Automation',
        image: '/images/team/2Adhirath Katyarmal.jpg',
        linkedin: 'https://www.linkedin.com/in/adhirath-k-9581b2341',
      },
      {
        name: 'Aryan Gupta',
        role: 'Automation',
        image: '/images/team/2AryanGupta.jpg',
        linkedin: 'https://www.linkedin.com/in/aryan-gupta-4893b6329',
        x: 'https://x.com/aryan20242011',
      },
      {
        name: 'Aditya Mallapur',
        role: 'Avionics',
        image: '/images/team/2Aditya MAllapur.jpg',
        linkedin: 'https://www.linkedin.com/in/aditya-mallapur-407917342',
        x: 'https://x.com/GKWDIEH',
      },
      {
        name: 'Samarth Gupta',
        role: 'Automation',
        image: '/images/team/samarth-gupta.jpg',
        linkedin: 'https://www.linkedin.com/in/samarth-gupta-7ab08a331',
      },
      {
        name: 'DEEPAK PUROHIT',
        role: 'Automation',
        image: '/images/team/deepak-purohit.jpeg',
        linkedin: 'https://www.linkedin.com/in/deepak-purohit-894984322/',
        instagram: 'https://www.instagram.com/deep_purohit16/',
        x: 'https://x.com/DeepakP70669756',
      },
      {
        name: 'Mourya N',
        role: 'Aerodynamics',
        image: '/images/team/mourya-n.jpg',
        linkedin: 'https://www.linkedin.com/in/mouryan13',
      },
      {
        name: 'Mohammed Mehran Masih',
        role: 'Avionics',
        image: '/images/team/mohammed-mehran-masih.jpg',
        linkedin: 'https://www.linkedin.com/in/mohammed-mehran-masih-047601328',
        instagram: 'https://www.instagram.com/mehxxn.25',
      },
      {
        name: 'Kalavati Tadasur',
        role: 'Aerodynamics',
        image: '/images/team/kalavati-tadasur.png',
        linkedin: 'https://www.linkedin.com/in/kalavati-tadasur-b23541330',
        instagram: 'https://www.instagram.com/kalavati_2006',
        x: 'https://x.com/Kalavati_21',
      },
      {
        name: 'Sounak Ghosh',
        role: 'Avionics',
        image: '/images/team/sounak-ghosh.jpg',
      },
      {
        name: 'Amartya R',
        role: 'Aerodynamics',
        image: '/images/team/amartya-r.png',
      },
      {
        name: 'M S Rakshith',
        role: 'Automation',
        image: '/images/team/ms-rakshith.jpg',
      },
    ],
  },
  {
    id: 'second-year',
    title: '2nd Year',
    label: '02 / YEAR',
    members: [
      {
        name: 'Arunish Rajput',
        role: 'Automation • Avionics',
        image: '/images/team/arunish-rajput.jpg',
        linkedin: 'https://www.linkedin.com/in/arunishrajput/',
        instagram: 'https://www.instagram.com/arunishrajput',
        x: 'https://x.com/arunishrajput',
      },
      {
        name: 'Yashas Wali',
        role: 'Aerodynamics • Automation • Avionics',
        image: '/images/team/yashas-wali.jpg',
        linkedin: 'https://www.linkedin.com/in/yashas-w-19203b2a1',
      },
      {
        name: 'Chaitanya Ravi',
        role: 'Aerodynamics',
        image: '/images/team/chaitanya-ravi.jpeg',
        linkedin: 'https://www.linkedin.com/in/chaitanya-ravi-b69992247/',
      },
      {
        name: 'Harsh Sharma',
        role: 'Avionics',
        image: '/images/team/harsh-sharma.jpg',
        linkedin: 'https://www.linkedin.com/in/iamharsharma',
        instagram: 'https://instagram.com/har_sh_arma_17',
      },
      {
        name: 'Impana V',
        role: 'Automation',
        image: '/images/team/impana-v.jpg',
        linkedin: 'https://www.linkedin.com/in/impana-v-52a4573b6',
        instagram: 'https://www.instagram.com/_impu__2923',
      },
      {
        name: 'Supreeth Kumar',
        role: 'Aerodynamics • Avionics',
        image: '/images/team/supreeth-kumar.jpg',
        linkedin: 'https://www.linkedin.com/in/supreeth-kumar-804455302',
        instagram: 'https://www.instagram.com/supreeth_kumar.28',
        x: 'https://x.com/Supreethku85218',
      },
      {
        name: 'Rucha Patwardhan',
        role: 'Aerodynamics',
        image: '/images/team/rucha-patwardhan.jpg',
        linkedin: 'https://www.linkedin.com/in/rucha-patwardhan-029593378',
        instagram: 'https://www.instagram.com/rucha_p26',
      },
      {
        name: 'Shreyan L Anand',
        role: 'Automation',
        image: '/images/team/shreyan-l-anand.jpeg',
        linkedin: 'https://www.linkedin.com/in/shreyan-l-ananad-2ab966399/',
      },
      {
        name: 'Aditya Swami',
        role: 'Aerodynamics',
        image: '/images/team/aditya-swami.jpg',
        linkedin: 'https://www.linkedin.com/in/aditya-swami-401651384',
        x: 'https://x.com/swami_aditya200',
      },
      {
        name: 'Adarsh Kumar',
        role: 'Automation',
        image: '/images/team/adarsh-kumar.jpg',
        linkedin: 'https://www.linkedin.com/in/adarsh-kumar-07b761409',
        instagram: 'https://www.instagram.com/calli.__.art',
      },
      {
        name: 'Kinshuk Basak',
        role: 'Aerodynamics',
        image: '/images/team/kinshuk-basak.jpg',
        linkedin: 'https://www.linkedin.com/in/kinshukbasak0906',
        instagram: 'https://www.instagram.com/oneb1ood/',
      },
    ],
  },
  {
    id: 'first-year',
    title: '1st Year',
    label: '01 / YEAR',
    members: [],
  },
]
