package app.nail.interfaces.client.dto;

import app.nail.domain.enums.ShopStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.OffsetDateTime;
import java.util.List;

/** English: Client-side order request/response DTOs. */
public class ClientOrderDtos {

    /** English: Order item request from client. */
    public record OrderItemReq(
            @NotNull Long productId,
            @NotNull @Positive Integer qty
    ) {}

    /** English: Create order request. */
    public record CreateOrderReq(
            @Valid @NotEmpty List<OrderItemReq> items,
            boolean freezeBalance,
            @NotBlank String address,
            @NotBlank String phone
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
