'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { useApp } from '@/lib/ctx';
import { api, ApiError } from '@/lib/api';

export default function LoginPage() {
  const { t, setTokens, setUser } = useApp();
  const router = useRouter();
  const [identifier, setId] = useState('');
  const [password, setPwd] = useState('');
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setErr(null); setBusy(true);
    try {
      const r = await api<{ accessToken: string; refreshToken: string; user: any }>(
        '/api/auth/login', { method: 'POST', body: { identifier, password } });
      setTokens(r.accessToken, r.refreshToken);
      setUser(r.user);
      router.push('/dashboard');
    } catch (e) {
      setErr(e instanceof ApiError ? e.message : t('common.error_generic'));
    } finally { setBusy(false); }
  }

  return (
    <div className="min-h-[calc(100vh-64px)] grid grid-cols-1 md:grid-cols-2">
      {/* Left rail: editorial side */}
      <div className="hidden md:flex flex-col justify-between p-12 border-r border-line bg-panel">
        <p className="eyebrow">A — return to studio</p>
        <div>
          <h1 className="display text-7xl tracking-tightest leading-none">
            Welcome <span className="italic text-accent">back</span>.
          </h1>
          <p className="mt-6 text-muted max-w-sm">
            Pick up exactly where you left off. Progress is preserved per lesson.
          </p>
        </div>
        <p className="mono text-[11px] tracking-[0.2em] uppercase text-muted">
          Learnly · Studio Edition
        </p>
      </div>

      {/* Form */}
      <div className="flex items-center justify-center p-8 md:p-16">
        <form onSubmit={submit} className="w-full max-w-sm rise">
          <p className="eyebrow mb-2">B — Authentication</p>
          <h2 className="display text-4xl mb-10">{t('auth.sign_in')}</h2>

          <label className="block mb-8">
            <span className="eyebrow">{t('auth.identifier')}</span>
            <input className="input" required value={identifier}
                   onChange={e => setId(e.target.value)}
                   placeholder="[email protected] / +1…" />
          </label>

          <label className="block mb-10">
            <span className="eyebrow">{t('auth.password')}</span>
            <input className="input" type="password" required value={password}
                   onChange={e => setPwd(e.target.value)} placeholder="••••••••" />
          </label>

          {err && <p className="text-danger text-sm mb-6">{err}</p>}

          <button className="btn btn-accent w-full justify-center" disabled={busy}>
            {busy ? t('common.loading') : t('auth.submit_login')}
          </button>

          <p className="mt-8 text-sm text-muted">
            {t('auth.no_account')}{' '}
            <Link href="/register" className="text-ink underline-offset-4 hover:underline">
              {t('auth.to_register')}
            </Link>
          </p>
        </form>
      </div>
    </div>
  );
}
