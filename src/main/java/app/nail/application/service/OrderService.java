package app.nail.application.service;

import app.nail.common.exception.ApiException;
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
import app.nail.infra.payment.PaymentGatewayClient;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Order orchestration (stock, payments, balance, notifications).
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
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final PaymentGatewayClient paymentGatewayClient;
    private final MeterRegistry meterRegistry;

    public record OrderItemDTO(Long productId, Integer qty) {}

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Long createOrder(Long userId, List<OrderItemDTO> items, boolean useBalance, String address, String phone) {
        userRepo.findById(userId).orElseThrow(() -> ApiException.resourceNotFound("User not found"));
        if (items == null || items.isEmpty()) {
            throw ApiException.businessViolation("Order items cannot be empty");
        }

        int total = 0;
        for (OrderItemDTO dto : items) {
            Product product = productRepo.findById(dto.productId())
                    .orElseThrow(() -> ApiException.resourceNotFound("Product not found"));
            total += product.getPriceCents() * dto.qty();
            productService.freezeStock(product.getId(), dto.qty());
        }

        if (useBalance) {
            balanceService.freeze(userId, total);
        }

        PaymentMethod payMethod = useBalance ? PaymentMethod.BALANCE : PaymentMethod.ONLINE;
        ShopOrder order = ShopOrder.builder()
                .user(User.builder().id(userId).build())
                .status(ShopStatus.PENDING_CONFIRM)
                .totalCents(total)
                .address(address)
                .phone(phone)
                .payMethod(payMethod)
                .balanceCentsUsed(useBalance ? total : 0)
                .build();
        order = orderRepo.save(order);

        for (OrderItemDTO dto : items) {
            Product product = productRepo.findById(dto.productId())
                    .orElseThrow(() -> ApiException.resourceNotFound("Product not found"));
            itemRepo.save(OrderItem.builder()
                    .order(order)
                    .product(product)
                    .qty(dto.qty())
                    .unitCents(product.getPriceCents())
                    .status(ShopStatus.PENDING_CONFIRM)
                    .build());
        }

        if (!useBalance) {
            String paymentRef = paymentGatewayClient.initiatePayment(
                    "ORDER", order.getId(), total, Map.of("userId", userId));
            order.setPaymentRef(paymentRef);
            orderRepo.save(order);
        }

        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("status", ShopStatus.PENDING_CONFIRM.name());
        changes.put("totalCents", total);
        changes.put("itemCount", items.size());
        changes.put("payMethod", payMethod.name());
        auditService.log("ShopOrder", order.getId(), "CREATE", changes, Map.of("userId", userId));

        notificationService.notifyOrderEvent(userId, "ORDER_CREATED",
                Map.of("orderId", order.getId(), "totalCents", total));
        meterRegistry.counter("orders.created.total").increment();
        return order.getId();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void confirmOrder(Long orderId) {
        ShopOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> ApiException.resourceNotFound("Order not found"));
        if (order.getStatus() != ShopStatus.PENDING_CONFIRM) {
            throw ApiException.businessViolation("Order is not awaiting confirmation");
        }

        List<OrderItem> items = itemRepo.findByOrderId(orderId);
        for (OrderItem item : items) {
            productService.adjustFrozen(item.getProduct().getId(), -item.getQty());
            productService.confirmDeduct(item.getProduct().getId(), item.getQty());
            item.setStatus(ShopStatus.AWAITING);
            itemRepo.save(item);
        }

        if (order.getPayMethod() == PaymentMethod.BALANCE && order.getBalanceCentsUsed() > 0) {
            balanceService.adjustPending(order.getUser().getId(), -order.getBalanceCentsUsed());
            balanceService.trySpend(order.getUser().getId(), order.getBalanceCentsUsed(), "ORDER", order.getId());
        }
        if (order.getPayMethod() == PaymentMethod.ONLINE && order.getPaymentRef() != null) {
            paymentGatewayClient.capturePayment(order.getPaymentRef(), order.getId(), order.getTotalCents());
        }

        order.setStatus(ShopStatus.AWAITING);
        orderRepo.save(order);

        auditService.log("ShopOrder", orderId, "CONFIRM",
                Map.of("status", ShopStatus.AWAITING.name()),
                Map.of("balanceUsed", order.getBalanceCentsUsed()));
        notificationService.notifyOrderEvent(order.getUser().getId(), "ORDER_CONFIRMED",
                Map.of("orderId", orderId));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void cancelOrder(Long orderId) {
        ShopOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> ApiException.resourceNotFound("Order not found"));
        if (order.getStatus() == ShopStatus.CANCELLED) {
            return;
        }

        List<OrderItem> items = itemRepo.findByOrderId(orderId);
        for (OrderItem item : items) {
            productService.adjustFrozen(item.getProduct().getId(), -item.getQty());
            item.setStatus(ShopStatus.CANCELLED);
            itemRepo.save(item);
        }

        if (order.getPayMethod() == PaymentMethod.BALANCE && order.getBalanceCentsUsed() > 0) {
            balanceService.adjustPending(order.getUser().getId(), -order.getBalanceCentsUsed());
        } else if (order.getPayMethod() == PaymentMethod.ONLINE && order.getPaymentRef() != null) {
            paymentGatewayClient.refundPayment(order.getPaymentRef(), order.getId(),
                    order.getTotalCents(), "ORDER_CANCELLED");
        }

        order.setStatus(ShopStatus.CANCELLED);
        orderRepo.save(order);

        auditService.log("ShopOrder", orderId, "CANCEL",
                Map.of("status", ShopStatus.CANCELLED.name()),
                Map.of("balanceReleased", order.getBalanceCentsUsed()));
        notificationService.notifyOrderEvent(order.getUser().getId(), "ORDER_CANCELLED",
                Map.of("orderId", orderId));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void ship(Long orderId, String trackingNo) {
        ShopOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> ApiException.resourceNotFound("Order not found"));
        order.setTrackingNo(trackingNo);
        orderRepo.save(order);

        auditService.log("ShopOrder", orderId, "SHIP", Map.of("trackingNo", trackingNo), null);
        notificationService.notifyOrderEvent(order.getUser().getId(), "ORDER_SHIPPED",
                Map.of("orderId", orderId, "trackingNo", trackingNo));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void complete(Long orderId) {
        ShopOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> ApiException.resourceNotFound("Order not found"));
        order.setStatus(ShopStatus.COMPLETED);
        orderRepo.save(order);

        auditService.log("ShopOrder", orderId, "COMPLETE",
                Map.of("status", ShopStatus.COMPLETED.name()), null);
        notificationService.notifyOrderEvent(order.getUser().getId(), "ORDER_COMPLETED",
                Map.of("orderId", orderId));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void refundOrder(Long orderId, int amountCents, String reason) {
        if (amountCents <= 0) {
            throw ApiException.businessViolation("Refund amount must be positive");
        }
        ShopOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> ApiException.resourceNotFound("Order not found"));
        if (order.getStatus() != ShopStatus.AWAITING && order.getStatus() != ShopStatus.COMPLETED) {
            throw ApiException.businessViolation("Only awaiting or completed orders can be refunded");
        }
        if (amountCents > order.getTotalCents()) {
            throw ApiException.businessViolation("Refund amount cannot exceed order total");
        }

        List<OrderItem> items = itemRepo.findByOrderId(orderId);
        for (OrderItem item : items) {
            productService.restoreStock(item.getProduct().getId(), item.getQty());
            item.setStatus(ShopStatus.REFUNDED);
            itemRepo.save(item);
        }

        if (order.getPayMethod() == PaymentMethod.BALANCE) {
            balanceService.refund(order.getUser().getId(), amountCents, "ORDER", orderId, reason);
        } else if (order.getPayMethod() == PaymentMethod.ONLINE && order.getPaymentRef() != null) {
            paymentGatewayClient.refundPayment(order.getPaymentRef(), orderId, amountCents, reason);
        }

        order.setStatus(ShopStatus.REFUNDED);
        orderRepo.save(order);

        Map<String, Object> changes = Map.of(
                "status", ShopStatus.REFUNDED.name(),
                "refundCents", amountCents
        );
        Map<String, Object> context = reason == null ? null : Map.of("reason", reason);
        auditService.log("ShopOrder", orderId, "REFUND", changes, context);
        notificationService.notifyOrderEvent(order.getUser().getId(), "ORDER_REFUNDED",
                Map.of("orderId", orderId, "amountCents", amountCents, "reason", reason));
        meterRegistry.counter("orders.refunded.total").increment();
    }
}
