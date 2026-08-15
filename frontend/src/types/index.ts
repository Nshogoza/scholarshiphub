// Mirrors the backend's dto.response / dto.request shapes (see
// backend/src/main/java/com/scholarshiphub/dto). Kept as one file for a
// frontend this size; if it grows, split per-domain.

export type Role = 'STUDENT' | 'REVIEWER' | 'ADMIN'
export type UserStatus = 'ACTIVE' | 'LOCKED' | 'DISABLED'
export type ScholarshipStatus = 'DRAFT' | 'PUBLISHED' | 'CLOSED' | 'ARCHIVED'
export type ApplicationStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'UNDER_REVIEW'
  | 'APPROVED'
  | 'REJECTED'
  | 'ADDITIONAL_INFO_REQUIRED'
export type ReviewRecommendation = 'APPROVE' | 'REJECT' | 'REQUEST_ADDITIONAL_INFO'

export interface ApiResponse<T> {
  success: boolean
  message?: string
  data: T
  timestamp: string
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export interface ApiError {
  success: false
  errorCode: string
  message: string
  path: string
  timestamp: string
  fieldErrors?: { field: string; message: string }[]
}

export interface UserSummary {
  id: number
  email: string
  firstName: string
  lastName: string
  phone?: string
  role: Role
  status: UserStatus
  emailVerified: boolean
  createdAt: string
}

export interface AuthResponse {
  accessToken: string
  tokenType: string
  expiresInSeconds: number
  user: UserSummary
}

export interface StudentProfile {
  userId: number
  educationLevel?: string
  school?: string
  gpa?: number
  personalStatement?: string
}

export interface RequiredDocument {
  id?: number
  documentName: string
  mandatory: boolean
}

export interface Scholarship {
  id: number
  title: string
  description: string
  eligibilityCriteria: string
  amount: number
  applicationDeadline: string
  status: ScholarshipStatus
  requiredDocuments: RequiredDocument[]
  createdByName: string
  createdAt: string
  updatedAt: string
}

export interface ApplicationDocument {
  id: number
  documentName: string
  originalFilename: string
  contentType: string
  fileSizeBytes: number
  uploadedAt: string
}

export interface Review {
  id: number
  reviewerId: number
  reviewerName: string
  score?: number
  comments?: string
  recommendation: ReviewRecommendation
  createdAt: string
}

export interface ApplicationSummary {
  id: number
  scholarshipId: number
  scholarshipTitle: string
  studentId: number
  studentName: string
  reviewerId?: number
  reviewerName?: string
  status: ApplicationStatus
  submittedAt?: string
  decidedAt?: string
  createdAt: string
  updatedAt: string
}

export interface ApplicationDetail {
  id: number
  scholarship: Scholarship
  studentId: number
  studentName: string
  reviewerId?: number
  reviewerName?: string
  status: ApplicationStatus
  submittedAt?: string
  decidedAt?: string
  documents: ApplicationDocument[]
  reviews: Review[]
  createdAt: string
  updatedAt: string
}

export interface DashboardAnalytics {
  totalStudents: number
  totalReviewers: number
  totalScholarships: number
  totalApplications: number
  approvalRatePercent: number
  applicationsByStatus: Record<string, number>
  recentActivity: AuditLogEntry[]
}

export interface AuditLogEntry {
  id: number
  actorUserId?: number
  actorEmail: string
  action: string
  entityType?: string
  entityId?: number
  details?: string
  ipAddress?: string
  createdAt: string
}
