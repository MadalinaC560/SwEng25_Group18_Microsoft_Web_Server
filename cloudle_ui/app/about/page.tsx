// app/about/page.tsx
'use client';

import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { 
  Github, 
  Linkedin, 
  Twitter,
  Mail,
  Users,
  Building2,
  Target,
  Shield
} from 'lucide-react';
import { JSX } from "react";
import Image from 'next/image';

// Team member type definition
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

// Company value type definition
interface CompanyValue {
  icon: JSX.Element;
  title: string;
  description: string;
}

const teamMembers: TeamMember[] = [
  {
    name: "Madalina Costovici",
    role: "CEO & Founder",
    image: "/static/team/Screenshot_2025-02-17_at_15.28.39.png",
    bio: "10+ years experience in cloud infrastructure and developer tools",
    social: {
      github: "https://github.com",
      linkedin: "https://linkedin.com",
      twitter: "https://twitter.com",
      email: "sarah@cloudle.com"
    }
  },
  {
    name: "Eniola Olumeyan",
    role: "CTO",
    image: "/static/team/Screenshot_2025-02-17_at_15.31.38.png",
    bio: "Former senior engineer at AWS, specialized in distributed systems",
    social: {
      github: "https://github.com",
      linkedin: "https://linkedin.com",
      email: "michael@cloudle.com"
    }
  },
  {
    name: "Dylan Gallagher",
    role: "CTO",
    image: "/static/team/Screenshot_2025-02-17_at_15.30.00.png",
    bio: "Former senior engineer at AWS, specialized in distributed systems",
    social: {
      github: "https://github.com",
      linkedin: "https://linkedin.com",
      email: "michael@cloudle.com"
    }
  },
  {
    name: "Daniel Fitzgerald",
    role: "CTO",
    image: "/static/team/Screenshot_2025-02-17_at_15.32.37.png",
    bio: "Former senior engineer at AWS, specialized in distributed systems",
    social: {
      github: "https://github.com",
      linkedin: "https://linkedin.com",
      email: "michael@cloudle.com"
    }
  },
  {
    name: "Ethan Duffy",
    role: "CTO",
    image: "/static/team/Screenshot_2025-02-17_at_15.30.41.png",
    bio: "Former senior engineer at AWS, specialized in distributed systems",
    social: {
      github: "https://github.com",
      linkedin: "https://linkedin.com",
      email: "michael@cloudle.com"
    }
  },
  {
    name: "Andy Yu",
    role: "CTO",
    image: "/static/team/Screenshot_2025-02-17_at_15.29.23.png",
    bio: "Former senior engineer at AWS, specialized in distributed systems",
    social: {
      github: "https://github.com",
      linkedin: "https://linkedin.com",
      email: "michael@cloudle.com"
    }
  },
  {
    name: "Abdul Rehan",
    role: "CTO",
    image: "/static/team/Screenshot_2025-02-17_at_15.33.12.png",
    bio: "Former senior engineer at AWS, specialized in distributed systems",
    social: {
      github: "https://github.com",
      linkedin: "https://linkedin.com",
      email: "michael@cloudle.com"
    }
  },
  {
    name: "Anastasia O'Donnell",
    role: "CTO",
    image: "/static/team/Screenshot_2025-02-17_at_15.32.12.png",
    bio: "Former senior engineer at AWS, specialized in distributed systems",
    social: {
      github: "https://github.com",
      linkedin: "https://linkedin.com",
      email: "michael@cloudle.com"
    }
  },
];

const companyValues: CompanyValue[] = [
  {
    icon: <Users className="h-6 w-6" />,
    title: "Customer First",
    description: "Our customers' success is our success. We're committed to providing exceptional service and support."
  },
  {
    icon: <Building2 className="h-6 w-6" />,
    title: "Innovation",
    description: "We continuously push the boundaries of what's possible in cloud infrastructure."
  },
  {
    icon: <Target className="h-6 w-6" />,
    title: "Reliability",
    description: "We build robust, scalable solutions that our customers can depend on 24/7."
  },
  {
    icon: <Shield className="h-6 w-6" />,
    title: "Security",
    description: "Security is at the core of everything we do, protecting our customers' data and infrastructure."
  }
];

export default function AboutPage() {
  return (
    <div className="min-h-screen bg-gray-50 py-12">
      <div className="container mx-auto px-4 space-y-16">
        {/* Hero Section */}
        <section className="text-center space-y-4">
          <h1 className="text-4xl font-bold">About Cloudle</h1>
          <p className="text-xl text-gray-600 max-w-3xl mx-auto">
            We are on a mission to make cloud infrastructure management simple,
            efficient, and accessible to developers worldwide.
          </p>
        </section>

        {/* Company Values */}
        <section className="space-y-8">
          <h2 className="text-3xl font-bold text-center">Our Values</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {companyValues.map((value, index) => (
              <Card key={index}>
                <CardContent className="pt-6 space-y-4">
                  <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center text-primary">
                    {value.icon}
                  </div>
                  <h3 className="text-xl font-semibold">{value.title}</h3>
                  <p className="text-gray-600">{value.description}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </section>

        {/* Team Section */}
        <section className="space-y-8">
          <h2 className="text-3xl font-bold text-center">Our Team</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {teamMembers.map((member, index) => (
              <Card key={index} className="overflow-hidden">
                <CardContent className="p-6 space-y-4">
                  <div className="space-y-4">
                    <Image
                      src={member.image}
                      alt={member.name}
                      width={500}
                      height={300}
                      className="w-24 h-24 rounded-full mx-auto"

                    />
                    <div className="text-center">
                      <h3 className="text-xl font-semibold">{member.name}</h3>
                      <p className="text-gray-600">{member.role}</p>
                    </div>
                  </div>
                  <p className="text-gray-600 text-center">{member.bio}</p>
                  <div className="flex justify-center space-x-2">
                    {member.social.github && (
                      <Button variant="ghost" size="icon" asChild>
                        <a href={member.social.github} target="_blank" rel="noopener noreferrer">
                          <Github className="h-5 w-5" />
                        </a>
                      </Button>
                    )}
                    {member.social.linkedin && (
                      <Button variant="ghost" size="icon" asChild>
                        <a href={member.social.linkedin} target="_blank" rel="noopener noreferrer">
                          <Linkedin className="h-5 w-5" />
                        </a>
                      </Button>
                    )}
                    {member.social.twitter && (
                      <Button variant="ghost" size="icon" asChild>
                        <a href={member.social.twitter} target="_blank" rel="noopener noreferrer">
                          <Twitter className="h-5 w-5" />
                        </a>
                      </Button>
                    )}
                    {member.social.email && (
                      <Button variant="ghost" size="icon" asChild>
                        <a href={`mailto:${member.social.email}`}>
                          <Mail className="h-5 w-5" />
                        </a>
                      </Button>
                    )}
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </section>

        {/* Contact Section */}
        <section className="max-w-2xl mx-auto text-center space-y-4">
          <h2 className="text-3xl font-bold">Get in Touch</h2>
          <p className="text-gray-600">
            Have questions about Cloudle? We would love to hear from you.
          </p>
          <Button asChild>
            <a href="mailto:contact@cloudle.com">Contact Us</a>
          </Button>
        </section>
      </div>
    </div>
  );
}