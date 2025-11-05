package app.nail.domain.entity;

import app.nail.domain.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.OffsetDateTime;

/** English: Catalog product with dual stock counters and generated display stock. */
@Getter @Setter
@Entity @Table(name="products", schema="app")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String category;

    @Column(name="price_cents", nullable=false)
    private Integer priceCents;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name="stock_actual", nullable=false)
    private Integer stockActual;

    @Column(name="stock_pending", nullable=false)
    private Integer stockPending;

    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.ON;

    @Column(name="created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    @Version
    private Integer version;
}
