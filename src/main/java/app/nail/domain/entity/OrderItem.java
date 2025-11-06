package app.nail.domain.entity;

import app.nail.domain.enums.ShopStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * 商城订单明细实体
 * 对应表：app.order_items
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "order_items", schema = "app", indexes = {
        @Index(name = "idx_items_order", columnList = "order_id"),
        @Index(name = "idx_items_product", columnList = "product_id"),
        @Index(name = "idx_items_status", columnList = "status")
})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属订单头 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private ShopOrder order;

    /** 关联商品 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** 数量 */
    @Column(nullable = false)
    private Integer qty;

    /** 单价（分） */
    @Column(name = "unit_cents", nullable = false)
    private Integer unitCents;

    /** 小计（分，生成列，只读） */
    @Column(name = "line_cents", insertable = false, updatable = false)
    private Integer lineCents;

    /** 明细状态（与头同枚举） */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "shop_status")
    private ShopStatus status;

    /** 创建时间（数据库 now()） */
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    /** 乐观锁版本 */
    @Version
    private Integer version;
}
