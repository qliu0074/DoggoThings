package app.nail.domain.entity;

import app.nail.domain.enums.ShopStatus;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.OffsetDateTime;

/** English: Order detail line. */
@Getter @Setter
@Entity @Table(name="order_items", schema="app")
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="order_id", nullable=false)
    private Long orderId;

    @Column(name="product_id", nullable=false)
    private Long productId;

    @Column(nullable=false)
    private Integer qty;

    @Column(name="unit_cents", nullable=false)
    private Integer unitCents;

    @Enumerated(EnumType.STRING)
    private ShopStatus status = ShopStatus.PENDING_CONFIRM;

    @Column(name="created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Version
    private Integer version;
}
