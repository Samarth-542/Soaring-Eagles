package in.soaringeagles.api.security;

import java.util.UUID;

import in.soaringeagles.api.config.AdminProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountBootstrap implements ApplicationRunner {

    private final JdbcTemplate jdbc;
    private final AdminProperties properties;

    public AdminAccountBootstrap(JdbcTemplate jdbc, AdminProperties properties) {
        this.jdbc = jdbc;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        int existing = jdbc.queryForObject(
                "SELECT COUNT(*) FROM admin_accounts WHERE email = ?",
                Integer.class,
                properties.normalizedEmail());
        if (existing == 0) {
            jdbc.update("""
                    INSERT INTO admin_accounts (id, email, password_hash, enabled, created_at, updated_at)
                    VALUES (?, ?, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """, UUID.randomUUID(), properties.normalizedEmail(), properties.passwordHash());
        }
    }
}
