package app.nail.domain.repository;

import app.nail.domain.entity.OrderItem;
import app.nail.domain.enums.ShopStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 商城订单明细仓储
 * 作用：按订单头或商品查询明细
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findByProductId(Long productId);

    List<OrderItem> findByStatus(ShopStatus status);
}
