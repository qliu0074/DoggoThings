package app.nail.domain.entity;

import app.nail.domain.enums.ApptStatus;
import app.nail.domain.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 预约单实体
 * 对应表：app.appointments
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "appointments", schema = "app", indexes = {
        @Index(name = "ux_appt_user_slot", columnList = "user_id, appointment_at", unique = true)
})
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 预约用户 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 预约时间 */
    @Column(name = "appointment_at", nullable = false)
    private OffsetDateTime appointmentAt;

    /** 预约状态 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "appt_status")
    private ApptStatus status;

    /** 总额（分） */
    @Column(name = "total_cents", nullable = false)
    private Integer totalCents;

    /** 支付方式（可空） */
    @Enumerated(EnumType.STRING)
    @Column(name = "pay_method", columnDefinition = "payment_method")
    private PaymentMethod payMethod;

    /** 使用余额（分） */
    @Column(name = "balance_cents_used", nullable = false)
    private Integer balanceCentsUsed;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /** 乐观锁版本 */
    @Version
    private Integer version;

    /** 明细列表 */
    @OneToMany(mappedBy = "appointment", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AppointmentItem> items = new ArrayList<>();
}
