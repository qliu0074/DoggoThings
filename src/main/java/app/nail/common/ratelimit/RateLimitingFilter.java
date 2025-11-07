package app.nail.common.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * English: Simple IP-based rate limiter using Bucket4j.
 * Limits each client to 60 requests per minute; tweak limits as needed.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Bandwidth IP_LIMIT = Bandwidth.classic(60, Refill.greedy(60, Duration.ofMinutes(1)));
    private static final Bandwidth USER_LIMIT = Bandwidth.classic(120, Refill.greedy(120, Duration.ofMinutes(1)));
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String userKey = resolveUserKey();
        if (userKey != null && !tryConsume(userKey, USER_LIMIT, response, "Too many requests for this user")) {
            return;
        }

        String ipKey = resolveIpKey(request);
        if (!tryConsume(ipKey, IP_LIMIT, response, "Too many requests from this IP")) {
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean tryConsume(String key, Bandwidth limit, HttpServletResponse response, String message) throws IOException {
        Bucket bucket = buckets.computeIfAbsent(key, k -> Bucket.builder().addLimit(limit).build());
        if (bucket.tryConsume(1)) {
            return true;
        }
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.getWriter().write(message);
        return false;
    }

    private String resolveIpKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int idx = forwarded.indexOf(',');
            String ip = idx > 0 ? forwarded.substring(0, idx).trim() : forwarded.trim();
            return "IP:" + ip;
        }
        return "IP:" + request.getRemoteAddr();
    }

    private String resolveUserKey() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal != null && !"anonymousUser".equals(principal)) {
                return "USER:" + authentication.getName();
            }
        }
        return null;
    }
}
