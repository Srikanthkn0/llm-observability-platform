import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../api/client'
import StatusBadge from '../components/StatusBadge'
import { useProject } from '../contexts/ProjectContext'
import type { TraceDetail } from '../types'

export default function TraceDetailPage() {
  const { traceId } = useParams()
  const { project } = useProject()
  const [trace, setTrace] = useState<TraceDetail | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!project || !traceId) return
    const load = () => api<TraceDetail>(`/api/v1/projects/${project.id}/traces/${traceId}`)
      .then(setTrace)
      .catch(e => setError(e.message))
    load()
    const id = setInterval(load, 4000)
    return () => clearInterval(id)
  }, [project, traceId])

  if (error) return <div className="text-rose-400">{error}</div>
  if (!trace) return <div className="text-slate-400">Loading…</div>

  return (
    <div className="space-y-6">
      <div>
        <Link to="/traces" className="text-sm text-accent-soft">← Traces</Link>
        <h1 className="text-2xl font-semibold mt-1">{trace.name}</h1>
        <div className="mt-2 flex flex-wrap gap-2 items-center text-sm text-slate-400">
          <StatusBadge value={trace.status} />
          <span>{trace.model}</span>
          <span>{trace.totalTokens} tokens</span>
          <span>${Number(trace.estimatedCostUsd || 0).toFixed(4)}</span>
          <span>{trace.latencyMs ?? '—'} ms</span>
        </div>
        {trace.errorMessage && <p className="mt-2 text-sm text-rose-400">{trace.errorMessage}</p>}
      </div>

      <section className="card space-y-3">
        <h2 className="font-medium">Spans</h2>
        {trace.spans.map(s => (
          <div key={s.id} className="rounded-lg border border-slate-800 p-3 space-y-2">
            <div className="flex justify-between text-sm">
              <span className="font-medium">{s.name}</span>
              <span className="text-slate-400">{s.kind} · {s.latencyMs ?? '—'} ms</span>
            </div>
            {s.inputText && (
              <div>
                <div className="text-xs text-slate-500 mb-1">Input</div>
                <pre className="whitespace-pre-wrap text-xs bg-ink-950 rounded p-2 max-h-40 overflow-auto">{s.inputText}</pre>
              </div>
            )}
            {s.outputText && (
              <div>
                <div className="text-xs text-slate-500 mb-1">Output</div>
                <pre className="whitespace-pre-wrap text-xs bg-ink-950 rounded p-2 max-h-40 overflow-auto">{s.outputText}</pre>
              </div>
            )}
          </div>
        ))}
        {!trace.spans.length && <p className="text-sm text-slate-500">No spans</p>}
      </section>

      <div className="grid gap-4 md:grid-cols-2">
        <section className="card space-y-2">
          <h2 className="font-medium">Evaluations</h2>
          {trace.evaluations.map(e => (
            <div key={e.id} className="flex items-start justify-between gap-2 border-b border-slate-800 py-2 text-sm">
              <div>
                <div className="font-medium capitalize">{e.evalType}</div>
                <div className="text-xs text-slate-400">{e.rationale}</div>
              </div>
              <div className="text-right">
                <div>{e.score != null ? Number(e.score).toFixed(2) : '—'}</div>
                <StatusBadge value={e.passed ? 'PROCESSED' : 'FAILED'} />
              </div>
            </div>
          ))}
          {!trace.evaluations.length && <p className="text-sm text-slate-500">Waiting for eval processor…</p>}
        </section>

        <section className="card space-y-2">
          <h2 className="font-medium">Guardrails</h2>
          {trace.guardrails.map(g => (
            <div key={g.id} className="flex items-center justify-between border-b border-slate-800 py-2 text-sm">
              <div>
                <div className="font-medium">{g.ruleName}</div>
                <div className="text-xs text-slate-400">{g.category} · {g.severity}</div>
              </div>
              <StatusBadge value={g.action} />
            </div>
          ))}
          {!trace.guardrails.length && <p className="text-sm text-slate-500">Waiting for processor…</p>}
        </section>
      </div>
    </div>
  )
}
