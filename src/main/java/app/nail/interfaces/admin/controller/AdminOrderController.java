package app.nail.interfaces.admin.controller;

import app.nail.application.service.OrderService;
import app.nail.common.exception.ApiException;
import app.nail.domain.entity.OrderItem;
import app.nail.domain.entity.ShopOrder;
import app.nail.domain.enums.ShopStatus;
import app.nail.domain.repository.OrderItemRepository;
import app.nail.domain.repository.ShopOrderRepository;
import app.nail.interfaces.admin.dto.AdminOrderDtos.*;
import app.nail.interfaces.common.PageRequestParams;
import app.nail.interfaces.common.dto.PageResp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** English: Admin order operations. */
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
@Tag(name = "Admin Orders", description = "Administrative order management APIs")
public class AdminOrderController {

    private final OrderService orderService;
    private final ShopOrderRepository orderRepo;
    private final OrderItemRepository itemRepo;

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm order and capture payment")
    public void confirm(@PathVariable Long id) { orderService.confirmOrder(id); }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel pending order")
    public void cancel(@PathVariable Long id) { orderService.cancelOrder(id); }

    @PostMapping("/{id}/ship")
    @Operation(summary = "Record shipping information")
    public void ship(@PathVariable Long id, @RequestBody @Valid ShipReq req) { orderService.ship(id, req.trackingNo()); }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Mark order as completed")
    public void complete(@PathVariable Long id) { orderService.complete(id); }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Refund an order")
    public void refund(@PathVariable Long id, @RequestBody @Valid RefundReq req) {
        orderService.refundOrder(id, req.amountCents(), req.reason());
    }

    /** English: Paged all orders; filter by status if provided. */
    @GetMapping
    @Operation(summary = "List orders with pagination")
    public PageResp<OrderResp> page(@RequestParam(required = false) Integer page,
                                    @RequestParam(required = false) Integer size,
                                    @RequestParam(required = false) ShopStatus status) {
        PageRequestParams params = PageRequestParams.of(page, size);
        Pageable pageable = PageRequest.of(params.page(), params.size(), Sort.by("createdAt").descending());
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
    @Operation(summary = "Get order detail by id")
    public OrderResp detail(@PathVariable @Positive Long orderId) {
        ShopOrder o = orderRepo.findById(orderId).orElseThrow(() -> ApiException.resourceNotFound("订单不存在"));
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
