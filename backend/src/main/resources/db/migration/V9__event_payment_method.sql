ALTER TABLE events
    ADD COLUMN payment_method VARCHAR(20) NOT NULL DEFAULT 'RAZORPAY';

ALTER TABLE events
    ADD COLUMN google_form_link VARCHAR(2048);

ALTER TABLE events
    ADD COLUMN google_form_qr_url VARCHAR(2048);

ALTER TABLE events
    ADD CONSTRAINT events_payment_method_check
    CHECK (payment_method IN ('RAZORPAY', 'GOOGLE_FORM'));
