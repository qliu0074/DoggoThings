package app.nail.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * English: Database-managed audit columns.
 * - Database supplies defaults via DEFAULT now().
 * - Triggers update 'updated_at' on every UPDATE.
 * - JPA marks both columns as non-insertable and non-updatable so Hibernate never sends values.
 */
@MappedSuperclass
public abstract class BaseAuditable {

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private java.time.OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private java.time.OffsetDateTime updatedAt;

    // English: getters only; setters are unnecessary because DB manages values
    public java.time.OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public java.time.OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
