package app.nail.application.service;

import app.nail.common.exception.ApiException;
import app.nail.domain.entity.Consumption;
import app.nail.domain.entity.User;
import app.nail.domain.enums.ConsumeType;
import app.nail.domain.repository.ConsumptionRepository;
import app.nail.domain.repository.SavingsCardRepository;
import app.nail.domain.repository.SavingsPendingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Balance domain service for wallet, pending holds and auditing.
 */
@Service
@RequiredArgsConstructor
public class BalanceService {

    private final SavingsCardRepository savingsCardRepo;
    private final SavingsPendingRepository savingsPendingRepo;
    private final ConsumptionRepository consumptionRepo;
    private final AuditService auditService;

    /** Recharge actual balance and log ledger entry. */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void topUp(long userId, int amountCents) {
        if (amountCents <= 0) {
            throw ApiException.businessViolation("充值金额必须为正");
        }
        savingsCardRepo.lockByUserId(userId).orElseThrow(() -> ApiException.resourceNotFound("充值失败：余额卡不存在"));
        int updated = savingsCardRepo.topUp(userId, amountCents);
        if (updated == 0) {
            throw ApiException.resourceNotFound("充值失败：余额卡不存在");
        }
        Consumption record = consumptionRepo.save(Consumption.builder()
                .user(User.builder().id(userId).build())
                .kind(ConsumeType.TOP_UP)
                .amountCents(amountCents)
                .createdAt(OffsetDateTime.now())
                .build());

        Map<String, Object> changes = Map.of(
                "amountCents", amountCents,
                "consumptionId", record.getId()
        );
        auditService.log("SavingsCard", userId, "TOP_UP", changes, null);
    }

    /** Attempt to spend balance; fails on insufficient funds. */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void trySpend(long userId, int amountCents, String refKind, Long refId) {
        if (amountCents <= 0) {
            throw ApiException.businessViolation("扣款金额必须为正");
        }
        savingsCardRepo.lockByUserId(userId).orElseThrow(() -> ApiException.businessViolation("用户余额卡不存在"));
        int ok = savingsCardRepo.trySpend(userId, amountCents);
        if (ok == 0) {
            throw ApiException.businessViolation("余额不足");
        }
        Consumption record = consumptionRepo.save(Consumption.builder()
                .user(User.builder().id(userId).build())
                .kind(ConsumeType.SPEND)
                .amountCents(amountCents)
                .refKind(refKind)
                .refId(refId)
                .createdAt(OffsetDateTime.now())
                .build());

        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("amountCents", amountCents);
        if (refKind != null) {
            changes.put("refKind", refKind);
        }
        if (refId != null) {
            changes.put("refId", refId);
        }
        changes.put("consumptionId", record.getId());
        auditService.log("SavingsCard", userId, "SPEND", changes, null);
    }

    /** Increase pending balance (hold). */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void freeze(long userId, int amountCents) {
        if (amountCents <= 0) {
            throw ApiException.businessViolation("冻结金额必须为正");
        }
        savingsPendingRepo.lockByUserId(userId);
        savingsPendingRepo.freeze(userId, amountCents);
        auditService.log("SavingsPending", userId, "FREEZE",
                Map.of("deltaCents", amountCents), null);
    }

    /** Adjust pending balance; prevents negative values. */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void adjustPending(long userId, int deltaCents) {
        savingsPendingRepo.lockByUserId(userId).orElseThrow(() -> ApiException.businessViolation("冻结记录不存在"));
        int ok = savingsPendingRepo.adjust(userId, deltaCents);
        if (ok == 0) {
            throw ApiException.businessViolation("冻结余额调整失败：结果为负");
        }
        auditService.log("SavingsPending", userId, "ADJUST",
                Map.of("deltaCents", deltaCents), null);
    }

    /** Refund balance back to user and capture reason. */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void refund(long userId, int amountCents, String refKind, Long refId, String reason) {
        if (amountCents <= 0) {
            throw ApiException.businessViolation("退款金额必须为正");
        }
        savingsCardRepo.lockByUserId(userId).orElseThrow(() -> ApiException.resourceNotFound("退款失败：余额卡不存在"));
        int ok = savingsCardRepo.topUp(userId, amountCents);
        if (ok == 0) {
            throw ApiException.businessViolation("退款处理异常");
        }
        Consumption record = consumptionRepo.save(Consumption.builder()
                .user(User.builder().id(userId).build())
                .kind(ConsumeType.REFUND)
                .amountCents(amountCents)
                .refKind(refKind)
                .refId(refId)
                .createdAt(OffsetDateTime.now())
                .build());

        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("amountCents", amountCents);
        changes.put("consumptionId", record.getId());
        if (refKind != null) {
            changes.put("refKind", refKind);
        }
        if (refId != null) {
            changes.put("refId", refId);
        }
        auditService.log("SavingsCard", userId, "REFUND", changes,
                reason == null ? null : Map.of("reason", reason));
    }
}
