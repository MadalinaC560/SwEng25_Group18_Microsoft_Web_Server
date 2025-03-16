
export interface PerformanceData {
  time: string;
  responseTime: number;
  requests: number;
  errors: number;
}

export interface RealTimeMetrics {
  requests24h: number;
  avgResponseTime: number;
  errorRate: number;
  storageUsed: number;
  performanceData: PerformanceData[];
}

export interface ApplicationData {
  name: string;
  status: 'running' | 'stopped';
  url: string;
  runtime: string;
  environment: string;
  sslStatus: string;
  autoScaling: string;
  version: string;
  lastDeployment: string;
}