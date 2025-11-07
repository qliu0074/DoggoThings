package app.nail.interfaces.client.controller;

import app.nail.application.service.ProductService;
import app.nail.domain.entity.Product;
import app.nail.domain.enums.ProductStatus;
import app.nail.domain.repository.ProductRepository;
import app.nail.interfaces.client.dto.ClientProductDtos.ProductResp;
import app.nail.interfaces.common.PageRequestParams;
import app.nail.interfaces.common.dto.PageResp;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** English: Client product browsing controller. */
@RestController
@RequestMapping("/api/v1/client/products")
@RequiredArgsConstructor
@Validated
public class ClientProductController {

    private final ProductRepository productRepo;
    private final ProductService productService;

    /** English: Paged product list for clients, only ON. */
    @GetMapping
    public PageResp<ProductResp> page(@RequestParam(required = false) Integer page,
                                      @RequestParam(required = false) Integer size,
                                      @RequestParam(required = false) String category) {
        PageRequestParams params = PageRequestParams.of(page, size);
        Pageable pageable = PageRequest.of(params.page(), params.size(), Sort.by("updatedAt").descending());
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
    public ProductResp get(@PathVariable @Positive Long id) {
        Product e = productService.getProduct(id);
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
