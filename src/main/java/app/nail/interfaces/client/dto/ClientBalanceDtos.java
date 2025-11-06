package app.nail.interfaces.client.dto;

/** English: Client-side balance DTOs. */
public class ClientBalanceDtos {

    /** English: Top-up request. */
    public record TopUpReq(Integer amountCents) {}

    /** English: Savings overview view (joined from v_savings_overview). */
    public record SavingsOverviewResp(
            Long userId,
            Integer balanceCents,
            Integer pendingCents,
            Integer availableCents
    ) {}
}
