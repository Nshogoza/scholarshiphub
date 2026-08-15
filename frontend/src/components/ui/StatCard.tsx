import { AnimatedNumber } from './AnimatedNumber'

interface StatCardProps {
  label: string
  value: number
  suffix?: string
  tint?: 'brand' | 'accent' | 'good' | 'neutral'
}

const TINT_BG = {
  brand: 'bg-brand-50',
  accent: 'bg-accent-50',
  good: 'bg-[#eaf6ea]',
  neutral: 'bg-ink-50',
}
const TINT_TEXT = {
  brand: 'text-brand-700',
  accent: 'text-accent-700',
  good: 'text-[#0d5c0d]',
  neutral: 'text-ink-800',
}

/** A colored report tile: a tinted panel with a serif figure that counts up
 *  into place -- the numbers do the talking, and the color says which kind
 *  of number it is. */
export function StatCard({ label, value, suffix = '', tint = 'neutral' }: StatCardProps) {
  return (
    <div className={`rounded-[10px] p-5 ${TINT_BG[tint]}`}>
      <p className={`font-serif text-[2rem] font-medium leading-none tabular-nums ${TINT_TEXT[tint]}`}>
        <AnimatedNumber value={value} suffix={suffix} />
      </p>
      <p className="mt-2 text-[11px] font-medium uppercase tracking-[0.08em] text-ink-500">{label}</p>
    </div>
  )
}
