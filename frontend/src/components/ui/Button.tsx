import type { ButtonHTMLAttributes, ReactNode } from 'react'
import { Loader2 } from 'lucide-react'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'accent' | 'secondary' | 'ghost' | 'danger'
  size?: 'sm' | 'md' | 'lg'
  loading?: boolean
  icon?: ReactNode
}

const VARIANTS = {
  primary: 'bg-brand-700 text-white hover:bg-brand-800 active:bg-brand-900 disabled:bg-brand-300',
  accent: 'bg-accent-600 text-white hover:bg-accent-700 active:bg-accent-800 disabled:bg-accent-300',
  secondary:
    'bg-transparent text-ink-800 border border-ink-300 hover:border-ink-800 hover:bg-ink-50 disabled:text-ink-300 disabled:border-ink-200',
  ghost: 'bg-transparent text-ink-600 hover:bg-ink-100 disabled:text-ink-300',
  danger:
    'bg-transparent text-[#ad2e2e] border border-[#ad2e2e]/30 hover:border-[#ad2e2e] hover:bg-[#f7e6e6] disabled:text-ink-300 disabled:border-ink-200',
}

const SIZES = {
  sm: 'px-3 py-1.5 text-xs gap-1.5',
  md: 'px-4 py-2 text-[13.5px] gap-2',
  lg: 'px-5 py-2.5 text-sm gap-2',
}

export function Button({
  variant = 'primary',
  size = 'md',
  loading,
  icon,
  disabled,
  children,
  className,
  ...rest
}: ButtonProps) {
  const shine = variant === 'primary' || variant === 'accent' ? 'btn-shine' : ''

  return (
    <button
      {...rest}
      disabled={disabled || loading}
      className={`inline-flex shrink-0 items-center justify-center rounded-[3px] font-medium tracking-[0.01em] transition-colors duration-150 disabled:cursor-not-allowed active:scale-[0.98] ${shine} ${VARIANTS[variant]} ${SIZES[size]} ${className ?? ''}`}
    >
      {loading ? <Loader2 className="size-4 animate-spin" /> : icon}
      {children}
    </button>
  )
}
