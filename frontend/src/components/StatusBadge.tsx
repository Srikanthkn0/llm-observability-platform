import type { TraceStatus } from '../types'

const colors: Record<string, string> = {
  PENDING: 'bg-amber-500/20 text-amber-300',
  PROCESSED: 'bg-emerald-500/20 text-emerald-300',
  BLOCKED: 'bg-rose-500/20 text-rose-300',
  FAILED: 'bg-red-500/20 text-red-300',
  ALLOW: 'bg-emerald-500/20 text-emerald-300',
  FLAG: 'bg-amber-500/20 text-amber-300',
  BLOCK: 'bg-rose-500/20 text-rose-300',
}

export default function StatusBadge({ value }: { value: TraceStatus | string }) {
  return <span className={`badge ${colors[value] || 'bg-slate-700 text-slate-200'}`}>{value}</span>
}
