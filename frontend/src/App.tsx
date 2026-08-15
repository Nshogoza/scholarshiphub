import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { PublicShell } from './components/shell/PublicShell'
import { AuthLayout } from './components/shell/AuthLayout'
import { DashboardShell } from './components/shell/DashboardShell'
import { ProtectedRoute } from './components/ProtectedRoute'
import { HomePage } from './pages/HomePage'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { VerifyEmailPage } from './pages/VerifyEmailPage'
import { ForgotPasswordPage } from './pages/ForgotPasswordPage'
import { ResetPasswordPage } from './pages/ResetPasswordPage'
import { ScholarshipListingPage } from './pages/ScholarshipListingPage'
import { ScholarshipDetailsPage } from './pages/ScholarshipDetailsPage'
import { MyApplicationsPage } from './pages/MyApplicationsPage'
import { ApplicationWorkspacePage } from './pages/ApplicationWorkspacePage'
import { ReviewerDashboardPage } from './pages/ReviewerDashboardPage'
import { ReviewApplicationPage } from './pages/ReviewApplicationPage'
import { ProfilePage } from './pages/ProfilePage'
import { AdminDashboardPage } from './pages/AdminDashboardPage'
import { AdminScholarshipsPage } from './pages/admin/AdminScholarshipsPage'
import { AdminScholarshipFormPage } from './pages/admin/AdminScholarshipFormPage'
import { AdminUsersPage } from './pages/admin/AdminUsersPage'
import { AdminApplicationsPage } from './pages/admin/AdminApplicationsPage'
import { NotFoundPage } from './pages/NotFoundPage'

export function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public marketing page keeps the lightweight top-nav shell */}
        <Route element={<PublicShell />}>
          <Route index element={<HomePage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Route>

        {/* Auth pages get their own full-screen split layout */}
        <Route element={<AuthLayout />}>
          <Route path="login" element={<LoginPage />} />
          <Route path="register" element={<RegisterPage />} />
          <Route path="verify-email" element={<VerifyEmailPage />} />
          <Route path="forgot-password" element={<ForgotPasswordPage />} />
          <Route path="reset-password" element={<ResetPasswordPage />} />
        </Route>

        {/* Authenticated app pages share the sidebar dashboard shell */}
        <Route element={<DashboardShell />}>
          <Route element={<ProtectedRoute />}>
            <Route path="profile" element={<ProfilePage />} />
          </Route>

          <Route element={<ProtectedRoute allowedRoles={['STUDENT']} />}>
            <Route path="scholarships" element={<ScholarshipListingPage />} />
            <Route path="scholarships/:id" element={<ScholarshipDetailsPage />} />
            <Route path="my-applications" element={<MyApplicationsPage />} />
            <Route path="my-applications/:id" element={<ApplicationWorkspacePage />} />
          </Route>

          <Route element={<ProtectedRoute allowedRoles={['REVIEWER']} />}>
            <Route path="reviewer" element={<ReviewerDashboardPage />} />
            <Route path="reviewer/:id" element={<ReviewApplicationPage />} />
          </Route>

          <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
            <Route path="admin" element={<AdminDashboardPage />} />
            <Route path="admin/scholarships" element={<AdminScholarshipsPage />} />
            <Route path="admin/scholarships/new" element={<AdminScholarshipFormPage />} />
            <Route path="admin/scholarships/:id/edit" element={<AdminScholarshipFormPage />} />
            <Route path="admin/users" element={<AdminUsersPage />} />
            <Route path="admin/applications" element={<AdminApplicationsPage />} />
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
