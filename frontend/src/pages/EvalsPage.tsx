import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'
import StatusBadge from '../components/StatusBadge'
import { useProject } from '../contexts/ProjectContext'
import type { Evaluation, PageResponse } from '../types'

export default function EvalsPage() {
  const { project } = useProject()
  const [data, setData] = useState<PageResponse<Evaluation> | null>(null)

  useEffect(() => {
    if (!project) return
    const load = () => api<PageResponse<Evaluation>>(`/api/v1/projects/${project.id}/evaluations?size=50`).then(setData)
    load()
    const id = setInterval(load, 8000)
    return () => clearInterval(id)
  }, [project])

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">Evaluations</h1>
      <div className="card overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="text-left text-slate-400 border-b border-slate-800">
            <tr>
              <th className="py-2">Type</th>
              <th className="py-2">Score</th>
              <th className="py-2">Passed</th>
              <th className="py-2">Judge</th>
              <th className="py-2">Trace</th>
              <th className="py-2">Rationale</th>
            </tr>
          </thead>
          <tbody>
            {(data?.content || []).map(e => (
              <tr key={e.id} className="border-b border-slate-800/60">
                <td className="py-2 capitalize">{e.evalType}</td>
                <td className="py-2">{e.score != null ? Number(e.score).toFixed(2) : '—'}</td>
                <td className="py-2"><StatusBadge value={e.passed ? 'PROCESSED' : 'FAILED'} /></td>
                <td className="py-2">{e.judge}</td>
                <td className="py-2"><Link className="text-accent-soft" to={`/traces/${e.traceId}`}>{e.traceId.slice(0, 8)}</Link></td>
                <td className="py-2 text-slate-400 max-w-md truncate">{e.rationale}</td>
              </tr>
            ))}
            {!data?.content?.length && <tr><td colSpan={6} className="py-8 text-center text-slate-500">No evaluations yet</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  )
}
