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
 * 服务项目实体
 * 对应表：app.services
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "services", schema = "app")
public class ServiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 服务分类 */
    @Column(nullable = false, length = 60)
    private String category;

    /** 服务价格（分） */
    @Column(name = "price_cents", nullable = false)
    private Integer priceCents;

    /** 描述 */
    @Lob
    private String description;

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
    @OneToMany(mappedBy = "service", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ServiceImage> images = new ArrayList<>();
}
