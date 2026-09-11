import { FormEvent, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

export default function LoginPage() {
  const { user, login } = useAuth()
  const [email, setEmail] = useState('admin@llmobs.local')
  const [password, setPassword] = useState('admin123')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (user) return <Navigate to="/" replace />

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      await login(email, password)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-4">
      <form onSubmit={onSubmit} className="card w-full max-w-md space-y-4">
        <div>
          <h1 className="text-xl font-semibold">Sign in</h1>
          <p className="text-sm text-slate-400">LLM Observability & Evaluation Platform</p>
        </div>
        <label className="block text-sm">
          Email
          <input className="input mt-1" value={email} onChange={e => setEmail(e.target.value)} />
        </label>
        <label className="block text-sm">
          Password
          <input className="input mt-1" type="password" value={password} onChange={e => setPassword(e.target.value)} />
        </label>
        {error && <div className="text-sm text-rose-400">{error}</div>}
        <button className="btn-primary w-full" disabled={loading}>
          {loading ? 'Signing in…' : 'Sign in'}
        </button>
        <p className="text-xs text-slate-500">Seed: admin@llmobs.local / admin123</p>
      </form>
    </div>
  )
}
