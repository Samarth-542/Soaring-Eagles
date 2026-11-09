package in.soaringeagles.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContactFormIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void adminProperties(DynamicPropertyRegistry registry) {
        TestAdminCredentials.register(registry);
    }

    @Test
    void contactSubmissionIsSavedAndCanBeManagedFromTheAdminInbox() throws Exception {
        MvcResult result = mockMvc.perform(multipart("/api/v1/contact")
                        .param("name", "Student Name")
                        .param("email", "student@example.com")
                        .param("subject", "Project question")
                        .param("message", "Please share more details.")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn();
        String id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/v1/admin/contact-messages")
                        .param("state", "NEW")
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].senderName").value("Student Name"))
                .andExpect(jsonPath("$.content[0].senderEmail").value("student@example.com"))
                .andExpect(jsonPath("$.content[0].subject").value("Project question"))
                .andExpect(jsonPath("$.content[0].state").value("NEW"));

        mockMvc.perform(get("/api/v1/admin/contact-messages/{id}", id)
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Please share more details."));

        mockMvc.perform(patch("/api/v1/admin/contact-messages/{id}/state", id)
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"state":"IN_PROGRESS"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("IN_PROGRESS"));

        mockMvc.perform(patch("/api/v1/admin/contact-messages/{id}/state", id)
                        .with(user(TestAdminCredentials.EMAIL).roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"state":"ARCHIVED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("ARCHIVED"));
    }

    @Test
    void adminInboxRequiresAnAdministrator() throws Exception {
        mockMvc.perform(get("/api/v1/admin/contact-messages"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/contact/{id}/attachment", java.util.UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void contactSubmissionRequiresCsrfProtection() throws Exception {
        mockMvc.perform(multipart("/api/v1/contact")
                        .param("name", "Student Name")
                        .param("email", "student@example.com")
                        .param("subject", "Project question")
                        .param("message", "Please share more details."))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidAttachmentIsRejectedBeforeStorageIsUsed() throws Exception {
        MockMultipartFile attachment = new MockMultipartFile(
                "attachment",
                "fake.pdf",
                "application/pdf",
                "not a PDF".getBytes(java.nio.charset.StandardCharsets.US_ASCII));

        mockMvc.perform(multipart("/api/v1/contact")
                        .file(attachment)
                        .param("name", "Student Name")
                        .param("email", "student@example.com")
                        .param("subject", "Project question")
                        .param("message", "Please share more details.")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
