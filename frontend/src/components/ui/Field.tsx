import type {
  InputHTMLAttributes,
  LabelHTMLAttributes,
  ReactNode,
  SelectHTMLAttributes,
  TextareaHTMLAttributes,
} from 'react'
import { ChevronDown } from 'lucide-react'

interface FieldWrapperProps {
  label: string
  htmlFor: string
  error?: string
  hint?: string
  children: ReactNode
}

export function FieldWrapper({ label, htmlFor, error, hint, children }: FieldWrapperProps) {
  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={htmlFor} className="text-[13px] font-medium uppercase tracking-[0.04em] text-ink-500">
        {label}
      </label>
      {children}
      {hint && !error && <span className="text-xs text-ink-400">{hint}</span>}
      {error && <span className="text-xs font-medium text-[#ad2e2e]">{error}</span>}
    </div>
  )
}

export const inputBase =
  'w-full rounded-[3px] border border-ink-300 bg-paper-raised px-3.5 py-2.5 text-sm text-ink-900 placeholder:text-ink-400 transition-colors focus:border-brand-600 focus:outline-none focus:ring-2 focus:ring-brand-600/15 disabled:bg-ink-50 disabled:text-ink-400'

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  icon?: ReactNode
  /** A clickable control anchored to the right edge (e.g. a show/hide password toggle). */
  endAdornment?: ReactNode
}

export function Input({ icon, endAdornment, className, ...props }: InputProps) {
  if (icon || endAdornment) {
    return (
      <div className="relative">
        {icon && (
          <span className="pointer-events-none absolute inset-y-0 left-3.5 flex items-center text-ink-400">
            {icon}
          </span>
        )}
        <input
          {...props}
          className={`${inputBase} ${icon ? 'pl-10' : ''} ${endAdornment ? 'pr-10' : ''} ${className ?? ''}`}
        />
        {endAdornment && (
          <span className="absolute inset-y-0 right-2.5 flex items-center">{endAdornment}</span>
        )}
      </div>
    )
  }
  return <input {...props} className={`${inputBase} ${className ?? ''}`} />
}

export function Textarea(props: TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return <textarea {...props} className={`${inputBase} resize-y ${props.className ?? ''}`} />
}

export function Select({ className, ...props }: SelectHTMLAttributes<HTMLSelectElement>) {
  return (
    <div className="relative">
      <select {...props} className={`${inputBase} cursor-pointer appearance-none pr-9 ${className ?? ''}`} />
      <ChevronDown className="pointer-events-none absolute right-3 top-1/2 size-4 -translate-y-1/2 text-ink-400" />
    </div>
  )
}

export function Label(props: LabelHTMLAttributes<HTMLLabelElement>) {
  return <label {...props} className={`text-sm font-medium text-ink-700 ${props.className ?? ''}`} />
}
