package app.nail.domain.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.OffsetDateTime;

/** English: Product images with optional cover and sort order. */
@Getter @Setter
@Entity @Table(name="product_images", schema="app")
public class ProductImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="product_id", nullable=false)
    private Long productId;

    @Column(name="image_url", nullable=false, columnDefinition="text")
    private String imageUrl;

    @Column(name="is_cover", nullable=false)
    private Boolean isCover = false;

    @Column(name="sort_order", nullable=false)
    private Short sortOrder = 0;

    @Column(name="created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
