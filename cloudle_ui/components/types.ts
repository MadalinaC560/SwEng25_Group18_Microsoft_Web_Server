
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
  appID: number
  appName: string
  tenantID: number
  appRuntime: string;
  appStatus: string;
  appUrl: string;
  sslStatus: string;
}
