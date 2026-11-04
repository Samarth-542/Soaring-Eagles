package in.soaringeagles.api.security;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Validated
@RestController
@RequestMapping("/api/v1/admin/accounts")
public class AdminAccountController {

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountController(JdbcTemplate jdbc, PasswordEncoder passwordEncoder) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<AdminAccountView> listAccounts() {
        return jdbc.query("""
                SELECT id, email, enabled, created_at
                FROM admin_accounts
                ORDER BY enabled DESC, created_at, email
                """, (resultSet, row) -> new AdminAccountView(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("email"),
                resultSet.getBoolean("enabled"),
                resultSet.getTimestamp("created_at").toInstant().toString()));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<AdminAccountView> createAccount(
            @Valid @RequestBody CreateAdminAccountRequest request, Principal actor) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passwords must be no more than 72 UTF-8 bytes.");
        }
        if (jdbc.queryForObject("SELECT COUNT(*) FROM admin_accounts WHERE email = ?", Integer.class, email) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An admin account with that email already exists.");
        }

        UUID id = UUID.randomUUID();
        try {
            jdbc.update("""
                    INSERT INTO admin_accounts (id, email, password_hash, enabled, created_at, updated_at)
                    VALUES (?, ?, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """, id, email, passwordEncoder.encode(request.password()));
        } catch (DuplicateKeyException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An admin account with that email already exists.");
        }
        recordAudit(actor.getName(), "ADMIN_ACCOUNT_CREATED", id);
        return ResponseEntity.status(HttpStatus.CREATED).body(findAccount(id));
    }

    @PatchMapping("/{id}/enabled")
    @Transactional
    public AdminAccountView setAccountEnabled(
            @PathVariable UUID id,
            @Valid @RequestBody SetAdminAccountEnabledRequest request,
            Principal actor) {
        if (!request.enabled()) {
            jdbc.query("SELECT id FROM admin_accounts WHERE enabled = TRUE FOR UPDATE",
                    (resultSet, row) -> resultSet.getObject("id", UUID.class));
        }
        AdminAccountView account = findAccount(id);
        if (account.enabled() == request.enabled()) {
            return account;
        }
        if (!request.enabled()) {
            int enabledCount = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM admin_accounts WHERE enabled = TRUE", Integer.class);
            if (enabledCount <= 1) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "The last active admin account cannot be deactivated.");
            }
            if (account.email().equalsIgnoreCase(actor.getName())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "You cannot deactivate your own account.");
            }
        }

        jdbc.update("""
                UPDATE admin_accounts
                SET enabled = ?, updated_at = CURRENT_TIMESTAMP, version = version + 1
                WHERE id = ?
                """, request.enabled(), id);
        if (!request.enabled()) {
            jdbc.update("DELETE FROM SPRING_SESSION WHERE PRINCIPAL_NAME = ?", account.email());
        }
        recordAudit(actor.getName(),
                request.enabled() ? "ADMIN_ACCOUNT_ACTIVATED" : "ADMIN_ACCOUNT_DEACTIVATED", id);
        return findAccount(id);
    }

    private AdminAccountView findAccount(UUID id) {
        List<AdminAccountView> accounts = jdbc.query("""
                SELECT id, email, enabled, created_at
                FROM admin_accounts
                WHERE id = ?
                """, (resultSet, row) -> new AdminAccountView(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("email"),
                resultSet.getBoolean("enabled"),
                resultSet.getTimestamp("created_at").toInstant().toString()), id);
        if (accounts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin account not found.");
        }
        return accounts.get(0);
    }

    private void recordAudit(String actorEmail, String action, UUID resourceId) {
        UUID actorId = jdbc.queryForObject(
                "SELECT id FROM admin_accounts WHERE email = ?", UUID.class, actorEmail);
        jdbc.update("""
                INSERT INTO admin_audit_events (id, admin_id, action, resource_type, resource_id, created_at)
                VALUES (?, ?, ?, 'ADMIN_ACCOUNT', ?, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), actorId, action, resourceId.toString());
    }

    public record AdminAccountView(UUID id, String email, boolean enabled, String createdAt) {
    }

    public record CreateAdminAccountRequest(
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 12, max = 72) String password) {
    }

    public record SetAdminAccountEnabledRequest(@NotNull Boolean enabled) {
    }
}
