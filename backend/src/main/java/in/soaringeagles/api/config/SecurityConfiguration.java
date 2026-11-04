package in.soaringeagles.api.config;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.soaringeagles.api.security.LoginThrottleFilter;
import in.soaringeagles.api.security.LoginThrottleService;
import in.soaringeagles.api.security.ProblemDetailAccessDeniedHandler;
import in.soaringeagles.api.security.ProblemDetailAuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectMapper objectMapper,
            LoginThrottleService loginThrottleService,
            CsrfTokenRepository csrfTokenRepository) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/api/v1/payments/razorpay/webhook"))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(new ProblemDetailAuthenticationEntryPoint(objectMapper))
                        .accessDeniedHandler(new ProblemDetailAccessDeniedHandler(objectMapper)))
                .requestCache(requestCache -> requestCache.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/events", "/api/v1/events/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/content/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/events/*/registrations").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/registrations/*/status").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/registrations/*/retry").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/razorpay/webhook").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/session").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").hasRole("ADMIN")
                        .anyRequest().hasRole("ADMIN"))
                .formLogin(formLogin -> formLogin
                        .loginPage("/api/v1/auth/login")
                        .loginProcessingUrl("/api/v1/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(authenticationSuccessHandler(loginThrottleService))
                        .failureHandler(authenticationFailureHandler(loginThrottleService, objectMapper))
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setHeader("Cache-Control", "no-store");
                            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                        }))
                .addFilterBefore(
                        new LoginThrottleFilter(loginThrottleService, objectMapper),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CsrfTokenRepository csrfTokenRepository(
            @Value("${server.servlet.session.cookie.secure:true}") boolean secureCookie,
            @Value("${server.servlet.session.cookie.same-site:lax}") String sameSite) {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(cookie -> cookie
                .path("/")
                .secure(secureCookie)
                .sameSite(sameSite));
        return repository;
    }

    private AuthenticationSuccessHandler authenticationSuccessHandler(
            LoginThrottleService loginThrottleService) {
        return (request, response, authentication) -> {
            loginThrottleService.recordSuccess(authentication.getName(), request.getRemoteAddr());
            response.setStatus(HttpServletResponse.SC_OK);
            response.setHeader("Cache-Control", "no-store");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"authenticated\":true}");
        };
    }

    private AuthenticationFailureHandler authenticationFailureHandler(
            LoginThrottleService loginThrottleService,
            ObjectMapper objectMapper) {
        return (request, response, exception) -> {
            var retryAfter = loginThrottleService.recordFailure(
                    request.getParameter("email"),
                    request.getRemoteAddr());
            if (!retryAfter.isZero()) {
                LoginThrottleFilter.writeRateLimit(response, retryAfter, objectMapper);
                return;
            }
            LoginThrottleFilter.writeUnauthorized(response, objectMapper);
        };
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.allowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Idempotency-Key", "X-Request-Id", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
