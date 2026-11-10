package in.soaringeagles.api.content;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import in.soaringeagles.api.content.ContentRecords.JourneyMilestone;
import in.soaringeagles.api.content.ContentRecords.JourneyMilestoneRequest;
import in.soaringeagles.api.content.ContentRecords.Project;
import in.soaringeagles.api.content.ContentRecords.ProjectRequest;
import in.soaringeagles.api.content.ContentRecords.TeamGroup;
import in.soaringeagles.api.content.ContentRecords.TeamMember;
import in.soaringeagles.api.content.ContentRecords.TeamMemberRequest;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class ContentController {

    private final ContentService contentService;
    private final ContentImageStorageService imageStorageService;

    ContentController(ContentService contentService, ContentImageStorageService imageStorageService) {
        this.contentService = contentService;
        this.imageStorageService = imageStorageService;
    }

    @PostMapping(path = "/api/v1/admin/content/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ContentImageStorageService.UploadedImage uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("section") String section) {
        return imageStorageService.upload(file, section);
    }

    @GetMapping("/api/v1/content/team")
    public ResponseEntity<TeamDirectory> publicTeam() {
        List<TeamMember> members = contentService.listTeam(true);
        List<TeamGroup> groups = contentService.listGroups();
        TeamDirectory directory = new TeamDirectory(groups.stream()
                .map(group -> new TeamGroupView(
                        group.id(), group.title(), group.label(),
                        members.stream().filter(member -> member.groupId().equals(group.id())).toList()))
                .toList());
        return ResponseEntity.ok()
                .cacheControl(publicContentCache())
                .body(directory);
    }

    @GetMapping("/api/v1/content/projects")
    public ResponseEntity<List<Project>> publicProjects() {
        return ResponseEntity.ok()
                .cacheControl(publicContentCache())
                .body(contentService.listProjects(true));
    }

    @GetMapping("/api/v1/content/journey")
    public ResponseEntity<List<JourneyMilestone>> publicJourney() {
        return ResponseEntity.ok()
                .cacheControl(publicContentCache())
                .body(contentService.listJourney(true));
    }

    @GetMapping("/api/v1/admin/content/team")
    public AdminTeamDirectory adminTeam() {
        return new AdminTeamDirectory(contentService.listGroups(), contentService.listTeam(false));
    }

    @PostMapping("/api/v1/admin/content/team")
    public ResponseEntity<TeamMember> createTeamMember(@Valid @RequestBody TeamMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contentService.createTeamMember(request));
    }

    @PutMapping("/api/v1/admin/content/team/{id}")
    public TeamMember updateTeamMember(
            @PathVariable UUID id, @Valid @RequestBody TeamMemberRequest request) {
        return contentService.updateTeamMember(id, request);
    }

    @DeleteMapping("/api/v1/admin/content/team/{id}")
    public ResponseEntity<Void> deleteTeamMember(@PathVariable UUID id) {
        contentService.deleteTeamMember(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/admin/content/projects")
    public List<Project> adminProjects() {
        return contentService.listProjects(false);
    }

    @PostMapping("/api/v1/admin/content/projects")
    public ResponseEntity<Project> createProject(@Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contentService.createProject(request));
    }

    @PutMapping("/api/v1/admin/content/projects/{id}")
    public Project updateProject(@PathVariable UUID id, @Valid @RequestBody ProjectRequest request) {
        return contentService.updateProject(id, request);
    }

    @DeleteMapping("/api/v1/admin/content/projects/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        contentService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/admin/content/journey")
    public List<JourneyMilestone> adminJourney() {
        return contentService.listJourney(false);
    }

    @PostMapping("/api/v1/admin/content/journey")
    public ResponseEntity<JourneyMilestone> createJourneyMilestone(
            @Valid @RequestBody JourneyMilestoneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contentService.createJourneyMilestone(request));
    }

    @PutMapping("/api/v1/admin/content/journey/{id}")
    public JourneyMilestone updateJourneyMilestone(
            @PathVariable UUID id, @Valid @RequestBody JourneyMilestoneRequest request) {
        return contentService.updateJourneyMilestone(id, request);
    }

    @DeleteMapping("/api/v1/admin/content/journey/{id}")
    public ResponseEntity<Void> deleteJourneyMilestone(@PathVariable UUID id) {
        contentService.deleteJourneyMilestone(id);
        return ResponseEntity.noContent().build();
    }

    public record TeamGroupView(String id, String title, String label, List<TeamMember> members) {
    }

    public record TeamDirectory(List<TeamGroupView> groups) {
    }

    public record AdminTeamDirectory(List<TeamGroup> groups, List<TeamMember> members) {
    }

    private CacheControl publicContentCache() {
        return CacheControl.maxAge(Duration.ofMinutes(5))
                .cachePublic()
                .staleWhileRevalidate(Duration.ofMinutes(5));
    }
}
