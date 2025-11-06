package app.nail.interfaces.admin.controller;

import app.nail.application.service.OrderService;
import app.nail.domain.entity.OrderItem;
import app.nail.domain.entity.ShopOrder;
import app.nail.domain.enums.ShopStatus;
import app.nail.domain.repository.OrderItemRepository;
import app.nail.domain.repository.ShopOrderRepository;
import app.nail.interfaces.admin.dto.AdminOrderDtos.*;
import app.nail.interfaces.common.dto.PageResp;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** English: Admin order operations. */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;
    private final ShopOrderRepository orderRepo;
    private final OrderItemRepository itemRepo;

    @PostMapping("/{id}/confirm")
    public void confirm(@PathVariable Long id) { orderService.confirmOrder(id); }

    @PostMapping("/{id}/cancel")
    public void cancel(@PathVariable Long id) { orderService.cancelOrder(id); }

    @PostMapping("/{id}/ship")
    public void ship(@PathVariable Long id, @RequestBody ShipReq req) { orderService.ship(id, req.trackingNo()); }

    @PostMapping("/{id}/complete")
    public void complete(@PathVariable Long id) { orderService.complete(id); }

    /** English: Paged all orders; filter by status if provided. */
    @GetMapping
    public PageResp<OrderResp> page(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(required = false) ShopStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ShopOrder> p = (status != null)
                ? orderRepo.findByStatus(status, pageable)
                : orderRepo.findAll(pageable);
        return new PageResp<>(
                p.map(this::toResp).getContent(),
                p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize()
        );
        }

    /** English: Order detail for admin. */
    @GetMapping("/{orderId}")
    public OrderResp detail(@PathVariable Long orderId) {
        ShopOrder o = orderRepo.findById(orderId).orElseThrow(() -> new RuntimeException("order not found"));
        return toResp(o);
    }

    private OrderResp toResp(ShopOrder o) {
        List<OrderItem> items = itemRepo.findByOrderId(o.getId());
        var itemResps = items.stream().map(this::toItemResp).toList();
        return new OrderResp(
                o.getId(), o.getUser().getId(), o.getStatus(),
                o.getTotalCents(), o.getAddress(), o.getPhone(), o.getTrackingNo(),
                o.getCreatedAt(), itemResps
        );
    }

    private ItemResp toItemResp(OrderItem it) {
        return new ItemResp(
                it.getId(), it.getProduct().getId(), it.getProduct().getName(),
                it.getQty(), it.getUnitCents(), it.getLineCents(), it.getStatus()
        );
    }
}
