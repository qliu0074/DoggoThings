package app.nail.domain.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.OffsetDateTime;

/** English: Appointment detail line. */
@Getter @Setter
@Entity @Table(name="appointment_items", schema="app")
public class AppointmentItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="appointment_id", nullable=false)
    private Long appointmentId;

    @Column(name="service_id", nullable=false)
    private Long serviceId;

    @Column(nullable=false)
    private Integer qty;

    @Column(name="unit_cents", nullable=false)
    private Integer unitCents;

    @Column(name="created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Version
    private Integer version;
}
