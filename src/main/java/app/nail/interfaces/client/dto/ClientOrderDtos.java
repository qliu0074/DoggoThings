package app.nail.interfaces.client.dto;

import app.nail.domain.enums.ShopStatus;
import java.time.OffsetDateTime;
import java.util.List;

/** English: Client-side order request/response DTOs. */
public class ClientOrderDtos {

    /** English: Order item request from client. */
    public record OrderItemReq(Long productId, Integer qty) {}

    /** English: Create order request. */
    public record CreateOrderReq(
            Long userId,
            List<OrderItemReq> items,
            boolean freezeBalance,
            String address,
            String phone
    ) {}

    /** English: Order item view for client. */
    public record OrderItemResp(
            Long id,
            Long productId,
            String productName,
            Integer qty,
            Integer unitCents,
            Integer lineCents,
            ShopStatus status
    ) {}

    /** English: Order header view for client. */
    public record OrderResp(
            Long id,
            ShopStatus status,
            Integer totalCents,
            String address,
            String phone,
            String trackingNo,
            OffsetDateTime createdAt,
            List<OrderItemResp> items
    ) {}
}
