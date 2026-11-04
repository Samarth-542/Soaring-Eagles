package in.soaringeagles.api.registrations;

import java.time.Instant;
import java.util.UUID;

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
@Table(name = "payment_attempts")
public class PaymentAttempt {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registration_id", nullable = false)
    private Registration registration;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "attempt_state", nullable = false, length = 16)
    private PaymentAttemptState state;

    @Column(name = "amount_paise", nullable = false)
    private long amountPaise;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "razorpay_order_id", length = 80)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 80)
    private String razorpayPaymentId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected PaymentAttempt() {
    }

    PaymentAttempt(Registration registration, int attemptNumber, long amountPaise, Instant now) {
        this.registration = registration;
        this.attemptNumber = attemptNumber;
        this.amountPaise = amountPaise;
        currency = "INR";
        state = PaymentAttemptState.CREATING;
        createdAt = now;
        updatedAt = now;
    }

    public void markReady(String orderId, Instant now) {
        razorpayOrderId = orderId;
        state = PaymentAttemptState.READY;
        updatedAt = now;
    }

    public void markFailed(Instant now) {
        state = PaymentAttemptState.FAILED;
        updatedAt = now;
    }

    public void markCaptured(String paymentId, Instant now) {
        razorpayPaymentId = paymentId;
        state = PaymentAttemptState.CAPTURED;
        updatedAt = now;
    }

    public UUID getId() { return id; }
    public Registration getRegistration() { return registration; }
    public int getAttemptNumber() { return attemptNumber; }
    public PaymentAttemptState getState() { return state; }
    public long getAmountPaise() { return amountPaise; }
    public String getCurrency() { return currency; }
    public String getRazorpayOrderId() { return razorpayOrderId; }
    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
