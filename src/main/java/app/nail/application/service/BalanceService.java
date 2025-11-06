package app.nail.application.service;

import app.nail.domain.entity.Consumption;
import app.nail.domain.entity.SavingsCard;
import app.nail.domain.enums.ConsumeType;
import app.nail.domain.repository.ConsumptionRepository;
import app.nail.domain.repository.SavingsCardRepository;
import app.nail.domain.repository.SavingsPendingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import app.nail.domain.entity.User;

import java.time.OffsetDateTime;

/**
 * 余额服务
 * 职责：
 * 1. 充值与扣款（实际余额）
 * 2. 冻结与释放（pending 余额）
 * 3. 记录消费/充值流水
 */
@Service
@RequiredArgsConstructor
public class BalanceService {

    private final SavingsCardRepository savingsCardRepo;
    private final SavingsPendingRepository savingsPendingRepo;
    private final ConsumptionRepository consumptionRepo;

    /** 充值：增加实际余额并记录流水 */
    @Transactional
    public void topUp(long userId, int amountCents) {
        if (amountCents <= 0) throw new IllegalArgumentException("充值金额必须为正");
        int updated = savingsCardRepo.topUp(userId, amountCents);
        if (updated == 0) {
            // 若余额记录未初始化，可在注册时创建；此处简单抛错
            throw new RuntimeException("充值失败：余额卡不存在");
        }
        consumptionRepo.save(Consumption.builder()
                .user(User.builder().id(userId).build())   
                .kind(ConsumeType.TOP_UP)
                .amountCents(amountCents)
                .createdAt(OffsetDateTime.now())
                .build());
    }

    /** 尝试扣款：不足时失败，不产生负数余额 */
    @Transactional
    public void trySpend(long userId, int amountCents, String refKind, Long refId) {
        if (amountCents <= 0) throw new IllegalArgumentException("扣款金额必须为正");
        int ok = savingsCardRepo.trySpend(userId, amountCents);
        if (ok == 0) throw new RuntimeException("余额不足");
        consumptionRepo.save(Consumption.builder()
                .user(User.builder().id(userId).build())   
                .kind(ConsumeType.SPEND)
                .amountCents(amountCents)
                .refKind(refKind)
                .refId(refId)
                .createdAt(OffsetDateTime.now())
                .build());
    }

    /** 冻结余额：下单或预约时先冻结（pending += X） */
    @Transactional
    public void freeze(long userId, int amountCents) {
        if (amountCents <= 0) throw new IllegalArgumentException("冻结金额必须为正");
        savingsPendingRepo.freeze(userId, amountCents);
    }

    /** 调整冻结余额：确认或取消时释放（delta 可为负），不可使 pending 为负 */
    @Transactional
    public void adjustPending(long userId, int deltaCents) {
        int ok = savingsPendingRepo.adjust(userId, deltaCents);
        if (ok == 0) throw new RuntimeException("冻结余额调整失败：结果为负");
    }
}
