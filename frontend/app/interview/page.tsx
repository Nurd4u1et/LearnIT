'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { useApp } from '@/lib/ctx';
import { api } from '@/lib/api';
import type { InterviewStart } from '@/lib/types';

export default function InterviewIntro() {
  const { t, locale, user } = useApp();
  const router = useRouter();
  const [busy, setBusy] = useState(false);

  async function begin() {
    if (!user) { router.push('/login'); return; }
    setBusy(true);
    try {
      const r = await api<InterviewStart>('/api/interview/start', { method: 'POST', locale });
      // Stash the start payload in sessionStorage so the take page reads it without re-starting
      try { sessionStorage.setItem('learnly.interview', JSON.stringify(r)); } catch {}
      router.push(`/interview/take`);
    } finally { setBusy(false); }
  }

  return (
    <div className="px-6 py-20 max-w-5xl mx-auto">
      <p className="eyebrow mb-3">001 — {t('interview.title')}</p>
      <h1 className="display text-6xl md:text-8xl tracking-tightest mb-10 leading-none">
        {t('interview.intro_lead').split('.').map((s, i, arr) => (
          <span key={i}>{s.trim()}{i < arr.length - 1 && '.'}<br/></span>
        ))}
      </h1>

      <div className="grid grid-cols-1 md:grid-cols-3 border-y border-line divide-y md:divide-y-0 md:divide-x divide-line mb-12">
        <Tile k="03" label="Questions" big="3" />
        <Tile k="15" label="Minutes"    big="15" />
        <Tile k="03" label="Warnings allowed" big="3" />
      </div>

      <p className="text-lg text-muted leading-relaxed max-w-2xl mb-10">{t('interview.intro_body')}</p>

      <div className="flex items-center gap-4">
        <button onClick={begin} disabled={busy} className="btn btn-accent">
          {busy ? t('common.loading') : t('interview.begin')} →
        </button>
        <Link href="/dashboard" className="btn btn-ghost">{t('common.back')}</Link>
      </div>
    </div>
  );
}

function Tile({ k, label, big }: { k: string; label: string; big: string }) {
  return (
    <div className="p-8">
      <p className="eyebrow mb-4">{k}</p>
      <p className="display text-7xl tnum tracking-tightest text-accent">{big}</p>
      <p className="mono text-[11px] tracking-[0.2em] uppercase text-muted mt-3">{label}</p>
    </div>
  );
}
