'use client';

import React, { useEffect } from "react";
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Button } from "@/components/ui/button";
import { UserCog, Users, LogOut } from 'lucide-react';
import { Cloud } from 'lucide-react'; // Importing the Cloud icon

interface NavigationProps {
  isAdmin: boolean;
  setIsAdmin: (value: boolean) => void;
  onLogout: () => void;
}

export function Navigation({ isAdmin, setIsAdmin, onLogout }: NavigationProps) {
  const pathname = usePathname();

  // When the component mounts, load the persisted role from localStorage
  useEffect(() => {
    const savedRole = localStorage.getItem('isAdmin');
    if (savedRole !== null) {
      setIsAdmin(savedRole === 'true');
    } else {
      // Default to user mode
      setIsAdmin(false);
      localStorage.setItem('isAdmin', 'false');
    }
  }, [setIsAdmin]);

  // Update localStorage whenever the role changes
  const handleRoleChange = (admin: boolean) => {
    setIsAdmin(admin);
    localStorage.setItem('isAdmin', admin.toString());
  };

  return (
    <nav className="fixed top-0 left-0 right-0 z-50 bg-blue-600 text-white border-b">
      <div className="container mx-auto px-4">
        <div className="h-16 flex items-center justify-between">
          {/* Left side - Logo and brand */}
          <div className="flex items-center gap-2 text-xl font-semibold">
            <Cloud className="text-white" />
            <span>Cloudle</span>
          </div>

          {/* Middle - Navigation Links */}
          <div className="hidden md:flex items-center space-x-4">
            <Link
                href="/about"
                className={`text-sm font-medium transition-colors hover:text-blue-300
                ${pathname === '/about' ? 'text-blue-300' : 'text-white'}`}
            >
              About Us
            </Link>
            {/* Add more navigation links as needed */}
          </div>

          {/* Right side - Role Switcher and Logout */}
          <div className="flex items-center space-x-4">
            <Button
                variant={isAdmin ? "default" : "outline"}
                size="sm"
                onClick={() => handleRoleChange(true)}
                className="bg-green-600 text-white border-transparent"
            >
              <UserCog className="h-4 w-4 mr-2" />
              Admin
            </Button>
            <Button
                variant={!isAdmin ? "default" : "outline"}
                size="sm"
                onClick={() => handleRoleChange(false)}
                className="bg-green-600 text-white border-transparent"
            >
              <Users className="h-4 w-4 mr-2" />
              User
            </Button>
            <Button
                variant="outline"
                size="sm"
                onClick={onLogout}
                className="bg-red-700 text-white border-transparent"
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

