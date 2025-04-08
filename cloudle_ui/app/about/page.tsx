'use client';

import { useRouter } from "next/navigation";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
  Github,
  Linkedin,
  Twitter,
  Cloud,
  Mail,
  Users,
  Building2,
  Target,
  Shield,
} from "lucide-react";
import { JSX } from "react";
import Image from "next/image";


interface TeamMember {
  name: string;
  role: string;
  image: string;
  bio: string;
  social: {
    github?: string;
    linkedin?: string;
    twitter?: string;
    email?: string;
  };
}

interface CompanyValue {
  icon: JSX.Element;
  title: string;
  description: string;
}



const teamMembers: TeamMember[] = [
  {
    name: "Madalina Costovici",
    role: "Team/Frontend Lead",
    image: "/static/team/Screenshot_2025-02-17_at_15.28.39.png",
    bio: "Skilled in designing intuitive, modern UIs using modern Javascript frameworks, as well as driving projects from concept to deployment, and co-ordinating cross functional teams to ensure seamless integration with backend services.",
    social: {
      github: "https://github.com",
      linkedin: "https://linkedin.com",
      twitter: "https://twitter.com",
      email: "Madz@cloudle.com",
    },
  },
  {
    name: "Eniola Olumeyan",
    role: "Backend Engineer/Documentation",
    image: "/static/team/Screenshot_2025-02-17_at_15.31.38.png",
    bio: "Detailed-oriented engineer with a focus on building robust, scalable server-side systems for cloud-based appliacations. Serves as the team's Documentation Lead, creating clear, user-friendly documentation for internal and external users.",
    social: {
      github: "https://github.com",
      linkedin: "https://linkedin.com",
      email: "Eniola@cloudle.com",
    },
  },
  {
    name: "Dylan Gallagher",
    role: "Lead Backend Engineer",
    image: "/static/team/Screenshot_2025-02-17_at_15.30.00.png",
    bio: "Seasoned engineer with a proven track record of architecting & delivering of backend systems for cloud-based applications. Experience in building and maintaining of CI/CD pipelines to automate testing, integration and deployment of the product.",
    social: {
      github: "https://github.com",
      linkedin: "https://linkedin.com",
      email: "Dylan@cloudle.com",
    },
  },
  {
    name: "Daniel Fitzgerald",
    role: "Lead Testing Engineer/Documentation",
    image: "/static/team/Screenshot_2025-02-17_at_15.32.37.png",
    bio: "Strong background in designing and implementing comprehensive testing strategies. Also serving on Documentation, helping to create clear and detailed technical documentation.",
    social: {
      github: "https://github.com",
      linkedin: "https://linkedin.com",
      email: "Daniel@cloudle.com",
    },
  },
  {
    name: "Ethan Duffy",
    role: "Backend Engineer",
    image: "/static/team/Screenshot_2025-02-17_at_15.30.41.png",
    bio: "Experienced with Microsoft Azure, including services like Azure App Services and Virtual Machines. Strong focus of clean code, maintainabillity and system reliability.",
    social: {
      github: "https://github.com",
      linkedin: "https://linkedin.com",
      email: "Ethan@cloudle.com",
    },
  },
  {
    name: "Andy Yu",
    role: "Frontend Engineer",
    image: "/static/team/Screenshot_2025-02-17_at_15.29.23.png",
    bio: "Focus on building secure, user-friendly web applications. Skills in implementing SSL and TLS for secure communication, as well as being well versed in modern frontend frameworks.",
    social: {
      github: "https://github.com",
      linkedin: "https://linkedin.com",
      email: "Andy@cloudle.com",
    },
  },
  {
    name: "Abdul Rehan",
    role: "Backend Engineer",
    image: "/static/team/Screenshot_2025-02-17_at_15.33.12.png",
    bio: "Stong focus on building and maintatining file-handling services and secure user authentication logic. Proficient in designing modular, maintainable code & integrating backend systems with databases.",
    social: {
      github: "https://github.com",
      linkedin: "https://linkedin.com",
      email: "Abdul@cloudle.com",
    },
  },
  {
    name: "Anastasia O'Donnell",
    role: "Backend Engineer",
    image: "/static/team/Screenshot_2025-02-17_at_15.32.12.png",
    bio: "Experience in server-side scripting to power dynamic web applications. Proficient in languages such as Java and PHP, with a deep understanding of web server environments.",
    social: {
      github: "https://github.com",
      linkedin: "https://linkedin.com",
      email: "Ana@cloudle.com",
    },
  },
];

