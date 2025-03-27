'use client';

import React, { useState, useEffect, useRef, useCallback } from 'react';
import { RefreshCw } from "lucide-react";
import { Card, CardContent, CardTitle, CardHeader } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import {
  Plus,
  Settings,
  Activity,
  Terminal,
  Power,
  Upload,
  ExternalLink,
} from "lucide-react";

import { useToast } from "@/hooks/use-toast";

// The interface for the apps your server sends back from /api/applications:
interface ServerApp {
  appId: number;
  userId: number;  // The server includes userId as well
  name: string;
  status: string;  // The server’s status is just a string
  runtime: string;
  routes?: string[];
}

// Your local interface for the displayed apps:
interface Application {
  id: number;
  name: string;
  status: "running" | "stopped";  // Strict literal type
  runtime: string;
  ssl: boolean;
  url?: string;
  // New field for the routes in the JSON
  routes?: string[];
}

interface UserDashboardProps {
  onAppClick?: (appId: number) => void;
}

export const UserDashboard: React.FC<UserDashboardProps> = ({ onAppClick }) => {
  const [apps, setApps] = useState<Application[]>([]);
  const [isNewAppDialogOpen, setIsNewAppDialogOpen] = useState(false);
  const [newApp, setNewApp] = useState({
    name: "",
    runtime: "",
    file: null as File | null,
  });

  const fileInputRef = useRef<HTMLInputElement>(null);
  const { toast } = useToast();

  // For demonstration, we assume userId = 3
  const userId = 3;

  // 1. Example: robustly handling errors in the client
  const fetchApps = useCallback(async () => {
    try {
      const response = await fetch(
          `http://localhost:8080/api/applications?userId=${userId}`,
          {
            method: "GET",
            headers: {
              Accept: "application/json",
            },
          }
      );

      if (!response.ok) {
        // The server responded, but not with a 2xx code
        throw new Error(`Server replied with HTTP ${response.status}`);
      }

      // We'll get an array of ServerApp objects:
      const data: ServerApp[] = await response.json();

      // Transform them into our local Application interface
      const transformed: Application[] = data.map((item) => ({
        id: item.appId,
        name: item.name,
        // Convert the server status to a strict literal "running" or "stopped"
        status: item.status === "running" ? "running" : "stopped",
        runtime: item.runtime,
        ssl: false,
        url: undefined,
        routes: item.routes || [],
      }));

      setApps(transformed);
    } catch (error) {
      console.error("Error fetching apps:", error);
      toast({
        title: "Error",
        description: String(error),
        variant: "destructive",
      });
    }
  }, [userId, toast]);

  useEffect(() => {
    fetchApps();
  }, [fetchApps]);

  // 2. Handling File selection
  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      setNewApp((prev) => ({ ...prev, file }));
    }
  };

  const handleDragOver = (event: React.DragEvent) => {
    event.preventDefault();
    event.stopPropagation();
  };

  const handleDrop = (event: React.DragEvent) => {
    event.preventDefault();
    event.stopPropagation();

    const file = event.dataTransfer.files?.[0];
    if (file) {
      setNewApp((prev) => ({ ...prev, file }));
    }
  };

  // 3. Create App + Upload
  const handleCreateApp = async () => {
    try {
      if (!newApp.name || !newApp.runtime) {
        toast({
          title: "Error",
          description: "Please fill in all required fields",
          variant: "destructive",
        });
        return;
      }

      // Create the application
      const resp = await fetch("http://localhost:8080/api/applications", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          userId,
          name: newApp.name,
          runtime: newApp.runtime,
        }),
      });
      if (!resp.ok) {
        throw new Error("Failed to create application");
      }
      const appData = await resp.json();

      // If file is selected, upload it
      if (newApp.file) {
        const formData = new FormData();
        formData.append("file", newApp.file);

        const uploadUrl = `http://localhost:8080/api/applications/upload?userId=${userId}&appId=${appData.appId}`;
        const uploadResp = await fetch(uploadUrl, {
          method: "POST",
          body: formData,
        });
        if (!uploadResp.ok) {
          throw new Error("Failed to upload file");
        }
      }

      setIsNewAppDialogOpen(false);
      setNewApp({ name: "", runtime: "", file: null });
      toast({
        title: "Success",
        description: "Application created successfully",
      });
      await fetchApps();
    } catch (error) {
      console.error("Error creating app:", error);
      toast({
        title: "Error",
        description: "Failed to create application. Please try again.",
        variant: "destructive",
      });
    }
  };

  // 4. Toggling App Status
  const toggleAppStatus = async (appId: number, currentStatus: string) => {
    try {
      const newStatus = currentStatus === "running" ? "stopped" : "running";
      const response = await fetch(
          `http://localhost:8080/api/applications/status?userId=${userId}&appId=${appId}&status=${newStatus}`,
          { method: "PUT" }
      );
      if (!response.ok) {
        throw new Error("Failed to update status");
      }

      toast({
        title: "Success",
        description: `Application ${
            newStatus === "running" ? "started" : "stopped"
        } successfully`,
      });
      fetchApps();
    } catch (error) {
      console.error("Error toggling status:", error);
      toast({
        title: "Error",
        description: "Failed to update application status",
        variant: "destructive",
      });
    }
  };

  const getStatusBadgeVariant = (status: string) => {
    return status === "running" ? "default" : "secondary";
  };

  // function to handle refresh
  const handleRefresh = async () => {
    try {
      // This calls your /api/refresh route, which forces the server to DB.load()
      await fetch("http://localhost:8080/api/refresh", {
        method: "POST",
      });

      // Then re-fetch the apps so the UI sees the updated data
      await fetchApps();
    } catch (error) {
      console.error("Error refreshing DB:", error);
      toast({
        title: "Error",
        description: String(error),
        variant: "destructive",
      });
    }
  };

  return (
      <div className="min-h-screen bg-gray-50 p-6">
        <div className="max-w-7xl mx-auto space-y-6">
          {/* Header with "Refresh" and "New Application" buttons */}
          <div className="flex justify-between items-center">
            <div className="flex items-center gap-2">
              <Button variant="outline" onClick={handleRefresh}>
                <RefreshCw className="h-4 w-4 mr-2" />
                Refresh
              </Button>
              <h1 className="text-3xl font-bold">My Web Applications</h1>
            </div>

            <Dialog
                open={isNewAppDialogOpen}
                onOpenChange={setIsNewAppDialogOpen}
            >
              <DialogTrigger asChild>
                <Button>
                  <Plus className="h-4 w-4 mr-2" />
                  New Application
                </Button>
              </DialogTrigger>
              <DialogContent className="sm:max-w-[600px]">
                <DialogHeader>
                  <DialogTitle>Deploy New Application</DialogTitle>
                </DialogHeader>
                <div className="space-y-6 py-4">
                  <div className="space-y-2">
                    <Label>Application Name</Label>
                    <Input
                        placeholder="my-awesome-app"
                        value={newApp.name}
                        onChange={(e) =>
                            setNewApp((prev) => ({ ...prev, name: e.target.value }))
                        }
                    />
                  </div>

                  <div className="space-y-2">
                    <Label>Runtime Environment</Label>
                    <Select
                        value={newApp.runtime}
                        onValueChange={(value) =>
                            setNewApp((prev) => ({ ...prev, runtime: value }))
                        }
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Select runtime" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="php">PHP</SelectItem>
                        <SelectItem value="nodejs">NodeJS</SelectItem>
                        <SelectItem value="dotnet">ASP.NET</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="space-y-2">
                    <Label>Application Files</Label>
                    <div
                        className="border-2 border-dashed rounded-lg p-6 text-center cursor-pointer"
                        onClick={() => fileInputRef.current?.click()}
                        onDragOver={handleDragOver}
                        onDrop={handleDrop}
                    >
                      <input
                          type="file"
                          ref={fileInputRef}
                          className="hidden"
                          onChange={handleFileSelect}
                          accept=".html,.zip,.py"
                      />
                      <Upload className="h-8 w-8 mx-auto mb-2 text-gray-400" />
                      {newApp.file ? (
                          <div className="text-sm text-gray-900">
                            Selected file: {newApp.file.name}
                          </div>
                      ) : (
                          <>
                            <p className="text-sm text-gray-500">
                              Drag and drop your application files here, or click to
                              browse
                            </p>
                            <p className="text-xs text-gray-400 mt-2">
                              Supported files: .html, .zip, .py
                            </p>
                          </>
                      )}
                    </div>
                  </div>

                  <div className="flex justify-end space-x-2">
                    <Button
                        variant="outline"
                        onClick={() => setIsNewAppDialogOpen(false)}
                    >
                      Cancel
                    </Button>
                    <Button
                        onClick={handleCreateApp}
                        disabled={!newApp.name || !newApp.runtime}
                    >
                      Deploy Application
                    </Button>
                  </div>
                </div>
              </DialogContent>
            </Dialog>
          </div>

          {/* Application List */}
          <div className="grid gap-4">
            {apps.map((app) => (
                <Card
                    key={app.id}
                    className="cursor-pointer hover:shadow-lg transition-shadow"
                    onClick={() => onAppClick?.(app.id)}
                >
                  <CardContent className="p-6">
                    <div className="flex items-center justify-between">
                      <div className="space-y-1">
                        <div className="flex items-center gap-2">
                          <h2 className="text-xl font-semibold">{app.name}</h2>
                          <Badge variant={getStatusBadgeVariant(app.status)}>
                            {app.status}
                          </Badge>
                        </div>
                        <div className="text-sm text-gray-500 space-x-4">
                          <span>Runtime: {app.runtime}</span>
                          <span>SSL: {app.ssl ? "Enabled" : "Disabled"}</span>
                          {app.url && (
                              <Button
                                  variant="link"
                                  className="p-0 h-auto text-sm"
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    window.open(app.url, "_blank");
                                  }}
                              >
                                <ExternalLink className="h-4 w-4 mr-1" />
                                Open Application
                              </Button>
                          )}
                        </div>
                      </div>

                      <div className="flex items-center gap-2">
                        {/* Some example actions (Metrics, Settings, Logs, Start/Stop) */}
                        <Button variant="outline" size="sm">
                          <Activity className="h-4 w-4 mr-2" />
                          Metrics
                        </Button>
                        <Button variant="outline" size="sm">
                          <Settings className="h-4 w-4 mr-2" />
                          Settings
                        </Button>
                        <Button variant="outline" size="sm">
                          <Terminal className="h-4 w-4 mr-2" />
                          Logs
                        </Button>
                        <Button
                            variant={app.status === "running" ? "destructive" : "default"}
                            size="sm"
                            onClick={(e) => {
                              e.stopPropagation();
                              toggleAppStatus(app.id, app.status);
                            }}
                        >
                          <Power className="h-4 w-4 mr-2" />
                          {app.status === "running" ? "Stop" : "Start"}
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
            ))}
          </div>

          {/* Quick Start Guide or Additional Info */}
          <Card>
            <CardHeader>
              <CardTitle>Application Deployment Guide</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="flex items-start gap-4">
                  <div className="flex-none w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center text-blue-600">
                    1
                  </div>
                  <div>
                    <h3 className="font-medium">Prepare your application</h3>
                    <p className="text-sm text-gray-500">
                      Create your application files (HTML, Node, etc.) and ensure
                      they work locally.
                    </p>
                  </div>
                </div>
                <div className="flex items-start gap-4">
                  <div className="flex-none w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center text-blue-600">
                    2
                  </div>
                  <div>
                    <h3 className="font-medium">Upload your files</h3>
                    <p className="text-sm text-gray-500">
                      Use our interface to deploy your files to Azure.
                    </p>
                  </div>
                </div>
                <div className="flex items-start gap-4">
                  <div className="flex-none w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center text-blue-600">
                    3
                  </div>
                  <div>
                    <h3 className="font-medium">Configure runtime</h3>
                    <p className="text-sm text-gray-500">
                      Select your app’s runtime environment and version if needed.
                    </p>
                  </div>
                </div>
                <div className="flex items-start gap-4">
                  <div className="flex-none w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center text-blue-600">
                    4
                  </div>
                  <div>
                    <h3 className="font-medium">Deploy and start</h3>
                    <p className="text-sm text-gray-500">
                      Deploy your application, then click “Start” once you’re
                      ready to go live.
                    </p>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
  );
};

export default UserDashboard;
