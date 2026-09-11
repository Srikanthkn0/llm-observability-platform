CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(320) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    display_name    VARCHAR(200) NOT NULL,
    role            VARCHAR(32)  NOT NULL CHECK (role IN ('ADMIN', 'DEVELOPER', 'VIEWER')),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE projects (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(200) NOT NULL,
    slug            VARCHAR(120) NOT NULL UNIQUE,
    description     TEXT,
    owner_id        UUID NOT NULL REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE project_memberships (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id      UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role            VARCHAR(32) NOT NULL CHECK (role IN ('ADMIN', 'DEVELOPER', 'VIEWER')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, user_id)
);

CREATE TABLE api_keys (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id      UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name            VARCHAR(120) NOT NULL,
    key_prefix      VARCHAR(16)  NOT NULL,
    key_hash        VARCHAR(255) NOT NULL,
    last_used_at    TIMESTAMPTZ,
    revoked_at      TIMESTAMPTZ,
    created_by      UUID REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_api_keys_prefix ON api_keys(key_prefix) WHERE revoked_at IS NULL;

CREATE TABLE traces (
    id              UUID PRIMARY KEY,
    project_id      UUID NOT NULL REFERENCES projects(id),
    name            VARCHAR(500),
    status          VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    model           VARCHAR(120),
    provider        VARCHAR(64),
    input_tokens    INTEGER DEFAULT 0,
    output_tokens   INTEGER DEFAULT 0,
    total_tokens    INTEGER DEFAULT 0,
    estimated_cost_usd NUMERIC(12, 6) DEFAULT 0,
    latency_ms      INTEGER,
    user_id_ext     VARCHAR(200),
    session_id      VARCHAR(200),
    metadata_json   JSONB DEFAULT '{}'::jsonb,
    started_at      TIMESTAMPTZ,
    ended_at        TIMESTAMPTZ,
    ingested_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at    TIMESTAMPTZ,
    error_message   TEXT
);
CREATE INDEX idx_traces_project_ingested ON traces(project_id, ingested_at DESC);
CREATE INDEX idx_traces_status ON traces(status);

CREATE TABLE spans (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trace_id        UUID NOT NULL REFERENCES traces(id) ON DELETE CASCADE,
    parent_span_id  UUID,
    name            VARCHAR(500) NOT NULL,
    kind            VARCHAR(64) NOT NULL DEFAULT 'LLM',
    status          VARCHAR(32) NOT NULL DEFAULT 'OK',
    model           VARCHAR(120),
    input_text      TEXT,
    output_text     TEXT,
    input_tokens    INTEGER DEFAULT 0,
    output_tokens   INTEGER DEFAULT 0,
    latency_ms      INTEGER,
    attributes_json JSONB DEFAULT '{}'::jsonb,
    started_at      TIMESTAMPTZ,
    ended_at        TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_spans_trace ON spans(trace_id);

CREATE TABLE evaluations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trace_id        UUID NOT NULL REFERENCES traces(id) ON DELETE CASCADE,
    project_id      UUID NOT NULL REFERENCES projects(id),
    eval_type       VARCHAR(64) NOT NULL,
    score           NUMERIC(6, 4),
    passed          BOOLEAN,
    judge           VARCHAR(64) NOT NULL DEFAULT 'deterministic',
    rationale       TEXT,
    details_json    JSONB DEFAULT '{}'::jsonb,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_evaluations_trace ON evaluations(trace_id);
CREATE INDEX idx_evaluations_project ON evaluations(project_id, created_at DESC);

CREATE TABLE guardrail_decisions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trace_id        UUID REFERENCES traces(id) ON DELETE SET NULL,
    project_id      UUID NOT NULL REFERENCES projects(id),
    rule_name       VARCHAR(120) NOT NULL,
    category        VARCHAR(64) NOT NULL,
    action          VARCHAR(32) NOT NULL CHECK (action IN ('ALLOW', 'FLAG', 'BLOCK')),
    matched         BOOLEAN NOT NULL DEFAULT FALSE,
    severity        VARCHAR(32) NOT NULL DEFAULT 'INFO',
    details_json    JSONB DEFAULT '{}'::jsonb,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_guardrail_project ON guardrail_decisions(project_id, created_at DESC);

CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_user_id   UUID REFERENCES users(id),
    project_id      UUID REFERENCES projects(id),
    action          VARCHAR(120) NOT NULL,
    resource_type   VARCHAR(64),
    resource_id     VARCHAR(120),
    details_json    JSONB DEFAULT '{}'::jsonb,
    ip_address      VARCHAR(64),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_audit_created ON audit_logs(created_at DESC);

CREATE TABLE alert_rules (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id      UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name            VARCHAR(200) NOT NULL,
    metric          VARCHAR(64) NOT NULL,
    operator        VARCHAR(8) NOT NULL,
    threshold       NUMERIC(14, 4) NOT NULL,
    window_minutes  INTEGER NOT NULL DEFAULT 15,
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    slack_webhook   VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE alert_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_id         UUID NOT NULL REFERENCES alert_rules(id) ON DELETE CASCADE,
    project_id      UUID NOT NULL REFERENCES projects(id),
    message         TEXT NOT NULL,
    metric_value    NUMERIC(14, 4),
    notified        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
