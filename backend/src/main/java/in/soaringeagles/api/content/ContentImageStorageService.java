package in.soaringeagles.api.content;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ContentImageStorageService {

    public static final long MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024;
    private static final long MAX_IMAGE_PIXELS = 20_000_000;
    private static final Logger logger = LoggerFactory.getLogger(ContentImageStorageService.class);

    private final RestClient restClient;
    private final SupabaseStorageProperties properties;
    private final JdbcTemplate jdbc;

    ContentImageStorageService(
            RestClient.Builder builder,
            SupabaseStorageProperties properties,
            JdbcTemplate jdbc) {
        this.restClient = builder.build();
        this.properties = properties;
        this.jdbc = jdbc;
    }

    public UploadedImage upload(MultipartFile file, String section) {
        if (!properties.isConfigured()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Image uploads are unavailable. Configure Supabase Storage on the backend.");
        }
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose an image to upload.");
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Images must be 10 MB or smaller.");
        }
        if (!section.matches("team|projects|journey|events")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported image category.");
        }

        byte[] content;
        ImageFormat format;
        try {
            content = file.getBytes();
            format = inspectImage(content);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The uploaded file is not a valid image.");
        }

        String key = section + "/" + UUID.randomUUID() + "." + format.extension();
        String objectUrl = properties.normalizedUrl() + "/storage/v1/object/"
                + properties.publicBucket() + "/" + key;
        try {
            restClient.post()
                    .uri(URI.create(objectUrl))
                    .header("apikey", properties.serviceRoleKey())
                    .header("Authorization", "Bearer " + properties.serviceRoleKey())
                    .header("x-upsert", "false")
                    .contentType(format.mediaType())
                    .body(content)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "The image could not be uploaded to Supabase Storage.");
        }

        String publicUrl = properties.normalizedUrl() + "/storage/v1/object/public/"
                + properties.publicBucket() + "/" + key;
        try {
            jdbc.update("""
                    INSERT INTO media_assets
                        (id, storage_bucket, storage_key, original_file_name, content_type,
                         size_bytes, visibility, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, 'PUBLIC', CURRENT_TIMESTAMP)
                    """, UUID.randomUUID(), properties.publicBucket(), key,
                    safeFileName(file.getOriginalFilename()), format.mediaType().toString(), content.length);
        } catch (DataAccessException exception) {
            deleteFromStorage(objectUrl);
            throw exception;
        }

        return new UploadedImage(publicUrl, key, format.mediaType().toString(), content.length);
    }

    private ImageFormat inspectImage(byte[] content) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
            if (input == null) throw invalidImage();
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw invalidImage();

            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String imageFormat = reader.getFormatName().toLowerCase(Locale.ROOT);
                ImageFormat format = switch (imageFormat) {
                    case "png" -> new ImageFormat("png", MediaType.IMAGE_PNG);
                    case "jpeg", "jpg" -> new ImageFormat("jpg", MediaType.IMAGE_JPEG);
                    default -> throw invalidImage();
                };
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || (long) width * height > MAX_IMAGE_PIXELS) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Image dimensions must be no larger than 20 megapixels.");
                }
                BufferedImage decoded = reader.read(0);
                if (decoded == null) throw invalidImage();
                decoded.flush();
                return format;
            } finally {
                reader.dispose();
            }
        } catch (IllegalArgumentException exception) {
            throw invalidImage();
        }
    }

    private void deleteFromStorage(String objectUrl) {
        try {
            restClient.delete()
                    .uri(URI.create(objectUrl))
                    .header("apikey", properties.serviceRoleKey())
                    .header("Authorization", "Bearer " + properties.serviceRoleKey())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ignored) {
            logger.warn("Could not remove Supabase Storage object after its metadata insert failed.");
        }
    }

    private String safeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) return "upload";
        String name = fileName.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "_");
        return name.length() <= 255 ? name : name.substring(name.length() - 255);
    }

    private ResponseStatusException invalidImage() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload a valid JPEG or PNG image.");
    }

    public record UploadedImage(String publicUrl, String storageKey, String contentType, long sizeBytes) {
    }

    private record ImageFormat(String extension, MediaType mediaType) {
    }
}
