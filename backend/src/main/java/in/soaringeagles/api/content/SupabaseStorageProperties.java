package in.soaringeagles.api.content;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.supabase.storage")
public record SupabaseStorageProperties(
        String url, String serviceRoleKey, String publicBucket, String privateBucket) {

    public boolean isConfigured() {
        return url != null && !url.isBlank()
                && serviceRoleKey != null && !serviceRoleKey.isBlank()
                && publicBucket != null && !publicBucket.isBlank();
    }

    public boolean isPrivateConfigured() {
        return url != null && !url.isBlank()
                && serviceRoleKey != null && !serviceRoleKey.isBlank()
                && privateBucket != null && !privateBucket.isBlank();
    }

    public String normalizedUrl() {
        return url == null ? "" : url.replaceAll("/+$", "");
    }
}
