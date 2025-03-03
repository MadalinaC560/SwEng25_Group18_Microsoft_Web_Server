
import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from "@/components/ui/card";
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
    Terminal,
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
    AlertTriangle,
    Info

  } from 'lucide-react';
  import { LineChart, Line, BarChart, Bar, PieChart, Pie, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, Label as RechartsLabel } from 'recharts';

import { api } from '@/components/api';
import { useRealTimeMetrics } from '@/hooks/useRealTimeMetrics';
import type { ApplicationData } from '@/components/types';
import { useToast } from "@/hooks/use-toast";

interface ApplicationDetailsProps {
  appId: number;
  onBack: () => void;
}

export const ApplicationDetails: React.FC<ApplicationDetailsProps> = ({ appId, onBack }) => {
  const [activeTab, setActiveTab] = useState('overview');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [appData, setAppData] = useState<ApplicationData>({
    name: '',
    status: 'stopped',
    url: '',
    runtime: '',
    environment: '',
    sslStatus: '',
    autoScaling: '',
    version: '',
    lastDeployment: '',
  });

  const { toast } = useToast();
  const { metrics, simulateLoad } = useRealTimeMetrics(appId, appData.status === 'running');

  // Initial data fetch
  useEffect(() => {
    const fetchInitialData = async () => {
      setIsLoading(true);
      setError(null);
      try {
        const appDataResponse = await api.fetchApplicationData(appId);
        setAppData(appDataResponse);
      } catch (error) {
        setError('Failed to load application data. Please try again later.');
        console.error('Error loading initial data:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchInitialData();
  }, [appId]);

  const handleToggleStatus = async () => {
    try {
      const newStatus = appData.status === 'running' ? 'stopped' : 'running';
      await api.toggleApplicationStatus(appId, newStatus);
      setAppData(prev => ({ ...prev, status: newStatus }));
      
      toast({
        title: "Status Updated",
        description: `Application ${newStatus === 'running' ? 'started' : 'stopped'} successfully.`,
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to update application status. Please try again.",
        variant: "destructive",
      });
    }
  };

  const handleDeploy = async () => {
    try {
      await api.deployNewVersion(appId, 'new-version');
      const newAppData = await api.fetchApplicationData(appId);
      setAppData(newAppData);
      
      toast({
        title: "Deployment Successful",
        description: "New version deployed successfully.",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to deploy new version. Please try again.",
        variant: "destructive",
      });
    }
  };

  const handleTestLoad = () => {
    if (appData.status !== 'running') return;
    
    simulateLoad(500);
    toast({
      title: "Load Test Started",
      description: "Simulating 500 requests...",
    });
  };

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

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-7xl mx-auto space-y-6">
        {/* Header */}
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={onBack}>
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div className="flex-1">
            <h1 className="text-3xl font-bold">{isLoading ? 'Loading...' : appData.name}</h1>
            <div className="flex items-center gap-2 mt-1">
              <Badge variant={appData.status === 'running' ? 'default' : 'secondary'}>
                {appData.status}
              </Badge>
              {appData.url && (
                <a 
                  href={appData.url} 
                  target="_blank" 
                  rel="noopener noreferrer"
                  className="text-sm text-gray-500 hover:text-gray-700 flex items-center gap-1"
                >
                  {appData.url}
                  <ExternalLink className="h-3 w-3" />
                </a>
              )}
            </div>
          </div>
          <div className="flex items-center gap-2">
            {/* <Button variant="outline" disabled={isLoading}>
              <Terminal className="h-4 w-4 mr-2" />
              Console
            </Button> */}
            <Button 
              variant="outline" 
              disabled={isLoading || appData.status !== 'running'}
              onClick={handleTestLoad}
            >
              <Activity className="h-4 w-4 mr-2" />
              Test Load (500 requests)
            </Button>
            <Button 
              variant="destructive" 
              disabled={isLoading}
              onClick={handleToggleStatus}
            >
              <Power className="h-4 w-4 mr-2" />
              {appData.status === 'running' ? 'Stop' : 'Start'} Application
            </Button>
          </div>
        </div>

        {/* Main Content */}
        <Tabs value={activeTab} onValueChange={setActiveTab}>
          <TabsList>
            <TabsTrigger value="overview">Overview</TabsTrigger>
            <TabsTrigger value="metrics">Metrics</TabsTrigger>
            <TabsTrigger value="settings">Settings</TabsTrigger>
            <TabsTrigger value="logs">Logs</TabsTrigger>
          </TabsList>

          <TabsContent value="overview" className="space-y-6">
            {/* Quick Stats */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <Card>
                <CardContent className="pt-6">
                  <div className="flex items-center gap-2">
                    <Globe className="h-5 w-5 text-blue-500" />
                    <div>
                      <p className="text-sm font-medium">Requests (24h)</p>
                      <p className="text-2xl font-bold">
                        {isLoading ? '...' : metrics.requests24h.toLocaleString()}
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
                        {isLoading ? '...' : `${metrics.avgResponseTime.toFixed(1)}ms`}
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
                        {isLoading ? '...' : `${(metrics.errorRate * 100).toFixed(2)}%`}
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
                      <p className="text-sm font-medium">Storage Used</p>
                      <p className="text-2xl font-bold">
                        {isLoading ? '...' : `${metrics.storageUsed.toFixed(1)}GB`}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Performance Chart */}
            <Card>
              <CardHeader>
                <CardTitle>Performance Overview</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="h-80">
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={metrics.performanceData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="time" />
                      <YAxis yAxisId="left" />
                      <YAxis yAxisId="right" orientation="right" />
                      <Tooltip />
                      <Legend />
                      <Line 
                        yAxisId="left"
                        type="monotone" 
                        dataKey="responseTime" 
                        stroke="#8884d8" 
                        name="Response Time (ms)"
                        dot={false}
                        isAnimationActive={false}
                      />
                      <Line 
                        yAxisId="right"
                        type="monotone" 
                        dataKey="requests" 
                        stroke="#82ca9d" 
                        name="Requests"
                        dot={false}
                        isAnimationActive={false}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </CardContent>
            </Card>

            {/* Application Info */}
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
                      <p className="font-medium">{isLoading ? '...' : appData.environment}</p>
                    </div>
                    <div>
                      <Label className="text-sm text-gray-500">SSL Status</Label>
                      <p className="font-medium text-green-600">{isLoading ? '...' : appData.sslStatus}</p>
                    </div>
                    <div>
                      <Label className="text-sm text-gray-500">Auto Scaling</Label>
                      <p className="font-medium">{isLoading ? '...' : appData.autoScaling}</p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Deployment</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div>
                    <Label className="text-sm text-gray-500">Current Version</Label>
                    <div className="flex items-center gap-2">
                      <GitBranch className="h-4 w-4" />
                      <p className="font-medium">
                        {isLoading ? '...' : appData.version}
                      </p>
                    </div>
                  </div>
                  <div>
                    <Label className="text-sm text-gray-500">Last Deployment</Label>
                    <p className="font-medium">
                      {isLoading ? '...' : appData.lastDeployment}
                    </p>
                  </div>
                  <Button 
                    variant="outline" 
                    className="w-full" 
                    disabled={isLoading}
                    onClick={handleDeploy}
                  >
                    Deploy New Version
                  </Button>
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          <TabsContent value="metrics" className="space-y-6">
  {/* Performance Summary */}
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
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="flex flex-col space-y-2">
          <span className="text-muted-foreground text-xs">Avg. Response Time</span>
          <div className="flex items-end gap-2">
            <span className="text-2xl font-bold">{metrics.avgResponseTime.toFixed(1)}ms</span>
            <span className="text-xs text-green-500 flex items-center mb-1">
              -12.4% <ArrowDownRight className="h-3 w-3" />
            </span>
          </div>
          <div className="h-10">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={metrics.performanceData.slice(-12)}>
                <Line type="monotone" dataKey="responseTime" stroke="#10b981" dot={false} strokeWidth={2} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="flex flex-col space-y-2">
          <span className="text-muted-foreground text-xs">Request Throughput</span>
          <div className="flex items-end gap-2">
            <span className="text-2xl font-bold">{Math.round(metrics.requests24h / 24)} req/hr</span>
            <span className="text-xs text-green-500 flex items-center mb-1">
              +8.2% <ArrowUpRight className="h-3 w-3" />
            </span>
          </div>
          <div className="h-10">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={metrics.performanceData.slice(-12)}>
                <Line type="monotone" dataKey="requests" stroke="#8884d8" dot={false} strokeWidth={2} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="flex flex-col space-y-2">
          <span className="text-muted-foreground text-xs">Error Rate</span>
          <div className="flex items-end gap-2">
            <span className="text-2xl font-bold">{(metrics.errorRate * 100).toFixed(2)}%</span>
            <span className="text-xs text-red-500 flex items-center mb-1">
              +0.8% <ArrowUpRight className="h-3 w-3" />
            </span>
          </div>
          <div className="h-10">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={metrics.performanceData.slice(-12)}>
                <Line type="monotone" dataKey="errors" stroke="#ef4444" dot={false} strokeWidth={2} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="flex flex-col space-y-2">
          <span className="text-muted-foreground text-xs">Availability</span>
          <div className="flex items-end gap-2">
            <span className="text-2xl font-bold">99.98%</span>
            <span className="text-xs text-green-500 flex items-center mb-1">
              +0.01% <ArrowUpRight className="h-3 w-3" />
            </span>
          </div>
          <div className="h-10 flex items-end">
            <div className="w-full bg-muted rounded-sm h-2">
              <div className="bg-green-500 h-2 rounded-sm" style={{ width: '99.98%' }}></div>
            </div>
          </div>
        </div>
      </div>
    </CardContent>
  </Card>

  {/* Status Code Distribution */}
  <Card>
    <CardHeader>
      <CardTitle>HTTP Status Codes</CardTitle>
    </CardHeader>
    <CardContent>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <h3 className="font-medium mb-4">Status Code Distribution</h3>
          <div className="space-y-3">
            <div className="space-y-1">
              <div className="flex items-center justify-between">
                <span className="text-sm">200 OK</span>
                <span className="text-sm font-medium">94.8%</span>
              </div>
              <div className="h-2 bg-muted rounded-full overflow-hidden">
                <div className="bg-green-500 h-full rounded-full" style={{ width: '94.8%' }}></div>
              </div>
            </div>
            <div className="space-y-1">
              <div className="flex items-center justify-between">
                <span className="text-sm">302 Found</span>
                <span className="text-sm font-medium">2.4%</span>
              </div>
              <div className="h-2 bg-muted rounded-full overflow-hidden">
                <div className="bg-blue-400 h-full rounded-full" style={{ width: '2.4%' }}></div>
              </div>
            </div>
            <div className="space-y-1">
              <div className="flex items-center justify-between">
                <span className="text-sm">404 Not Found</span>
                <span className="text-sm font-medium">2.1%</span>
              </div>
              <div className="h-2 bg-muted rounded-full overflow-hidden">
                <div className="bg-amber-400 h-full rounded-full" style={{ width: '2.1%' }}></div>
              </div>
            </div>
            <div className="space-y-1">
              <div className="flex items-center justify-between">
                <span className="text-sm">500 Server Error</span>
                <span className="text-sm font-medium">0.7%</span>
              </div>
              <div className="h-2 bg-muted rounded-full overflow-hidden">
                <div className="bg-red-500 h-full rounded-full" style={{ width: '0.7%' }}></div>
              </div>
            </div>
          </div>
        </div>
        
        <div>
          <h3 className="font-medium mb-4">Top Endpoints</h3>
          <div className="space-y-3">
            <div className="flex justify-between items-center">
              <div className="flex items-center gap-2">
                <Badge variant="outline" className="bg-blue-50">GET</Badge>
                <span className="text-sm font-medium">/api/products</span>
              </div>
              <div className="text-sm text-muted-foreground">
                242 requests | 87ms avg
              </div>
            </div>
            <div className="flex justify-between items-center">
              <div className="flex items-center gap-2">
                <Badge variant="outline" className="bg-green-50">POST</Badge>
                <span className="text-sm font-medium">/api/users/login</span>
              </div>
              <div className="text-sm text-muted-foreground">
                189 requests | 112ms avg
              </div>
            </div>
            <div className="flex justify-between items-center">
              <div className="flex items-center gap-2">
                <Badge variant="outline" className="bg-blue-50">GET</Badge>
                <span className="text-sm font-medium">/api/dashboard</span>
              </div>
              <div className="text-sm text-muted-foreground">
                156 requests | 95ms avg
              </div>
            </div>
            <div className="flex justify-between items-center">
              <div className="flex items-center gap-2">
                <Badge variant="outline" className="bg-blue-50">GET</Badge>
                <span className="text-sm font-medium">/static/images</span>
              </div>
              <div className="text-sm text-muted-foreground">
                124 requests | 32ms avg
              </div>
            </div>
            <div className="flex justify-between items-center">
              <div className="flex items-center gap-2">
                <Badge variant="outline" className="bg-purple-50">PUT</Badge>
                <span className="text-sm font-medium">/api/settings</span>
              </div>
              <div className="text-sm text-muted-foreground">
                98 requests | 89ms avg
              </div>
            </div>
          </div>
        </div>
      </div>
    </CardContent>
  </Card>

  {/* Resource Usage */}
  <Card>
    <CardContent>
      {/* <div className="grid grid-cols-1 md:grid-cols-3 gap-6"> */}
        {/* Memory Usage */}
        {/* <div className="space-y-4">
          <div className="flex justify-between">
            <h3 className="font-medium">Memory</h3>
            <span className="text-sm text-muted-foreground">512MB allocated</span>
          </div>
          <div className="h-48">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={[
                    { name: 'Used', value: 240, fill: '#8884d8' },
                    { name: 'Free', value: 272, fill: '#f3f4f6' }
                  ]}
                  cx="50%"
                  cy="50%"
                  innerRadius={50}
                  outerRadius={70}
                  paddingAngle={2}
                  dataKey="value"
                >
                  <RechartsLabel
                    value="47% used"
                    position="center"
                    className="text-sm font-medium"
                  />
                </Pie>
                <Tooltip formatter={(value) => `${value}MB`} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <div className="text-sm text-muted-foreground text-center">
            240MB used of 512MB
          </div>
        </div> */}

        {/* CPU Usage */}
        {/* <div className="space-y-4">
          <div className="flex justify-between">
            <h3 className="font-medium">CPU</h3>
            <span className="text-sm text-muted-foreground">2 cores allocated</span>
          </div>
          <div className="h-48">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart
                data={Array(24).fill(0).map((_, i) => ({
                  time: i,
                  usage: 15 + Math.random() * 35
                }))}
              >
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="time" label={<RechartsLabel value="Hours" position="insideBottom" />} />
                <YAxis domain={[0, 100]} label={<RechartsLabel value="% Usage" angle={-90} position="insideLeft" />} />
                <Tooltip formatter={(value) => `${Math.round(value)}%`} />
                <Line type="monotone" dataKey="usage" stroke="#8884d8" dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </div>
          <div className="text-sm text-muted-foreground text-center">
            Average: 32% CPU utilization
          </div>
        </div> */}

        {/* Storage Usage */}
        {/* <div className="space-y-4">
          <div className="flex justify-between">
            <h3 className="font-medium">Storage</h3>
            <span className="text-sm text-muted-foreground">10GB allocated</span>
          </div>
          <div className="h-48">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart
                data={[
                  { type: 'Code', size: 0.5 },
                  { type: 'Static', size: 1.8 },
                  { type: 'Media', size: 5.2 },
                  { type: 'DB', size: 1.2 },
                  { type: 'Logs', size: 0.6 }
                ]}
                layout="vertical"
                margin={{ left: 20 }}
              >
                <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                <XAxis type="number" domain={[0, 10]} tickFormatter={(value) => `${value}GB`} />
                <YAxis type="category" dataKey="type" width={60} />
                <Tooltip formatter={(value) => `${value}GB`} />
                <Bar dataKey="size" fill="#82ca9d" />
              </BarChart>
            </ResponsiveContainer>
          </div>
          <div className="text-sm text-muted-foreground text-center">
            {metrics.storageUsed.toFixed(1)}GB used of 10GB ({Math.round((metrics.storageUsed / 10) * 100)}%)
          </div>
        </div> */}
      {/* </div> */}

      {/* Bandwidth Usage */}
      <div className="mt-3 pt-3">
        <h3 className="font-medium mb-4">Bandwidth Usage</h3>
        <div className="h-64">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart
              data={metrics.performanceData.map(point => ({
                ...point,
                inbound: Math.round(point.requests * 0.05),
                outbound: Math.round(point.requests * 0.25)
              }))}
            >
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="time" />
              <YAxis tickFormatter={(value) => `${value}MB`} />
              <Tooltip formatter={(value) => `${value}MB`} />
              <Legend />
              <Line type="monotone" dataKey="inbound" name="Inbound Traffic" stroke="#3b82f6" />
              <Line type="monotone" dataKey="outbound" name="Outbound Traffic" stroke="#f97316" />
            </LineChart>
          </ResponsiveContainer>
        </div>
        {/* <div className="flex justify-between mt-2 text-sm text-muted-foreground">
          <span>Total Today: 2.8GB transferred</span>
          <span>Monthly: 43.6GB of 100GB used (43.6%)</span>
        </div> */}
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
            <div className={`h-4 w-4 rounded-full ${appData.sslStatus === 'Active' ? 'bg-green-500' : 'bg-yellow-500'}`}></div>
            <h3 className="font-medium">SSL Certificate</h3>
          </div>
          <div className="grid grid-cols-[120px_1fr] gap-y-2 text-sm">
            <span className="text-muted-foreground">Domain:</span>
            <span>www.{appData.name.toLowerCase()}.cloudle.com</span>
            
            <span className="text-muted-foreground">Status:</span>
            <span>{appData.sslStatus === 'Active' ? 'Valid and active' : 'Pending'}</span>
            
            <span className="text-muted-foreground">Issuer:</span>
            <span>Let's Encrypt</span>
            
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

</TabsContent>

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
                        <Input id="app-name" defaultValue={appData.name} className="max-w-md" />
                        <Button variant="outline" size="sm">
                          Update
                        </Button>
                      </div>
                    </div>
                    
                    <div className="space-y-2">
                      <Label htmlFor="environment">Environment</Label>
                      <Select defaultValue={appData.environment}>
                        <SelectTrigger className="max-w-md">
                          <SelectValue placeholder="Select environment" />
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
                        <Switch 
                          id="auto-scaling" 
                          checked={appData.autoScaling === 'Enabled'}
                        />
                        <span>{appData.autoScaling === 'Enabled' ? 'Enabled' : 'Disabled'}</span>
                      </div>
                      <p className="text-sm text-muted-foreground">
                        Automatically scale your application based on load
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>
              
              {/* SSL Settings */}
              <Card>
                <CardHeader>
                  <CardTitle>SSL Certificate</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex items-center gap-2">
                    <div className={`h-3 w-3 rounded-full ${appData.sslStatus === 'Active' ? 'bg-green-500' : 'bg-yellow-500'}`}></div>
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
                      <Upload className="h-8 w-8 mx-auto mb-2 text-gray-400" />
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
                              This action cannot be undone. This will permanently delete your
                              application <span className="font-semibold">{appData.name}</span> and all associated data.
                            </p>
                            <div className="mt-4 space-y-2">
                              <Label htmlFor="confirm">Type <span className="font-semibold">{appData.name}</span> to confirm</Label>
                              <Input id="confirm" placeholder={`Type ${appData.name} to confirm`} />
                            </div>
                          </div>
                          <DialogFooter>
                            <Button variant="outline">Cancel</Button>
                            <Button variant="destructive">
                              Delete Permanently
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
        <Input 
          placeholder="Search logs..." 
          className="w-[200px]"
        />
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
              <td className="py-2 px-4 text-red-600">Failed to connect to database: Connection timed out after 5000ms</td>
            </tr>
            <tr className="border-b hover:bg-muted/50">
              <td className="whitespace-nowrap py-2 px-4 text-muted-foreground">2024-02-18 14:32:42</td>
              <td className="whitespace-nowrap py-2 px-4">
                <Badge variant="outline" className="bg-yellow-50 text-yellow-700 hover:bg-yellow-50">WARN</Badge>
              </td>
              <td className="py-2 px-4 text-yellow-600">Slow query detected: SELECT * FROM products WHERE category_id = 5 (took 2134ms)</td>
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
              <td className="py-2 px-4 text-yellow-600">Memory usage approaching threshold: 412MB/512MB (80.4%)</td>
            </tr>
            <tr className="border-b hover:bg-muted/50">
              <td className="whitespace-nowrap py-2 px-4 text-muted-foreground">2024-02-18 14:32:20</td>
              <td className="whitespace-nowrap py-2 px-4">
                <Badge variant="outline" className="bg-blue-50 text-blue-700 hover:bg-blue-50">INFO</Badge>
              </td>
              <td className="py-2 px-4">Application startup complete. Environment: Production, Version: v1.5.2</td>
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
                  Uncaught TypeError: Cannot read property 'id' of undefined
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
  
  {/* <Card>
    <CardHeader>
      <CardTitle>Log Settings</CardTitle>
    </CardHeader>
    <CardContent>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        <div className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="log-level">Default Log Level</Label>
            <Select defaultValue="info">
              <SelectTrigger id="log-level">
                <SelectValue placeholder="Select log level" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="error">Error</SelectItem>
                <SelectItem value="warn">Warning</SelectItem>
                <SelectItem value="info">Info</SelectItem>
                <SelectItem value="debug">Debug</SelectItem>
                <SelectItem value="trace">Trace</SelectItem>
              </SelectContent>
            </Select>
            <p className="text-xs text-muted-foreground">
              Sets the minimum severity level for logs to be recorded
            </p>
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="log-retention">Log Retention Period</Label>
            <Select defaultValue="30">
              <SelectTrigger id="log-retention">
                <SelectValue placeholder="Select retention period" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="7">7 days</SelectItem>
                <SelectItem value="14">14 days</SelectItem>
                <SelectItem value="30">30 days</SelectItem>
                <SelectItem value="60">60 days</SelectItem>
                <SelectItem value="90">90 days</SelectItem>
              </SelectContent>
            </Select>
            <p className="text-xs text-muted-foreground">
              Logs older than this period will be automatically deleted
            </p>
          </div>
          
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <Label htmlFor="external-logging">External Logging</Label>
              <Switch id="external-logging" />
            </div>
            <p className="text-xs text-muted-foreground">
              Forward logs to an external logging service
            </p>
          </div>
        </div>
        
        <div className="space-y-4">
          <div className="space-y-2">
            <Label>Notification Alerts</Label>
            <div className="rounded-md border p-4 space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <AlertCircle className="h-4 w-4 text-red-500" />
                  <span className="text-sm">Error alerts</span>
                </div>
                <Switch checked />
              </div>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <AlertTriangle className="h-4 w-4 text-yellow-500" />
                  <span className="text-sm">Warning alerts</span>
                </div>
                <Switch checked />
              </div>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Info className="h-4 w-4 text-blue-500" />
                  <span className="text-sm">Info alerts</span>
                </div>
                <Switch />
              </div>
            </div>
            <p className="text-xs text-muted-foreground">
              Receive email notifications for selected log levels
            </p>
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="notification-email">Notification Email</Label>
            <Input id="notification-email" placeholder="admin@example.com" />
          </div>
          
          <Button className="mt-2">
            Save Settings
          </Button>
        </div>
      </div>
    </CardContent>
  </Card> */}
</TabsContent>
        </Tabs>
      </div>
    </div>
  );
};

export default ApplicationDetails;