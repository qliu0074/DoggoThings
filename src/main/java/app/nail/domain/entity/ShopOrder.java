package app.nail.domain.entity;

import app.nail.common.model.SoftDeletable;
import app.nail.domain.enums.PaymentMethod;
import app.nail.domain.enums.ShopStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * English: Shop order header entity (soft-deletable).
 * Table: app.shop_orders
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "shop_orders", schema = "app", indexes = {
        @Index(name = "idx_shop_orders_status", columnList = "status"),
        @Index(name = "idx_shop_orders_user_created", columnList = "user_id, created_at DESC")
})
@SQLDelete(sql = "UPDATE app.shop_orders SET deleted_at = now() WHERE id = ?")
@Where(clause = "deleted_at IS NULL") // English: filter out soft-deleted rows by default
public class ShopOrder extends SoftDeletable { // English: provides deletedAt field

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** English: Who placed the order */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** English: Order status (Postgres enum shop_status) */
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false, columnDefinition = "shop_status")
    private ShopStatus status;

    /** English: Total amount in cents */
    @Column(name = "total_cents", nullable = false)
    private Integer totalCents;

    /** English: Shipping address */
    @Column(name = "address", columnDefinition = "text")
    private String address;

    /** English: Contact phone (shipping contact) */
    @Column(length = 30)
    private String phone;

    /** English: Payment method (Postgres enum payment_method) */
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "pay_method", columnDefinition = "payment_method")
    private PaymentMethod payMethod;

    /** English: Balance used in cents */
    @Column(name = "balance_cents_used", nullable = false)
    private Integer balanceCentsUsed;

    /** English: Tracking number */
    @Column(name = "tracking_no", length = 80)
    private String trackingNo;

    /** English: External payment reference (gateway transaction id). */
    @Column(name = "payment_ref", length = 120)
    private String paymentRef;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /** English: Optimistic lock version */
    @Version
    private Integer version;

    /**
     * English: Order items.
     * - Do NOT orphan-remove with soft delete, or it will hard-delete children.
     * - Cascade persist/merge only; delete is handled by item's own @SQLDelete.
     */
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    /** English: Helper to keep both sides consistent. */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    /** English: Helper to detach, not hard-delete. */
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
}
