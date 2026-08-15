import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import * as authApi from '../api/auth'
import { apiClient, getAccessToken, registerSessionExpiredHandler, setAccessToken } from '../api/client'
import type { UserSummary } from '../types'

interface AuthContextValue {
  user: UserSummary | null
  isLoading: boolean
  login: (email: string, password: string) => Promise<UserSummary>
  logout: () => Promise<void>
  refreshCurrentUser: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserSummary | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const clearSession = useCallback(() => {
    setAccessToken(null)
    setUser(null)
  }, [])

  // On first load there is no access token in memory (a full page reload
  // wipes it), so we attempt a silent refresh using the httpOnly cookie
  // before deciding the user is logged out.
  useEffect(() => {
    registerSessionExpiredHandler(clearSession)

    async function bootstrap() {
      try {
        if (!getAccessToken()) {
          const response = await apiClient.post('/auth/refresh')
          setAccessToken(response.data.data.accessToken)
        }
        const me = await authApi.fetchCurrentUser()
        setUser(me)
      } catch {
        clearSession()
      } finally {
        setIsLoading(false)
      }
    }

    void bootstrap()
  }, [clearSession])

  const login = useCallback(async (email: string, password: string) => {
    const result = await authApi.login(email, password)
    setAccessToken(result.accessToken)
    setUser(result.user)
    return result.user
  }, [])

  const logout = useCallback(async () => {
    try {
      await authApi.logout()
    } finally {
      clearSession()
    }
  }, [clearSession])

  const refreshCurrentUser = useCallback(async () => {
    const me = await authApi.fetchCurrentUser()
    setUser(me)
  }, [])

  const value = useMemo(
    () => ({ user, isLoading, login, logout, refreshCurrentUser }),
    [user, isLoading, login, logout, refreshCurrentUser],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return ctx
}
