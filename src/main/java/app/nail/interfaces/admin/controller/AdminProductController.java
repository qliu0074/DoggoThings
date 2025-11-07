package app.nail.interfaces.admin.controller;

import app.nail.application.service.ProductService;
import app.nail.domain.entity.Product;
import app.nail.domain.repository.ProductRepository;
import app.nail.interfaces.admin.dto.AdminProductDtos.*;
import app.nail.interfaces.common.PageRequestParams;
import app.nail.interfaces.common.dto.PageResp;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** English: Admin product management. */
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductService productService;
    private final ProductRepository productRepo;

    @PostMapping
    public Long create(@RequestBody CreateProductReq req) {
        return productService.createProduct(req.name(), req.category(), req.priceCents());
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody UpdateProductReq req) {
        productService.updateProduct(id, req.name(), req.category(), req.priceCents(), req.status());
    }

    @GetMapping
    public PageResp<ProductDetailResp> page(@RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer size) {
        PageRequestParams params = PageRequestParams.of(page, size);
        Pageable pageable = PageRequest.of(params.page(), params.size(), Sort.by("updatedAt").descending());
        Page<Product> p = productRepo.findAll(pageable);
        return new PageResp<>(
                p.map(this::toResp).getContent(),
                p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize()
        );
    }

    @PostMapping("/{id}/images")
    public Long addImage(@PathVariable long id, @RequestBody AddImageReq req) {
        return productService.addImage(id, req.url(), req.cover(), req.sortOrder());
    }

    private ProductDetailResp toResp(Product e) {
        return new ProductDetailResp(
                e.getId(), e.getName(), e.getCategory(), e.getPriceCents(),
                e.getStockActual(), e.getStockPending(), e.getStockDisplay(),
                e.getStatus(), e.getUpdatedAt()
        );
    }
}
