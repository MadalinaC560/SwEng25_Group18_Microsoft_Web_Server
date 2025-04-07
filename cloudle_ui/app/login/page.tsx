"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Image from "next/image";
import myGif from "@/public/web-browser.gif";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Cloud } from "lucide-react";

export default function LoginPage() {
  const router = useRouter();

  // Form fields
  const [tenantEmail, setTenantEmail] = useState("");
  const [userName, setUserName] = useState("");
  const [password, setPassword] = useState("");

  // UI states
  const [errorMessage, setErrorMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage("");
    setLoading(true);

    try {
      const res = await fetch("http://localhost:8080/api/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ tenantEmail, username: userName, password }),
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
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = () => {
    router.push("/register_account");
  };

  const handleHome = () => {
    router.push("/landing");
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
          {/* Left side */}
          <div className="w-[470px] bg-blue-600 p-10 flex flex-col justify-between text-white">
            <div>
              <div className="flex items-center space-x-2">
                <Image
                  src="/static/images/Cloudl.png"
                  alt="Cloudle Logo"
                  width={48}
                  height={48}
                  className="object-contain"
                />
                <h1 className="text-xl font-semibold">Cloudle</h1>
              </div>
            </div>
            <div className="space-y-4">
              <blockquote className="text-2xl font-medium leading-relaxed">
                Cloud first Web Server
              </blockquote>
              <div className="text-white/60">
                <Image
                  src={myGif}
                  alt="login gif"
                  width={400}
                  height={400}
                  className="object-contain rounded-full mx-auto"
                />
                <p className="font-medium">Group 18</p>
                <p className="text-sm">Microsoft • SWEng</p>
              </div>
            </div>
          </div>

          {/* Right side (Login form) */}
          <div className="flex-1 flex flex-col bg-white">
            <div className="flex-1 flex items-center justify-center px-12">
              <div className="w-full max-w-sm space-y-8">
                <div className="space-y-2">
                  <h2 className="text-3xl font-bold tracking-tight text-blue-800">
                    User Login
                  </h2>
                  <p className="text-gray-500">
                    Log in with your org email and credentials
                  </p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4">
                  <div>
                    <label
                      htmlFor="tenantEmail"
                      className="block text-sm font-medium text-gray-700 mb-1"
                    >
                      Tenant Org Email
                    </label>
                    <Input
                      id="tenantEmail"
                      type="email"
                      placeholder="tenant@org.com"
                      value={tenantEmail}
                      onChange={(e) => setTenantEmail(e.target.value)}
                      required
                    />
                  </div>

                  <div className="text-sm text-center text-gray-500">
                    Want to create a new tenant organisation?{" "}
                    <button
                      type="button"
                      onClick={() => router.push("/create_tenant")}
                      className="text-blue-600 hover:underline font-medium"
                    >
                      Register Tenant
                    </button>
                  </div>

                  <div>
                    <label
                      htmlFor="userName"
                      className="block text-sm font-medium text-gray-700 mb-1"
                    >
                      User Name
                    </label>
                    <Input
                      id="userName"
                      type="text"
                      placeholder="Enter your username"
                      value={userName}
                      onChange={(e) => setUserName(e.target.value)}
                      required
                    />
                  </div>

                  <div>
                    <label
                      htmlFor="password"
                      className="block text-sm font-medium text-gray-700 mb-1"
                    >
                      Password
                    </label>
                    <Input
                      id="password"
                      type="password"
                      placeholder="Enter your password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      required
                    />
                  </div>

                  {errorMessage && (
                    <p className="text-red-500 text-sm">{errorMessage}</p>
                  )}

                  <Button
                    type="submit"
                    className="w-full bg-blue-600 text-white hover:bg-blue-700"
                    disabled={loading}
                  >
                    {loading ? "Logging in..." : "Sign In"}
                  </Button>

                  <div className="relative">
                    <div className="absolute inset-0 flex items-center">
                      <span className="w-full border-t" />
                    </div>
                    <div className="relative flex justify-center text-xs uppercase">
                      <span className="bg-white px-2 text-gray-500">Or</span>
                    </div>
                  </div>

                  <Button
                    onClick={handleRegister}
                    variant="outline"
                    className="w-full border-blue-600 text-blue-700 hover:bg-blue-50"
                  >
                    Register New Account
                  </Button>
                </form>
              </div>
            </div>

            <div className="p-6 text-center text-sm text-gray-500">
              By clicking continue, you agree to our{" "}
              <a
                href="#"
                className="underline underline-offset-4 hover:text-blue-800"
              >
                Terms of Service
              </a>{" "}
              and{" "}
              <a
                href="#"
                className="underline underline-offset-4 hover:text-blue-800"
              >
                Privacy Policy
              </a>
              .
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
