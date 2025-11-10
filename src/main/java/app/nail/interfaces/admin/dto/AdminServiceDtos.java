package app.nail.interfaces.admin.dto;

import app.nail.domain.enums.ProductStatus;

/** English: Admin-side service DTOs. */
public class AdminServiceDtos {

    /** English: Create service request. */
    public record CreateServiceReq(String category, Integer priceCents, String description, Integer estimatedMinutes) {}

    /** English: Update service request. */
    public record UpdateServiceReq(String category, Integer priceCents, String description,
                                   ProductStatus status, Integer estimatedMinutes) {}

    /** English: Add service image request. */
    public record AddImageReq(String url, boolean cover, short sortOrder) {}
}
