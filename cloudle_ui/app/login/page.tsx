'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Image from 'next/image';
import myGif from "@/public/web-browser.gif";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Cloud } from 'lucide-react';

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });

      const data = await response.json();
      if (!response.ok) throw new Error(data.error);

      localStorage.setItem('token', data.token);
      localStorage.setItem('isAuthenticated', 'true');

      router.push('/');
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Something went wrong.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = () => {
    router.push('/register_account');
  };

  const handleTenant = () => {
    router.push('/tenant_login');
  };

  const handleHome = () => {
    router.push('/landing')
  }

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
              <Button onClick={handleTenant} variant="ghost" className="text-blue-700 hover:text-blue-900">Tenant Login</Button>
            </div>
            <div className="flex-1 flex items-center justify-center px-12">
              <div className="w-full max-w-sm space-y-8">
                <div className="space-y-2">
                  <h2 className="text-3xl font-bold tracking-tight text-blue-800">User Login</h2>
                  <p className="text-gray-500">Enter your email below to log into your account</p>
                </div>
                <form onSubmit={handleSubmit} className="space-y-4">
                  <Input type="email" placeholder="name@example.com" value={email} onChange={(e) => setEmail(e.target.value)} required />
                  <Input type="password" placeholder="Enter your password" value={password} onChange={(e) => setPassword(e.target.value)} required />
                  {error && <p className="text-red-500 text-sm">{error}</p>}
                  <Button type="submit" className="w-full bg-blue-600 text-white hover:bg-blue-700" disabled={loading}>{loading ? "Logging in..." : "Sign In with Email"}</Button>
                  <div className="relative">
                    <div className="absolute inset-0 flex items-center">
                      <span className="w-full border-t"></span>
                    </div>
                    <div className="relative flex justify-center text-xs uppercase">
                      <span className="bg-white px-2 text-gray-500">Or Register an Account</span>
                    </div>
                  </div>
                  <Button onClick={handleRegister} variant="outline" className="w-full border-blue-600 text-blue-700 hover:bg-blue-50">Register New Account</Button>
                </form>
              </div>
            </div>
            <div className="p-6 text-center text-sm text-gray-500">
              By clicking continue, you agree to our{' '}
              <a href="#" className="underline underline-offset-4 hover:text-blue-800">Terms of Service</a>{' '} and{' '}
              <a href="#" className="underline underline-offset-4 hover:text-blue-800">Privacy Policy</a>.
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}