
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
  }[];
}

interface TenantUsage {
  tenantName: string;
  apps: number;
  cpu: number;
  memory: number;
}

// Dummy API call functions; replace with your actual API calls.
async function fetchPlatformMetrics(): Promise<PlatformMetrics> {
  const res = await fetch("http://localhost:8080/api/metrics");
  if (!res.ok) throw new Error("Failed to fetch metrics");
  return await res.json();
}

async function fetchTenantUsage(): Promise<TenantUsage[]> {
  // Replace with a real API call when available.
  return [
    { tenantName: "Tenant A", apps: 12, cpu: 24, memory: 64 },
    { tenantName: "Tenant B", apps: 5, cpu: 12, memory: 32 },
    { tenantName: "Tenant C", apps: 7, cpu: 15, memory: 48 },
  ];
}

export function EngineeringDashboard() {
  const [activeTab, setActiveTab] = useState("platform");
  const [refreshing, setRefreshing] = useState(false);
  const [platformMetrics, setPlatformMetrics] = useState<PlatformMetrics | null>(null);
  const [tenantUsage, setTenantUsage] = useState<TenantUsage[]>([]);

  const handleRefresh = async () => {
    setRefreshing(true);
    try {
      const metrics = await fetchPlatformMetrics();
      setPlatformMetrics(metrics);
      const tenants = await fetchTenantUsage();
      setTenantUsage(tenants);
    } catch (error) {
      console.error("Error refreshing dashboard data:", error);
    } finally {
      setRefreshing(false);
    }
  };

  // Fetch data on mount
  useEffect(() => {
    handleRefresh();
  }, []);

  // While loading metrics, show a loading indicator.
  if (!platformMetrics) {
    return (
        <div className="min-h-screen bg-gray-100 p-6 flex items-center justify-center">
          <p>Loading metrics...</p>
        </div>
    );
  }

  return (
      <div className="min-h-screen bg-gray-100 p-6">
        <div className="max-w-7xl mx-auto space-y-6">
          {/* Dashboard Header */}
          <div className="bg-white rounded-lg shadow p-4 flex items-center justify-between">
            <h1 className="text-2xl font-bold">Engineering Dashboard</h1>
            <Button size="icon" onClick={handleRefresh} disabled={refreshing}>
              <RefreshCw className={`h-4 w-4 ${refreshing ? "animate-spin" : ""}`} />
            </Button>
          </div>

          <Tabs value={activeTab} onValueChange={setActiveTab}>
            <TabsList className="grid grid-cols-2 mb-4">
              <TabsTrigger value="platform">Platform Metrics</TabsTrigger>
              <TabsTrigger value="tenants">Multi-Tenant</TabsTrigger>
            </TabsList>

            {/* PLATFORM METRICS TAB */}
            <TabsContent value="platform" className="space-y-6">
              {/* Quick Stats Cards */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <Card>
                  <CardHeader className="pb-2">
                    <CardTitle className="text-sm font-medium">Active Servers</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="flex items-center gap-2">
                      <Server className="h-5 w-5 text-blue-500" />
                      <div className="text-2xl font-bold">24/26</div>
                    </div>
                    <p className="text-xs text-muted-foreground">2 in maintenance</p>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader className="pb-2">
                    <CardTitle className="text-sm font-medium">System Load</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="flex items-center gap-2">
                      <Activity className="h-5 w-5 text-amber-500" />
                      <div className="text-2xl font-bold">{(platformMetrics.systemLoad * 100).toFixed(0)}%</div>
                    </div>
                    <p className="text-xs text-muted-foreground">Current load based on CPU</p>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader className="pb-2">
                    <CardTitle className="text-sm font-medium">Avg. Response Time</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="flex items-center gap-2">
                      <Clock className="h-5 w-5 text-indigo-500" />
                      <div className="text-2xl font-bold">{platformMetrics.avgResponseTime} ms</div>
                    </div>
                    <p className="text-xs text-muted-foreground">Across all requests</p>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader className="pb-2">
                    <CardTitle className="text-sm font-medium">Error Rate</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="flex items-center gap-2">
                      <AlertCircle className="h-5 w-5 text-red-500" />
                      <div className="text-2xl font-bold">{platformMetrics.errorRate}%</div>
                    </div>
                    <p className="text-xs text-muted-foreground">Errors per total requests</p>
                  </CardContent>
                </Card>
              </div>

              {/* Resource Usage Cards */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <Card>
                  <CardHeader className="pb-2">
                    <CardTitle className="text-sm font-medium">CPU Utilization</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="flex items-center gap-2">
                      <Cpu className="h-5 w-5 text-blue-500" />
                      <div className="text-2xl font-bold">{(platformMetrics.cpuUtilization * 100).toFixed(1)}%</div>
                    </div>
                    <p className="text-xs text-muted-foreground">Across all servers</p>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader className="pb-2">
                    <CardTitle className="text-sm font-medium">Memory Usage</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="flex items-center gap-2">
                      <HardDrive className="h-5 w-5 text-purple-500" />
                      <div className="text-2xl font-bold">{(platformMetrics.memoryUsage * 100).toFixed(1)}%</div>
                    </div>
                    <p className="text-xs text-muted-foreground">System memory used</p>
                  </CardContent>
                </Card>
              </div>

              {/* Performance Chart */}
              <Card>
                <CardHeader>
                  <CardTitle>Platform Performance</CardTitle>
                  <CardDescription>
                    Real-time performance metrics from the web server
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="h-80">
                    <ResponsiveContainer width="100%" height="100%">
                      <LineChart data={platformMetrics.performanceData}>
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
              <Card>
                <CardHeader>
                  <CardTitle>Tenant Resource Usage</CardTitle>
                  <CardDescription>Overview of tenant metrics</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="overflow-auto">
                    <table className="w-full text-sm">
                      <thead>
                      <tr className="border-b bg-muted/50">
                        <th className="px-4 py-2 text-left">Tenant</th>
                        <th className="px-4 py-2 text-left">Apps</th>
                        <th className="px-4 py-2 text-left">CPU (cores)</th>
                        <th className="px-4 py-2 text-left">Memory (GB)</th>
                      </tr>
                      </thead>
                      <tbody>
                      {tenantUsage.map((tenant) => (
                          <tr key={tenant.tenantName} className="border-b hover:bg-muted/50">
                            <td className="px-4 py-2">{tenant.tenantName}</td>
                            <td className="px-4 py-2">{tenant.apps}</td>
                            <td className="px-4 py-2">{tenant.cpu}</td>
                            <td className="px-4 py-2">{tenant.memory}</td>
                          </tr>
                      ))}
                      </tbody>
                    </table>
                  </div>
                </CardContent>
              </Card>

              {/* Tenant CPU Usage Chart */}
              <Card>
                <CardHeader>
                  <CardTitle>Tenant CPU Usage</CardTitle>
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
            </TabsContent>
          </Tabs>
        </div>
      </div>
  );
}

export default EngineeringDashboard;
