const PALETTE = ['#234a70', '#9c6b1e', '#3d3729', '#0d5c0d', '#a3440c', '#1a3a5a']

function hashToIndex(input: string) {
  let hash = 0
  for (let i = 0; i < input.length; i++) hash = (hash * 31 + input.charCodeAt(i)) >>> 0
  return hash % PALETTE.length
}

interface AvatarProps {
  name: string
  size?: 'sm' | 'md' | 'lg'
}

const SIZES = { sm: 'size-7 text-[10.5px]', md: 'size-9 text-[13px]', lg: 'size-14 text-lg' }

/** A stamped monogram, not a circular gradient avatar -- square with a
 *  hairline ring, small-caps serif initials. */
export function Avatar({ name, size = 'md' }: AvatarProps) {
  const initials = name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join('')
  const color = PALETTE[hashToIndex(name)]

  return (
    <span
      className={`inline-flex shrink-0 items-center justify-center rounded-[3px] font-serif font-semibold text-white ${SIZES[size]}`}
      style={{ backgroundColor: color }}
    >
      {initials || '?'}
    </span>
  )
}
