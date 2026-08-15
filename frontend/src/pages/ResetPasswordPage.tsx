import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { ArrowRight, ChevronLeft, Eye, EyeOff } from 'lucide-react'
import * as authApi from '../api/auth'
import { Button } from '../components/ui/Button'
import { FieldWrapper, Input } from '../components/ui/Field'
import { Alert, extractErrorMessage } from '../components/ui/Alert'

export function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') ?? ''
  const navigate = useNavigate()

  const [newPassword, setNewPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await authApi.resetPassword(token, newPassword)
      navigate('/login', { replace: true })
    } catch (err) {
      setError(extractErrorMessage(err, 'This reset link is invalid or has expired.'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div>
      <h1 className="font-serif text-3xl font-medium text-ink-950">Choose a new password</h1>
      <p className="mt-2 text-sm text-ink-500">Make it something you haven&apos;t used before.</p>

      {!token && (
        <div className="mt-6">
          <Alert variant="error">This reset link is missing its token.</Alert>
        </div>
      )}
      {error && (
        <div className="mt-6">
          <Alert variant="error">{error}</Alert>
        </div>
      )}

      <form onSubmit={handleSubmit} className="mt-8 flex flex-col gap-5">
        <FieldWrapper
          label="New password"
          htmlFor="newPassword"
          hint="At least 8 characters, with an uppercase letter, lowercase letter, digit, and symbol."
        >
          <Input
            id="newPassword"
            type={showPassword ? 'text' : 'password'}
            required
            minLength={8}
            maxLength={72}
            autoComplete="new-password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            placeholder="••••••••"
            endAdornment={
              <button
                type="button"
                tabIndex={-1}
                onClick={() => setShowPassword((v) => !v)}
                className="text-ink-400 hover:text-ink-600"
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? <EyeOff className="size-4" /> : <Eye className="size-4" />}
              </button>
            }
          />
        </FieldWrapper>
        <Button type="submit" loading={submitting} disabled={!token} size="lg" className="w-full">
          <span className="flex w-full items-center justify-center gap-2">
            Reset password <ArrowRight className="size-4" />
          </span>
        </Button>
      </form>

      <Link to="/login" className="link-underline mt-8 flex items-center justify-center gap-1 text-sm font-medium text-ink-400">
        <ChevronLeft className="size-4" /> Back to log in
      </Link>
    </div>
  )
}
