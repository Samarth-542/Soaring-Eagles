package in.soaringeagles.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.soaringeagles.api.integrations.payment.PaymentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(RegistrationPaymentIntegrationTest.TestConfigurationBeans.class)
class RegistrationPaymentIntegrationTest {

    private static final String VALID_EVENT = """
            {
              "title": "Team Flight Challenge",
              "description": "A team event for testing registration and payment.",
              "startsAt": "2031-02-10T09:00:00+05:30",
              "endsAt": "2031-02-10T17:00:00+05:30",
              "venue": "SIT, Tumakuru",
              "posterUrl": null,
              "eligibility": "Open to students.",
              "feeInr": 250.00,
              "paymentMethod": "RAZORPAY",
              "registrationType": "TEAM",
              "registrationDeadline": "2031-02-08T23:59:00+05:30",
              "capacity": 1,
              "minimumTeamSize": 2,
              "maximumTeamSize": 4,
              "archiveStory": null,
              "archiveResults": null,
              "archivePhotos": []
            }
            """;

    private static final String TEAM_REGISTRATION = """
            {
              "teamName": "Northwind",
              "captainName": "Aarav Rao",
              "captainPhone": "+919876543210",
              "captainEmail": "captain@example.test",
              "members": [{
                "name": "Diya Sen",
                "year": "SECOND",
                "department": "Aerospace Engineering",
                "institution": "SIT",
                "email": "member@example.test"
              }]
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MutableClock clock;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetClock() {
        clock.setInstant(Instant.parse("2026-11-03T00:00:00Z"));
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestAdminCredentials.register(registry);
        registry.add("app.payment.razorpay.webhook-secret", () -> "test-webhook-secret");
        registry.add("app.registration.hold-duration", () -> "PT10M");
    }

    @Test
    void paymentHoldsOneTeamSlotAndOnlyVerifiedWebhookConfirms() throws Exception {
        String eventId = createPublishedEvent(VALID_EVENT);
        String idempotencyKey = "7732c2d2-37b4-4a98-a53c-8a344af2f217";

        String created = mockMvc.perform(post("/api/v1/events/{eventId}/registrations", eventId)
                        .with(csrf())
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TEAM_REGISTRATION))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.amountPaise").value(25000))
                .andExpect(jsonPath("$.razorpayOrderId").exists())
                .andExpect(jsonPath("$.captainEmail").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String registrationId = read(created, "registrationId");
        String orderId = read(created, "razorpayOrderId");

        mockMvc.perform(get("/api/v1/events/{eventId}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remainingCapacity").value(0))
                .andExpect(jsonPath("$.registrationWindowOpen").value(false));

        mockMvc.perform(post("/api/v1/events/{eventId}/registrations", eventId)
                        .with(csrf())
                        .header("Idempotency-Key", "bf7bea45-3ceb-475a-80dd-33150dcac15b")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TEAM_REGISTRATION))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/v1/registrations/{registrationId}/status", registrationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.captainEmail").doesNotExist())
                .andExpect(jsonPath("$.teamName").doesNotExist());

        String captured = capturedPayment(orderId, 25000);
        mockMvc.perform(post("/api/v1/payments/razorpay/webhook")
                        .header("x-razorpay-event-id", "evt_capture_team_01")
                        .header("x-razorpay-signature", "test-signature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(captured))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/payments/razorpay/webhook")
                        .header("x-razorpay-event-id", "evt_capture_team_01")
                        .header("x-razorpay-signature", "test-signature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(captured))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/registrations/{registrationId}/status", registrationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.retryAllowed").value(false));

        mockMvc.perform(post("/api/v1/admin/registrations/{registrationId}/email/retry", registrationId)
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void failedPaymentReleasesCapacityAndAllowsRetry() throws Exception {
        String eventId = createPublishedEvent(VALID_EVENT);
        String created = mockMvc.perform(post("/api/v1/events/{eventId}/registrations", eventId)
                        .with(csrf())
                        .header("Idempotency-Key", "2692c85b-4afb-41dc-9145-7759f67e56c0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TEAM_REGISTRATION))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String registrationId = read(created, "registrationId");
        String orderId = read(created, "razorpayOrderId");

        mockMvc.perform(post("/api/v1/payments/razorpay/webhook")
                        .header("x-razorpay-event-id", "evt_failure_team_01")
                        .header("x-razorpay-signature", "test-signature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(failedPayment(orderId)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/registrations/{registrationId}/status", registrationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAYMENT_FAILED"))
                .andExpect(jsonPath("$.retryAllowed").value(true));

        mockMvc.perform(get("/api/v1/events/{eventId}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remainingCapacity").value(1));

        mockMvc.perform(post("/api/v1/registrations/{registrationId}/retry", registrationId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.razorpayOrderId").exists());
    }

    @Test
    void deletingEventRemovesRegistrationsAndTheirLinkedRecords() throws Exception {
        String paidEventId = createPublishedEvent(VALID_EVENT);
        String paidRegistration = mockMvc.perform(post("/api/v1/events/{eventId}/registrations", paidEventId)
                        .with(csrf())
                        .header("Idempotency-Key", "bf19a208-3fbb-4a2d-b1ef-44a32dd4e217")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TEAM_REGISTRATION))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String paidRegistrationId = read(paidRegistration, "registrationId");

        String freeEventId = createPublishedEvent(VALID_EVENT.replace("250.00", "0.00"));
        String freeRegistration = mockMvc.perform(post("/api/v1/events/{eventId}/registrations", freeEventId)
                        .with(csrf())
                        .header("Idempotency-Key", "a6c634a5-98cd-4ac8-86c1-12f617079eb1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TEAM_REGISTRATION))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String freeRegistrationId = read(freeRegistration, "registrationId");

        TestTransaction.flagForCommit();
        TestTransaction.end();

        mockMvc.perform(delete("/api/v1/admin/events/{eventId}", paidEventId)
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/admin/events/{eventId}", freeEventId)
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertThatCountIsZero("event_registrations", "id", paidRegistrationId);
        assertThatCountIsZero("event_registrations", "id", freeRegistrationId);
        assertThatCountIsZero("registration_team_members", "registration_id", paidRegistrationId);
        assertThatCountIsZero("payment_attempts", "registration_id", paidRegistrationId);
        assertThatCountIsZero("registration_email_deliveries", "registration_id", freeRegistrationId);

        mockMvc.perform(get("/api/v1/events/{eventId}", paidEventId))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/registrations/{registrationId}/status", paidRegistrationId))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/registrations/{registrationId}/status", freeRegistrationId))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidSignatureCannotConfirmAndExpiredHoldReleasesCapacity() throws Exception {
        String eventId = createPublishedEvent(VALID_EVENT);
        String created = mockMvc.perform(post("/api/v1/events/{eventId}/registrations", eventId)
                        .with(csrf())
                        .header("Idempotency-Key", "74936562-2e5e-46c5-ad58-8dd1be2f2062")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TEAM_REGISTRATION))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String registrationId = read(created, "registrationId");
        String orderId = read(created, "razorpayOrderId");

        mockMvc.perform(post("/api/v1/payments/razorpay/webhook")
                        .header("x-razorpay-event-id", "evt_invalid_signature_01")
                        .header("x-razorpay-signature", "invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(capturedPayment(orderId, 25000)))
                .andExpect(status().isUnauthorized());

        clock.setInstant(clock.instant().plusSeconds(601));
        mockMvc.perform(get("/api/v1/registrations/{registrationId}/status", registrationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXPIRED"))
                .andExpect(jsonPath("$.retryAllowed").value(true));

        mockMvc.perform(get("/api/v1/events/{eventId}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remainingCapacity").value(1));
    }

    @Test
    void rejectsTeamSizesThatExcludeTheCaptainFromTheConfiguredLimit() throws Exception {
        String eventId = createPublishedEvent(VALID_EVENT);
        String noOtherMembers = """
                {
                  "teamName": "Solo captain",
                  "captainName": "Aarav Rao",
                  "captainPhone": "+919876543210",
                  "captainEmail": "captain@example.test",
                  "members": []
                }
                """;

        mockMvc.perform(post("/api/v1/events/{eventId}/registrations", eventId)
                        .with(csrf())
                        .header("Idempotency-Key", "e12bf431-c20a-4a48-a227-f7599cd8f0be")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noOtherMembers))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminCanReviewEditAndExportPrivateRegistrationDetails() throws Exception {
        String eventId = createPublishedEvent(VALID_EVENT);
        String created = mockMvc.perform(post("/api/v1/events/{eventId}/registrations", eventId)
                        .with(csrf())
                        .header("Idempotency-Key", "4ed1d74e-a7f5-4e1f-b09a-2de6a8c170c8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TEAM_REGISTRATION))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String registrationId = read(created, "registrationId");

        mockMvc.perform(get("/api/v1/admin/events/{eventId}/registrations", eventId))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/events/{eventId}/registrations", eventId)
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].captainEmail").value("captain@example.test"))
                .andExpect(jsonPath("$.content[0].members[0].email").value("member@example.test"));

        String edited = """
                {
                  "teamName": "Northwind Rebuilt",
                  "captainName": "Aarav Rao",
                  "captainPhone": "+919876543210",
                  "captainEmail": "captain.updated@example.test",
                  "members": [{
                    "name": "Diya Sen",
                    "year": "SECOND",
                    "department": "Aerospace Engineering",
                    "institution": "SIT",
                    "email": "member.updated@example.test"
                  }]
                }
                """;
        mockMvc.perform(put("/api/v1/admin/registrations/{registrationId}", registrationId)
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(edited))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamName").value("Northwind Rebuilt"))
                .andExpect(jsonPath("$.captainEmail").value("captain.updated@example.test"))
                .andExpect(jsonPath("$.members[0].email").value("member.updated@example.test"));

        mockMvc.perform(get("/api/v1/admin/events/{eventId}/registrations/export.csv", eventId)
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Northwind Rebuilt")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("member.updated@example.test")));
    }

    private String createPublishedEvent(String event) throws Exception {
        String created = mockMvc.perform(post("/api/v1/admin/events")
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(event))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = read(created, "id");
        mockMvc.perform(post("/api/v1/admin/events/{id}/publish", id)
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk());
        return id;
    }

    private void assertThatCountIsZero(String table, String column, String registrationId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE " + column + " = ?",
                Integer.class,
                java.util.UUID.fromString(registrationId));
        org.assertj.core.api.Assertions.assertThat(count).isZero();
    }

    private String capturedPayment(String orderId, int amount) {
        return """
                {
                  "event": "payment.captured",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "pay_test_01",
                        "order_id": "%s",
                        "amount": %d,
                        "currency": "INR",
                        "status": "captured"
                      }
                    }
                  }
                }
                """.formatted(orderId, amount);
    }

    private String failedPayment(String orderId) {
        return """
                {
                  "event": "payment.failed",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "pay_test_failed_01",
                        "order_id": "%s",
                        "amount": 25000,
                        "currency": "INR",
                        "status": "failed"
                      }
                    }
                  }
                }
                """.formatted(orderId);
    }

    private String read(String json, String field) throws java.io.IOException {
        return objectMapper.readTree(json).get(field).asText();
    }

    @TestConfiguration
    static class TestConfigurationBeans {

        @Bean
        @Primary
        PaymentGateway paymentGateway() {
            return new PaymentGateway() {
                @Override
                public boolean isCheckoutConfigured() {
                    return true;
                }

                @Override
                public boolean isWebhookConfigured() {
                    return true;
                }

                @Override
                public String createOrder(String receipt, long amountPaise, String currency, String registrationId) {
                    return "order_" + receipt.replace("-", "");
                }

                @Override
                public boolean isWebhookSignatureValid(byte[] rawBody, String signature) {
                    return "test-signature".equals(signature);
                }

                @Override
                public String checkoutKeyId() {
                    return "rzp_test_public";
                }
            };
        }

        @Bean
        @Primary
        MutableClock registrationTestClock() {
            return new MutableClock(Instant.parse("2026-11-03T00:00:00Z"));
        }
    }

    static final class MutableClock extends Clock {
        private final AtomicReference<Instant> instant;
        private final ZoneId zone;

        MutableClock(Instant instant) {
            this(instant, ZoneOffset.UTC);
        }

        private MutableClock(Instant instant, ZoneId zone) {
            this.instant = new AtomicReference<>(instant);
            this.zone = zone;
        }

        void setInstant(Instant value) {
            instant.set(value);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant.get(), zone);
        }

        @Override
        public Instant instant() {
            return instant.get();
        }
    }
}
