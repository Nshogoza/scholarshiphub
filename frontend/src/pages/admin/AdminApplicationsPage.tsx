import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { FileStack, UserCog } from 'lucide-react'
import { adminListApplications, assignReviewer } from '../../api/applications'
import { listUsers } from '../../api/admin'
import { Card } from '../../components/ui/Card'
import { Button } from '../../components/ui/Button'
import { StatusBadge } from '../../components/ui/StatusBadge'
import { Avatar } from '../../components/ui/Avatar'
import { Select } from '../../components/ui/Field'
import { PageHeader } from '../../components/ui/PageHeader'
import { RowSkeleton } from '../../components/ui/Skeleton'
import { EmptyState } from '../../components/ui/EmptyState'
import { Alert, extractErrorMessage } from '../../components/ui/Alert'
import type { ApplicationStatus } from '../../types'

const STATUS_OPTIONS: (ApplicationStatus | '')[] = [
  '',
  'SUBMITTED',
  'UNDER_REVIEW',
  'ADDITIONAL_INFO_REQUIRED',
  'APPROVED',
  'REJECTED',
]

export function AdminApplicationsPage() {
  const [status, setStatus] = useState<ApplicationStatus | ''>('')
  const [page, setPage] = useState(0)
  const [error, setError] = useState<string | null>(null)
  const [selectedReviewer, setSelectedReviewer] = useState<Record<number, string>>({})
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['adminApplications', status, page],
    queryFn: () => adminListApplications(status, undefined, page),
  })

  const { data: reviewers } = useQuery({
    queryKey: ['reviewersList'],
    queryFn: () => listUsers('REVIEWER', '', '', 0),
  })

  const assignMutation = useMutation({
    mutationFn: ({ applicationId, reviewerId }: { applicationId: number; reviewerId: number }) =>
      assignReviewer(applicationId, reviewerId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['adminApplications'] }),
    onError: (err) => setError(extractErrorMessage(err, 'Failed to assign reviewer.')),
  })

  return (
    <div>
      <PageHeader
        eyebrow="Administration"
        title="Applications"
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

      {error && <div className="mb-4"><Alert variant="error">{error}</Alert></div>}

      {isLoading && (
        <div className="flex flex-col gap-2">
          {Array.from({ length: 3 }).map((_, i) => (
            <RowSkeleton key={i} />
          ))}
        </div>
      )}

      {data && data.content.length === 0 && (
        <EmptyState icon={FileStack} title="No applications found" description="Try a different filter." />
      )}

      <div className="flex flex-col gap-3">
        {data?.content.map((app, i) => (
          <Card
            key={app.id}
            padding="none"
            className="flex animate-fade-in-up flex-wrap items-center justify-between gap-4 px-5 py-4"
            style={{ animationDelay: `${i * 50}ms` }}
          >
            <div className="flex min-w-0 items-center gap-3">
              <Avatar name={app.studentName} />
              <div className="min-w-0">
                <h2 className="truncate font-medium text-ink-900">{app.scholarshipTitle}</h2>
                <p className="truncate text-xs text-ink-500">
                  {app.studentName}
                  {app.reviewerName && ` · reviewer: ${app.reviewerName}`}
                </p>
              </div>
            </div>
            <div className="flex flex-wrap items-center gap-2">
              <StatusBadge status={app.status} />
              <Select
                value={selectedReviewer[app.id] ?? ''}
                onChange={(e) => setSelectedReviewer((m) => ({ ...m, [app.id]: e.target.value }))}
                className="w-auto py-1.5 text-xs"
              >
                <option value="">Assign reviewer…</option>
                {reviewers?.content.map((r) => (
                  <option key={r.id} value={r.id}>
                    {r.firstName} {r.lastName}
                  </option>
                ))}
              </Select>
              <Button
                variant="secondary"
                size="sm"
                icon={<UserCog className="size-3.5" />}
                disabled={!selectedReviewer[app.id]}
                loading={assignMutation.isPending}
                onClick={() =>
                  assignMutation.mutate({ applicationId: app.id, reviewerId: Number(selectedReviewer[app.id]) })
                }
              >
                Assign
              </Button>
            </div>
          </Card>
        ))}
      </div>
    </div>
  )
}
