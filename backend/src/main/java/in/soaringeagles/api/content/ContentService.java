package in.soaringeagles.api.content;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import in.soaringeagles.api.content.ContentRecords.JourneyMilestone;
import in.soaringeagles.api.content.ContentRecords.JourneyMilestoneRequest;
import in.soaringeagles.api.content.ContentRecords.Project;
import in.soaringeagles.api.content.ContentRecords.ProjectRequest;
import in.soaringeagles.api.content.ContentRecords.TeamGroup;
import in.soaringeagles.api.content.ContentRecords.TeamMember;
import in.soaringeagles.api.content.ContentRecords.TeamMemberRequest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ContentService {

    private final JdbcTemplate jdbc;

    ContentService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public List<TeamGroup> listGroups() {
        return jdbc.query("""
                SELECT id, title, label, position
                FROM team_groups
                ORDER BY position, id
                """, (rs, row) -> new TeamGroup(
                rs.getString("id"), rs.getString("title"), rs.getString("label"), rs.getInt("position")));
    }

    @Transactional(readOnly = true)
    public List<TeamMember> listTeam(boolean publishedOnly) {
        return jdbc.query("""
                SELECT m.id, m.group_id, g.title AS group_title, g.label AS group_label,
                       m.name, m.role, m.image_url, m.linkedin_url, m.instagram_url, m.x_url,
                       m.position, m.published
                FROM team_members m
                JOIN team_groups g ON g.id = m.group_id
                WHERE (? = FALSE OR m.published = TRUE)
                ORDER BY g.position, m.position, m.name
                """, (rs, row) -> new TeamMember(
                rs.getObject("id", UUID.class),
                rs.getString("group_id"),
                rs.getString("group_title"),
                rs.getString("group_label"),
                rs.getString("name"),
                rs.getString("role"),
                rs.getString("image_url"),
                rs.getString("linkedin_url"),
                rs.getString("instagram_url"),
                rs.getString("x_url"),
                rs.getInt("position"),
                rs.getBoolean("published")), !publishedOnly);
    }

    public TeamMember createTeamMember(TeamMemberRequest request) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO team_members
                    (id, group_id, name, role, image_url, linkedin_url, instagram_url, x_url,
                     position, published, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, id, request.groupId(), request.name().trim(), request.role().trim(),
                request.imageUrl().trim(), optional(request.linkedinUrl()), optional(request.instagramUrl()),
                optional(request.xUrl()), request.position(), request.published());
        return findTeamMember(id);
    }

    public ContentMutation<TeamMember> updateTeamMember(UUID id, TeamMemberRequest request) {
        boolean wasPublished = wasPublished("team_members", id, "Team member");
        int updated = jdbc.update("""
                UPDATE team_members
                SET group_id = ?, name = ?, role = ?, image_url = ?, linkedin_url = ?,
                    instagram_url = ?, x_url = ?, position = ?, published = ?,
                    updated_at = CURRENT_TIMESTAMP, version = version + 1
                WHERE id = ?
                """, request.groupId(), request.name().trim(), request.role().trim(),
                request.imageUrl().trim(), optional(request.linkedinUrl()), optional(request.instagramUrl()),
                optional(request.xUrl()), request.position(), request.published(), id);
        if (updated == 0) throw notFound("Team member");
        TeamMember member = findTeamMember(id);
        return new ContentMutation<>(member, wasPublished || member.published());
    }

    public boolean deleteTeamMember(UUID id) {
        boolean wasPublished = wasPublished("team_members", id, "Team member");
        if (jdbc.update("DELETE FROM team_members WHERE id = ?", id) == 0) throw notFound("Team member");
        return wasPublished;
    }

    @Transactional(readOnly = true)
    public List<Project> listProjects(boolean publishedOnly) {
        List<ProjectRow> rows = jdbc.query("""
                SELECT id, title, project_year, description, image_url, position, published
                FROM projects
                WHERE (? = FALSE OR published = TRUE)
                ORDER BY project_year DESC, position, title
                """, (rs, row) -> new ProjectRow(
                rs.getObject("id", UUID.class), rs.getString("title"), rs.getInt("project_year"),
                rs.getString("description"), rs.getString("image_url"), rs.getInt("position"),
                rs.getBoolean("published")), !publishedOnly);
        if (rows.isEmpty()) return List.of();

        String placeholders = String.join(",", java.util.Collections.nCopies(rows.size(), "?"));
        Object[] projectIds = rows.stream().map(ProjectRow::id).toArray();
        Map<UUID, List<String>> tagsByProject = new HashMap<>();
        rows.forEach(row -> tagsByProject.put(row.id(), new java.util.ArrayList<>()));
        jdbc.query("""
                SELECT project_id, tag
                FROM project_tags
                WHERE project_id IN (%s)
                ORDER BY project_id, position
                """.formatted(placeholders), rs -> {
            UUID projectId = rs.getObject("project_id", UUID.class);
            tagsByProject.get(projectId).add(rs.getString("tag"));
        }, projectIds);
        return rows.stream().map(row -> toProject(row, tagsByProject.get(row.id()))).toList();
    }

    public Project createProject(ProjectRequest request) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO projects
                    (id, title, project_year, description, image_url, position, published, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, id, request.title().trim(), request.year(), request.description().trim(),
                request.imageUrl().trim(), request.position(), request.published());
        replaceTags(id, request.tags());
        return findProject(id);
    }

    public ContentMutation<Project> updateProject(UUID id, ProjectRequest request) {
        boolean wasPublished = wasPublished("projects", id, "Project");
        int updated = jdbc.update("""
                UPDATE projects
                SET title = ?, project_year = ?, description = ?, image_url = ?, position = ?,
                    published = ?, updated_at = CURRENT_TIMESTAMP, version = version + 1
                WHERE id = ?
                """, request.title().trim(), request.year(), request.description().trim(),
                request.imageUrl().trim(), request.position(), request.published(), id);
        if (updated == 0) throw notFound("Project");
        replaceTags(id, request.tags());
        Project project = findProject(id);
        return new ContentMutation<>(project, wasPublished || project.published());
    }

    public boolean deleteProject(UUID id) {
        boolean wasPublished = wasPublished("projects", id, "Project");
        if (jdbc.update("DELETE FROM projects WHERE id = ?", id) == 0) throw notFound("Project");
        return wasPublished;
    }

    @Transactional(readOnly = true)
    public List<JourneyMilestone> listJourney(boolean publishedOnly) {
        return jdbc.query("""
                SELECT id, milestone_year, title, description, image_url, position, published
                FROM journey_milestones
                WHERE (? = FALSE OR published = TRUE)
                ORDER BY milestone_year DESC, position, title
                """, (rs, row) -> new JourneyMilestone(
                rs.getObject("id", UUID.class), rs.getInt("milestone_year"), rs.getString("title"),
                rs.getString("description"), rs.getString("image_url"), rs.getInt("position"),
                rs.getBoolean("published")), !publishedOnly);
    }

    public JourneyMilestone createJourneyMilestone(JourneyMilestoneRequest request) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO journey_milestones
                    (id, milestone_year, title, description, image_url, position, published, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, id, request.year(), request.title().trim(), request.description().trim(),
                request.imageUrl().trim(), request.position(), request.published());
        return findJourneyMilestone(id);
    }

    public ContentMutation<JourneyMilestone> updateJourneyMilestone(UUID id, JourneyMilestoneRequest request) {
        boolean wasPublished = wasPublished("journey_milestones", id, "Journey milestone");
        int updated = jdbc.update("""
                UPDATE journey_milestones
                SET milestone_year = ?, title = ?, description = ?, image_url = ?, position = ?,
                    published = ?, updated_at = CURRENT_TIMESTAMP, version = version + 1
                WHERE id = ?
                """, request.year(), request.title().trim(), request.description().trim(),
                request.imageUrl().trim(), request.position(), request.published(), id);
        if (updated == 0) throw notFound("Journey milestone");
        JourneyMilestone milestone = findJourneyMilestone(id);
        return new ContentMutation<>(milestone, wasPublished || milestone.published());
    }

    public boolean deleteJourneyMilestone(UUID id) {
        boolean wasPublished = wasPublished("journey_milestones", id, "Journey milestone");
        if (jdbc.update("DELETE FROM journey_milestones WHERE id = ?", id) == 0) throw notFound("Journey milestone");
        return wasPublished;
    }

    private TeamMember findTeamMember(UUID id) {
        return jdbc.query("""
                SELECT m.id, m.group_id, g.title AS group_title, g.label AS group_label,
                       m.name, m.role, m.image_url, m.linkedin_url, m.instagram_url, m.x_url,
                       m.position, m.published
                FROM team_members m JOIN team_groups g ON g.id = m.group_id
                WHERE m.id = ?
                """, (rs, row) -> new TeamMember(
                rs.getObject("id", UUID.class), rs.getString("group_id"), rs.getString("group_title"),
                rs.getString("group_label"), rs.getString("name"), rs.getString("role"),
                rs.getString("image_url"), rs.getString("linkedin_url"), rs.getString("instagram_url"),
                rs.getString("x_url"), rs.getInt("position"), rs.getBoolean("published")), id)
                .stream().findFirst().orElseThrow(() -> notFound("Team member"));
    }

    private Project findProject(UUID id) {
        return jdbc.query("""
                SELECT id, title, project_year, description, image_url, position, published
                FROM projects WHERE id = ?
                """, (rs, row) -> new ProjectRow(
                rs.getObject("id", UUID.class), rs.getString("title"), rs.getInt("project_year"),
                rs.getString("description"), rs.getString("image_url"), rs.getInt("position"),
                rs.getBoolean("published")), id).stream().findFirst().map(this::toProject)
                .orElseThrow(() -> notFound("Project"));
    }

    private JourneyMilestone findJourneyMilestone(UUID id) {
        return jdbc.query("""
                SELECT id, milestone_year, title, description, image_url, position, published
                FROM journey_milestones WHERE id = ?
                """, (rs, row) -> new JourneyMilestone(
                rs.getObject("id", UUID.class), rs.getInt("milestone_year"), rs.getString("title"),
                rs.getString("description"), rs.getString("image_url"), rs.getInt("position"),
                rs.getBoolean("published")), id).stream().findFirst()
                .orElseThrow(() -> notFound("Journey milestone"));
    }

    private Project toProject(ProjectRow row) {
        List<String> tags = jdbc.query("""
                SELECT tag FROM project_tags WHERE project_id = ? ORDER BY position
                """, (rs, index) -> rs.getString("tag"), row.id());
        return toProject(row, tags);
    }

    private Project toProject(ProjectRow row, List<String> tags) {
        return new Project(row.id(), row.title(), row.year(), row.description(), row.imageUrl(),
                tags, row.position(), row.published());
    }

    private void replaceTags(UUID projectId, List<String> tags) {
        jdbc.update("DELETE FROM project_tags WHERE project_id = ?", projectId);
        for (int position = 0; position < tags.size(); position++) {
            jdbc.update("INSERT INTO project_tags (project_id, position, tag) VALUES (?, ?, ?)",
                    projectId, position, tags.get(position).trim());
        }
    }

    private String optional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean wasPublished(String table, UUID id, String resource) {
        List<Boolean> published = jdbc.query(
                "SELECT published FROM " + table + " WHERE id = ? FOR UPDATE",
                (rs, row) -> rs.getBoolean("published"),
                id);
        if (published.isEmpty()) throw notFound(resource);
        return published.get(0);
    }

    private ResponseStatusException notFound(String resource) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, resource + " not found.");
    }

    private record ProjectRow(
            UUID id, String title, int year, String description, String imageUrl, int position, boolean published) {
    }

    public record ContentMutation<T>(T record, boolean publishedContentChanged) {
    }
}
