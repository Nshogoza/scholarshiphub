interface StatusBarChartProps {
  data: Record<string, number>
}

const ORDER: { key: string; label: string; color: string }[] = [
  { key: 'DRAFT', label: 'Draft', color: '#9c9077' },
  { key: 'SUBMITTED', label: 'Submitted', color: '#234a70' },
  { key: 'UNDER_REVIEW', label: 'Under review', color: '#97640c' },
  { key: 'ADDITIONAL_INFO_REQUIRED', label: 'Info requested', color: '#a3440c' },
  { key: 'APPROVED', label: 'Approved', color: '#0ca30c' },
  { key: 'REJECTED', label: 'Rejected', color: '#ad2e2e' },
]

/** Horizontal bar list -- the right form for comparing magnitude across a
 *  handful of named categories. Sharp-edged bars in the reserved status
 *  palette, each row labeled directly (no separate legend needed). */
export function StatusBarChart({ data }: StatusBarChartProps) {
  const max = Math.max(1, ...ORDER.map((o) => data[o.key] ?? 0))

  return (
    <div className="flex flex-col gap-3.5">
      {ORDER.map(({ key, label, color }) => {
        const value = data[key] ?? 0
        const widthPct = (value / max) * 100
        return (
          <div key={key} className="flex items-center gap-3">
            <span className="w-32 shrink-0 truncate text-[13px] text-ink-600">{label}</span>
            <div className="h-2 flex-1 bg-ink-100">
              <div
                className="h-full transition-all duration-500"
                style={{ width: `${Math.max(widthPct, value > 0 ? 2 : 0)}%`, backgroundColor: color }}
              />
            </div>
            <span className="w-6 shrink-0 text-right font-mono text-[13px] font-medium tabular-nums text-ink-800">
              {value}
            </span>
          </div>
        )
      })}
    </div>
  )
}
