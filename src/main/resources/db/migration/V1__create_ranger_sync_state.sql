CREATE SCHEMA IF NOT EXISTS sync;

CREATE TABLE sync.ranger_sync_state (
    policy_id   BIGINT PRIMARY KEY,
    content_hash VARCHAR(64) NOT NULL
);

CREATE TABLE sync.audit_state (
    id INT PRIMARY KEY,
    last_sync_time TIMESTAMP NOT NULL
);
INSERT INTO sync.audit_state (id, last_sync_time)
VALUES (1, '1970-01-01 00:00:00')
ON CONFLICT (id) DO NOTHING;
