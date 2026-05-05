/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./app/**/*.{ts,tsx}', './components/**/*.{ts,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      fontFamily: {
        display: ['var(--font-display)', 'serif'],
        sans:    ['var(--font-sans)', 'system-ui'],
        mono:    ['var(--font-mono)', 'monospace'],
      },
      colors: {
        canvas:   'rgb(var(--canvas)   / <alpha-value>)',
        ink:      'rgb(var(--ink)      / <alpha-value>)',
        muted:    'rgb(var(--muted)    / <alpha-value>)',
        line:     'rgb(var(--line)     / <alpha-value>)',
        panel:    'rgb(var(--panel)    / <alpha-value>)',
        accent:   'rgb(var(--accent)   / <alpha-value>)',
        warn:     'rgb(var(--warn)     / <alpha-value>)',
        danger:   'rgb(var(--danger)   / <alpha-value>)',
      },
      letterSpacing: { tightest: '-0.04em' },
      transitionTimingFunction: {
        'out-expo': 'cubic-bezier(0.16, 1, 0.3, 1)',
      },
    },
  },
  plugins: [],
};
