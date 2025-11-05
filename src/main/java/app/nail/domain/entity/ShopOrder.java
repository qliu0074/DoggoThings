package app.nail.domain.entity;

import app.nail.domain.enums.PaymentMethod;
import app.nail.domain.enums.ShopStatus;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.OffsetDateTime;

/** English: Order header. */
@Getter @Setter
@Entity @Table(name="shop_orders", schema="app")
public class ShopOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    private ShopStatus status = ShopStatus.PENDING_CONFIRM;

    @Column(name="total_cents", nullable=false)
    private Integer totalCents = 0;

    @Column(columnDefinition="text")
    private String address;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name="pay_method")
    private PaymentMethod payMethod;

    @Column(name="balance_cents_used", nullable=false)
    private Integer balanceCentsUsed = 0;

    @Column(name="tracking_no", length=80)
    private String trackingNo;

    @Column(name="created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    @Version
    private Integer version;
}
