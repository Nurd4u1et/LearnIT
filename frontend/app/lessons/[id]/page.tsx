'use client';

import Link from 'next/link';
import { useEffect, useMemo, useState } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { useApp } from '@/lib/ctx';
import { api, ApiError } from '@/lib/api';
import type { LessonDetail, Comment, Submission, AiMessage } from '@/lib/types';

export default function LessonPage({ params }: { params: { id: string } }) {
  const { t, locale, user } = useApp();
  const lessonId = Number(params.id);

  const [lesson, setLesson] = useState<LessonDetail | null>(null);
  const [code, setCode] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [lastSub, setLastSub] = useState<Submission | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);

  async function loadLesson() {
    const l = await api<LessonDetail>(`/api/lessons/${lessonId}`, { locale });
    setLesson(l);
  }
  async function loadComments() {
    const c = await api<Comment[]>(`/api/lessons/${lessonId}/comments`, { locale });
    setComments(c);
  }
  useEffect(() => {
    loadLesson(); loadComments();
    // eslint-disable-next-line
  }, [lessonId, locale]);

  async function submit() {
    if (!user) return;
    setSubmitting(true);
    try {
      const s = await api<Submission>(`/api/lessons/${lessonId}/submissions`, {
        method: 'POST', body: { content: code },
      });
      setLastSub(s);
      if (s.status === 'PASSED') loadLesson();
    } catch (e) {
      setLastSub({
        id: -1, status: 'FAILED', content: code,
        feedback: e instanceof ApiError ? e.message : t('common.error_generic'),
        createdAt: new Date().toISOString(),
      });
    } finally { setSubmitting(false); }
  }

  async function markComplete() {
    if (!user) return;
    await api(`/api/lessons/${lessonId}/complete`, { method: 'POST' });
    loadLesson();
  }

  if (!lesson) {
    return <div className="px-6 py-12 max-w-7xl mx-auto text-muted">{t('common.loading')}</div>;
  }

  return (
    <div className="grid grid-cols-1 xl:grid-cols-[1fr_360px] min-h-[calc(100vh-64px)]">
      {/* Main column */}
      <div className="px-6 py-10 max-w-4xl mx-auto w-full">
        {/* Header */}
        <div className="mb-10">
          <Link href={`/courses`} className="eyebrow hover:text-ink transition">← {t('courses.title')}</Link>
          <p className="mono text-[11px] tracking-[0.18em] uppercase text-muted mt-6">
            {t('interview.question_n', { n: lesson.position, total: lesson.position })}
          </p>
          <h1 className="display text-4xl md:text-6xl tracking-tightest leading-none mt-2">
            {lesson.title}
          </h1>
        </div>

        {/* Video */}
        {lesson.videoUrl && (
          <div className="aspect-video w-full mb-10 border border-line bg-black">
            <iframe
              src={lesson.videoUrl}
              title={lesson.title}
              allow="accelerometer; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
              allowFullScreen
              className="w-full h-full"
            />
          </div>
        )}

        {/* Description */}
        <article className="prose-mini text-base leading-relaxed mb-12 max-w-2xl">
          <ReactMarkdown remarkPlugins={[remarkGfm]}>{lesson.description}</ReactMarkdown>
        </article>

        {/* Task */}
        <section className="border-l-2 border-accent pl-6 py-2 mb-8">
          <p className="eyebrow mb-3">{t('lesson.task')}</p>
          <p className="text-lg leading-relaxed">{lesson.task}</p>
        </section>

        {/* Submission */}
        <section className="mb-12">
          <div className="flex items-center justify-between mb-3">
            <p className="eyebrow">{t('lesson.your_solution')}</p>
            <span className="mono text-[11px] tracking-[0.18em] uppercase text-muted">CODE / TEXT</span>
          </div>
          <textarea
            value={code}
            onChange={e => setCode(e.target.value)}
            rows={10}
            spellCheck={false}
            placeholder="// your solution here"
            className="w-full bg-panel border border-line p-4 mono text-sm leading-relaxed focus:border-accent outline-none transition resize-y"
          />
          <div className="flex flex-wrap items-center gap-3 mt-4">
            <button onClick={submit} disabled={submitting || !code.trim() || !user}
                    className="btn btn-accent">
              {submitting ? t('lesson.submitting') : t('lesson.submit')}
            </button>
            {!lesson.completed && user && (
              <button onClick={markComplete} className="btn">
                {t('lesson.complete')}
              </button>
            )}
            {lesson.completed && (
              <span className="mono text-[11px] tracking-[0.18em] uppercase text-accent">
                ● {t('lesson.completed')}
              </span>
            )}
          </div>

          {lastSub && (
            <div className={`mt-6 p-4 border ${
              lastSub.status === 'PASSED' ? 'border-accent bg-accent/5' :
              lastSub.status === 'FAILED' ? 'border-danger bg-danger/5' :
              'border-line bg-panel'
            }`}>
              <p className={`eyebrow mb-1 ${
                lastSub.status === 'PASSED' ? 'text-accent' :
                lastSub.status === 'FAILED' ? 'text-danger' : ''
              }`}>
                {lastSub.status === 'PASSED' ? t('lesson.passed')
                  : lastSub.status === 'FAILED' ? t('lesson.failed')
                  : t('lesson.submitted')}
              </p>
              {lastSub.feedback && <p className="text-sm">{lastSub.feedback}</p>}
            </div>
          )}
        </section>

        {/* Comments */}
        <section className="border-t border-line pt-10">
          <p className="eyebrow mb-6">{t('lesson.comments')} · {comments.length}</p>
          <CommentList comments={comments} t={t} />
          {user && <CommentBox lessonId={lessonId} onPosted={loadComments} t={t} />}
        </section>
      </div>

      {/* AI sidebar */}
      <aside className="border-l border-line bg-panel hidden xl:flex flex-col h-[calc(100vh-64px)] sticky top-16">
        <AiPanel lessonId={lessonId} />
      </aside>
    </div>
  );
}

