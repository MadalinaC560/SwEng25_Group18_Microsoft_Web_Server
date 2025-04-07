import React, { useState, useEffect, useCallback } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Switch } from "@/components/ui/switch";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogFooter,
    DialogTrigger,
} from "@/components/ui/dialog";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import {
    ArrowLeft,
    Activity,
    Power,
    Globe,
    Database,
    GitBranch,
    AlertCircle,
    ExternalLink,
    Upload,
    ArrowUpRight,
    ArrowDownRight,
    Download,
    RefreshCw,
    ChevronLeft,
    ChevronRight,
    Loader2,
} from 'lucide-react';

import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import {
  getTenantApp,
  updateTenantApp,
  deleteTenantApp,
  getTenantAppMetrics,
} from '@/components/api';
import type { DBApp } from '@/components/api';
import type { AppMetrics } from '@/components/api';  // the interface from the step above

import { useToast } from "@/hooks/use-toast";

interface ApplicationData extends Partial<DBApp> {
  sslStatus?: string;
  appUrl?: string;
}

const SERVER_BASE_URL = process.env.NEXT_PUBLIC_SERVER_BASE_URL || "http://localhost:8080";

interface ApplicationDetailsProps {
  appId: number;
  onBack: () => void;
}

export const ApplicationDetails: React.FC<ApplicationDetailsProps> = ({
  appId,
  onBack
}) => {
  // We now read tenantId from localStorage
  const [tenantId, setTenantId] = useState<number | null>(null);

  // Local states
  const [activeTab, setActiveTab] = useState('overview');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [appData, setAppData] = useState<ApplicationData>({
    appId: 0,
    tenantId: 0,
    ownerUserId: 0,
    name: '',
    runtime: '',
    status: '',
    routes: [],
    sslStatus: '',
    appUrl: 'index.html',
  });

  const [appMetrics, setAppMetrics] = useState<AppMetrics | null>(null);
  const [isMetricsLoading, setIsMetricsLoading] = useState(false);
  const [metricsError, setMetricsError] = useState<string | null>(null);

  const [deleteConfirmText, setDeleteConfirmText] = useState('');
  const [isDeleting, setIsDeleting] = useState(false);
  const { toast } = useToast();

  // -------------------------------
  // 1) On mount, load tenantId from localStorage
  // -------------------------------
  useEffect(() => {
    const rawUser = localStorage.getItem("user");
    if (!rawUser) {
      // If there's no user in local storage, we can't fetch anything
      setTenantId(null);
      return;
    }
    try {
      const userObj = JSON.parse(rawUser);
      setTenantId(userObj.tenantId ?? null);
    } catch (parseErr) {
      console.error("Failed to parse user from localStorage:", parseErr);
      setTenantId(null);
    }
  }, []);

  // -------------------------------
  // 2) Data fetching
  // -------------------------------
  const fetchInitialData = useCallback(async () => {
    if (tenantId === null) return;
    setIsLoading(true);
    setError(null);
    try {
      const dbApp = await getTenantApp(tenantId, appId);
      setAppData({
        ...dbApp,
        sslStatus: 'Active',
        appUrl: 'index.html',
      });
    } catch (err) {
      setError("Failed to load application data. Please try again later.");
      console.error("Error loading initial data:", err);
    } finally {
      setIsLoading(false);
    }
  }, [tenantId, appId]);

  const fetchAppMetrics = useCallback(async () => {
    if (tenantId === null) return;
    setIsMetricsLoading(true);
    setMetricsError(null);
    try {
      const data = await getTenantAppMetrics(tenantId, appId);
      setAppMetrics(data);
    } catch (err: unknown) {
      setMetricsError("Failed to load application metrics...");
      console.error("Error loading app metrics:", err);
      if (err instanceof Error) {
        console.error("Error message:", err.message);
      }
    } finally {
      setIsMetricsLoading(false);
    }
  }, [tenantId, appId]);

  // -------------------------------
  // 3) Re-run fetches if tenantId changes or appId changes
  // -------------------------------
  useEffect(() => {
    if (tenantId !== null) {
      fetchInitialData();
      fetchAppMetrics();
    }
  }, [tenantId, appId, fetchInitialData, fetchAppMetrics]);

  // -------------------------------
  // 4) Handlers
  // -------------------------------
  const handleToggleStatus = async () => {
    if (tenantId === null) return;
    if (!appData || !appData.appId) return;
    const newStatus = appData.status === 'running' ? 'stopped' : 'running';

    try {
      const updated = await updateTenantApp(tenantId, appData.appId, { status: newStatus });
      setAppData((prev) => ({
        ...prev,
        status: updated.status,
      }));
      toast({
        title: "Status Updated",
        description: `Application is now ${updated.status}`,
      });
    } catch (err) {
      console.error('Error toggling status:', err);
      toast({
        title: "Error",
        description: "Failed to update application status. Please try again.",
        variant: "destructive",
      });
    }
  };

  const handleRefresh = async () => {
    setIsLoading(true);
    setIsMetricsLoading(true);
    try {
      await Promise.all([fetchInitialData(), fetchAppMetrics()]);
    } catch (error) {
      console.error("Error refreshing data:", error);
    } finally {
      setIsLoading(false);
      setIsMetricsLoading(false);
    }
  };

  const handleDeleteApp = async () => {
    if (tenantId === null) return;
    if (!appData || !appData.appId) return;
    setIsDeleting(true);
    try {
      await deleteTenantApp(tenantId, appData.appId);
      toast({
        title: "Application Deleted",
        description: `App ${appData.name} has been removed.`,
      });
      onBack();
    } catch (err) {
      console.error("Error deleting app:", err);
      toast({
        title: "Error",
        description: "Failed to delete application. Please try again.",
        variant: "destructive",
      });
    } finally {
      setIsDeleting(false);
    }
  };

  const handleTestLoad = () => {
    if (appData.status !== 'running') return;
    toast({
      title: "Load Test Started",
      description: "Simulating 500 requests... (purely local mock)",
    });
  };

  // -------------------------------
  // 5) Rendering
  // -------------------------------
  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 p-6">
        <div className="max-w-7xl mx-auto">
          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <h2 className="text-red-800 font-medium">Error</h2>
            <p className="text-red-600">{error}</p>
            <Button onClick={() => window.location.reload()} className="mt-4">
              Retry
            </Button>
          </div>
        </div>
      </div>
    );
  }

  // If tenantId is still null, show a loading or a “No tenant found” message
  if (tenantId === null) {
    return (
      <div className="min-h-screen bg-gray-50 p-6">
        <div className="max-w-7xl mx-auto text-center">
          <h2 className="text-xl text-gray-500">
            No tenant available in localStorage. Please log in or check your storage.
          </h2>
        </div>
      </div>
    );
  }

  const isAppDataLoading = isLoading;
  const isAppMetricsLoading = isMetricsLoading;

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-7xl mx-auto space-y-6">
        {/* Header */}
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={onBack}>
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div className="flex-1">
            <h1 className="text-3xl font-bold">
              {isAppDataLoading ? 'Loading...' : appData.name}
            </h1>
            <div className="flex items-center gap-2 mt-1">
              <Badge variant={appData.status === 'running' ? 'default' : 'secondary'}>
                {appData.status}
              </Badge>
              {appData.appId !== 0 && (
                <a
                  href={`${SERVER_BASE_URL}/app_${appData.appId}/${appData.appUrl}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-sm text-gray-500 hover:text-gray-700 flex items-center gap-1"
                >
                  {`${SERVER_BASE_URL}/app_${appData.appId}/${appData.appUrl}`}
                  <ExternalLink className="h-3 w-3" />
                </a>
              )}
            </div>
          </div>
          <div className="flex items-center gap-2">
            {/* Refresh button */}
            <Button
              variant="ghost"
              size="icon"
              onClick={handleRefresh}
              disabled={isLoading || isMetricsLoading}
            >
              {(isLoading || isMetricsLoading)
                ? <RefreshCw className="h-4 w-4 animate-spin" />
                : <RefreshCw className="h-4 w-4" />
              }
            </Button>

            {/* Test Load */}
            <Button
              variant="outline"
              disabled={isAppDataLoading || appData.status !== 'running'}
              onClick={handleTestLoad}
            >
              <Activity className="h-4 w-4 mr-2" />
              Test Load (500 requests)
            </Button>

            {/* Start/Stop app */}
            <Button
              variant={appData.status === 'running' ? 'destructive' : 'default'}
              disabled={isAppDataLoading}
              onClick={handleToggleStatus}
            >
              <Power className="h-4 w-4 mr-2" />
              {appData.status === 'running' ? 'Stop' : 'Start'} Application
            </Button>
          </div>
        </div>

        {/* Tabs */}
        <Tabs value={activeTab} onValueChange={setActiveTab}>
          <TabsList>
            <TabsTrigger value="overview">Overview</TabsTrigger>
            <TabsTrigger value="metrics">Metrics</TabsTrigger>
            <TabsTrigger value="settings">Settings</TabsTrigger>
            <TabsTrigger value="logs">Logs</TabsTrigger>
          </TabsList>

          {/* OVERVIEW TAB */}
          <TabsContent value="overview" className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <Card>
                <CardContent className="pt-6">
                  <div className="flex items-center gap-2">
                    <Globe className="h-5 w-5 text-blue-500" />
                    <div>
                      <p className="text-sm font-medium">Request Throughput</p>
                      <p className="text-2xl font-bold">
                        {isAppMetricsLoading || !appMetrics
                          ? '...'
                          : appMetrics.requestThroughput
                        }
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="pt-6">
                  <div className="flex items-center gap-2">
                    <Activity className="h-5 w-5 text-green-500" />
                    <div>
                      <p className="text-sm font-medium">Avg Response Time</p>
                      <p className="text-2xl font-bold">
                        {isAppMetricsLoading || !appMetrics
                          ? '...'
                          : `${appMetrics.avgResponseTime.toFixed(1)} ms`}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="pt-6">
                  <div className="flex items-center gap-2">
                    <AlertCircle className="h-5 w-5 text-red-500" />
                    <div>
                      <p className="text-sm font-medium">Error Rate</p>
                      <p className="text-2xl font-bold">
                        {isAppMetricsLoading || !appMetrics
                          ? '...'
                          : `${appMetrics.errorRate.toFixed(2)}%`}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="pt-6">
                  <div className="flex items-center gap-2">
                    <Database className="h-5 w-5 text-purple-500" />
                    <div>
                      <p className="text-sm font-medium">Availability</p>
                      <p className="text-2xl font-bold">
                        {isAppMetricsLoading || !appMetrics
                          ? '...'
                          : `${appMetrics.availability.toFixed(2)}%`}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>

            <Card>
              <CardHeader>
                <CardTitle>Performance Overview</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="h-80">
                  {metricsError && (
                    <div className="text-red-500">{metricsError}</div>
                  )}
                  {isAppMetricsLoading ? (
                    <p>Loading chart...</p>
                  ) : appMetrics && appMetrics.performanceData.length > 0 ? (
                    <ResponsiveContainer width="100%" height="100%">
                      <LineChart data={appMetrics.performanceData}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="time" />
                        <YAxis />
                        <Tooltip />
                        <Legend />
                        <Line
                          type="monotone"
                          dataKey="someKeyIfYouHaveIt"
                          stroke="#8884d8"
                          name="Response Time (ms)"
                          dot={false}
                        />
                      </LineChart>
                    </ResponsiveContainer>
                  ) : (
                    <p className="text-sm text-gray-500">
                      No performance data available.
                    </p>
                  )}
                </div>
              </CardContent>
            </Card>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle>Configuration</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <Label className="text-sm text-gray-500">Runtime</Label>
                      <p className="font-medium">{isLoading ? '...' : appData.runtime}</p>
                    </div>
                    <div>
                      <Label className="text-sm text-gray-500">Environment</Label>
                      <p className="font-medium">{isLoading ? '...' : 'Dev'}</p>
                    </div>
                    <div>
                      <Label className="text-sm text-gray-500">SSL Status</Label>
                      <p className="font-medium text-green-600">
                        {isLoading ? '...' : (appData.sslStatus || 'Active')}
                      </p>
                    </div>
                    <div>
                      <Label className="text-sm text-gray-500">Auto Scaling</Label>
                      <p className="font-medium">{isLoading ? '...' : 'Azure Enabled'}</p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* SSL & Security */}
              <Card>
                <CardHeader>
                  <CardTitle>SSL & Security</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-1 gap-6">
                    {/* SSL Certificate */}
                    <div className="border rounded-lg p-4">
                      <div className="flex items-center gap-3 mb-4">
                        <div
                          className={`h-4 w-4 rounded-full ${
                            appData.sslStatus === 'Active' ? 'bg-green-500' : 'bg-yellow-500'
                          }`}
                        />
                        <h3 className="font-medium">SSL Certificate</h3>
                      </div>
                      <div className="grid grid-cols-[120px_1fr] gap-y-2 text-sm">
                        <span className="text-muted-foreground">Domain:</span>
                        <span>www.{appData.name}.cloudle.com</span>

                        <span className="text-muted-foreground">Status:</span>
                        <span>{appData.sslStatus === 'Active' ? 'Valid and active' : 'Pending'}</span>

                        <span className="text-muted-foreground">Issuer:</span>
                        <span>Let us Encrypt</span>

                        <span className="text-muted-foreground">Expires in:</span>
                        <span>72 days</span>

                        <span className="text-muted-foreground">Auto-renewal:</span>
                        <span>Enabled</span>

                        <span className="text-muted-foreground">Protocols:</span>
                        <span>TLS 1.2, TLS 1.3</span>
                      </div>
                      <Button variant="outline" size="sm" className="mt-4">
                        Manage Certificate
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          {/* METRICS TAB */}
          <TabsContent value="metrics" className="space-y-6">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between">
                <CardTitle>Application Performance</CardTitle>
                <Select defaultValue="24h">
                  <SelectTrigger className="w-36">
                    <SelectValue placeholder="Time Range" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="1h">Last Hour</SelectItem>
                    <SelectItem value="6h">Last 6 Hours</SelectItem>
                    <SelectItem value="24h">Last 24 Hours</SelectItem>
                    <SelectItem value="7d">Last 7 Days</SelectItem>
                    <SelectItem value="30d">Last 30 Days</SelectItem>
                  </SelectContent>
                </Select>
              </CardHeader>
              <CardContent>
                {metricsError && (
                  <div className="text-red-500">{metricsError}</div>
                )}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                  <div className="flex flex-col space-y-2">
                    <span className="text-muted-foreground text-xs">Avg. Response Time</span>
                    <div className="flex items-end gap-2">
                      <span className="text-2xl font-bold">
                        {isAppMetricsLoading || !appMetrics
                          ? '...'
                          : `${appMetrics.avgResponseTime.toFixed(1)}ms`}
                      </span>
                      <span className="text-xs text-green-500 flex items-center mb-1">
                        -12.4% <ArrowDownRight className="h-3 w-3" />
                      </span>
                    </div>
                    <div className="h-10">
                      {(!appMetrics || appMetrics.performanceData.length === 0) ? (
                        <p className="text-sm text-gray-400">No data</p>
                      ) : (
                        <ResponsiveContainer width="100%" height="100%">
                          <LineChart data={appMetrics.performanceData.slice(-12)}>
                            <Line
                              type="monotone"
                              dataKey="someKeyIfYouHaveIt"
                              stroke="#10b981"
                              dot={false}
                              strokeWidth={2}
                            />
                          </LineChart>
                        </ResponsiveContainer>
                      )}
                    </div>
                  </div>
                  <div className="flex flex-col space-y-2">
                    <span className="text-muted-foreground text-xs">Request Throughput</span>
                    <div className="flex items-end gap-2">
                      <span className="text-2xl font-bold">
                        {isAppMetricsLoading || !appMetrics
                          ? '...'
                          : `${Math.round(appMetrics.requestThroughput)} total`
                        }
                      </span>
                      <span className="text-xs text-green-500 flex items-center mb-1">
                        +8.2% <ArrowUpRight className="h-3 w-3" />
                      </span>
                    </div>
                  </div>
                  <div className="flex flex-col space-y-2">
                    <span className="text-muted-foreground text-xs">Error Rate</span>
                    <div className="flex items-end gap-2">
                      <span className="text-2xl font-bold">
                        {isAppMetricsLoading || !appMetrics
                          ? '...'
                          : `${appMetrics.errorRate.toFixed(2)}%`
                        }
                      </span>
                      <span className="text-xs text-red-500 flex items-center mb-1">
                        +0.8% <ArrowUpRight className="h-3 w-3" />
                      </span>
                    </div>
                  </div>
                  <div className="flex flex-col space-y-2">
                    <span className="text-muted-foreground text-xs">Availability</span>
                    <div className="flex items-end gap-2">
                      <span className="text-2xl font-bold">
                        {isAppMetricsLoading || !appMetrics
                          ? '...'
                          : `${appMetrics.availability.toFixed(2)}%`
                        }
                      </span>
                      <span className="text-xs text-green-500 flex items-center mb-1">
                        +0.01% <ArrowUpRight className="h-3 w-3" />
                      </span>
                    </div>
                    <div className="h-10 flex items-end">
                      <div className="w-full bg-muted rounded-sm h-2">
                        <div
                          className="bg-green-500 h-2 rounded-sm"
                          style={{
                            width: appMetrics
                              ? `${appMetrics.availability.toFixed(2)}%`
                              : '0%',
                          }}
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>HTTP Method Distribution</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {/* HTTP Method Distribution */}
                  <div>
                    <h3 className="font-medium mb-4">Method Breakdown</h3>
                    <div className="space-y-3">
                      <div className="space-y-1">
                        <div className="flex items-center justify-between">
                          <span className="text-sm">GET</span>
                          <span className="text-sm font-medium">72.5%</span>
                        </div>
                        <div className="h-2 bg-muted rounded-full overflow-hidden">
                          <div
                            className="bg-blue-500 h-full rounded-full"
                            style={{ width: '72.5%' }}
                          />
                        </div>
                      </div>
                      <div className="space-y-1">
                        <div className="flex items-center justify-between">
                          <span className="text-sm">POST</span>
                          <span className="text-sm font-medium">17.2%</span>
                        </div>
                        <div className="h-2 bg-muted rounded-full overflow-hidden">
                          <div
                            className="bg-green-500 h-full rounded-full"
                            style={{ width: '17.2%' }}
                          />
                        </div>
                      </div>
                      <div className="space-y-1">
                        <div className="flex items-center justify-between">
                          <span className="text-sm">PUT</span>
                          <span className="text-sm font-medium">7.1%</span>
                        </div>
                        <div className="h-2 bg-muted rounded-full overflow-hidden">
                          <div
                            className="bg-yellow-500 h-full rounded-full"
                            style={{ width: '7.1%' }}
                          />
                        </div>
                      </div>
                      <div className="space-y-1">
                        <div className="flex items-center justify-between">
                          <span className="text-sm">DELETE</span>
                          <span className="text-sm font-medium">3.2%</span>
                        </div>
                        <div className="h-2 bg-muted rounded-full overflow-hidden">
                          <div
                            className="bg-red-500 h-full rounded-full"
                            style={{ width: '3.2%' }}
                          />
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* File routes => Show actual file routes from appData.routes */}
                  <div>
                    <h3 className="font-medium mb-4">File Routes</h3>
                    {(!appData.routes || appData.routes.length === 0) ? (
                      <p className="text-sm text-gray-500">
                        No routes found for this application.
                      </p>
                    ) : (
                      <div className="space-y-3">
                        {appData.routes.map((route, index) => (
                          <div
                            key={index}
                            className="flex justify-between items-center"
                          >
                            <div className="flex items-center gap-2">
                              <Badge variant="outline" className="bg-blue-50">
                                GET
                              </Badge>
                              <span className="text-sm font-medium">
                                {route}
                              </span>
                            </div>
                            <div className="text-sm text-muted-foreground">
                              42 requests | 65ms avg
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Resource Usage */}
            <Card>
              <CardContent>
                <div className="mt-3 pt-3">
                  <h3 className="font-medium mb-4">Bandwidth Usage</h3>
                  <div className="h-64">
                    <ResponsiveContainer width="100%" height="100%">
                      <LineChart data={appMetrics?.performanceData || []}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="time" />
                        <YAxis tickFormatter={(value) => `${value.toFixed(2)}MB`} />
                        <Tooltip formatter={(val) => `${(val as number).toFixed(2)} MB`} />
                        <Legend />
                        <Line
                          type="monotone"
                          dataKey="inbound"
                          name="Inbound Traffic"
                          stroke="#3b82f6"
                        />
                        <Line
                          type="monotone"
                          dataKey="outbound"
                          name="Outbound Traffic"
                          stroke="#f97316"
                        />
                      </LineChart>
                    </ResponsiveContainer>
                  </div>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          {/* SETTINGS TAB */}
          <TabsContent value="settings">
            <div className="space-y-6">
              {/* General Settings */}
              <Card>
                <CardHeader>
                  <CardTitle>General Settings</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-1 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="app-name">Application Name</Label>
                      <div className="flex space-x-2">
                        <Input id="app-name" defaultValue={appData.name} className="max-w-md"/>
                        <Button variant="outline" size="sm">
                          Update
                        </Button>
                      </div>
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="environment">Environment</Label>
                      <Select defaultValue={'Dev'}>
                        <SelectTrigger className="max-w-md">
                          <SelectValue placeholder="Select environment"/>
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="Development">Development</SelectItem>
                          <SelectItem value="Staging">Staging</SelectItem>
                          <SelectItem value="Production">Production</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="auto-scaling">Auto Scaling</Label>
                      <div className="flex items-center space-x-2">
                        <Switch id="auto-scaling" checked={'Enabled' === 'Enabled'} />
                        <span>{'Enabled' === 'Enabled' ? 'Enabled' : 'Disabled'}</span>
                      </div>
                      <p className="text-sm text-muted-foreground">
                        Automatically scaled by Azure
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Deployment settings */}
              <Card>
                <CardHeader>
                  <CardTitle>Deployment</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div>
                    <Label className="text-sm text-gray-500">Current Version</Label>
                    <div className="flex items-center gap-2">
                      <GitBranch className="h-4 w-4" />
                      <p className="font-medium">--</p>
                    </div>
                  </div>
                  <div>
                    <Label className="text-sm text-gray-500">Last Deployment</Label>
                    <p className="font-medium">--</p>
                  </div>
                  <Button variant="outline" className="w-full" disabled={isLoading}>
                    Deploy New Version
                  </Button>
                </CardContent>
              </Card>

              {/* SSL Settings */}
              <Card>
                <CardHeader>
                  <CardTitle>SSL Certificate</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex items-center gap-2">
                    <div
                      className={`h-3 w-3 rounded-full ${
                        appData.sslStatus === 'Active' ? 'bg-green-500' : 'bg-yellow-500'
                      }`}
                    />
                    <span className="font-medium">
                      {appData.sslStatus === 'Active' ? 'SSL Certificate Active' : 'SSL Certificate Pending'}
                    </span>
                  </div>

                  <div className="grid gap-2">
                    <Label>Upload SSL Certificate</Label>
                    <div
                      className="border-2 border-dashed rounded-lg p-6 text-center cursor-pointer"
                    >
                      <input
                        type="file"
                        className="hidden"
                        accept=".crt,.pem,.key"
                      />
                      <Upload className="h-8 w-8 mx-auto mb-2 text-gray-400"/>
                      <p className="text-sm text-gray-500">
                        Drag and drop your SSL certificate files here, or click to browse
                      </p>
                      <p className="text-xs text-gray-400 mt-2">
                        Supported files: .crt, .pem, .key
                      </p>
                    </div>
                    <Button className="mt-2">
                      Upload Certificate
                    </Button>
                  </div>
                </CardContent>
              </Card>

              {/* Danger Zone */}
              <Card className="border-destructive">
                <CardHeader className="text-destructive">
                  <CardTitle>Danger Zone</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="rounded-md border border-destructive p-4">
                    <div className="flex justify-between items-center">
                      <div>
                        <h3 className="font-medium">Delete this application</h3>
                        <p className="text-sm text-muted-foreground">
                          Once deleted, it cannot be recovered. All data will be permanently removed.
                        </p>
                      </div>
                      <Dialog>
                        <DialogTrigger asChild>
                          <Button variant="destructive">
                            Delete Application
                          </Button>
                        </DialogTrigger>
                        <DialogContent>
                          <DialogHeader>
                            <DialogTitle>Are you absolutely sure?</DialogTitle>
                          </DialogHeader>
                          <div className="py-4">
                            <p className="text-muted-foreground">
                              This action cannot be undone. This will permanently
                              delete your application <span className="font-semibold">{appData.name}</span> and all associated data.
                            </p>
                            <div className="mt-4 space-y-2">
                              <Label htmlFor="confirm">
                                Type <span className="font-semibold">{appData.name}</span> to confirm
                              </Label>
                              <Input
                                id="confirm"
                                placeholder={`Type ${appData.name} to confirm`}
                                value={deleteConfirmText}
                                onChange={(e) => setDeleteConfirmText(e.target.value)}
                              />
                            </div>
                          </div>
                          <DialogFooter>
                            <Button variant="outline" onClick={() => setIsDeleting(false)}>
                              Cancel
                            </Button>
                            <Button
                              variant="destructive"
                              disabled={isDeleting}
                              onClick={() => {
                                if (deleteConfirmText === appData.name) {
                                  handleDeleteApp();
                                } else {
                                  toast({
                                    title: "Confirmation mismatch",
                                    description: "Please type the app name exactly to confirm.",
                                    variant: "destructive",
                                  });
                                }
                              }}
                            >
                              {isDeleting ? (
                                <>
                                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                                  Deleting...
                                </>
                              ) : (
                                "Delete Permanently"
                              )}
                            </Button>
                          </DialogFooter>
                        </DialogContent>
                      </Dialog>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          {/* LOGS TAB */}
          <TabsContent value="logs" className="space-y-6">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between">
                <CardTitle>Application Logs</CardTitle>
                <div className="flex items-center gap-2">
                  <Select defaultValue="all">
                    <SelectTrigger className="w-[150px]">
                      <SelectValue placeholder="Log Level" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Levels</SelectItem>
                      <SelectItem value="error">Errors</SelectItem>
                      <SelectItem value="warn">Warnings</SelectItem>
                      <SelectItem value="info">Info</SelectItem>
                      <SelectItem value="debug">Debug</SelectItem>
                    </SelectContent>
                  </Select>
                  <Input placeholder="Search logs..." className="w-[200px]" />
                  <Button variant="outline" size="icon">
                    <RefreshCw className="h-4 w-4" />
                  </Button>
                  <Button variant="outline" size="sm">
                    <Download className="h-4 w-4 mr-2" />
                    Export
                  </Button>
                </div>
              </CardHeader>
              <CardContent className="p-0">
                <div className="border-b flex items-center justify-between px-4 py-2 bg-muted/50">
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-medium">Showing most recent logs</span>
                    <Badge variant="outline">Live</Badge>
                  </div>
                  <div className="flex items-center gap-2">
                    <Switch id="auto-refresh" />
                    <Label htmlFor="auto-refresh" className="text-sm">Auto-refresh</Label>
                  </div>
                </div>

                <div className="max-h-[600px] overflow-auto font-mono text-sm">
                  <table className="w-full">
                    <tbody>
                      <tr className="border-b hover:bg-muted/50">
                        <td className="whitespace-nowrap py-2 px-4 text-muted-foreground">2024-02-18 14:32:45</td>
                        <td className="whitespace-nowrap py-2 px-4">
                          <Badge variant="outline" className="bg-red-50 text-red-700 hover:bg-red-50">ERROR</Badge>
                        </td>
                        <td className="py-2 px-4 text-red-600">
                          Failed to connect to database: Connection timed out after 5000ms
                        </td>
                      </tr>
                      <tr className="border-b hover:bg-muted/50">
                        <td className="whitespace-nowrap py-2 px-4 text-muted-foreground">2024-02-18 14:32:42</td>
                        <td className="whitespace-nowrap py-2 px-4">
                          <Badge variant="outline" className="bg-yellow-50 text-yellow-700 hover:bg-yellow-50">WARN</Badge>
                        </td>
                        <td className="py-2 px-4 text-yellow-600">
                          Slow query detected: SELECT * FROM products WHERE category_id = 5 (took 2134ms)
                        </td>
                      </tr>
                      <tr className="border-b hover:bg-muted/50">
                        <td className="whitespace-nowrap py-2 px-4 text-muted-foreground">2024-02-18 14:32:38</td>
                        <td className="whitespace-nowrap py-2 px-4">
                          <Badge variant="outline" className="bg-blue-50 text-blue-700 hover:bg-blue-50">INFO</Badge>
                        </td>
                        <td className="py-2 px-4">User authentication successful: user_id=1542, ip=192.168.1.105</td>
                      </tr>
                      <tr className="border-b hover:bg-muted/50">
                        <td className="whitespace-nowrap py-2 px-4 text-muted-foreground">2024-02-18 14:32:35</td>
                        <td className="whitespace-nowrap py-2 px-4">
                          <Badge variant="outline" className="bg-blue-50 text-blue-700 hover:bg-blue-50">INFO</Badge>
                        </td>
                        <td className="py-2 px-4">Request completed: GET /api/products/featured status=200 time=84ms</td>
                      </tr>
                      <tr className="border-b hover:bg-muted/50">
                        <td className="whitespace-nowrap py-2 px-4 text-muted-foreground">2024-02-18 14:32:30</td>
                        <td className="whitespace-nowrap py-2 px-4">
                          <Badge variant="outline" className="bg-green-50 text-green-700 hover:bg-green-50">DEBUG</Badge>
                        </td>
                        <td className="py-2 px-4">Cache hit for key: featured-products-list</td>
                      </tr>
                      <tr className="border-b hover:bg-muted/50">
                        <td className="whitespace-nowrap py-2 px-4 text-muted-foreground">2024-02-18 14:32:28</td>
                        <td className="whitespace-nowrap py-2 px-4">
                          <Badge variant="outline" className="bg-blue-50 text-blue-700 hover:bg-blue-50">INFO</Badge>
                        </td>
                        <td className="py-2 px-4">New connection established: session_id=a8f5e120-5dfc-4ebd-b76a-9cb68ef2590a</td>
                      </tr>
                      <tr className="border-b hover:bg-muted/50">
                        <td className="whitespace-nowrap py-2 px-4 text-muted-foreground">2024-02-18 14:32:25</td>
                        <td className="whitespace-nowrap py-2 px-4">
                          <Badge variant="outline" className="bg-yellow-50 text-yellow-700 hover:bg-yellow-50">WARN</Badge>
                        </td>
                        <td className="py-2 px-4 text-yellow-600">
                          Memory usage approaching threshold: 412MB/512MB (80.4%)
                        </td>
                      </tr>
                      <tr className="border-b hover:bg-muted/50">
                        <td className="whitespace-nowrap py-2 px-4 text-muted-foreground">2024-02-18 14:32:20</td>
                        <td className="whitespace-nowrap py-2 px-4">
                          <Badge variant="outline" className="bg-blue-50 text-blue-700 hover:bg-blue-50">INFO</Badge>
                        </td>
                        <td className="py-2 px-4">
                          Application startup complete. Environment: Production, Version: v1.5.2
                        </td>
                      </tr>
                      <tr className="border-b hover:bg-muted/50">
                        <td className="whitespace-nowrap py-2 px-4 text-muted-foreground">2024-02-18 14:32:18</td>
                        <td className="whitespace-nowrap py-2 px-4">
                          <Badge variant="outline" className="bg-blue-50 text-blue-700 hover:bg-blue-50">INFO</Badge>
                        </td>
                        <td className="py-2 px-4">Loading configuration from /etc/app/config.json</td>
                      </tr>
                      <tr className="border-b hover:bg-muted/50">
                        <td className="whitespace-nowrap py-2 px-4 text-muted-foreground">2024-02-18 14:32:15</td>
                        <td className="whitespace-nowrap py-2 px-4">
                          <Badge variant="outline" className="bg-blue-50 text-blue-700 hover:bg-blue-50">INFO</Badge>
                        </td>
                        <td className="py-2 px-4">Application starting with process ID 4528</td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <div className="border-t flex items-center justify-between p-4">
                  <div className="text-sm text-muted-foreground">
                    Showing 10 of 1,248 logs
                  </div>
                  <div className="flex items-center gap-1">
                    <Button variant="outline" size="icon" disabled>
                      <ChevronLeft className="h-4 w-4" />
                    </Button>
                    <Button variant="outline" size="sm" className="h-8 min-w-8">1</Button>
                    <Button variant="ghost" size="sm" className="h-8 min-w-8">2</Button>
                    <Button variant="ghost" size="sm" className="h-8 min-w-8">3</Button>
                    <span className="mx-1">...</span>
                    <Button variant="ghost" size="sm" className="h-8 min-w-8">125</Button>
                    <Button variant="outline" size="icon">
                      <ChevronRight className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle>Log Summary</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-5">
                    <div>
                      <div className="mb-2 flex items-center justify-between">
                        <h3 className="text-sm font-medium">Log Levels (Last 24h)</h3>
                        <span className="text-xs text-muted-foreground">Total: 1,248</span>
                      </div>
                      <div className="space-y-2">
                        <div className="grid grid-cols-[100px_1fr_50px] gap-2 items-center">
                          <span className="text-xs">Error</span>
                          <div className="h-2 rounded-full bg-muted overflow-hidden">
                            <div className="h-full bg-red-500" style={{ width: '5%' }}></div>
                          </div>
                          <span className="text-xs text-right">64</span>
                        </div>
                        <div className="grid grid-cols-[100px_1fr_50px] gap-2 items-center">
                          <span className="text-xs">Warning</span>
                          <div className="h-2 rounded-full bg-muted overflow-hidden">
                            <div className="h-full bg-yellow-500" style={{ width: '12%' }}></div>
                          </div>
                          <span className="text-xs text-right">152</span>
                        </div>
                        <div className="grid grid-cols-[100px_1fr_50px] gap-2 items-center">
                          <span className="text-xs">Info</span>
                          <div className="h-2 rounded-full bg-muted overflow-hidden">
                            <div className="h-full bg-blue-500" style={{ width: '57%' }}></div>
                          </div>
                          <span className="text-xs text-right">712</span>
                        </div>
                        <div className="grid grid-cols-[100px_1fr_50px] gap-2 items-center">
                          <span className="text-xs">Debug</span>
                          <div className="h-2 rounded-full bg-muted overflow-hidden">
                            <div className="h-full bg-green-500" style={{ width: '26%' }}></div>
                          </div>
                          <span className="text-xs text-right">320</span>
                        </div>
                      </div>
                    </div>
                    <div className="pt-4 border-t">
                      <h3 className="text-sm font-medium mb-3">Most Frequent Errors</h3>
                      <div className="space-y-3">
                        <div className="group">
                          <div className="text-xs text-red-600 truncate">
                            Failed to connect to database: Connection timed out
                          </div>
                          <div className="flex justify-between text-xs text-muted-foreground">
                            <span>23 occurrences</span>
                            <span>First: 2h ago</span>
                          </div>
                        </div>
                        <div className="group">
                          <div className="text-xs text-red-600 truncate">
                            Uncaught TypeError: Cannot read property id of undefined
                          </div>
                          <div className="flex justify-between text-xs text-muted-foreground">
                            <span>18 occurrences</span>
                            <span>First: 5h ago</span>
                          </div>
                        </div>
                        <div className="group">
                          <div className="text-xs text-red-600 truncate">
                            API rate limit exceeded for endpoint: /api/users
                          </div>
                          <div className="flex justify-between text-xs text-muted-foreground">
                            <span>12 occurrences</span>
                            <span>First: 1h ago</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card className="md:col-span-2">
                <CardHeader>
                  <CardTitle>Log Trends</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="h-72">
                    <ResponsiveContainer width="100%" height="100%">
                      <LineChart
                        data={Array(24).fill(0).map((_, i) => ({
                          hour: `${i}:00`,
                          error: Math.floor(Math.random() * 10),
                          warning: Math.floor(Math.random() * 15 + 5),
                          info: Math.floor(Math.random() * 40 + 20),
                          debug: Math.floor(Math.random() * 20 + 10),
                        }))}
                      >
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="hour" />
                        <YAxis />
                        <Tooltip />
                        <Legend />
                        <Line type="monotone" dataKey="error" stroke="#ef4444" name="Errors" />
                        <Line type="monotone" dataKey="warning" stroke="#f59e0b" name="Warnings" />
                        <Line type="monotone" dataKey="info" stroke="#3b82f6" name="Info" />
                        <Line type="monotone" dataKey="debug" stroke="#10b981" name="Debug" />
                      </LineChart>
                    </ResponsiveContainer>
                  </div>
                </CardContent>
              </Card>
            </div>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
};

export default ApplicationDetails;
