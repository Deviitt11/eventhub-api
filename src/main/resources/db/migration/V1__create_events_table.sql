-- V1__create_events_table.sql
-- Initial schema for Events persistence (PostgreSQL)

CREATE TABLE IF NOT EXISTS events (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    starts_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
    );

-- Basic sanity constraint: if ends_at is present, it must be after starts_at
ALTER TABLE events
    ADD CONSTRAINT chk_events_ends_after_starts
        CHECK (ends_at IS NULL OR ends_at > starts_at);
