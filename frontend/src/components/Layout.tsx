import { NavLink, Outlet } from 'react-router-dom'
import {
  Activity, AlertTriangle, BarChart3, Shield, Settings, DollarSign,
  LayoutDashboard, LogOut, FlaskConical
} from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import { useProject } from '../contexts/ProjectContext'

const links = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/traces', label: 'Traces', icon: Activity },
  { to: '/evals', label: 'Evals', icon: FlaskConical },
  { to: '/guardrails', label: 'Guardrails', icon: Shield },
  { to: '/costs', label: 'Costs', icon: DollarSign },
  { to: '/alerts', label: 'Alerts', icon: AlertTriangle },
  { to: '/settings', label: 'Settings', icon: Settings },
]

export default function Layout() {
  const { user, logout } = useAuth()
  const { projects, project, setProjectId } = useProject()

  return (
    <div className="min-h-screen flex bg-ink-950">
      <aside className="w-60 border-r border-slate-800 bg-ink-900/60 p-4 flex flex-col">
        <div className="mb-6">
          <div className="flex items-center gap-2 text-accent font-semibold">
            <BarChart3 size={20} />
            LLM Observability
          </div>
          <p className="text-xs text-slate-500 mt-1">Eval · Guardrails · Cost</p>
        </div>
        <nav className="space-y-1 flex-1">
          {links.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/'}
              className={({ isActive }) =>
                `flex items-center gap-2 rounded-lg px-3 py-2 text-sm ${isActive ? 'bg-accent/20 text-accent-soft' : 'text-slate-300 hover:bg-slate-800'}`
              }
            >
              <Icon size={16} /> {label}
            </NavLink>
          ))}
        </nav>
        <div className="border-t border-slate-800 pt-3 space-y-2">
          <select
            className="input text-xs"
            value={project?.id || ''}
            onChange={(e) => setProjectId(e.target.value)}
          >
            {projects.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
          </select>
          <div className="text-xs text-slate-400 truncate">{user?.email}</div>
          <button className="btn-ghost w-full text-xs" onClick={logout}>
            <LogOut size={14} /> Sign out
          </button>
        </div>
      </aside>
      <main className="flex-1 overflow-auto">
        <div className="mx-auto max-w-6xl p-6">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
