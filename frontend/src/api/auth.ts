import { apiClient } from './client'
import type { ApiResponse, AuthResponse, UserSummary } from '../types'

export interface RegisterPayload {
  email: string
  password: string
  firstName: string
  lastName: string
  phone?: string
}

export async function register(payload: RegisterPayload) {
  const { data } = await apiClient.post<ApiResponse<UserSummary>>('/auth/register', payload)
  return data
}

export async function login(email: string, password: string) {
  const { data } = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login', { email, password })
  return data.data
}

export async function logout() {
  await apiClient.post('/auth/logout')
}

export async function verifyEmail(token: string) {
  const { data } = await apiClient.get<ApiResponse<void>>('/auth/verify-email', { params: { token } })
  return data
}

export async function resendVerification(email: string) {
  const { data } = await apiClient.post<ApiResponse<void>>('/auth/resend-verification', { email })
  return data
}

export async function forgotPassword(email: string) {
  const { data } = await apiClient.post<ApiResponse<void>>('/auth/forgot-password', { email })
  return data
}

export async function resetPassword(token: string, newPassword: string) {
  const { data } = await apiClient.post<ApiResponse<void>>('/auth/reset-password', { token, newPassword })
  return data
}

export async function changePassword(currentPassword: string, newPassword: string) {
  const { data } = await apiClient.put<ApiResponse<void>>('/auth/change-password', {
    currentPassword,
    newPassword,
  })
  return data
}

export async function fetchCurrentUser() {
  const { data } = await apiClient.get<ApiResponse<UserSummary>>('/users/me')
  return data.data
}
