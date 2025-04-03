'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Image from 'next/image';
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

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

  return (
      <div className="min-h-screen w-full flex items-center justify-center bg-custom">
        <div className="w-[1100px] h-[600px] flex rounded-xl overflow-hidden shadow-xl">
          <div className="w-[470px] bg-black p-10 flex flex-col justify-between">
            <div>
              <div className="flex items-center space-x-2">
                <Image src="/static/images/Cloudl.png" alt="Cloudle Logo" width={48} height={48} className="object-contain" />
                <h1 className="text-white text-xl font-semibold">Cloudle</h1>
              </div>
            </div>
            <div className="space-y-4">
              <blockquote className="text-white text-2xl font-medium leading-relaxed">Cloud first Web Server</blockquote>
              <div className="text-white/60">
                <p className="font-medium">Group 18</p>
                <p className="text-sm">Microsoft • SWEng</p>
              </div>
            </div>
          </div>

          <div className="flex-1 flex flex-col bg-white">
            <div className="flex justify-end p-6">
              <Button onClick={handleLogin} variant="ghost">User Login</Button>
            </div>

            <div className="flex-1 flex items-center justify-center px-12">
              <div className="w-full max-w-sm space-y-8">
                <div className="space-y-2">
                  <h2 className="text-3xl font-bold tracking-tight text-gray-900">Create Account</h2>
                  <p className="text-gray-500">Enter your email below to create an account</p>
                </div>

                {error && <p className="text-red-500 text-sm">{error}</p>}

                <form onSubmit={handleSubmit} className="space-y-4">
                  <Input type="email" placeholder="name@example.com" value={email} onChange={(e) => setEmail(e.target.value)} required />
                  <Input type="password" placeholder="Enter your password" value={password} onChange={(e) => setPassword(e.target.value)} required />
                  <Input type="password" placeholder="Re-enter your password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} required />
                  <Button type="submit" className="w-full" disabled={loading}>{loading ? 'Creating...' : 'Create'}</Button>
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
  );
}