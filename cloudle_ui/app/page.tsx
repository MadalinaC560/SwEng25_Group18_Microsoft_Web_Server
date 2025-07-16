// app/page.tsx
'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { EngineeringDashboard } from '@/components/EngineeringDashboard';
import { UserDashboard } from '@/components/UserDashboard';
import { ApplicationDetails } from '@/components/ApplicationDetails';
import { Navigation } from '@/components/Navigation';

export default function Home() {
  const router = useRouter();
  const [isAdmin, setIsAdmin] = useState(true);
  const [selectedAppId, setSelectedAppId] = useState<number | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    const checkAuth = () => {
      const hasAuth = localStorage.getItem('isAuthenticated');
      if (!hasAuth) {
        router.push('/landing');
      } else {
        setIsAuthenticated(true);
      }
    };

    checkAuth();
  }, [router]);

  const handleLogout = () => {
    localStorage.removeItem('isAuthenticated');
    router.push('/landing');
  };

  const handleAppClick = (appId: number) => {
    setSelectedAppId(appId);
  };

  const handleBack = () => {
    setSelectedAppId(null);
  };

  if (!isAuthenticated) {
    return null; // or a loading spinner
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navigation 
        isAdmin={isAdmin}
        setIsAdmin={setIsAdmin}
        onLogout={handleLogout}
      />
      <main className="pt-16">
        {isAdmin ? (
          <EngineeringDashboard />
        ) : (
          selectedAppId !== null ? (
            <ApplicationDetails 
              appId={selectedAppId} 
              onBack={handleBack}
            />
          ) : (
            <UserDashboard 
              onAppClick={handleAppClick}
            />
          )
        )}
      </main>
    </div>
  );
}