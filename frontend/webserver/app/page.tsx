// app/page.tsx
'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function Home() {
  const router = useRouter();
  const [selectedAppId, setSelectedAppId] = useState<number | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    const checkAuth = () => {
      const hasAuth = localStorage.getItem('isAuthenticated');
      if (!hasAuth) {
        router.push('/login');
      } else {
        setIsAuthenticated(true);
      }
    };

    checkAuth();
  }, [router]);

  const handleLogout = () => {
    localStorage.removeItem('isAuthenticated');
    router.push('/login');
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

    </div>
  );
}