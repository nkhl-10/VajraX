-- VAJRAX server schema v1: accounts, sessions, sync, template library, audit.

CREATE TABLE users (
    id                  UUID PRIMARY KEY,
    email               TEXT        NOT NULL,
    email_normalized    TEXT        NOT NULL UNIQUE,
    password_hash       TEXT        NOT NULL,
    display_name        TEXT        NOT NULL DEFAULT '',
    role                TEXT        NOT NULL DEFAULT 'user' CHECK (role IN ('user', 'admin')),
    -- Bumped on "sign out everywhere", password change and reset: older access tokens stop working.
    token_version       INT         NOT NULL DEFAULT 0,
    email_verified_at   TIMESTAMPTZ,
    failed_logins       INT         NOT NULL DEFAULT 0,
    locked_until        TIMESTAMPTZ,
    -- Sync tombstones up to this version were purged; older cursors must pull everything again.
    sync_purged_version BIGINT      NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY,
    user_id     UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    -- All tokens rotated from one sign-in share a family; reuse of an old one revokes the family.
    family_id   UUID        NOT NULL,
    token_hash  BYTEA       NOT NULL UNIQUE,
    device_name TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked_at  TIMESTAMPTZ,
    replaced_by UUID
);
CREATE INDEX refresh_tokens_user ON refresh_tokens (user_id);
CREATE INDEX refresh_tokens_family ON refresh_tokens (family_id);

CREATE TABLE password_reset_tokens (
    token_hash BYTEA PRIMARY KEY,
    user_id    UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    used_at    TIMESTAMPTZ
);
CREATE INDEX password_reset_tokens_user ON password_reset_tokens (user_id);

-- One row per synced record of an account. The payload is stored as the app sent it.
CREATE SEQUENCE sync_version_seq;
CREATE TABLE sync_records (
    user_id           UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    entity            TEXT        NOT NULL,
    entity_id         TEXT        NOT NULL,
    payload           JSONB,
    deleted           BOOLEAN     NOT NULL DEFAULT false,
    schema_version    INT         NOT NULL DEFAULT 1,
    client_updated_at BIGINT      NOT NULL,
    device_id         TEXT        NOT NULL,
    server_version    BIGINT      NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, entity, entity_id)
);
CREATE INDEX sync_records_pull ON sync_records (user_id, server_version);

-- Public template library (managed by admins, read by every app).
CREATE TABLE library_templates (
    id         TEXT PRIMARY KEY,
    payload    JSONB       NOT NULL,
    sort_order INT         NOT NULL DEFAULT 0,
    published  BOOLEAN     NOT NULL DEFAULT true,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE library_meta (
    id         INT PRIMARY KEY CHECK (id = 1),
    version    TEXT        NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Important account events. No foreign key: the trail outlives a deleted account (id only, no email).
CREATE TABLE audit_log (
    id      BIGSERIAL PRIMARY KEY,
    user_id UUID,
    action  TEXT        NOT NULL,
    ip_hash TEXT,
    detail  JSONB,
    at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX audit_log_user ON audit_log (user_id, at);
