'use client';

import Link from 'next/link';
import { useEffect, useMemo, useState } from 'react';
import { useApp } from '@/lib/ctx';
import { api } from '@/lib/api';
import type { CourseSummary } from '@/lib/types';

export default function CoursesPage() {
  const { t, locale } = useApp();
  const [courses, setCourses] = useState<CourseSummary[] | null>(null);
  const [filter, setFilter] = useState<string>('All');

  useEffect(() => {
    api<CourseSummary[]>('/api/courses', { locale }).then(setCourses).catch(() => setCourses([]));
  }, [locale]);

  const filtered = useMemo(() => {
    if (!courses) return null;
    return filter === 'All' ? courses : courses.filter(c => c.language === filter);
  }, [courses, filter]);

  const languages = useMemo(
    () => Array.from(new Set((courses ?? []).map(c => c.language))),
    [courses]);

  return (
    <div className="px-6 py-12 max-w-7xl mx-auto">
      <p className="eyebrow mb-3">001 — {t('courses.title')}</p>
      <h1 className="display text-6xl md:text-8xl tracking-tightest mb-12">
        {t('courses.title')}<span className="text-accent">.</span>
      </h1>

      {/* Filter pills */}
      <div className="flex gap-2 flex-wrap mb-10">
        <Pill label={t('courses.filter_all')} active={filter === 'All'} onClick={() => setFilter('All')} />
        {languages.map(l => (
          <Pill key={l} label={l} active={filter === l} onClick={() => setFilter(l)} />
        ))}
      </div>

      {!filtered ? (
        <p className="text-muted">{t('common.loading')}</p>
      ) : filtered.length === 0 ? (
        <p className="text-muted">No courses match this filter.</p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filtered.map((c, i) => <CourseCard key={c.id} c={c} index={i} t={t} />)}
        </div>
      )}
    </div>
  );
}

function Pill({ label, active, onClick }: { label: string; active: boolean; onClick: () => void }) {
  return (
    <button
      onClick={onClick}
      className={`mono text-[11px] tracking-[0.2em] uppercase px-4 py-2 border transition ${
        active ? 'border-ink bg-ink text-canvas' : 'border-line hover:border-ink'
      }`}
    >
      {label}
    </button>
  );
}

function CourseCard({ c, index, t }: {
  c: CourseSummary; index: number; t: (k: string, v?: any) => string;
}) {
  return (
    <Link
      href={`/courses/${c.slug}`}
      className="group panel hover:border-ink transition rise"
      style={{ animationDelay: `${index * 80}ms` }}
    >
      <div className="aspect-[16/10] relative overflow-hidden border-b border-line bg-canvas">
        {c.coverUrl && (
          // eslint-disable-next-line @next/next/no-img-element
          <img src={c.coverUrl} alt=""
               className="absolute inset-0 w-full h-full object-cover opacity-60 group-hover:opacity-90 transition" />
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-canvas/70 to-transparent" />
        <p className="absolute top-4 left-4 mono text-[10px] tracking-[0.2em] uppercase bg-canvas/80 px-2 py-1">
          {c.language}
        </p>
        <p className="absolute bottom-4 left-4 mono text-[10px] tracking-[0.2em] uppercase text-muted">
          {t(`courses.level_${c.level}`)}
        </p>
      </div>
      <div className="p-6">
        <h3 className="display text-3xl tracking-tightest mb-3 leading-tight">{c.title}</h3>
        <p className="text-sm text-muted line-clamp-3 mb-6">{c.description}</p>
        <div className="flex items-end justify-between">
          <p className="mono text-[11px] tracking-[0.18em] uppercase text-muted">
            {t('courses.lessons', { count: c.lessonCount })}
          </p>
          <p className="mono text-[11px] tracking-[0.18em] uppercase text-muted">
            ★ {c.avgRating.toFixed(1)}
          </p>
        </div>
      </div>
    </Link>
  );
}
