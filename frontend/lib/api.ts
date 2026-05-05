'use client';

import { getRefreshToken } from './ctx';

type ApiOpts = {
  method?: 'GET' | 'POST' | 'PATCH' | 'DELETE';
  body?: unknown;
  token?: string | null;
  locale?: string;
};

export class ApiError extends Error {
  status: number;
  fields?: Record<string, string>;
  constructor(status: number, message: string, fields?: Record<string, string>) {
    super(message); this.status = status; this.fields = fields;
  }
}

async function refreshAccess(): Promise<string | null> {
  const r = getRefreshToken();
  if (!r) return null;
  try {
    const res = await fetch('/proxy/api/auth/refresh', {
      method: 'POST',
      headers: { 'content-type': 'application/json' },
      body: JSON.stringify({ refreshToken: r }),
    });
    if (!res.ok) return null;
    const data = await res.json();
    try {
      localStorage.setItem('learnly.access', data.accessToken);
      localStorage.setItem('learnly.refresh', data.refreshToken);
    } catch {}
    return data.accessToken as string;
  } catch { return null; }
}

export async function api<T = unknown>(path: string, opts: ApiOpts = {}): Promise<T> {
  const headers: Record<string, string> = {
    'content-type': 'application/json',
    'Accept-Language': opts.locale || 'en',
  };
  let token = opts.token ?? (typeof window !== 'undefined' ? localStorage.getItem('learnly.access') : null);
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const send = (tok: string | null): Promise<Response> => {
    const h = { ...headers };
    if (tok) h['Authorization'] = `Bearer ${tok}`;
    else delete h['Authorization'];
    return fetch(`/proxy${path}`, {
      method: opts.method || 'GET',
      headers: h,
      body: opts.body !== undefined ? JSON.stringify(opts.body) : undefined,
      cache: 'no-store',
    });
  };

  let res = await send(token);
  // Auto-refresh once on 401
  if (res.status === 401 && token) {
    const fresh = await refreshAccess();
    if (fresh) res = await send(fresh);
  }

  // 204 No Content
  if (res.status === 204) return undefined as T;

  const text = await res.text();
  const data = text ? safeJson(text) : null;

  if (!res.ok) {
    const msg = (data && (data.message || data.error)) || `HTTP ${res.status}`;
    throw new ApiError(res.status, msg, data?.fields);
  }
  return data as T;
}

function safeJson(s: string): any {
  try { return JSON.parse(s); } catch { return s; }
}
