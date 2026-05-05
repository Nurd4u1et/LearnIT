'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useApp } from '@/lib/ctx';
import { api } from '@/lib/api';
import type { EnrolledCourse } from '@/lib/types';

export default function Dashboard() {
  const { user, t, locale, loading } = useApp();
  const router = useRouter();
  const [items, setItems] = useState<EnrolledCourse[] | null>(null);
  const [interviews, setInterviews] = useState<number>(0);

  useEffect(() => {
    if (!loading && !user) router.replace('/login');
  }, [loading, user, router]);

  useEffect(() => {
    if (!user) return;
    api<EnrolledCourse[]>('/api/me/courses', { locale }).then(setItems).catch(() => setItems([]));
    // Interview count is best-effort; backend doesn't expose a list endpoint, so we leave 0.
    setInterviews(0);
  }, [user, locale]);

  if (!user) return null;

  const completed = items?.reduce((s, c) => s + c.lessonsCompleted, 0) ?? 0;
  const active    = items?.length ?? 0;

  return (
    <div className="px-6 py-12 max-w-7xl mx-auto">
      <p className="eyebrow mb-3">001 — {t('dashboard.title')}</p>
      <h1 className="display text-5xl md:text-6xl tracking-tightest mb-12">
        {t('dashboard.greeting', { name: user.name })}
      </h1>

      {/* Stat tiles */}
      <section className="grid grid-cols-3 border-y border-line divide-x divide-line mb-16">
        <Stat label={t('dashboard.stats.courses')}    value={active} />
        <Stat label={t('dashboard.stats.completed')}  value={completed} />
        <Stat label={t('dashboard.stats.interviews')} value={interviews} />
      </section>

      {/* Active courses */}
      <p className="eyebrow mb-6">002 — {t('dashboard.your_courses')}</p>
      {!items ? (
        <p className="text-muted">{t('common.loading')}</p>
      ) : items.length === 0 ? (
        <div className="border border-line p-12 text-center">
          <p className="text-muted mb-6">{t('dashboard.no_courses')}</p>
          <Link href="/courses" className="btn btn-accent">{t('dashboard.browse')} →</Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {items.map((it) => (
            <ActiveCourseCard key={it.course.id} item={it} t={t} />
          ))}
        </div>
      )}
    </div>
  );
}

function Stat({ label, value }: { label: string; value: number }) {
  return (
    <div className="p-8">
      <p className="display text-7xl tnum tracking-tightest">{String(value).padStart(2, '0')}</p>
      <p className="eyebrow mt-2">{label}</p>
    </div>
  );
}

function ActiveCourseCard({ item, t }: { item: EnrolledCourse; t: (k: string, v?: any) => string }) {
  const c = item.course;
  return (
    <Link
      href={`/courses/${c.slug}`}
      className="group panel p-6 hover:border-ink transition relative overflow-hidden"
    >
      <div className="flex items-start justify-between gap-6">
        <div className="flex-1 min-w-0">
          <p className="mono text-[11px] tracking-[0.18em] uppercase text-muted mb-3">
            {c.language}
          </p>
          <h3 className="display text-2xl mb-2 truncate">{c.title}</h3>
          <p className="text-sm text-muted mb-6">
            {t('dashboard.progress', { done: item.lessonsCompleted, total: item.lessonsTotal })}
          </p>
        </div>
        <div className="text-right">
          <p className="display text-5xl tnum text-accent">{item.progressPercent}%</p>
        </div>
      </div>
      {/* Progress bar */}
      <div className="h-px bg-line mt-2">
        <div className="h-px bg-accent transition-all duration-500 ease-out-expo"
             style={{ width: `${item.progressPercent}%` }} />
      </div>
      <p className="mt-4 mono text-[11px] tracking-[0.18em] uppercase text-muted group-hover:text-accent transition">
        {t('dashboard.continue')} →
      </p>
    </Link>
  );
}
