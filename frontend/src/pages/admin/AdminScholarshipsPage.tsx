import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { Award, PenSquare, Plus, Trash2 } from 'lucide-react'
import { adminListScholarships, deleteScholarship, updateScholarshipStatus } from '../../api/scholarships'
import { Card } from '../../components/ui/Card'
import { Button } from '../../components/ui/Button'
import { StatusBadge } from '../../components/ui/StatusBadge'
import { RowSkeleton } from '../../components/ui/Skeleton'
import { EmptyState } from '../../components/ui/EmptyState'
import { Select } from '../../components/ui/Field'
import { PageHeader } from '../../components/ui/PageHeader'
import { Alert, extractErrorMessage } from '../../components/ui/Alert'
import type { ScholarshipStatus } from '../../types'

const STATUS_OPTIONS: (ScholarshipStatus | '')[] = ['', 'DRAFT', 'PUBLISHED', 'CLOSED', 'ARCHIVED']

const NEXT_STATUS: Record<ScholarshipStatus, ScholarshipStatus[]> = {
  DRAFT: ['PUBLISHED'],
  PUBLISHED: ['CLOSED', 'ARCHIVED'],
  CLOSED: ['PUBLISHED', 'ARCHIVED'],
  ARCHIVED: [],
}

export function AdminScholarshipsPage() {
  const [status, setStatus] = useState<ScholarshipStatus | ''>('')
  const [page, setPage] = useState(0)
  const [error, setError] = useState<string | null>(null)
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['adminScholarships', status, page],
    queryFn: () => adminListScholarships(status, page),
  })

  function invalidate() {
    return queryClient.invalidateQueries({ queryKey: ['adminScholarships'] })
  }

  const statusMutation = useMutation({
    mutationFn: ({ id, next }: { id: number; next: ScholarshipStatus }) => updateScholarshipStatus(id, next),
    onSuccess: () => invalidate(),
    onError: (err) => setError(extractErrorMessage(err)),
  })

  const deleteMutation = useMutation({
    mutationFn: (id: number) => deleteScholarship(id),
    onSuccess: () => invalidate(),
    onError: (err) => setError(extractErrorMessage(err, 'Cannot delete a scholarship that already has applications.')),
  })

  return (
    <div>
      <PageHeader
        eyebrow="Administration"
        title="Scholarships"
        action={
          <div className="flex items-center gap-3">
            <Select
              value={status}
              onChange={(e) => {
                setStatus(e.target.value as ScholarshipStatus | '')
                setPage(0)
              }}
              className="w-auto"
            >
              {STATUS_OPTIONS.map((s) => (
                <option key={s} value={s}>
                  {s === '' ? 'All statuses' : s}
                </option>
              ))}
            </Select>
            <Link to="/admin/scholarships/new">
              <Button icon={<Plus className="size-4" />}>New scholarship</Button>
            </Link>
          </div>
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
        <EmptyState
          icon={Award}
          title="No scholarships yet"
          description="Create your first scholarship to start accepting applications."
          action={
            <Link to="/admin/scholarships/new">
              <Button size="sm">New scholarship</Button>
            </Link>
          }
        />
      )}

      <div className="flex flex-col gap-3">
        {data?.content.map((s, i) => (
          <Card
            key={s.id}
            padding="none"
            className="flex animate-fade-in-up flex-wrap items-center justify-between gap-3 px-5 py-4"
            style={{ animationDelay: `${i * 50}ms` }}
          >
            <div className="min-w-0">
              <div className="mb-1 flex flex-wrap items-center gap-2">
                <h2 className="font-medium text-ink-900">{s.title}</h2>
                <StatusBadge status={s.status} />
              </div>
              <p className="font-mono text-xs text-ink-400">
                ${s.amount.toLocaleString()} · deadline {new Date(s.applicationDeadline).toLocaleDateString()}
              </p>
            </div>
            <div className="flex flex-wrap gap-2">
              <Link to={`/admin/scholarships/${s.id}/edit`}>
                <Button variant="secondary" size="sm" icon={<PenSquare className="size-3.5" />}>
                  Edit
                </Button>
              </Link>
              {NEXT_STATUS[s.status].map((next) => (
                <Button
                  key={next}
                  variant="secondary"
                  size="sm"
                  onClick={() => statusMutation.mutate({ id: s.id, next })}
                  loading={statusMutation.isPending}
                >
                  {next === 'PUBLISHED' ? 'Publish' : next === 'CLOSED' ? 'Close' : 'Archive'}
                </Button>
              ))}
              <Button
                variant="danger"
                size="sm"
                onClick={() => deleteMutation.mutate(s.id)}
                loading={deleteMutation.isPending}
                icon={<Trash2 className="size-3.5" />}
              >
                Delete
              </Button>
            </div>
          </Card>
        ))}
      </div>
    </div>
  )
}
