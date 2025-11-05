package app.nail.interfaces.client.dto;

/** English: DTOs for product APIs. */
public class ProductDtos {
    /** English: Product detail response DTO. */
    public record ProductDetail(Long id, String name, String category, Integer priceCents) {}
}
