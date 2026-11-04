package in.soaringeagles.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BackendFoundationIntegrationTest {

    @DynamicPropertySource
    static void adminProperties(DynamicPropertyRegistry registry) {
        TestAdminCredentials.register(registry);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void healthEndpointIsPublicAndDoesNotExposeDetails() {
        var response = restTemplate.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
        assertThat(response.getBody()).doesNotContain("\"components\"");
    }

    @Test
    void apiRoutesRequireAuthenticationByDefault() {
        var response = restTemplate.getForEntity("/api/v1/admin/users", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(response.getBody())
                .contains("\"title\":\"Unauthorized\"")
                .doesNotContain("SUPABASE_DB_PASSWORD");
    }

    @Test
    void corsAllowsCredentialsOnlyForConfiguredOrigins() {
        HttpHeaders headers = new HttpHeaders();
        headers.setOrigin("http://localhost:5173");
        headers.setAccessControlRequestMethod(HttpMethod.GET);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/v1/admin/users",
                HttpMethod.OPTIONS,
                request,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getAccessControlAllowOrigin()).isEqualTo("http://localhost:5173");
        assertThat(response.getHeaders().getAccessControlAllowCredentials()).isTrue();
    }

    @Test
    void corsRejectsUnconfiguredOrigins() {
        HttpHeaders headers = new HttpHeaders();
        headers.setOrigin("https://attacker.example");
        headers.setAccessControlRequestMethod(HttpMethod.GET);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/v1/admin/users",
                HttpMethod.OPTIONS,
                request,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getHeaders().getAccessControlAllowOrigin()).isNull();
    }

    @Test
    void flywayAppliesTheInitialSchemaBaseline() {
        String version = jdbcTemplate.queryForObject(
                """
                SELECT "version"
                FROM "flyway_schema_history"
                WHERE "version" = '1' AND "success" = TRUE
                """,
                String.class);

        assertThat(version).isEqualTo("1");
    }
}
