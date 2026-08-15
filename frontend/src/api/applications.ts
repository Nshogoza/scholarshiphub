import { apiClient } from './client'
import type {
  ApiResponse,
  ApplicationDetail,
  ApplicationDocument,
  ApplicationStatus,
  ApplicationSummary,
  PageResponse,
  Review,
  ReviewRecommendation,
} from '../types'

export async function createApplication(scholarshipId: number) {
  const { data } = await apiClient.post<ApiResponse<ApplicationDetail>>('/applications', { scholarshipId })
  return data.data
}

export async function listMyApplications(status: ApplicationStatus | '', page: number) {
  const { data } = await apiClient.get<ApiResponse<PageResponse<ApplicationSummary>>>('/applications/me', {
    params: { status: status || undefined, page, size: 20 },
  })
  return data.data
}

export async function getApplication(id: number) {
  const { data } = await apiClient.get<ApiResponse<ApplicationDetail>>(`/applications/${id}`)
  return data.data
}

export async function uploadApplicationDocument(id: number, documentName: string, file: File) {
  const formData = new FormData()
  formData.append('documentName', documentName)
  formData.append('file', file)
  const { data } = await apiClient.post<ApiResponse<ApplicationDocument>>(
    `/applications/${id}/documents`,
    formData,
  )
  return data.data
}

export async function deleteApplicationDocument(id: number, documentId: number) {
  await apiClient.delete(`/applications/${id}/documents/${documentId}`)
}

export async function downloadApplicationDocument(id: number, documentId: number, filename: string) {
  const response = await apiClient.get(`/applications/${id}/documents/${documentId}/download`, {
    responseType: 'blob',
  })
  const url = window.URL.createObjectURL(response.data as Blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}

export async function submitApplication(id: number) {
  const { data } = await apiClient.post<ApiResponse<ApplicationDetail>>(`/applications/${id}/submit`)
  return data.data
}

// --- Reviewer ---

export async function listAssignedApplications(status: ApplicationStatus | '', page: number) {
  const { data } = await apiClient.get<ApiResponse<PageResponse<ApplicationSummary>>>(
    '/reviewer/applications',
    { params: { status: status || undefined, page, size: 20 } },
  )
  return data.data
}

export async function getAssignedApplication(id: number) {
  const { data } = await apiClient.get<ApiResponse<ApplicationDetail>>(`/reviewer/applications/${id}`)
  return data.data
}

export async function submitReview(
  id: number,
  payload: { score?: number; comments?: string; recommendation: ReviewRecommendation },
) {
  const { data } = await apiClient.post<ApiResponse<Review>>(`/reviewer/applications/${id}/reviews`, payload)
  return data.data
}

// --- Admin ---

export async function adminListApplications(
  status: ApplicationStatus | '',
  scholarshipId: number | undefined,
  page: number,
) {
  const { data } = await apiClient.get<ApiResponse<PageResponse<ApplicationSummary>>>(
    '/admin/applications',
    { params: { status: status || undefined, scholarshipId, page, size: 20 } },
  )
  return data.data
}

export async function assignReviewer(applicationId: number, reviewerId: number) {
  const { data } = await apiClient.patch<ApiResponse<ApplicationSummary>>(
    `/admin/applications/${applicationId}/assign-reviewer`,
    { reviewerId },
  )
  return data.data
}
