package app.nail.application.service;

import app.nail.common.exception.ApiException;
import app.nail.domain.entity.Appointment;
import app.nail.domain.entity.AppointmentItem;
import app.nail.domain.entity.ServiceItem;
import app.nail.domain.entity.User;
import app.nail.domain.enums.ApptStatus;
import app.nail.domain.enums.PaymentMethod;
import app.nail.domain.repository.AppointmentItemRepository;
import app.nail.domain.repository.AppointmentRepository;
import app.nail.domain.repository.ServiceItemRepository;
import app.nail.domain.repository.UserRepository;
import app.nail.infra.payment.PaymentGatewayClient;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Appointment workflow with balance/payment handling and notifications.
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository apptRepo;
    private final AppointmentItemRepository itemRepo;
    private final ServiceItemRepository serviceRepo;
    private final UserRepository userRepo;
    private final BalanceService balanceService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final PaymentGatewayClient paymentGatewayClient;
    private final MeterRegistry meterRegistry;

    /** DTO placeholder matching service-layer contract. */
    public record ServiceItemDTO(Long serviceId, Integer qty) {}

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Long book(Long userId, OffsetDateTime time, List<ServiceItemDTO> items, boolean freezeBalance) {
        userRepo.findById(userId).orElseThrow(() -> ApiException.resourceNotFound("用户不存在"));
        if (apptRepo.existsByUserIdAndAppointmentAt(userId, time)) {
            throw ApiException.conflict("该时段已有预约");
        }
        if (items == null || items.isEmpty()) {
            throw ApiException.businessViolation("预约项目不能为空");
        }

        int total = 0;
        for (ServiceItemDTO dto : items) {
            ServiceItem s = serviceRepo.findById(dto.serviceId()).orElseThrow(() -> ApiException.resourceNotFound("服务不存在"));
            total += s.getPriceCents() * dto.qty();
        }

        if (freezeBalance) {
            balanceService.freeze(userId, total);
        }

        PaymentMethod payMethod = freezeBalance ? PaymentMethod.BALANCE : PaymentMethod.ONLINE;
        Appointment appt = Appointment.builder()
                .user(User.builder().id(userId).build())
                .appointmentAt(time)
                .status(ApptStatus.UNCONFIRMED)
                .totalCents(total)
                .payMethod(payMethod)
                .balanceCentsUsed(freezeBalance ? total : 0)
                .build();
        appt = apptRepo.save(appt);

        for (ServiceItemDTO dto : items) {
            ServiceItem s = serviceRepo.findById(dto.serviceId()).orElseThrow(() -> ApiException.resourceNotFound("服务不存在"));
            AppointmentItem ai = AppointmentItem.builder()
                    .appointment(appt)
                    .service(s)
                    .qty(dto.qty())
                    .unitCents(s.getPriceCents())
                    .build();
            itemRepo.save(ai);
        }

        if (!freezeBalance) {
            String paymentRef = paymentGatewayClient.initiatePayment("APPOINTMENT", appt.getId(), total,
                    Map.of("userId", userId));
            appt.setPaymentRef(paymentRef);
            apptRepo.save(appt);
        }

        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("status", ApptStatus.UNCONFIRMED.name());
        changes.put("totalCents", total);
        changes.put("itemCount", items.size());
        changes.put("payMethod", payMethod.name());
        auditService.log("Appointment", appt.getId(), "CREATE", changes, Map.of("userId", userId));

        notificationService.notifyAppointmentEvent(userId, "APPOINTMENT_BOOKED",
                Map.of("appointmentId", appt.getId(), "time", time));
        meterRegistry.counter("appointments.booked.total").increment();
        return appt.getId();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void finish(Long apptId) {
        Appointment appt = apptRepo.findById(apptId).orElseThrow(() -> ApiException.resourceNotFound("预约不存在"));
        if (appt.getStatus() == ApptStatus.FINISHED) {
            return;
        }
        if (appt.getPayMethod() == PaymentMethod.BALANCE && appt.getBalanceCentsUsed() > 0) {
            balanceService.adjustPending(appt.getUser().getId(), -appt.getBalanceCentsUsed());
            balanceService.trySpend(appt.getUser().getId(), appt.getBalanceCentsUsed(), "APPOINTMENT", appt.getId());
        }
        if (appt.getPayMethod() == PaymentMethod.ONLINE && appt.getPaymentRef() != null) {
            paymentGatewayClient.capturePayment(appt.getPaymentRef(), appt.getId(), appt.getTotalCents());
        }
        appt.setStatus(ApptStatus.FINISHED);
        apptRepo.save(appt);

        auditService.log("Appointment", apptId, "FINISH",
                Map.of("status", ApptStatus.FINISHED.name()),
                Map.of("balanceUsed", appt.getBalanceCentsUsed()));
        notificationService.notifyAppointmentEvent(appt.getUser().getId(), "APPOINTMENT_FINISHED",
                Map.of("appointmentId", apptId));
        meterRegistry.counter("appointments.finished.total").increment();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void cancel(Long apptId) {
        Appointment appt = apptRepo.findById(apptId).orElseThrow(() -> ApiException.resourceNotFound("预约不存在"));
        if (appt.getStatus() == ApptStatus.CANCELLED) {
            return;
        }
        if (appt.getPayMethod() == PaymentMethod.BALANCE && appt.getBalanceCentsUsed() > 0) {
            balanceService.adjustPending(appt.getUser().getId(), -appt.getBalanceCentsUsed());
        } else if (appt.getPayMethod() == PaymentMethod.ONLINE && appt.getPaymentRef() != null) {
            paymentGatewayClient.refundPayment(appt.getPaymentRef(), appt.getId(), appt.getTotalCents(), "APPOINTMENT_CANCELLED");
        }
        appt.setStatus(ApptStatus.CANCELLED);
        apptRepo.save(appt);

        auditService.log("Appointment", apptId, "CANCEL",
                Map.of("status", ApptStatus.CANCELLED.name()),
                Map.of("balanceReleased", appt.getBalanceCentsUsed()));
        notificationService.notifyAppointmentEvent(appt.getUser().getId(), "APPOINTMENT_CANCELLED",
                Map.of("appointmentId", apptId));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void refund(Long apptId, int amountCents, String reason) {
        if (amountCents <= 0) {
            throw ApiException.businessViolation("退款金额必须为正");
        }
        Appointment appt = apptRepo.findById(apptId).orElseThrow(() -> ApiException.resourceNotFound("预约不存在"));
        if (appt.getStatus() != ApptStatus.FINISHED) {
            throw ApiException.businessViolation("仅已完成预约支持退款");
        }

        if (appt.getPayMethod() == PaymentMethod.BALANCE) {
            balanceService.refund(appt.getUser().getId(), amountCents, "APPOINTMENT", apptId, reason);
        } else if (appt.getPayMethod() == PaymentMethod.ONLINE && appt.getPaymentRef() != null) {
            paymentGatewayClient.refundPayment(appt.getPaymentRef(), apptId, amountCents, reason);
        }

        appt.setStatus(ApptStatus.REFUNDED);
        apptRepo.save(appt);

        Map<String, Object> changes = Map.of(
                "status", ApptStatus.REFUNDED.name(),
                "refundCents", amountCents
        );
        Map<String, Object> context = reason == null ? null : Map.of("reason", reason);
        auditService.log("Appointment", apptId, "REFUND", changes, context);
        notificationService.notifyAppointmentEvent(appt.getUser().getId(), "APPOINTMENT_REFUNDED",
                Map.of("appointmentId", apptId, "amountCents", amountCents, "reason", reason));
        meterRegistry.counter("appointments.refunded.total").increment();
    }
}
