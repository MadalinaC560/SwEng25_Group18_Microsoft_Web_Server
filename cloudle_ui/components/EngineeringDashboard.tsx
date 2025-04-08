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

                <Card className="bg-white border border-blue-200 shadow-sm">
                  <CardHeader className="pb-2">
                    <CardTitle className="text-sm font-medium text-blue-800">System Load</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="flex items-center gap-2">
                      <Activity className="h-5 w-5 text-amber-500" />
                      <div className="text-2xl font-bold text-blue-800">{(platformMetrics.systemLoad * 100).toFixed(0)}%</div>
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
                      <div className="text-2xl font-bold text-blue-800">{platformMetrics.avgResponseTime} ms</div>
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
                      <div className="text-2xl font-bold text-blue-800">{platformMetrics.errorRate}%</div>
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
                      <div className="text-2xl font-bold text-blue-800">{(platformMetrics.cpuUtilization * 100).toFixed(1)}%</div>
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
                      <div className="text-2xl font-bold text-blue-800">{(platformMetrics.memoryUsage * 100).toFixed(1)}%</div>
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
              <Card className="bg-white border border-blue-200 shadow-sm">
                <CardHeader>
                  <CardTitle className="text-blue-800">Tenant Resource Usage</CardTitle>
                  <CardDescription className="text-blue-700">Overview of tenant metrics</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="overflow-auto">
                    <table className="w-full text-sm">
                      <thead>
                      <tr className="border-b bg-blue-100">
                        <th className="p-2 text-left">Tenant</th>
                        <th className="p-2 text-left">Apps</th>
                        <th className="p-2 text-left">CPU (%)</th>
                        <th className="p-2 text-left">Memory (%)</th>
                      </tr>
                      </thead>
                      <tbody>
                      {tenantUsage.map((tenant, index) => (
                          <tr key={index} className="border-b hover:bg-blue-50">
                            <td className="p-2">{tenant.tenantName}</td>
                            <td className="p-2">{tenant.apps}</td>
                            <td className="p-2">{tenant.cpu}%</td>
                            <td className="p-2">{tenant.memory}%</td>
                          </tr>
                      ))}
                      </tbody>
                    </table>
                  </div>
                </CardContent>
              </Card>
              {/* Tenant CPU Usage Chart */}
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
                        <CartesianGrid strokeDasharray="3 3" stroke="#d1d5db" />
                        <XAxis dataKey="tenantName" tick={{ fill: '#3b82f6' }} />
                        <YAxis tick={{ fill: '#3b82f6' }} />
                        <Tooltip contentStyle={{ backgroundColor: '#f3f4f6', borderColor: '#e5e7eb' }} />
                        <Legend wrapperStyle={{ color: '#3b82f6' }} />
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
