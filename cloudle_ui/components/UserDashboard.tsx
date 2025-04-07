
import React, { useState, useEffect, useRef, useCallback } from 'react';
import {
  RefreshCw,
  Plus,
  Activity,
  Settings,
  Terminal,
  Power,
  Upload,
  Loader2,
} from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
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
import { useToast } from "@/hooks/use-toast";

import {
  DBApp,
  createApp,
  uploadZip,
  setAppStatus,
  refreshDB,
  getTenantApps,
} from "@/components/api";

interface UserDashboardProps {
  onAppClick?: (appId: number) => void;
}

export const UserDashboard: React.FC<UserDashboardProps> = ({ onAppClick }) => {
  const [apps, setApps] = useState<DBApp[]>([]);

  // Dialog form states for creating a new app
  const [isNewAppDialogOpen, setIsNewAppDialogOpen] = useState(false);
  const [newApp, setNewApp] = useState({
    name: "",
    runtime: "",
    file: null as File | null,
  });

  // NEW: track creation flow specifically
  const [isCreatingApp, setIsCreatingApp] = useState(false);

  const fileInputRef = useRef<HTMLInputElement>(null);
  const { toast } = useToast();

  // Hardcoded for demonstration
  const userId = 3;
  const tenantId = 1101;

  // Track whether we’re in the middle of any API call (fetch apps, refresh, etc.)
  const [isLoading, setIsLoading] = useState(false);

  // === 1) Fetch list of apps
  const fetchApps = useCallback(async () => {
    setIsLoading(true);
    try {
      const data = await getTenantApps(tenantId);
      setApps(data);
    } catch (error) {
      console.error("Error fetching apps:", error);
      toast({
        title: "Error",
        description: String(error),
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  }, [tenantId, toast]);

  useEffect(() => {
    fetchApps();
  }, [fetchApps]);

  // === 2) Handle create app + (optionally) upload zip
  const handleCreateApp = async () => {
    if (!newApp.name || !newApp.runtime) {
      toast({
        title: "Error",
        description: "Please fill in all required fields",
        variant: "destructive",
      });
      return;
    }
    setIsCreatingApp(true);
    try {
      // 1) Create the app
      const created = await createApp(tenantId, newApp.name, newApp.runtime, userId);

      // 2) If file is selected, upload it
      if (newApp.file) {
        const arrayBuffer = await newApp.file.arrayBuffer();
        await uploadZip(tenantId, created.appId, new Uint8Array(arrayBuffer));
      }

      toast({ title: "Success", description: "Application created successfully" });

      // 3) Clear form + close dialog
      setIsNewAppDialogOpen(false);
      setNewApp({ name: "", runtime: "", file: null });

      // 4) Refresh the list
      await fetchApps();
    } catch (error) {
      console.error("Error creating app:", error);
      toast({
        title: "Error",
        description: "Failed to create application. Please try again.",
        variant: "destructive",
      });
      // If there's an error, we remain in the dialog. The user can fix + retry.
    } finally {
      setIsCreatingApp(false);
    }
  };

  // === 3) Toggle an app’s status (running/stopped)
  const toggleAppStatus = async (appId: number, currentStatus: string) => {
    setIsLoading(true);
    try {
      const newStatus = currentStatus === "running" ? "stopped" : "running";
      await setAppStatus(tenantId, appId, newStatus);
      toast({ title: "Success", description: `Application is now ${newStatus}` });
      await fetchApps();
    } catch (error) {
      console.error("Error toggling status:", error);
      toast({
        title: "Error",
        description: "Failed to update application status",
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  // === 4) Refresh DB
  const handleRefresh = async () => {
    setIsLoading(true);
    try {
      await refreshDB();
      await fetchApps();
    } catch (error) {
      console.error("Error refreshing DB:", error);
      toast({
        title: "Error",
        description: String(error),
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  // === 5) File selection & drag-drop
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

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-7xl mx-auto space-y-6">
        {/* === Header row === */}
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold">My Web Applications</h1>

          <div className="flex items-center gap-2">
            {/* Refresh icon button */}
            <Button variant="ghost" size="icon" onClick={handleRefresh}>
              {isLoading ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <RefreshCw className="h-4 w-4" />
              )}
            </Button>

            <Dialog open={isNewAppDialogOpen} onOpenChange={setIsNewAppDialogOpen}>
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
                      disabled={isCreatingApp}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label>Runtime Environment</Label>
                    <Select
                      value={newApp.runtime}
                      onValueChange={(value) =>
                        setNewApp((prev) => ({ ...prev, runtime: value }))
                      }
                      disabled={isCreatingApp}
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
                      onClick={() => !isCreatingApp && fileInputRef.current?.click()}
                      onDragOver={handleDragOver}
                      onDrop={handleDrop}
                    >
                      <input
                        type="file"
                        ref={fileInputRef}
                        className="hidden"
                        onChange={handleFileSelect}
                        accept=".html,.zip,.py"
                        disabled={isCreatingApp}
                      />
                      <Upload className="h-8 w-8 mx-auto mb-2 text-gray-400" />
                      {newApp.file ? (
                        <div className="text-sm text-gray-900">
                          Selected file: {newApp.file.name}
                        </div>
                      ) : (
                        <>
                          <p className="text-sm text-gray-500">
                            Drag and drop your app files here, or click to browse
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
                      disabled={isCreatingApp}
                    >
                      Cancel
                    </Button>
                    <Button
                      onClick={handleCreateApp}
                      disabled={!newApp.name || !newApp.runtime || isCreatingApp}
                    >
                      {isCreatingApp ? (
                        <>
                          <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                          Deploying...
                        </>
                      ) : (
                        <>Deploy Application</>
                      )}
                    </Button>
                  </div>
                </div>
              </DialogContent>
            </Dialog>
          </div>
        </div>

        {/* === Application List === */}
        <div className="grid gap-4">
          {apps.map((app) => (
            <Card
              key={app.appId}
              className="cursor-pointer hover:shadow-lg transition-shadow"
              onClick={() => onAppClick?.(app.appId)}
            >
              <CardContent className="p-6">
                <div className="flex items-center justify-between">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <h2 className="text-xl font-semibold">{app.name}</h2>
                      <Badge
                        variant={app.status === "running" ? "default" : "secondary"}
                      >
                        {app.status}
                      </Badge>
                    </div>
                    <div className="text-sm text-gray-500 space-x-4">
                      <span>Runtime: {app.runtime}</span>
                      <span>Tenant: {app.tenantId}</span>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
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
                        e.stopPropagation(); // prevent card click
                        toggleAppStatus(app.appId, app.status);
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
      </div>
    </div>
  );
};

export default UserDashboard;
