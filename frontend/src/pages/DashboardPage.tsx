import { useEffect, useState } from 'react'
import { api } from '../api/client'
import StatCard from '../components/StatCard'
import { useProject } from '../contexts/ProjectContext'
import type { DashboardMetrics } from '../types'

export default function DashboardPage() {
  const { project } = useProject()
  const [m, setM] = useState<DashboardMetrics | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!project) return
    const load = () => api<DashboardMetrics>(`/api/v1/projects/${project.id}/metrics/dashboard`)
      .then(setM)
      .catch(e => setError(e.message))
    load()
    const id = setInterval(load, 10000)
    return () => clearInterval(id)
  }, [project])

  if (!project) return <div className="text-slate-400">No project selected</div>
  if (error) return <div className="text-rose-400">{error}</div>

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Dashboard</h1>
        <p className="text-sm text-slate-400">{project.name} · last 24h · auto-refresh 10s</p>
      </div>
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Traces" value={m?.tracesLast24h ?? '—'} />
        <StatCard label="Blocked" value={m?.blockedLast24h ?? '—'} />
        <StatCard label="Cost (USD)" value={m ? Number(m.costLast24h).toFixed(4) : '—'} />
        <StatCard label="Avg latency" value={m ? `${Math.round(m.avgLatencyMs)} ms` : '—'} />
        <StatCard label="Avg quality" value={m?.avgQualityScore != null ? m.avgQualityScore.toFixed(2) : '—'} />
        <StatCard label="Avg safety" value={m?.avgSafetyScore != null ? m.avgSafetyScore.toFixed(2) : '—'} />
        <StatCard label="Eval failures" value={m?.evalFailuresLast24h ?? '—'} />
        <StatCard label="Guardrail blocks (all time)" value={m?.guardrailBlocksTotal ?? '—'} />
      </div>
    </div>
  )
}
