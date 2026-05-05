'use client';

import en from '@/messages/en.json';
import ru from '@/messages/ru.json';

export type Locale = 'en' | 'ru';
const DICTS: Record<Locale, Record<string, unknown>> = { en, ru };

function pickPlural(locale: Locale, n: number): string {
  if (locale === 'ru') {
    const mod10 = n % 10, mod100 = n % 100;
    if (mod10 === 1 && mod100 !== 11) return 'one';
    if (mod10 >= 2 && mod10 <= 4 && (mod100 < 10 || mod100 >= 20)) return 'few';
    return 'many';
  }
  // en
  return n === 1 ? 'one' : 'other';
}

function getPath(obj: Record<string, unknown>, path: string): string | undefined {
  return path.split('.').reduce<unknown>((acc, k) => {
    if (acc && typeof acc === 'object' && k in (acc as Record<string, unknown>)) {
      return (acc as Record<string, unknown>)[k];
    }
    return undefined;
  }, obj) as string | undefined;
}

/**
 * Mini ICU: supports {name} substitution and
 * {count, plural, one {# x} few {# y} other {# z}} (also =1 / =0).
 */
export function formatMessage(
  template: string,
  vars: Record<string, string | number> = {},
  locale: Locale = 'en',
): string {
  // Handle plural blocks first
  const pluralRe = /\{(\w+),\s*plural,([^{}]*(?:\{[^{}]*\}[^{}]*)*)\}/g;
  let out = template.replace(pluralRe, (_full, varName: string, body: string) => {
    const n = Number(vars[varName] ?? 0);
    const cases: Record<string, string> = {};
    const caseRe = /(=\d+|zero|one|two|few|many|other)\s*\{([^}]*)\}/g;
    let m: RegExpExecArray | null;
    while ((m = caseRe.exec(body))) cases[m[1]] = m[2];
    const key = `=${n}` in cases ? `=${n}` : pickPlural(locale, n);
    const chosen = cases[key] ?? cases.other ?? '';
    return chosen.replace(/#/g, String(n));
  });
  // Then simple substitutions
  out = out.replace(/\{(\w+)\}/g, (_, k: string) => String(vars[k] ?? ''));
  return out;
}

export function makeT(locale: Locale) {
  const dict = DICTS[locale] ?? DICTS.en;
  return function t(key: string, vars?: Record<string, string | number>): string {
    const tpl = getPath(dict as Record<string, unknown>, key);
    if (typeof tpl !== 'string') return key;
    return vars ? formatMessage(tpl, vars, locale) : tpl;
  };
}
