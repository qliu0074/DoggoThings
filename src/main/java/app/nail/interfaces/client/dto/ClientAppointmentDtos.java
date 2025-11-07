package app.nail.interfaces.client.dto;

import app.nail.domain.enums.ApptStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.OffsetDateTime;
import java.util.List;

/** English: Client-side appointment DTOs. */
public class ClientAppointmentDtos {

    /** English: Appointment item request. */
    public record SrvItem(
            @NotNull Long serviceId,
            @NotNull @Positive Integer qty
    ) {}

    /** English: Book appointment request. */
    public record BookReq(
            @NotNull @FutureOrPresent OffsetDateTime time,
            @Valid @NotEmpty List<SrvItem> items,
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

    /** English: Refund request payload. */
    public record RefundReq(
            @NotNull @Positive Integer amountCents,
            String reason
    ) {}
}
