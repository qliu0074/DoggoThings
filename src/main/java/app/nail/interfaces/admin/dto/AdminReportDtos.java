package app.nail.interfaces.admin.dto;

import app.nail.domain.enums.ConsumeType;
import java.time.OffsetDateTime;

/** English: Admin-side simple reporting DTOs. */
public class AdminReportDtos {

    /** English: Balance flow row. */
    public record FlowResp(
            Long id,
            Long userId,
            ConsumeType kind,
            Integer amountCents,
            String refKind,
            Long refId,
            OffsetDateTime createdAt
    ) {}
}
