// components/Navigation.tsx
'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Button } from "@/components/ui/button";
import { UserCog, Users, LogOut, Info } from 'lucide-react';

interface NavigationProps {
  isAdmin: boolean;
  setIsAdmin: (value: boolean) => void;
  onLogout: () => void;
}

export function Navigation({ isAdmin, setIsAdmin, onLogout }: NavigationProps) {
  const pathname = usePathname();

  return (
    <nav className="fixed top-0 left-0 right-0 z-50 bg-white border-b">
      <div className="container mx-auto px-4">
        <div className="h-16 flex items-center justify-between">
          {/* Left side - Logo and brand */}
          <div className="flex items-center space-x-4">
            <Link href="/" className="flex items-center space-x-2">
              <div className="w-8 h-8 bg-black rounded flex items-center justify-center">
                <span className="text-white text-xl">⚡</span>
              </div>
              <span className="font-semibold text-lg">Cloudle</span>
            </Link>
          </div>

          {/* Middle - Navigation Links */}
          <div className="hidden md:flex items-center space-x-4">
            <Link 
              href="/about" 
              className={`text-sm font-medium transition-colors hover:text-primary
                ${pathname === '/about' ? 'text-primary' : 'text-muted-foreground'}`}
            >
              About Us
            </Link>
            {/* Add more navigation links as needed */}
          </div>

          {/* Right side - Role Switcher and Logout */}
          <div className="flex items-center space-x-2">
            <Button 
              variant={isAdmin ? "default" : "outline"}
              size="sm"
              onClick={() => setIsAdmin(true)}
            >
              <UserCog className="h-4 w-4 mr-2" />
              Admin
            </Button>
            <Button 
              variant={!isAdmin ? "default" : "outline"}
              size="sm"
              onClick={() => setIsAdmin(false)}
            >
              <Users className="h-4 w-4 mr-2" />
              User
            </Button>
            <Button 
              variant="outline"
              size="sm"
              onClick={onLogout}
            >
              <LogOut className="h-4 w-4 mr-2" />
              Logout
            </Button>
          </div>
        </div>
      </div>
    </nav>
  );
}