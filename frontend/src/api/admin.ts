import { apiClient } from './client'
import type { ApiResponse, AuditLogEntry, DashboardAnalytics, PageResponse, Role, UserStatus, UserSummary } from '../types'

export async function listUsers(role: Role | '', status: UserStatus | '', search: string, page: number) {
  const { data } = await apiClient.get<ApiResponse<PageResponse<UserSummary>>>('/admin/users', {
    params: { role: role || undefined, status: status || undefined, search: search || undefined, page, size: 20 },
  })
  return data.data
}

export interface CreateUserPayload {
  email: string
  password: string
  firstName: string
  lastName: string
  phone?: string
  role: Role
}

export async function createUser(payload: CreateUserPayload) {
  const { data } = await apiClient.post<ApiResponse<UserSummary>>('/admin/users', payload)
  return data.data
}

export async function updateUserStatus(userId: number, status: UserStatus) {
  const { data } = await apiClient.patch<ApiResponse<UserSummary>>(`/admin/users/${userId}/status`, { status })
  return data.data
}

export async function getDashboardAnalytics() {
  const { data } = await apiClient.get<ApiResponse<DashboardAnalytics>>('/admin/analytics')
  return data.data
}

export async function getAuditLogs(action: string, page: number) {
  const { data } = await apiClient.get<ApiResponse<PageResponse<AuditLogEntry>>>('/admin/audit-logs', {
    params: { action: action || undefined, page, size: 30 },
  })
  return data.data
}
