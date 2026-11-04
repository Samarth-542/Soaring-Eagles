package in.soaringeagles.api;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;

final class TestAdminCredentials {

    static final String EMAIL = "admin@example.invalid";
    static final String PASSWORD = "test-only-not-a-real-admin-password";
    private static final String PASSWORD_HASH = new BCryptPasswordEncoder(4).encode(PASSWORD);

    private TestAdminCredentials() {
    }

    static void register(DynamicPropertyRegistry registry) {
        registry.add("app.admin.email", () -> EMAIL);
        registry.add("app.admin.password-hash", () -> PASSWORD_HASH);
    }
}
