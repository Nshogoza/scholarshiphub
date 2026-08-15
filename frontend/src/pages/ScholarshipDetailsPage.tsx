import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ChevronLeft } from 'lucide-react'
import { getScholarship } from '../api/scholarships'
import { createApplication } from '../api/applications'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { LoadingSpinner } from '../components/ui/LoadingSpinner'
import { Alert, extractErrorMessage } from '../components/ui/Alert'
import { useAuth } from '../context/AuthContext'

export function ScholarshipDetailsPage() {
  const { id } = useParams()
  const scholarshipId = Number(id)
  const navigate = useNavigate()
  const { user } = useAuth()
  const queryClient = useQueryClient()
  const [error, setError] = useState<string | null>(null)

  const { data: scholarship, isLoading } = useQuery({
    queryKey: ['scholarship', scholarshipId],
    queryFn: () => getScholarship(scholarshipId),
  })

  const applyMutation = useMutation({
    mutationFn: () => createApplication(scholarshipId),
    onSuccess: (application) => {
      void queryClient.invalidateQueries({ queryKey: ['myApplications'] })
      navigate(`/my-applications/${application.id}`)
    },
    onError: (err) => setError(extractErrorMessage(err, 'Could not start an application for this scholarship.')),
  })

  if (isLoading) return <LoadingSpinner />
  if (!scholarship) return <Alert variant="error">Scholarship not found.</Alert>

  const deadlinePassed = new Date(scholarship.applicationDeadline).getTime() < Date.now()

  return (
    <div className="mx-auto max-w-3xl">
      <Link to="/scholarships" className="link-underline mb-6 inline-flex items-center gap-1 text-sm font-medium text-ink-500">
        <ChevronLeft className="size-4" /> Back to scholarships
      </Link>

      <Card className="mb-6">
        <div className="flex items-start justify-between gap-4">
          <div>
            <p className="mb-1 font-mono text-xs uppercase tracking-[0.1em] text-ink-400">
              Posted by {scholarship.createdByName}
            </p>
            <h1 className="font-serif text-2xl font-medium leading-tight text-ink-950 sm:text-3xl">
              {scholarship.title}
            </h1>
          </div>
          <div className="shrink-0 text-right">
            <p className="font-serif text-2xl text-brand-700">${scholarship.amount.toLocaleString()}</p>
            <p className="font-mono text-[10.5px] uppercase tracking-[0.08em] text-ink-400">award</p>
          </div>
        </div>
      </Card>

      <Card className="mb-6">
        <h3 className="mb-2 text-[13px] font-medium uppercase tracking-[0.06em] text-ink-500">About this scholarship</h3>
        <p className="whitespace-pre-line text-sm leading-relaxed text-ink-700">{scholarship.description}</p>
      </Card>

      <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Card>
          <h3 className="mb-2 text-[13px] font-medium uppercase tracking-[0.06em] text-ink-500">Eligibility</h3>
          <p className="text-sm leading-relaxed text-ink-700">{scholarship.eligibilityCriteria}</p>
        </Card>
        <Card>
          <h3 className="mb-2 text-[13px] font-medium uppercase tracking-[0.06em] text-ink-500">Application deadline</h3>
          <p className={`text-sm font-medium ${deadlinePassed ? 'text-[#ad2e2e]' : 'text-ink-700'}`}>
            {new Date(scholarship.applicationDeadline).toLocaleString(undefined, {
              dateStyle: 'medium',
              timeStyle: 'short',
            })}
            {deadlinePassed && ' — closed'}
          </p>
        </Card>
      </div>

      <Card className="mb-6">
        <h3 className="mb-3 text-[13px] font-medium uppercase tracking-[0.06em] text-ink-500">Required documents</h3>
        <ul className="flex flex-wrap gap-2">
          {scholarship.requiredDocuments.map((doc) => (
            <li
              key={doc.documentName}
              className="rounded-full bg-ink-100 px-3 py-1.5 text-sm font-medium text-ink-700"
            >
              {doc.documentName}
              {!doc.mandatory && <span className="ml-1 font-normal text-ink-400">(optional)</span>}
            </li>
          ))}
        </ul>
      </Card>

      {error && (
        <div className="mb-4">
          <Alert variant="error">{error}</Alert>
        </div>
      )}

      {user?.role === 'STUDENT' && (
        <Button
          size="lg"
          onClick={() => applyMutation.mutate()}
          loading={applyMutation.isPending}
          disabled={deadlinePassed}
          className="w-full sm:w-auto"
        >
          {deadlinePassed ? 'Applications closed' : 'Start application'}
        </Button>
      )}
    </div>
  )
}
