import { Link } from 'react-router-dom'
import { Button } from '../components/ui/Button'

export function NotFoundPage() {
  return (
    <div className="mx-auto max-w-md text-center">
      <p className="font-serif text-6xl text-ink-200">404</p>
      <h1 className="mt-4 font-serif text-2xl font-medium text-ink-950">Page not found</h1>
      <p className="mt-2 text-sm text-ink-500">
        The page you&apos;re looking for doesn&apos;t exist or may have moved.
      </p>
      <Link to="/" className="mt-6 inline-block">
        <Button variant="secondary">Go home</Button>
      </Link>
    </div>
  )
}
