CREATE TABLE registration_email_deliveries (
    id UUID PRIMARY KEY,
    registration_id UUID NOT NULL REFERENCES event_registrations (id) ON DELETE CASCADE,
    delivery_state VARCHAR(16) NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_error VARCHAR(500),
    resend_email_id VARCHAR(120),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT registration_email_delivery_unique UNIQUE (registration_id),
    CONSTRAINT registration_email_delivery_state CHECK (
        delivery_state IN ('PENDING', 'SENDING', 'DELIVERED', 'FAILED')
    ),
    CONSTRAINT registration_email_attempt_count CHECK (attempt_count >= 0)
);

CREATE INDEX registration_email_delivery_due_idx
    ON registration_email_deliveries (delivery_state, next_attempt_at, created_at);
