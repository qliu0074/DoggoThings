package app.nail.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * 预约明细实体
 * 对应表：app.appointment_items
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "appointment_items", schema = "app", indexes = {
        @Index(name = "idx_appt_items_appt", columnList = "appointment_id"),
        @Index(name = "idx_appt_items_srv", columnList = "service_id")
})
public class AppointmentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属预约单 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    /** 关联服务 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceItem service;

    /** 数量 */
    @Column(nullable = false)
    private Integer qty;

    /** 单价（分） */
    @Column(name = "unit_cents", nullable = false)
    private Integer unitCents;

    /** 小计（分，生成列，只读） */
    @Column(name = "line_cents", insertable = false, updatable = false)
    private Integer lineCents;

    /** 创建时间（由数据库默认 now() 自动赋值，保留字段便于读取） */
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    /** 乐观锁版本 */
    @Version
    private Integer version;
}
