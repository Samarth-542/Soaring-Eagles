package in.soaringeagles.api.security;

import java.util.Locale;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminAuthenticationConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public UserDetailsService userDetailsService(JdbcTemplate jdbc) {
        return username -> {
            if (username == null) {
                throw new UsernameNotFoundException("Invalid credentials.");
            }
            return jdbc.query("""
                    SELECT email, password_hash, enabled
                    FROM admin_accounts
                    WHERE email = ?
                    """, resultSet -> {
                if (!resultSet.next()) {
                    throw new UsernameNotFoundException("Invalid credentials.");
                }
                return User.withUsername(resultSet.getString("email").toLowerCase(Locale.ROOT))
                        .password(resultSet.getString("password_hash"))
                        .roles("ADMIN")
                        .disabled(!resultSet.getBoolean("enabled"))
                        .build();
            }, username.trim().toLowerCase(Locale.ROOT));
        };
    }
}
