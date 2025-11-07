package app.nail.infra.notification;

import app.nail.application.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Logging-based notification stub. Replace with email/SMS providers later.
 */
@Component
@Slf4j
public class LoggingNotificationService implements NotificationService {

    @Override
    public void notifyOrderEvent(Long userId, String event, Map<String, Object> payload) {
        log.info("Notify order event '{}' for user {} -> {}", event, userId, payload);
    }

    @Override
    public void notifyAppointmentEvent(Long userId, String event, Map<String, Object> payload) {
        log.info("Notify appointment event '{}' for user {} -> {}", event, userId, payload);
    }
}
