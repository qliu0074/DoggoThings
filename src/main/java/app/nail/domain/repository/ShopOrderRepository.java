package app.nail.domain.repository;

import app.nail.domain.entity.ShopOrder;
import app.nail.domain.enums.ShopStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 商城订单头仓储
 * 作用：按用户/状态分页，按创建时间排序
 */
public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {

    Page<ShopOrder> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<ShopOrder> findByStatus(ShopStatus status, Pageable pageable);
}
