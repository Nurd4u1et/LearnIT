'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useApp } from '@/lib/ctx';
import { api } from '@/lib/api';
import type { Notification } from '@/lib/types';

export default function ProfilePage() {
  const { user, t, locale, setLocale, setUser, loading } = useApp();
  const router = useRouter();
  const [notifications, setNotif] = useState<Notification[]>([]);

  useEffect(() => {
    if (!loading && !user) router.replace('/login');
  }, [loading, user, router]);

  useEffect(() => {
    if (!user) return;
    api<Notification[]>('/api/notifications', { locale }).then(setNotif).catch(() => {});
  }, [user, locale]);

  async function changeLanguage(l: 'en' | 'ru') {
    if (!user || user.language === l) return;
    setLocale(l); // immediate UX feedback
    const u = await api<typeof user>('/api/me/language', { method: 'PATCH', body: { language: l }, locale: l });
    setUser(u);
  }

  async function changePlan(plan: 'FREE' | 'PRO') {
    if (!user) return;
    const u = await api<typeof user>('/api/me/plan', { method: 'PATCH', body: { plan }, locale });
    setUser(u);
  }

  if (!user) return null;

  return (
    <div className="px-6 py-12 max-w-5xl mx-auto">
      <p className="eyebrow mb-3">001 — {t('profile.title')}</p>
      <h1 className="display text-6xl md:text-7xl tracking-tightest mb-12">{user.name}</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-16">
        <div className="panel p-6">
          <p className="eyebrow mb-4">{t('profile.language')}</p>
          <div className="flex gap-2">
            {(['en', 'ru'] as const).map(l => (
              <button key={l} onClick={() => changeLanguage(l)}
                      className={`mono text-[11px] tracking-[0.2em] uppercase px-4 py-2 border transition ${
                        user.language === l ? 'bg-ink text-canvas border-ink' : 'border-line hover:border-ink'
                      }`}>
                {l === 'en' ? 'English' : 'Русский'}
              </button>
            ))}
          </div>
        </div>

        <div className="panel p-6">
          <p className="eyebrow mb-4">{t('profile.plan')}</p>
          <p className="display text-3xl mb-4">
            {user.plan === 'PRO'
              ? <><span className="text-accent">Pro</span> · AI unlocked</>
              : <>Free</>}
          </p>
          {user.plan === 'FREE'
            ? <button onClick={() => changePlan('PRO')} className="btn btn-accent">{t('profile.upgrade_pro')}</button>
            : <button onClick={() => changePlan('FREE')} className="btn">{t('profile.downgrade')}</button>}
        </div>
      </div>

      {/* Account info */}
      <section className="border-y border-line py-6 mb-16">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mono text-sm">
          <div><p className="eyebrow mb-1">Email</p><p>{user.email}</p></div>
          <div><p className="eyebrow mb-1">Phone</p><p>{user.phone}</p></div>
          <div><p className="eyebrow mb-1">Role</p><p>{user.role}</p></div>
        </div>
      </section>

      {/* Notifications */}
      <section>
        <p className="eyebrow mb-6">Notifications</p>
        {notifications.length === 0 ? (
          <p className="text-muted text-sm">Nothing to show.</p>
        ) : (
          <ul className="divide-y divide-line border-y border-line">
            {notifications.map(n => (
              <li key={n.id} className={`py-4 flex items-start gap-4 ${n.read ? 'opacity-60' : ''}`}>
                <span className={`mt-2 w-2 h-2 rounded-full ${n.read ? 'bg-line' : 'bg-accent'}`} />
                <div className="flex-1">
                  <p className="text-sm font-medium">{n.title}</p>
                  {n.body && <p className="text-sm text-muted mt-1">{n.body}</p>}
                  <p className="mono text-[10px] tracking-[0.18em] uppercase text-muted mt-2">
                    {new Date(n.createdAt).toLocaleString()}
                  </p>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
