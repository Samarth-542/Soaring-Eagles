package in.soaringeagles.api.contact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import in.soaringeagles.api.content.SupabaseStorageProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class ContactAttachmentStorageServiceTest {

    @Test
    void uploadsValidAttachmentsIntoTheConfiguredPrivateBucket() throws Exception {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        UUID messageId = UUID.randomUUID();
        server.expect(requestTo(org.hamcrest.Matchers.matchesPattern(
                        "https://storage.example/storage/v1/object/private/contact-messages/"
                                + messageId + "/[0-9a-f-]+")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("apikey", "service-key"))
                .andExpect(content().contentType("application/pdf"))
                .andRespond(withSuccess());
        ContactAttachmentStorageService service = new ContactAttachmentStorageService(
                builder,
                new SupabaseStorageProperties(
                        "https://storage.example", "service-key", "public", "private"));
        MockMultipartFile file = new MockMultipartFile(
                "attachment",
                "C:\\private\\brief.pdf",
                "application/pdf",
                "%PDF-1.7\nsample".getBytes(StandardCharsets.US_ASCII));

        ContactAttachmentStorageService.StoredAttachment result = service.upload(messageId, file);

        assertThat(result.fileName()).isEqualTo("brief.pdf");
        assertThat(result.contentType()).isEqualTo("application/pdf");
        assertThat(result.sizeBytes()).isEqualTo(file.getSize());
        assertThat(result.storageKey()).startsWith("contact-messages/" + messageId + "/");
        server.verify();
    }

    @Test
    void rejectsAnAttachmentWhoseContentDoesNotMatchItsExtension() {
        ContactAttachmentStorageService service = new ContactAttachmentStorageService(
                RestClient.builder(),
                new SupabaseStorageProperties(
                        "https://storage.example", "service-key", "public", "private"));
        MockMultipartFile file = new MockMultipartFile(
                "attachment",
                "fake.pdf",
                "application/pdf",
                "not a PDF".getBytes(StandardCharsets.US_ASCII));

        assertThatThrownBy(() -> service.upload(UUID.randomUUID(), file))
                .isInstanceOf(InvalidContactAttachmentException.class);
    }
}
