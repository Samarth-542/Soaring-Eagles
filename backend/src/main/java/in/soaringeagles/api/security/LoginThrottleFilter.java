package in.soaringeagles.api.security;

import java.io.IOException;
import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.filter.OncePerRequestFilter;

public final class LoginThrottleFilter extends OncePerRequestFilter {

    static final String LOGIN_PATH = "/api/v1/auth/login";

    private final LoginThrottleService throttleService;
    private final ObjectMapper objectMapper;

    public LoginThrottleFilter(LoginThrottleService throttleService, ObjectMapper objectMapper) {
        this.throttleService = throttleService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestPath = request.getRequestURI().substring(contextPath.length());
        return !("POST".equals(request.getMethod()) && LOGIN_PATH.equals(requestPath));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (request.getQueryString() != null || !isFormUrlEncoded(request.getContentType())) {
            writeBadRequest(response, objectMapper);
            return;
        }

        Duration retryAfter = throttleService.retryAfter(
                request.getParameter("email"),
                request.getRemoteAddr());
        if (!retryAfter.isZero()) {
            writeRateLimit(response, retryAfter, objectMapper);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isFormUrlEncoded(String contentType) {
        if (contentType == null) return false;
        try {
            return MediaType.APPLICATION_FORM_URLENCODED
                    .isCompatibleWith(MediaType.parseMediaType(contentType));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static void writeBadRequest(HttpServletResponse response, ObjectMapper objectMapper)
            throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setHeader("Cache-Control", "no-store");
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Credentials must be sent in an application/x-www-form-urlencoded request body.");
        problem.setTitle("Invalid sign-in request");
        objectMapper.writeValue(response.getOutputStream(), problem);
    }

    public static void writeRateLimit(
            HttpServletResponse response,
            Duration retryAfter,
            ObjectMapper objectMapper) throws IOException {
        long seconds = Math.max(1, (retryAfter.toMillis() + 999) / 1000);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", Long.toString(seconds));
        response.setHeader("Cache-Control", "no-store");
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too many sign-in attempts. Try again later.");
        problem.setTitle("Too Many Requests");
        objectMapper.writeValue(response.getOutputStream(), problem);
    }

    public static void writeUnauthorized(HttpServletResponse response, ObjectMapper objectMapper)
            throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setHeader("Cache-Control", "no-store");
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Email or password is incorrect.");
        problem.setTitle("Unauthorized");
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
