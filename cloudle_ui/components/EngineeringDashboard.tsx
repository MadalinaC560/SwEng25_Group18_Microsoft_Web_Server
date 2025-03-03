
'use client';

import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
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
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Badge } from "@/components/ui/badge";
import {
  ArrowLeft,
  RefreshCw,
  AlertCircle,
  Server,
  Database,
  Globe,
  Cpu,
  HardDrive,
  Activity,
  Zap,
  DollarSign,
  Lock,
  Search,
  Network,
  Users,
  Layers,
  Clock,
  TrendingUp,
  Download,
  Upload,
  Shield
} from 'lucide-react';
import { LineChart, Line, BarChart, Bar, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, AreaChart, Area } from 'recharts';
import { useToast } from "@/hooks/use-toast";

// Mock data for platform metrics
const generatePlatformData = () => {
  return Array.from({ length: 24 }, (_, i) => ({
    time: `${i}:00`,
    totalUsers: Math.floor(Math.random() * 1000) + 500,
    activeApps: Math.floor(Math.random() * 100) + 50,
    serverLoad: Math.floor(Math.random() * 40) + 20,
    responseTime: Math.floor(Math.random() * 100) + 50,
    connections: Math.floor(Math.random() * 300) + 200,
    errorRate: (Math.random() * 1.5).toFixed(2)
  }));
};

// Mock data for critical alerts
const criticalAlerts = [
  {
    timestamp: '2024-02-18 15:23:45',
    component: 'Load Balancer',
    severity: 'High',
    message: 'High latency detected in EU-West region',
    status: 'Active'
  },
  {
    timestamp: '2024-02-18 15:20:30',
    component: 'Database Cluster',
    severity: 'Medium',
    message: 'Replication lag increased to 15s',
    status: 'Investigating'
  },
  {
    timestamp: '2024-02-18 14:45:12',
    component: 'Web Server Node3',
    severity: 'High',
    message: 'Memory usage over 92%, potential leak',
    status: 'Active'
  },
  {
    timestamp: '2024-02-18 14:10:05',
    component: 'SSL Certificate',
    severity: 'Medium',
    message: 'Certificate expires in 15 days for 12 domains',
    status: 'Scheduled'
  }
];

// Mock data for server nodes
const serverNodes = [
  {
    id: 'web-server-1',
    location: 'US East (Virginia)',
    status: 'healthy',
    cpu: 42,
    memory: 63,
    connections: 456,
    uptime: '14d 7h',
    load: 3.24
  },
  {
    id: 'web-server-2',
    location: 'US East (Virginia)',
    status: 'healthy',
    cpu: 38,
    memory: 57,
    connections: 389,
    uptime: '14d 7h',
    load: 2.87
  },
  {
    id: 'web-server-3',
    location: 'US West (Oregon)',
    status: 'warning',
    cpu: 78,
    memory: 92,
    connections: 712,
    uptime: '7d 22h',
    load: 6.51
  },
  {
    id: 'web-server-4',
    location: 'EU West (Ireland)',
    status: 'healthy',
    cpu: 45,
    memory: 60,
    connections: 523,
    uptime: '21d 3h',
    load: 3.45
  },
  {
    id: 'web-server-5',
    location: 'EU West (Ireland)',
    status: 'maintenance',
    cpu: 12,
    memory: 34,
    connections: 122,
    uptime: '2h 15m',
    load: 0.87
  },
  {
    id: 'web-server-6',
    location: 'Asia Pacific (Tokyo)',
    status: 'healthy',
    cpu: 51,
    memory: 64,
    connections: 341,
    uptime: '9d 11h',
    load: 4.12
  }
];

// Mock data for tenant resource usage
const tenantUsageData = [
  { name: 'Tenant A', servers: 12, cpu: 18, memory: 24, storage: 156, cost: 2345 },
  { name: 'Tenant B', servers: 8, cpu: 12, memory: 16, storage: 92, cost: 1568 },
  { name: 'Tenant C', servers: 15, cpu: 22, memory: 30, storage: 240, cost: 3102 },
  { name: 'Tenant D', servers: 5, cpu: 7, memory: 10, storage: 64, cost: 945 },
  { name: 'Tenant E', servers: 9, cpu: 14, memory: 18, storage: 108, cost: 1734 },
  { name: 'Tenant F', servers: 3, cpu: 4, memory: 6, storage: 32, cost: 587 }
];

