package app.nail.interfaces.client.controller;

import app.nail.application.service.OrderService;
import app.nail.common.exception.ApiException;
import app.nail.common.security.PrincipalUser;
import app.nail.domain.entity.OrderItem;
import app.nail.domain.entity.ShopOrder;
import app.nail.domain.repository.OrderItemRepository;
import app.nail.domain.repository.ShopOrderRepository;
import app.nail.interfaces.client.dto.ClientOrderDtos.CreateOrderReq;
import app.nail.interfaces.client.dto.ClientOrderDtos.OrderItemResp;
import app.nail.interfaces.client.dto.ClientOrderDtos.OrderResp;
import app.nail.interfaces.common.PageRequestParams;
import app.nail.interfaces.common.dto.PageResp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** English: Client order controller (create/cancel/list/detail). */
@RestController
@RequestMapping("/api/v1/client/orders")
@RequiredArgsConstructor
@Validated
@Tag(name = "Client Orders", description = "Client-facing order operations")
public class ClientOrderController {

    private final OrderService orderService;
    private final ShopOrderRepository orderRepo;
    private final OrderItemRepository itemRepo;

    /** English: Create order; convert client DTO to service DTO. */
    @PostMapping
    @Operation(summary = "Create a new shop order")
    public Long create(@AuthenticationPrincipal PrincipalUser principal,
                       @Valid @RequestBody CreateOrderReq req) {
        Long userId = requireUserId(principal);
        List<OrderService.OrderItemDTO> dto = req.items().stream()
                .map(i -> new OrderService.OrderItemDTO(i.productId(), i.qty()))
                .toList();
        return orderService.createOrder(userId, dto, req.freezeBalance(), req.address(), req.phone());
    }

    /** English: Cancel order. */
    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel an existing order")
    public void cancel(@AuthenticationPrincipal PrincipalUser principal,
                       @PathVariable @Positive Long orderId) {
        Long userId = requireUserId(principal);
        ShopOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> ApiException.resourceNotFound("订单不存在"));
        ensureOwner(order.getUser().getId(), userId);
        orderService.cancelOrder(orderId);
    }

    /** English: Paged orders of current user. */
    @GetMapping
    @Operation(summary = "List orders for current user")
    public PageResp<OrderResp> my(@AuthenticationPrincipal PrincipalUser principal,
                                  @RequestParam(required = false) Integer page,
                                  @RequestParam(required = false) Integer size) {
        Long userId = requireUserId(principal);
        PageRequestParams params = PageRequestParams.of(page, size);
        Pageable pageable = PageRequest.of(params.page(), params.size(), Sort.by("createdAt").descending());
        Page<ShopOrder> p = orderRepo.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return new PageResp<>(
                p.map(this::toResp).getContent(),
                p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize()
        );
    }

    /** English: Order detail with items. */
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order detail by id")
    public OrderResp detail(@AuthenticationPrincipal PrincipalUser principal,
                            @PathVariable @Positive Long orderId) {
        Long userId = requireUserId(principal);
        ShopOrder o = orderRepo.findById(orderId)
                .orElseThrow(() -> ApiException.resourceNotFound("订单不存在"));
        ensureOwner(o.getUser().getId(), userId);
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

    private Long requireUserId(PrincipalUser principal) {
        if (principal == null || principal.id() == null) {
            throw ApiException.unauthorized("未登录或token缺少用户信息");
        }
        return principal.id();
    }

    private void ensureOwner(Long ownerId, Long userId) {
        if (ownerId == null || !ownerId.equals(userId)) {
            throw ApiException.forbidden("无权访问该订单");
        }
    }
}
