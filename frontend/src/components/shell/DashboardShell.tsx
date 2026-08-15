import { useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { Award, ClipboardCheck, FileText, LayoutDashboard, LogOut, Menu, User, Users, X } from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { Avatar } from '../ui/Avatar'
import { Logo } from './Logo'

interface NavItem {
  to: string
  label: string
  icon: LucideIcon
  end?: boolean
}

const NAV_BY_ROLE: Record<string, NavItem[]> = {
  STUDENT: [
    { to: '/scholarships', label: 'Scholarships', icon: Award },
    { to: '/my-applications', label: 'My Applications', icon: FileText },
  ],
  REVIEWER: [{ to: '/reviewer', label: 'Assigned Applications', icon: ClipboardCheck }],
  ADMIN: [
    { to: '/admin', label: 'Dashboard', icon: LayoutDashboard, end: true },
    { to: '/admin/scholarships', label: 'Scholarships', icon: Award },
    { to: '/admin/applications', label: 'Applications', icon: FileText },
    { to: '/admin/users', label: 'Users', icon: Users },
  ],
}

export function DashboardShell() {
  const { user, logout } = useAuth()
  const [mobileOpen, setMobileOpen] = useState(false)

  if (!user) return null
  const navItems = [...(NAV_BY_ROLE[user.role] ?? []), { to: '/profile', label: 'Profile', icon: User }]

  const navLinkClasses = ({ isActive }: { isActive: boolean }) =>
    `group flex items-center gap-3 border-l-2 py-2.5 pl-4 pr-3 text-[13px] font-medium tracking-[0.01em] transition-colors ${
      isActive
        ? 'border-accent-500 bg-white/[0.06] text-white'
        : 'border-transparent text-brand-200/70 hover:border-white/20 hover:bg-white/[0.04] hover:text-white'
    }`

  const sidebarContent = (
    <div className="flex h-full flex-col">
      <div className="px-5 py-6">
        <Logo to={user.role === 'ADMIN' ? '/admin' : user.role === 'REVIEWER' ? '/reviewer' : '/scholarships'} dark />
      </div>

      <p className="px-5 pb-2 font-mono text-[10px] uppercase tracking-[0.14em] text-brand-300/60">Navigation</p>
      <nav className="flex-1 space-y-0.5 px-0">
        {navItems.map((item) => (
          <NavLink key={item.to} to={item.to} end={item.end} className={navLinkClasses} onClick={() => setMobileOpen(false)}>
            <item.icon className="size-[17px] shrink-0" strokeWidth={1.75} />
            {item.label}
          </NavLink>
        ))}
      </nav>

      <div className="m-3 rounded-[10px] bg-white/[0.05] px-4 py-4">
        <div className="mb-3 flex items-center gap-3">
          <Avatar name={`${user.firstName} ${user.lastName}`} />
          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-medium text-white">
              {user.firstName} {user.lastName}
            </p>
            <p className="truncate font-mono text-[10.5px] uppercase tracking-[0.08em] text-brand-300/70">
              {user.role}
            </p>
          </div>
        </div>
        <button
          onClick={() => void logout()}
          className="flex w-full items-center gap-2.5 py-1.5 text-[13px] font-medium text-brand-200/70 transition-colors hover:text-accent-400"
        >
          <LogOut className="size-4" strokeWidth={1.75} />
          Log out
        </button>
      </div>
    </div>
  )

  return (
    <div className="min-h-screen bg-paper">
      {/* Desktop sidebar */}
      <aside className="fixed inset-y-0 left-0 z-30 hidden w-64 bg-brand-950 lg:block">{sidebarContent}</aside>

      {/* Mobile top bar + slide-in sidebar */}
      <div className="sticky top-0 z-30 flex items-center justify-between bg-paper-raised px-4 py-3 shadow-[0_1px_0_rgba(184,134,42,0.25)] lg:hidden">
        <Logo />
        <button onClick={() => setMobileOpen(true)} className="rounded-[3px] p-2 text-ink-600 hover:bg-ink-100" aria-label="Open menu">
          <Menu className="size-5" />
        </button>
      </div>

      {mobileOpen && (
        <div className="fixed inset-0 z-40 lg:hidden">
          <div className="absolute inset-0 bg-ink-950/50" onClick={() => setMobileOpen(false)} />
          <aside className="absolute inset-y-0 left-0 w-72 bg-brand-950 shadow-2xl">
            <button
              onClick={() => setMobileOpen(false)}
              className="absolute right-3 top-5 rounded-[3px] p-2 text-brand-200 hover:bg-white/10"
              aria-label="Close menu"
            >
              <X className="size-5" />
            </button>
            {sidebarContent}
          </aside>
        </div>
      )}

      <main className="lg:pl-64">
        <div className="mx-auto max-w-6xl px-4 py-8 sm:px-6 lg:px-10 lg:py-10">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
