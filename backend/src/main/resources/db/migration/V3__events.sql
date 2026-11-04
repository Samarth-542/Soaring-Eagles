CREATE TABLE events (
    id UUID PRIMARY KEY,
    title VARCHAR(180) NOT NULL,
    description TEXT NOT NULL,
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
    venue VARCHAR(240) NOT NULL,
    poster_url VARCHAR(2048),
    eligibility TEXT NOT NULL,
    fee_inr NUMERIC(10, 2) NOT NULL,
    registration_type VARCHAR(20) NOT NULL,
    registration_deadline TIMESTAMP WITH TIME ZONE NOT NULL,
    capacity INTEGER NOT NULL,
    minimum_team_size INTEGER,
    maximum_team_size INTEGER,
    publication_state VARCHAR(20) NOT NULL,
    scheduled_publish_at TIMESTAMP WITH TIME ZONE,
    archive_story TEXT,
    archive_results TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT events_fee_nonnegative CHECK (fee_inr >= 0),
    CONSTRAINT events_capacity_positive CHECK (capacity > 0),
    CONSTRAINT events_valid_dates CHECK (ends_at > starts_at),
    CONSTRAINT events_registration_deadline CHECK (registration_deadline <= starts_at),
    CONSTRAINT events_registration_type CHECK (registration_type IN ('INDIVIDUAL', 'TEAM')),
    CONSTRAINT events_valid_team_size CHECK (
        (registration_type = 'INDIVIDUAL' AND minimum_team_size IS NULL AND maximum_team_size IS NULL)
        OR
        (registration_type = 'TEAM' AND minimum_team_size >= 1 AND maximum_team_size >= minimum_team_size)
    ),
    CONSTRAINT events_valid_publication_state CHECK (
        publication_state IN ('DRAFT', 'SCHEDULED', 'PUBLISHED', 'CLOSED')
    ),
    CONSTRAINT events_scheduled_publication CHECK (
        (publication_state = 'SCHEDULED' AND scheduled_publish_at IS NOT NULL)
        OR
        (publication_state <> 'SCHEDULED' AND scheduled_publish_at IS NULL)
    )
);

CREATE INDEX events_publication_state_start_idx ON events (publication_state, starts_at);

CREATE TABLE event_archive_photos (
    event_id UUID NOT NULL REFERENCES events (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    url VARCHAR(2048) NOT NULL,
    PRIMARY KEY (event_id, position)
);
