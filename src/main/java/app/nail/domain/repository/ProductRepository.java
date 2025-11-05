// ProductRepository.java
package app.nail.domain.repository;
import app.nail.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
/** English: Basic CRUD for products. */
public interface ProductRepository extends JpaRepository<Product, Long> {}