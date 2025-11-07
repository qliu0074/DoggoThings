package app.nail.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.OffsetDateTime;

/**
 * English: Base class for soft-deletable entities.
 * Field 'deleted_at' is mapped and reused by children.
 */
@Getter
@MappedSuperclass
public abstract class SoftDeletable {

    /** English: Soft delete timestamp; null means active. */
    @Column(name = "deleted_at")
    protected OffsetDateTime deletedAt;

    /** English: Helper to check logical deletion state. */
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
