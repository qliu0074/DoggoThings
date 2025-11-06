package app.nail.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * 用户待结算余额实体
 * 对应表：app.savings_pending
 * 说明：下单/预约冻结金额，确认后再结转到实际余额
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name = "savings_pending", schema = "app")
public class SavingsPending {

    /** 主键同 users.id，一对一共享键 */
    @Id
    @Column(name = "user_id")
    private Long userId;

    /** 待结算金额（分） */
    @Column(name = "pending_cents", nullable = false)
    private Integer pendingCents;

    /** 更新时间 */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /** 便捷访问：用户（可选映射） */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
