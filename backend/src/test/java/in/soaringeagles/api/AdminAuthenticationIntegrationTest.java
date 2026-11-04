package in.soaringeagles.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminAuthenticationIntegrationTest {

    private static final String ADMIN_IP = "192.0.2.10";
    private static final String ATTACKER_IP = "192.0.2.11";

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void adminProperties(DynamicPropertyRegistry registry) {
        TestAdminCredentials.register(registry);
    }

    @Test
    void loginCreatesHttpOnlyServerSessionAndLogoutInvalidatesIt() throws Exception {
        CsrfState csrf = csrf(ADMIN_IP, null);

        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .with(remoteAddress(ADMIN_IP))
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", TestAdminCredentials.EMAIL)
                        .param("password", TestAdminCredentials.PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andReturn();

        Cookie sessionCookie = login.getResponse().getCookie("SOARING_SESSION");
        assertThat(sessionCookie).isNotNull();
        assertThat(sessionCookie.isHttpOnly()).isTrue();
        assertThat(login.getResponse().getHeaders("Set-Cookie").stream()
                .filter(header -> header.startsWith("SOARING_SESSION="))
                .findFirst()
                .orElse(""))
                .contains("HttpOnly", "SameSite=Lax", "Secure");

        mockMvc.perform(get("/api/v1/auth/session")
                        .with(remoteAddress(ADMIN_IP))
                        .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(TestAdminCredentials.EMAIL));

        CsrfState logoutCsrf = csrf(ADMIN_IP, sessionCookie);
        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(remoteAddress(ADMIN_IP))
                        .cookie(sessionCookie, logoutCsrf.cookie())
                        .header("X-XSRF-TOKEN", logoutCsrf.token()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/auth/session")
                        .with(remoteAddress(ADMIN_IP))
                        .cookie(sessionCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginRequiresCsrfAndAllAdminApiRoutesRemainDeniedWithoutSession() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(remoteAddress("192.0.2.12"))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", TestAdminCredentials.EMAIL)
                        .param("password", TestAdminCredentials.PASSWORD))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/auth/session"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void loginDoesNotAcceptCredentialsInTheUrl() throws Exception {
        CsrfState csrf = csrf("192.0.2.13", null);
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(remoteAddress("192.0.2.13"))
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .queryParam("email", TestAdminCredentials.EMAIL)
                        .queryParam("password", TestAdminCredentials.PASSWORD))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidCredentialsAreGenericAndLockTheEmailAndSourceAddress() throws Exception {
        CsrfState csrf = csrf(ATTACKER_IP, null);

        for (int attempt = 1; attempt <= 4; attempt++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .with(remoteAddress(ATTACKER_IP))
                            .cookie(csrf.cookie())
                            .header("X-XSRF-TOKEN", csrf.token())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("email", "unknown@example.invalid")
                            .param("password", "incorrect"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.detail").value("Email or password is incorrect."));
        }

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(remoteAddress(ATTACKER_IP))
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "unknown@example.invalid")
                        .param("password", "incorrect"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.detail").value("Too many sign-in attempts. Try again later."));

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(remoteAddress(ATTACKER_IP))
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", TestAdminCredentials.EMAIL)
                        .param("password", TestAdminCredentials.PASSWORD))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void corsAllowsCsrfHeaderOnlyFromConfiguredFrontendOrigins() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type,x-xsrf-token"))
                .andExpect(status().isOk());

        mockMvc.perform(options("/api/v1/auth/login")
                        .header("Origin", "https://attacker.example")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type,x-xsrf-token"))
                .andExpect(status().isForbidden());
    }

    private CsrfState csrf(String remoteAddress, Cookie sessionCookie) throws Exception {
        var request = get("/api/v1/auth/csrf").with(remoteAddress(remoteAddress));
        if (sessionCookie != null) request.cookie(sessionCookie);
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        Cookie csrfCookie = result.getResponse().getCookie("XSRF-TOKEN");
        assertThat(csrfCookie).isNotNull();
        return new CsrfState(csrfCookie, csrfCookie.getValue());
    }

    private RequestPostProcessor remoteAddress(String address) {
        return request -> {
            request.setRemoteAddr(address);
            return request;
        };
    }

    private record CsrfState(Cookie cookie, String token) {
    }
}
