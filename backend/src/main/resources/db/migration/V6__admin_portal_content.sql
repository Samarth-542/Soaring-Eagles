CREATE TABLE admin_accounts (
    id UUID PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT admin_accounts_email_unique UNIQUE (email)
);

CREATE TABLE admin_audit_events (
    id UUID PRIMARY KEY,
    admin_id UUID REFERENCES admin_accounts (id) ON DELETE SET NULL,
    action VARCHAR(80) NOT NULL,
    resource_type VARCHAR(80) NOT NULL,
    resource_id VARCHAR(120),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX admin_audit_events_created_idx ON admin_audit_events (created_at DESC);
CREATE INDEX admin_audit_events_admin_created_idx ON admin_audit_events (admin_id, created_at DESC);

CREATE TABLE team_groups (
    id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    label VARCHAR(80) NOT NULL,
    position INTEGER NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT team_groups_position_unique UNIQUE (position)
);

CREATE TABLE team_members (
    id UUID PRIMARY KEY,
    group_id VARCHAR(50) NOT NULL REFERENCES team_groups (id),
    name VARCHAR(120) NOT NULL,
    role VARCHAR(180) NOT NULL,
    image_url VARCHAR(2048) NOT NULL,
    linkedin_url VARCHAR(2048),
    instagram_url VARCHAR(2048),
    x_url VARCHAR(2048),
    position INTEGER NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT team_members_group_position_unique UNIQUE (group_id, position)
);

CREATE INDEX team_members_public_order_idx ON team_members (published, group_id, position);

CREATE TABLE projects (
    id UUID PRIMARY KEY,
    title VARCHAR(180) NOT NULL,
    project_year INTEGER NOT NULL,
    description TEXT NOT NULL,
    image_url VARCHAR(2048) NOT NULL,
    position INTEGER NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT projects_year_valid CHECK (project_year BETWEEN 1900 AND 2200),
    CONSTRAINT projects_position_unique UNIQUE (position)
);

CREATE INDEX projects_public_order_idx ON projects (published, project_year DESC, position);

CREATE TABLE project_tags (
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    tag VARCHAR(80) NOT NULL,
    PRIMARY KEY (project_id, position),
    CONSTRAINT project_tags_value_unique UNIQUE (project_id, tag)
);

CREATE TABLE journey_milestones (
    id UUID PRIMARY KEY,
    milestone_year INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    image_url VARCHAR(2048) NOT NULL,
    position INTEGER NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT journey_milestones_year_valid CHECK (milestone_year BETWEEN 1900 AND 2200),
    CONSTRAINT journey_milestones_position_unique UNIQUE (position)
);

CREATE INDEX journey_milestones_public_order_idx
    ON journey_milestones (published, milestone_year DESC, position);

CREATE TABLE contact_messages (
    id UUID PRIMARY KEY,
    sender_name VARCHAR(120) NOT NULL,
    sender_email VARCHAR(254) NOT NULL,
    subject VARCHAR(180) NOT NULL,
    message TEXT NOT NULL,
    state VARCHAR(20) NOT NULL DEFAULT 'NEW',
    attachment_storage_key VARCHAR(512),
    attachment_file_name VARCHAR(255),
    attachment_content_type VARCHAR(120),
    attachment_size_bytes BIGINT,
    replied_at TIMESTAMP WITH TIME ZONE,
    replied_by_admin_id UUID REFERENCES admin_accounts (id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT contact_messages_state_valid
        CHECK (state IN ('NEW', 'IN_PROGRESS', 'REPLIED', 'ARCHIVED')),
    CONSTRAINT contact_messages_attachment_complete CHECK (
        (attachment_storage_key IS NULL
            AND attachment_file_name IS NULL
            AND attachment_content_type IS NULL
            AND attachment_size_bytes IS NULL)
        OR
        (attachment_storage_key IS NOT NULL
            AND attachment_file_name IS NOT NULL
            AND attachment_content_type IS NOT NULL
            AND attachment_size_bytes > 0)
    ),
    CONSTRAINT contact_messages_attachment_size_valid
        CHECK (attachment_size_bytes IS NULL OR attachment_size_bytes <= 10485760)
);

CREATE INDEX contact_messages_state_created_idx ON contact_messages (state, created_at DESC);

CREATE TABLE media_assets (
    id UUID PRIMARY KEY,
    storage_bucket VARCHAR(80) NOT NULL,
    storage_key VARCHAR(512) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    size_bytes BIGINT NOT NULL,
    visibility VARCHAR(10) NOT NULL,
    uploaded_by_admin_id UUID REFERENCES admin_accounts (id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT media_assets_storage_location_unique UNIQUE (storage_bucket, storage_key),
    CONSTRAINT media_assets_size_positive CHECK (size_bytes > 0),
    CONSTRAINT media_assets_visibility_valid CHECK (visibility IN ('PUBLIC', 'PRIVATE'))
);

CREATE INDEX media_assets_created_idx ON media_assets (created_at DESC);

CREATE TABLE site_settings (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value TEXT NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by_admin_id UUID REFERENCES admin_accounts (id) ON DELETE SET NULL,
    version BIGINT NOT NULL DEFAULT 0
);
