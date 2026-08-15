import { Outlet, Link } from 'react-router-dom'
import { ChevronRight } from 'lucide-react'
import { Logo } from './Logo'
import { FloatingOrbs } from '../ui/FloatingOrbs'

/** Full-screen split layout for every auth page: a dark promo panel on the
 *  left (hidden below lg), the form on the right. Replaces the shared
 *  site header/footer for this one flow -- it's the front door, it gets
 *  to look like one. */
export function AuthLayout() {
  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      <div className="relative hidden flex-col justify-between overflow-hidden bg-brand-950 p-12 lg:flex">
        <FloatingOrbs variant="dark" />
        <div className="bg-ledger-dots pointer-events-none absolute inset-0 opacity-25 [mask-image:radial-gradient(ellipse_at_bottom_left,black,transparent_70%)]" />

        <div className="relative">
          <Logo dark />
        </div>

        <div className="relative animate-fade-in-up">
          <p className="mb-4 font-mono text-xs uppercase tracking-[0.16em] text-accent-400">
            Vol. I — Scholarship Application Registry
          </p>
          <h1 className="max-w-md font-serif text-5xl font-medium leading-[1.08] text-white">
            Launch your scholarship journey from here.
          </h1>
          <p className="mt-6 max-w-sm text-[15px] leading-relaxed text-brand-200">
            The official platform for managing scholarship applications — from submission to final decision.
          </p>
          <Link
            to="/"
            className="link-underline mt-6 inline-flex items-center gap-1 text-sm font-medium text-white"
          >
            Learn about the program <ChevronRight className="size-4" />
          </Link>
        </div>

        <p className="relative font-mono text-[11px] uppercase tracking-[0.1em] text-brand-400">
          ScholarshipHub · Est. {new Date().getFullYear()}
        </p>
      </div>

      <div className="relative flex flex-col items-center justify-center overflow-hidden bg-paper px-6 py-12 sm:px-12">
        <FloatingOrbs variant="light" />
        <div className="relative mb-10 lg:hidden">
          <Logo />
        </div>
        <div className="relative w-full max-w-sm animate-fade-in-up">
          <Outlet />
        </div>
      </div>
    </div>
  )
}
