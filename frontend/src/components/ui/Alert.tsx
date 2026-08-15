import type { ReactNode } from 'react'
import { AlertTriangle, CheckCircle2, Info, XCircle } from 'lucide-react'

const VARIANTS = {
  error: {
    classes: 'bg-[#f7e6e6] text-[#7a1f1f] border-[#ad2e2e]/25',
    icon: <XCircle className="size-4.5 shrink-0 text-[#ad2e2e]" />,
  },
  success: {
    classes: 'bg-[#eaf6ea] text-[#0d5c0d] border-[#0ca30c]/25',
    icon: <CheckCircle2 className="size-4.5 shrink-0 text-[#0ca30c]" />,
  },
  info: {
    classes: 'bg-brand-50 text-brand-800 border-brand-600/20',
    icon: <Info className="size-4.5 shrink-0 text-brand-600" />,
  },
  warning: {
    classes: 'bg-[#f8f0dd] text-[#5c3f08] border-[#97640c]/25',
    icon: <AlertTriangle className="size-4.5 shrink-0 text-[#97640c]" />,
  },
}

export function Alert({ variant = 'info', children }: { variant?: keyof typeof VARIANTS; children: ReactNode }) {
  const { classes, icon } = VARIANTS[variant]
  return (
    <div className={`flex items-start gap-2.5 rounded-[3px] border px-4 py-3 text-sm ${classes}`} role="alert">
      {icon}
      <div className="min-w-0">{children}</div>
    </div>
  )
}

export function extractErrorMessage(error: unknown, fallback = 'Something went wrong. Please try again.') {
  if (
    typeof error === 'object' &&
    error !== null &&
    'response' in error &&
    typeof (error as any).response?.data?.message === 'string'
  ) {
    return (error as any).response.data.message as string
  }
  return fallback
}
