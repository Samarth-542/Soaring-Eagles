package in.soaringeagles.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(EventManagementIntegrationTest.EventClockConfiguration.class)
class EventManagementIntegrationTest {

    private static final String VALID_EVENT = """
            {
              "title": "Flight Systems Workshop",
              "description": "A workshop on aircraft design and testing.",
              "startsAt": "2031-02-10T09:00:00+05:30",
              "endsAt": "2031-02-10T17:00:00+05:30",
              "venue": "SIT, Tumakuru",
              "posterUrl": "/images/events/workshop.png",
              "eligibility": "Open to SIT students.",
              "feeInr": 250.00,
              "paymentMethod": "RAZORPAY",
              "registrationType": "TEAM",
              "registrationDeadline": "2031-02-08T23:59:00+05:30",
              "capacity": 24,
              "minimumTeamSize": 2,
              "maximumTeamSize": 5,
              "archiveStory": "",
              "archiveResults": "",
              "archivePhotos": []
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MutableClock clock;

    @BeforeEach
    void resetClock() {
        clock.setInstant(Instant.parse("2026-11-03T00:00:00Z"));
    }

    @DynamicPropertySource
    static void adminProperties(DynamicPropertyRegistry registry) {
        TestAdminCredentials.register(registry);
    }

    @Test
    void draftIsPrivateUntilExplicitlyPublished() throws Exception {
        String created = mockMvc.perform(post("/api/v1/admin/events")
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_EVENT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = objectMapper.readTree(created).get("id").asText();

        mockMvc.perform(post("/api/v1/admin/events/{id}/close", id)
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(post("/api/v1/admin/events/{id}/publish", id)
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        mockMvc.perform(get("/api/v1/events/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Flight Systems Workshop"))
                .andExpect(jsonPath("$.feeInr").value(250.0))
                .andExpect(jsonPath("$.registrationType").value("TEAM"))
                .andExpect(jsonPath("$.capacity").value(24))
                .andExpect(jsonPath("$.remainingCapacity").value(24))
                .andExpect(jsonPath("$.registrationWindowOpen").value(true))
                .andExpect(jsonPath("$.createdAt").doesNotExist())
                .andExpect(jsonPath("$.scheduledPublishAt").doesNotExist());

        mockMvc.perform(post("/api/v1/admin/events/{id}/close", id)
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        mockMvc.perform(get("/api/v1/events/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationWindowOpen").value(false));
    }

    @Test
    void scheduledEventsRemainPrivateUntilTheirPublicationTime() throws Exception {
        String created = mockMvc.perform(post("/api/v1/admin/events")
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_EVENT))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = objectMapper.readTree(created).get("id").asText();

        mockMvc.perform(post("/api/v1/admin/events/{id}/schedule", id)
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"publishAt":"2026-11-04T00:00:00+05:30"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SCHEDULED"));

        mockMvc.perform(post("/api/v1/admin/events/{id}/close", id)
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        clock.setInstant(Instant.parse("2026-11-03T18:31:00Z"));
        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PUBLISHED"))
                .andExpect(jsonPath("$[0].title").value("Flight Systems Workshop"))
                .andExpect(jsonPath("$[0].scheduledPublishAt").doesNotExist())
                .andExpect(jsonPath("$[0].updatedAt").doesNotExist());
    }

    @Test
    void deadlineAutomaticallyClosesRegistrationAndAdminRoutesRequireAuthentication() throws Exception {
        String expiredEvent = VALID_EVENT.replace(
                "2031-02-08T23:59:00+05:30",
                "2020-02-08T23:59:00+05:30");
        String created = mockMvc.perform(post("/api/v1/admin/events")
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expiredEvent))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = objectMapper.readTree(created).get("id").asText();

        mockMvc.perform(post("/api/v1/admin/events/{id}/publish", id)
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/events/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationWindowOpen").value(false));

        mockMvc.perform(get("/api/v1/admin/events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsInvalidDatesTeamLimitsAndUnsafeImageUrls() throws Exception {
        String invalidTeam = VALID_EVENT
                .replace("\"minimumTeamSize\": 2", "\"minimumTeamSize\": 6")
                .replace("\"maximumTeamSize\": 5", "\"maximumTeamSize\": 2");

        mockMvc.perform(post("/api/v1/admin/events")
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidTeam))
                .andExpect(status().isBadRequest());

        String zeroCapacity = VALID_EVENT.replace("\"capacity\": 24", "\"capacity\": 0");
        mockMvc.perform(post("/api/v1/admin/events")
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(zeroCapacity))
                .andExpect(status().isBadRequest());

        String wrongTimeZone = VALID_EVENT.replace("+05:30", "+00:00");
        mockMvc.perform(post("/api/v1/admin/events")
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongTimeZone))
                .andExpect(status().isBadRequest());

        String unsafePoster = VALID_EVENT.replace(
                "/images/events/workshop.png",
                "javascript:alert(1)");
        mockMvc.perform(post("/api/v1/admin/events")
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(unsafePoster))
                .andExpect(status().isBadRequest());
    }

    @Test
    void endedEventsCanBeEditedForThePublicArchive() throws Exception {
        String pastEvent = VALID_EVENT
                .replace("2031-02-10T09:00:00+05:30", "2020-02-10T09:00:00+05:30")
                .replace("2031-02-10T17:00:00+05:30", "2020-02-10T17:00:00+05:30")
                .replace("2031-02-08T23:59:00+05:30", "2020-02-08T23:59:00+05:30");
        String created = mockMvc.perform(post("/api/v1/admin/events")
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pastEvent))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = objectMapper.readTree(created).get("id").asText();

        mockMvc.perform(post("/api/v1/admin/events/{id}/publish", id)
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk());

        var archiveUpdate = objectMapper.readTree(pastEvent);
        var archiveObject = (com.fasterxml.jackson.databind.node.ObjectNode) archiveUpdate;
        archiveObject.put("archiveStory", "The workshop brought student teams together.");
        archiveObject.put("archiveResults", "Teams completed their first test flights.");
        archiveObject.putArray("archivePhotos").add("/images/events/workshop-recap.png");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/v1/admin/events/{id}", id)
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(archiveObject.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archiveStory").value("The workshop brought student teams together."))
                .andExpect(jsonPath("$.archivePhotos[0]").value("/images/events/workshop-recap.png"));

        mockMvc.perform(get("/api/v1/events/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ended").value(true))
                .andExpect(jsonPath("$.archiveResults").value("Teams completed their first test flights."));
    }

    @TestConfiguration
    static class EventClockConfiguration {

        @Bean
        @Primary
        MutableClock eventTestClock() {
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
