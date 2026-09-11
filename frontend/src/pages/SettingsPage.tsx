import { FormEvent, useEffect, useState } from 'react'
import { api } from '../api/client'
import { useAuth } from '../contexts/AuthContext'
import { useProject } from '../contexts/ProjectContext'

interface KeyRow {
  id: string
  name: string
  keyPrefix: string
  createdAt: string
  lastUsedAt?: string
}

export default function SettingsPage() {
  const { user } = useAuth()
  const { project } = useProject()
  const [keys, setKeys] = useState<KeyRow[]>([])
  const [name, setName] = useState('ci-key')
  const [createdRaw, setCreatedRaw] = useState('')

  const refresh = async () => {
    if (!project) return
    const list = await api<KeyRow[]>(`/api/v1/projects/${project.id}/api-keys`)
    setKeys(list)
  }

  useEffect(() => { void refresh() }, [project])

  async function onCreate(e: FormEvent) {
    e.preventDefault()
    if (!project) return
    const res = await api<{ rawKey: string }>(`/api/v1/projects/${project.id}/api-keys`, {
      method: 'POST',
      body: JSON.stringify({ name }),
    })
    setCreatedRaw(res.rawKey)
    await refresh()
  }

  async function revoke(id: string) {
    if (!project) return
    await api(`/api/v1/projects/${project.id}/api-keys/${id}`, { method: 'DELETE' })
    await refresh()
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold">Settings</h1>
      <div className="card text-sm space-y-1">
        <div><span className="text-slate-400">User:</span> {user?.displayName} ({user?.email})</div>
        <div><span className="text-slate-400">Role:</span> {user?.role}</div>
        <div><span className="text-slate-400">Project:</span> {project?.name} <code className="text-xs text-slate-500">{project?.id}</code></div>
      </div>

      <section className="card space-y-3">
        <h2 className="font-medium">API keys</h2>
        <form onSubmit={onCreate} className="flex gap-2">
          <input className="input" value={name} onChange={e => setName(e.target.value)} />
          <button className="btn-primary">Create</button>
        </form>
        {createdRaw && (
          <div className="rounded-lg bg-amber-500/10 border border-amber-500/30 p-3 text-sm">
            Copy now (shown once): <code className="break-all">{createdRaw}</code>
          </div>
        )}
        <ul className="text-sm space-y-2">
          {keys.map(k => (
            <li key={k.id} className="flex justify-between items-center border-b border-slate-800 py-2">
              <div>
                <div className="font-medium">{k.name}</div>
                <div className="text-xs text-slate-500">{k.keyPrefix}…</div>
              </div>
              <button className="btn-ghost text-xs" onClick={() => revoke(k.id)}>Revoke</button>
            </li>
          ))}
        </ul>
      </section>
    </div>
  )
}
