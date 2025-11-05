package app.nail.application.service;

import app.nail.domain.entity.Product;
import app.nail.domain.repository.ProductRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

/** English: Use case layer for product queries and commands. */
@Service
public class ProductAppService {
    private final ProductRepository repo;
    public ProductAppService(ProductRepository repo){ this.repo = repo; }

    /** English: Query product by id. Caches result to cut DB hits on hot paths. */
    @Cacheable(value = "products", key = "#id")
    public Optional<Product> getById(Long id) { return repo.findById(id); }

    /** English: Paged query for product listing. */
    public Page<Product> list(Pageable pageable) { return repo.findAll(pageable); }
}
