"use client";

import React, { useState, useEffect } from "react";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
} from "@/components/ui/card";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { Button } from "@/components/ui/button";
import {
  RefreshCw,
  Activity,
  Cpu,
  HardDrive,
  AlertCircle,
  Clock,
  Server,
} from "lucide-react";
import {
  ResponsiveContainer,
  LineChart,
  Line,
  CartesianGrid,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  BarChart,
  Bar,
} from "recharts";

// Import real API functions
import { fetchPlatformMetrics, getTenantUsage, TenantUsage } from "@/components/api";

// Define interfaces for type safety
interface PlatformMetrics {
  systemLoad: number;
  avgResponseTime: number;
  errorRate: number;
  cpuUtilization: number;
  memoryUsage: number;
  performanceData: {
    time: string;
    serverLoad: number;
    responseTime: number;
    errorRate: number;
    inbound: number;
    outbound: number;
  }[];
}

export function EngineeringDashboard() {
  const [activeTab, setActiveTab] = useState("platform");
  const [refreshing, setRefreshing] = useState(false);
  const [platformMetrics, setPlatformMetrics] = useState<PlatformMetrics | null>(null);
  const [tenantUsage, setTenantUsage] = useState<TenantUsage[]>([]);
  const [error, setError] = useState<string | null>(null);

  const handleRefresh = async () => {
    setRefreshing(true);
    setError(null);
    try {
      // Fetch real data from our endpoints
      const metrics = await fetchPlatformMetrics();
      setPlatformMetrics(metrics);

      const tenants = await getTenantUsage();
      setTenantUsage(tenants);
    } catch (error) {
      console.error("Error refreshing dashboard data:", error);
      setError("Failed to load dashboard data. Please try again.");
    } finally {
      setRefreshing(false);
    }
  };

  // Fetch data on mount
  useEffect(() => {
    handleRefresh();
  }, []);

  // While loading metrics, show a loading indicator.
  // if (!platformMetrics && !error) {
  //   return (
  //     <div className="min-h-screen bg-gray-100 p-6 flex items-center justify-center">
  //       <p>Loading metrics...</p>
  //     </div>
  //   );
  // }
  //
  // if (error) {
  //   return (
  //     <div className="min-h-screen bg-gray-100 p-6 flex items-center justify-center">
  //       <div className="bg-red-50 border border-red-200 rounded-lg p-4 max-w-md">
  //         <h2 className="text-red-800 font-medium">Error</h2>
  //         <p className="text-red-600">{error}</p>
  //         <Button onClick={handleRefresh} className="mt-4" disabled={refreshing}>
  //           {refreshing ? "Retrying..." : "Retry"}
  //         </Button>
  //       </div>
  //     </div>
  //   );
  // }

  // While loading metrics, show a loading indicator.
if (!platformMetrics && ! error) {
  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-100 to-blue-50 p-6 flex items-center justify-center">
      <div className="flex items-center space-x-4">
        <div className="animate-spin rounded-full border-4 border-t-4 border-blue-500 border-solid w-12 h-12"></div>
        <p className="text-lg text-blue-600 font-semibold">Loading metrics...</p>
      </div>
    </div>
  );
}


  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-100 to-blue-50 p-6">
        <div className="max-w-7xl mx-auto space-y-6">

        {/* Dashboard Header */}
       <div className="bg-white border border-blue-200 rounded-lg shadow p-4 flex items-center justify-between">
            <h1 className="text-2xl font-bold text-blue-800">Engineering Dashboard</h1>
          <Button size="icon" onClick={handleRefresh} disabled={refreshing}>
            <RefreshCw className={`h-4 w-4 ${refreshing ? "animate-spin" : ""}`} />
          </Button>
        </div>

        <Tabs value={activeTab} onValueChange={setActiveTab}>
          <TabsList className="grid grid-cols-2 bg-blue-100 text-blue-800 rounded-md mb-4">
            <TabsTrigger value="platform">Platform Metrics</TabsTrigger>
            <TabsTrigger value="tenants">Multi-Tenant</TabsTrigger>
          </TabsList>

          {/* PLATFORM METRICS TAB */}
          <TabsContent value="platform" className="space-y-6">
            {/* Quick Stats Cards */}
           <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <Card className="bg-white border border-blue-200 shadow-sm">
                  <CardHeader className="pb-2">
                    <CardTitle className="text-sm font-medium text-blue-800">Active Servers</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="flex items-center gap-2">
                      <Server className="h-5 w-5 text-blue-600" />
                      <div className="text-2xl font-bold text-blue-800">24/26</div>
                    </div>
                    <p className="text-xs text-blue-700/70">2 in maintenance</p>
                </CardContent>
              </Card>

              <Card>
                <CardHeader >
                  <CardTitle className="text-sm font-medium text-blue-800">System Load</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-2">
                    <Activity className="h-5 w-5 text-amber-500" />
                    <div className="text-2xl font-bold text-blue-800">{Math.ceil((platformMetrics?.systemLoad ?? 0) * 100)}%</div>
                  </div>
                  <p className="text-xs text-blue-700/70">Current load based on CPU</p>
                </CardContent>
              </Card>

              <Card className="bg-white border border-blue-200 shadow-sm">
                <CardHeader className="pb-2">
                 <CardTitle className="text-sm font-medium text-blue-800">Avg. Response Time</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-2">
                    <Clock className="h-5 w-5 text-indigo-500" />
                    <div className="text-2xl font-bold text-blue-800">{Math.ceil(platformMetrics?.avgResponseTime || 0)} ms</div>
                  </div>
                  <p className="text-xs text-blue-700/70">Across all requests</p>
                </CardContent>
              </Card>

              <Card className="bg-white border border-blue-200 shadow-sm">
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm font-medium text-blue-800">Error Rate</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-2">
                    <AlertCircle className="h-5 w-5 text-red-500" />
                    <div className="text-2xl font-bold text-blue-800">{Math.ceil(platformMetrics?.errorRate || 0)}%</div>
                  </div>
                   <p className="text-xs text-blue-700/70">Errors per total requests</p>
                </CardContent>
              </Card>
            </div>

            {/* Resource Usage Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Card className="bg-white border border-blue-200 shadow-sm">
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm font-medium text-blue-800">CPU Utilization</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-2">
                    <Cpu className="h-5 w-5 text-blue-600" />
                    <div className="text-2xl font-bold text-blue-800">{Math.ceil((platformMetrics?.memoryUsage || 0) * 100)}%%</div>
                  </div>
                  <p className="text-xs text-blue-700/70">Across all servers</p>
                </CardContent>
              </Card>

             <Card className="bg-white border border-blue-200 shadow-sm">
                <CardHeader className="pb-2">
                   <CardTitle className="text-sm font-medium text-blue-800">Memory Usage</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-2">
                    <HardDrive className="h-5 w-5 text-purple-500" />
                    <div className="text-2xl font-bold text-blue-800">{((platformMetrics?.memoryUsage || 0) * 100).toFixed(1)}%</div>
                  </div>
                  <p className="text-xs text-blue-700/70">System memory used</p>
                </CardContent>
              </Card>
            </div>

            {/* Performance Chart */}
           <Card className="bg-white border border-blue-200 shadow-sm">
                <CardHeader>
                  <CardTitle className="text-blue-800">Platform Performance</CardTitle>
                  <CardDescription className="text-blue-700">
                    Real-time performance metrics from the web server
                  </CardDescription>
                </CardHeader>
              <CardContent>
                <div className="h-80">
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={platformMetrics?.performanceData || []}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="time" />
                      <YAxis />
                      <Tooltip />
                      <Legend />
                      <Line
                        type="monotone"
                        dataKey="serverLoad"
                        name="Server Load (%)"
                        stroke="#f59e0b"
                        activeDot={{ r: 8 }}
                      />
                      <Line
                        type="monotone"
                        dataKey="responseTime"
                        name="Response Time (ms)"
                        stroke="#8884d8"
                      />
                      <Line
                        type="monotone"
                        dataKey="errorRate"
                        name="Error Rate (%)"
                        stroke="#ef4444"
                      />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          {/* MULTI-TENANT TAB */}
          <TabsContent value="tenants" className="space-y-6">
            <Card className="bg-white border border-blue-200 shadow-sm">
                <CardHeader>
                  <CardTitle className="text-blue-800">Tenant Resource Usage</CardTitle>
                  <CardDescription className="text-blue-700">Overview of tenant metrics</CardDescription>
              </CardHeader>
              <CardContent>
                {tenantUsage.length === 0 ? (
                  <div className="text-center py-6 text-gray-500">
                    No tenant usage data available
                  </div>
                ) : (
                  <div className="overflow-auto">
                    <table className="w-full text-sm">
                      <thead>
                        <tr className="border-b bg-blue-100">
                          <th className="p-2 text-left">Tenant ID</th>
                          <th className="p-2 text-left">Tenant Name</th>
                          <th className="p-2 text-left">Apps</th>
                          <th className="p-2 text-left">CPU (cores)</th>
                          <th className="p-2 text-left">Memory (GB)</th>
                        </tr>
                      </thead>
                      <tbody>
                        {tenantUsage.map((tenant) => (
                          <tr key={tenant.tenantId} className="border-b hover:bg-muted/50">
                            <td className="p-2">{tenant.tenantId}</td>
                            <td className="p-2">{tenant.tenantName}</td>
                            <td className="p-2">{tenant.apps}</td>
                            <td className="p-2">{tenant.cpu}</td>
                            <td className="p-2">{tenant.memory}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Tenant CPU Usage Chart */}
            {tenantUsage.length > 0 && (
              <Card className="bg-white border border-blue-200 shadow-sm">
                <CardHeader>
                  <CardTitle className="text-blue-800">Tenant CPU Usage</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="h-64">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart
                        data={tenantUsage}
                        margin={{ top: 20, right: 30, left: 20, bottom: 5 }}
                      >
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="tenantName" />
                        <YAxis />
                        <Tooltip />
                        <Legend />
                        <Bar dataKey="cpu" name="CPU (cores)" fill="#3b82f6" />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </CardContent>
              </Card>
            )}

            {/* Tenant Memory Usage Chart */}
            {tenantUsage.length > 0 && (
              <Card>
                <CardHeader>
                  <CardTitle>Tenant Memory Usage</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="h-64">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart
                        data={tenantUsage}
                        margin={{ top: 20, right: 30, left: 20, bottom: 5 }}
                      >
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="tenantName" />
                        <YAxis />
                        <Tooltip />
                        <Legend />
                        <Bar dataKey="memory" name="Memory (GB)" fill="#8b5cf6" />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </CardContent>
              </Card>
            )}
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}

export default EngineeringDashboard;