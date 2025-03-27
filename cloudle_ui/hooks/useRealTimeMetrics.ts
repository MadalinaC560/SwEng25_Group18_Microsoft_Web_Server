// hooks/useRealTimeMetrics.ts
import { useState, useEffect, useCallback } from 'react';
import { RealTimeMetrics } from '@/components/types';

// Mock initial metrics state
const initialMetrics: RealTimeMetrics = {
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

export const useRealTimeMetrics = (appId: number, isRunning: boolean) => {
  const [metrics, setMetrics] = useState<RealTimeMetrics>(initialMetrics);
  const [socket, setSocket] = useState<WebSocket | null>(null);
  
  // Function to update metrics smoothly
  const updateMetrics = useCallback((newRequests: number, avgResponseTime: number) => {
    setMetrics(prev => {
      // Calculate new 24h total
      const new24hTotal = prev.requests24h + newRequests;
      
      // Update performance data
      const currentHour = new Date().getHours();
      const newPerformanceData = [...prev.performanceData];
      const currentHourData = newPerformanceData[currentHour];
      
      newPerformanceData[currentHour] = {
        time: `${currentHour}:00`,
        requests: currentHourData.requests + newRequests,
        responseTime: avgResponseTime,
        errors: Math.floor(Math.random() * (newRequests * 0.05)) // Simulate ~5% error rate
      };

      return {
        ...prev,
        requests24h: new24hTotal,
        avgResponseTime,
        errorRate: 0.05, // Simulated constant error rate
        storageUsed: Math.min(10, prev.storageUsed + (newRequests * 0.0001)), // Simulate storage growth
        performanceData: newPerformanceData
      };
    });
  }, []);

  // Set up WebSocket connection
  useEffect(() => {
    if (!isRunning) return;

    const ws = new WebSocket(`ws://localhost:5001/ws/metrics/${appId}`);
    setSocket(ws);

    // Fallback to polling if WebSocket fails
    let pollInterval: NodeJS.Timeout;
    
    ws.onopen = () => {
      console.log('WebSocket connected');
    };

    ws.onerror = () => {
      console.log('WebSocket failed, falling back to polling');
      // Start polling as fallback
      pollInterval = setInterval(() => {
        const newRequests = Math.floor(Math.random() * 10) + 1; // 1-10 new requests
        const avgResponseTime = Math.floor(Math.random() * 100) + 50; // 50-150ms
        updateMetrics(newRequests, avgResponseTime);
      }, 1000);
    };

    ws.onmessage = (event) => {
      const data = JSON.parse(event.data);
      updateMetrics(data.newRequests, data.avgResponseTime);
    };

    return () => {
      ws.close();
      if (pollInterval) clearInterval(pollInterval);
    };
  }, [appId, isRunning, updateMetrics]);

  // Mock quick bursts of traffic for load testing
  const simulateLoad = useCallback((requestCount: number) => {
    const batchSize = 10;
    const delay = 100; // ms between batches
    let remaining = requestCount;
    
    const processBatch = () => {
      if (remaining <= 0) return;
      
      const currentBatch = Math.min(batchSize, remaining);
      const avgResponseTime = Math.floor(Math.random() * 100) + 50;
      
      updateMetrics(currentBatch, avgResponseTime);
      remaining -= currentBatch;
      
      if (remaining > 0) {
        setTimeout(processBatch, delay);
      }
    };
    
    processBatch();
  }, [updateMetrics]);

  return { metrics, simulateLoad };
};