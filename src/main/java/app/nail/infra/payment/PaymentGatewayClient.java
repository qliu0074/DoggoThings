package app.nail.infra.payment;

import java.util.Map;

/**
 * Abstraction for interacting with external payment gateway.
 */
public interface PaymentGatewayClient {

    /**
     * Initiate a payment authorization and return gateway reference.
     */
    String initiatePayment(String businessType, Long businessId, int amountCents, Map<String, Object> metadata);

    /**
     * Capture/confirm a previously authorized payment.
     */
    void capturePayment(String paymentRef, Long businessId, int amountCents);

    /**
     * Refund a payment (full or partial).
     */
    void refundPayment(String paymentRef, Long businessId, int amountCents, String reason);
}
