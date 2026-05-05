'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useApp } from '@/lib/ctx';
import { api } from '@/lib/api';

type Analytics = {
  users: number; courses: number; lessons: number;
  enrollments: number; submissions: number; comments: number;
};

export default function AdminPage() {
  const { user, t, locale, loading } = useApp();
  const router = useRouter();
  const [analytics, setAnalytics] = useState<Analytics | null>(null);
  const [form, setForm] = useState({
    slug: '', title: '', titleRu: '',
    description: '', descriptionRu: '',
    language: 'Python', level: 'BEGINNER', coverUrl: '',
  });
  const [creating, setCreating] = useState(false);
  const [msg, setMsg] = useState<string | null>(null);

  useEffect(() => {
    if (!loading && (!user || user.role !== 'ADMIN')) router.replace('/');
  }, [loading, user, router]);

  useEffect(() => {
    if (user?.role !== 'ADMIN') return;
    api<Analytics>('/api/admin/analytics', { locale }).then(setAnalytics);
  }, [user, locale]);

  async function createCourse(e: React.FormEvent) {
    e.preventDefault();
    setCreating(true); setMsg(null);
    try {
      await api('/api/admin/courses', { method: 'POST', body: form });
      setMsg('✓ Created.');
      setForm(f => ({ ...f, slug: '', title: '', titleRu: '', description: '', descriptionRu: '', coverUrl: '' }));
      api<Analytics>('/api/admin/analytics', { locale }).then(setAnalytics);
    } catch (e: any) {
      setMsg(e.message || 'Error');
    } finally { setCreating(false); }
  }

  if (user?.role !== 'ADMIN') return null;

  return (
    <div className="px-6 py-12 max-w-6xl mx-auto">
      <p className="eyebrow mb-3">001 — {t('admin.title')}</p>
      <h1 className="display text-6xl md:text-7xl tracking-tightest mb-12">{t('admin.title')}</h1>

      {/* Analytics */}
      <p className="eyebrow mb-6">02 — {t('admin.analytics')}</p>
      {!analytics ? (
        <p className="text-muted">{t('common.loading')}</p>
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-6 border-y border-line divide-y md:divide-y-0 md:divide-x divide-line mb-16">
          <Stat label="Users"        v={analytics.users} />
          <Stat label="Courses"      v={analytics.courses} />
          <Stat label="Lessons"      v={analytics.lessons} />
          <Stat label="Enrollments"  v={analytics.enrollments} />
          <Stat label="Submissions"  v={analytics.submissions} />
          <Stat label="Comments"     v={analytics.comments} />
        </div>
      )}

      {/* Create course */}
      <p className="eyebrow mb-6">03 — {t('admin.create_course')}</p>
      <form onSubmit={createCourse} className="panel p-6 grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-6">
        <Field label={t('admin.form_slug')} value={form.slug}     onChange={v => setForm({ ...form, slug: v })} />
        <Field label={t('admin.form_cover')} value={form.coverUrl} onChange={v => setForm({ ...form, coverUrl: v })} />
        <Field label={t('admin.form_title_en')} value={form.title}      onChange={v => setForm({ ...form, title: v })} />
        <Field label={t('admin.form_title_ru')} value={form.titleRu}    onChange={v => setForm({ ...form, titleRu: v })} />
        <Field label={t('admin.form_description_en')} value={form.description}   onChange={v => setForm({ ...form, description: v })} multiline />
        <Field label={t('admin.form_description_ru')} value={form.descriptionRu} onChange={v => setForm({ ...form, descriptionRu: v })} multiline />
        <SelectField label={t('admin.form_language')} value={form.language} onChange={v => setForm({ ...form, language: v })}
                     options={['Python','Java','Go']} />
        <SelectField label={t('admin.form_level')} value={form.level} onChange={v => setForm({ ...form, level: v })}
                     options={['BEGINNER','INTERMEDIATE','ADVANCED']} />

        <div className="md:col-span-2 flex items-center gap-4">
          <button className="btn btn-accent" disabled={creating}>{creating ? t('common.loading') : t('admin.create')}</button>
          {msg && <span className="mono text-[11px] tracking-[0.18em] uppercase text-muted">{msg}</span>}
        </div>
      </form>
    </div>
  );
}

function Stat({ label, v }: { label: string; v: number }) {
  return (
    <div className="p-6">
      <p className="display text-4xl tnum tracking-tightest">{v}</p>
      <p className="mono text-[11px] tracking-[0.18em] uppercase text-muted mt-2">{label}</p>
    </div>
  );
}

function Field({ label, value, onChange, multiline }: {
  label: string; value: string; onChange: (v: string) => void; multiline?: boolean;
}) {
  return (
    <label className="block">
      <span className="eyebrow">{label}</span>
      {multiline
        ? <textarea rows={3} value={value} onChange={e => onChange(e.target.value)}
                    className="w-full bg-transparent border-b border-line py-2 text-sm focus:border-accent outline-none transition resize-none mt-1" />
        : <input value={value} onChange={e => onChange(e.target.value)} className="input" />}
    </label>
  );
}

function SelectField({ label, value, onChange, options }: {
  label: string; value: string; onChange: (v: string) => void; options: string[];
}) {
  return (
    <label className="block">
      <span className="eyebrow">{label}</span>
      <select value={value} onChange={e => onChange(e.target.value)}
              className="w-full bg-transparent border-b border-line py-2 text-sm focus:border-accent outline-none transition mono">
        {options.map(o => <option key={o} value={o}>{o}</option>)}
      </select>
    </label>
  );
}
