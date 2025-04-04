'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Image from 'next/image';
import myGif from "@/public/register.gif"
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Cloud } from 'lucide-react';

export default function RegisterPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }
    setLoading(true);
    setError('');
    try {
      const response = await fetch('./api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      });
      if (!response.ok) {
        throw new Error('Failed to register');
      }
      router.push('/login');
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setLoading(false);
    }
  };

  const handleLogin = () => {
    router.push('/login');
  };

  const handleHome = () => {
    router.push('/landing')
  }

  return (
    <div className="min-h-screen w-full bg-blue-50">
      {/* Header */}
      <header className="sticky top-0 z-50 bg-blue-600 text-white shadow-md w-full">
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
        
          <div className="w-[470px] bg-blue-600 p-10 flex flex-col justify-between text-white">
            <div>
              <div className="flex items-center space-x-2">
                <Image src="/static/images/Cloudl.png" alt="Cloudle Logo" width={48} height={48} className="object-contain" />
                <h1 className="text-xl font-semibold">Cloudle</h1>
              </div>
            </div>
            <div className="space-y-4">
              <blockquote className="text-2xl font-medium leading-relaxed">Cloud first Web Server</blockquote>
              <div className="text-white/60">
                <Image src={myGif} alt="login gif" width={400} height={400} className="object-contain rounded-full mx-auto"/>
                <p className="font-medium">Group 18</p>
                <p className="text-sm">Microsoft • SWEng</p>
              </div>
            </div>
          </div>

          <div className="flex-1 flex flex-col bg-white">
            <div className="flex justify-end p-6">
              <Button onClick={handleLogin} variant="ghost" className="text-blue-600 hover:bg-blue-100">User Login</Button>
            </div>

            <div className="flex-1 flex items-center justify-center px-12">
              <div className="w-full max-w-sm space-y-8">
                <div className="space-y-2">
                  <h2 className="text-3xl font-bold tracking-tight text-blue-700">Create Account</h2>
                  <p className="text-gray-500">Enter your email below to create an account</p>
                </div>

                {error && <p className="text-red-500 text-sm">{error}</p>}

                <form onSubmit={handleSubmit} className="space-y-4">
                  <Input
                    type="email"
                    placeholder="name@example.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    className="border-blue-300 focus:ring-2 focus:ring-blue-500"
                  />
                  <Input
                    type="password"
                    placeholder="Enter your password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    className="border-blue-300 focus:ring-2 focus:ring-blue-500"
                  />
                  <Input
                    type="password"
                    placeholder="Re-enter your password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                    className="border-blue-300 focus:ring-2 focus:ring-blue-500"
                  />
                  <Button
                    type="submit"
                    className="w-full bg-blue-600 text-white hover:bg-blue-700"
                    disabled={loading}
                  >
                    {loading ? 'Creating...' : 'Create'}
                  </Button>
                </form>
              </div>
            </div>

            <div className="p-6 text-center text-sm text-gray-500">
              By clicking continue, you agree to our{' '}
              <a href="#" className="underline underline-offset-4 hover:text-gray-900">Terms of Service</a>{' '}
              and{' '}
              <a href="#" className="underline underline-offset-4 hover:text-gray-900">Privacy Policy</a>.
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
