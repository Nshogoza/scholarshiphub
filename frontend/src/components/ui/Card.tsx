import type { HTMLAttributes, ReactNode } from 'react'

interface CardProps extends HTMLAttributes<HTMLDivElement> {
  children: ReactNode
  hoverable?: boolean
  padding?: 'none' | 'sm' | 'md'
  /** Soft color fill -- an accent panel, not a ruled border. */
  tint?: 'none' | 'brand' | 'accent' | 'good' | 'warning' | 'critical'
}

const PADDING = { none: '', sm: 'p-4', md: 'p-6' }

const TINT = {
  none: 'bg-paper-raised border-ink-200',
  brand: 'bg-brand-50 border-brand-100',
  accent: 'bg-accent-50 border-accent-100',
  good: 'bg-[#eaf6ea] border-[#cfe9cf]',
  warning: 'bg-[#f8f0dd] border-[#eddcae]',
  critical: 'bg-[#f7e6e6] border-[#eecccc]',
}

export function Card({ children, hoverable, padding = 'md', tint = 'none', className, ...rest }: CardProps) {
  return (
    <div
      {...rest}
      className={`rounded-[10px] border shadow-[var(--shadow-card)] ${TINT[tint]} ${PADDING[padding]} ${
        hoverable ? 'transition-all duration-200 hover:-translate-y-0.5 hover:shadow-[var(--shadow-raised)]' : ''
      } ${className ?? ''}`}
    >
      {children}
    </div>
  )
}
