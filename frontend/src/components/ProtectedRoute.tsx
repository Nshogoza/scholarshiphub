import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import type { Role } from '../types'
import { LoadingSpinner } from './ui/LoadingSpinner'

interface ProtectedRouteProps {
  allowedRoles?: Role[]
}

/** Gate for authenticated (and optionally role-restricted) routes. Renders
 *  a spinner while the silent-refresh bootstrap in AuthContext resolves, so
 *  a hard page reload never briefly flashes the login page for a valid session. */
export function ProtectedRoute({ allowedRoles }: ProtectedRouteProps) {
  const { user, isLoading } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return <LoadingSpinner label="Checking your session…" />
  }

  if (!user) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/" replace />
  }

  return <Outlet />
}
