package app.nail.integration.support;

import app.nail.application.service.NotificationService;
import app.nail.infra.payment.PaymentGatewayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Overrides external integration clients with HTTP clients targeting the shared WireMock server.
 */
@TestConfiguration
public class TestIntegrationsConfig {

    @Bean
    @Primary
    PaymentGatewayClient testPaymentGatewayClient(RestTemplateBuilder builder,
                                                  @Value("${app.test.wiremock.base-url}") String baseUrl) {
        return new WireMockPaymentGatewayClient(builder, baseUrl);
    }

    @Bean
    @Primary
    NotificationService testNotificationService(RestTemplateBuilder builder,
                                                @Value("${app.test.wiremock.base-url}") String baseUrl) {
        return new WireMockNotificationClient(builder, baseUrl);
    }
}
