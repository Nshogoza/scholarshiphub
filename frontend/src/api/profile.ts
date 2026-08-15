import { apiClient } from './client'
import type { ApiResponse, StudentProfile, UserSummary } from '../types'

export interface UpdateProfilePayload {
  firstName: string
  lastName: string
  phone?: string
}

export async function updateProfile(payload: UpdateProfilePayload) {
  const { data } = await apiClient.put<ApiResponse<UserSummary>>('/users/me', payload)
  return data.data
}

export async function getStudentProfile() {
  const { data } = await apiClient.get<ApiResponse<StudentProfile>>('/users/me/student-profile')
  return data.data
}

export interface UpdateStudentProfilePayload {
  educationLevel?: string
  school?: string
  gpa?: number
  personalStatement?: string
}

export async function updateStudentProfile(payload: UpdateStudentProfilePayload) {
  const { data } = await apiClient.put<ApiResponse<StudentProfile>>('/users/me/student-profile', payload)
  return data.data
}
