package in.soaringeagles.api.registrations;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import in.soaringeagles.api.events.Event;
import in.soaringeagles.api.events.RegistrationType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "event_registrations")
public class Registration {

    @Id
    @GeneratedValue
    private UUID id;

    @jakarta.persistence.ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "idempotency_key", nullable = false)
    private UUID idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_type", nullable = false, length = 20)
    private RegistrationType registrationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_state", nullable = false, length = 24)
    private RegistrationState state;

    @Column(name = "registration_fee_inr", nullable = false, precision = 10, scale = 2)
    private BigDecimal feeInr;

    @Column(name = "individual_name", length = 120)
    private String individualName;

    @Column(name = "individual_phone", length = 30)
    private String individualPhone;

    @Column(name = "individual_email", length = 254)
    private String individualEmail;

    @Column(name = "individual_year")
    private Integer individualYear;

    @Column(name = "individual_department", length = 160)
    private String individualDepartment;

    @Column(name = "individual_institution", length = 200)
    private String individualInstitution;

    @Column(name = "team_name", length = 120)
    private String teamName;

    @Column(name = "captain_name", length = 120)
    private String captainName;

    @Column(name = "captain_phone", length = 30)
    private String captainPhone;

    @Column(name = "captain_email", length = 254)
    private String captainEmail;

    @ElementCollection
    @CollectionTable(
            name = "registration_team_members",
            joinColumns = @JoinColumn(name = "registration_id", nullable = false))
    @OrderColumn(name = "position")
    private List<RegistrationTeamMember> members = new ArrayList<>();

    @Column(name = "hold_expires_at")
    private Instant holdExpiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected Registration() {
    }

    Registration(Event event, UUID idempotencyKey, RegistrationRequest request, Instant now, Instant holdExpiresAt) {
        this.event = event;
        this.idempotencyKey = idempotencyKey;
        registrationType = event.getRegistrationType();
        feeInr = event.getFeeInr();
        state = RegistrationState.PENDING_PAYMENT;
        this.holdExpiresAt = holdExpiresAt;
        createdAt = now;
        updatedAt = now;
        if (registrationType == RegistrationType.INDIVIDUAL) {
            individualName = trim(request.name());
            individualPhone = trim(request.phone());
            individualEmail = trim(request.email());
            individualYear = request.year().value();
            individualDepartment = trim(request.department());
            individualInstitution = trim(request.institution());
        } else {
            teamName = trim(request.teamName());
            captainName = trim(request.captainName());
            captainPhone = trim(request.captainPhone());
            captainEmail = trim(request.captainEmail());
            request.members().forEach(member -> members.add(new RegistrationTeamMember(member)));
        }
    }

    void updateDetails(RegistrationRequest request, Instant now) {
        members.clear();
        if (registrationType == RegistrationType.INDIVIDUAL) {
            individualName = trim(request.name());
            individualPhone = trim(request.phone());
            individualEmail = trim(request.email());
            individualYear = request.year().value();
            individualDepartment = trim(request.department());
            individualInstitution = trim(request.institution());
        } else {
            teamName = trim(request.teamName());
            captainName = trim(request.captainName());
            captainPhone = trim(request.captainPhone());
            captainEmail = trim(request.captainEmail());
            request.members().forEach(member -> members.add(new RegistrationTeamMember(member)));
        }
        updatedAt = now;
    }

    void retry(Instant now, Instant newHoldExpiresAt) {
        state = RegistrationState.PENDING_PAYMENT;
        holdExpiresAt = newHoldExpiresAt;
        updatedAt = now;
    }

    public void confirm(Instant now) {
        state = RegistrationState.CONFIRMED;
        holdExpiresAt = null;
        updatedAt = now;
    }

    public void fail(Instant now) {
        state = RegistrationState.PAYMENT_FAILED;
        holdExpiresAt = null;
        updatedAt = now;
    }

    void expire(Instant now) {
        if (state == RegistrationState.PENDING_PAYMENT) {
            state = RegistrationState.EXPIRED;
            holdExpiresAt = null;
            updatedAt = now;
        }
    }

    public void needsPaymentReview(Instant now) {
        state = RegistrationState.PAYMENT_REVIEW;
        holdExpiresAt = now;
        updatedAt = now;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    public UUID getId() { return id; }
    public Event getEvent() { return event; }
    public UUID getIdempotencyKey() { return idempotencyKey; }
    public RegistrationType getRegistrationType() { return registrationType; }
    public RegistrationState getState() { return state; }
    public BigDecimal getFeeInr() { return feeInr; }
    public String getIndividualName() { return individualName; }
    public String getIndividualPhone() { return individualPhone; }
    public String getIndividualEmail() { return individualEmail; }
    public Integer getIndividualYear() { return individualYear; }
    public String getIndividualDepartment() { return individualDepartment; }
    public String getIndividualInstitution() { return individualInstitution; }
    public String getTeamName() { return teamName; }
    public String getCaptainName() { return captainName; }
    public String getCaptainPhone() { return captainPhone; }
    public String getCaptainEmail() { return captainEmail; }
    public List<RegistrationTeamMember> getMembers() { return List.copyOf(members); }
    public Instant getHoldExpiresAt() { return holdExpiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