const companyValues: CompanyValue[] = [
  {
    icon: <Users className="h-6 w-6 text-blue-600" />,
    title: "Customer First",
    description:
      "Our customers' success is our success. We're committed to providing exceptional service and support.",
  },
  {
    icon: <Building2 className="h-6 w-6 text-blue-600" />,
    title: "Innovation",
    description:
      "We continuously push the boundaries of what's possible in cloud infrastructure.",
  },
  {
    icon: <Target className="h-6 w-6 text-blue-600" />,
    title: "Reliability",
    description:
      "We build robust, scalable solutions that our customers can depend on 24/7.",
  },
  {
    icon: <Shield className="h-6 w-6 text-blue-600" />,
    title: "Security",
    description:
      "Security is at the core of everything we do, protecting our customers' data and infrastructure.",
  },
];


export default function AboutPage() {
  
  const router = useRouter();

  const handleHome = () => {
    router.push('/landing')
}


  return (
    
    <div className="min-h-screen bg-blue-50 py-16 text-gray-800">
          <header className="fixed top-0 left-0 w-full z-50 bg-blue-600 text-white shadow-md">
      <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-2 text-xl font-semibold">
          <Cloud className="text-white" />
          <span>Cloudle</span>
        </div>
        <Button
          onClick={handleHome}
          variant="ghost"
          className="text-white hover:bg-blue-700 border-white border"
        >
          Home
        </Button>
      </div>
    </header>
      <div className="container mx-auto px-6 space-y-20">
        <section className="text-center space-y-6">
          <h1 className="text-5xl font-bold text-blue-800">About Cloudle</h1>
          <p className="text-xl text-gray-700 max-w-3xl mx-auto">
            We are on a mission to make cloud infrastructure management simple,
            efficient, and accessible to developers worldwide.
          </p>
        </section>

        <section className="space-y-10">
          <h2 className="text-3xl font-bold text-center text-blue-700">
            Our Values
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {companyValues.map((value, index) => (
              <Card
                key={index}
                className="bg-white border-blue-100 shadow-sm hover:shadow-md transition"
              >
                <CardContent className="pt-6 space-y-4">
                  <div className="w-12 h-12 rounded-full bg-blue-100 flex items-center justify-center">
                    {value.icon}
                  </div>
                  <h3 className="text-xl font-semibold text-blue-800">
                    {value.title}
                  </h3>
                  <p className="text-gray-600">{value.description}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </section>

        <section className="space-y-10">
          <h2 className="text-3xl font-bold text-center text-blue-700">
            Our Team
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {teamMembers.map((member, index) => (
              <Card
                key={index}
                className="overflow-hidden border-blue-100 shadow hover:shadow-md transition"
              >
                <CardContent className="p-6 space-y-4">
                  <div className="space-y-4">
                    <Image
                      src={member.image}
                      alt={member.name}
                      width={120}
                      height={120}
                      className="w-24 h-24 rounded-full mx-auto object-cover"
                    />
                    <div className="text-center">
                      <h3 className="text-xl font-semibold text-blue-800">
                        {member.name}
                      </h3>
                      <p className="text-blue-600">{member.role}</p>
                    </div>
                  </div>
                  <p className="text-gray-600 text-center">{member.bio}</p>
                  <div className="flex justify-center space-x-2">
                    {member.social.github && (
                      <Button variant="ghost" size="icon" asChild>
                        <a
                          href={member.social.github}
                          target="_blank"
                          rel="noopener noreferrer"
                        >
                          <Github className="h-5 w-5 text-blue-600" />
                        </a>
                      </Button>
                    )}
                    {member.social.linkedin && (
                      <Button variant="ghost" size="icon" asChild>
                        <a
                          href={member.social.linkedin}
                          target="_blank"
                          rel="noopener noreferrer"
                        >
                          <Linkedin className="h-5 w-5 text-blue-600" />
                        </a>
                      </Button>
                    )}
                    {member.social.twitter && (
                      <Button variant="ghost" size="icon" asChild>
                        <a
                          href={member.social.twitter}
                          target="_blank"
                          rel="noopener noreferrer"
                        >
                          <Twitter className="h-5 w-5 text-blue-600" />
                        </a>
                      </Button>
                    )}
                    {member.social.email && (
                      <Button variant="ghost" size="icon" asChild>
                        <a href={`mailto:${member.social.email}`}>
                          <Mail className="h-5 w-5 text-blue-600" />
                        </a>
                      </Button>
                    )}
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </section>

        <section className="max-w-2xl mx-auto text-center space-y-6">
          <h2 className="text-3xl font-bold text-blue-700">Get in Touch</h2>
          <p className="text-gray-700">
            Have questions about Cloudle? We would love to hear from you.
          </p>
          <Button className="bg-blue-600 text-white hover:bg-blue-700" asChild>
            <a href="mailto:contact@cloudle.com">Contact Us</a>
          </Button>
        </section>
      </div>
    </div>
  );
}
