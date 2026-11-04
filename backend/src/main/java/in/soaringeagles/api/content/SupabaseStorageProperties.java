package in.soaringeagles.api.content;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.supabase.storage")
public record SupabaseStorageProperties(String url, String serviceRoleKey, String publicBucket) {

    public boolean isConfigured() {
        return url != null && !url.isBlank()
                && serviceRoleKey != null && !serviceRoleKey.isBlank()
                && publicBucket != null && !publicBucket.isBlank();
    }

    public String normalizedUrl() {
        return url == null ? "" : url.replaceAll("/+$", "");
    }
}
