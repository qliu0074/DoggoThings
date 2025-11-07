package app.nail.integration.support;

import app.nail.application.service.NotificationService;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Test-only NotificationService that forwards events to WireMock for verification.
 */
class WireMockNotificationClient implements NotificationService {

    private final RestTemplate restTemplate;

    WireMockNotificationClient(RestTemplateBuilder builder, String baseUrl) {
        this.restTemplate = builder
                .rootUri(baseUrl)
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(2))
                .build();
    }

    @Override
    public void notifyOrderEvent(Long userId, String event, Map<String, Object> payload) {
        post("/mock-gateway/notifications/orders", "ORDER", userId, event, payload);
    }

    @Override
    public void notifyAppointmentEvent(Long userId, String event, Map<String, Object> payload) {
        post("/mock-gateway/notifications/appointments", "APPOINTMENT", userId, event, payload);
    }

    private void post(String path, String channel, Long userId, String event, Map<String, Object> payload) {
        restTemplate.postForEntity(path,
                new NotificationRequest(channel, userId, event, sanitize(payload)),
                Void.class);
    }

    private Map<String, Object> sanitize(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return Collections.emptyMap();
        }
        return new LinkedHashMap<>(payload);
    }

    private record NotificationRequest(String channel, Long userId, String event, Map<String, Object> payload) {}
}
