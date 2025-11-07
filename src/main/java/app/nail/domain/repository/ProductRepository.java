package app.nail.domain.repository;

import app.nail.domain.entity.Product;
import app.nail.domain.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * 商品仓储
 * 作用：管理商品与状态，提供按分类与状态检索
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** 按分类+状态分页检索，用于前台商品列表 */
    Page<Product> findByCategoryAndStatus(String category, ProductStatus status, Pageable pageable);

    /** 仅上架商品分页检索 */
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    /**
     * 安全扣减实际库存（确认订单时）
     * 约束：只在剩余库存满足时成功，避免负数
     */
    @Modifying
    @Query(value = """
            UPDATE app.products
               SET stock_actual = stock_actual - :delta,
                   updated_at = NOW()
             WHERE id = :productId
               AND stock_actual >= :delta
            """, nativeQuery = true)
    int tryDeductActualStock(long productId, int delta);

    /**
     * 增加/减少 pending 库存（下单未确认时冻结或释放）
     * 参数可以是正数（冻结）或负数（释放）
     * 约束：结果不得为负
     */
    @Modifying
    @Query(value = """
            UPDATE app.products
               SET stock_pending = stock_pending + :delta,
                   updated_at = NOW()
             WHERE id = :productId
               AND stock_pending + :delta >= 0
            """, nativeQuery = true)
    int adjustPendingStock(long productId, int delta);

    @Modifying
    @Query(value = """
            UPDATE app.products
               SET stock_actual = stock_actual + :delta,
                   updated_at = NOW()
             WHERE id = :productId
               AND stock_actual + :delta >= 0
            """, nativeQuery = true)
    int increaseActualStock(long productId, int delta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :productId")
    java.util.Optional<Product> lockById(long productId);
}
