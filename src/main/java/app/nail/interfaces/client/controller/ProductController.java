package app.nail.interfaces.client.controller;

import app.nail.application.service.ProductAppService;
import app.nail.interfaces.client.dto.ProductDtos.ProductDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client/products")
public class ProductController {
    private final ProductAppService svc;
    public ProductController(ProductAppService svc){ this.svc = svc; }

    @Operation(summary = "分页查询产品列表")
    @GetMapping
    public Page<ProductDetail> list(Pageable pageable) {
        return svc.list(pageable).map(p -> new ProductDetail(p.getId(), p.getName(), p.getCategory(), p.getPriceCents()));
    }

    /** English: Get one product detail. */
    @Operation(summary = "获取产品详情")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功"),
        @ApiResponse(responseCode = "404", description = "产品不存在")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id){
        return svc.getById(id)
                .map(p -> ResponseEntity.ok(new ProductDetail(p.getId(), p.getName(), p.getCategory(), p.getPriceCents())))
                .orElse(ResponseEntity.notFound().build());
    }
}
