import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate, useParams, Link } from 'react-router-dom'
import {
  CheckCircle2,
  ChevronLeft,
  Download,
  FileText,
  HelpCircle,
  Star,
  XCircle,
} from 'lucide-react'
import { downloadApplicationDocument, getAssignedApplication, submitReview } from '../api/applications'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { Avatar } from '../components/ui/Avatar'
import { FieldWrapper, Input, Textarea } from '../components/ui/Field'
import { StatusBadge } from '../components/ui/StatusBadge'
import { LoadingSpinner } from '../components/ui/LoadingSpinner'
import { Alert, extractErrorMessage } from '../components/ui/Alert'
import type { ReviewRecommendation } from '../types'

const RECOMMENDATIONS: { value: ReviewRecommendation; label: string; icon: typeof CheckCircle2; classes: string }[] = [
  { value: 'APPROVE', label: 'Approve', icon: CheckCircle2, classes: 'peer-checked:border-[#0ca30c] peer-checked:bg-[#eaf6ea] peer-checked:text-[#0d5c0d]' },
  { value: 'REQUEST_ADDITIONAL_INFO', label: 'Request info', icon: HelpCircle, classes: 'peer-checked:border-[#a3440c] peer-checked:bg-[#f7ece2] peer-checked:text-[#a3440c]' },
  { value: 'REJECT', label: 'Reject', icon: XCircle, classes: 'peer-checked:border-[#ad2e2e] peer-checked:bg-[#f7e6e6] peer-checked:text-[#ad2e2e]' },
]

export function ReviewApplicationPage() {
  const { id } = useParams()
  const applicationId = Number(id)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const { data: application, isLoading } = useQuery({
    queryKey: ['assignedApplication', applicationId],
    queryFn: () => getAssignedApplication(applicationId),
  })

  const [score, setScore] = useState('')
  const [comments, setComments] = useState('')
  const [recommendation, setRecommendation] = useState<ReviewRecommendation>('APPROVE')
  const [error, setError] = useState<string | null>(null)

  const reviewMutation = useMutation({
    mutationFn: () =>
      submitReview(applicationId, {
        score: score ? Number(score) : undefined,
        comments: comments || undefined,
        recommendation,
      }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['assignedApplications'] })
      navigate('/reviewer')
    },
    onError: (err) => setError(extractErrorMessage(err, 'Failed to submit the review.')),
  })

  if (isLoading) return <LoadingSpinner />
  if (!application) return <Alert variant="error">Application not found.</Alert>

  const canReview = application.status === 'UNDER_REVIEW'

  return (
    <div className="mx-auto flex max-w-3xl flex-col gap-6">
      <Link to="/reviewer" className="link-underline inline-flex items-center gap-1 text-sm font-medium text-ink-500">
        <ChevronLeft className="size-4" /> Back to assigned applications
      </Link>

      <Card>
        <div className="mb-3 flex items-start justify-between gap-3">
          <div className="flex items-center gap-3">
            <Avatar name={application.studentName} size="lg" />
            <div>
              <h1 className="font-serif text-lg font-medium text-ink-950">{application.scholarship.title}</h1>
              <p className="text-sm text-ink-500">Applicant: {application.studentName}</p>
            </div>
          </div>
          <StatusBadge status={application.status} />
        </div>
        <p className="mt-3 text-sm leading-relaxed text-ink-600">{application.scholarship.description}</p>
        <p className="mt-3 rounded-[8px] bg-ink-50 p-3 text-sm text-ink-700">
          <strong className="text-ink-800">Eligibility: </strong>
          {application.scholarship.eligibilityCriteria}
        </p>
      </Card>

      <Card>
        <h2 className="mb-4 text-[13px] font-medium uppercase tracking-[0.06em] text-ink-500">Submitted documents</h2>
        <ul className="flex flex-col gap-2">
          {application.documents.map((doc) => (
            <li
              key={doc.id}
              className="flex items-center justify-between gap-3 border border-ink-200 bg-ink-50/40 px-4 py-3"
            >
              <div className="flex min-w-0 items-center gap-3">
                <FileText className="size-4 shrink-0 text-brand-700" />
                <span className="truncate text-sm font-medium text-ink-800">{doc.documentName}</span>
              </div>
              <Button
                variant="ghost"
                size="sm"
                icon={<Download className="size-3.5" />}
                onClick={() => downloadApplicationDocument(application.id, doc.id, doc.originalFilename)}
              >
                Download
              </Button>
            </li>
          ))}
          {application.documents.length === 0 && (
            <p className="border border-dashed border-ink-300 py-6 text-center text-sm text-ink-400">
              No documents were submitted.
            </p>
          )}
        </ul>
      </Card>

      {application.reviews.length > 0 && (
        <Card>
          <h2 className="mb-4 text-[13px] font-medium uppercase tracking-[0.06em] text-ink-500">Previous reviews</h2>
          <ul className="flex flex-col gap-3">
            {application.reviews.map((review) => (
              <li key={review.id} className="border border-ink-200 p-4 text-sm">
                <div className="mb-1 flex items-center gap-3">
                  <span className="font-semibold text-ink-800">{review.recommendation.replaceAll('_', ' ')}</span>
                  {review.score != null && (
                    <span className="inline-flex items-center gap-1 text-accent-600">
                      <Star className="size-3.5 fill-current" />
                      <span className="font-medium text-ink-600">{review.score}/100</span>
                    </span>
                  )}
                </div>
                {review.comments && <p className="text-ink-600">{review.comments}</p>}
              </li>
            ))}
          </ul>
        </Card>
      )}

      {canReview ? (
        <Card>
          <h2 className="mb-4 text-[13px] font-medium uppercase tracking-[0.06em] text-ink-500">Record your decision</h2>
          {error && <div className="mb-4"><Alert variant="error">{error}</Alert></div>}
          <form
            onSubmit={(e) => {
              e.preventDefault()
              setError(null)
              reviewMutation.mutate()
            }}
            className="flex flex-col gap-5"
          >
            <div>
              <span className="mb-2 block text-[13px] font-medium uppercase tracking-[0.04em] text-ink-500">
                Recommendation
              </span>
              <div className="grid grid-cols-3 gap-3">
                {RECOMMENDATIONS.map(({ value, label, icon: Icon, classes }) => (
                  <label key={value} className="relative cursor-pointer">
                    <input
                      type="radio"
                      name="recommendation"
                      value={value}
                      checked={recommendation === value}
                      onChange={() => setRecommendation(value)}
                      className="peer sr-only"
                    />
                    <span
                      className={`flex flex-col items-center gap-1.5 border-2 border-ink-200 px-2 py-3 text-center text-xs font-medium text-ink-500 transition-colors hover:border-ink-400 ${classes}`}
                    >
                      <Icon className="size-5" />
                      {label}
                    </span>
                  </label>
                ))}
              </div>
            </div>
            <FieldWrapper label="Score (0-100, optional)" htmlFor="score">
              <Input id="score" type="number" min={0} max={100} value={score} onChange={(e) => setScore(e.target.value)} />
            </FieldWrapper>
            <FieldWrapper label="Comments" htmlFor="comments">
              <Textarea
                id="comments"
                rows={4}
                maxLength={5000}
                value={comments}
                onChange={(e) => setComments(e.target.value)}
              />
            </FieldWrapper>
            <Button type="submit" loading={reviewMutation.isPending} size="lg" className="self-start">
              Submit review
            </Button>
          </form>
        </Card>
      ) : (
        <Alert variant="info">
          This application is not currently awaiting your review (status: {application.status.replaceAll('_', ' ')}).
        </Alert>
      )}
    </div>
  )
}
