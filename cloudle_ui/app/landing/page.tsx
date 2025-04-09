'use client'

import { useRouter } from 'next/navigation'
import Link from 'next/link'
import myGif from "@/public/test1.gif";
import Image from 'next/image'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import {
  Cloud,
  Database,
  Globe,
  Lock,
  Server,
  Shield,
} from 'lucide-react'

export default function LandingPage() {
  const router = useRouter()

  const handleClick = () => router.push('/login')
  const handleStart = () => router.push('/register_account')
  const handleAbout = () => router.push('/about')

  return (
    <div className="bg-white text-gray-800 min-h-screen flex flex-col">
      {/* Header */}
      <header className="sticky top-0 z-50 bg-blue-600 text-white shadow-md">
        <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-2 text-xl font-semibold">
            <Cloud className="text-white" />
            <span>Cloudle</span>
          </div>
          <Button
            onClick={handleClick}
            variant="ghost"
            className="text-white hover:bg-blue-700 border-white border"
          >
            Login
          </Button>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1">
        {/* Hero Section */}
        <section className="py-24 bg-gradient-to-br from-blue-50 to-white">
          <div className="max-w-7xl mx-auto px-6 grid grid-cols-1 md:grid-cols-2 gap-10 items-center">
            <div>
              <h1 className="text-4xl md:text-6xl font-bold text-blue-700">
                Cloud Web Servers for Modern Applications
              </h1>
              <p className="mt-6 text-lg text-gray-700">
                Powerful, scalable, and secure infrastructure for your web apps.
                Deploy in seconds. Scale effortlessly.
              </p>
              <div className="mt-8 flex gap-4">
                <Button onClick={handleStart} className="bg-blue-600 text-white hover:bg-blue-700">
                  Get Started
                </Button>
                <Button onClick={handleAbout} variant="outline" className="border-blue-600 text-blue-700 hover:bg-blue-100">
                  Learn More
                </Button>
              </div>
            </div>
            <div className="flex justify-center">
              <Image
                src={myGif}
                width={600}
                height={600}
                alt="Logo"
              />
            </div>
          </div>
        </section>

        {/* Features Section */}
        <section id="features" className="bg-blue-50 py-24 border-t border-blue-100">
          <div className="max-w-7xl mx-auto px-6">
            <div className="text-center mb-16">
              <h2 className="text-4xl font-bold text-blue-800">
                Powerful Features for Your Web Applications
              </h2>
              <p className="mt-4 text-gray-600 max-w-2xl mx-auto">
                Everything you need to build, deploy, and scale your web applications with confidence.
              </p>
            </div>

            <div className="grid gap-8 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3">
              {[
                {
                  icon: <Server className="text-blue-600 w-8 h-8" />,
                  title: 'High Performance',
                  desc: 'SSD-optimized servers with fast response times.',
                },
                {
                  icon: <Shield className="text-blue-600 w-8 h-8" />,
                  title: 'Advanced Security',
                  desc: 'DDoS protection, SSL, and regular security patches.',
                },
                {
                  icon: <Database className="text-blue-600 w-8 h-8" />,
                  title: 'Scalable Resources',
                  desc: 'Grow your resources as your app grows.',
                },
                {
                  icon: <Globe className="text-blue-600 w-8 h-8" />,
                  title: 'Global CDN',
                  desc: 'Fast delivery worldwide with edge locations.',
                },
                {
                  icon: <Lock className="text-blue-600 w-8 h-8" />,
                  title: 'Automated Backups',
                  desc: 'Daily backups and easy recovery options.',
                },
                {
                  icon: <Cloud className="text-blue-600 w-8 h-8" />,
                  title: 'One-Click Deploy',
                  desc: 'Launch your app with minimal setup.',
                },
              ].map((item, index) => (
                <Card
                  key={index}
                  className="h-full shadow-sm border hover:shadow-md transition duration-300"
                >
                  <CardHeader>
                    {item.icon}
                    <CardTitle className="mt-4 text-blue-800">
                      {item.title}
                    </CardTitle>
                    <CardDescription className="mt-2 text-gray-600">
                      {item.desc}
                    </CardDescription>
                  </CardHeader>
                </Card>
              ))}
            </div>
          </div>
        </section>
      </main>

      {/* Footer */}
      <footer className="bg-blue-600 text-white py-8 mt-auto">
        <div className="max-w-7xl mx-auto px-6 flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2 text-lg font-semibold">
            <Cloud className="h-5 w-5 text-white" />
            <span>Cloudle</span>
          </div>
          <p className="text-sm text-blue-100">
            © {new Date().getFullYear()} Cloudle. All rights reserved.
          </p>
          <div className="flex gap-6">
            {['Terms', 'Privacy', 'Contact'].map((text) => (
              <Link
                key={text}
                href="#"
                className="text-sm text-blue-100 hover:text-white transition"
              >
                {text}
              </Link>
            ))}
          </div>
        </div>
      </footer>
    </div>
  )
}
