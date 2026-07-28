import type { AuthUser } from '../types';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export class ApiError extends Error {
  status: number;
  validationErrors?: Record<string, string>;

  constructor(message: string, status: number, validationErrors?: Record<string, string>) {
    super(message);
    this.status = status;
    this.validationErrors = validationErrors;
  }
}

export async function api<T>(
  path: string,
  options: RequestInit = {},
  token?: string,
): Promise<T> {
  const headers = new Headers(options.headers);
  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`${API_URL}${path}`, { ...options, headers });
  const hasJson = response.headers.get('content-type')?.includes('application/json');
  const body = hasJson ? await response.json() : null;

  if (!response.ok) {
    throw new ApiError(
      body?.message || `Request failed with status ${response.status}`,
      response.status,
      body?.validationErrors,
    );
  }
  return body as T;
}

export function getStoredUser(): AuthUser | null {
  const raw = localStorage.getItem('stayfinder_user');
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    localStorage.removeItem('stayfinder_user');
    return null;
  }
}
