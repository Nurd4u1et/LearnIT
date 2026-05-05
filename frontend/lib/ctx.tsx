'use client';

import {
  createContext, useCallback, useContext, useEffect, useMemo, useState,
  type ReactNode,
} from 'react';
import { makeT, type Locale } from './i18n';

export type User = {
  id: number; name: string; email: string; phone: string;
  role: 'USER' | 'ADMIN'; plan: 'FREE' | 'PRO';
  language: Locale; emailVerified: boolean;
};

type Theme = 'light' | 'dark';

type AppCtx = {
  // theme
  theme: Theme;
  setTheme: (t: Theme) => void;
  // locale
  locale: Locale;
  setLocale: (l: Locale) => void;
  t: (k: string, vars?: Record<string, string | number>) => string;
  // auth
  user: User | null;
  loading: boolean;
  setUser: (u: User | null) => void;
  setTokens: (a: string | null, r: string | null) => void;
  accessToken: string | null;
  signOut: () => void;
};

const Ctx = createContext<AppCtx | null>(null);

const STORAGE = {
  access:  'learnly.access',
  refresh: 'learnly.refresh',
  theme:   'learnly.theme',
  locale:  'learnly.locale',
};

export function AppProvider({ children }: { children: ReactNode }) {
  const [theme, setThemeState] = useState<Theme>('dark');
  const [locale, setLocaleState] = useState<Locale>('en');
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  // Initial hydrate from localStorage
  useEffect(() => {
    try {
      const t = (localStorage.getItem(STORAGE.theme) as Theme) || 'dark';
      const l = (localStorage.getItem(STORAGE.locale) as Locale) || 'en';
      const a = localStorage.getItem(STORAGE.access);
      setThemeState(t);
      setLocaleState(l);
      setAccessToken(a);
    } catch { /* SSR-safe */ }
  }, []);

  // Apply theme class to <html>
  useEffect(() => {
    const root = document.documentElement;
    if (theme === 'dark') root.classList.add('dark'); else root.classList.remove('dark');
    try { localStorage.setItem(STORAGE.theme, theme); } catch {}
  }, [theme]);

  useEffect(() => {
    try { localStorage.setItem(STORAGE.locale, locale); } catch {}
    document.documentElement.setAttribute('lang', locale);
  }, [locale]);

  // If we have an access token but no user, fetch /api/me
  useEffect(() => {
    let cancelled = false;
    (async () => {
      if (!accessToken) { setUser(null); setLoading(false); return; }
      setLoading(true);
      try {
        const res = await fetch('/proxy/api/me', {
          headers: {
            Authorization: `Bearer ${accessToken}`,
            'Accept-Language': locale,
          },
        });
        if (!res.ok) throw new Error('unauth');
        const u = (await res.json()) as User;
        if (!cancelled) {
          setUser(u);
          if (u.language && u.language !== locale) setLocaleState(u.language);
        }
      } catch {
        if (!cancelled) {
          setAccessToken(null);
          setUser(null);
          try { localStorage.removeItem(STORAGE.access); } catch {}
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => { cancelled = true; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [accessToken]);

  const setTheme   = useCallback((t: Theme)  => setThemeState(t), []);
  const setLocale  = useCallback((l: Locale) => setLocaleState(l), []);
  const setTokens  = useCallback((a: string | null, r: string | null) => {
    setAccessToken(a);
    try {
      if (a) localStorage.setItem(STORAGE.access, a); else localStorage.removeItem(STORAGE.access);
      if (r) localStorage.setItem(STORAGE.refresh, r); else localStorage.removeItem(STORAGE.refresh);
    } catch {}
  }, []);
  const signOut = useCallback(() => {
    const r = (() => { try { return localStorage.getItem(STORAGE.refresh); } catch { return null; } })();
    if (r) {
      fetch('/proxy/api/auth/logout', {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify({ refreshToken: r }),
      }).catch(() => {});
    }
    setTokens(null, null);
    setUser(null);
  }, [setTokens]);

  const t = useMemo(() => makeT(locale), [locale]);

  const value = useMemo<AppCtx>(() => ({
    theme, setTheme,
    locale, setLocale,
    t,
    user, loading, setUser,
    setTokens, accessToken,
    signOut,
  }), [theme, setTheme, locale, setLocale, t, user, loading, setTokens, accessToken, signOut]);

  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export function useApp(): AppCtx {
  const v = useContext(Ctx);
  if (!v) throw new Error('useApp must be used inside <AppProvider>');
  return v;
}

export function getRefreshToken(): string | null {
  try { return localStorage.getItem(STORAGE.refresh); } catch { return null; }
}
