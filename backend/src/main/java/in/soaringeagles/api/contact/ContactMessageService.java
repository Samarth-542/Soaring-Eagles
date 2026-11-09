package in.soaringeagles.api.contact;

import java.io.IOException;
import java.security.Principal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ContactMessageService {

    private final JdbcTemplate jdbc;
    private final ContactAttachmentStorageService attachmentStorage;

    ContactMessageService(JdbcTemplate jdbc, ContactAttachmentStorageService attachmentStorage) {
        this.jdbc = jdbc;
        this.attachmentStorage = attachmentStorage;
    }

    @Transactional
    public UUID submit(String name, String email, String subject, String message, MultipartFile upload)
            throws IOException {
        UUID id = UUID.randomUUID();
        ContactAttachmentStorageService.StoredAttachment attachment =
                upload == null || upload.isEmpty() ? null : attachmentStorage.upload(id, upload);
        try {
            jdbc.update("""
                    INSERT INTO contact_messages (
                        id, sender_name, sender_email, subject, message, state,
                        attachment_storage_key, attachment_file_name, attachment_content_type,
                        attachment_size_bytes, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, 'NEW', ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """,
                    id,
                    name.trim(),
                    email.trim(),
                    subject.trim(),
                    message.trim(),
                    attachment == null ? null : attachment.storageKey(),
                    attachment == null ? null : attachment.fileName(),
                    attachment == null ? null : attachment.contentType(),
                    attachment == null ? null : attachment.sizeBytes());
        } catch (DataAccessException exception) {
            if (attachment != null) {
                try {
                    attachmentStorage.delete(attachment.storageKey());
                } catch (ResponseStatusException cleanupException) {
                    exception.addSuppressed(cleanupException);
                }
            }
            throw exception;
        }
        return id;
    }

    @Transactional(readOnly = true)
    public ContactMessagePage list(ContactMessageState state, int page, int size) {
        List<ContactMessageListItem> messages = state == null
                ? jdbc.query("""
                        SELECT id, sender_name, sender_email, subject, state, attachment_file_name, created_at
                        FROM contact_messages
                        ORDER BY created_at DESC, id
                        LIMIT ? OFFSET ?
                        """, this::mapListItem, size, (long) page * size)
                : jdbc.query("""
                        SELECT id, sender_name, sender_email, subject, state, attachment_file_name, created_at
                        FROM contact_messages
                        WHERE state = ?
                        ORDER BY created_at DESC, id
                        LIMIT ? OFFSET ?
                        """, this::mapListItem, state.name(), size, (long) page * size);
        Long total = state == null
                ? jdbc.queryForObject("SELECT COUNT(*) FROM contact_messages", Long.class)
                : jdbc.queryForObject("SELECT COUNT(*) FROM contact_messages WHERE state = ?", Long.class, state.name());
        int totalPages = total == null || total == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new ContactMessagePage(messages, total == null ? 0 : total, totalPages, page, size);
    }

    @Transactional(readOnly = true)
    public ContactMessageView get(UUID id) {
        return findMessage(id);
    }

    @Transactional
    public ContactMessageView updateState(UUID id, ContactMessageState state, Principal actor) {
        ContactMessageView current = findMessage(id);
        if (current.state() == state) {
            return current;
        }
        jdbc.update("""
                UPDATE contact_messages
                SET state = ?, updated_at = CURRENT_TIMESTAMP, version = version + 1
                WHERE id = ?
                """, state.name(), id);
        recordAudit(actor.getName(), auditAction(state), id);
        return findMessage(id);
    }

    @Transactional(readOnly = true)
    public ContactAttachment downloadAttachment(UUID id) {
        List<StoredAttachmentMetadata> attachments = jdbc.query("""
                SELECT attachment_storage_key, attachment_file_name, attachment_content_type
                FROM contact_messages
                WHERE id = ?
                """, (resultSet, row) -> new StoredAttachmentMetadata(
                resultSet.getString("attachment_storage_key"),
                resultSet.getString("attachment_file_name"),
                resultSet.getString("attachment_content_type")), id);
        if (attachments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact message not found.");
        }
        StoredAttachmentMetadata metadata = attachments.get(0);
        if (metadata.storageKey() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This message has no attachment.");
        }
        return new ContactAttachment(
                metadata.fileName(),
                metadata.contentType(),
                attachmentStorage.download(metadata.storageKey()));
    }

    private ContactMessageListItem mapListItem(ResultSet resultSet, int row) throws SQLException {
        return new ContactMessageListItem(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("sender_name"),
                resultSet.getString("sender_email"),
                resultSet.getString("subject"),
                ContactMessageState.valueOf(resultSet.getString("state")),
                resultSet.getString("attachment_file_name"),
                resultSet.getTimestamp("created_at").toInstant());
    }

    private ContactMessageView findMessage(UUID id) {
        List<ContactMessageView> messages = jdbc.query("""
                SELECT id, sender_name, sender_email, subject, message, state,
                       attachment_file_name, attachment_content_type,
                       created_at, updated_at
                FROM contact_messages
                WHERE id = ?
                """, (resultSet, row) -> new ContactMessageView(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("sender_name"),
                resultSet.getString("sender_email"),
                resultSet.getString("subject"),
                resultSet.getString("message"),
                ContactMessageState.valueOf(resultSet.getString("state")),
                resultSet.getString("attachment_file_name"),
                resultSet.getString("attachment_content_type"),
                resultSet.getTimestamp("created_at").toInstant(),
                resultSet.getTimestamp("updated_at").toInstant()), id);
        if (messages.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact message not found.");
        }
        return messages.get(0);
    }

    private void recordAudit(String actorEmail, String action, UUID messageId) {
        UUID actorId = jdbc.queryForObject(
                "SELECT id FROM admin_accounts WHERE email = ?", UUID.class, actorEmail);
        jdbc.update("""
                INSERT INTO admin_audit_events (id, admin_id, action, resource_type, resource_id, created_at)
                VALUES (?, ?, ?, 'CONTACT_MESSAGE', ?, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), actorId, action, messageId.toString());
    }

    private String auditAction(ContactMessageState state) {
        return switch (state) {
            case NEW -> "CONTACT_MESSAGE_MARKED_UNREAD";
            case IN_PROGRESS -> "CONTACT_MESSAGE_MARKED_READ";
            case ARCHIVED -> "CONTACT_MESSAGE_ARCHIVED";
        };
    }

    private record StoredAttachmentMetadata(String storageKey, String fileName, String contentType) {
    }

    public record ContactMessageListItem(
            UUID id,
            String senderName,
            String senderEmail,
            String subject,
            ContactMessageState state,
            String attachmentFileName,
            Instant createdAt) {
    }

    public record ContactMessageView(
            UUID id,
            String senderName,
            String senderEmail,
            String subject,
            String message,
            ContactMessageState state,
            String attachmentFileName,
            String attachmentContentType,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record ContactMessagePage(
            List<ContactMessageListItem> content,
            long totalElements,
            int totalPages,
            int number,
            int size) {
    }

    public record ContactAttachment(String fileName, String contentType, byte[] content) {
    }
}
