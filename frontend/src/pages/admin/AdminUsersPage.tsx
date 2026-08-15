import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Lock, Search, ShieldOff, UserPlus, Users, X } from 'lucide-react'
import * as adminApi from '../../api/admin'
import { Card } from '../../components/ui/Card'
import { Button } from '../../components/ui/Button'
import { Input, FieldWrapper, Select } from '../../components/ui/Field'
import { StatusBadge } from '../../components/ui/StatusBadge'
import { Avatar } from '../../components/ui/Avatar'
import { PageHeader } from '../../components/ui/PageHeader'
import { RowSkeleton } from '../../components/ui/Skeleton'
import { EmptyState } from '../../components/ui/EmptyState'
import { Alert, extractErrorMessage } from '../../components/ui/Alert'
import type { Role, UserStatus } from '../../types'

const ROLE_OPTIONS: (Role | '')[] = ['', 'STUDENT', 'REVIEWER', 'ADMIN']

export function AdminUsersPage() {
  const [role, setRole] = useState<Role | ''>('')
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const [error, setError] = useState<string | null>(null)
  const [showCreateForm, setShowCreateForm] = useState(false)
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['adminUsers', role, search, page],
    queryFn: () => adminApi.listUsers(role, '', search, page),
  })

  const statusMutation = useMutation({
    mutationFn: ({ userId, status }: { userId: number; status: UserStatus }) =>
      adminApi.updateUserStatus(userId, status),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['adminUsers'] }),
    onError: (err) => setError(extractErrorMessage(err)),
  })

  const [createForm, setCreateForm] = useState<adminApi.CreateUserPayload>({
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    phone: '',
    role: 'REVIEWER',
  })

  const createMutation = useMutation({
    mutationFn: () => adminApi.createUser(createForm),
    onSuccess: () => {
      setShowCreateForm(false)
      setCreateForm({ email: '', password: '', firstName: '', lastName: '', phone: '', role: 'REVIEWER' })
      return queryClient.invalidateQueries({ queryKey: ['adminUsers'] })
    },
    onError: (err) => setError(extractErrorMessage(err, 'Failed to create the account.')),
  })

  return (
    <div>
      <PageHeader
        eyebrow="Administration"
        title="Users"
        action={
          <Button
            variant={showCreateForm ? 'secondary' : 'primary'}
            onClick={() => setShowCreateForm((v) => !v)}
            icon={showCreateForm ? <X className="size-4" /> : <UserPlus className="size-4" />}
          >
            {showCreateForm ? 'Cancel' : 'New reviewer / admin'}
          </Button>
        }
      />

      {error && <div className="mb-4"><Alert variant="error">{error}</Alert></div>}

      {showCreateForm && (
        <Card tint="accent" className="mb-6 animate-fade-in-up">
          <h2 className="mb-4 font-serif text-lg text-ink-900">Provision a new account</h2>
          <form
            onSubmit={(e) => {
              e.preventDefault()
              createMutation.mutate()
            }}
            className="grid grid-cols-1 gap-4 sm:grid-cols-2"
          >
            <FieldWrapper label="First name" htmlFor="c-firstName">
              <Input
                id="c-firstName"
                required
                maxLength={100}
                value={createForm.firstName}
                onChange={(e) => setCreateForm((f) => ({ ...f, firstName: e.target.value }))}
              />
            </FieldWrapper>
            <FieldWrapper label="Last name" htmlFor="c-lastName">
              <Input
                id="c-lastName"
                required
                maxLength={100}
                value={createForm.lastName}
                onChange={(e) => setCreateForm((f) => ({ ...f, lastName: e.target.value }))}
              />
            </FieldWrapper>
            <FieldWrapper label="Email" htmlFor="c-email">
              <Input
                id="c-email"
                type="email"
                required
                maxLength={255}
                value={createForm.email}
                onChange={(e) => setCreateForm((f) => ({ ...f, email: e.target.value }))}
              />
            </FieldWrapper>
            <FieldWrapper
              label="Temporary password"
              htmlFor="c-password"
              hint="At least 8 characters, with an uppercase letter, lowercase letter, digit, and symbol."
            >
              <Input
                id="c-password"
                type="password"
                required
                minLength={8}
                maxLength={72}
                autoComplete="new-password"
                value={createForm.password}
                onChange={(e) => setCreateForm((f) => ({ ...f, password: e.target.value }))}
              />
            </FieldWrapper>
            <FieldWrapper label="Phone (optional)" htmlFor="c-phone">
              <Input
                id="c-phone"
                maxLength={20}
                pattern="^$|^[+0-9()\-\s]{7,20}$"
                title="Use digits, spaces, and + ( ) - only, 7-20 characters"
                value={createForm.phone}
                onChange={(e) => setCreateForm((f) => ({ ...f, phone: e.target.value }))}
              />
            </FieldWrapper>
            <FieldWrapper label="Role" htmlFor="c-role">
              <Select
                id="c-role"
                value={createForm.role}
                onChange={(e) => setCreateForm((f) => ({ ...f, role: e.target.value as Role }))}
              >
                <option value="REVIEWER">Reviewer</option>
                <option value="ADMIN">Admin</option>
                <option value="STUDENT">Student</option>
              </Select>
            </FieldWrapper>
            <div className="sm:col-span-2">
              <Button type="submit" loading={createMutation.isPending}>
                Create account
              </Button>
            </div>
          </form>
        </Card>
      )}

      <div className="mb-5 flex flex-wrap gap-3">
        <Select
          value={role}
          onChange={(e) => {
            setRole(e.target.value as Role | '')
            setPage(0)
          }}
          className="w-auto"
        >
          {ROLE_OPTIONS.map((r) => (
            <option key={r} value={r}>
              {r === '' ? 'All roles' : r}
            </option>
          ))}
        </Select>
        <Input
          placeholder="Search by email…"
          icon={<Search className="size-4" />}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="max-w-xs"
        />
      </div>

      {isLoading && (
        <div className="flex flex-col gap-2">
          {Array.from({ length: 4 }).map((_, i) => (
            <RowSkeleton key={i} />
          ))}
        </div>
      )}

      {data && data.content.length === 0 && <EmptyState icon={Users} title="No users found" description="Try a different filter." />}

      <div className="flex flex-col gap-2">
        {data?.content.map((u, i) => (
          <Card
            key={u.id}
            padding="none"
            className="flex animate-fade-in-up flex-wrap items-center justify-between gap-3 px-5 py-3.5"
            style={{ animationDelay: `${i * 50}ms` }}
          >
            <div className="flex min-w-0 items-center gap-3">
              <Avatar name={`${u.firstName} ${u.lastName}`} size="sm" />
              <div className="min-w-0">
                <p className="truncate text-sm font-medium text-ink-900">
                  {u.firstName} {u.lastName}{' '}
                  <span className="font-mono text-[11px] font-normal uppercase tracking-[0.04em] text-ink-400">
                    {u.role}
                  </span>
                </p>
                <p className="truncate text-xs text-ink-500">{u.email}</p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <StatusBadge status={u.status} />
              {u.status !== 'ACTIVE' && (
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={() => statusMutation.mutate({ userId: u.id, status: 'ACTIVE' })}
                >
                  Activate
                </Button>
              )}
              {u.status !== 'LOCKED' && (
                <Button
                  variant="secondary"
                  size="sm"
                  icon={<Lock className="size-3.5" />}
                  onClick={() => statusMutation.mutate({ userId: u.id, status: 'LOCKED' })}
                >
                  Lock
                </Button>
              )}
              {u.status !== 'DISABLED' && (
                <Button
                  variant="danger"
                  size="sm"
                  icon={<ShieldOff className="size-3.5" />}
                  onClick={() => statusMutation.mutate({ userId: u.id, status: 'DISABLED' })}
                >
                  Disable
                </Button>
              )}
            </div>
          </Card>
        ))}
      </div>
    </div>
  )
}
