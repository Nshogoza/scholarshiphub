import { useRef, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'
import {
  ChevronLeft,
  Download,
  FileText,
  MessageSquare,
  Star,
  Trash2,
  UploadCloud,
} from 'lucide-react'
import {
  deleteApplicationDocument,
  downloadApplicationDocument,
  getApplication,
  submitApplication,
  uploadApplicationDocument,
} from '../api/applications'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { FieldWrapper, Input } from '../components/ui/Field'
import { StatusBadge } from '../components/ui/StatusBadge'
import { ApplicationStepper } from '../components/ui/ApplicationStepper'
import { Avatar } from '../components/ui/Avatar'
import { LoadingSpinner } from '../components/ui/LoadingSpinner'
import { Alert, extractErrorMessage } from '../components/ui/Alert'

export function ApplicationWorkspacePage() {
  const { id } = useParams()
  const applicationId = Number(id)
  const queryClient = useQueryClient()
  const fileInputRef = useRef<HTMLInputElement>(null)

  const MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024
  const ALLOWED_TYPES = new Set([
    'application/pdf',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  ])

  const [documentName, setDocumentName] = useState('')
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [dragActive, setDragActive] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const { data: application, isLoading } = useQuery({
    queryKey: ['application', applicationId],
    queryFn: () => getApplication(applicationId),
  })

  function invalidate() {
    return queryClient.invalidateQueries({ queryKey: ['application', applicationId] })
  }

  const uploadMutation = useMutation({
    mutationFn: (file: File) => uploadApplicationDocument(applicationId, documentName, file),
    onSuccess: async () => {
      setDocumentName('')
      setSelectedFile(null)
      if (fileInputRef.current) fileInputRef.current.value = ''
      await invalidate()
    },
    onError: (err) => setError(extractErrorMessage(err, 'Failed to upload the document.')),
  })

  const deleteMutation = useMutation({
    mutationFn: (documentId: number) => deleteApplicationDocument(applicationId, documentId),
    onSuccess: () => invalidate(),
    onError: (err) => setError(extractErrorMessage(err, 'Failed to remove the document.')),
  })

  const submitMutation = useMutation({
    mutationFn: () => submitApplication(applicationId),
    onSuccess: () => invalidate(),
    onError: (err) => setError(extractErrorMessage(err, 'Failed to submit the application.')),
  })

  if (isLoading) return <LoadingSpinner />
  if (!application) return <Alert variant="error">Application not found.</Alert>

  const editable = application.status === 'DRAFT' || application.status === 'ADDITIONAL_INFO_REQUIRED'

  function handleUpload(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    if (!selectedFile) {
      setError('Choose a file to upload.')
      return
    }
    if (!documentName.trim()) {
      setError('Enter a document name (e.g. Transcript).')
      return
    }
    uploadMutation.mutate(selectedFile)
  }

  function pickFile(file: File | undefined) {
    if (!file) return
    setError(null)
    if (!ALLOWED_TYPES.has(file.type)) {
      setError('Only PDF or DOCX files are accepted.')
      return
    }
    if (file.size > MAX_FILE_SIZE_BYTES) {
      setError('File is too large. The maximum size is 10MB.')
      return
    }
    setSelectedFile(file)
    if (!documentName) setDocumentName(file.name.replace(/\.[^/.]+$/, ''))
  }

  return (
    <div className="mx-auto max-w-3xl">
      <Link
        to="/my-applications"
        className="link-underline mb-6 inline-flex items-center gap-1 text-sm font-medium text-ink-500"
      >
        <ChevronLeft className="size-4" /> Back to my applications
      </Link>

      <Card className="mb-6">
        <div className="mb-6 flex flex-wrap items-center justify-between gap-2">
          <div>
            <p className="mb-1 font-mono text-xs uppercase tracking-[0.08em] text-ink-400">
              Application No. {String(application.id).padStart(4, '0')}
            </p>
            <h1 className="font-serif text-xl font-medium text-ink-950">{application.scholarship.title}</h1>
            <p className="mt-1 text-sm text-ink-500">
              Deadline:{' '}
              {new Date(application.scholarship.applicationDeadline).toLocaleString(undefined, {
                dateStyle: 'medium',
                timeStyle: 'short',
              })}
            </p>
          </div>
          <StatusBadge status={application.status} />
        </div>
        <div className="rounded-[8px] bg-ink-50/60 p-5">
          <ApplicationStepper status={application.status} />
        </div>
      </Card>

      {error && (
        <div className="mb-4">
          <Alert variant="error">{error}</Alert>
        </div>
      )}

      {application.status === 'ADDITIONAL_INFO_REQUIRED' && (
        <div className="mb-4">
          <Alert variant="warning">
            The reviewer requested additional information. Upload the missing documents and resubmit.
          </Alert>
        </div>
      )}

      <Card className="mb-6">
        <h2 className="mb-4 text-[13px] font-medium uppercase tracking-[0.06em] text-ink-500">Supporting documents</h2>

        <ul className="mb-4 flex flex-col gap-2">
          {application.documents.map((doc) => (
            <li
              key={doc.id}
              className="flex items-center justify-between gap-3 border border-ink-200 bg-ink-50/40 px-4 py-3"
            >
              <div className="flex min-w-0 items-center gap-3">
                <FileText className="size-4 shrink-0 text-brand-700" />
                <div className="min-w-0">
                  <p className="truncate text-sm font-medium text-ink-800">{doc.documentName}</p>
                  <p className="truncate font-mono text-[11px] text-ink-400">
                    {doc.originalFilename} · {(doc.fileSizeBytes / 1024).toFixed(0)} KB
                  </p>
                </div>
              </div>
              <div className="flex shrink-0 gap-1">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => downloadApplicationDocument(application.id, doc.id, doc.originalFilename)}
                  icon={<Download className="size-3.5" />}
                >
                  Download
                </Button>
                {editable && (
                  <Button
                    variant="ghost"
                    size="sm"
                    className="hover:bg-[#f7e6e6] hover:text-[#ad2e2e]"
                    onClick={() => deleteMutation.mutate(doc.id)}
                    loading={deleteMutation.isPending}
                    icon={<Trash2 className="size-3.5" />}
                  >
                    Remove
                  </Button>
                )}
              </div>
            </li>
          ))}
          {application.documents.length === 0 && (
            <p className="border border-dashed border-ink-300 py-6 text-center text-sm text-ink-400">
              No documents uploaded yet.
            </p>
          )}
        </ul>

        {editable && (
          <form onSubmit={handleUpload} className="mt-2">
            <div
              onDragOver={(e) => {
                e.preventDefault()
                setDragActive(true)
              }}
              onDragLeave={() => setDragActive(false)}
              onDrop={(e) => {
                e.preventDefault()
                setDragActive(false)
                pickFile(e.dataTransfer.files?.[0])
              }}
              onClick={() => fileInputRef.current?.click()}
              className={`mb-4 flex cursor-pointer flex-col items-center justify-center gap-2 border-2 border-dashed px-6 py-8 text-center transition-colors ${
                dragActive ? 'border-brand-600 bg-brand-50' : 'border-ink-300 hover:border-ink-500 hover:bg-ink-50/50'
              }`}
            >
              <UploadCloud className="size-6 text-brand-700" strokeWidth={1.5} />
              {selectedFile ? (
                <p className="text-sm font-medium text-ink-700">{selectedFile.name}</p>
              ) : (
                <>
                  <p className="text-sm font-medium text-ink-600">
                    <span className="text-brand-700 underline">Click to upload</span> or drag and drop
                  </p>
                  <p className="font-mono text-[11px] text-ink-400">PDF or DOCX, up to 10MB</p>
                </>
              )}
              <input
                id="file"
                type="file"
                ref={fileInputRef}
                accept=".pdf,.docx"
                className="hidden"
                onChange={(e) => pickFile(e.target.files?.[0])}
              />
            </div>

            <div className="flex flex-wrap items-end gap-3">
              <div className="min-w-[200px] flex-1">
                <FieldWrapper label="Document name" htmlFor="documentName">
                  <Input
                    id="documentName"
                    placeholder="e.g. Transcript"
                    maxLength={255}
                    value={documentName}
                    onChange={(e) => setDocumentName(e.target.value)}
                  />
                </FieldWrapper>
              </div>
              <Button type="submit" loading={uploadMutation.isPending}>
                Upload document
              </Button>
            </div>
          </form>
        )}
      </Card>

      {application.reviews.length > 0 && (
        <Card className="mb-6">
          <h2 className="mb-4 flex items-center gap-2 text-[13px] font-medium uppercase tracking-[0.06em] text-ink-500">
            <MessageSquare className="size-4 text-brand-700" /> Review history
          </h2>
          <ul className="flex flex-col gap-4">
            {application.reviews.map((review) => (
              <li key={review.id} className="flex gap-3 border border-ink-200 p-4">
                <Avatar name={review.reviewerName} size="sm" />
                <div className="min-w-0 flex-1">
                  <div className="mb-1 flex flex-wrap items-center justify-between gap-1">
                    <span className="text-sm font-semibold text-ink-800">{review.reviewerName}</span>
                    <span className="font-mono text-[11px] text-ink-400">
                      {new Date(review.createdAt).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })}
                    </span>
                  </div>
                  <div className="mb-2 flex items-center gap-3 text-sm">
                    <span className="font-medium text-ink-700">{review.recommendation.replaceAll('_', ' ')}</span>
                    {review.score != null && (
                      <span className="inline-flex items-center gap-1 text-accent-600">
                        <Star className="size-3.5 fill-current" />
                        <span className="font-medium text-ink-600">{review.score}/100</span>
                      </span>
                    )}
                  </div>
                  {review.comments && <p className="text-sm leading-relaxed text-ink-600">{review.comments}</p>}
                </div>
              </li>
            ))}
          </ul>
        </Card>
      )}

      {editable && (
        <Button onClick={() => submitMutation.mutate()} loading={submitMutation.isPending} size="lg" className="w-full">
          Submit application
        </Button>
      )}
    </div>
  )
}
