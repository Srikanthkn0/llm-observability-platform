import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'
import StatusBadge from '../components/StatusBadge'
import { useProject } from '../contexts/ProjectContext'
import type { GuardrailDecision, PageResponse } from '../types'

export default function GuardrailsPage() {
  const { project } = useProject()
  const [data, setData] = useState<PageResponse<GuardrailDecision> | null>(null)

  useEffect(() => {
    if (!project) return
    const load = () => api<PageResponse<GuardrailDecision>>(`/api/v1/projects/${project.id}/guardrails?size=50`).then(setData)
    load()
    const id = setInterval(load, 8000)
    return () => clearInterval(id)
  }, [project])

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">Guardrails</h1>
      <p className="text-sm text-slate-400">Injection · PII · Toxicity heuristics with ALLOW / FLAG / BLOCK</p>
      <div className="card overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="text-left text-slate-400 border-b border-slate-800">
            <tr>
              <th className="py-2">Rule</th>
              <th className="py-2">Category</th>
              <th className="py-2">Action</th>
              <th className="py-2">Matched</th>
              <th className="py-2">Severity</th>
              <th className="py-2">Trace</th>
              <th className="py-2">When</th>
            </tr>
          </thead>
          <tbody>
            {(data?.content || []).map(g => (
              <tr key={g.id} className="border-b border-slate-800/60">
                <td className="py-2">{g.ruleName}</td>
                <td className="py-2">{g.category}</td>
                <td className="py-2"><StatusBadge value={g.action} /></td>
                <td className="py-2">{g.matched ? 'yes' : 'no'}</td>
                <td className="py-2">{g.severity}</td>
                <td className="py-2">{g.traceId ? <Link className="text-accent-soft" to={`/traces/${g.traceId}`}>{g.traceId.slice(0, 8)}</Link> : '—'}</td>
                <td className="py-2 text-slate-400">{new Date(g.createdAt).toLocaleString()}</td>
              </tr>
            ))}
            {!data?.content?.length && <tr><td colSpan={7} className="py-8 text-center text-slate-500">No guardrail decisions yet</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  )
}
