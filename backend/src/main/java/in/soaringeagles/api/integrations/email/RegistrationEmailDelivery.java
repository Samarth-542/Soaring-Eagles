package in.soaringeagles.api.integrations.email;

import java.time.Instant;
import java.util.UUID;

import in.soaringeagles.api.registrations.Registration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "registration_email_deliveries")
public class RegistrationEmailDelivery {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registration_id", nullable = false, unique = true)
    private Registration registration;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_state", nullable = false, length = 16)
    private EmailDeliveryState state;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "resend_email_id", length = 120)
    private String resendEmailId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected RegistrationEmailDelivery() {
    }

    RegistrationEmailDelivery(Registration registration, Instant now) {
        this.registration = registration;
        state = EmailDeliveryState.PENDING;
        nextAttemptAt = now;
        createdAt = now;
        updatedAt = now;
    }

    void markSending(Instant now) {
        state = EmailDeliveryState.SENDING;
        attemptCount++;
        updatedAt = now;
    }

    void markDelivered(String emailId, Instant now) {
        state = EmailDeliveryState.DELIVERED;
        resendEmailId = emailId;
        lastError = null;
        updatedAt = now;
    }

    void markFailed(Instant retryAt, Instant now) {
        state = EmailDeliveryState.FAILED;
        nextAttemptAt = retryAt;
        lastError = "Email provider request failed; delivery will retry.";
        updatedAt = now;
    }

    void retryNow(Instant now) {
        state = EmailDeliveryState.PENDING;
        nextAttemptAt = now;
        lastError = null;
        updatedAt = now;
    }

    public UUID getId() { return id; }
    public Registration getRegistration() { return registration; }
    public EmailDeliveryState getState() { return state; }
    public int getAttemptCount() { return attemptCount; }
    public Instant getNextAttemptAt() { return nextAttemptAt; }
    public String getLastError() { return lastError; }
    public String getResendEmailId() { return resendEmailId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
