package in.soaringeagles.api.content;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SiteDetailsService {

    private static final List<String> SETTING_KEYS = List.of(
            "public.contact_email",
            "public.linkedin_url",
            "public.x_url",
            "public.location");
    private final JdbcTemplate jdbc;

    SiteDetailsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public SiteDetails get() {
        List<SiteSetting> settings = jdbc.query("""
                SELECT setting_key, setting_value
                FROM site_settings
                WHERE setting_key IN (?, ?, ?, ?)
                """, (resultSet, row) -> new SiteSetting(
                resultSet.getString("setting_key"),
                resultSet.getString("setting_value")),
                SETTING_KEYS.toArray());
        Map<String, String> values = settings.stream()
                .collect(Collectors.toMap(SiteSetting::key, SiteSetting::value));
        if (values.size() != SETTING_KEYS.size()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Site details are not initialized in the database.");
        }
        return new SiteDetails(
                values.get("public.contact_email"),
                values.get("public.linkedin_url"),
                values.get("public.x_url"),
                normalizeLineEndings(values.get("public.location")));
    }

    @Transactional
    public SiteDetails update(SiteDetailsRequest request, String actorEmail) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String linkedinUrl = normalizeOptional(request.linkedinUrl());
        String xUrl = normalizeOptional(request.xUrl());
        String location = normalizeLineEndings(request.location().trim());
        validateOptionalUrl("LinkedIn", linkedinUrl);
        validateOptionalUrl("X", xUrl);

        updateSetting("public.contact_email", email);
        updateSetting("public.linkedin_url", linkedinUrl);
        updateSetting("public.x_url", xUrl);
        updateSetting("public.location", location);

        UUID actorId = jdbc.queryForObject(
                "SELECT id FROM admin_accounts WHERE email = ?", UUID.class, actorEmail);
        jdbc.update("""
                INSERT INTO admin_audit_events (id, admin_id, action, resource_type, resource_id, created_at)
                VALUES (?, ?, 'SITE_DETAILS_UPDATED', 'SITE_SETTINGS', 'public-contact-details', CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), actorId);
        return get();
    }

    private void updateSetting(String key, String value) {
        int updated = jdbc.update("""
                UPDATE site_settings
                SET setting_value = ?, updated_at = CURRENT_TIMESTAMP, version = version + 1
                WHERE setting_key = ?
                """, value, key);
        if (updated != 1) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Site details are not initialized in the database.");
        }
    }

    private String normalizeOptional(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeLineEndings(String value) {
        return value.replace("\r\n", "\n").replace('\r', '\n');
    }

    private void validateOptionalUrl(String label, String value) {
        if (value.isEmpty()) return;
        try {
            URI uri = URI.create(value);
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null
                    || uri.getUserInfo() != null) {
                throw new IllegalArgumentException("Invalid web URL.");
            }
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    label + " must be a valid http or https URL.",
                    exception);
        }
    }

    private record SiteSetting(String key, String value) {
    }

    public record SiteDetails(
            String email,
            String linkedinUrl,
            String xUrl,
            String location) {
    }

    public record SiteDetailsRequest(
            @NotBlank @Email @Size(max = 254) String email,
            @Size(max = 2048) String linkedinUrl,
            @Size(max = 2048) String xUrl,
            @NotBlank @Size(max = 500) String location) {
    }
}
