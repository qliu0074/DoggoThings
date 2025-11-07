package app.nail.infra.health;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Adds extra health details for core dependencies (database, tracing).
 */
@Component
@RequiredArgsConstructor
public class DependenciesHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    @Value("${management.zipkin.tracing.endpoint:}")
    private String zipkinEndpoint;

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            details.put("database", "UP");
        } catch (Exception ex) {
            details.put("database", "DOWN");
            details.put("databaseError", ex.getMessage());
            return Health.down(ex).withDetails(details).build();
        }
        details.put("zipkinEndpoint", zipkinEndpoint == null || zipkinEndpoint.isBlank()
                ? "not-configured"
                : zipkinEndpoint);

        return Health.up().withDetails(details).build();
    }
}
