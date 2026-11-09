package in.soaringeagles.api.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class ContentImageStorageServiceTest {

    @Test
    void uploadsValidEventImageToConfiguredPublicBucketAndRecordsMetadata() throws Exception {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        JdbcTemplate jdbc = org.mockito.Mockito.mock(JdbcTemplate.class);
        byte[] image = pngImage();
        server.expect(requestTo(org.hamcrest.Matchers.matchesPattern(
                        "https://storage.example/storage/v1/object/se_data/events/[0-9a-f-]+\\.png")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("apikey", "service-key"))
                .andExpect(header("Authorization", "Bearer service-key"))
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(image))
                .andRespond(withSuccess());

        ContentImageStorageService service = new ContentImageStorageService(
                builder,
                new SupabaseStorageProperties(
                        "https://storage.example", "service-key", "se_data", "se_private"),
                jdbc);

        ContentImageStorageService.UploadedImage uploaded = service.upload(
                new org.springframework.mock.web.MockMultipartFile(
                        "file", "event-poster.png", "image/png", image),
                "events");

        assertThat(uploaded.publicUrl())
                .matches("https://storage\\.example/storage/v1/object/public/se_data/events/[0-9a-f-]+\\.png");
        assertThat(uploaded.storageKey()).startsWith("events/").endsWith(".png");
        assertThat(uploaded.contentType()).isEqualTo("image/png");
        assertThat(uploaded.sizeBytes()).isEqualTo(image.length);
        verify(jdbc).update(contains("INSERT INTO media_assets"), any(Object[].class));
        server.verify();
    }

    @Test
    void missingStorageConfigurationFailsClearly() throws Exception {
        ContentImageStorageService service = new ContentImageStorageService(
                RestClient.builder(),
                new SupabaseStorageProperties("", "", "se_data", "se_private"),
                org.mockito.Mockito.mock(JdbcTemplate.class));
        var image = new org.springframework.mock.web.MockMultipartFile(
                "file", "event-poster.png", "image/png", pngImage());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.upload(image, "events"))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY");
    }

    private byte[] pngImage() throws Exception {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bytes);
        image.flush();
        return bytes.toByteArray();
    }
}