// Mock data for CPU and Memory history
const generateResourceHistory = () => {
  const data = [];
  const now = new Date();
  
  for (let i = 30; i >= 0; i--) {
    const date = new Date(now);
    date.setDate(date.getDate() - i);
    const dateStr = date.toISOString().split('T')[0];
    
    data.push({
      date: dateStr,
      cpu: Math.floor(Math.random() * 30) + 40,
      memory: Math.floor(Math.random() * 25) + 50,
      network: Math.floor(Math.random() * 40) + 30
    });
  }
  
  return data;
};

// Mock data for bandwidth by region
const bandwidthByRegion = [
  { region: 'North America', inbound: 42.5, outbound: 68.3 },
  { region: 'Europe', inbound: 36.2, outbound: 54.7 },
  { region: 'Asia Pacific', inbound: 28.4, outbound: 47.2 },
  { region: 'South America', inbound: 12.6, outbound: 18.9 },
  { region: 'Africa', inbound: 5.8, outbound: 9.2 },
  { region: 'Middle East', inbound: 8.7, outbound: 14.1 }
];

// Mock data for request distribution
const requestDistribution = [
  { name: 'HTTP GET', value: 68 },
  { name: 'HTTP POST', value: 17 },
  { name: 'HTTP PUT', value: 8 },
  { name: 'HTTP DELETE', value: 4 },
  { name: 'HTTP OPTIONS', value: 3 }
];

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8'];

// Mock data for resource allocation
const resourceAllocation = {
  total: {
    cpu: 384, // 384 cores
    memory: 1536, // 1.5 TB
    storage: 8192, // 8 TB
    bandwidth: 20480 // 20 TB
  },
  used: {
    cpu: 218,
    memory: 984,
    storage: 5734,
    bandwidth: 14336
  }
};

