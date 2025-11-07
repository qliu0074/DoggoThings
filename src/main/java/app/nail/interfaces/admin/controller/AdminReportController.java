package app.nail.interfaces.admin.controller;

import app.nail.application.service.ReportService;
import app.nail.domain.entity.Consumption;
import app.nail.domain.entity.ShopOrder;
import app.nail.domain.enums.ConsumeType;
import app.nail.interfaces.admin.dto.AdminReportDtos.FlowResp;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

/** English: Simple reporting endpoints. */
@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final ReportService reportService;

    /** English: Recent completed orders. */
    @GetMapping("/orders/recent")
    public List<ShopOrder> recent(@RequestParam(defaultValue = "10") int limit) {
        return reportService.recentCompleted(limit);
    }

    /** English: Balance flows between time range. */
    @GetMapping("/balance/flows")
    public List<FlowResp> flows(@RequestParam long userId,
                                @RequestParam OffsetDateTime from,
                                @RequestParam OffsetDateTime to,
                                @RequestParam ConsumeType type,
                                @RequestParam(defaultValue = "50") int limit) {
        List<Consumption> list = reportService.rangeOf(userId, from, to, type, limit);
        return list.stream().map(c -> new FlowResp(
                c.getId(), c.getUser().getId(), c.getKind(),
                c.getAmountCents(), c.getRefKind(), c.getRefId(), c.getCreatedAt()
        )).toList();
    }
}
