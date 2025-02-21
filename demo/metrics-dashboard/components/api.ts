
import { ApplicationData, RealTimeMetrics } from "@/components/types";

const API_BASE_URL = 'http://localhost:5001/api'; // Replace with your actual API base URL

export const api = {
  async fetchApplicationData(appId: number): Promise<ApplicationData> {
    try {
      const response = await fetch(`${API_BASE_URL}/applications/${appId}`);
      if (!response.ok) throw new Error('Failed to fetch application data');
      return await response.json();
    } catch (error) {
      console.error('Error fetching application data:', error);
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
          errors: 0
        }))
      };
    }
  },

  async toggleApplicationStatus(appId: number, newStatus: 'running' | 'stopped'): Promise<void> {
    try {
      const response = await fetch(`${API_BASE_URL}/applications/${appId}/status`, {
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
  }
};

