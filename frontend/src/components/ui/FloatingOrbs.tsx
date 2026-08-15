/**
 * Slow-drifting blurred circles kept to the site's own navy/gold palette at
 * low opacity -- ambient movement behind a hero or CTA panel, not a
 * decorative gradient blob imported from nowhere in particular.
 */
export function FloatingOrbs({ variant = 'light' }: { variant?: 'light' | 'dark' }) {
  const colors =
    variant === 'light'
      ? { a: '#234a70', b: '#b8862a', c: '#0ca30c' }
      : { a: '#3c6288', b: '#dbb968', c: '#5d84a8' }
  const opacity = variant === 'light' ? 0.1 : 0.22

  return (
    <div className="pointer-events-none absolute inset-0 overflow-hidden">
      <span
        className="orb orb-drift-a -left-10 -top-16 size-72"
        style={{ backgroundColor: colors.a, opacity }}
      />
      <span
        className="orb orb-drift-b right-[-4rem] top-1/4 size-56"
        style={{ backgroundColor: colors.b, opacity: opacity * 0.9 }}
      />
      <span
        className="orb orb-drift-c bottom-[-3rem] left-1/3 size-64"
        style={{ backgroundColor: colors.c, opacity: opacity * 0.7 }}
      />
    </div>
  )
}
