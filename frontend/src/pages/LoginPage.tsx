import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { ArrowRight, ChevronLeft, Eye, EyeOff, Mail } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { Button } from '../components/ui/Button'
import { FieldWrapper, Input } from '../components/ui/Field'
import { Alert, extractErrorMessage } from '../components/ui/Alert'

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation() as { state?: { from?: Location } }

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      const user = await login(email, password)
      const redirectTo =
        location.state?.from?.pathname ??
        (user.role === 'ADMIN' ? '/admin' : user.role === 'REVIEWER' ? '/reviewer' : '/scholarships')
      navigate(redirectTo, { replace: true })
    } catch (err) {
      setError(extractErrorMessage(err, 'Invalid email or password.'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div>
      <h1 className="font-serif text-3xl font-medium text-ink-950">Welcome back</h1>
      <p className="mt-2 text-sm text-ink-500">Sign in with your ScholarshipHub credentials.</p>

      {error && (
        <div className="mt-6">
          <Alert variant="error">{error}</Alert>
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
            autoComplete="email"
            placeholder="you@example.com"
          />
        </FieldWrapper>

        <div>
          <div className="mb-1.5 flex items-center justify-between">
            <label htmlFor="password" className="text-[13px] font-medium uppercase tracking-[0.04em] text-ink-500">
              Password
            </label>
            <Link to="/forgot-password" className="link-underline text-xs font-medium text-brand-700">
              Forgot password?
            </Link>
          </div>
          <Input
            id="password"
            type={showPassword ? 'text' : 'password'}
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
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
        </div>

        <Button type="submit" loading={submitting} size="lg" className="w-full">
          <span className="flex w-full items-center justify-center gap-2">
            Sign in <ArrowRight className="size-4" />
          </span>
        </Button>
      </form>

      <p className="mt-8 text-center text-sm text-ink-500">
        Don&apos;t have an account?{' '}
        <Link to="/register" className="link-underline font-semibold text-brand-700">
          Register
        </Link>
      </p>

      <Link
        to="/"
        className="link-underline mt-6 flex items-center justify-center gap-1 text-sm font-medium text-ink-400"
      >
        <ChevronLeft className="size-4" /> Back to home page
      </Link>
    </div>
  )
}
