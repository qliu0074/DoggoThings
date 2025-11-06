package app.nail.application.service;

import app.nail.domain.entity.OrderItem;
import app.nail.domain.entity.Product;
import app.nail.domain.entity.ShopOrder;
import app.nail.domain.entity.User;
import app.nail.domain.enums.PaymentMethod;
import app.nail.domain.enums.ShopStatus;
import app.nail.domain.repository.OrderItemRepository;
import app.nail.domain.repository.ProductRepository;
import app.nail.domain.repository.ShopOrderRepository;
import app.nail.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商城订单服务
 * 职责：
 * 1. 创建订单（冻结库存与余额）
 * 2. 确认订单（扣实际库存、扣余额、改状态）
 * 3. 取消订单（释放库存与余额）
 * 4. 发货与完成
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final ShopOrderRepository orderRepo;
    private final OrderItemRepository itemRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final ProductService productService;
    private final BalanceService balanceService;

    /** DTO 占位：请按你的接口层定义实际 DTO */
    public record OrderItemDTO(Long productId, Integer qty) {}

    /** 创建订单：冻结库存与余额（可选） */
    @Transactional
    public Long createOrder(Long userId, List<OrderItemDTO> items, boolean freezeBalance, String address, String phone) {
        userRepo.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));

        int total = 0;
        // 计算总价并冻结库存
        for (OrderItemDTO dto : items) {
            Product p = productRepo.findById(dto.productId()).orElseThrow(() -> new RuntimeException("商品不存在"));
            total += p.getPriceCents() * dto.qty();
            productService.freezeStock(p.getId(), dto.qty()); // pending += qty
        }

        if (freezeBalance) {
            balanceService.freeze(userId, total); // pending += total
        }

        ShopOrder order = ShopOrder.builder()
                .user(User.builder().id(userId).build())
                .status(ShopStatus.PENDING_CONFIRM)
                .totalCents(total)
                .address(address)
                .phone(phone)
                .payMethod(PaymentMethod.BALANCE)
                .balanceCentsUsed(freezeBalance ? total : 0)
                .build();
        order = orderRepo.save(order);

        for (OrderItemDTO dto : items) {
            Product p = productRepo.findById(dto.productId()).orElseThrow(() -> new RuntimeException("商品不存在"));
            itemRepo.save(OrderItem.builder()
                    .order(order)
                    .product(p)
                    .qty(dto.qty())
                    .unitCents(p.getPriceCents())
                    .status(ShopStatus.PENDING_CONFIRM)
                    .build());
        }
        return order.getId();
    }

    /** 确认订单：扣实际库存、释放冻结库存、扣余额、释放冻结余额、状态改为 AWAITING */
    @Transactional
    public void confirmOrder(Long orderId) {
        ShopOrder order = orderRepo.findById(orderId).orElseThrow(() -> new RuntimeException("订单不存在"));
        if (order.getStatus() != ShopStatus.PENDING_CONFIRM) throw new RuntimeException("订单状态不允许确认");

        List<OrderItem> items = itemRepo.findByOrderId(orderId);
        // 先将冻结库存转为实际扣减，并把 pending 库存释放
        for (OrderItem it : items) {
            productService.adjustFrozen(it.getProduct().getId(), -it.getQty()); // pending -= qty
            productService.confirmDeduct(it.getProduct().getId(), it.getQty()); // actual -= qty
            it.setStatus(ShopStatus.AWAITING);
            itemRepo.save(it);
        }

        if (order.getBalanceCentsUsed() > 0) {
            balanceService.adjustPending(order.getUser().getId(), -order.getBalanceCentsUsed()); // pending -= total
            balanceService.trySpend(order.getUser().getId(), order.getBalanceCentsUsed(), "ORDER", order.getId()); // 实扣
        }

        order.setStatus(ShopStatus.AWAITING);
        orderRepo.save(order);
    }

    /** 取消订单：释放冻结库存与余额，状态改为 CANCELLED */
    @Transactional
    public void cancelOrder(Long orderId) {
        ShopOrder order = orderRepo.findById(orderId).orElseThrow(() -> new RuntimeException("订单不存在"));
        if (order.getStatus() == ShopStatus.CANCELLED) return;

        List<OrderItem> items = itemRepo.findByOrderId(orderId);
        for (OrderItem it : items) {
            productService.adjustFrozen(it.getProduct().getId(), -it.getQty()); // 释放 pending
            it.setStatus(ShopStatus.CANCELLED);
            itemRepo.save(it);
        }

        if (order.getBalanceCentsUsed() > 0) {
            balanceService.adjustPending(order.getUser().getId(), -order.getBalanceCentsUsed()); // 释放 pending 余额
        }

        order.setStatus(ShopStatus.CANCELLED);
        orderRepo.save(order);
    }

    /** 发货：写入物流单号，状态不变或从 AWAITING→COMPLETED 的前一步 */
    @Transactional
    public void ship(Long orderId, String trackingNo) {
        ShopOrder order = orderRepo.findById(orderId).orElseThrow(() -> new RuntimeException("订单不存在"));
        order.setTrackingNo(trackingNo);
        orderRepo.save(order);
    }

    /** 完成订单：状态改为 COMPLETED */
    @Transactional
    public void complete(Long orderId) {
        ShopOrder order = orderRepo.findById(orderId).orElseThrow(() -> new RuntimeException("订单不存在"));
        order.setStatus(ShopStatus.COMPLETED);
        orderRepo.save(order);
    }
}
