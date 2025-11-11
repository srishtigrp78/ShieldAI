-- ShieldAI Database Schema Migration V1
-- Creates detection and sync tracking tables

-- Detection Entity Table
CREATE TABLE IF NOT EXISTS detection_entity (
    id BIGSERIAL PRIMARY KEY,
    candidate_id VARCHAR(255) NOT NULL,
    tool_name VARCHAR(255) NOT NULL,
    tool_type VARCHAR(100),
    timestamp VARCHAR(255) NOT NULL,
    os_info TEXT,
    process_details TEXT,
    confidence DOUBLE PRECISION,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Offline Detection Sync Table
CREATE TABLE IF NOT EXISTS offline_detection_sync (
    id BIGSERIAL PRIMARY KEY,
    detection_data JSONB NOT NULL,
    sync_status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    synced_at TIMESTAMP,
    retry_count INTEGER DEFAULT 0,
    last_error TEXT
);

-- Notification Entity Table
CREATE TABLE IF NOT EXISTS notification_entity (
    id BIGSERIAL PRIMARY KEY,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for Performance
CREATE INDEX IF NOT EXISTS idx_detection_candidate_id ON detection_entity(candidate_id);
CREATE INDEX IF NOT EXISTS idx_detection_tool_name ON detection_entity(tool_name);
CREATE INDEX IF NOT EXISTS idx_detection_timestamp ON detection_entity(timestamp);
CREATE INDEX IF NOT EXISTS idx_detection_created_at ON detection_entity(created_at);

CREATE INDEX IF NOT EXISTS idx_offline_sync_status ON offline_detection_sync(sync_status);
CREATE INDEX IF NOT EXISTS idx_offline_sync_created_at ON offline_detection_sync(created_at);

CREATE INDEX IF NOT EXISTS idx_notification_is_read ON notification_entity(is_read);
CREATE INDEX IF NOT EXISTS idx_notification_created_at ON notification_entity(created_at);