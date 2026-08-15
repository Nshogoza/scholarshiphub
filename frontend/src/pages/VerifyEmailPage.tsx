import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { ArrowRight, ChevronLeft } from 'lucide-react'
import * as authApi from '../api/auth'
import { Button } from '../components/ui/Button'
import { Alert, extractErrorMessage } from '../components/ui/Alert'
import { LoadingSpinner } from '../components/ui/LoadingSpinner'

export function VerifyEmailPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading')
  const [message, setMessage] = useState('')

  useEffect(() => {
    if (!token) {
      setStatus('error')
      setMessage('This verification link is missing its token.')
      return
    }
    authApi
      .verifyEmail(token)
      .then((res) => {
        setStatus('success')
        setMessage(res.message ?? 'Email verified successfully.')
      })
      .catch((err) => {
        setStatus('error')
        setMessage(extractErrorMessage(err, 'This verification link is invalid or has expired.'))
      })
  }, [token])

  return (
    <div>
      <h1 className="font-serif text-3xl font-medium text-ink-950">Email verification</h1>

      <div className="mt-8">
        {status === 'loading' && <LoadingSpinner label="Verifying your email…" />}
        {status === 'success' && (
          <>
            <Alert variant="success">{message}</Alert>
            <Link to="/login" className="mt-6 block">
              <Button size="lg" className="w-full">
                <span className="flex w-full items-center justify-center gap-2">
                  Continue to log in <ArrowRight className="size-4" />
                </span>
              </Button>
            </Link>
          </>
        )}
        {status === 'error' && <Alert variant="error">{message}</Alert>}
      </div>

      <Link to="/" className="link-underline mt-8 flex items-center justify-center gap-1 text-sm font-medium text-ink-400">
        <ChevronLeft className="size-4" /> Back to home page
      </Link>
    </div>
  )
}
