package app.nail.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * 服务图片实体
 * 对应表：app.service_images
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "service_images", schema = "app")
public class ServiceImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属服务 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceItem service;

    /** 图片 URL */
    @Column(name = "image_url", nullable = false, columnDefinition = "text")
    private String imageUrl;

    /** 是否封面 */
    @Column(name = "is_cover", nullable = false)
    private Boolean cover;

    /** 排序 */
    @Column(name = "sort_order", nullable = false)
    private Short sortOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
