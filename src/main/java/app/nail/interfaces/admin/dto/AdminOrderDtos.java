package app.nail.interfaces.admin.dto;

import app.nail.domain.enums.ShopStatus;
import java.time.OffsetDateTime;
import java.util.List;

/** English: Admin-side order DTOs. */
public class AdminOrderDtos {

    /** English: Ship request. */
    public record ShipReq(String trackingNo) {}

    /** English: Refund request payload. */
    public record RefundReq(
            @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Positive Integer amountCents,
            String reason
    ) {}

    /** English: Admin order item view. */
    public record ItemResp(
            Long id,
            Long productId,
            String productName,
            Integer qty,
            Integer unitCents,
            Integer lineCents,
            ShopStatus status
    ) {}

    /** English: Admin order header view. */
    public record OrderResp(
            Long id,
            Long userId,
            ShopStatus status,
            Integer totalCents,
            String address,
            String phone,
            String trackingNo,
            OffsetDateTime createdAt,
            List<ItemResp> items
    ) {}
}
