import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { ChevronRight, GraduationCap } from 'lucide-react'
import { listMyApplications } from '../api/applications'
import { Card } from '../components/ui/Card'
import { StatusBadge } from '../components/ui/StatusBadge'
import { RowSkeleton } from '../components/ui/Skeleton'
import { EmptyState } from '../components/ui/EmptyState'
import { Select } from '../components/ui/Field'
import { Button } from '../components/ui/Button'
import { PageHeader } from '../components/ui/PageHeader'
import type { ApplicationStatus } from '../types'

const STATUS_OPTIONS: (ApplicationStatus | '')[] = [
  '',
  'DRAFT',
  'SUBMITTED',
  'UNDER_REVIEW',
  'ADDITIONAL_INFO_REQUIRED',
  'APPROVED',
  'REJECTED',
]

export function MyApplicationsPage() {
  const [status, setStatus] = useState<ApplicationStatus | ''>('')
  const [page, setPage] = useState(0)

  const { data, isLoading } = useQuery({
    queryKey: ['myApplications', status, page],
    queryFn: () => listMyApplications(status, page),
  })

  return (
    <div>
      <PageHeader
        eyebrow="Register"
        title="My applications"
        action={
          <Select
            value={status}
            onChange={(e) => {
              setStatus(e.target.value as ApplicationStatus | '')
              setPage(0)
            }}
            className="w-auto"
          >
            {STATUS_OPTIONS.map((s) => (
              <option key={s} value={s}>
                {s === '' ? 'All statuses' : s.replaceAll('_', ' ')}
              </option>
            ))}
          </Select>
        }
      />

      {isLoading && (
        <div className="flex flex-col gap-2">
          {Array.from({ length: 3 }).map((_, i) => (
            <RowSkeleton key={i} />
          ))}
        </div>
      )}

      {data && data.content.length === 0 && (
        <EmptyState
          icon={GraduationCap}
          title="No applications yet"
          description="Browse open scholarships and start your first application."
          action={
            <Link to="/scholarships">
              <Button size="sm">Browse scholarships</Button>
            </Link>
          }
        />
      )}

      <div className="flex flex-col gap-3">
        {data?.content.map((app, i) => (
          <Link key={app.id} to={`/my-applications/${app.id}`} className="animate-fade-in-up block" style={{ animationDelay: `${i * 50}ms` }}>
            <Card hoverable padding="none" className="flex items-center justify-between gap-4 px-5 py-4">
              <div className="min-w-0">
                <h2 className="truncate font-medium text-ink-900">{app.scholarshipTitle}</h2>
                <p className="font-mono text-xs text-ink-400">
                  Updated {new Date(app.updatedAt).toLocaleDateString(undefined, { dateStyle: 'medium' })}
                </p>
              </div>
              <div className="flex shrink-0 items-center gap-3">
                <StatusBadge status={app.status} />
                <ChevronRight className="size-4 text-ink-300" />
              </div>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  )
}
