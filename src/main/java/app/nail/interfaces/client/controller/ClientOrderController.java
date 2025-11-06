package app.nail.interfaces.client.controller;

import app.nail.application.service.OrderService;
import app.nail.domain.entity.OrderItem;
import app.nail.domain.entity.ShopOrder;
import app.nail.domain.repository.OrderItemRepository;
import app.nail.domain.repository.ShopOrderRepository;
import app.nail.interfaces.client.dto.ClientOrderDtos.*;
import app.nail.interfaces.common.dto.PageResp;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** English: Client order controller (create/cancel/list/detail). */
@RestController
@RequestMapping("/api/client/orders")
@RequiredArgsConstructor
public class ClientOrderController {

    private final OrderService orderService;
    private final ShopOrderRepository orderRepo;
    private final OrderItemRepository itemRepo;

    /** English: Create order; convert client DTO to service DTO. */
    @PostMapping
    public Long create(@RequestBody CreateOrderReq req) {
        List<OrderService.OrderItemDTO> dto = req.items().stream()
                .map(i -> new OrderService.OrderItemDTO(i.productId(), i.qty()))
                .toList();
        return orderService.createOrder(req.userId(), dto, req.freezeBalance(), req.address(), req.phone());
    }

    /** English: Cancel order. */
    @PostMapping("/{orderId}/cancel")
    public void cancel(@PathVariable Long orderId) { orderService.cancelOrder(orderId); }

    /** English: Paged orders of current user. Replace userId with token-derived id in real auth. */
    @GetMapping
    public PageResp<OrderResp> my(@RequestParam Long userId,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ShopOrder> p = orderRepo.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return new PageResp<>(
                p.map(this::toResp).getContent(),
                p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize()
        );
    }

    /** English: Order detail with items. */
    @GetMapping("/{orderId}")
    public OrderResp detail(@PathVariable Long orderId) {
        ShopOrder o = orderRepo.findById(orderId).orElseThrow(() -> new RuntimeException("order not found"));
        return toResp(o);
    }

    private OrderResp toResp(ShopOrder o) {
        List<OrderItem> items = itemRepo.findByOrderId(o.getId());
        var itemResps = items.stream().map(this::toItemResp).toList();
        return new OrderResp(
                o.getId(), o.getStatus(), o.getTotalCents(),
                o.getAddress(), o.getPhone(), o.getTrackingNo(),
                o.getCreatedAt(), itemResps
        );
    }

    private OrderItemResp toItemResp(OrderItem it) {
        return new OrderItemResp(
                it.getId(),
                it.getProduct().getId(),
                it.getProduct().getName(),
                it.getQty(),
                it.getUnitCents(),
                it.getLineCents(),
                it.getStatus()
        );
    }
}
