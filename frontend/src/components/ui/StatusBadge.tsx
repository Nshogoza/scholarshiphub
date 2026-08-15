/**
 * Reserved status palette (good/warning/serious/critical) applied
 * consistently to every lifecycle status across the app. Rendered as a
 * registry-style marker -- a colored rule + small-caps mono label -- rather
 * than a soft rounded "chip," which reads as a stamped record status
 * instead of a generic UI pill.
 */
const STATUS_MAP: Record<string, { color: string; bg: string }> = {
  DRAFT: { color: '#746b57', bg: '#ece8de' },
  SUBMITTED: { color: '#234a70', bg: '#eef3f8' },
  UNDER_REVIEW: { color: '#97640c', bg: '#f8f0dd' },
  ADDITIONAL_INFO_REQUIRED: { color: '#a3440c', bg: '#f7ece2' },
  APPROVED: { color: '#0d5c0d', bg: '#eaf6ea' },
  REJECTED: { color: '#ad2e2e', bg: '#f7e6e6' },
  PUBLISHED: { color: '#0d5c0d', bg: '#eaf6ea' },
  CLOSED: { color: '#746b57', bg: '#ece8de' },
  ARCHIVED: { color: '#9c9077', bg: '#f7f5f1' },
  ACTIVE: { color: '#0d5c0d', bg: '#eaf6ea' },
  LOCKED: { color: '#97640c', bg: '#f8f0dd' },
  DISABLED: { color: '#ad2e2e', bg: '#f7e6e6' },
}

/** Maps a lifecycle status to the matching <Card tint> value, so list rows
 *  can carry the same color language as their status badge. */
const CARD_TINT_MAP: Record<string, 'good' | 'warning' | 'critical' | 'accent' | 'none'> = {
  UNDER_REVIEW: 'warning',
  ADDITIONAL_INFO_REQUIRED: 'warning',
  APPROVED: 'good',
  PUBLISHED: 'good',
  ACTIVE: 'good',
  REJECTED: 'critical',
  DISABLED: 'critical',
  LOCKED: 'warning',
}

export function statusCardTint(status: string): 'good' | 'warning' | 'critical' | 'accent' | 'none' {
  return CARD_TINT_MAP[status] ?? 'none'
}

export function StatusBadge({ status }: { status: string }) {
  const entry = STATUS_MAP[status] ?? { color: '#746b57', bg: '#ece8de' }
  return (
    <span
      className="inline-flex items-center gap-1.5 border-l-2 py-0.5 pl-2 pr-2.5 font-mono text-[10.5px] font-medium uppercase tracking-[0.08em]"
      style={{ borderColor: entry.color, backgroundColor: entry.bg, color: entry.color }}
    >
      {status.replaceAll('_', ' ')}
    </span>
  )
}
