package app.nail.application.service;

import java.util.Map;

/**
 * Simple notification abstraction for email/SMS/push.
 */
public interface NotificationService {

    void notifyOrderEvent(Long userId, String event, Map<String, Object> payload);

    void notifyAppointmentEvent(Long userId, String event, Map<String, Object> payload);
}
