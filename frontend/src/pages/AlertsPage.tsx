import { FormEvent, useEffect, useState } from 'react'
import { api } from '../api/client'
import { useProject } from '../contexts/ProjectContext'
import type { AlertEvent, AlertRule, PageResponse } from '../types'

export default function AlertsPage() {
  const { project } = useProject()
  const [rules, setRules] = useState<AlertRule[]>([])
  const [events, setEvents] = useState<AlertEvent[]>([])
  const [name, setName] = useState('Latency spike')
  const [metric, setMetric] = useState('avg_latency_ms')
  const [threshold, setThreshold] = useState('2000')

  const refresh = async () => {
    if (!project) return
    const [r, e] = await Promise.all([
      api<AlertRule[]>(`/api/v1/projects/${project.id}/alerts/rules`),
      api<PageResponse<AlertEvent>>(`/api/v1/projects/${project.id}/alerts/events`),
    ])
    setRules(r)
    setEvents(e.content)
  }

  useEffect(() => { void refresh() }, [project])

  async function onCreate(ev: FormEvent) {
    ev.preventDefault()
    if (!project) return
    await api(`/api/v1/projects/${project.id}/alerts/rules`, {
      method: 'POST',
      body: JSON.stringify({
        name,
        metric,
        operator: '>',
        threshold: Number(threshold),
        windowMinutes: 15,
      }),
    })
    await refresh()
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold">Alerts</h1>
      <form onSubmit={onCreate} className="card grid gap-3 md:grid-cols-4 items-end">
        <label className="text-sm">Name<input className="input mt-1" value={name} onChange={e => setName(e.target.value)} /></label>
        <label className="text-sm">Metric
          <select className="input mt-1" value={metric} onChange={e => setMetric(e.target.value)}>
            <option value="avg_latency_ms">avg_latency_ms</option>
            <option value="cost_usd">cost_usd</option>
            <option value="trace_count">trace_count</option>
            <option value="blocked_count">blocked_count</option>
          </select>
        </label>
        <label className="text-sm">Threshold &gt;<input className="input mt-1" value={threshold} onChange={e => setThreshold(e.target.value)} /></label>
        <button className="btn-primary">Add rule</button>
      </form>

      <div className="grid gap-4 md:grid-cols-2">
        <section className="card">
          <h2 className="font-medium mb-2">Rules</h2>
          <ul className="space-y-2 text-sm">
            {rules.map(r => (
              <li key={r.id} className="border-b border-slate-800 py-2">
                <div className="font-medium">{r.name}</div>
                <div className="text-slate-400">{r.metric} {r.operator} {r.threshold} / {r.windowMinutes}m</div>
              </li>
            ))}
            {!rules.length && <li className="text-slate-500">No rules</li>}
          </ul>
        </section>
        <section className="card">
          <h2 className="font-medium mb-2">Events</h2>
          <ul className="space-y-2 text-sm">
            {events.map(e => (
              <li key={e.id} className="border-b border-slate-800 py-2">
                <div>{e.message}</div>
                <div className="text-xs text-slate-500">{new Date(e.createdAt).toLocaleString()}</div>
              </li>
            ))}
            {!events.length && <li className="text-slate-500">No alert events yet (Slack webhook is stubbed to logs)</li>}
          </ul>
        </section>
      </div>
    </div>
  )
}
