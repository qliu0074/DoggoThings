package app.nail.interfaces.client.controller;

import app.nail.application.service.ProductAppService;
import app.nail.interfaces.client.dto.ProductDtos.ProductDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client/products")
public class ProductController {
    private final ProductAppService svc;
    public ProductController(ProductAppService svc){ this.svc = svc; }

    /** English: Get one product detail. */
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id){
        return svc.getById(id)
                .map(p -> ResponseEntity.ok(new ProductDetail(p.getId(), p.getName(), p.getCategory(), p.getPriceCents())))
                .orElse(ResponseEntity.notFound().build());
    }
}
