package app.nail.interfaces.client.controller;

import app.nail.application.service.AppointmentService;
import app.nail.common.exception.ApiException;
import app.nail.common.security.PrincipalUser;
import app.nail.domain.entity.Appointment;
import app.nail.domain.entity.AppointmentItem;
import app.nail.domain.repository.AppointmentItemRepository;
import app.nail.domain.repository.AppointmentRepository;
import app.nail.interfaces.client.dto.ClientAppointmentDtos.ApptResp;
import app.nail.interfaces.client.dto.ClientAppointmentDtos.BookReq;
import app.nail.interfaces.client.dto.ClientAppointmentDtos.ItemResp;
import app.nail.interfaces.client.dto.ClientAppointmentDtos.RefundReq;
import app.nail.interfaces.common.dto.PageResp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

/** English: Client appointment controller (book/cancel/list/detail). */
@RestController
@RequestMapping("/api/v1/client/appointments")
@RequiredArgsConstructor
@Validated
@Tag(name = "Client Appointments", description = "Client appointment booking and management")
public class ClientAppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentRepository apptRepo;
    private final AppointmentItemRepository itemRepo;

    /** English: Book; convert client DTO -> service DTO. */
    @PostMapping
    @Operation(summary = "Book a new appointment")
    public Long book(@AuthenticationPrincipal PrincipalUser principal,
                     @Valid @RequestBody BookReq req) {
        Long userId = requireUserId(principal);
        var dto = req.items().stream()
                .map(i -> new AppointmentService.ServiceItemDTO(i.serviceId(), i.qty()))
                .toList();
        return appointmentService.book(userId, req.time(), dto, req.freezeBalance());
    }

    /** English: Cancel appointment. */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel appointment")
    public void cancel(@AuthenticationPrincipal PrincipalUser principal,
                       @PathVariable @Positive Long id) {
        Long userId = requireUserId(principal);
        Appointment appt = apptRepo.findById(id)
                .orElseThrow(() -> ApiException.resourceNotFound("预约不存在"));
        ensureOwner(appt.getUser().getId(), userId, "预约");
        appointmentService.cancel(id);
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Request refund for appointment")
    public void refund(@AuthenticationPrincipal PrincipalUser principal,
                       @PathVariable @Positive Long id,
                       @Valid @RequestBody RefundReq req) {
        Long userId = requireUserId(principal);
        Appointment appt = apptRepo.findById(id)
                .orElseThrow(() -> ApiException.resourceNotFound("预约不存在"));
        ensureOwner(appt.getUser().getId(), userId, "预约");
        appointmentService.refund(id, req.amountCents(), req.reason());
    }

    /** English: Paged appointments by time range. */
    @GetMapping
    @Operation(summary = "List appointments in a time range")
    public PageResp<ApptResp> page(@AuthenticationPrincipal PrincipalUser principal,
                                   @RequestParam OffsetDateTime from,
                                   @RequestParam OffsetDateTime to,
                                   @RequestParam(defaultValue = "0") @PositiveOrZero int page,
                                   @RequestParam(defaultValue = "10") @Positive int size) {
        Long userId = requireUserId(principal);
        Pageable pageable = PageRequest.of(page, size, Sort.by("appointmentAt").descending());
        Page<Appointment> p = apptRepo.findByUserIdAndAppointmentAtBetweenOrderByAppointmentAtDesc(userId, from, to, pageable);
        return new PageResp<>(
                p.map(this::toResp).getContent(),
                p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize()
        );
    }

    /** English: Appointment detail. */
    @GetMapping("/{id}")
    @Operation(summary = "Get appointment detail")
    public ApptResp detail(@AuthenticationPrincipal PrincipalUser principal,
                           @PathVariable @Positive Long id) {
        Long userId = requireUserId(principal);
        Appointment a = apptRepo.findById(id).orElseThrow(() -> ApiException.resourceNotFound("预约不存在"));
        ensureOwner(a.getUser().getId(), userId, "预约");
        return toResp(a);
    }

    private ApptResp toResp(Appointment a) {
        List<AppointmentItem> items = itemRepo.findByAppointmentId(a.getId());
        var itemResps = items.stream().map(it -> new ItemResp(
                it.getId(), it.getService().getId(), it.getService().getCategory(),
                it.getQty(), it.getUnitCents(), it.getLineCents()
        )).toList();
        return new ApptResp(a.getId(), a.getStatus(), a.getAppointmentAt(), a.getTotalCents(), itemResps);
    }

    private Long requireUserId(PrincipalUser principal) {
        if (principal == null || principal.id() == null) {
            throw ApiException.unauthorized("未登录或token缺少用户信息");
        }
        return principal.id();
    }

    private void ensureOwner(Long ownerId, Long userId, String resourceName) {
        if (ownerId == null || !ownerId.equals(userId)) {
            throw ApiException.forbidden("无权访问该" + resourceName);
        }
    }
}
