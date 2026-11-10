package in.soaringeagles.api;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SiteDetailsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void adminProperties(DynamicPropertyRegistry registry) {
        TestAdminCredentials.register(registry);
    }

    @Test
    void defaultsArePublicAndAdminCanUpdateDetailsShownOnTheWebsite() throws Exception {
        mockMvc.perform(get("/api/v1/content/site-details"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("public")))
                .andExpect(header().string("Cache-Control", containsString("max-age=300")))
                .andExpect(header().string("Cache-Control", containsString("stale-while-revalidate=300")))
                .andExpect(jsonPath("$.email").value("tseofficial2011@gmail.com"))
                .andExpect(jsonPath("$.linkedinUrl").value("https://linkedin.com/in/soaring-eagles-a7a19831b"))
                .andExpect(jsonPath("$.xUrl").value("https://x.com/TSE_015"))
                .andExpect(jsonPath("$.location").value(
                        "Soaring Eagles Aeromodelling Lab\nNear Mechanical Block,\n"
                                + "Siddaganga Institute of Technology\nTumkur, 572103"));

        mockMvc.perform(put("/api/v1/admin/site-details")
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "new-contact@example.com",
                                  "linkedinUrl": "https://linkedin.com/company/soaring-eagles",
                                  "xUrl": "",
                                  "location": "Updated club location"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new-contact@example.com"))
                .andExpect(jsonPath("$.xUrl").value(""))
                .andExpect(jsonPath("$.location").value("Updated club location"));

        mockMvc.perform(get("/api/v1/content/site-details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new-contact@example.com"))
                .andExpect(jsonPath("$.location").value("Updated club location"));
    }

    @Test
    void editingSiteDetailsRequiresAdminAuthenticationAndCsrf() throws Exception {
        mockMvc.perform(get("/api/v1/admin/site-details"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/v1/admin/site-details")
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "contact@example.com",
                                  "linkedinUrl": "",
                                  "xUrl": "",
                                  "location": "A location"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsNonHttpSocialLinks() throws Exception {
        mockMvc.perform(put("/api/v1/admin/site-details")
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "contact@example.com",
                                  "linkedinUrl": "javascript:alert(1)",
                                  "xUrl": "",
                                  "location": "A location"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
