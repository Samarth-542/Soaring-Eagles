package in.soaringeagles.api.security;

import java.io.Console;
import java.nio.CharBuffer;
import java.util.Arrays;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Interactive one-time helper for provisioning APP_ADMIN_PASSWORD_HASH.
 * The password is read from the terminal and never accepted as a command-line argument.
 */
public final class AdminPasswordHashCli {

    private AdminPasswordHashCli() {
    }

    public static void main(String[] args) {
        if (args.length != 0) {
            throw new IllegalArgumentException("Do not pass passwords or hash values as command-line arguments.");
        }
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException(
                    "Run this command from an interactive terminal so the password is not echoed.");
        }

        char[] password = console.readPassword("New admin password: ");
        char[] confirmation = console.readPassword("Repeat admin password: ");
        try {
            if (password.length < 12) {
                throw new IllegalArgumentException("Use an admin password with at least 12 characters.");
            }
            if (!Arrays.equals(password, confirmation)) {
                throw new IllegalArgumentException("The passwords did not match.");
            }
            System.out.println(new BCryptPasswordEncoder(12).encode(CharBuffer.wrap(password)));
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(confirmation, '\0');
        }
    }
}
