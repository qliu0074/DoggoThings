package app.nail.domain.entity;

import app.nail.common.model.SoftDeletable;
import app.nail.domain.enums.ConsumeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.OffsetDateTime;

/**
 * 余额变动纪录实体
 * 对应表：app.consumptions
 * 说明：记录充值与消费，并可关联来源（订单/预约等）
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "consumptions", schema = "app", indexes = {
        @Index(name = "idx_consumptions_user_time", columnList = "user_id, created_at")
})
@SQLDelete(sql = "UPDATE app.consumptions SET deleted_at = now() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Consumption extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 类型：充值或消费 */
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "kind", nullable = false, columnDefinition = "consume_type")
    private ConsumeType kind;

    /** 金额（分） */
    @Column(name = "amount_cents", nullable = false)
    private Integer amountCents;

    /** 参考来源类型与 ID（可空） */
    @Column(name = "ref_kind", columnDefinition = "text")
    private String refKind;

    @Column(name = "ref_id")
    private Long refId;

    /** 创建时间（数据库 now() 自动赋值） */
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
