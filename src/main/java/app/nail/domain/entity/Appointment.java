package app.nail.domain.entity;

import app.nail.domain.enums.ApptStatus;
import app.nail.domain.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.OffsetDateTime;

/** English: Appointment header row. */
@Getter @Setter
@Entity @Table(name="appointments", schema="app")
public class Appointment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="appointment_at", nullable=false)
    private OffsetDateTime appointmentAt;

    @Enumerated(EnumType.STRING)
    private ApptStatus status = ApptStatus.UNCONFIRMED;

    @Column(name="total_cents", nullable=false)
    private Integer totalCents = 0;

    @Enumerated(EnumType.STRING)
    @Column(name="pay_method")
    private PaymentMethod payMethod;

    @Column(name="balance_cents_used", nullable=false)
    private Integer balanceCentsUsed = 0;

    @Column(name="created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    @Version
    private Integer version;
}
