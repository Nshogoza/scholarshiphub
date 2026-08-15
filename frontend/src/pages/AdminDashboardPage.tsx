import { useQuery } from '@tanstack/react-query'
import { Activity } from 'lucide-react'
import { getDashboardAnalytics } from '../api/admin'
import { Card } from '../components/ui/Card'
import { StatCard } from '../components/ui/StatCard'
import { StatusBarChart } from '../components/ui/StatusBarChart'
import { LoadingSpinner } from '../components/ui/LoadingSpinner'
import { EmptyState } from '../components/ui/EmptyState'
import { PageHeader } from '../components/ui/PageHeader'

const ACTION_LABELS: Record<string, string> = {
  LOGIN_SUCCESS: 'logged in',
  LOGIN_FAILED: 'failed a login attempt',
  USER_REGISTERED: 'registered a new account',
  EMAIL_VERIFIED: 'verified their email',
  SCHOLARSHIP_CREATED: 'created a scholarship',
  SCHOLARSHIP_STATUS_CHANGED: 'changed a scholarship status',
  APPLICATION_SUBMITTED: 'submitted an application',
  APPLICATION_DOCUMENT_UPLOADED: 'uploaded a document',
  REVIEWER_ASSIGNED: 'assigned a reviewer',
  REVIEW_SUBMITTED: 'submitted a review',
  USER_STATUS_CHANGED: "changed a user's status",
}

export function AdminDashboardPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['dashboardAnalytics'],
    queryFn: getDashboardAnalytics,
    refetchInterval: 30_000,
  })

  if (isLoading || !data) return <LoadingSpinner />

  return (
    <div className="flex flex-col gap-10">
      <PageHeader eyebrow="Administration" title="Dashboard" />

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-5">
        {[
          { label: 'Students', value: data.totalStudents },
          { label: 'Reviewers', value: data.totalReviewers },
          { label: 'Scholarships', value: data.totalScholarships },
          { label: 'Applications', value: data.totalApplications },
          { label: 'Approval rate', value: data.approvalRatePercent, suffix: '%', tint: 'good' as const },
        ].map((stat, i) => (
          <div key={stat.label} className="animate-fade-in-up" style={{ animationDelay: `${i * 60}ms` }}>
            <StatCard {...stat} />
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 gap-10 lg:grid-cols-5">
        <div className="lg:col-span-3">
          <h2 className="mb-5 font-serif text-lg font-medium text-ink-900">Applications by status</h2>
          <Card padding="md">
            <StatusBarChart data={data.applicationsByStatus} />
          </Card>
        </div>

        <div className="lg:col-span-2">
          <h2 className="mb-5 font-serif text-lg font-medium text-ink-900">Recent activity</h2>
          <Card padding="md">
            {data.recentActivity.length === 0 ? (
              <EmptyState icon={Activity} title="No activity yet" />
            ) : (
              <ul className="flex flex-col gap-1">
                {data.recentActivity.map((entry, i) => (
                  <li
                    key={entry.id}
                    className="flex animate-fade-in-up items-start justify-between gap-3 rounded-md px-2 py-2 text-sm transition-colors hover:bg-ink-50"
                    style={{ animationDelay: `${i * 40}ms` }}
                  >
                    <p className="min-w-0 truncate text-ink-600">
                      <span className="font-medium text-ink-900">{entry.actorEmail}</span>{' '}
                      {ACTION_LABELS[entry.action] ?? entry.action.replaceAll('_', ' ').toLowerCase()}
                    </p>
                    <span className="shrink-0 whitespace-nowrap font-mono text-[11px] text-ink-400">
                      {new Date(entry.createdAt).toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </Card>
        </div>
      </div>
    </div>
  )
}
