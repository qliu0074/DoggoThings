package app.nail.application.service;

import app.nail.common.security.PrincipalUser;
import app.nail.domain.entity.AuditLog;
import app.nail.domain.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** English: Central audit logging service. */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditRepo;

    @Transactional
    public void log(Long actorId, String actorType,
                    String entityType, Long entityId,
                    String action, Map<String, Object> changes, Map<String, Object> context) {

        AuditLog log = AuditLog.builder()
                .actorId(actorId)
                .actorType(actorType)
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .changes(copy(changes))
                .context(copy(context))
                .build();

        auditRepo.save(log);
    }

    /** English: Log and automatically capture actor from security context if available. */
    @Transactional
    public void log(String entityType, Long entityId,
                    String action, Map<String, Object> changes, Map<String, Object> context) {
        Actor actor = resolveActor();
        log(actor == null ? null : actor.id(), actor == null ? null : actor.type(),
                entityType, entityId, action, changes, context);
    }

    private Actor resolveActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        if ("anonymousUser".equals(authentication.getName())) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof PrincipalUser principalUser) {
            return new Actor(principalUser.id(), principalUser.role());
        }
        return new Actor(null, Objects.toString(authentication.getName(), null));
    }

    private Map<String, Object> copy(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        return new LinkedHashMap<>(source);
    }

    private record Actor(Long id, String type) {}
}
