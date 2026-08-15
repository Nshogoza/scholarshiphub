import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { ChevronLeft, ChevronRight, Search } from 'lucide-react'
import { browseScholarships } from '../api/scholarships'
import { Card } from '../components/ui/Card'
import { Input } from '../components/ui/Field'
import { Button } from '../components/ui/Button'
import { CardSkeleton } from '../components/ui/Skeleton'
import { EmptyState } from '../components/ui/EmptyState'
import { PageHeader } from '../components/ui/PageHeader'
import { Alert } from '../components/ui/Alert'

function daysRemaining(deadline: string) {
  const diff = new Date(deadline).getTime() - Date.now()
  return Math.ceil(diff / (1000 * 60 * 60 * 24))
}

function urgency(days: number) {
  const label = days > 0 ? `${days}d left` : 'closing soon'
  return days <= 14 ? { label, urgent: true } : { label, urgent: false }
}

export function ScholarshipListingPage() {
  const [search, setSearch] = useState('')
  const [appliedSearch, setAppliedSearch] = useState('')
  const [page, setPage] = useState(0)

  const { data, isLoading, isError } = useQuery({
    queryKey: ['scholarships', appliedSearch, page],
    queryFn: () => browseScholarships(appliedSearch, page),
  })

  return (
    <div>
      <PageHeader eyebrow="Register" title="Open scholarships" />

      <form
        onSubmit={(e) => {
          e.preventDefault()
          setPage(0)
          setAppliedSearch(search)
        }}
        className="mb-8 flex max-w-md gap-2"
      >
        <Input
          placeholder="Search by title…"
          icon={<Search className="size-4" />}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <Button type="submit" variant="secondary">
          Search
        </Button>
      </form>

      {isError && <Alert variant="error">Failed to load scholarships.</Alert>}

      {isLoading && (
        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <CardSkeleton key={i} />
          ))}
        </div>
      )}

      {data && data.content.length === 0 && (
        <EmptyState
          icon={Search}
          title="No scholarships found"
          description="Try a different search term, or check back later for new opportunities."
        />
      )}

      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
        {data?.content.map((s, i) => {
          const days = daysRemaining(s.applicationDeadline)
          const u = urgency(days)
          return (
            <Link key={s.id} to={`/scholarships/${s.id}`} className="group animate-fade-in-up" style={{ animationDelay: `${i * 50}ms` }}>
              <Card hoverable className="flex h-full flex-col justify-between">
                <div>
                  <div className="mb-3 flex items-start justify-between gap-2">
                    <span
                      className={`rounded-[4px] px-2 py-0.5 font-mono text-[10.5px] uppercase tracking-[0.06em] ${
                        u.urgent ? 'bg-accent-50 text-accent-700' : 'text-ink-400'
                      }`}
                    >
                      {u.label}
                    </span>
                    <span className="font-serif text-lg text-brand-700">${s.amount.toLocaleString()}</span>
                  </div>
                  <h2 className="mb-1.5 font-serif text-lg leading-snug text-ink-950 group-hover:underline">
                    {s.title}
                  </h2>
                  <p className="line-clamp-3 text-sm leading-relaxed text-ink-500">{s.description}</p>
                </div>
                <span className="mt-5 inline-flex items-center gap-1 text-[13px] font-medium text-brand-700">
                  View details
                  <ChevronRight className="size-3.5 transition-transform group-hover:translate-x-0.5" />
                </span>
              </Card>
            </Link>
          )
        })}
      </div>

      {data && data.totalPages > 1 && (
        <div className="mt-8 flex items-center justify-center gap-3">
          <Button variant="secondary" size="sm" disabled={data.first} onClick={() => setPage((p) => p - 1)} icon={<ChevronLeft className="size-4" />}>
            Previous
          </Button>
          <span className="font-mono text-xs text-ink-500">
            Page {data.page + 1} of {data.totalPages}
          </span>
          <Button variant="secondary" size="sm" disabled={data.last} onClick={() => setPage((p) => p + 1)}>
            <span className="flex items-center gap-2">
              Next <ChevronRight className="size-4" />
            </span>
          </Button>
        </div>
      )}
    </div>
  )
}
