import { Check, X } from 'lucide-react'
import type { ReactNode } from 'react'
import type { ApplicationStatus } from '../../types'

const STEPS: { key: ApplicationStatus; label: string }[] = [
  { key: 'DRAFT', label: 'Draft' },
  { key: 'SUBMITTED', label: 'Submitted' },
  { key: 'UNDER_REVIEW', label: 'Under review' },
  { key: 'APPROVED', label: 'Decision' },
]

const STEP_INDEX: Record<string, number> = {
  DRAFT: 0,
  ADDITIONAL_INFO_REQUIRED: 1,
  SUBMITTED: 1,
  UNDER_REVIEW: 2,
  APPROVED: 3,
  REJECTED: 3,
}

/** A ledger timeline, not a rounded wizard stepper: square serif-numbered
 *  markers joined by a hairline rule. */
export function ApplicationStepper({ status }: { status: ApplicationStatus }) {
  const currentIndex = STEP_INDEX[status] ?? 0
  const isRejected = status === 'REJECTED'
  const isInfoRequested = status === 'ADDITIONAL_INFO_REQUIRED'

  return (
    <div className="flex items-start">
      {STEPS.map((step, i) => {
        const isComplete = i < currentIndex || (i === currentIndex && status === 'APPROVED')
        const isCurrent = i === currentIndex && status !== 'APPROVED'
        const isFinal = i === STEPS.length - 1

        let markerClasses = 'border-ink-300 bg-paper-raised text-ink-400'
        let content: ReactNode = <span className="font-serif text-[13px]">{i + 1}</span>

        if (isFinal && isRejected) {
          markerClasses = 'border-[#ad2e2e] bg-[#f7e6e6] text-[#ad2e2e]'
          content = <X className="size-3.5" />
        } else if (isFinal && status === 'APPROVED') {
          markerClasses = 'border-[#0ca30c] bg-[#0ca30c] text-white'
          content = <Check className="size-3.5" />
        } else if (isComplete) {
          markerClasses = 'border-brand-700 bg-brand-700 text-white'
          content = <Check className="size-3.5" />
        } else if (isCurrent) {
          markerClasses =
            isInfoRequested && i === 1
              ? 'border-[#a3440c] bg-[#f7ece2] text-[#a3440c]'
              : 'border-accent-600 bg-accent-50 text-accent-700'
        }

        return (
          <div key={step.key} className={`flex items-start ${isFinal ? '' : 'flex-1'}`}>
            <div className="flex flex-col items-center gap-2">
              <span
                className={`flex size-7 items-center justify-center border ${markerClasses} ${
                  isFinal && (isRejected || status === 'APPROVED') ? 'animate-pop-in' : ''
                }`}
              >
                {content}
              </span>
              <span className="whitespace-nowrap font-mono text-[10px] uppercase tracking-[0.06em] text-ink-500">
                {isFinal ? (isRejected ? 'Rejected' : status === 'APPROVED' ? 'Approved' : 'Decision') : step.label}
              </span>
            </div>
            {!isFinal && (
              <div className={`mx-2 mt-3.5 h-px flex-1 ${i < currentIndex ? 'bg-brand-700' : 'bg-ink-200'}`} />
            )}
          </div>
        )
      })}
    </div>
  )
}
