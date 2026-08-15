import { useEffect, useState } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import { useNavigate, useParams, Link } from 'react-router-dom'
import { ChevronLeft, FilePlus2, Trash2 } from 'lucide-react'
import { createScholarship, getScholarship, updateScholarship } from '../../api/scholarships'
import { Card } from '../../components/ui/Card'
import { Button } from '../../components/ui/Button'
import { FieldWrapper, Input, Textarea } from '../../components/ui/Field'
import { Alert, extractErrorMessage } from '../../components/ui/Alert'
import { LoadingSpinner } from '../../components/ui/LoadingSpinner'
import type { RequiredDocument } from '../../types'

function toDatetimeLocal(iso: string) {
  const d = new Date(iso)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

export function AdminScholarshipFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const scholarshipId = Number(id)
  const navigate = useNavigate()

  const { data: existing, isLoading } = useQuery({
    queryKey: ['scholarship', scholarshipId],
    queryFn: () => getScholarship(scholarshipId),
    enabled: isEdit,
  })

  const [form, setForm] = useState({
    title: '',
    description: '',
    eligibilityCriteria: '',
    amount: '',
    applicationDeadline: '',
  })
  const [requiredDocuments, setRequiredDocuments] = useState<RequiredDocument[]>([
    { documentName: 'Transcript', mandatory: true },
  ])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (existing) {
      setForm({
        title: existing.title,
        description: existing.description,
        eligibilityCriteria: existing.eligibilityCriteria,
        amount: String(existing.amount),
        applicationDeadline: toDatetimeLocal(existing.applicationDeadline),
      })
      setRequiredDocuments(existing.requiredDocuments)
    }
  }, [existing])

  const mutation = useMutation({
    mutationFn: () => {
      const payload = {
        title: form.title.trim(),
        description: form.description.trim(),
        eligibilityCriteria: form.eligibilityCriteria.trim(),
        amount: Number(form.amount),
        applicationDeadline: new Date(form.applicationDeadline).toISOString(),
        requiredDocuments: requiredDocuments.map((d) => ({ ...d, documentName: d.documentName.trim() })),
      }
      return isEdit ? updateScholarship(scholarshipId, payload) : createScholarship(payload)
    },
    onSuccess: () => navigate('/admin/scholarships'),
    onError: (err) => setError(extractErrorMessage(err, 'Failed to save the scholarship.')),
  })

  function updateDocument(index: number, patch: Partial<RequiredDocument>) {
    setRequiredDocuments((docs) => docs.map((d, i) => (i === index ? { ...d, ...patch } : d)))
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)

    if (!isEdit && new Date(form.applicationDeadline).getTime() <= Date.now()) {
      setError('Application deadline must be in the future.')
      return
    }
    if (requiredDocuments.length === 0) {
      setError('Add at least one required document.')
      return
    }
    if (requiredDocuments.some((d) => !d.documentName.trim())) {
      setError('Every required document needs a name — remove any blank rows.')
      return
    }

    mutation.mutate()
  }

  if (isEdit && isLoading) return <LoadingSpinner />

  return (
    <div className="mx-auto max-w-2xl">
      <Link
        to="/admin/scholarships"
        className="link-underline mb-6 inline-flex items-center gap-1 text-sm font-medium text-ink-500"
      >
        <ChevronLeft className="size-4" /> Back to scholarships
      </Link>
      <Card>
        <p className="mb-1 font-mono text-xs uppercase tracking-[0.14em] text-accent-700">Administration</p>
        <h1 className="mb-6 font-serif text-2xl font-medium text-ink-950">
          {isEdit ? 'Edit scholarship' : 'Create scholarship'}
        </h1>

        {error && <div className="mb-4"><Alert variant="error">{error}</Alert></div>}

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <FieldWrapper label="Title" htmlFor="title">
            <Input
              id="title"
              required
              maxLength={255}
              value={form.title}
              onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))}
            />
          </FieldWrapper>
          <FieldWrapper label="Description" htmlFor="description">
            <Textarea
              id="description"
              rows={4}
              required
              maxLength={20000}
              value={form.description}
              onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
            />
          </FieldWrapper>
          <FieldWrapper label="Eligibility criteria" htmlFor="eligibilityCriteria">
            <Textarea
              id="eligibilityCriteria"
              rows={3}
              required
              maxLength={20000}
              value={form.eligibilityCriteria}
              onChange={(e) => setForm((f) => ({ ...f, eligibilityCriteria: e.target.value }))}
            />
          </FieldWrapper>
          <div className="grid grid-cols-2 gap-4">
            <FieldWrapper label="Amount (USD)" htmlFor="amount">
              <Input
                id="amount"
                type="number"
                min={0.01}
                max={100000000}
                step="0.01"
                required
                value={form.amount}
                onChange={(e) => setForm((f) => ({ ...f, amount: e.target.value }))}
              />
            </FieldWrapper>
            <FieldWrapper label="Application deadline" htmlFor="applicationDeadline">
              <Input
                id="applicationDeadline"
                type="datetime-local"
                required
                min={isEdit ? undefined : toDatetimeLocal(new Date().toISOString())}
                value={form.applicationDeadline}
                onChange={(e) => setForm((f) => ({ ...f, applicationDeadline: e.target.value }))}
              />
            </FieldWrapper>
          </div>

          <div>
            <span className="mb-2 block text-[13px] font-medium uppercase tracking-[0.04em] text-ink-500">
              Required documents
            </span>
            <div className="flex flex-col gap-2 border border-ink-200 bg-ink-50/50 p-3">
              {requiredDocuments.map((doc, index) => (
                <div key={index} className="flex items-center gap-2">
                  <Input
                    value={doc.documentName}
                    onChange={(e) => updateDocument(index, { documentName: e.target.value })}
                    className="flex-1 bg-paper-raised"
                    placeholder="Document name"
                    maxLength={255}
                    required
                  />
                  <label className="flex shrink-0 items-center gap-1.5 text-xs font-medium text-ink-600">
                    <input
                      type="checkbox"
                      checked={doc.mandatory}
                      onChange={(e) => updateDocument(index, { mandatory: e.target.checked })}
                      className="size-3.5 accent-brand-700"
                    />
                    Mandatory
                  </label>
                  <button
                    type="button"
                    onClick={() => setRequiredDocuments((docs) => docs.filter((_, i) => i !== index))}
                    className="flex size-8 shrink-0 items-center justify-center text-ink-400 hover:bg-[#f7e6e6] hover:text-[#ad2e2e]"
                    aria-label="Remove document"
                  >
                    <Trash2 className="size-4" />
                  </button>
                </div>
              ))}
              <Button
                type="button"
                variant="ghost"
                size="sm"
                className="self-start"
                icon={<FilePlus2 className="size-3.5" />}
                onClick={() => setRequiredDocuments((docs) => [...docs, { documentName: '', mandatory: true }])}
              >
                Add document
              </Button>
            </div>
          </div>

          <Button type="submit" loading={mutation.isPending} size="lg" className="self-start">
            {isEdit ? 'Save changes' : 'Create scholarship'}
          </Button>
        </form>
      </Card>
    </div>
  )
}
