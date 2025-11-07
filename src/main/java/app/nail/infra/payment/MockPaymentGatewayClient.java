package app.nail.infra.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Mock gateway client that logs operations; swap with real provider in production.
 */
@Component
@Slf4j
public class MockPaymentGatewayClient implements PaymentGatewayClient {

    @Override
    public String initiatePayment(String businessType, Long businessId, int amountCents, Map<String, Object> metadata) {
        String ref = "PAY-" + UUID.randomUUID();
        log.info("Mock initiate payment {} for {}#{} amount={} metadata={}", ref, businessType, businessId, amountCents, metadata);
        return ref;
    }

    @Override
    public void capturePayment(String paymentRef, Long businessId, int amountCents) {
        log.info("Mock capture payment {} for businessId {} amount={}", paymentRef, businessId, amountCents);
    }

    @Override
    public void refundPayment(String paymentRef, Long businessId, int amountCents, String reason) {
        log.info("Mock refund payment {} for businessId {} amount={} reason={}", paymentRef, businessId, amountCents, reason);
    }
}
