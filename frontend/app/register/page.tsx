'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { useApp } from '@/lib/ctx';
import { api, ApiError } from '@/lib/api';

export default function RegisterPage() {
  const { t, setTokens, setUser } = useApp();
  const router = useRouter();
  const [form, setForm] = useState({ name: '', email: '', phone: '', password: '' });
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState<string | null>(null);
  const [fieldErrs, setFieldErrs] = useState<Record<string, string>>({});

  function update<K extends keyof typeof form>(k: K, v: string) {
    setForm(s => ({ ...s, [k]: v }));
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setErr(null); setFieldErrs({}); setBusy(true);
    try {
      const r = await api<{ accessToken: string; refreshToken: string; user: any }>(
        '/api/auth/register', { method: 'POST', body: form });
      setTokens(r.accessToken, r.refreshToken);
      setUser(r.user);
      router.push('/dashboard');
    } catch (e) {
      if (e instanceof ApiError) {
        setErr(e.message);
        if (e.fields) setFieldErrs(e.fields);
      } else setErr(t('common.error_generic'));
    } finally { setBusy(false); }
  }

  return (
    <div className="min-h-[calc(100vh-64px)] grid grid-cols-1 md:grid-cols-2">
      <div className="flex items-center justify-center p-8 md:p-16 order-2 md:order-1">
        <form onSubmit={submit} className="w-full max-w-md rise">
          <p className="eyebrow mb-2">A — New account</p>
          <h2 className="display text-4xl mb-10">{t('auth.create_account')}</h2>

          <Field label={t('auth.name')} error={fieldErrs.name}>
            <input className="input" required value={form.name}
                   onChange={e => update('name', e.target.value)} />
          </Field>

          <Field label={t('auth.email')} error={fieldErrs.email}>
            <input className="input" type="email" required value={form.email}
                   onChange={e => update('email', e.target.value)} />
          </Field>

          <Field label={t('auth.phone')} error={fieldErrs.phone}>
            <input className="input" required value={form.phone}
                   onChange={e => update('phone', e.target.value)}
                   placeholder="+1234567890" />
          </Field>

          <Field label={t('auth.password')} error={fieldErrs.password}>
            <input className="input" type="password" required value={form.password}
                   onChange={e => update('password', e.target.value)}
                   placeholder="min. 8 characters" />
          </Field>

          {err && <p className="text-danger text-sm mb-6">{err}</p>}

          <button className="btn btn-accent w-full justify-center" disabled={busy}>
            {busy ? t('common.loading') : t('auth.submit_register')}
          </button>

          <p className="mt-8 text-sm text-muted">
            {t('auth.have_account')}{' '}
            <Link href="/login" className="text-ink underline-offset-4 hover:underline">
              {t('auth.to_login')}
            </Link>
          </p>
        </form>
      </div>

      <div className="hidden md:flex flex-col justify-between p-12 border-l border-line bg-panel order-1 md:order-2">
        <p className="eyebrow">B — Begin</p>
        <div>
          <h1 className="display text-7xl tracking-tightest leading-none">
            Open the<br/><span className="italic text-accent">studio</span>.
          </h1>
          <p className="mt-6 text-muted max-w-sm">
            One account. Progress saved per lesson. Free forever for the basics.
          </p>
        </div>
        <p className="mono text-[11px] tracking-[0.2em] uppercase text-muted">
          Free plan · Pro for AI tutoring
        </p>
      </div>
    </div>
  );
}

function Field({ label, error, children }: {
  label: string; error?: string; children: React.ReactNode;
}) {
  return (
    <label className="block mb-8">
      <span className="eyebrow">{label}</span>
      {children}
      {error && <span className="block mt-1 text-xs text-danger">{error}</span>}
    </label>
  );
}
