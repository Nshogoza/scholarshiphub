import type { ReactNode } from 'react'

interface PageHeaderProps {
  eyebrow: string
  title: string
  action?: ReactNode
}

export function PageHeader({ eyebrow, title, action }: PageHeaderProps) {
  return (
    <div className="mb-8 flex flex-wrap items-end justify-between gap-4">
      <div>
        <p className="mb-2 font-mono text-xs uppercase tracking-[0.14em] text-accent-700">{eyebrow}</p>
        <h1 className="font-serif text-2xl font-medium text-ink-950 sm:text-3xl">{title}</h1>
      </div>
      {action}
    </div>
  )
}
