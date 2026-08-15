import { useEffect, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useAuth } from '../context/AuthContext'
import * as profileApi from '../api/profile'
import * as authApi from '../api/auth'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { FieldWrapper, Input, Textarea } from '../components/ui/Field'
import { Alert, extractErrorMessage } from '../components/ui/Alert'
import { LoadingSpinner } from '../components/ui/LoadingSpinner'
import { Avatar } from '../components/ui/Avatar'
import { StatusBadge } from '../components/ui/StatusBadge'
import { FloatingOrbs } from '../components/ui/FloatingOrbs'

export function ProfilePage() {
  const { user, refreshCurrentUser } = useAuth()
  const queryClient = useQueryClient()

  const [basicForm, setBasicForm] = useState({ firstName: '', lastName: '', phone: '' })
  const [basicMessage, setBasicMessage] = useState<string | null>(null)
  const [basicError, setBasicError] = useState<string | null>(null)

  useEffect(() => {
    if (user) {
      setBasicForm({ firstName: user.firstName, lastName: user.lastName, phone: user.phone ?? '' })
    }
  }, [user])

  const basicMutation = useMutation({
    mutationFn: () => profileApi.updateProfile(basicForm),
    onSuccess: async () => {
      setBasicMessage('Profile updated.')
      setBasicError(null)
      await refreshCurrentUser()
    },
    onError: (err) => setBasicError(extractErrorMessage(err)),
  })

  const isStudent = user?.role === 'STUDENT'

  const { data: studentProfile, isLoading: loadingStudentProfile } = useQuery({
    queryKey: ['studentProfile'],
    queryFn: profileApi.getStudentProfile,
    enabled: isStudent,
  })

  const [academicForm, setAcademicForm] = useState({
    educationLevel: '',
    school: '',
    gpa: '',
    personalStatement: '',
  })
  const [academicMessage, setAcademicMessage] = useState<string | null>(null)
  const [academicError, setAcademicError] = useState<string | null>(null)

  useEffect(() => {
    if (studentProfile) {
      setAcademicForm({
        educationLevel: studentProfile.educationLevel ?? '',
        school: studentProfile.school ?? '',
        gpa: studentProfile.gpa != null ? String(studentProfile.gpa) : '',
        personalStatement: studentProfile.personalStatement ?? '',
      })
    }
  }, [studentProfile])

  const academicMutation = useMutation({
    mutationFn: () =>
      profileApi.updateStudentProfile({
        educationLevel: academicForm.educationLevel || undefined,
        school: academicForm.school || undefined,
        gpa: academicForm.gpa ? Number(academicForm.gpa) : undefined,
        personalStatement: academicForm.personalStatement || undefined,
      }),
    onSuccess: async () => {
      setAcademicMessage('Academic profile updated.')
      setAcademicError(null)
      await queryClient.invalidateQueries({ queryKey: ['studentProfile'] })
    },
    onError: (err) => setAcademicError(extractErrorMessage(err)),
  })

  const [passwordForm, setPasswordForm] = useState({ currentPassword: '', newPassword: '' })
  const [passwordMessage, setPasswordMessage] = useState<string | null>(null)
  const [passwordError, setPasswordError] = useState<string | null>(null)

  const passwordMutation = useMutation({
    mutationFn: () => authApi.changePassword(passwordForm.currentPassword, passwordForm.newPassword),
    onSuccess: () => {
      setPasswordMessage('Password changed successfully.')
      setPasswordError(null)
      setPasswordForm({ currentPassword: '', newPassword: '' })
    },
    onError: (err) => setPasswordError(extractErrorMessage(err)),
  })

  if (!user) return null

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-6">
      <div className="relative flex items-center gap-4 overflow-hidden rounded-[10px] bg-brand-950 p-6 shadow-[var(--shadow-raised)]">
        <FloatingOrbs variant="dark" />
        <div className="relative">
          <Avatar name={`${user.firstName} ${user.lastName}`} size="lg" />
        </div>
        <div className="relative">
          <h1 className="font-serif text-xl font-medium text-white">
            {user.firstName} {user.lastName}
          </h1>
          <p className="font-mono text-xs text-brand-300">{user.email}</p>
          <div className="mt-2 flex items-center gap-2">
            <StatusBadge status={user.status} />
            <span className="font-mono text-[10.5px] uppercase tracking-[0.08em] text-brand-300">{user.role}</span>
          </div>
        </div>
      </div>

      <Card>
        <h2 className="mb-4 text-[13px] font-medium uppercase tracking-[0.06em] text-ink-500">Account details</h2>
        {basicError && <div className="mb-4"><Alert variant="error">{basicError}</Alert></div>}
        {basicMessage && <div className="mb-4"><Alert variant="success">{basicMessage}</Alert></div>}
        <form
          onSubmit={(e) => {
            e.preventDefault()
            basicMutation.mutate()
          }}
          className="flex flex-col gap-4"
        >
          <FieldWrapper label="Email" htmlFor="email-ro">
            <Input id="email-ro" value={user.email} disabled />
          </FieldWrapper>
          <div className="grid grid-cols-2 gap-4">
            <FieldWrapper label="First name" htmlFor="firstName">
              <Input
                id="firstName"
                required
                maxLength={100}
                value={basicForm.firstName}
                onChange={(e) => setBasicForm((f) => ({ ...f, firstName: e.target.value }))}
              />
            </FieldWrapper>
            <FieldWrapper label="Last name" htmlFor="lastName">
              <Input
                id="lastName"
                required
                maxLength={100}
                value={basicForm.lastName}
                onChange={(e) => setBasicForm((f) => ({ ...f, lastName: e.target.value }))}
              />
            </FieldWrapper>
          </div>
          <FieldWrapper label="Phone" htmlFor="phone">
            <Input
              id="phone"
              maxLength={20}
              pattern="^$|^[+0-9()\-\s]{7,20}$"
              title="Use digits, spaces, and + ( ) - only, 7-20 characters"
              value={basicForm.phone}
              onChange={(e) => setBasicForm((f) => ({ ...f, phone: e.target.value }))}
            />
          </FieldWrapper>
          <Button type="submit" loading={basicMutation.isPending} className="self-start">
            Save changes
          </Button>
        </form>
      </Card>

      {isStudent && (
        <Card>
          <h2 className="mb-4 text-[13px] font-medium uppercase tracking-[0.06em] text-ink-500">Academic profile</h2>
          {loadingStudentProfile && <LoadingSpinner />}
          {academicError && <div className="mb-4"><Alert variant="error">{academicError}</Alert></div>}
          {academicMessage && <div className="mb-4"><Alert variant="success">{academicMessage}</Alert></div>}
          <form
            onSubmit={(e) => {
              e.preventDefault()
              academicMutation.mutate()
            }}
            className="flex flex-col gap-4"
          >
            <div className="grid grid-cols-2 gap-4">
              <FieldWrapper label="Education level" htmlFor="educationLevel">
                <Input
                  id="educationLevel"
                  placeholder="e.g. Undergraduate"
                  maxLength={50}
                  value={academicForm.educationLevel}
                  onChange={(e) => setAcademicForm((f) => ({ ...f, educationLevel: e.target.value }))}
                />
              </FieldWrapper>
              <FieldWrapper label="GPA" htmlFor="gpa">
                <Input
                  id="gpa"
                  type="number"
                  step="0.01"
                  min={0}
                  max={10}
                  value={academicForm.gpa}
                  onChange={(e) => setAcademicForm((f) => ({ ...f, gpa: e.target.value }))}
                />
              </FieldWrapper>
            </div>
            <FieldWrapper label="School" htmlFor="school">
              <Input
                id="school"
                maxLength={255}
                value={academicForm.school}
                onChange={(e) => setAcademicForm((f) => ({ ...f, school: e.target.value }))}
              />
            </FieldWrapper>
            <FieldWrapper label="Personal statement" htmlFor="personalStatement">
              <Textarea
                id="personalStatement"
                rows={5}
                maxLength={5000}
                value={academicForm.personalStatement}
                onChange={(e) => setAcademicForm((f) => ({ ...f, personalStatement: e.target.value }))}
              />
            </FieldWrapper>
            <Button type="submit" loading={academicMutation.isPending} className="self-start">
              Save academic profile
            </Button>
          </form>
        </Card>
      )}

      <Card>
        <h2 className="mb-4 text-[13px] font-medium uppercase tracking-[0.06em] text-ink-500">Change password</h2>
        {passwordError && <div className="mb-4"><Alert variant="error">{passwordError}</Alert></div>}
        {passwordMessage && <div className="mb-4"><Alert variant="success">{passwordMessage}</Alert></div>}
        <form
          onSubmit={(e) => {
            e.preventDefault()
            passwordMutation.mutate()
          }}
          className="flex flex-col gap-4"
        >
          <FieldWrapper label="Current password" htmlFor="currentPassword">
            <Input
              id="currentPassword"
              type="password"
              required
              autoComplete="current-password"
              value={passwordForm.currentPassword}
              onChange={(e) => setPasswordForm((f) => ({ ...f, currentPassword: e.target.value }))}
            />
          </FieldWrapper>
          <FieldWrapper
            label="New password"
            htmlFor="newPassword"
            hint="At least 8 characters, with an uppercase letter, lowercase letter, digit, and symbol."
          >
            <Input
              id="newPassword"
              type="password"
              required
              minLength={8}
              maxLength={72}
              autoComplete="new-password"
              value={passwordForm.newPassword}
              onChange={(e) => setPasswordForm((f) => ({ ...f, newPassword: e.target.value }))}
            />
          </FieldWrapper>
          <Button type="submit" loading={passwordMutation.isPending} className="self-start">
            Change password
          </Button>
        </form>
      </Card>
    </div>
  )
}
