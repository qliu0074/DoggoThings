package app.nail.unit;

import app.nail.application.service.*;
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
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private ShopOrderRepository orderRepo;
    @Mock private OrderItemRepository itemRepo;
    @Mock private ProductRepository productRepo;
    @Mock private UserRepository userRepo;
    @Mock private ProductService productService;
    @Mock private BalanceService balanceService;
    @Mock private AuditService auditService;
    @Mock private NotificationService notificationService;
    @Mock private PaymentGatewayClient paymentGatewayClient;
    @Mock private MeterRegistry meterRegistry;
    @Mock private Counter counter;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void initCounters() {
        lenient().when(meterRegistry.counter(anyString())).thenReturn(counter);
    }

    @Test
    void confirmOrder_deductsStockAndBalance() {
        ShopOrder order = ShopOrder.builder()
                .id(10L)
                .user(User.builder().id(1L).build())
                .status(ShopStatus.PENDING_CONFIRM)
                .payMethod(PaymentMethod.BALANCE)
                .balanceCentsUsed(500)
                .build();
        OrderItem item = OrderItem.builder()
                .id(1L)
                .product(Product.builder().id(100L).build())
                .qty(2)
                .build();

        when(orderRepo.findById(10L)).thenReturn(Optional.of(order));
        when(itemRepo.findByOrderId(10L)).thenReturn(List.of(item));

        orderService.confirmOrder(10L);

        verify(productService).adjustFrozen(100L, -2);
        verify(productService).confirmDeduct(100L, 2);
        verify(balanceService).adjustPending(1L, -500);
        verify(balanceService).trySpend(1L, 500, "ORDER", 10L);
        verify(orderRepo, atLeastOnce()).save(order);
        verify(notificationService).notifyOrderEvent(1L, "ORDER_CONFIRMED", Map.of("orderId", 10L));
    }

    @Test
    void refundOrder_restoresInventory_andDelegatesToGateway() {
        ShopOrder order = ShopOrder.builder()
                .id(99L)
                .user(User.builder().id(3L).build())
                .status(ShopStatus.COMPLETED)
                .totalCents(1200)
                .payMethod(PaymentMethod.ONLINE)
                .paymentRef("PAY-123")
                .build();
        OrderItem item = OrderItem.builder()
                .id(7L)
                .product(Product.builder().id(55L).build())
                .qty(3)
                .build();

        when(orderRepo.findById(99L)).thenReturn(Optional.of(order));
        when(itemRepo.findByOrderId(99L)).thenReturn(List.of(item));

        orderService.refundOrder(99L, 600, "customer_request");

        verify(productService).restoreStock(55L, 3);
        verify(paymentGatewayClient).refundPayment("PAY-123", 99L, 600, "customer_request");
        verify(notificationService).notifyOrderEvent(3L, "ORDER_REFUNDED",
                Map.of("orderId", 99L, "amountCents", 600, "reason", "customer_request"));
    }

    @Test
    void refundOrder_rejectsAmountGreaterThanTotal() {
        ShopOrder order = ShopOrder.builder()
                .id(88L)
                .user(User.builder().id(5L).build())
                .status(ShopStatus.AWAITING)
                .totalCents(500)
                .payMethod(PaymentMethod.BALANCE)
                .build();
        when(orderRepo.findById(88L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.refundOrder(88L, 600, null))
                .isInstanceOf(ApiException.class);
    }
}
