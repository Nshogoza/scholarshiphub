import { Loader2 } from 'lucide-react'

export function LoadingSpinner({ label = 'Loading…' }: { label?: string }) {
  return (
    <div className="flex items-center justify-center gap-2.5 py-16 text-sm text-ink-400">
      <Loader2 className="size-4.5 animate-spin text-brand-500" />
      <span>{label}</span>
    </div>
  )
}
