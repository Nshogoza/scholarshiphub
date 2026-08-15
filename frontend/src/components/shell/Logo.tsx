import { Link } from 'react-router-dom'

/** A registrar's seal, not an icon-in-a-gradient-square: a double ring in
 *  ink and gold around a serif monogram -- the kind of mark a scholarship
 *  office would actually stamp on a letter. */
function Seal({ size = 32, ink = '#0f2036' }: { size?: number; ink?: string }) {
  return (
    <svg width={size} height={size} viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
      <circle cx="16" cy="16" r="15" stroke="#b8862a" strokeWidth="1.25" fill={ink} />
      <circle cx="16" cy="16" r="12" stroke="#b8862a" strokeWidth="0.75" opacity="0.6" />
      <text
        x="16"
        y="21.5"
        textAnchor="middle"
        fontFamily="'Fraunces Variable', Georgia, serif"
        fontWeight="600"
        fontSize="14"
        fill="#faf7f0"
      >
        S
      </text>
      <path d="M9 9.5 L9 6.5 M23 9.5 L23 6.5" stroke="#b8862a" strokeWidth="1" strokeLinecap="round" />
    </svg>
  )
}

export function Logo({ to = '/', dark = false }: { to?: string; dark?: boolean }) {
  return (
    <Link to={to} className="group flex items-center gap-2.5">
      <span className="inline-block transition-transform duration-300 ease-out group-hover:rotate-[8deg] group-hover:scale-105">
        <Seal />
      </span>
      <span className={`font-serif text-[17px] font-semibold tracking-tight ${dark ? 'text-white' : 'text-ink-900'}`}>
        ScholarshipHub
      </span>
    </Link>
  )
}
