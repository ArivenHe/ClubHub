const THEME_KEY = 'clubhub_theme'
const LIGHT = 'light'
const DARK = 'dark'

function getSystemTheme() {
  if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
    return DARK
  }
  return LIGHT
}

export function getTheme() {
  const stored = localStorage.getItem(THEME_KEY)
  if (stored === LIGHT || stored === DARK) {
    return stored
  }
  return getSystemTheme()
}

export function applyTheme(theme) {
  const nextTheme = theme === DARK ? DARK : LIGHT
  document.documentElement.setAttribute('data-theme', nextTheme)
}

export function setTheme(theme) {
  const nextTheme = theme === DARK ? DARK : LIGHT
  localStorage.setItem(THEME_KEY, nextTheme)
  applyTheme(nextTheme)
  return nextTheme
}

export function toggleTheme() {
  const nextTheme = getTheme() === DARK ? LIGHT : DARK
  return setTheme(nextTheme)
}

export function initTheme() {
  const current = getTheme()
  applyTheme(current)
  return current
}
