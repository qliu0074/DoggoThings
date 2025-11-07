package app.nail.integration.support;

import app.nail.infra.payment.PaymentGatewayClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Test-only PaymentGatewayClient that hits the shared WireMock server via HTTP.
 */
class WireMockPaymentGatewayClient implements PaymentGatewayClient {

    private static final String BASE_PATH = "/mock-gateway/payments";

    private final RestTemplate restTemplate;

    WireMockPaymentGatewayClient(RestTemplateBuilder builder, String baseUrl) {
        this.restTemplate = builder
                .rootUri(baseUrl)
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(2))
                .build();
    }

    @Override
    public String initiatePayment(String businessType, Long businessId, int amountCents, Map<String, Object> metadata) {
        PaymentInitResponse response = restTemplate.postForObject(
                BASE_PATH,
                new PaymentInitRequest(businessType, businessId, amountCents, sanitize(metadata)),
                PaymentInitResponse.class);
        if (response == null || response.paymentRef() == null) {
            throw new IllegalStateException("Mock gateway did not return paymentRef");
        }
        return response.paymentRef();
    }

    @Override
    public void capturePayment(String paymentRef, Long businessId, int amountCents) {
        restTemplate.postForEntity(
                BASE_PATH + "/{paymentRef}/capture",
                new PaymentCaptureRequest(businessId, amountCents),
                Void.class,
                paymentRef);
    }

    @Override
    public void refundPayment(String paymentRef, Long businessId, int amountCents, String reason) {
        restTemplate.postForEntity(
                BASE_PATH + "/{paymentRef}/refund",
                new PaymentRefundRequest(businessId, amountCents, reason),
                Void.class,
                paymentRef);
    }

    private Map<String, Object> sanitize(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return Collections.emptyMap();
        }
        return new LinkedHashMap<>(payload);
    }

    private record PaymentInitRequest(String businessType, Long businessId, int amountCents, Map<String, Object> metadata) {}
    private record PaymentInitResponse(String paymentRef) {}
    private record PaymentCaptureRequest(Long businessId, int amountCents) {}
    private record PaymentRefundRequest(Long businessId, int amountCents, String reason) {}
}
