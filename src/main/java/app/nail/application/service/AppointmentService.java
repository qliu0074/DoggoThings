package app.nail.application.service;

import app.nail.domain.entity.*;
import app.nail.domain.enums.ApptStatus;
import app.nail.domain.enums.PaymentMethod;
import app.nail.domain.repository.AppointmentItemRepository;
import app.nail.domain.repository.AppointmentRepository;
import app.nail.domain.repository.ServiceItemRepository;
import app.nail.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 预约服务
 * 职责：
 * 1. 下预约单（校验档期、计算金额、可冻结余额）
 * 2. 修改预约（时间或明细）
 * 3. 状态流转：UNCONFIRMED → PENDING/FINISHED/等
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository apptRepo;
    private final AppointmentItemRepository itemRepo;
    private final ServiceItemRepository serviceRepo;
    private final UserRepository userRepo;
    private final BalanceService balanceService;

    /** DTO 占位：请按你的接口层定义实际 DTO */
    public record ServiceItemDTO(Long serviceId, Integer qty) {}

    /** 下预约单：校验同档期唯一 + 计算总价 + 可选冻结余额 */
    @Transactional
    public Long book(Long userId, OffsetDateTime time, List<ServiceItemDTO> items, boolean freezeBalance) {
        userRepo.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        if (apptRepo.existsByUserIdAndAppointmentAt(userId, time)) {
            throw new RuntimeException("该时段已有预约");
        }

        int total = 0;
        for (ServiceItemDTO dto : items) {
            ServiceItem s = serviceRepo.findById(dto.serviceId()).orElseThrow(() -> new RuntimeException("服务不存在"));
            total += s.getPriceCents() * dto.qty();
        }

        if (freezeBalance) {
            balanceService.freeze(userId, total);
        }

        Appointment appt = Appointment.builder()
                .user(User.builder().id(userId).build())
                .appointmentAt(time)
                .status(ApptStatus.UNCONFIRMED)
                .totalCents(total)
                .payMethod(PaymentMethod.BALANCE)
                .balanceCentsUsed(freezeBalance ? total : 0)
                .build();
        appt = apptRepo.save(appt);

        for (ServiceItemDTO dto : items) {
            ServiceItem s = serviceRepo.findById(dto.serviceId()).orElseThrow(() -> new RuntimeException("服务不存在"));
            AppointmentItem ai = AppointmentItem.builder()
                    .appointment(appt)
                    .service(s)
                    .qty(dto.qty())
                    .unitCents(s.getPriceCents())
                    .build();
            itemRepo.save(ai);
        }
        return appt.getId();
    }

    /** 确认完成：扣除冻结余额（若已冻结），状态改为 FINISHED */
    @Transactional
    public void finish(Long apptId) {
        Appointment appt = apptRepo.findById(apptId).orElseThrow(() -> new RuntimeException("预约不存在"));
        if (appt.getStatus() == ApptStatus.FINISHED) return;
        if (appt.getBalanceCentsUsed() > 0) {
            // 将 pending 转实际扣款：先从 pending 释放，再实际扣
            balanceService.adjustPending(appt.getUser().getId(), -appt.getBalanceCentsUsed());
            balanceService.trySpend(appt.getUser().getId(), appt.getBalanceCentsUsed(), "APPOINTMENT", appt.getId());
        }
        appt.setStatus(ApptStatus.FINISHED);
        apptRepo.save(appt);
    }

    /** 取消预约：释放冻结余额并置状态 CANCELLED */
    @Transactional
    public void cancel(Long apptId) {
        Appointment appt = apptRepo.findById(apptId).orElseThrow(() -> new RuntimeException("预约不存在"));
        if (appt.getBalanceCentsUsed() > 0) {
            balanceService.adjustPending(appt.getUser().getId(), -appt.getBalanceCentsUsed());
        }
        appt.setStatus(ApptStatus.CANCELLED);
        apptRepo.save(appt);
    }
}