export const EngineeringDashboard = () => {
  const [activeTab, setActiveTab] = useState('overview');
  const [timeRange, setTimeRange] = useState('24h');
  const [refreshing, setRefreshing] = useState(false);
  const [platformData, setPlatformData] = useState(generatePlatformData());
  const [resourceHistory] = useState(generateResourceHistory());
  const { toast } = useToast();

  const handleRefresh = () => {
    setRefreshing(true);
    setTimeout(() => {
      setPlatformData(generatePlatformData());
      setRefreshing(false);
      toast({
        title: "Dashboard Refreshed",
        description: "All metrics updated successfully",
      });
    }, 1000);
  };

  return (
    <div className="min-h-screen bg-gray-100 p-6">
      <div className="max-w-7xl mx-auto space-y-6">
        {/* Header */}
        <div className="bg-white rounded-lg shadow p-4 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <h1 className="text-2xl font-bold">Cloudle Engineering Dashboard</h1>
          </div>
          <div className="flex items-center gap-4">
            <Select 
              defaultValue={timeRange}
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
            <Button size="icon" onClick={handleRefresh} disabled={refreshing}>
              <RefreshCw className={`h-4 w-4 ${refreshing ? 'animate-spin' : ''}`} />
            </Button>
          </div>
        </div>

        <Tabs value={activeTab} onValueChange={setActiveTab}>
          <TabsList className="grid grid-cols-3 mb-4">
            <TabsTrigger value="overview">System Overview</TabsTrigger>
            <TabsTrigger value="servers">Server Infrastructure</TabsTrigger>
            <TabsTrigger value="tenants">Multi-Tenant Management</TabsTrigger>
          </TabsList>

          {/* OVERVIEW TAB */}
          <TabsContent value="overview" className="space-y-6">
            {/* Platform Status Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm font-medium">Server Health</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-2">
                    <div className="h-2 w-2 bg-green-500 rounded-full"></div>
                    <div className="text-2xl font-bold text-green-600">Healthy</div>
                  </div>
                  <p className="text-xs text-muted-foreground">All systems operational</p>
                </CardContent>
              </Card>
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
                  <CardTitle className="text-sm font-medium">Average Response Time</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-2">
                    <Clock className="h-5 w-5 text-indigo-500" />
                    <div className="text-2xl font-bold">76ms</div>
                  </div>
                  <p className="text-xs text-muted-foreground">Across all regions</p>
                </CardContent>
              </Card>
              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm font-medium">System Load</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-2">
                    <Activity className="h-5 w-5 text-amber-500" />
                    <div className="text-2xl font-bold">42%</div>
                  </div>
                  <p className="text-xs text-muted-foreground">Average across servers</p>
                </CardContent>
              </Card>
            </div>

            {/* Platform Metrics Graph */}
            <Card>
              <CardHeader>
                <CardTitle>Infrastructure Performance</CardTitle>
                <CardDescription>System-wide metrics for web server platform</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="h-80">
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={platformData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="time" />
                      <YAxis />
                      <Tooltip />
                      <Legend />
                      <Line 
                        type="monotone" 
                        dataKey="serverLoad" 
                        name="CPU Load (%)" 
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
                        dataKey="connections" 
                        name="Active Connections" 
                        stroke="#82ca9d" 
                      />
                      <Line 
                        type="monotone" 
                        dataKey="errorRate" 
                        name="Error Rate (%)" 
                        stroke="#ff0000" 
                      />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </CardContent>
            </Card>

            {/* Global Server Distribution */}
            {/* <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              <Card className="lg:col-span-2">
                <CardHeader>
                  <CardTitle>Server Distribution</CardTitle>
                  <CardDescription>Global distribution of web servers by region</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="h-80 bg-gray-50 rounded-md flex items-center justify-center">
                    <div className="text-center">
                      <Globe className="h-10 w-10 text-muted-foreground mx-auto mb-2" />
                      <p className="text-sm text-muted-foreground">Geographic distribution map</p>
                    </div>
                  </div>
                  <div className="grid grid-cols-2 md:grid-cols-3 gap-4 mt-4">
                    <div className="flex flex-col">
                      <span className="text-sm font-medium">US East</span>
                      <div className="flex items-center">
                        <div className="h-2 bg-blue-500 rounded-full w-24"></div>
                        <span className="ml-2 text-sm">8 servers</span>
                      </div>
                    </div>
                    <div className="flex flex-col">
                      <span className="text-sm font-medium">US West</span>
                      <div className="flex items-center">
                        <div className="h-2 bg-blue-500 rounded-full w-16"></div>
                        <span className="ml-2 text-sm">5 servers</span>
                      </div>
                    </div>
                    <div className="flex flex-col">
                      <span className="text-sm font-medium">EU West</span>
                      <div className="flex items-center">
                        <div className="h-2 bg-blue-500 rounded-full w-20"></div>
                        <span className="ml-2 text-sm">6 servers</span>
                      </div>
                    </div>
                    <div className="flex flex-col">
                      <span className="text-sm font-medium">EU Central</span>
                      <div className="flex items-center">
                        <div className="h-2 bg-blue-500 rounded-full w-12"></div>
                        <span className="ml-2 text-sm">4 servers</span>
                      </div>
                    </div>
                    <div className="flex flex-col">
                      <span className="text-sm font-medium">Asia Pacific</span>
                      <div className="flex items-center">
                        <div className="h-2 bg-blue-500 rounded-full w-10"></div>
                        <span className="ml-2 text-sm">3 servers</span>
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Request Methods</CardTitle>
                  <CardDescription>Distribution by HTTP method</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="h-80">
                    <ResponsiveContainer width="100%" height="100%">
                      <PieChart>
                        <Pie
                          data={requestDistribution}
                          cx="50%"
                          cy="50%"
                          labelLine={false}
                          outerRadius={80}
                          fill="#8884d8"
                          dataKey="value"
                          label={({name, percent}) => `${name}: ${(percent * 100).toFixed(0)}%`}
                        >
                          {requestDistribution.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                          ))}
                        </Pie>
                        <Tooltip formatter={(value) => `${value}%`} />
                        <Legend />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>
                </CardContent>
              </Card>
            </div> */}

            {/* Critical Alerts */}
            <Card>
              <CardHeader>
                <CardTitle>Critical Alerts</CardTitle>
                <CardDescription>Active issues requiring attention</CardDescription>
                <div className="flex gap-4 mt-4">
                  <Input placeholder="Search alerts..." className="max-w-sm" />
                  <Select defaultValue="all">
                    <SelectTrigger className="w-40">
                      <SelectValue placeholder="Severity" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Severities</SelectItem>
                      <SelectItem value="high">High</SelectItem>
                      <SelectItem value="medium">Medium</SelectItem>
                      <SelectItem value="low">Low</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </CardHeader>
              <CardContent>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Timestamp</TableHead>
                      <TableHead>Component</TableHead>
                      <TableHead>Severity</TableHead>
                      <TableHead>Message</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead>Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {criticalAlerts.map((alert, index) => (
                      <TableRow key={index}>
                        <TableCell>{alert.timestamp}</TableCell>
                        <TableCell>{alert.component}</TableCell>
                        <TableCell>
                          <div className="flex items-center gap-2">
                            <AlertCircle className={`h-4 w-4 ${
                              alert.severity === 'High' ? 'text-red-500' : 
                              alert.severity === 'Medium' ? 'text-amber-500' : 'text-blue-500'
                            }`} />
                            {alert.severity}
                          </div>
                        </TableCell>
                        <TableCell>{alert.message}</TableCell>
                        <TableCell>
                          <Badge variant={
                            alert.status === 'Active' ? 'destructive' :
                            alert.status === 'Investigating' ? 'default' : 'outline'
                          }>
                            {alert.status}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <Button variant="ghost" size="sm">Investigate</Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          </TabsContent>

          {/* SERVERS TAB */}
          <TabsContent value="servers" className="space-y-6">
            {/* Resource Usage Summary */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm font-medium">CPU Utilization</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-2">
                    <Cpu className="h-5 w-5 text-blue-500" />
                    <div className="text-2xl font-bold">
                      {Math.round((resourceAllocation.used.cpu / resourceAllocation.total.cpu) * 100)}%
                    </div>
                  </div>
                  <p className="text-xs text-muted-foreground">
                    {resourceAllocation.used.cpu} of {resourceAllocation.total.cpu} cores
                  </p>
                  <div className="w-full h-2 bg-gray-200 rounded-full mt-2">
                    <div 
                      className="h-2 bg-blue-500 rounded-full" 
                      style={{ width: `${(resourceAllocation.used.cpu / resourceAllocation.total.cpu) * 100}%` }}
                    ></div>
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm font-medium">Memory Usage</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-2">
                    <HardDrive className="h-5 w-5 text-purple-500" />
                    <div className="text-2xl font-bold">
                      {Math.round((resourceAllocation.used.memory / resourceAllocation.total.memory) * 100)}%
                    </div>
                  </div>
                  <p className="text-xs text-muted-foreground">
                    {resourceAllocation.used.memory} of {resourceAllocation.total.memory} GB
                  </p>
                  <div className="w-full h-2 bg-gray-200 rounded-full mt-2">
                    <div 
                      className="h-2 bg-purple-500 rounded-full" 
                      style={{ width: `${(resourceAllocation.used.memory / resourceAllocation.total.memory) * 100}%` }}
                    ></div>
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm font-medium">Storage Utilization</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-2">
                    <Database className="h-5 w-5 text-amber-500" />
                    <div className="text-2xl font-bold">
                      {Math.round((resourceAllocation.used.storage / resourceAllocation.total.storage) * 100)}%
                    </div>
                  </div>
                  <p className="text-xs text-muted-foreground">
                    {resourceAllocation.used.storage} of {resourceAllocation.total.storage} GB
                  </p>
                  <div className="w-full h-2 bg-gray-200 rounded-full mt-2">
                    <div 
                      className="h-2 bg-amber-500 rounded-full" 
                      style={{ width: `${(resourceAllocation.used.storage / resourceAllocation.total.storage) * 100}%` }}
                    ></div>
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm font-medium">Bandwidth Usage</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-2">
                    <Network className="h-5 w-5 text-green-500" />
                    <div className="text-2xl font-bold">
                      {Math.round((resourceAllocation.used.bandwidth / resourceAllocation.total.bandwidth) * 100)}%
                    </div>
                  </div>
                  <p className="text-xs text-muted-foreground">
                    {resourceAllocation.used.bandwidth} of {resourceAllocation.total.bandwidth} GB
                  </p>
                  <div className="w-full h-2 bg-gray-200 rounded-full mt-2">
                    <div 
                      className="h-2 bg-green-500 rounded-full" 
                      style={{ width: `${(resourceAllocation.used.bandwidth / resourceAllocation.total.bandwidth) * 100}%` }}
                    ></div>
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Resource History Chart */}
            <Card>
              <CardHeader>
                <CardTitle>Resource Utilization History</CardTitle>
                <CardDescription>30-day history of server resource usage</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="h-80">
                  <ResponsiveContainer width="100%" height="100%">
                    <AreaChart data={resourceHistory}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="date" />
                      <YAxis unit="%" />
                      <Tooltip />
                      <Legend />
                      <Area 
                        type="monotone" 
                        dataKey="cpu" 
                        name="CPU Usage" 
                        stackId="1"
                        stroke="#3b82f6" 
                        fill="#3b82f6" 
                        fillOpacity={0.3}
                      />
                      <Area 
                        type="monotone" 
                        dataKey="memory" 
                        name="Memory Usage" 
                        stackId="2"
                        stroke="#8b5cf6" 
                        fill="#8b5cf6" 
                        fillOpacity={0.3}
                      />
                      <Area 
                        type="monotone" 
                        dataKey="network" 
                        name="Network Usage" 
                        stackId="3"
                        stroke="#10b981" 
                        fill="#10b981" 
                        fillOpacity={0.3}
                      />
                    </AreaChart>
                  </ResponsiveContainer>
                </div>
              </CardContent>
            </Card>

            {/* Server Nodes Table */}
            {/* <Card>
              <CardHeader>
                <CardTitle>Web Server Nodes</CardTitle>
                <CardDescription>Status and metrics of all server instances</CardDescription>
                <div className="flex gap-4 mt-4">
                  <Input placeholder="Search servers..." className="max-w-sm" />
                  <Select defaultValue="all-regions">
                    <SelectTrigger className="w-40">
                      <SelectValue placeholder="Region" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all-regions">All Regions</SelectItem>
                      <SelectItem value="us-east">US East</SelectItem>
                      <SelectItem value="us-west">US West</SelectItem>
                      <SelectItem value="eu-west">EU West</SelectItem>
                      <SelectItem value="ap-north">Asia Pacific</SelectItem>
                    </SelectContent>
                  </Select>
                  <Select defaultValue="all-status">
                    <SelectTrigger className="w-40">
                      <SelectValue placeholder="Status" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all-status">All Status</SelectItem>
                      <SelectItem value="healthy">Healthy</SelectItem>
                      <SelectItem value="warning">Warning</SelectItem>
                      <SelectItem value="critical">Critical</SelectItem>
                      <SelectItem value="maintenance">Maintenance</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </CardHeader>
              <CardContent>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Server ID</TableHead>
                      <TableHead>Location</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead>CPU</TableHead>
                      <TableHead>Memory</TableHead>
                      <TableHead>Connections</TableHead>
                      <TableHead>Load</TableHead>
                      <TableHead>Uptime</TableHead>
                      <TableHead>Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {serverNodes.map((node) => (
                      <TableRow key={node.id}>
                        <TableCell className="font-medium">{node.id}</TableCell>
                        <TableCell>{node.location}</TableCell>
                        <TableCell>
                          <Badge variant={
                            node.status === 'healthy' ? 'default' :
                            node.status === 'warning' ? 'warning' :
                            node.status === 'critical' ? 'destructive' :
                            'secondary'
                          }>
                            {node.status.charAt(0).toUpperCase() + node.status.slice(1)}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center gap-2">
                            <div className="w-full bg-gray-200 rounded-full h-2">
                              <div 
                                className={`h-2 rounded-full ${
                                  node.cpu > 80 ? 'bg-red-500' : 
                                  node.cpu > 60 ? 'bg-yellow-500' : 'bg-green-500'
                                }`}
                                style={{ width: `${node.cpu}%` }}
                              ></div>
                            </div>
                            <span className="text-xs">{node.cpu}%</span>
                          </div>
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center gap-2">
                            <div className="w-full bg-gray-200 rounded-full h-2">
                              <div 
                                className={`h-2 rounded-full ${
                                  node.memory > 80 ? 'bg-red-500' : 
                                  node.memory > 60 ? 'bg-yellow-500' : 'bg-green-500'
                                }`}
                                style={{ width: `${node.memory}%` }}
                              ></div>
                            </div>
                            <span className="text-xs">{node.memory}%</span>
                          </div>
                        </TableCell>
                        <TableCell>{node.connections}</TableCell>
                        <TableCell>{node.load}</TableCell>
                        <TableCell>{node.uptime}</TableCell>
                        <TableCell>
                          <div className="flex gap-2">
                            <Button variant="ghost" size="icon">
                              <Zap className="h-4 w-4" />
                            </Button>
                            <Button variant="ghost" size="icon">
                              <RefreshCw className="h-4 w-4" />
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card> */}

            {/* Bandwidth Usage by Region */}
            {/* <Card>
              <CardHeader>
                <CardTitle>Bandwidth Usage by Region</CardTitle>
                <CardDescription>Data transfer by geographic region</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="h-80">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={bandwidthByRegion}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="region" />
                      <YAxis label={{ value: 'Data Transfer (TB)', angle: -90, position: 'insideLeft' }} />
                      <Tooltip />
                      <Legend />
                      <Bar dataKey="inbound" name="Inbound Traffic" fill="#3b82f6" />
                      <Bar dataKey="outbound" name="Outbound Traffic" fill="#ef4444" />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </CardContent>
            </Card> */}

            {/* Security Metrics */}
            {/* <Card>
              <CardHeader>
                <CardTitle>Security Metrics</CardTitle>
                <CardDescription>Web server security statistics</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  <div className="space-y-4">
                    <h3 className="text-lg font-medium">SSL Certificate Status</h3>
                    <div className="grid grid-cols-2 gap-4">
                      <div className="bg-green-50 p-4 rounded-lg">
                        <div className="flex items-center gap-2 mb-2">
                          <Lock className="h-5 w-5 text-green-600" />
                          <span className="font-medium text-green-700">Valid</span>
                        </div>
                        <p className="text-3xl font-bold text-green-700">94%</p>
                        <p className="text-sm text-green-600">124 certificates</p>
                      </div>
                      <div className="bg-amber-50 p-4 rounded-lg">
                        <div className="flex items-center gap-2 mb-2">
                          <AlertCircle className="h-5 w-5 text-amber-600" />
                          <span className="font-medium text-amber-700">Expiring Soon</span>
                        </div>
                        <p className="text-3xl font-bold text-amber-700">6%</p>
                        <p className="text-sm text-amber-600">8 certificates</p>
                      </div>
                    </div>
                  </div>

                  <div className="space-y-4">
                    <h3 className="text-lg font-medium">Traffic Type</h3>
                    <div className="h-48">
                      <ResponsiveContainer width="100%" height="100%">
                        <PieChart>
                          <Pie
                            data={[
                              { name: 'HTTPS', value: 92 },
                              { name: 'HTTP', value: 8 }
                            ]}
                            cx="50%"
                            cy="50%"
                            innerRadius={40}
                            outerRadius={80}
                            fill="#8884d8"
                            paddingAngle={5}
                            dataKey="value"
                            label={({name, percent}) => `${name}: ${(percent * 100).toFixed(0)}%`}
                          >
                            <Cell fill="#16a34a" />
                            <Cell fill="#f97316" />
                          </Pie>
                          <Tooltip />
                        </PieChart>
                      </ResponsiveContainer>
                    </div>
                  </div>

                  <div className="space-y-4">
                    <h3 className="text-lg font-medium">Security Events (24h)</h3>
                    <div className="space-y-3">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <Shield className="h-4 w-4 text-red-500" />
                          <span className="text-sm">Blocked Attacks</span>
                        </div>
                        <Badge variant="outline">1,248</Badge>
                      </div>
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <Shield className="h-4 w-4 text-amber-500" />
                          <span className="text-sm">WAF Triggers</span>
                        </div>
                        <Badge variant="outline">683</Badge>
                      </div>
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <Shield className="h-4 w-4 text-blue-500" />
                          <span className="text-sm">Rate Limiting</span>
                        </div>
                        <Badge variant="outline">416</Badge>
                      </div>
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <Shield className="h-4 w-4 text-purple-500" />
                          <span className="text-sm">Bot Traffic</span>
                        </div>
                        <Badge variant="outline">3,842</Badge>
                      </div>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card> */}
          </TabsContent>

          {/* TENANTS TAB */}
          <TabsContent value="tenants" className="space-y-6">
            {/* Cost Summary */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm font-medium">Total Monthly Cost</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-2">
                    <DollarSign className="h-5 w-5 text-green-500" />
                    <div className="text-2xl font-bold">$12,487</div>
                  </div>
                  <p className="text-xs text-muted-foreground">
                    <span className="text-green-500">↓ 4.2%</span> from last month
                  </p>
                </CardContent>
              </Card>
              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm font-medium">Active Tenants</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-2">
                    <Users className="h-5 w-5 text-blue-500" />
                    <div className="text-2xl font-bold">48</div>
                  </div>
                  <p className="text-xs text-muted-foreground">
                    <span className="text-green-500">↑ 3 new</span> this month
                  </p>
                </CardContent>
              </Card>
              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm font-medium">Avg. Cost Per Tenant</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-2">
                    <BarChart className="h-5 w-5 text-purple-500" />
                    <div className="text-2xl font-bold">$260</div>
                  </div>
                  <p className="text-xs text-muted-foreground">
                    <span className="text-green-500">↓ 7.1%</span> from last month
                  </p>
                </CardContent>
              </Card>
              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm font-medium">Resource Efficiency</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-2">
                    <TrendingUp className="h-5 w-5 text-cyan-500" />
                    <div className="text-2xl font-bold">72%</div>
                  </div>
                  <p className="text-xs text-muted-foreground">
                    <span className="text-green-500">↑ 5.3%</span> from last month
                  </p>
                </CardContent>
              </Card>
            </div>

            {/* Tenant Resource Usage */}
            <Card>
              <CardHeader>
                <CardTitle>Tenant Resource Allocation</CardTitle>
                <CardDescription>Resource usage across top tenants</CardDescription>
                <div className="flex gap-4 mt-4">
                  <Input placeholder="Search tenants..." className="max-w-sm" />
                  <Select defaultValue="resource-desc">
                    <SelectTrigger className="w-40">
                      <SelectValue placeholder="Sort By" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="resource-desc">Resource Usage ↓</SelectItem>
                      <SelectItem value="resource-asc">Resource Usage ↑</SelectItem>
                      <SelectItem value="cost-desc">Cost ↓</SelectItem>
                      <SelectItem value="cost-asc">Cost ↑</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </CardHeader>
              <CardContent>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Tenant</TableHead>
                      <TableHead>Servers</TableHead>
                      <TableHead>CPU (cores)</TableHead>
                      <TableHead>Memory (GB)</TableHead>
                      <TableHead>Storage (GB)</TableHead>
                      <TableHead>Monthly Cost ($)</TableHead>
                      <TableHead>Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {tenantUsageData.map((tenant) => (
                      <TableRow key={tenant.name}>
                        <TableCell className="font-medium">{tenant.name}</TableCell>
                        <TableCell>{tenant.servers}</TableCell>
                        <TableCell>{tenant.cpu}</TableCell>
                        <TableCell>{tenant.memory}</TableCell>
                        <TableCell>{tenant.storage}</TableCell>
                        <TableCell>${tenant.cost.toLocaleString()}</TableCell>
                        <TableCell>
                          <Button variant="ghost" size="sm">View Details</Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>

            {/* Resource Distribution */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle>Resource Distribution</CardTitle>
                  <CardDescription>CPU and memory allocation across tenants</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="h-96">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart
                        data={tenantUsageData}
                        layout="vertical"
                        margin={{
                          top: 20,
                          right: 30,
                          left: 20,
                          bottom: 5,
                        }}
                      >
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis type="number" />
                        <YAxis dataKey="name" type="category" width={100} />
                        <Tooltip />
                        <Legend />
                        <Bar dataKey="cpu" name="CPU (cores)" stackId="a" fill="#3b82f6" />
                        <Bar dataKey="memory" name="Memory (GB)" stackId="a" fill="#8b5cf6" />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Cost Analysis</CardTitle>
                  <CardDescription>Monthly cost breakdown by tenant</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="h-96">
                    <ResponsiveContainer width="100%" height="100%">
                      <PieChart>
                        <Pie
                          data={tenantUsageData}
                          cx="50%"
                          cy="50%"
                          labelLine={false}
                          outerRadius={120}
                          fill="#8884d8"
                          dataKey="cost"
                          nameKey="name"
                          label={({name, percent}) => `${name}: ${(percent * 100).toFixed(1)}%`}
                        >
                          {tenantUsageData.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                          ))}
                        </Pie>
                        <Tooltip formatter={(value) => `$${value}`} />
                        <Legend />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Cost History */}
            <Card>
              <CardHeader>
                <CardTitle>Cost History</CardTitle>
                <CardDescription>Monthly infrastructure costs over time</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="h-80">
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart
                      data={[
                        { month: 'Jan', compute: 4200, storage: 1200, network: 850, misc: 450 },
                        { month: 'Feb', compute: 4500, storage: 1300, network: 900, misc: 480 },
                        { month: 'Mar', compute: 5100, storage: 1400, network: 950, misc: 520 },
                        { month: 'Apr', compute: 4800, storage: 1500, network: 1000, misc: 550 },
                        { month: 'May', compute: 5300, storage: 1600, network: 1050, misc: 570 },
                        { month: 'Jun', compute: 5600, storage: 1700, network: 1100, misc: 590 },
                        { month: 'Jul', compute: 5200, storage: 1800, network: 1150, misc: 610 },
                        { month: 'Aug', compute: 4900, storage: 1900, network: 1200, misc: 630 },
                        { month: 'Sep', compute: 5400, storage: 2000, network: 1250, misc: 650 },
                        { month: 'Oct', compute: 5800, storage: 2100, network: 1300, misc: 670 },
                        { month: 'Nov', compute: 6200, storage: 2200, network: 1350, misc: 690 },
                        { month: 'Dec', compute: 5500, storage: 2300, network: 1400, misc: 710 },
                      ]}
                    >
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="month" />
                      <YAxis />
                      <Tooltip formatter={(value) => `$${value}`} />
                      <Legend />
                      <Line type="monotone" dataKey="compute" name="Compute" stroke="#3b82f6" activeDot={{ r: 8 }} />
                      <Line type="monotone" dataKey="storage" name="Storage" stroke="#8b5cf6" />
                      <Line type="monotone" dataKey="network" name="Network" stroke="#10b981" />
                      <Line type="monotone" dataKey="misc" name="Miscellaneous" stroke="#f59e0b" />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </CardContent>
            </Card>

            {/* Cost Optimization Recommendations */}
            <Card>
              <CardHeader>
                <CardTitle>Cost Optimization Recommendations</CardTitle>
                <CardDescription>Suggestions to improve resource efficiency</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex items-start gap-4 p-4 bg-blue-50 rounded-lg">
                    <div className="flex-shrink-0 bg-blue-100 rounded-full p-2">
                      <Cpu className="h-5 w-5 text-blue-600" />
                    </div>
                    <div>
                      <h3 className="font-medium text-blue-900">Right-size over-provisioned servers</h3>
                      <p className="text-sm text-blue-700 mt-1">
                        8 servers are running at less than 20% CPU utilization. Consider downsizing these instances.
                      </p>
                      <div className="mt-2">
                        <Badge variant="outline" className="bg-blue-100 text-blue-700">Estimated savings: $840/month</Badge>
                      </div>
                    </div>
                  </div>

                  <div className="flex items-start gap-4 p-4 bg-green-50 rounded-lg">
                    <div className="flex-shrink-0 bg-green-100 rounded-full p-2">
                      <Database className="h-5 w-5 text-green-600" />
                    </div>
                    <div>
                      <h3 className="font-medium text-green-900">Optimize storage usage</h3>
                      <p className="text-sm text-green-700 mt-1">
                        3 tenants have unused storage volumes. Consider implementing an auto-cleanup policy for inactive storage.
                      </p>
                      <div className="mt-2">
                        <Badge variant="outline" className="bg-green-100 text-green-700">Estimated savings: $320/month</Badge>
                      </div>
                    </div>
                  </div>

                  <div className="flex items-start gap-4 p-4 bg-purple-50 rounded-lg">
                    <div className="flex-shrink-0 bg-purple-100 rounded-full p-2">
                      <Users className="h-5 w-5 text-purple-600" />
                    </div>
                    <div>
                      <h3 className="font-medium text-purple-900">Implement shared resource pools</h3>
                      <p className="text-sm text-purple-700 mt-1">
                        Tenants with similar workload patterns could benefit from shared resource allocation.
                        This would improve utilization during peak/off-peak hours.
                      </p>
                      <div className="mt-2">
                        <Badge variant="outline" className="bg-purple-100 text-purple-700">Estimated savings: $560/month</Badge>
                      </div>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
};

export default EngineeringDashboard;