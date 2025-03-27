
import { ApplicationData, RealTimeMetrics } from "@/components/types";

// Example interface matching your server's JSON
interface ServerAppData {
  appId: number;
  name: string;
  status: string; // The server returns a generic string.
  url?: string;
  runtime?: string;
  environment?: string;
  sslStatus?: string;
  autoScaling?: string;
  version?: string;
  lastDeployment?: string;
}

const API_BASE_URL = 'http://localhost:8080/api';

export const api = {
  // NEW version: fetch data for a single app by calling ?userId=...,
  // then pick out the one with matching appId.
  async fetchApplicationData(userId: number, appId: number): Promise<ApplicationData> {
    try {
      // 1) Call the Java endpoint to fetch all apps for this user
      const response = await fetch(`${API_BASE_URL}/applications?userId=${userId}`, {
        method: 'GET',
        headers: {
          Accept: 'application/json',
        },
      });
      if (!response.ok) {
        throw new Error(`Failed to fetch apps for userId=${userId}; status=${response.status}`);
      }

      const apps: ServerAppData[] = await response.json();

      // 3) Find the specific app
      const found = apps.find((app) => app.appId === appId);

      if (!found) {
        throw new Error(`No app with appId=${appId} for userId=${userId}`);
      }

      // Convert server's status (string) to a literal type "running" | "stopped"
      const status = found.status === 'running' ? 'running' : 'stopped';

      // 4) Return an object shaped the way ApplicationDetails.tsx expects
      return {
        name: found.name || '',
        status, // now typed as "running" | "stopped"
        url: found.url || '',
        runtime: found.runtime || '',
        environment: found.environment || '',
        sslStatus: found.sslStatus || '',
        autoScaling: found.autoScaling || '',
        version: found.version || '',
        lastDeployment: found.lastDeployment || '',
      };

    } catch (error) {
      console.error('Error fetching application data:', error);
      // Fallback: blank default object
      return {
        name: '',
        status: 'stopped',
        url: '',
        runtime: '',
        environment: '',
        sslStatus: '',
        autoScaling: '',
        version: '',
        lastDeployment: '',
      };
    }
  },

  async fetchMetrics(appId: number): Promise<RealTimeMetrics> {
    try {
      const response = await fetch(`${API_BASE_URL}/applications/${appId}/metrics`);
      if (!response.ok) throw new Error('Failed to fetch metrics');
      return await response.json();
    } catch (error) {
      console.error('Error fetching metrics:', error);
      return {
        requests24h: 0,
        avgResponseTime: 0,
        errorRate: 0,
        storageUsed: 0,
        performanceData: Array(24).fill(null).map((_, i) => ({
          time: `${i}:00`,
          responseTime: 0,
          requests: 0,
          errors: 0,
        })),
      };
    }
  },

  async toggleApplicationStatus(appId: number, newStatus: 'running' | 'stopped'): Promise<void> {
    const userId = 3
    try {
      const response = await fetch(`${API_BASE_URL}/applications/status?userId=${userId}&appId=${appId}&status=${newStatus}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ status: newStatus }),
      });
      if (!response.ok) throw new Error('Failed to update application status');
    } catch (error) {
      console.error('Error updating application status:', error);
      throw error;
    }
  },

  async deployNewVersion(appId: number, version: string): Promise<void> {
    try {
      const response = await fetch(`${API_BASE_URL}/applications/${appId}/deploy`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ version }),
      });
      if (!response.ok) throw new Error('Failed to deploy new version');
    } catch (error) {
      console.error('Error deploying new version:', error);
      throw error;
    }
  },
};
