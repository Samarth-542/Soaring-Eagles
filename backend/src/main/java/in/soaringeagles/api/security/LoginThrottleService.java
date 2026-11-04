package in.soaringeagles.api.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class LoginThrottleService {

    static final int MAX_FAILURES = 5;
    static final Duration WINDOW = Duration.ofMinutes(15);
    static final Duration LOCKOUT = Duration.ofMinutes(15);
    private static final int MAX_TRACKED_KEYS = 10_000;

    private final Clock clock;
    private final Map<String, AttemptWindow> attempts = new LinkedHashMap<>();

    public LoginThrottleService() {
        this(Clock.systemUTC());
    }

    LoginThrottleService(Clock clock) {
        this.clock = clock;
    }

    public synchronized Duration retryAfter(String email, String remoteAddress) {
        Instant now = clock.instant();
        removeExpired(now);
        Duration remaining = Duration.ZERO;
        for (String key : keys(email, remoteAddress)) {
            AttemptWindow window = attempts.get(key);
            if (window != null && window.lockedUntil != null && window.lockedUntil.isAfter(now)) {
                Duration candidate = Duration.between(now, window.lockedUntil);
                if (candidate.compareTo(remaining) > 0) {
                    remaining = candidate;
                }
            }
        }
        return attempts.size() >= MAX_TRACKED_KEYS ? LOCKOUT : remaining;
    }

    public synchronized Duration recordFailure(String email, String remoteAddress) {
        Instant now = clock.instant();
        removeExpired(now);

        List<String> keys = keys(email, remoteAddress);
        if (attempts.size() + keys.stream().filter(key -> !attempts.containsKey(key)).count()
                > MAX_TRACKED_KEYS) {
            return LOCKOUT;
        }

        Duration remaining = Duration.ZERO;
        for (String key : keys) {
            AttemptWindow window = attempts.get(key);
            if (window == null || !now.isBefore(window.startedAt.plus(WINDOW))) {
                window = new AttemptWindow(now);
                attempts.put(key, window);
            }

            window.failures++;
            if (window.failures >= MAX_FAILURES) {
                window.lockedUntil = now.plus(LOCKOUT);
            }
            if (window.lockedUntil != null && window.lockedUntil.isAfter(now)) {
                Duration candidate = Duration.between(now, window.lockedUntil);
                if (candidate.compareTo(remaining) > 0) {
                    remaining = candidate;
                }
            }
        }
        return remaining;
    }

    public synchronized void recordSuccess(String email, String remoteAddress) {
        for (String key : keys(email, remoteAddress)) {
            attempts.remove(key);
        }
    }

    private void removeExpired(Instant now) {
        attempts.entrySet().removeIf(entry -> {
            AttemptWindow window = entry.getValue();
            Instant expiry = window.lockedUntil == null
                    ? window.startedAt.plus(WINDOW)
                    : window.lockedUntil;
            return !expiry.isAfter(now);
        });
    }

    private List<String> keys(String email, String remoteAddress) {
        String normalizedEmail = email == null
                ? ""
                : email.trim().toLowerCase(Locale.ROOT);
        String normalizedAddress = remoteAddress == null ? "" : remoteAddress;
        return List.of(
                digest("email:" + normalizedEmail),
                digest("address:" + normalizedAddress));
    }

    private String digest(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required by the Java runtime.", exception);
        }
    }

    private static final class AttemptWindow {
        private final Instant startedAt;
        private int failures;
        private Instant lockedUntil;

        private AttemptWindow(Instant startedAt) {
            this.startedAt = startedAt;
        }
    }
}
