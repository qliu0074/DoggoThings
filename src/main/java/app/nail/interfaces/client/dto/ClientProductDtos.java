package app.nail.interfaces.client.dto;

import app.nail.domain.enums.ProductStatus;
import java.time.OffsetDateTime;

/** English: Client-side product response models. */
public class ClientProductDtos {

    /** English: Product basic view for listing. */
    public record ProductResp(
            Long id,
            String name,
            String category,
            Integer priceCents,
            Integer stockDisplay,
            ProductStatus status,
            OffsetDateTime updatedAt
    ) {}

    /** English: Product image view. */
    public record ImageResp(
            Long id,
            String url,
            boolean cover,
            short sortOrder
    ) {}
}
