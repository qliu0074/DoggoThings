package app.nail.domain.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.OffsetDateTime;

/** English: Immutable savings ledger row. */
@Getter @Setter
@Entity @Table(name="consumptions", schema="app")
public class Consumption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="kind", nullable=false)
    private String kind; // English: keep simple here ('TOP_UP' | 'SPEND')

    @Column(name="amount_cents", nullable=false)
    private Integer amountCents;

    @Column(name="ref_kind")
    private String refKind;

    @Column(name="ref_id")
    private Long refId;

    @Column(name="created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
