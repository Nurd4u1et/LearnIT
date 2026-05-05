'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { useApp } from '@/lib/ctx';
import { api, ApiError } from '@/lib/api';
import type { CourseDetail, Review } from '@/lib/types';

export default function CourseDetailPage({ params }: { params: { slug: string } }) {
  const { t, locale, user } = useApp();
  const [data, setData]   = useState<CourseDetail | null>(null);
  const [reviews, setRev] = useState<Review[]>([]);
  const [enrolling, setEnrolling] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  // Review form
  const [rating, setRating] = useState(5);
  const [body, setBody]     = useState('');

  async function load() {
    try {
      const d = await api<CourseDetail>(`/api/courses/${params.slug}`, { locale });
      setData(d);
      const rs = await api<Review[]>(`/api/courses/${d.summary.id}/reviews`, { locale });
      setRev(rs);
    } catch (e) {
      setErr(e instanceof ApiError ? e.message : t('common.error_generic'));
    }
  }
  useEffect(() => { load(); /* eslint-disable-next-line */ }, [params.slug, locale]);

  async function enroll() {
    if (!user || !data) return;
    setEnrolling(true);
    try { await api(`/api/courses/${data.summary.id}/enroll`, { method: 'POST' }); await load(); }
    finally { setEnrolling(false); }
  }

  async function submitReview(e: React.FormEvent) {
    e.preventDefault();
    if (!user || !data) return;
    await api(`/api/courses/${data.summary.id}/reviews`, {
      method: 'POST', body: { rating, body }, locale,
    });
    setBody(''); load();
  }

  if (err)   return <div className="px-6 py-12 max-w-7xl mx-auto text-danger">{err}</div>;
  if (!data) return <div className="px-6 py-12 max-w-7xl mx-auto text-muted">{t('common.loading')}</div>;

  const c = data.summary;
  const firstLesson = data.lessons[0];

  return (
    <div className="px-6 py-12 max-w-7xl mx-auto">

      {/* Hero */}
      <div className="grid grid-cols-12 gap-8 mb-16">
        <div className="col-span-12 md:col-span-7 rise">
          <Link href="/courses" className="eyebrow hover:text-ink transition">← {t('courses.title')}</Link>
          <p className="eyebrow mt-6 mb-3">{c.language} · {t(`courses.level_${c.level}`)}</p>
          <h1 className="display text-5xl md:text-7xl tracking-tightest leading-none mb-6">{c.title}</h1>
          <p className="text-lg text-muted leading-relaxed max-w-xl mb-8">{c.description}</p>

          <div className="flex flex-wrap items-center gap-6">
            {data.enrolled ? (
              firstLesson && (
                <Link href={`/lessons/${firstLesson.id}`} className="btn btn-accent">
                  {data.progressPercent > 0 ? t('courses.continue') : t('courses.start')} →
                </Link>
              )
            ) : (
              user
                ? <button className="btn btn-accent" onClick={enroll} disabled={enrolling}>
                    {enrolling ? t('common.loading') : t('courses.enroll')}
                  </button>
                : <Link href="/login" className="btn btn-accent">{t('nav.login')} →</Link>
            )}
            <p className="mono text-[11px] tracking-[0.18em] uppercase text-muted">
              {t('courses.lessons', { count: c.lessonCount })} · ★ {c.avgRating.toFixed(1)}
            </p>
          </div>
        </div>

        <div className="col-span-12 md:col-span-5">
          {c.coverUrl && (
            // eslint-disable-next-line @next/next/no-img-element
            <img src={c.coverUrl} alt=""
                 className="w-full aspect-[4/3] object-cover border border-line" />
          )}
          {data.enrolled && (
            <div className="mt-4 p-4 panel">
              <div className="flex items-center justify-between mb-2">
                <span className="eyebrow">Progress</span>
                <span className="mono text-sm tnum">{data.progressPercent}%</span>
              </div>
              <div className="h-px bg-line">
                <div className="h-px bg-accent transition-all duration-500"
                     style={{ width: `${data.progressPercent}%` }} />
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Lessons list */}
      <section className="mb-20">
        <p className="eyebrow mb-6">{t('courses.lessons_in')}</p>
        <ol className="border-y border-line divide-y divide-line">
          {data.lessons.map((l) => (
            <li key={l.id}>
              <Link href={`/lessons/${l.id}`}
                    className="flex items-center gap-6 py-6 group hover:bg-panel px-4 -mx-4 transition">
                <span className="mono tnum text-2xl text-muted w-12">{String(l.position).padStart(2, '0')}</span>
                <span className="display text-2xl flex-1 group-hover:text-accent transition">{l.title}</span>
                <span className={`mono text-[11px] tracking-[0.18em] uppercase ${l.completed ? 'text-accent' : 'text-muted'}`}>
                  {l.completed ? '● ' + t('lesson.completed') : '○'}
                </span>
              </Link>
            </li>
          ))}
        </ol>
      </section>

      {/* Reviews */}
      <section>
        <div className="flex items-end justify-between mb-6">
          <p className="eyebrow">{t('courses.reviews')}</p>
          <p className="mono text-sm tnum">{c.avgRating.toFixed(1)} / 5.0 · {reviews.length} {t('courses.reviews').toLowerCase()}</p>
        </div>

        {user && (
          <form onSubmit={submitReview} className="panel p-6 mb-8">
            <p className="eyebrow mb-3">{t('courses.leave_review')}</p>
            <div className="flex items-center gap-2 mb-4">
              {[1, 2, 3, 4, 5].map(n => (
                <button type="button" key={n} onClick={() => setRating(n)}
                        className={`text-2xl transition ${n <= rating ? 'text-accent' : 'text-muted hover:text-ink'}`}>
                  ★
                </button>
              ))}
            </div>
            <textarea value={body} onChange={e => setBody(e.target.value)}
                      rows={3} placeholder={t('courses.review_body')}
                      className="w-full bg-transparent border border-line p-3 text-sm focus:border-accent outline-none transition resize-none" />
            <button className="btn btn-accent mt-4">{t('courses.submit_review')}</button>
          </form>
        )}

        {reviews.length === 0 ? (
          <p className="text-muted">{t('courses.no_reviews')}</p>
        ) : (
          <ul className="space-y-6">
            {reviews.map(r => (
              <li key={r.id} className="border-t border-line pt-6">
                <div className="flex items-center justify-between mb-2">
                  <span className="font-medium">{r.userName}</span>
                  <span className="text-accent">{'★'.repeat(r.rating)}<span className="text-muted">{'★'.repeat(5 - r.rating)}</span></span>
                </div>
                {r.body && <p className="text-muted leading-relaxed">{r.body}</p>}
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
