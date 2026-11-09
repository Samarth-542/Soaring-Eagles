package in.soaringeagles.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ContentManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void adminProperties(DynamicPropertyRegistry registry) {
        TestAdminCredentials.register(registry);
        registry.add("app.supabase.storage.url", () -> "");
        registry.add("app.supabase.storage.service-role-key", () -> "");
    }

    @Test
    void migratedWebsiteContentIsServedPublicly() throws Exception {
        mockMvc.perform(get("/api/v1/content/team"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groups").isArray())
                .andExpect(jsonPath("$.groups.length()").value(6))
                .andExpect(jsonPath("$.groups[0].members.length()").value(2));

        mockMvc.perform(get("/api/v1/content/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(9))
                .andExpect(jsonPath("$[0].title").value("Netra 2025"));

        mockMvc.perform(get("/api/v1/content/journey"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(11))
                .andExpect(jsonPath("$[0].title").value("SAEINDIA ADDC Workshop Phase 1 — SRM Chennai"));
    }

    @Test
    void contentManagementRequiresAdminAndPublishesNewRecords() throws Exception {
        mockMvc.perform(get("/api/v1/admin/content/projects"))
                .andExpect(status().isUnauthorized());

        String project = """
                {
                  "title": "Live database project",
                  "year": 2026,
                  "description": "A project created through the protected content API.",
                  "imageUrl": "/images/projects/live-project.png",
                  "tags": ["Database", "API"],
                  "position": 1010,
                  "published": true
                }
                """;

        mockMvc.perform(post("/api/v1/admin/content/projects")
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(project))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Live database project"))
                .andExpect(jsonPath("$.tags.length()").value(2));

        mockMvc.perform(get("/api/v1/content/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Live database project"));
    }

    @Test
    void imageUploadsRequireAdminAndStorageConfiguration() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "portrait.png", "image/png", new byte[]{1, 2, 3});

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/v1/admin/content/images")
                        .file(file)
                        .param("section", "team")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/v1/admin/content/images")
                        .file(file)
                        .param("section", "team")
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value(
                        "Image uploads require SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY on the backend. "
                                + "Configure a public Supabase Storage bucket (default: se_data) and restart the API."));
    }
}
