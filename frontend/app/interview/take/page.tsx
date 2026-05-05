'use client';

import { useEffect, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useApp } from '@/lib/ctx';
import { api } from '@/lib/api';
import type { InterviewStart, InterviewFinish } from '@/lib/types';

const MAX_WARNINGS = 3;

export default function InterviewTake() {
  const { t, locale } = useApp();
  const router = useRouter();

  const [start, setStart] = useState<InterviewStart | null>(null);
  const [active, setActive] = useState(0);                // current question index
  const [answers, setAnswers] = useState<string[]>([]);   // local cache
  const [warnings, setWarnings] = useState(0);
  const [warningMsg, setWarningMsg] = useState<string | null>(null);
  const [secondsLeft, setSecondsLeft] = useState(0);
  const [finishing, setFinishing] = useState(false);
  const [terminated, setTerminated] = useState(false);

  const finishRef     = useRef<() => void>(() => {});
  const containerRef  = useRef<HTMLDivElement | null>(null);

  // Boot: read the start payload from sessionStorage
  useEffect(() => {
    let raw: string | null = null;
    try { raw = sessionStorage.getItem('learnly.interview'); } catch {}
    if (!raw) { router.replace('/interview'); return; }
    const s = JSON.parse(raw) as InterviewStart;
    setStart(s);
    setAnswers(new Array(s.questions.length).fill(''));
    setSecondsLeft(s.durationMin * 60);
  }, [router]);

  // Enter fullscreen on mount
  useEffect(() => {
    const el = containerRef.current;
    if (!el || !start) return;
    el.requestFullscreen?.().catch(() => {});
  }, [start]);

  // Timer
  useEffect(() => {
    if (!start || terminated) return;
    const id = setInterval(() => {
      setSecondsLeft(s => {
        if (s <= 1) { clearInterval(id); finishRef.current(); return 0; }
        return s - 1;
      });
    }, 1000);
    return () => clearInterval(id);
  }, [start, terminated]);

  // Anti-cheat: tab switch + fullscreen exit
  useEffect(() => {
    if (!start || terminated) return;

    const reportWarning = async (reason: string, message: string) => {
      setWarnings(w => w + 1);
      setWarningMsg(message);
      try {
        await api(`/api/interview/${start.interviewId}/warning`, {
          method: 'POST', body: { reason },
        });
      } catch {}
      setTimeout(() => setWarningMsg(null), 3500);
    };

    const onVis = () => {
      if (document.hidden) reportWarning('tab-switch', t('interview.warning_tab'));
    };
    const onFs = () => {
      if (!document.fullscreenElement) reportWarning('exit-fullscreen', t('interview.warning_fullscreen'));
    };
    const onBlur = () => reportWarning('window-blur', t('interview.warning_tab'));

    document.addEventListener('visibilitychange', onVis);
    document.addEventListener('fullscreenchange', onFs);
    window.addEventListener('blur', onBlur);
    return () => {
      document.removeEventListener('visibilitychange', onVis);
      document.removeEventListener('fullscreenchange', onFs);
      window.removeEventListener('blur', onBlur);
    };
  }, [start, terminated, t]);

  // Auto-terminate at MAX_WARNINGS
  useEffect(() => {
    if (warnings >= MAX_WARNINGS && !terminated) {
      setTerminated(true);
      finishRef.current();
    }
  }, [warnings, terminated]);

  // Finish handler (also referenced by timer/termination effects)
  finishRef.current = async () => {
    if (!start || finishing) return;
    setFinishing(true);
    try {
      // Persist any unsaved answers in parallel
      await Promise.all(answers.map((a, i) =>
        api(`/api/interview/${start.interviewId}/answer`, {
          method: 'POST', body: { questionId: start.questions[i].id, answer: a },
        }).catch(() => {})));
      const r = await api<InterviewFinish>(`/api/interview/${start.interviewId}/finish`, {
        method: 'POST',
      });
      try { sessionStorage.setItem('learnly.interview.result', JSON.stringify(r)); } catch {}
      try { sessionStorage.removeItem('learnly.interview'); } catch {}
      // Exit fullscreen before navigating
      if (document.fullscreenElement) document.exitFullscreen?.().catch(() => {});
      router.replace('/interview/result');
    } catch {
      setFinishing(false);
    }
  };

  // Update answer, debounced save
  function updateAnswer(idx: number, v: string) {
    setAnswers(a => {
      const next = [...a]; next[idx] = v; return next;
    });
  }

  if (!start) return <div className="p-12 text-muted">{t('common.loading')}</div>;

  const minutes = Math.floor(secondsLeft / 60);
  const seconds = secondsLeft % 60;
  const timeStr = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
  const lowTime = secondsLeft < 120;
  const q = start.questions[active];

  return (
    <div ref={containerRef} className={`min-h-screen ${warnings >= MAX_WARNINGS - 1 ? 'bg-danger/5' : 'bg-canvas'}`}>
      {/* Top bar */}
      <header className="border-b border-line px-6 h-16 flex items-center justify-between sticky top-0 bg-canvas z-10">
        <div className="flex items-center gap-6">
          <span className="display text-xl">Learnly<span className="text-accent">.</span></span>
          <span className="eyebrow">Interview · in progress</span>
        </div>
        <div className="flex items-center gap-6">
          <span className={`mono text-[11px] tracking-[0.18em] uppercase ${
            warnings === 0 ? 'text-muted' : warnings >= 2 ? 'text-danger' : 'text-warn'
          }`}>
            {t('interview.warning_count', { count: warnings })}
          </span>
          <span className={`display text-3xl tnum tabular-nums ${lowTime ? 'text-danger pulse-dot' : 'text-ink'}`}>
            {timeStr}
          </span>
        </div>
      </header>

      <div className="grid grid-cols-1 md:grid-cols-[280px_1fr] min-h-[calc(100vh-64px)]">
        {/* Question rail */}
        <aside className="border-r border-line p-6 bg-panel">
          <p className="eyebrow mb-4">Questions</p>
          <ol className="space-y-1">
            {start.questions.map((qq, i) => (
              <li key={qq.id}>
                <button
                  onClick={() => setActive(i)}
                  className={`w-full text-left p-3 border transition flex items-center gap-3 ${
                    i === active ? 'border-accent bg-accent/5' : 'border-transparent hover:border-line'
                  }`}
                >
                  <span className="mono tnum text-sm w-6">{String(i + 1).padStart(2, '0')}</span>
                  <span className="text-sm flex-1 truncate">{qq.title}</span>
                  {answers[i]?.trim() && <span className="text-accent">●</span>}
                </button>
              </li>
            ))}
          </ol>

          <button onClick={() => finishRef.current()}
                  disabled={finishing}
                  className="btn btn-accent w-full justify-center mt-8">
            {finishing ? t('common.loading') : t('interview.submit_finish')} →
          </button>
        </aside>

        {/* Workspace */}
        <main className="p-8 md:p-12 max-w-4xl">
          <p className="eyebrow mb-2">
            {t('interview.question_n', { n: active + 1, total: start.questions.length })} · {q.language} · {q.difficulty}
          </p>
          <h2 className="display text-4xl md:text-5xl tracking-tightest mb-6 leading-none">{q.title}</h2>
          <p className="text-lg text-muted leading-relaxed mb-8 max-w-2xl">{q.prompt}</p>

          <p className="eyebrow mb-3">{t('interview.your_answer')}</p>
          <textarea
            value={answers[active] ?? ''}
            onChange={e => updateAnswer(active, e.target.value)}
            spellCheck={false}
            rows={16}
            className="w-full bg-panel border border-line p-4 mono text-sm leading-relaxed focus:border-accent outline-none transition resize-y"
            placeholder={`// ${q.language}…`}
          />

          <div className="flex items-center justify-between mt-6">
            <button
              onClick={() => setActive(i => Math.max(0, i - 1))}
              disabled={active === 0}
              className="btn"
            >← Prev</button>
            <button
              onClick={() => setActive(i => Math.min(start.questions.length - 1, i + 1))}
              disabled={active === start.questions.length - 1}
              className="btn"
            >Next →</button>
          </div>
        </main>
      </div>

      {/* Warning toast */}
      {warningMsg && (
        <div className="fixed top-20 left-1/2 -translate-x-1/2 bg-danger text-canvas px-6 py-3 mono text-[12px] tracking-[0.18em] uppercase z-50 rise">
          ⚠ {warningMsg} — {t('interview.warning_count', { count: warnings })}
        </div>
      )}
    </div>
  );
}
