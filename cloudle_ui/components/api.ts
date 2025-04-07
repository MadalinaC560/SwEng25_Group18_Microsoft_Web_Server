
export interface DBApp {
  appId: number;
  tenantId: number;
  ownerUserId: number;
  name: string;
  runtime: string;    // e.g. "nodejs", "php", "dotnet"
  status: string;     // e.g. "running" or "stopped"
  routes?: string[];  // optional array of routes
}

// Returns per-app metrics from new endpoint
export interface AppMetrics {
  avgResponseTime: number;       // e.g. 52.3 (ms)
  requestThroughput: number;     // e.g. 100 (lifetime or rolling count)
  errorRate: number;             // as a percentage, e.g. 3.2
  availability: number;          // as a percentage, e.g. 96.8
  performanceData: Array<{
    time: string;
    // might consider other fields if time permits
  }>;
}

export async function getTenantAppMetrics(tenantId: number, appId: number): Promise<AppMetrics> {
  const resp = await fetch(`http://localhost:8080/api/tenants/${tenantId}/apps/${appId}/metrics`);
  if (!resp.ok) {
    throw new Error(`Failed to fetch metrics for app #${appId} in tenant #${tenantId}; status=${resp.status}`);
  }
  return resp.json();
}


// -----------------------
//  Basic or Global calls
// -----------------------

// Lists all apps (GET /api/apps)
export async function getAllApps(): Promise<DBApp[]> {
  const resp = await fetch("http://localhost:8080/api/apps");
  if (!resp.ok) {
    throw new Error(`Failed to fetch apps; status=${resp.status}`);
  }
  return resp.json();
}

// Refresh DB (POST /api/refresh)
export async function refreshDB(): Promise<void> {
  const resp = await fetch("http://localhost:8080/api/refresh", { method: "POST" });
  if (!resp.ok) {
    throw new Error(`Failed to refresh DB; status=${resp.status}`);
  }
}

// -----------------------
//  Tenant-level endpoints
// -----------------------

// Create an app under a specific tenant (POST /api/tenants/{tenantId}/apps)
export async function createApp(
    tenantId: number,
    name: string,
    runtime: string,
    ownerUserId: number
): Promise<DBApp> {
  const resp = await fetch(`http://localhost:8080/api/tenants/${tenantId}/apps`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, runtime, ownerUserId }),
  });
  if (!resp.ok) {
    throw new Error(`Failed to create app; status=${resp.status}`);
  }
  return resp.json();
}

// Upload zip as raw bytes (POST /api/tenants/{tenantId}/apps/{appId}/upload)
export async function uploadZip(
    tenantId: number,
    appId: number,
    zipBytes: Uint8Array
): Promise<void> {
  const url = `http://localhost:8080/api/tenants/${tenantId}/apps/${appId}/upload`;
  const resp = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/octet-stream" },
    body: zipBytes,
  });
  if (!resp.ok) {
    throw new Error(`Failed to upload zip; status=${resp.status}`);
  }
}

// -----------------------
// Single tenant-app calls
// (handleSingleAppUnderTenant routes)
// -----------------------

// Get a single app by tenantId & appId (GET /api/tenants/{tenantId}/apps/{appId})
export async function getTenantApp(tenantId: number, appId: number): Promise<DBApp> {
  const resp = await fetch(`http://localhost:8080/api/tenants/${tenantId}/apps/${appId}`);
  if (!resp.ok) {
    throw new Error(`Failed to fetch app #${appId} in tenant ${tenantId}; status=${resp.status}`);
  }
  return resp.json();
}

export async function getTenantApps(tenantId: number): Promise<DBApp[]> {
  const resp = await fetch(`http://localhost:8080/api/tenants/${tenantId}/apps`);
  if (!resp.ok) {
    throw new Error(`Failed to list apps for tenant=${tenantId}, status=${resp.status}`);
  }
  return resp.json();
}

// Update an app’s fields, e.g. name, runtime, or status (PUT /api/tenants/{tenantId}/apps/{appId})
export async function updateTenantApp(
    tenantId: number,
    appId: number,
    fields: Partial<{ name: string; runtime: string; status: string }>
): Promise<DBApp> {
  const resp = await fetch(`http://localhost:8080/api/tenants/${tenantId}/apps/${appId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(fields),
  });
  if (!resp.ok) {
    throw new Error(`Failed to update app #${appId}; status=${resp.status}`);
  }
  return resp.json();
}

// Delete an app (DELETE /api/tenants/{tenantId}/apps/{appId})
export async function deleteTenantApp(tenantId: number, appId: number): Promise<void> {
  const resp = await fetch(`http://localhost:8080/api/tenants/${tenantId}/apps/${appId}`, {
    method: "DELETE",
  });
  // The server typically returns 204 on success
  if (!resp.ok && resp.status !== 204) {
    throw new Error(`Failed to delete app #${appId}; status=${resp.status}`);
  }
}


export async function setAppStatus(tenantId: number, appId: number, newStatus: string): Promise<void> {
  await updateTenantApp(tenantId, appId, { status: newStatus });
}

