package app.nail.domain.entity;

import app.nail.domain.enums.PaymentMethod;
import app.nail.domain.enums.ShopStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 商城订单头实体
 * 对应表：app.shop_orders
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shop_orders", schema = "app", indexes = {
        @Index(name = "idx_shop_orders_status", columnList = "status"),
        @Index(name = "idx_shop_orders_user_created", columnList = "user_id, created_at DESC")
})
public class ShopOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 下单用户 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 订单状态 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "shop_status")
    private ShopStatus status;

    /** 订单总额（分） */
    @Column(name = "total_cents", nullable = false)
    private Integer totalCents;

    /** 地址与联系电话 */
    @Lob
    private String address;

    @Column(length = 30)
    private String phone;

    /** 支付方式 */
    @Enumerated(EnumType.STRING)
    @Column(name = "pay_method", columnDefinition = "payment_method")
    private PaymentMethod payMethod;

    /** 使用余额（分） */
    @Column(name = "balance_cents_used", nullable = false)
    private Integer balanceCentsUsed;

    /** 物流单号 */
    @Column(name = "tracking_no", length = 80)
    private String trackingNo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /** 乐观锁版本 */
    @Version
    private Integer version;

    /** 订单项 */
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}
