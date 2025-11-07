package app.nail.integration;

import app.nail.application.service.AppointmentService;
import app.nail.domain.entity.Appointment;
import app.nail.domain.entity.AuditLog;
import app.nail.domain.entity.ServiceItem;
import app.nail.domain.entity.User;
import app.nail.domain.enums.ApptStatus;
import app.nail.domain.enums.ProductStatus;
import app.nail.domain.repository.AppointmentRepository;
import app.nail.domain.repository.AuditLogRepository;
import app.nail.domain.repository.ServiceItemRepository;
import app.nail.domain.repository.UserRepository;
import app.nail.integration.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
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
 * Full appointment lifecycle backed by Testcontainers + WireMock.
 */
class AppointmentFlowIT extends IntegrationTestSupport {

    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private ServiceItemRepository serviceItemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void should_book_finish_and_refund_online_appointment() {
        User user = userRepository.save(User.builder()
                .nickname("it-appointment-user")
                .phone("18800001111")
                .build());
        ServiceItem serviceItem = serviceItemRepository.save(ServiceItem.builder()
                .category("spa")
                .priceCents(8800)
                .description("Integration manicure")
                .status(ProductStatus.ON)
                .build());

        stubFor(post(urlEqualTo("/mock-gateway/payments"))
                .withRequestBody(matchingJsonPath("$.businessType", equalTo("APPOINTMENT")))
                .willReturn(okJson("{\"paymentRef\":\"PAY-APPT-IT\"}")));
        stubFor(post(urlEqualTo("/mock-gateway/payments/PAY-APPT-IT/capture"))
                .willReturn(ok()));
        stubFor(post(urlEqualTo("/mock-gateway/payments/PAY-APPT-IT/refund"))
                .willReturn(ok()));
        stubFor(post(urlEqualTo("/mock-gateway/notifications/appointments"))
                .willReturn(ok()));

        OffsetDateTime slot = OffsetDateTime.now().plusDays(1).withNano(0);
        Long appointmentId = appointmentService.book(
                user.getId(),
                slot,
                List.of(new AppointmentService.ServiceItemDTO(serviceItem.getId(), 1)),
                false);

        Appointment booked = appointmentRepository.findById(appointmentId).orElseThrow();
        assertThat(booked.getStatus()).isEqualTo(ApptStatus.UNCONFIRMED);
        assertThat(booked.getPaymentRef()).isEqualTo("PAY-APPT-IT");

        appointmentService.finish(appointmentId);
        Appointment finished = appointmentRepository.findById(appointmentId).orElseThrow();
        assertThat(finished.getStatus()).isEqualTo(ApptStatus.FINISHED);

        appointmentService.refund(appointmentId, booked.getTotalCents(), "ARTIST_UNAVAILABLE");
        Appointment refunded = appointmentRepository.findById(appointmentId).orElseThrow();
        assertThat(refunded.getStatus()).isEqualTo(ApptStatus.REFUNDED);

        assertThat(actionsOf("Appointment", appointmentId))
                .contains("CREATE", "FINISH", "REFUND");

        wireMockServer.verify(postRequestedFor(urlEqualTo("/mock-gateway/payments"))
                .withRequestBody(matchingJsonPath("$.amountCents", equalTo(String.valueOf(booked.getTotalCents())))));
        wireMockServer.verify(postRequestedFor(urlEqualTo("/mock-gateway/payments/PAY-APPT-IT/capture")));
        wireMockServer.verify(postRequestedFor(urlEqualTo("/mock-gateway/payments/PAY-APPT-IT/refund"))
                .withRequestBody(matchingJsonPath("$.reason", equalTo("ARTIST_UNAVAILABLE"))));
        wireMockServer.verify(3, postRequestedFor(urlEqualTo("/mock-gateway/notifications/appointments")));
    }

    private List<String> actionsOf(String entityType, Long entityId) {
        return auditLogRepository.findAll().stream()
                .filter(log -> entityType.equals(log.getEntityType()) && entityId.equals(log.getEntityId()))
                .map(AuditLog::getAction)
                .toList();
    }
}
