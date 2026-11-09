package in.soaringeagles.api.contact;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

import in.soaringeagles.api.content.SupabaseStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ContactAttachmentStorageService {

    private static final long MAX_ATTACHMENT_SIZE = 10L * 1024 * 1024;
    private static final Logger LOGGER = LoggerFactory.getLogger(ContactAttachmentStorageService.class);
    private final RestClient restClient;
    private final SupabaseStorageProperties properties;

    ContactAttachmentStorageService(RestClient.Builder builder, SupabaseStorageProperties properties) {
        this.restClient = builder.build();
        this.properties = properties;
    }

    public StoredAttachment upload(UUID messageId, MultipartFile file) throws IOException {
        if (file.getSize() > MAX_ATTACHMENT_SIZE) {
            throw new InvalidContactAttachmentException("Attachments must be 10 MB or smaller.");
        }
        byte[] content = file.getBytes();
        String fileName = safeFilename(file.getOriginalFilename());
        String contentType = detectContentType(fileName, content);
        if (!properties.isPrivateConfigured()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Private attachment storage is not configured on the backend.");
        }

        String key = "contact-messages/" + messageId + "/" + UUID.randomUUID();
        try {
            restClient.post()
                    .uri(objectUri(key))
                    .header("apikey", properties.serviceRoleKey())
                    .header("Authorization", "Bearer " + properties.serviceRoleKey())
                    .header("x-upsert", "false")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(content)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "The contact attachment could not be stored securely.",
                    exception);
        }
        return new StoredAttachment(key, fileName, contentType, content.length);
    }

    public byte[] download(String storageKey) {
        if (!properties.isPrivateConfigured()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Private attachment storage is not configured on the backend.");
        }
        try {
            byte[] content = restClient.get()
                    .uri(objectUri(storageKey))
                    .header("apikey", properties.serviceRoleKey())
                    .header("Authorization", "Bearer " + properties.serviceRoleKey())
                    .retrieve()
                    .body(byte[].class);
            if (content == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The contact attachment could not be read.");
            }
            return content;
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "The contact attachment could not be read securely.",
                    exception);
        }
    }

    public void delete(String storageKey) {
        if (!properties.isPrivateConfigured()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Private attachment storage is not configured on the backend.");
        }
        try {
            restClient.delete()
                    .uri(objectUri(storageKey))
                    .header("apikey", properties.serviceRoleKey())
                    .header("Authorization", "Bearer " + properties.serviceRoleKey())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            LOGGER.error("Could not remove an unreferenced contact attachment from private storage.", exception);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "The unreferenced contact attachment could not be removed from storage.",
                    exception);
        }
    }

    private URI objectUri(String storageKey) {
        return URI.create(properties.normalizedUrl() + "/storage/v1/object/"
                + properties.privateBucket() + "/" + storageKey);
    }

    private String safeFilename(String originalFilename) {
        String filename = originalFilename == null ? "attachment" : originalFilename.replace('\\', '/');
        filename = filename.substring(filename.lastIndexOf('/') + 1)
                .replaceAll("[^A-Za-z0-9._ -]", "_");
        if (filename.isBlank() || filename.length() > 120) {
            throw new InvalidContactAttachmentException("The attachment filename is invalid.");
        }
        return filename;
    }

    private String detectContentType(String filename, byte[] content) {
        String lowerName = filename.toLowerCase(Locale.ROOT);
        if (lowerName.endsWith(".pdf") && startsWith(content, "%PDF-".getBytes(StandardCharsets.US_ASCII))) {
            return "application/pdf";
        }
        if ((lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg"))
                && content.length >= 3
                && (content[0] & 0xff) == 0xff
                && (content[1] & 0xff) == 0xd8
                && (content[2] & 0xff) == 0xff) {
            return "image/jpeg";
        }
        if (lowerName.endsWith(".png")
                && startsWith(content, new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a})) {
            return "image/png";
        }
        throw new InvalidContactAttachmentException("Attach a valid PDF, JPEG, or PNG file.");
    }

    private boolean startsWith(byte[] content, byte[] prefix) {
        if (content.length < prefix.length) return false;
        for (int index = 0; index < prefix.length; index++) {
            if (content[index] != prefix[index]) return false;
        }
        return true;
    }

    public record StoredAttachment(String storageKey, String fileName, String contentType, long sizeBytes) {
    }
}
