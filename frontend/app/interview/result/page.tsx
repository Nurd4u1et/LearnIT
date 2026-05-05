'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { useApp } from '@/lib/ctx';
import type { InterviewFinish } from '@/lib/types';

export default function InterviewResult() {
  const { t } = useApp();
  const [r, setR] = useState<InterviewFinish | null>(null);

  useEffect(() => {
    try {
      const raw = sessionStorage.getItem('learnly.interview.result');
      if (raw) setR(JSON.parse(raw));
    } catch {}
  }, []);

  if (!r) {
    return (
      <div className="px-6 py-20 max-w-3xl mx-auto">
        <p className="text-muted">{t('common.loading')}</p>
        <Link href="/dashboard" className="btn mt-6 inline-flex">{t('interview.back_dashboard')}</Link>
      </div>
    );
  }

  const isTerm = r.status === 'TERMINATED';
  const grade =
    r.score >= 90 ? 'A' :
    r.score >= 75 ? 'B' :
    r.score >= 60 ? 'C' :
    r.score >= 40 ? 'D' : 'F';

  return (
    <div className="px-6 py-20 max-w-4xl mx-auto">
      <p className="eyebrow mb-3">001 — {t('interview.result')}</p>
      <h1 className="display text-7xl md:text-9xl tracking-tightest mb-8 leading-none">
        {isTerm ? <span className="text-danger">{t('interview.result_status_TERMINATED')}.</span>
                : <>You scored <span className="text-accent">{r.score}</span>.</>}
      </h1>

      <div className="grid grid-cols-3 border-y border-line divide-x divide-line mb-12">
        <div className="p-8">
          <p className="eyebrow">Score</p>
          <p className={`display text-7xl tnum tracking-tightest mt-3 ${isTerm ? 'text-danger' : 'text-accent'}`}>
            {r.score}<span className="text-muted text-3xl"> / 100</span>
          </p>
        </div>
        <div className="p-8">
          <p className="eyebrow">Grade</p>
          <p className="display text-7xl tracking-tightest mt-3">{isTerm ? '—' : grade}</p>
        </div>
        <div className="p-8">
          <p className="eyebrow">Warnings</p>
          <p className="display text-7xl tnum tracking-tightest mt-3">{r.warnings}</p>
        </div>
      </div>

      <div className="flex flex-wrap gap-3">
        <Link href="/interview" className="btn btn-accent">{t('interview.again')} →</Link>
        <Link href="/dashboard" className="btn">{t('interview.back_dashboard')}</Link>
      </div>
    </div>
  );
}
