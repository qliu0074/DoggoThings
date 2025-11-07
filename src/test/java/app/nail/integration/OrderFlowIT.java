package app.nail.integration;

import app.nail.application.service.OrderService;
import app.nail.domain.entity.AuditLog;
import app.nail.domain.entity.OrderItem;
import app.nail.domain.entity.Product;
import app.nail.domain.entity.ShopOrder;
import app.nail.domain.entity.User;
import app.nail.domain.enums.ProductStatus;
import app.nail.domain.enums.ShopStatus;
import app.nail.domain.repository.AuditLogRepository;
import app.nail.domain.repository.OrderItemRepository;
import app.nail.domain.repository.ProductRepository;
import app.nail.domain.repository.ShopOrderRepository;
import app.nail.domain.repository.UserRepository;
import app.nail.integration.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack verification of the order lifecycle create → pay → refund.
 */
class OrderFlowIT extends IntegrationTestSupport {

    @Autowired
    private OrderService orderService;
    @Autowired
    private ShopOrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void should_complete_online_order_lifecycle_and_emit_integrations() {
        User user = userRepository.save(User.builder()
                .nickname("it-order-user")
                .phone("18800000000")
                .build());
        Product product = productRepository.save(Product.builder()
                .name("Flow Product")
                .category("nails")
                .priceCents(5200)
                .stockActual(100)
                .stockPending(0)
                .status(ProductStatus.ON)
                .build());

        stubFor(post(urlEqualTo("/mock-gateway/payments"))
                .withRequestBody(matchingJsonPath("$.businessType", equalTo("ORDER")))
                .willReturn(okJson("{\"paymentRef\":\"PAY-ORDER-IT\"}")));
        stubFor(post(urlEqualTo("/mock-gateway/payments/PAY-ORDER-IT/capture"))
                .willReturn(ok()));
        stubFor(post(urlEqualTo("/mock-gateway/payments/PAY-ORDER-IT/refund"))
                .willReturn(ok()));
        stubFor(post(urlEqualTo("/mock-gateway/notifications/orders"))
                .willReturn(ok()));

        Long orderId = orderService.createOrder(
                user.getId(),
                List.of(new OrderService.OrderItemDTO(product.getId(), 2)),
                false,
                "Shanghai Pudong Road",
                "18800000000");

        ShopOrder created = orderRepository.findById(orderId).orElseThrow();
        assertThat(created.getStatus()).isEqualTo(ShopStatus.PENDING_CONFIRM);
        assertThat(created.getPaymentRef()).isEqualTo("PAY-ORDER-IT");
        assertThat(created.getTotalCents()).isEqualTo(10400);

        orderService.confirmOrder(orderId);
        ShopOrder awaiting = orderRepository.findById(orderId).orElseThrow();
        assertThat(awaiting.getStatus()).isEqualTo(ShopStatus.AWAITING);

        Product afterConfirm = productRepository.findById(product.getId()).orElseThrow();
        assertThat(afterConfirm.getStockActual()).isEqualTo(98);
        assertThat(afterConfirm.getStockPending()).isZero();

        orderService.complete(orderId);
        orderService.refundOrder(orderId, awaiting.getTotalCents(), "CLIENT_CHANGED_MIND");

        ShopOrder refunded = orderRepository.findById(orderId).orElseThrow();
        assertThat(refunded.getStatus()).isEqualTo(ShopStatus.REFUNDED);
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        assertThat(items).allMatch(item -> item.getStatus() == ShopStatus.REFUNDED);

        Product afterRefund = productRepository.findById(product.getId()).orElseThrow();
        assertThat(afterRefund.getStockActual()).isEqualTo(100);

        assertThat(actionsOf("ShopOrder", orderId))
                .contains("CREATE", "CONFIRM", "COMPLETE", "REFUND");

        wireMockServer.verify(postRequestedFor(urlEqualTo("/mock-gateway/payments"))
                .withRequestBody(matchingJsonPath("$.amountCents", equalTo("10400"))));
        wireMockServer.verify(postRequestedFor(urlEqualTo("/mock-gateway/payments/PAY-ORDER-IT/capture")));
        wireMockServer.verify(postRequestedFor(urlEqualTo("/mock-gateway/payments/PAY-ORDER-IT/refund"))
                .withRequestBody(matchingJsonPath("$.reason", equalTo("CLIENT_CHANGED_MIND"))));
        wireMockServer.verify(4, postRequestedFor(urlEqualTo("/mock-gateway/notifications/orders")));
    }

    private List<String> actionsOf(String entityType, Long entityId) {
        return auditLogRepository.findAll().stream()
                .filter(log -> entityType.equals(log.getEntityType()) && entityId.equals(log.getEntityId()))
                .map(AuditLog::getAction)
                .toList();
    }
}
