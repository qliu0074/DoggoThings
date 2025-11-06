package app.nail.interfaces.client.dto;

import app.nail.domain.enums.ApptStatus;
import java.time.OffsetDateTime;
import java.util.List;

/** English: Client-side appointment DTOs. */
public class ClientAppointmentDtos {

    /** English: Appointment item request. */
    public record SrvItem(Long serviceId, Integer qty) {}

    /** English: Book appointment request. */
    public record BookReq(
            Long userId,
            OffsetDateTime time,
            List<SrvItem> items,
            boolean freezeBalance
    ) {}

    /** English: Appointment item response. */
    public record ItemResp(
            Long id,
            Long serviceId,
            String serviceCategory,
            Integer qty,
            Integer unitCents,
            Integer lineCents
    ) {}

    /** English: Appointment header response. */
    public record ApptResp(
            Long id,
            ApptStatus status,
            OffsetDateTime appointmentAt,
            Integer totalCents,
            List<ItemResp> items
    ) {}
}
