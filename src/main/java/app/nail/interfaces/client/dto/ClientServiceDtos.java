package app.nail.interfaces.client.dto;

import app.nail.domain.enums.ProductStatus;
import java.time.OffsetDateTime;

/** English: Client-side service response models. */
public class ClientServiceDtos {

    /** English: Service basic view for listing. */
    public record ServiceResp(
            Long id,
            String category,
            Integer priceCents,
            Integer estimatedMinutes,
            String description,
            ProductStatus status,
            OffsetDateTime updatedAt
    ) {}

    /** English: Service image view. */
    public record ImageResp(
            Long id,
            String url,
            boolean cover,
            short sortOrder
    ) {}
}
