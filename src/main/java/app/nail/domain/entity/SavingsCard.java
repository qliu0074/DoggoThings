package app.nail.domain.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.OffsetDateTime;

/** English: Per-user balance. */
@Getter @Setter
@Entity @Table(name="savings_cards", schema="app")
public class SavingsCard {
    @Id
    @Column(name="user_id")
    private Long userId;

    @Column(name="balance_cents", nullable=false)
    private Integer balanceCents = 0;

    @Column(name="updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    @Version
    private Integer version;
}
