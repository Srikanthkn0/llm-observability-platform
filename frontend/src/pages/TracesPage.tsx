import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'
import StatusBadge from '../components/StatusBadge'
import { useProject } from '../contexts/ProjectContext'
import type { PageResponse, TraceSummary } from '../types'

export default function TracesPage() {
  const { project } = useProject()
  const [data, setData] = useState<PageResponse<TraceSummary> | null>(null)

  useEffect(() => {
    if (!project) return
    const load = () => api<PageResponse<TraceSummary>>(`/api/v1/projects/${project.id}/traces?size=50`).then(setData)
    load()
    const id = setInterval(load, 5000)
    return () => clearInterval(id)
  }, [project])

  if (!project) return null

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold">Traces</h1>
        <p className="text-sm text-slate-400">Polling every 5s</p>
      </div>
      <div className="card overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="text-left text-slate-400 border-b border-slate-800">
            <tr>
              <th className="py-2 pr-3">Name</th>
              <th className="py-2 pr-3">Status</th>
              <th className="py-2 pr-3">Model</th>
              <th className="py-2 pr-3">Tokens</th>
              <th className="py-2 pr-3">Cost</th>
              <th className="py-2 pr-3">Latency</th>
              <th className="py-2">Ingested</th>
            </tr>
          </thead>
          <tbody>
            {(data?.content || []).map(t => (
              <tr key={t.id} className="border-b border-slate-800/60 hover:bg-slate-800/40">
                <td className="py-2 pr-3">
                  <Link className="text-accent-soft hover:underline" to={`/traces/${t.id}`}>{t.name || t.id.slice(0, 8)}</Link>
                </td>
                <td className="py-2 pr-3"><StatusBadge value={t.status} /></td>
                <td className="py-2 pr-3">{t.model || '—'}</td>
                <td className="py-2 pr-3">{t.totalTokens ?? 0}</td>
                <td className="py-2 pr-3">${Number(t.estimatedCostUsd || 0).toFixed(4)}</td>
                <td className="py-2 pr-3">{t.latencyMs ?? '—'} ms</td>
                <td className="py-2 text-slate-400">{new Date(t.ingestedAt).toLocaleString()}</td>
              </tr>
            ))}
            {!data?.content?.length && (
              <tr><td colSpan={7} className="py-8 text-center text-slate-500">No traces yet — ingest with curl (see README)</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
