package app.nail.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * English: Audit trail entity mapping app.audit_logs table.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "audit_logs", schema = "app", indexes = {
        @Index(name = "idx_audit_entity_time", columnList = "entity_type, entity_id, event_time DESC")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "event_time", nullable = false, updatable = false)
    private OffsetDateTime eventTime;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "actor_type")
    private String actorType;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "action", nullable = false)
    private String action;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "changes")
    private Map<String, Object> changes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context")
    private Map<String, Object> context;
}
