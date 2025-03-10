// app/login/page.tsx
'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Image from 'next/image';
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Github } from 'lucide-react';

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    localStorage.setItem('isAuthenticated', 'true');
    router.push('/');
  };

  return (
      // <div className="min-h-screen w-full flex items-center justify-center bg-zinc-50">
      <div className="min-h-screen w-full flex items-center justify-center bg-custom">
        <div className="w-[1100px] h-[600px] flex rounded-xl overflow-hidden shadow-xl">
          {/* Left Section - Branding */}
          <div className="w-[470px] bg-black p-10 flex flex-col justify-between">
            <div>
              <div className="flex items-center space-x-2">
                <Image
                    src="/static/images/Cloudl.png"
                    alt="Cloudle Logo"
                    width={48}
                    height={48}
                    className="object-contain"
                />
                <h1 className="text-white text-xl font-semibold">Cloudle</h1>
              </div>
            </div>

            <div className="space-y-4">
              <blockquote className="text-white text-2xl font-medium leading-relaxed">
                Cloud first Web Server
              </blockquote>
              <div className="text-white/60">
                <p className="font-medium">Group 18</p>
                <p className="text-sm">Microsoft • SWEng</p>
              </div>
            </div>
          </div>

          {/* Right Section - Login Form */}
          <div className="flex-1 flex flex-col bg-white">
            <div className="flex justify-end p-6">
              <Button variant="ghost">
                Login
              </Button>
            </div>

            <div className="flex-1 flex items-center justify-center px-12">
              <div className="w-full max-w-sm space-y-8">
                <div className="space-y-2">
                  <h2 className="text-3xl font-bold tracking-tight text-gray-900">
                    Create an account
                  </h2>
                  <p className="text-gray-500">
                    Enter your email below to create your account
                  </p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="space-y-2">
                    <Input
                        type="email"
                        placeholder="name@example.com"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                  </div>

                  <Button type="submit" className="w-full">
                    Sign In with Email
                  </Button>

                  <div className="relative">
                    <div className="absolute inset-0 flex items-center">
                      <span className="w-full border-t"></span>
                    </div>
                    <div className="relative flex justify-center text-xs uppercase">
                      <span className="bg-white px-2 text-gray-500">Or continue with</span>
                    </div>
                  </div>

                  <Button variant="outline" className="w-full" type="button">
                    <Github className="mr-2 h-4 w-4"/>
                    Github
                  </Button>
                </form>
              </div>
            </div>

            <div className="p-6 text-center text-sm text-gray-500">
              By clicking continue, you agree to our{' '}
              <a href="#" className="underline underline-offset-4 hover:text-gray-900">
                Terms of Service
              </a>{' '}
              and{' '}
              <a href="#" className="underline underline-offset-4 hover:text-gray-900">
                Privacy Policy
              </a>
              .
            </div>
          </div>
        </div>
      </div>
  );
}