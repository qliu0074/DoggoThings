package app.nail.unit;

import app.nail.application.service.*;
import app.nail.common.exception.ApiException;
import app.nail.domain.entity.Appointment;
import app.nail.domain.entity.User;
import app.nail.domain.enums.ApptStatus;
import app.nail.domain.enums.PaymentMethod;
import app.nail.domain.repository.AppointmentItemRepository;
import app.nail.domain.repository.AppointmentRepository;
import app.nail.domain.repository.ServiceItemRepository;
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

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceFlowTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private AppointmentItemRepository itemRepository;
    @Mock private ServiceItemRepository serviceRepository;
    @Mock private UserRepository userRepository;
    @Mock private BalanceService balanceService;
    @Mock private AuditService auditService;
    @Mock private NotificationService notificationService;
    @Mock private PaymentGatewayClient paymentGatewayClient;
    @Mock private MeterRegistry meterRegistry;
    @Mock private Counter counter;

    @InjectMocks
    private AppointmentService appointmentService;

    @BeforeEach
    void initCounters() {
        lenient().when(meterRegistry.counter(anyString())).thenReturn(counter);
    }

    @Test
    void finishAppointment_spendsBalance() {
        Appointment appt = Appointment.builder()
                .id(5L)
                .user(User.builder().id(10L).build())
                .status(ApptStatus.UNCONFIRMED)
                .balanceCentsUsed(800)
                .payMethod(PaymentMethod.BALANCE)
                .build();
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(appt));

        appointmentService.finish(5L);

        verify(balanceService).adjustPending(10L, -800);
        verify(balanceService).trySpend(10L, 800, "APPOINTMENT", 5L);
        verify(notificationService).notifyAppointmentEvent(10L, "APPOINTMENT_FINISHED",
                Map.of("appointmentId", 5L));
    }

    @Test
    void refundAppointment_rejectsNonFinished() {
        Appointment appt = Appointment.builder()
                .id(12L)
                .user(User.builder().id(30L).build())
                .status(ApptStatus.UNCONFIRMED)
                .totalCents(500)
                .build();
        when(appointmentRepository.findById(12L)).thenReturn(Optional.of(appt));

        assertThatThrownBy(() -> appointmentService.refund(12L, 200, null))
                .isInstanceOf(ApiException.class);
    }
}
