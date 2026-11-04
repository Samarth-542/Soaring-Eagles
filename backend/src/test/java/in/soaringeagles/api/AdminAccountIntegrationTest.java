package in.soaringeagles.api;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminAccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DynamicPropertySource
    static void adminProperties(DynamicPropertyRegistry registry) {
        TestAdminCredentials.register(registry);
    }

    @Test
    void configuredAdminIsBootstrappedAndCanCreateLoginAndDeactivateAnotherAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/accounts")
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email").value(hasItem(TestAdminCredentials.EMAIL)));

        String email = "new-" + UUID.randomUUID() + "@example.invalid";
        String password = "test-admin-account-password";
        MvcResult created = mockMvc.perform(post("/api/v1/admin/accounts")
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email.toUpperCase(), password)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn();
        String accountId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();
        var newAdmin = userDetailsService.loadUserByUsername(email);
        org.assertj.core.api.Assertions.assertThat(newAdmin.isEnabled()).isTrue();
        org.assertj.core.api.Assertions.assertThat(passwordEncoder.matches(password, newAdmin.getPassword())).isTrue();

        mockMvc.perform(patch("/api/v1/admin/accounts/{id}/enabled", accountId)
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        org.assertj.core.api.Assertions.assertThat(userDetailsService.loadUserByUsername(email).isEnabled()).isFalse();
    }

    @Test
    void unauthenticatedUsersCannotListAccounts() throws Exception {
        mockMvc.perform(get("/api/v1/admin/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void lastActiveAdminCannotBeDeactivated() throws Exception {
        JsonNode accounts = objectMapper.readTree(mockMvc.perform(get("/api/v1/admin/accounts")
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN")))
                .andReturn().getResponse().getContentAsString());
        String accountId = null;
        for (JsonNode account : accounts) {
            if (TestAdminCredentials.EMAIL.equals(account.path("email").asText())) {
                accountId = account.path("id").asText();
                break;
            }
        }
        org.assertj.core.api.Assertions.assertThat(accountId).isNotNull();

        mockMvc.perform(patch("/api/v1/admin/accounts/{id}/enabled", accountId)
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":false}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("The last active admin account cannot be deactivated."));
    }

}
