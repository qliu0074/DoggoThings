package app.nail.infra.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

/** English: Logs API requests/responses for auditing. */
@Slf4j
@Component
public class ApiLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Instant start = Instant.now();
        String path = request.getRequestURI();
        String method = request.getMethod();
        String query = request.getQueryString();

        filterChain.doFilter(request, response);

        long duration = Duration.between(start, Instant.now()).toMillis();
        int status = response.getStatus();
        String correlationId = MDC.get("correlationId");
        log.info("API {} {}{} -> {} ({} ms) corrId={}",
                method,
                path,
                query == null ? "" : "?" + query,
                status,
                duration,
                correlationId);
    }
}
