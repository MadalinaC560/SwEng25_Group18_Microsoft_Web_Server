"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Image from "next/image";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

export default function LoginPage() {
  const router = useRouter();

  // We now store tenantEmail, username, password
  const [tenantEmail, setTenantEmail] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const [errorMessage, setErrorMessage] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(""); // Clear any old error

    try {
      const res = await fetch("http://localhost:8080/api/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ tenantEmail, username, password }),
      });

      if (!res.ok) {
        const errText = await res.text();
        setErrorMessage(`Login failed: ${errText}`);
        return;
      }

      const userData = await res.json();
      localStorage.setItem("user", JSON.stringify(userData));
      localStorage.setItem("isAuthenticated", "true");

      router.push("/");
    } catch (error: unknown) {
      if (error instanceof Error) {
        setErrorMessage(`Error: ${error.message}`);
      } else {
        setErrorMessage("Unknown error occurred");
      }
    }
  };

  const goToSignUp = () => {
    alert("Sign-up is not yet implemented!");
  };

  return (
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
                <Button variant="ghost">Login</Button>
              </div>

              <div className="flex-1 flex items-center justify-center px-12">
                <div className="w-full max-w-sm space-y-6">
                  <div className="space-y-2">
                    <h2 className="text-3xl font-bold tracking-tight text-gray-900">
                      Sign In
                    </h2>
                    <p className="text-gray-500">
                      Enter your Tenant Email, Username, and Password below
                    </p>
                  </div>

                  {errorMessage && (
                      <div className="bg-red-50 border border-red-300 text-red-700 p-3 text-sm rounded">
                        {errorMessage}
                      </div>
                  )}

                  <form onSubmit={handleSubmit} className="space-y-4">
                    {/* Tenant Email */}
                    <div className="space-y-2">
                      <Input
                          type="email"
                          placeholder="Tenant email, e.g. admin_01@sweng.tcd"
                          value={tenantEmail}
                          onChange={(e) => setTenantEmail(e.target.value)}
                          required
                      />
                    </div>

                    {/* Username */}
                    <div className="space-y-2">
                      <Input
                          type="text"
                          placeholder="Your username"
                          value={username}
                          onChange={(e) => setUsername(e.target.value)}
                          required
                      />
                    </div>

                    {/* Password */}
                    <div className="space-y-2">
                      <Input
                          type="password"
                          placeholder="Password"
                          value={password}
                          onChange={(e) => setPassword(e.target.value)}
                          required
                      />
                    </div>

                    <Button type="submit" className="w-full">
                      Sign In
                    </Button>
                  </form>

                  <div className="text-sm text-gray-500 flex gap-2 justify-center">
                    <span>Don`&apos;`t have an account?</span>
                    <button
                        onClick={goToSignUp}
                        className="text-blue-600 hover:underline"
                    >
                      Sign Up
                    </button>
                  </div>
                </div>
              </div>

              <div className="p-6 text-center text-sm text-gray-500">
                By clicking continue, you agree to our{" "}
                <a
                    href="#"
                    className="underline underline-offset-4 hover:text-gray-900"
                >
                  Terms of Service
                </a>{" "}
                and{" "}
                <a
                    href="#"
                    className="underline underline-offset-4 hover:text-gray-900"
                >
                  Privacy Policy
                </a>
                .
              </div>
            </div>
          </div>
        </div>
    );
  }
