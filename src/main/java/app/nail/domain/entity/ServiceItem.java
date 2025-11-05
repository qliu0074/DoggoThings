package app.nail.domain.entity;

import app.nail.domain.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.OffsetDateTime;

/** English: Service catalog row (e.g., manicure). */
@Getter @Setter
@Entity @Table(name="services", schema="app")
public class ServiceItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;

    @Column(name="price_cents", nullable=false)
    private Integer priceCents;

    @Column(columnDefinition="text")
    private String description;

    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.ON;

    @Column(name="created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    @Version
    private Integer version;
}
