'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Image from 'next/image';
import myGif from "@/public/register.gif";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Cloud } from 'lucide-react';

export default function RegisterAccountPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [tenantEmail, setTenantEmail] = useState('');

  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ tenantEmail, email, password }),

      });

      const data = await response.json();
      if (!response.ok) throw new Error(data.error);
      router.push('/login');
    } catch (err: any) {
      setError(err.message || 'Something went wrong.');
    } finally {
      setLoading(false);
    }
  };

  const handleHome = () => {
    router.push('/landing');
  };

  return (
    <div className="min-h-screen w-full bg-blue-50 text-gray-800">
      {/* Header */}
      <header className="sticky top-0 z-50 bg-blue-600 text-white shadow-md">
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

      {/* Main Content */}
      <div className="w-full flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
        <div className="w-[1100px] h-[600px] flex rounded-xl overflow-hidden shadow-xl bg-white">
          {/* Left Side (Branding) */}
          <div className="w-[470px] bg-blue-600 p-10 flex flex-col justify-between text-white">
            <div className="flex items-center space-x-2">
              <Image src="/static/images/Cloudl.png" alt="Cloudle Logo" width={48} height={48} className="object-contain" />
              <h1 className="text-xl font-semibold">Cloudle</h1>
            </div>
            <div className="space-y-4">
              <blockquote className="text-2xl font-medium leading-relaxed">Cloud first Web Server</blockquote>
              <div className="text-white/60">
                <Image src={myGif} alt="register gif" width={400} height={400} className="object-contain rounded-full mx-auto"/>
                <p className="font-medium">Group 18</p>
                <p className="text-sm">Microsoft • SWEng</p>
              </div>
            </div>
          </div>

          {/* Right Side (Register Form) */}
          <div className="flex-1 flex flex-col bg-white">
            <div className="flex-1 flex items-center justify-center px-12">
              <div className="w-full max-w-sm space-y-8">
                <div className="space-y-2">
                  <h2 className="text-3xl font-bold tracking-tight text-blue-800">Register Account</h2>
                  <p className="text-gray-500">Create a new user account</p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4">
                  <div>
                    <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                    <Input
                      id="email"
                      type="email"
                      placeholder="you@example.com"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      required
                    />
                  </div>

                  <div>
                    <label htmlFor="tenantEmail" className="block text-sm font-medium text-gray-700 mb-1">Tenant Email</label>
                    <Input
                      id="tenantEmail"
                      type="email"
                      placeholder="org@tenant.com"
                      value={tenantEmail}
                      onChange={(e) => setTenantEmail(e.target.value)}
                      required
                    />
                  </div>


                  <div>
                    <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">Password</label>
                    <Input
                      id="password"
                      type="password"
                      placeholder="Create a password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      required
                    />
                  </div>

                  {error && <p className="text-red-500 text-sm">{error}</p>}

                  <Button type="submit" className="w-full bg-blue-600 text-white hover:bg-blue-700" disabled={loading}>
                    {loading ? "Registering..." : "Register"}
                  </Button>
                </form>
              </div>
            </div>
            <div className="p-6 text-center text-sm text-gray-500">
              Already have an account?{' '}
              <button
                onClick={() => router.push('/login')}
                className="text-blue-600 hover:underline font-medium"
              >
                Login here
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
