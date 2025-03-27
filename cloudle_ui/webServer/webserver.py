import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { 
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { ArrowLeft, RefreshCw } from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, BarChart, Bar } from 'recharts';

export const MetricsDashboard = () => {
  // State for all metrics
  const [metrics, setMetrics] = useState({
    uptime: 0,
    activeInstances: { current: 0, total: 0 },
    responseTime: { avg: 0 },
    storage: { used: 0, total: 100 },
    trafficData: [],
    errorRates: [],
    serverHealth: [],
    recentLogs: []
  });
  
  const [timeRange, setTimeRange] = useState('24h');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // Function to fetch metrics
  const fetchMetrics = async () => {
    try {
      const response = await fetch('http://localhost:5000/api/metrics');
      if (!response.ok) throw new Error('Failed to fetch metrics');
      const data = await response.json();
      
      setMetrics({
        uptime: data.uptime_seconds,
        activeInstances: {
          current: data.active_instances,
          total: 5 // This could come from backend
        },
        responseTime: {
          avg: data.avg_response_time
        },
        storage: {
          used: Math.floor(data.storage_used),
          total: 100
        },
        trafficData: Object.entries(data.requests_per_second).map(([time, requests]) => ({
          time,
          requests,
          errors4xx: data.error_counts['4xx'] || 0,
          errors5xx: data.error_counts['5xx'] || 0
        })),
        serverHealth: data.server_health || [],
        recentLogs: data.recent_logs || []
      });
      setIsLoading(false);
    } catch (err) {
      setError(err.message);
      setIsLoading(false);
    }
  };

  // Fetch data initially and set up polling
  useEffect(() => {
    fetchMetrics();
    const interval = setInterval(fetchMetrics, 5000); // Poll every 5 seconds
    return () => clearInterval(interval);
  }, [timeRange]);

  // Handle refresh button click
  const handleRefresh = () => {
    setIsLoading(true);
    fetchMetrics();
  };

  if (error) {
    return <div className="p-4">Error loading metrics: {error}</div>;
  }

  return (
    <div className="min-h-screen bg-gray-100 p-6">
      <div className="max-w-7xl mx-auto space-y-6">
        {/* Header */}
        <div className="bg-white rounded-lg p-4 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="icon">
              <ArrowLeft className="h-4 w-4" />
            </Button>
            <h1 className="text-2xl font-bold">Web Server Metrics Dashboard</h1>
          </div>
          <div className="flex items-center gap-4">
            <Select 
              value={timeRange} 
              onValueChange={setTimeRange}
            >
              <SelectTrigger className="w-40">
                <SelectValue placeholder="Time Range" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="24h">Last 24h</SelectItem>
                <SelectItem value="7d">Last 7 days</SelectItem>
                <SelectItem value="30d">Last 30 days</SelectItem>
              </SelectContent>
            </Select>
            <Button 
              size="icon" 
              onClick={handleRefresh}
              disabled={isLoading}
            >
              <RefreshCw className={`h-4 w-4 ${isLoading ? 'animate-spin' : ''}`} />
            </Button>
          </div>
        </div>

        {/* Summary Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium">Uptime</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {Math.floor(metrics.uptime / 3600)}h {Math.floor((metrics.uptime % 3600) / 60)}m
              </div>
              <p className="text-xs text-muted-foreground">Server Uptime</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium">Active Instances</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {metrics.activeInstances.current}/{metrics.activeInstances.total}
              </div>
              <p className="text-xs text-muted-foreground">Running Servers</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium">Avg. Response Time</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {metrics.responseTime.avg.toFixed(2)}ms
              </div>
              <p className="text-xs text-muted-foreground">Last hour</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium">Storage Used</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {metrics.storage.used}GB/{metrics.storage.total}GB
              </div>
              <p className="text-xs text-muted-foreground">Total capacity</p>
            </CardContent>
          </Card>
        </div>

        {/* Live Traffic Graph */}
        <Card>
          <CardHeader>
            <CardTitle>Live Traffic</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="h-80">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={metrics.trafficData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="time" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Line type="monotone" dataKey="requests" stroke="#8884d8" name="Requests/s" />
                  <Line type="monotone" dataKey="errors4xx" stroke="#ffc658" name="4xx Errors" />
                  <Line type="monotone" dataKey="errors5xx" stroke="#ff8042" name="5xx Errors" />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </CardContent>
        </Card>

        {/* Server Health */}
        <Card>
          <CardHeader>
            <CardTitle>Server Health</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="h-80">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={metrics.serverHealth}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="time" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Line type="monotone" dataKey="cpu" stroke="#8884d8" name="CPU Usage %" />
                  <Line type="monotone" dataKey="memory" stroke="#82ca9d" name="Memory Usage %" />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </CardContent>
        </Card>

        {/* Recent Errors & Logs */}
        <Card>
          <CardHeader>
            <CardTitle>Recent Errors & Logs</CardTitle>
            <div className="flex gap-4 mt-4">
              <Input placeholder="Search logs..." className="max-w-sm" />
              <Select defaultValue="all">
                <SelectTrigger className="w-40">
                  <SelectValue placeholder="Status Code" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Status Codes</SelectItem>
                  <SelectItem value="4xx">4xx Errors</SelectItem>
                  <SelectItem value="5xx">5xx Errors</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Timestamp</TableHead>
                  <TableHead>Endpoint</TableHead>
                  <TableHead>Status Code</TableHead>
                  <TableHead>Error Message</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {metrics.recentLogs.map((log, index) => (
                  <TableRow key={index}>
                    <TableCell>{log.timestamp}</TableCell>
                    <TableCell>{log.endpoint}</TableCell>
                    <TableCell>{log.statusCode}</TableCell>
                    <TableCell>{log.errorMessage}</TableCell>
                    <TableCell>
                      <Button variant="ghost">View Logs</Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default MetricsDashboard;