import { useEffect, useRef, useState } from 'react'

/** Counts up from 0 to `value` once, on mount/change -- a small "the
 *  numbers are alive" touch for stat tiles. */
export function AnimatedNumber({
  value,
  suffix = '',
  duration = 900,
}: {
  value: number
  suffix?: string
  duration?: number
}) {
  const [display, setDisplay] = useState(0)
  const prefersReduced = useRef(
    typeof window !== 'undefined' && window.matchMedia('(prefers-reduced-motion: reduce)').matches,
  )

  useEffect(() => {
    if (prefersReduced.current) {
      setDisplay(value)
      return
    }
    let raf: number
    let start: number | null = null
    function step(ts: number) {
      if (start === null) start = ts
      const progress = Math.min((ts - start) / duration, 1)
      const eased = 1 - Math.pow(1 - progress, 3)
      setDisplay(Math.round(eased * value))
      if (progress < 1) raf = requestAnimationFrame(step)
    }
    raf = requestAnimationFrame(step)
    return () => cancelAnimationFrame(raf)
  }, [value, duration])

  return (
    <>
      {display.toLocaleString()}
      {suffix}
    </>
  )
}