/* ---------------- Comments ---------------- */

function CommentList({ comments, t }: {
  comments: Comment[]; t: (k: string, v?: any) => string;
}) {
  // Group replies under their parent
  const tree = useMemo(() => {
    const roots: (Comment & { children: Comment[] })[] = [];
    const map = new Map<number, Comment & { children: Comment[] }>();
    for (const c of comments) map.set(c.id, { ...c, children: [] });
    for (const c of comments) {
      const node = map.get(c.id)!;
      if (c.parentId && map.has(c.parentId)) map.get(c.parentId)!.children.push(node);
      else roots.push(node);
    }
    return roots;
  }, [comments]);

  if (comments.length === 0) return <p className="text-muted text-sm mb-8">No comments yet — start the discussion.</p>;
  return (
    <ul className="space-y-6 mb-8">
      {tree.map(c => <CommentItem key={c.id} c={c} t={t} />)}
    </ul>
  );
}

function CommentItem({ c, t }: { c: Comment & { children: Comment[] }; t: (k: string, v?: any) => string }) {
  return (
    <li>
      <div className="flex items-start gap-3 mb-1">
        <span className="display text-lg leading-none">{c.userName}</span>
        <span className="mono text-[10px] tracking-[0.18em] uppercase text-muted">
          {new Date(c.createdAt).toLocaleString()}
        </span>
      </div>
      <p className="text-sm leading-relaxed mb-2">{c.body}</p>
      {c.children.length > 0 && (
        <ul className="ml-6 pl-4 border-l border-line space-y-4 mt-4">
          {c.children.map(ch => <CommentItem key={ch.id} c={{ ...ch, children: [] }} t={t} />)}
        </ul>
      )}
    </li>
  );
}

