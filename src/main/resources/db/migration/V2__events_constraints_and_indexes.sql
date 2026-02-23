-- V2__events_constraints_and_indexes.sql
-- Add constraints/indexes for stronger data integrity and query performance.

-- Prevent accidental duplicate creation (e.g., double submit)
CREATE UNIQUE INDEX IF NOT EXISTS uq_events_title_starts_at
    ON events (title, starts_at);

-- Common query pattern: sort/filter by starts time
CREATE INDEX IF NOT EXISTS idx_events_starts_at
    ON events (starts_at);