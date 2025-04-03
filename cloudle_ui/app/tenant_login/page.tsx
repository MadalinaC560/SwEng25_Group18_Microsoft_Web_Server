'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Image from 'next/image';
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

export default function TenantPage(){
    const [organisation, setOrg] = useState('');
    const [password, setPassword] = useState('');

    const router = useRouter();

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        localStorage.setItem('isAuthenticated', 'true');
        router.push('/');
      };
    const handleUser = () => {
        router.push('/login');
      }

    const handleTenantRegister = () => {
        router.push('/register_tenant')
    }
    
    return(
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
                <Button
                onClick={handleUser} 
                variant="ghost">
                  User Login
                </Button>
              </div>
  
              <div className="flex-1 flex items-center justify-center px-12">
              <div className="w-full max-w-sm space-y-8">
                <div className="space-y-2">
                  <h2 className="text-3xl font-bold tracking-tight text-gray-900">
                    Tenant Account Login
                  </h2>
                  <p className="text-gray-500">
                    Enter organisation email to login
                  </p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="space-y-2">
                    <Input
                        type="email"
                        placeholder="name@org.domain"
                        value={organisation}
                        onChange={(e) => setOrg(e.target.value)}
                        required
                    />
                  </div>

                {/* Password Input */}
                <div className="space-y-2">
                  <Input
                    type="password"
                    placeholder="Enter your password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </div>

                  <Button type="submit" className="w-full">
                    Sign In with tenant
                  </Button>

                  <div className="relative">
                    <div className="absolute inset-0 flex items-center">
                      <span className="w-full border-t"></span>
                    </div>
                    <div className="relative flex justify-center text-xs uppercase">
                      <span className="bg-white px-2 text-gray-500">Or Create a Tenant Organisation</span>

                    </div>
                  </div>
                  <Button onClick={handleTenantRegister} type="submit" variant="secondary" className="w-full">
                        Register Tenant Organisation
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