function CommentBox({ lessonId, onPosted, t }: {
  lessonId: number; onPosted: () => void; t: (k: string, v?: any) => string;
}) {
  const [body, setBody] = useState('');
  const [busy, setBusy] = useState(false);
  async function post() {
    if (!body.trim()) return;
    setBusy(true);
    try {
      await api(`/api/lessons/${lessonId}/comments`, { method: 'POST', body: { body } });
      setBody(''); onPosted();
    } finally { setBusy(false); }
  }
  return (
    <div className="border-t border-line pt-6">
      <textarea
        value={body}
        onChange={e => setBody(e.target.value)}
        rows={3}
        placeholder={t('lesson.write_comment')}
        className="w-full bg-canvas border border-line p-3 text-sm focus:border-accent outline-none transition resize-none"
      />
      <button className="btn btn-accent mt-3" onClick={post} disabled={busy || !body.trim()}>
        {busy ? t('common.loading') : t('lesson.post')}
      </button>
    </div>
  );
}

/* ---------------- AI sidebar ---------------- */

function AiPanel({ lessonId }: { lessonId: number }) {
  const { user, t, locale } = useApp();
  const [history, setHistory] = useState<AiMessage[]>([]);
  const [input, setInput] = useState('');
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    if (!user || user.plan !== 'PRO') return;
    api<AiMessage[]>('/api/ai/history', { locale }).then(setHistory).catch(() => {});
  }, [user, locale]);

  async function ask() {
    if (!input.trim()) return;
    const text = input.trim();
    setInput(''); setBusy(true); setErr(null);
    // Optimistic
    setHistory(h => [...h, {
      id: -Date.now(), role: 'USER', content: text, createdAt: new Date().toISOString(),
    }]);
    try {
      const reply = await api<AiMessage>('/api/ai/ask', {
        method: 'POST', body: { message: text, lessonId },
      });
      setHistory(h => [...h, reply]);
    } catch (e) {
      setErr(e instanceof ApiError ? e.message : t('common.error_generic'));
    } finally { setBusy(false); }
  }

  if (!user) {
    return <div className="p-6 m-auto text-center text-sm text-muted">
      <Link href="/login" className="underline">{t('nav.login')}</Link> {t('lesson.ai_assistant').toLowerCase()}
    </div>;
  }
  if (user.plan !== 'PRO') {
    return (
      <div className="p-6 m-auto text-center max-w-xs">
        <p className="display text-2xl mb-3">{t('lesson.ai_assistant')}</p>
        <p className="text-sm text-muted mb-6">{t('lesson.ai_pro_only')}</p>
        <Link href="/profile" className="btn btn-accent">{t('lesson.upgrade')} →</Link>
      </div>
    );
  }

  return (
    <>
      <div className="p-5 border-b border-line">
        <p className="eyebrow">{t('lesson.ai_assistant')}</p>
      </div>
      <div className="flex-1 overflow-y-auto p-5 space-y-4">
        {history.length === 0 && (
          <p className="text-sm text-muted italic">Ask a question about this lesson…</p>
        )}
        {history.map(m => (
          <div key={m.id} className={`text-sm ${m.role === 'USER' ? 'text-ink' : 'text-muted'}`}>
            <p className="eyebrow mb-1">{m.role === 'USER' ? 'You' : 'Tutor'}</p>
            <div className="prose-mini">
              <ReactMarkdown remarkPlugins={[remarkGfm]}>{m.content}</ReactMarkdown>
            </div>
          </div>
        ))}
        {busy && <p className="text-xs text-muted italic"><span className="pulse-dot">●</span> thinking…</p>}
        {err && <p className="text-xs text-danger">{err}</p>}
      </div>
      <div className="p-5 border-t border-line">
        <textarea
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => { if (e.key === 'Enter' && (e.metaKey || e.ctrlKey)) ask(); }}
          rows={3}
          placeholder={t('lesson.ai_placeholder')}
          className="w-full bg-canvas border border-line p-3 text-sm focus:border-accent outline-none transition resize-none mono"
        />
        <button onClick={ask} disabled={busy || !input.trim()} className="btn btn-accent w-full justify-center mt-3">
          {t('lesson.post')} ⌘↵
        </button>
      </div>
    </>
  );
}
