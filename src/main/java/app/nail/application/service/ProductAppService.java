package app.nail.application.service;

import app.nail.domain.entity.Product;
import app.nail.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

/** English: Use case layer for product queries and commands. */
@Service
public class ProductAppService {
    private final ProductRepository repo;
    public ProductAppService(ProductRepository repo){ this.repo = repo; }

    /** English: Query product by id. Expand with cache later. */
    public Optional<Product> getById(Long id) { return repo.findById(id); }
}
