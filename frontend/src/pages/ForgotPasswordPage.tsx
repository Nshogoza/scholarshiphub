import { useState } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, ChevronLeft, Mail } from 'lucide-react'
import * as authApi from '../api/auth'
import { Button } from '../components/ui/Button'
import { FieldWrapper, Input } from '../components/ui/Field'
import { Alert, extractErrorMessage } from '../components/ui/Alert'

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      const res = await authApi.forgotPassword(email)
      setMessage(res.message ?? 'If an account exists for that email, a reset link has been sent.')
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div>
      <h1 className="font-serif text-3xl font-medium text-ink-950">Forgot your password?</h1>
      <p className="mt-2 text-sm text-ink-500">
        Enter your email and we&apos;ll send you a link to reset your password.
      </p>

      {error && (
        <div className="mt-6">
          <Alert variant="error">{error}</Alert>
        </div>
      )}
      {message && (
        <div className="mt-6">
          <Alert variant="success">{message}</Alert>
        </div>
      )}

      <form onSubmit={handleSubmit} className="mt-8 flex flex-col gap-5">
        <FieldWrapper label="Email address" htmlFor="email">
          <Input
            id="email"
            type="email"
            required
            maxLength={255}
            icon={<Mail className="size-4" />}
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="you@example.com"
          />
        </FieldWrapper>
        <Button type="submit" loading={submitting} size="lg" className="w-full">
          <span className="flex w-full items-center justify-center gap-2">
            Send reset link <ArrowRight className="size-4" />
          </span>
        </Button>
      </form>

      <Link to="/login" className="link-underline mt-8 flex items-center justify-center gap-1 text-sm font-medium text-ink-400">
        <ChevronLeft className="size-4" /> Back to log in
      </Link>
    </div>
  )
}
