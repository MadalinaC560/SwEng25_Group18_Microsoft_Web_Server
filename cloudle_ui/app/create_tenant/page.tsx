// create_tenant/page.tsx
'use client';
import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Image from 'next/image';
import myGif from "@/public/web-browser.gif";
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Cloud } from 'lucide-react';

// const SERVER_BASE_URL = process.env.NEXT_PUBLIC_SERVER_BASE_URL || "http://localhost:8080";
// const SERVER_BASE_URL = process.env.NEXT_PUBLIC_SERVER_BASE_URL || "http://108.143.71.239:8080";
const SERVER_BASE_URL = process.env.NEXT_PUBLIC_SERVER_BASE_URL || "https://108.143.71.239";





export default function CreateTenantPage() {
  const router = useRouter();
  const [tenantName, setTenantName] = useState(''); // Changed to match backend
  const [tenantEmail, setTenantEmail] = useState(''); // This needs to be added to backend
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  setLoading(true);
  setError('');

  try {

    const response = await fetch(`${SERVER_BASE_URL}/api/tenants`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        tenantName: tenantName,
        tenantEmail: tenantEmail
      }),
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.error || 'Failed to create tenant');
    }

    // Store success message in sessionStorage for display on login page
    sessionStorage.setItem('tenantSuccess', 'Tenant organization successfully created! You can now register users under this tenant.');

    router.push('/login');
  } catch (err) {
  if (err instanceof Error) {
    setError(err.message || 'Something went wrong.');
  } else {
    setError('Something went wrong.');
  }
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
                <Image src={myGif} alt="tenant gif" width={400} height={400} className="object-contain rounded-full mx-auto"/>
                <p className="font-medium">Group 18</p>
                <p className="text-sm">Microsoft • SWEng</p>
              </div>
            </div>
          </div>

          {/* Right Side (Form) */}
          <div className="flex-1 flex flex-col bg-white">
            <div className="flex-1 flex items-center justify-center px-12">
              <div className="w-full max-w-sm space-y-8">
                <div className="space-y-2">
                  <h2 className="text-3xl font-bold tracking-tight text-blue-800">Register a Tenant</h2>
                  <p className="text-gray-500">Create a new organisation account</p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4">
                  <div>
                    <label htmlFor="tenantName" className="block text-sm font-medium text-gray-700 mb-1">Organisation Name</label>
                    <Input
                      id="tenantName"
                      type="text"
                      placeholder="Acme Corp"
                      value={tenantName}
                      onChange={(e) => setTenantName(e.target.value)}
                      required
                    />
                  </div>

                  <div>
                    <label htmlFor="tenantEmail" className="block text-sm font-medium text-gray-700 mb-1">Organisation Email</label>
                    <Input
                      id="tenantEmail"
                      type="email"
                      placeholder="org@example.com"
                      value={tenantEmail}
                      onChange={(e) => setTenantEmail(e.target.value)}
                      required
                    />
                  </div>

                  {error && <p className="text-red-500 text-sm">{error}</p>}

                  <Button type="submit" className="w-full bg-blue-600 text-white hover:bg-blue-700" disabled={loading}>
                    {loading ? "Registering..." : "Register Tenant"}
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