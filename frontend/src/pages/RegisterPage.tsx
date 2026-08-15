import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ArrowRight, ChevronLeft, Eye, EyeOff, Mail, Phone } from 'lucide-react'
import * as authApi from '../api/auth'
import { Button } from '../components/ui/Button'
import { FieldWrapper, Input } from '../components/ui/Field'
import { Alert, extractErrorMessage } from '../components/ui/Alert'

export function RegisterPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '', firstName: '', lastName: '', phone: '' })
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  function update<K extends keyof typeof form>(key: K, value: string) {
    setForm((f) => ({ ...f, [key]: value }))
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      const result = await authApi.register(form)
      setSuccessMessage(result.message ?? 'Registration successful. Please check your email to verify your account.')
      setTimeout(() => navigate('/login'), 2500)
    } catch (err) {
      setError(extractErrorMessage(err, 'Registration failed. Please check your details and try again.'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div>
      <h1 className="font-serif text-3xl font-medium text-ink-950">Create your account</h1>
      <p className="mt-2 text-sm text-ink-500">
        Reviewer and administrator accounts are provisioned by an administrator.
      </p>

      {error && (
        <div className="mt-6">
          <Alert variant="error">{error}</Alert>
        </div>
      )}
      {successMessage && (
        <div className="mt-6">
          <Alert variant="success">{successMessage}</Alert>
        </div>
      )}

      <form onSubmit={handleSubmit} className="mt-8 flex flex-col gap-5">
        <div className="grid grid-cols-2 gap-4">
          <FieldWrapper label="First name" htmlFor="firstName">
            <Input
              id="firstName"
              required
              maxLength={100}
              value={form.firstName}
              onChange={(e) => update('firstName', e.target.value)}
            />
          </FieldWrapper>
          <FieldWrapper label="Last name" htmlFor="lastName">
            <Input
              id="lastName"
              required
              maxLength={100}
              value={form.lastName}
              onChange={(e) => update('lastName', e.target.value)}
            />
          </FieldWrapper>
        </div>
        <FieldWrapper label="Email address" htmlFor="email">
          <Input
            id="email"
            type="email"
            required
            maxLength={255}
            icon={<Mail className="size-4" />}
            value={form.email}
            onChange={(e) => update('email', e.target.value)}
            placeholder="you@example.com"
          />
        </FieldWrapper>
        <FieldWrapper label="Phone (optional)" htmlFor="phone">
          <Input
            id="phone"
            maxLength={20}
            pattern="^$|^[+0-9()\-\s]{7,20}$"
            title="Use digits, spaces, and + ( ) - only, 7-20 characters"
            icon={<Phone className="size-4" />}
            value={form.phone}
            onChange={(e) => update('phone', e.target.value)}
          />
        </FieldWrapper>
        <FieldWrapper
          label="Password"
          htmlFor="password"
          hint="At least 8 characters, with an uppercase letter, lowercase letter, digit, and symbol."
        >
          <Input
            id="password"
            type={showPassword ? 'text' : 'password'}
            required
            minLength={8}
            maxLength={72}
            autoComplete="new-password"
            value={form.password}
            onChange={(e) => update('password', e.target.value)}
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
        <Button type="submit" loading={submitting} size="lg" className="mt-2 w-full">
          <span className="flex w-full items-center justify-center gap-2">
            Create account <ArrowRight className="size-4" />
          </span>
        </Button>
      </form>

      <p className="mt-8 text-center text-sm text-ink-500">
        Already have an account?{' '}
        <Link to="/login" className="link-underline font-semibold text-brand-700">
          Log in
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
