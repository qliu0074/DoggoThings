package app.nail.domain.entity;

import app.nail.domain.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品实体
 * 对应表：app.products
 * 说明：包含实际库存、待确认库存、展示库存(生成列)
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products", schema = "app")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 商品名 */
    @Column(nullable = false, length = 160)
    private String name;

    /** 分类（例：穿戴甲/玩具等） */
    @Column(nullable = false, length = 60)
    private String category;

    /** 价格（单位：分） */
    @Column(name = "price_cents", nullable = false)
    private Integer priceCents;

    /** 描述 */
    @Lob
    private String description;

    /** 实际库存 */
    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual;

    /** 待确认库存（下单未确认冻结） */
    @Column(name = "stock_pending", nullable = false)
    private Integer stockPending;

    /** 展示库存 = actual - pending（生成列，只读） */
    @Column(name = "stock_display", insertable = false, updatable = false)
    private Integer stockDisplay;

    /** 上下架状态 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "product_status")
    private ProductStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /** 乐观锁版本 */
    @Version
    private Integer version;

    /** 图片一对多 */
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();
}
