import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios'

const baseURL = (import.meta.env.VITE_API_BASE_URL as string | undefined) || '/api/v1'

// Access token lives in memory only (never localStorage) to limit exposure
// to XSS; the refresh token lives solely in the httpOnly cookie the backend
// sets, which client-side JS can never read.
let accessToken: string | null = null

export function setAccessToken(token: string | null) {
  accessToken = token
}

export function getAccessToken() {
  return accessToken
}

// Fired when a silent refresh fails, so AuthContext can clear state and
// redirect to /login without client.ts needing to know about React Router.
type LogoutListener = () => void
let onSessionExpired: LogoutListener | null = null
export function registerSessionExpiredHandler(handler: LogoutListener) {
  onSessionExpired = handler
}

export const apiClient = axios.create({
  baseURL,
  withCredentials: true, // sends the httpOnly refresh-token cookie
})

apiClient.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers.set('Authorization', `Bearer ${accessToken}`)
  }
  return config
})

interface RetryableRequestConfig extends InternalAxiosRequestConfig {
  _retried?: boolean
}

let refreshInFlight: Promise<string> | null = null

async function refreshAccessToken(): Promise<string> {
  const response = await axios.post<{ data: { accessToken: string } }>(
    `${baseURL}/auth/refresh`,
    {},
    { withCredentials: true },
  )
  const token = response.data.data.accessToken
  setAccessToken(token)
  return token
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as RetryableRequestConfig | undefined
    const isAuthEndpoint = original?.url?.includes('/auth/login') || original?.url?.includes('/auth/refresh')

    if (error.response?.status === 401 && original && !original._retried && !isAuthEndpoint) {
      original._retried = true
      try {
        refreshInFlight ??= refreshAccessToken().finally(() => {
          refreshInFlight = null
        })
        const token = await refreshInFlight
        original.headers.set('Authorization', `Bearer ${token}`)
        return apiClient(original)
      } catch {
        setAccessToken(null)
        onSessionExpired?.()
      }
    }
    return Promise.reject(error)
  },
)
