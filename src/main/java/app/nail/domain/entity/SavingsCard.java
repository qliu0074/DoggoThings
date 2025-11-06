package app.nail.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * 用户余额卡实体
 * 对应表：app.savings_cards
 * 说明：与用户一对一，存放可用余额
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "savings_cards", schema = "app")
public class SavingsCard {

    /** 主键同 users.id，一对一共享键 */
    @Id
    @Column(name = "user_id")
    private Long userId;

    /** 余额（分） */
    @Column(name = "balance_cents", nullable = false)
    private Integer balanceCents;

    /** 更新时间（数据库 now() + @UpdateTimestamp 保障） */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /** 乐观锁版本 */
    @Version
    private Integer version;

    /** 便捷访问：用户（可选映射） */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
