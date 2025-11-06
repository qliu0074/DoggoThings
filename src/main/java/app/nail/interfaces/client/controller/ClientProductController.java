package app.nail.interfaces.client.controller;

import app.nail.domain.entity.Product;
import app.nail.domain.enums.ProductStatus;
import app.nail.domain.repository.ProductRepository;
import app.nail.interfaces.client.dto.ClientProductDtos.ProductResp;
import app.nail.interfaces.common.dto.PageResp;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

/** English: Client product browsing controller. */
@RestController
@RequestMapping("/api/client/products")
@RequiredArgsConstructor
public class ClientProductController {

    private final ProductRepository productRepo;

    /** English: Paged product list for clients, only ON. */
    @GetMapping
    public PageResp<ProductResp> page(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(required = false) String category) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<Product> p;
        if (category != null && !category.isBlank()) {
            p = productRepo.findByCategoryAndStatus(category, ProductStatus.ON, pageable);
        } else {
            p = productRepo.findByStatus(ProductStatus.ON, pageable);
        }
        return new PageResp<>(
                p.map(this::toResp).getContent(),
                p.getTotalElements(),
                p.getTotalPages(),
                p.getNumber(),
                p.getSize()
        );
    }

    /** English: Product detail. */
    @GetMapping("/{id}")
    public ProductResp get(@PathVariable Long id) {
        Product e = productRepo.findById(id).orElseThrow(() -> new RuntimeException("product not found"));
        return toResp(e);
    }

    /** English: Entity -> DTO mapper. */
    private ProductResp toResp(Product e) {
        return new ProductResp(
                e.getId(), e.getName(), e.getCategory(), e.getPriceCents(),
                e.getStockDisplay(), e.getStatus(), e.getUpdatedAt()
        );
    }
}
