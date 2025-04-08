export function getTenantIdFromStorage(): number | null {
  if (typeof window === 'undefined') {
    // If this is running server-side, localStorage won't be defined
    return null;
  }
  const raw = localStorage.getItem("user");
  if (!raw) return null;
  try {
    const parsed = JSON.parse(raw);
    return parsed.tenantId ?? null;
  } catch (err) {
    return null;
  }
}
