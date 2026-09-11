import { useEffect, useState } from 'react'
import { api } from '../api/client'
import StatCard from '../components/StatCard'
import { useProject } from '../contexts/ProjectContext'
import type { DashboardMetrics, PageResponse, TraceSummary } from '../types'

export default function CostsPage() {
  const { project } = useProject()
  const [m, setM] = useState<DashboardMetrics | null>(null)
  const [traces, setTraces] = useState<TraceSummary[]>([])

  useEffect(() => {
    if (!project) return
    Promise.all([
      api<DashboardMetrics>(`/api/v1/projects/${project.id}/metrics/dashboard`),
      api<PageResponse<TraceSummary>>(`/api/v1/projects/${project.id}/traces?size=20`),
    ]).then(([metrics, page]) => {
      setM(metrics)
      setTraces(page.content)
    })
  }, [project])

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">Costs</h1>
      <div className="grid gap-4 sm:grid-cols-3">
        <StatCard label="24h cost" value={m ? `$${Number(m.costLast24h).toFixed(4)}` : '—'} />
        <StatCard label="24h traces" value={m?.tracesLast24h ?? '—'} />
        <StatCard label="Avg latency" value={m ? `${Math.round(m.avgLatencyMs)} ms` : '—'} />
      </div>
      <div className="card">
        <h2 className="font-medium mb-3">Recent trace costs</h2>
        <ul className="space-y-2 text-sm">
          {traces.map(t => (
            <li key={t.id} className="flex justify-between border-b border-slate-800 py-2">
              <span>{t.name}</span>
              <span>${Number(t.estimatedCostUsd || 0).toFixed(4)} · {t.model || 'n/a'}</span>
            </li>
          ))}
          {!traces.length && <li className="text-slate-500">No cost data yet</li>}
        </ul>
      </div>
    </div>
  )
}
