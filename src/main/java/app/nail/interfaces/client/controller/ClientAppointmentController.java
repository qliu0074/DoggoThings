package app.nail.interfaces.client.controller;

import app.nail.application.service.AppointmentService;
import app.nail.domain.entity.Appointment;
import app.nail.domain.entity.AppointmentItem;
import app.nail.domain.repository.AppointmentItemRepository;
import app.nail.domain.repository.AppointmentRepository;
import app.nail.interfaces.client.dto.ClientAppointmentDtos.*;
import app.nail.interfaces.common.dto.PageResp;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

/** English: Client appointment controller (book/cancel/list/detail). */
@RestController
@RequestMapping("/api/client/appointments")
@RequiredArgsConstructor
public class ClientAppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentRepository apptRepo;
    private final AppointmentItemRepository itemRepo;

    /** English: Book; convert client DTO -> service DTO. */
    @PostMapping
    public Long book(@RequestBody BookReq req) {
        var dto = req.items().stream()
                .map(i -> new AppointmentService.ServiceItemDTO(i.serviceId(), i.qty()))
                .toList();
        return appointmentService.book(req.userId(), req.time(), dto, req.freezeBalance());
    }

    /** English: Cancel appointment. */
    @PostMapping("/{id}/cancel")
    public void cancel(@PathVariable Long id) { appointmentService.cancel(id); }

    /** English: Paged appointments by time range. */
    @GetMapping
    public PageResp<ApptResp> page(@RequestParam Long userId,
                                   @RequestParam OffsetDateTime from,
                                   @RequestParam OffsetDateTime to,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("appointmentAt").descending());
        var p = apptRepo.findByUserIdAndAppointmentAtBetweenOrderByAppointmentAtDesc(userId, from, to, pageable);
        return new PageResp<>(
                p.map(this::toResp).getContent(),
                p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize()
        );
    }

    /** English: Appointment detail. */
    @GetMapping("/{id}")
    public ApptResp detail(@PathVariable Long id) {
        Appointment a = apptRepo.findById(id).orElseThrow(() -> new RuntimeException("appointment not found"));
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
}
