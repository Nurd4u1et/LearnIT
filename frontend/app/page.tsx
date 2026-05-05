'use client';

import Link from 'next/link';
import { useApp } from '@/lib/ctx';

export default function Home() {
  const { t } = useApp();

  return (
    <div className="px-6 py-16 md:py-24">
      <div className="max-w-7xl mx-auto">

        {/* Hero */}
        <section className="grid grid-cols-12 gap-6 items-end mb-32">
          <div className="col-span-12 md:col-span-8 rise">
            <p className="eyebrow mb-6">001 — {t('tagline')}</p>
            <h1 className="display text-[clamp(48px,10vw,160px)] font-light tracking-tightest">
              Learn<span className="text-accent">.</span><br/>
              <span className="italic font-medium">Practice.</span> Ship<span className="text-accent">.</span>
            </h1>
          </div>
          <div className="col-span-12 md:col-span-4 md:pb-8">
            <p className="text-lg leading-relaxed text-muted mb-6 max-w-sm">
              {t('home.lead')}
            </p>
            <div className="flex flex-wrap gap-3">
              <Link href="/courses" className="btn btn-accent">{t('home.browse')} →</Link>
              <Link href="/interview" className="btn">{t('home.interview_cta')}</Link>
            </div>
          </div>
        </section>

        {/* Three pillars */}
        <section className="grid grid-cols-12 gap-px bg-line/60 border border-line mb-32">
          <Pillar n="02" t={t('home.feature_practice_t')} b={t('home.feature_practice_b')} />
          <Pillar n="03" t={t('home.feature_ai_t')}       b={t('home.feature_ai_b')} />
          <Pillar n="04" t={t('home.feature_iv_t')}       b={t('home.feature_iv_b')} />
        </section>

        {/* Languages strip */}
        <section className="border-t border-line pt-12">
          <p className="eyebrow mb-8">05 — Currently teaching</p>
          <div className="grid grid-cols-3 divide-x divide-line border-y border-line">
            <LangCell name="Python" lessons={2} slug="python-basics" />
            <LangCell name="Java"   lessons={1} slug="java-basics" />
            <LangCell name="Go"     lessons={1} slug="go-basics" />
          </div>
        </section>

      </div>
    </div>
  );
}

function Pillar({ n, t, b }: { n: string; t: string; b: string }) {
  return (
    <div className="col-span-12 md:col-span-4 bg-canvas p-8 md:p-10 min-h-[280px] flex flex-col">
      <span className="eyebrow mb-6">{n}</span>
      <h3 className="display text-3xl mb-4 leading-tight">{t}</h3>
      <p className="text-muted leading-relaxed mt-auto">{b}</p>
    </div>
  );
}

function LangCell({ name, lessons, slug }: { name: string; lessons: number; slug: string }) {
  return (
    <Link
      href={`/courses/${slug}`}
      className="group p-8 hover:bg-panel transition-colors flex items-end justify-between"
    >
      <div>
        <p className="display text-5xl md:text-7xl tracking-tightest">{name}</p>
        <p className="mono text-[11px] tracking-[0.18em] uppercase text-muted mt-3">
          {lessons} {lessons === 1 ? 'lesson' : 'lessons'} · ready
        </p>
      </div>
      <span className="display text-3xl text-muted group-hover:text-accent transition">→</span>
    </Link>
  );
}
