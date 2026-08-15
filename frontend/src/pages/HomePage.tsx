import { Navigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Button } from '../components/ui/Button'
import { StatusBadge } from '../components/ui/StatusBadge'
import { FloatingOrbs } from '../components/ui/FloatingOrbs'
import { Reveal } from '../components/ui/Reveal'

const REGISTER_LINES = [
  {
    n: '01',
    title: 'Browse open scholarships',
    description: 'Search live listings by deadline, amount, and eligibility -- updated the moment an administrator publishes one.',
  },
  {
    n: '02',
    title: 'Apply with confidence',
    description: 'Save a draft, attach exactly the documents required, and track every status change as it happens.',
  },
  {
    n: '03',
    title: 'Transparent review',
    description: 'Reviewers evaluate with scores and comments you can see the moment a decision is recorded.',
  },
  {
    n: '04',
    title: 'Secure by design',
    description: 'Rotating session tokens, account lockout protection, and a full audit trail on every action taken.',
  },
]

const STEPS = [
  { n: '1', title: 'Register', description: 'Create an account and verify your email in under a minute.' },
  { n: '2', title: 'Apply', description: 'Find a matching scholarship and submit your documents.' },
  { n: '3', title: 'Track', description: 'Follow your application from submission to decision.' },
]

export function HomePage() {
  const { user, isLoading } = useAuth()

  if (isLoading) return null

  if (user) {
    const target = user.role === 'ADMIN' ? '/admin' : user.role === 'REVIEWER' ? '/reviewer' : '/scholarships'
    return <Navigate to={target} replace />
  }

  return (
    <div>
      {/* Masthead */}
      <section className="relative overflow-hidden">
        <FloatingOrbs variant="light" />
        <div className="bg-ledger-dots pointer-events-none absolute inset-0 opacity-40 [mask-image:radial-gradient(ellipse_at_top,black,transparent_70%)]" />
        <div className="relative mx-auto max-w-6xl px-4 py-20 sm:px-6 sm:py-28">
          <div className="grid grid-cols-1 gap-14 lg:grid-cols-12 lg:gap-8">
            <div className="animate-fade-in-up lg:col-span-7">
              <p className="mb-5 font-mono text-xs uppercase tracking-[0.16em] text-accent-700">
                Vol. I — Scholarship Application Registry
              </p>
              <h1 className="max-w-xl font-serif text-[2.75rem] font-medium leading-[1.08] tracking-tight text-ink-950 sm:text-6xl">
                Your next scholarship, properly recorded.
              </h1>
              <p className="mt-6 max-w-lg text-[17px] leading-relaxed text-ink-600">
                ScholarshipHub keeps students, reviewers, and administrators on the same page — from the first
                application to the final decision, with nothing lost in translation.
              </p>
              <div className="mt-9 flex flex-col gap-3 sm:flex-row">
                <Link to="/register">
                  <Button size="lg" className="w-full sm:w-auto">
                    Start your application
                  </Button>
                </Link>
                <Link to="/login">
                  <Button size="lg" variant="secondary" className="w-full sm:w-auto">
                    I already have an account
                  </Button>
                </Link>
              </div>
            </div>

            <div className="animate-fade-in-up lg:col-span-5 lg:pt-2" style={{ animationDelay: '150ms' }}>
              <div className="rounded-[10px] border border-ink-200 bg-paper-raised shadow-[var(--shadow-raised)] transition-transform duration-500 hover:-translate-y-1">
                <div className="flex items-center justify-between px-5 py-3">
                  <span className="font-mono text-[10.5px] uppercase tracking-[0.1em] text-ink-400">
                    Application No. 0412
                  </span>
                  <StatusBadge status="APPROVED" />
                </div>
                <div className="space-y-5 px-5 pb-6">
                  <div>
                    <p className="font-mono text-[10.5px] uppercase tracking-[0.08em] text-ink-400">Scholarship</p>
                    <p className="mt-1 font-serif text-lg text-ink-900">Dean&apos;s Merit Award</p>
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <p className="font-mono text-[10.5px] uppercase tracking-[0.08em] text-ink-400">Award</p>
                      <p className="mt-1 font-serif text-xl text-brand-700">$5,000</p>
                    </div>
                    <div>
                      <p className="font-mono text-[10.5px] uppercase tracking-[0.08em] text-ink-400">Reviewed by</p>
                      <p className="mt-1 text-sm text-ink-700">R. Adeyemi</p>
                    </div>
                  </div>
                  <p className="text-sm italic leading-relaxed text-ink-500">
                    &ldquo;Strong academic record and a clearly articulated statement of purpose.&rdquo;
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Register of features */}
      <section className="mx-auto max-w-6xl px-4 py-20 sm:px-6">
        <Reveal className="mb-12 text-center">
          <p className="mb-2 font-mono text-xs uppercase tracking-[0.14em] text-accent-700">§ 1.0</p>
          <h2 className="font-serif text-2xl font-medium text-ink-950 sm:text-3xl">What the platform keeps track of</h2>
        </Reveal>
        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
          {REGISTER_LINES.map((line, i) => (
            <Reveal key={line.n} delay={i * 90}>
              <div className="group h-full rounded-[10px] border border-ink-200 bg-paper-raised p-7 transition-all duration-300 hover:-translate-y-1 hover:border-accent-300 hover:shadow-[var(--shadow-raised)]">
                <span className="font-serif text-3xl text-ink-300 transition-colors group-hover:text-accent-600">
                  {line.n}
                </span>
                <h3 className="mt-3 font-medium text-ink-900">{line.title}</h3>
                <p className="mt-1.5 text-sm leading-relaxed text-ink-600">{line.description}</p>
              </div>
            </Reveal>
          ))}
        </div>
      </section>

      {/* How it works */}
      <section className="bg-ink-50/60 py-20">
        <div className="mx-auto max-w-5xl px-4 sm:px-6">
          <Reveal>
            <h2 className="mb-14 text-center font-serif text-2xl font-medium text-ink-950 sm:text-3xl">
              Three steps, start to decision
            </h2>
          </Reveal>
          <div className="grid grid-cols-1 gap-12 sm:grid-cols-3">
            {STEPS.map((step, i) => (
              <Reveal key={step.n} delay={i * 120} className="text-center sm:text-left">
                <span className="inline-flex size-11 items-center justify-center rounded-full bg-brand-700 font-serif text-lg font-medium text-white transition-transform duration-300 hover:scale-110">
                  {step.n}
                </span>
                <h3 className="mt-4 font-medium text-ink-900">{step.title}</h3>
                <p className="mt-1.5 text-sm leading-relaxed text-ink-500">{step.description}</p>
              </Reveal>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="mx-auto max-w-6xl px-4 py-20 sm:px-6">
        <Reveal className="relative overflow-hidden rounded-[10px] bg-brand-950 shadow-[var(--shadow-raised)]">
          <FloatingOrbs variant="dark" />
          <div className="relative flex flex-col items-start justify-between gap-8 px-8 py-12 sm:flex-row sm:items-center sm:px-12">
            <div>
              <p className="mb-2 font-mono text-xs uppercase tracking-[0.14em] text-accent-400">Ready when you are</p>
              <h2 className="max-w-md font-serif text-3xl font-medium text-white">Find your scholarship and apply today.</h2>
            </div>
            <Link to="/register" className="shrink-0">
              <Button size="lg" variant="accent">
                Create your account
              </Button>
            </Link>
          </div>
        </Reveal>
      </section>
    </div>
  )
}
