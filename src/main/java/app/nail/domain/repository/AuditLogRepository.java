package app.nail.domain.repository;

import app.nail.domain.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * English: Repository for audit log persistence.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
