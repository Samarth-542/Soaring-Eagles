CREATE TABLE event_registrations (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL REFERENCES events (id),
    idempotency_key UUID NOT NULL,
    registration_type VARCHAR(20) NOT NULL,
    registration_state VARCHAR(24) NOT NULL,
    registration_fee_inr NUMERIC(10, 2) NOT NULL,
    individual_name VARCHAR(120),
    individual_phone VARCHAR(30),
    individual_email VARCHAR(254),
    individual_year INTEGER,
    individual_department VARCHAR(160),
    individual_institution VARCHAR(200),
    team_name VARCHAR(120),
    captain_name VARCHAR(120),
    captain_phone VARCHAR(30),
    captain_email VARCHAR(254),
    hold_expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT event_registrations_idempotency UNIQUE (event_id, idempotency_key),
    CONSTRAINT event_registrations_type CHECK (registration_type IN ('INDIVIDUAL', 'TEAM')),
    CONSTRAINT event_registrations_fee CHECK (registration_fee_inr >= 0),
    CONSTRAINT event_registrations_state CHECK (
        registration_state IN ('PENDING_PAYMENT', 'CONFIRMED', 'PAYMENT_FAILED', 'EXPIRED', 'PAYMENT_REVIEW')
    ),
    CONSTRAINT event_registrations_individual_fields CHECK (
        (registration_type = 'INDIVIDUAL'
            AND individual_name IS NOT NULL
            AND individual_phone IS NOT NULL
            AND individual_email IS NOT NULL
            AND individual_year BETWEEN 1 AND 4
            AND individual_department IS NOT NULL
            AND individual_institution IS NOT NULL
            AND team_name IS NULL
            AND captain_name IS NULL
            AND captain_phone IS NULL
            AND captain_email IS NULL)
        OR
        (registration_type = 'TEAM'
            AND individual_name IS NULL
            AND individual_phone IS NULL
            AND individual_email IS NULL
            AND individual_year IS NULL
            AND individual_department IS NULL
            AND individual_institution IS NULL
            AND team_name IS NOT NULL
            AND captain_name IS NOT NULL
            AND captain_phone IS NOT NULL
            AND captain_email IS NOT NULL)
    ),
    CONSTRAINT event_registrations_hold_state CHECK (
        (registration_state IN ('PENDING_PAYMENT', 'PAYMENT_REVIEW') AND hold_expires_at IS NOT NULL)
        OR registration_state NOT IN ('PENDING_PAYMENT', 'PAYMENT_REVIEW')
    )
);

CREATE INDEX event_registrations_event_state_hold_idx
    ON event_registrations (event_id, registration_state, hold_expires_at);

CREATE TABLE registration_team_members (
    registration_id UUID NOT NULL REFERENCES event_registrations (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    name VARCHAR(120) NOT NULL,
    study_year INTEGER NOT NULL,
    department VARCHAR(160) NOT NULL,
    institution VARCHAR(200) NOT NULL,
    email VARCHAR(254) NOT NULL,
    PRIMARY KEY (registration_id, position),
    CONSTRAINT registration_team_member_year CHECK (study_year BETWEEN 1 AND 4)
);

CREATE TABLE payment_attempts (
    id UUID PRIMARY KEY,
    registration_id UUID NOT NULL REFERENCES event_registrations (id) ON DELETE CASCADE,
    attempt_number INTEGER NOT NULL,
    attempt_state VARCHAR(16) NOT NULL,
    amount_paise BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    razorpay_order_id VARCHAR(80),
    razorpay_payment_id VARCHAR(80),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT payment_attempts_number UNIQUE (registration_id, attempt_number),
    CONSTRAINT payment_attempts_order_id UNIQUE (razorpay_order_id),
    CONSTRAINT payment_attempts_payment_id UNIQUE (razorpay_payment_id),
    CONSTRAINT payment_attempts_state CHECK (attempt_state IN ('CREATING', 'READY', 'FAILED', 'CAPTURED')),
    CONSTRAINT payment_attempts_amount CHECK (amount_paise > 0),
    CONSTRAINT payment_attempts_currency CHECK (currency = 'INR')
);

CREATE INDEX payment_attempts_registration_created_idx
    ON payment_attempts (registration_id, created_at DESC);

CREATE TABLE processed_payment_webhook_events (
    provider_event_id VARCHAR(120) PRIMARY KEY,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processing_state VARCHAR(16) NOT NULL,
    CONSTRAINT processed_payment_webhook_state CHECK (processing_state IN ('PROCESSED', 'REVIEW'))
);
