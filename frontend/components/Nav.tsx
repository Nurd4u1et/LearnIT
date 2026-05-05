'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useApp } from '@/lib/ctx';
import { useEffect, useState } from 'react';
import { api } from '@/lib/api';

export function Nav() {
  const { user, t, locale, setLocale, theme, setTheme, signOut } = useApp();
  const path = usePathname();
  const [unread, setUnread] = useState(0);

  // Hide nav entirely while taking an interview (fullscreen mode)
  const hide = path?.startsWith('/interview/take');

  useEffect(() => {
    if (!user) { setUnread(0); return; }
    let alive = true;
    api<{ count: number }>('/api/notifications/unread-count', { locale })
      .then(d => { if (alive) setUnread(d.count); })
      .catch(() => {});
    return () => { alive = false; };
  }, [user, locale, path]);

  if (hide) return null;

  return (
    <header className="border-b border-line/60 bg-canvas/80 backdrop-blur-sm sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-6 h-16 flex items-center gap-8">
        <Link href="/" className="display text-2xl font-medium tracking-tightest leading-none">
          {t('brand')}<span className="text-accent">.</span>
        </Link>

        <nav className="hidden md:flex items-center gap-6 text-sm">
          <NavLink href="/courses"     label={t('nav.courses')}    active={path?.startsWith('/courses')} />
          {user && <NavLink href="/dashboard" label={t('nav.dashboard')} active={path === '/dashboard'} />}
          <NavLink href="/interview"   label={t('nav.interview')}  active={path?.startsWith('/interview')} />
          {user?.role === 'ADMIN' &&
            <NavLink href="/admin" label={t('nav.admin')} active={path?.startsWith('/admin')} />}
        </nav>

        <div className="ml-auto flex items-center gap-3">
          {/* Locale toggle */}
          <button
            onClick={() => setLocale(locale === 'en' ? 'ru' : 'en')}
            className="mono text-[11px] tracking-[0.2em] uppercase px-2 py-1 border border-line hover:border-ink transition"
            aria-label="Toggle language"
          >
            {locale === 'en' ? 'EN · РУ' : 'РУ · EN'}
          </button>
          {/* Theme toggle */}
          <button
            onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
            className="w-8 h-8 grid place-items-center border border-line hover:border-ink transition"
            aria-label={t('common.theme_toggle')}
            title={t('common.theme_toggle')}
          >
            {theme === 'dark' ? <SunIcon /> : <MoonIcon />}
          </button>

          {user ? (
            <>
              {unread > 0 && (
                <span className="mono text-[11px] tracking-wider px-2 py-1 bg-accent text-canvas">
                  {unread}
                </span>
              )}
              <Link href="/profile" className="text-sm hover:text-accent transition">{user.name}</Link>
              <button onClick={signOut} className="btn btn-ghost text-[11px]">{t('nav.logout')}</button>
            </>
          ) : (
            <>
              <Link href="/login"    className="text-sm hover:text-accent transition">{t('nav.login')}</Link>
              <Link href="/register" className="btn btn-accent text-[11px]">{t('nav.register')}</Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}

function NavLink({ href, label, active }: { href: string; label: string; active?: boolean }) {
  return (
    <Link href={href} className={`relative transition ${active ? 'text-ink' : 'text-muted hover:text-ink'}`}>
      {label}
      {active && <span className="absolute -bottom-[22px] left-0 right-0 h-px bg-accent" />}
    </Link>
  );
}

function SunIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <circle cx="12" cy="12" r="4" />
      <path d="M12 2v3M12 19v3M2 12h3M19 12h3M4.9 4.9l2.1 2.1M17 17l2.1 2.1M4.9 19.1L7 17M17 7l2.1-2.1" />
    </svg>
  );
}
function MoonIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <path d="M21 13a8 8 0 11-9.5-9.5A6.5 6.5 0 0021 13z" />
    </svg>
  );
}
