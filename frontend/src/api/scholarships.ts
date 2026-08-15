import { apiClient } from './client'
import type { ApiResponse, PageResponse, RequiredDocument, Scholarship, ScholarshipStatus } from '../types'

export interface ScholarshipFormValues {
  title: string
  description: string
  eligibilityCriteria: string
  amount: number
  applicationDeadline: string
  requiredDocuments: RequiredDocument[]
}

export async function browseScholarships(search: string, page: number) {
  const { data } = await apiClient.get<ApiResponse<PageResponse<Scholarship>>>('/scholarships', {
    params: { search: search || undefined, page, size: 12 },
  })
  return data.data
}

export async function getScholarship(id: number) {
  const { data } = await apiClient.get<ApiResponse<Scholarship>>(`/scholarships/${id}`)
  return data.data
}

export async function adminListScholarships(status: ScholarshipStatus | '', page: number) {
  const { data } = await apiClient.get<ApiResponse<PageResponse<Scholarship>>>('/admin/scholarships', {
    params: { status: status || undefined, page, size: 20 },
  })
  return data.data
}

export async function createScholarship(payload: ScholarshipFormValues) {
  const { data } = await apiClient.post<ApiResponse<Scholarship>>('/admin/scholarships', payload)
  return data.data
}

export async function updateScholarship(id: number, payload: ScholarshipFormValues) {
  const { data } = await apiClient.put<ApiResponse<Scholarship>>(`/admin/scholarships/${id}`, payload)
  return data.data
}

export async function updateScholarshipStatus(id: number, status: ScholarshipStatus) {
  const { data } = await apiClient.patch<ApiResponse<Scholarship>>(`/admin/scholarships/${id}/status`, { status })
  return data.data
}

export async function deleteScholarship(id: number) {
  await apiClient.delete(`/admin/scholarships/${id}`)
}
