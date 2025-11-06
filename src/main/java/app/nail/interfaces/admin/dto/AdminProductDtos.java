package app.nail.interfaces.admin.dto;

import app.nail.domain.enums.ProductStatus;
import java.time.OffsetDateTime;

/** English: Admin-side product DTOs. */
public class AdminProductDtos {

    /** English: Create product request. */
    public record CreateProductReq(String name, String category, Integer priceCents) {}

    /** English: Update product request. */
    public record UpdateProductReq(String name, String category, Integer priceCents, ProductStatus status) {}

    /** English: Add product image request. */
    public record AddImageReq(String url, boolean cover, short sortOrder) {}

    /** English: Product management detail view. */
    public record ProductDetailResp(
            Long id,
            String name,
            String category,
            Integer priceCents,
            Integer stockActual,
            Integer stockPending,
            Integer stockDisplay,
            ProductStatus status,
            OffsetDateTime updatedAt
    ) {}
}
