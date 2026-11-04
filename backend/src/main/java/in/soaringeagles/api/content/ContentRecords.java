package in.soaringeagles.api.content;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ContentRecords {

    private ContentRecords() {
    }

    public record TeamGroup(String id, String title, String label, int position) {
    }

    public record TeamMember(
            UUID id,
            String groupId,
            String groupTitle,
            String groupLabel,
            String name,
            String role,
            String imageUrl,
            String linkedinUrl,
            String instagramUrl,
            String xUrl,
            int position,
            boolean published) {
    }

    public record TeamMemberRequest(
            @NotBlank @Size(max = 50) String groupId,
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 180) String role,
            @NotBlank @Size(max = 2048) String imageUrl,
            @Size(max = 2048) String linkedinUrl,
            @Size(max = 2048) String instagramUrl,
            @Size(max = 2048) String xUrl,
            @NotNull @Min(0) Integer position,
            boolean published) {
    }

    public record Project(
            UUID id,
            String title,
            int year,
            String description,
            String imageUrl,
            List<String> tags,
            int position,
            boolean published) {
    }

    public record ProjectRequest(
            @NotBlank @Size(max = 180) String title,
            @NotNull @Min(1900) @Max(2200) Integer year,
            @NotBlank @Size(max = 12000) String description,
            @NotBlank @Size(max = 2048) String imageUrl,
            @NotNull @Size(max = 20) List<@NotBlank @Size(max = 80) String> tags,
            @NotNull @Min(0) Integer position,
            boolean published) {
    }

    public record JourneyMilestone(
            UUID id,
            int year,
            String title,
            String description,
            String imageUrl,
            int position,
            boolean published) {
    }

    public record JourneyMilestoneRequest(
            @NotNull @Min(1900) @Max(2200) Integer year,
            @NotBlank @Size(max = 200) String title,
            @NotBlank @Size(max = 12000) String description,
            @NotBlank @Size(max = 2048) String imageUrl,
            @NotNull @Min(0) Integer position,
            boolean published) {
    }
}
