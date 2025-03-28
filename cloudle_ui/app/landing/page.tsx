"use client"

import { useRouter } from "next/navigation";
import Link from "next/link";
import Image from "next/image";
import { Button } from "@/components/ui/button";
import { Card, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Cloud, Database, Globe, Lock, Server, Shield } from "lucide-react";

export default function LandingPage() {
  const router = useRouter();

  const handleClick = () => {
    router.push('/login');
  }

  return (
    <div className="bg-neutral-50 flex min-h-screen flex-col">
      <header className="sticky top-0 z-40 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container flex h-16 items-center px-8"> {/* Removed justify-between */}
    
        <div className="flex gap-2 items-center text-xl font-bold">
          <Button
            onClick={handleClick}
            variant="outline"
            className="border-green-400 text-green-400 hover:bg-green-50 hover:text-green-700">
            Login
          </Button>

          <Cloud className="h-6 w-6 text-green-400" />
          <span>Cloudle</span>
        </div>

      </div>
      </header>
      <main className="flex-1">
        {/* Hero Section */}
        <section className="w-full py-12 md:py-24 lg:py-32 xl:py-48">
          <div className="container px-4 md:px-6">
            <div className="grid gap-6 lg:grid-cols-2 lg:gap-12 xl:grid-cols-2">
              <div className="flex flex-col justify-center space-y-4">
                <div className="space-y-2">
                  <h1 className="text-3xl font-bold tracking-tighter sm:text-5xl xl:text-6xl/none">
                    Cloud Web Servers for Modern Applications
                  </h1>
                  <p className="max-w-[600px] text-muted-foreground md:text-xl">
                    Powerful, scalable, and secure cloud infrastructure for your web applications. Deploy in seconds,
                    scale with ease.
                  </p>
                </div>
              </div>
              <div className="flex items-center justify-center">
                <Image
                  src="/cloudl.png"
                  width={550}
                  height={550}
                  alt="Server Dashboard"
                  className="rounded-lg object-cover"
                />
              </div>
            </div>
          </div>
        </section>

        {/* Features Section */}
        <section id="features" className="w-full py-12 md:py-24 lg:py-32 bg-green-50">
          <div className="container px-4 md:px-6 ml-24">
            <div className="flex flex-col items-center justify-center space-y-4 text-center">
              <div className="space-y-2">
                <h2 className="text-3xl font-bold tracking-tighter md:text-4xl/tight">
                  Powerful Features for Your Web Applications
                </h2>
                <p className="mx-auto max-w-[700px] text-muted-foreground md:text-xl">
                  Everything you need to build, deploy, and scale your web applications with confidence.
                </p>
              </div>
            </div>
            <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3 mt-12">
              <Card>
                <CardHeader>
                  <Server className="h-10 w-10 text-green-400 mb-2" />
                  <CardTitle>High Performance</CardTitle>
                  <CardDescription>
                    Optimized servers with SSD storage for lightning-fast response times.
                  </CardDescription>
                </CardHeader>
              </Card>
              <Card>
                <CardHeader>
                  <Shield className="h-10 w-10 text-green-400 mb-2" />
                  <CardTitle>Advanced Security</CardTitle>
                  <CardDescription>
                    Built-in DDoS protection, SSL certificates, and regular security updates.
                  </CardDescription>
                </CardHeader>
              </Card>
              <Card>
                <CardHeader>
                  <Database className="h-10 w-10 text-green-400 mb-2" />
                  <CardTitle>Scalable Resources</CardTitle>
                  <CardDescription>
                    Easily scale your resources up or down based on your application needs.
                  </CardDescription>
                </CardHeader>
              </Card>
              <Card>
                <CardHeader>
                  <Globe className="h-10 w-10 text-green-400 mb-2" />
                  <CardTitle>Global CDN</CardTitle>
                  <CardDescription>
                    Deliver content quickly to users worldwide with our global content delivery network.
                  </CardDescription>
                </CardHeader>
              </Card>
              <Card>
                <CardHeader>
                  <Lock className="h-10 w-10 text-green-400 mb-2" />
                  <CardTitle>Automated Backups</CardTitle>
                  <CardDescription>Daily automated backups with point-in-time recovery options.</CardDescription>
                </CardHeader>
              </Card>
              <Card>
                <CardHeader>
                  <Cloud className="h-10 w-10 text-green-400 mb-2" />
                  <CardTitle>One-Click Deployments</CardTitle>
                  <CardDescription>
                    Deploy your applications with a single click using our intuitive dashboard.
                  </CardDescription>
                </CardHeader>
              </Card>
            </div>
          </div>
        </section>

      </main>
      <footer className="w-full border-t py-6 md:py-0">
        <div className="container flex flex-col items-center justify-between gap-4 md:h-24 md:flex-row">
          <div className="flex gap-2 items-center text-lg font-semibold ml-16">
            <Cloud className="h-5 w-5 text-green-400" />
            <span>CloudServe</span>
          </div>
          <p className="text-center text-sm leading-loose text-muted-foreground md:text-left">
            © {new Date().getFullYear()} Cloudle. All rights reserved.
          </p>
          <div className="flex gap-4">
            <Link href="#" className="text-sm text-muted-foreground hover:text-green-400">
              Terms
            </Link>
            <Link href="#" className="text-sm text-muted-foreground hover:text-green-400">
              Privacy
            </Link>
            <Link href="#" className="text-sm text-muted-foreground hover:text-green-400">
              Contact
            </Link>
          </div>
        </div>
      </footer>
    </div>
  )
}

