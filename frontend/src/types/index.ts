export type Role = 'ADMIN' | 'DEVELOPER' | 'VIEWER'
export type TraceStatus = 'PENDING' | 'PROCESSED' | 'BLOCKED' | 'FAILED'

export interface AuthUser {
  token: string
  userId: string
  email: string
  displayName: string
  role: Role
}

export interface Project {
  id: string
  name: string
  slug: string
  description?: string
}

export interface TraceSummary {
  id: string
  name: string
  status: TraceStatus
  model?: string
  provider?: string
  totalTokens?: number
  estimatedCostUsd?: number
  latencyMs?: number
  ingestedAt: string
}

export interface Span {
  id: string
  parentSpanId?: string
  name: string
  kind: string
  status: string
  model?: string
  inputText?: string
  outputText?: string
  inputTokens?: number
  outputTokens?: number
  latencyMs?: number
  startedAt?: string
  endedAt?: string
}

export interface Evaluation {
  id: string
  traceId: string
  evalType: string
  score?: number
  passed?: boolean
  judge: string
  rationale?: string
  createdAt: string
}

export interface GuardrailDecision {
  id: string
  traceId?: string
  ruleName: string
  category: string
  action: string
  matched: boolean
  severity: string
  createdAt: string
}

export interface TraceDetail extends TraceSummary {
  projectId: string
  inputTokens?: number
  outputTokens?: number
  userIdExt?: string
  sessionId?: string
  metadata?: Record<string, unknown>
  startedAt?: string
  endedAt?: string
  processedAt?: string
  errorMessage?: string
  spans: Span[]
  evaluations: Evaluation[]
  guardrails: GuardrailDecision[]
}

export interface DashboardMetrics {
  tracesLast24h: number
  blockedLast24h: number
  failedLast24h: number
  evalFailuresLast24h: number
  costLast24h: number
  avgLatencyMs: number
  avgQualityScore?: number
  avgSafetyScore?: number
  guardrailBlocksTotal: number
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface AlertRule {
  id: string
  name: string
  metric: string
  operator: string
  threshold: number
  windowMinutes: number
  enabled: boolean
}

export interface AlertEvent {
  id: string
  message: string
  metricValue?: number
  notified: boolean
  createdAt: string
}
