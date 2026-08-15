import { Outlet, Link, useLocation } from 'react-router-dom'
import { Logo } from './Logo'
import { FloatingOrbs } from '../ui/FloatingOrbs'

export function PublicShell() {
  const location = useLocation()
  const isLanding = location.pathname === '/'

  return (
    <div className="flex min-h-screen flex-col bg-paper">
      <header className="sticky top-0 z-30 bg-paper/90 shadow-[0_1px_0_rgba(184,134,42,0.25)] backdrop-blur-sm">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-4 sm:px-6">
          <Logo />
          <nav className="flex items-center gap-5">
            <Link to="/login" className="link-underline text-[13px] font-medium text-ink-700">
              Log in
            </Link>
            <Link
              to="/register"
              className="rounded-[3px] bg-brand-700 px-4 py-2 text-[13px] font-medium text-white hover:bg-brand-800"
            >
              Apply now
            </Link>
          </nav>
        </div>
      </header>

      <main className={`relative flex-1 ${isLanding ? '' : 'flex items-center justify-center overflow-hidden px-4 py-16'}`}>
        {!isLanding && <FloatingOrbs variant="light" />}
        <div className="relative">
          <Outlet />
        </div>
      </main>

      <footer>
        <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-2 px-4 py-6 text-center sm:flex-row sm:px-6 sm:text-left">
          <p className="font-mono text-[11px] uppercase tracking-[0.1em] text-ink-400">
            ScholarshipHub · Est. {new Date().getFullYear()}
          </p>
          <p className="text-xs text-ink-400">Built for students, reviewers, and administrators.</p>
        </div>
      </footer>
    </div>
  )
}
