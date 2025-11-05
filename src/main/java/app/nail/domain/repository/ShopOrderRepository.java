package app.nail.domain.repository;
import app.nail.domain.entity.ShopOrder;
import org.springframework.data.jpa.repository.JpaRepository;
/** English: Basic CRUD for shop orders. */
public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {}